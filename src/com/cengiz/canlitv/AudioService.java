package com.cengiz.canlitv;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.List;

/**
 * Ekran kapaliyken / kilitliyken sesli devam eden on plan servisi.
 * Kilit ekranindaki medya bildiriminden durdurma ve kanal degistirme yapilabilir.
 */
public class AudioService extends Service {

    public static final String CHANNEL_ID = "umay_audio";
    private static final int NOTIF_ID = 7;

    // 9.87: MediaPlayer bu cihazda AAC+ (HE-AAC) akışlarında -1004 veriyordu.
    // Media3 ExoPlayer AAC/MP3/ICY akışlarını sorunsuz çözüyor.
    private androidx.media3.exoplayer.ExoPlayer player;
    private MediaSession session;
    private String name = "Umay Multi TV";
    private String currentUrl;
    private List<Channel> channels;
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    private boolean paused = false;

    // P0-5d: ses odağı + kulaklık çıkarma. Önceden yoktu: arama gelince veya kulaklık
    // çıkınca yayın hoparlörden devam ediyor, başka uygulamayla ses üst üste biniyordu.
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private boolean resumeOnFocus = false;
    private BroadcastReceiver noisyReceiver;

    // --- gercek ekolayzir (AudioEffect) ---
    private static android.media.audiofx.Equalizer eq;
    private static final short[] LEVELS = new short[8];
    private static AudioService INSTANCE;

    public static int[] eqRange() {
        try {
            if (eq != null) {
                short[] r = eq.getBandLevelRange();
                return new int[]{r[0], r[1]};
            }
        } catch (Throwable ignored) { }
        return new int[]{-1500, 1500};
    }

    public static short[] eqFreqs() {
        try {
            if (eq != null) {
                short n = eq.getNumberOfBands();
                short[] f = new short[n];
                for (short i = 0; i < n; i++) f[i] = (short) (eq.getCenterFreq(i) / 1000);
                return f;
            }
        } catch (Throwable ignored) { }
        return new short[]{60, 230, 910, 3600, 14000};
    }

    public static short[] eqLevels() { return LEVELS.clone(); }

    public static void eqSet(int band, short level) {
        if (band < 0 || band >= LEVELS.length) return;
        LEVELS[band] = level;
        try {
            if (eq != null && band < eq.getNumberOfBands()) eq.setBandLevel((short) band, level);
        } catch (Throwable ignored) { }
    }

    // --- yayin durumu (ekran bunu okuyup "caliyor mu" anlar) ---
    public static final int S_BUFFERING = 0, S_PLAYING = 1, S_ERROR = 2, S_PAUSED = 3;
    private static volatile int state = S_BUFFERING;
    private static volatile String stateUrl = "";

    public static int stateStatic() { return state; }

    /** Ekranlarda "çalıyor" şeridi için istasyon adı (çalmıyorsa null). */
    public static String nowPlayingName() {
        try {
            if (INSTANCE == null || state == S_ERROR) return null;
            if (state == S_PAUSED) return null;
            return INSTANCE.name;
        } catch (Throwable t) { return null; }
    }
    public static String stateUrlStatic() { return stateUrl; }

    /** Yayin gercekten caliyor mu (tamponlama bitmis, hata yok). */
    public static boolean isPlayingStatic() {
        try {
            return INSTANCE != null && INSTANCE.player != null
                    && state == S_PLAYING && INSTANCE.player.isPlaying();
        } catch (Throwable e) { return false; }
    }

    /**
     * Ses gercekten duyuluyor mu? Canli akista BUFFERING olaylari state'i
     * S_BUFFERING'e cekiyor; bu yuzden durum yazisi "bağlanıyor…" da kaliyordu (9.85).
     */
    public static boolean isAudibleStatic() {
        try {
            return INSTANCE != null && INSTANCE.player != null
                    && INSTANCE.preparedOk && INSTANCE.player.isPlaying();
        } catch (Throwable e) { return false; }
    }

    /** Yayin hala kurtarilabilir mi (baglaniyor / yeniden deneniyor). */
    public static boolean isConnectingStatic() {
        return state == S_BUFFERING;
    }

    public static void toggleStatic() {
        if (INSTANCE != null) INSTANCE.toggle();
    }

    public static void start(Context ctx, String url, String channelName) {
        Intent i = new Intent(ctx, AudioService.class);
        i.putExtra("url", url);
        i.putExtra("name", channelName);
        if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i);
        else ctx.startService(i);
    }

    public static void stop(Context ctx) {
        ctx.stopService(new Intent(ctx, AudioService.class));
    }

    /**
     * Uyku zamanlayıcısı. HATA (tur 2): sayaç ekranda tutuluyordu; kullanıcı listeye
     * dönünce (onDestroy) iptal oluyor ve radyo sabaha kadar çalıyordu. Sayaç artık
     * servisde: ekran kapansa da süre sonunda yayın durur. 0 = iptal.
     */
    public static void sleepIn(Context ctx, long ms) {
        pendingSleep = ms;
        if (INSTANCE == null) return;
        Intent i = new Intent(ctx, AudioService.class);
        i.putExtra("action", "sleep");
        i.putExtra("sleepMs", ms);
        if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i);
        else ctx.startService(i);
    }

    /** Kalan uyku süresi (ms); sayaç yoksa 0. */
    public static long sleepLeft() {
        return INSTANCE == null || INSTANCE.sleepAt <= 0 ? 0
                : Math.max(0, INSTANCE.sleepAt - System.currentTimeMillis());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerNoisyReceiver();
        ui.removeCallbacks(usageTick);
        ui.post(usageTick);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        String action = intent.getStringExtra("action");
        // Bildirimdeki Durdur: PendingIntent "action=stop" gonderir (eskiden yalniz
        // boolean extra "stop" bekleniyordu, dugme bu yuzden calismiyordu)
        if ("stop".equals(action) || intent.getBooleanExtra("stop", false)) {
            try { if (player != null) player.stop(); } catch (Throwable ignored) { }
            stopSelf();
            return START_NOT_STICKY;
        }

        INSTANCE = this;
        createChannel();
        if (pendingSleep > 0) {
            sleepAt = System.currentTimeMillis() + pendingSleep;
            pendingSleep = 0;
            armSleep();
        }
        ensureSession();
        startForeground(NOTIF_ID, notification());

        if ("sleep".equals(action)) {
            long ms = intent.getLongExtra("sleepMs", 0);
            sleepAt = ms > 0 ? System.currentTimeMillis() + ms : 0;
            armSleep();
            return START_STICKY;
        }
        if ("next".equals(action)) { step(1); return START_STICKY; }
        if ("prev".equals(action)) { step(-1); return START_STICKY; }
        if ("toggle".equals(action)) { toggle(); return START_STICKY; }

        String url = intent.getStringExtra("url");
        if (url != null) {
            name = intent.getStringExtra("name") == null ? "Umay Multi TV" : intent.getStringExtra("name");
            currentUrl = url;
            requestFocus();
            // Radyo akisi (.aac/.mp3) bir playlist degil: varyant cozumlemesi
            // sonsuz akista hic bitmiyordu ve istasyon "bağlanıyor…" da kaliyordu (9.84).
            if (!url.contains(".m3u8")) { playUrl(url); return START_STICKY; }
            // M-24: arka plan/"sadece ses" modunda master playlist verilince ABR 1080p'ye
            // çıkabiliyordu; "en az veri" iddiası yanlıştı. Önce en düşük varyantı seç.
            Quality.resolveLowest(this, url, new Quality.UrlCallback() {
                @Override public void onUrl(final String resolved) {
                    ui.post(new Runnable() {
                        @Override public void run() {
                            if (!url.equals(currentUrl)) return;   // bu arada başka istasyon seçildi
                            playUrl(resolved != null ? resolved : url);
                        }
                    });
                }
            });
        }
        return START_STICKY;
    }

    // ---------- medya oturumu (kilit ekrani kontrolleri) ----------
    private void ensureSession() {
        if (session != null) return;
        session = new MediaSession(this, "UmayMultiTV");
        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new MediaSession.Callback() {
            @Override public void onPlay() { toggle(); }
            @Override public void onPause() { toggle(); }
            @Override public void onStop() { stopSelf(); }
            @Override public void onSkipToNext() { step(1); }
            @Override public void onSkipToPrevious() { step(-1); }
        });
        session.setActive(true);
    }

    private void updateState() {
        if (session == null) return;
        int state = paused ? PlaybackState.STATE_PAUSED : PlaybackState.STATE_PLAYING;
        session.setPlaybackState(new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE
                        | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_STOP
                        | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS)
                .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f).build());
        session.setMetadata(new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, name)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Umay Multi TV")
                .build());
    }

    private List<Channel> list() {
        // M-33: canlı liste yerine kopya — ülke değişince liste boşalırken adım atmasın.
        if (channels == null) channels = ChannelRepo.snapshot(this);
        return channels;
    }

    private void step(int d) {
        List<Channel> l = list();
        if (l.isEmpty()) return;
        int idx = -1;
        for (int i = 0; i < l.size(); i++) if (l.get(i).url.equals(currentUrl)) { idx = i; break; }
        if (idx >= 0) {
            idx = (idx + d + l.size()) % l.size();
            Channel c = l.get(idx);
            name = c.name;
            currentUrl = c.url;
            playUrl(c.url);
            return;
        }
        // HATA (tur 2): radyo çalarken bildirimdeki ⏮⏭ hiçbir şey yapmıyordu
        // (URL TV listesinde yok). Radyo listesinde ara ve orada ilerle.
        RadioRepo.ensure(this);
        int r = -1;
        for (int i = 0; i < RadioRepo.URLS.size(); i++)
            if (RadioRepo.URLS.get(i).equals(currentUrl)) { r = i; break; }
        if (r < 0 || RadioRepo.URLS.isEmpty()) return;
        r = (r + d + RadioRepo.URLS.size()) % RadioRepo.URLS.size();
        name = RadioRepo.NAMES.get(r);
        currentUrl = RadioRepo.URLS.get(r);
        playUrl(currentUrl);
    }

    private void toggle() {
        try {
            if (player == null) return;
            if (player.isPlaying()) pause(); else resume();
        } catch (Exception ignored) { }
    }

    private void pause() {
        ui.removeCallbacks(watchdog);
        try {
            if (player != null && player.isPlaying()) player.pause();
            paused = true;
            state = S_PAUSED;
            updateState();
            startForeground(NOTIF_ID, notification());
        } catch (Exception ignored) { }
    }

    private void resume() {
        try {
            if (player == null) return;
            requestFocus();
            player.play();
            paused = false;
            state = S_PLAYING;
            updateState();
            startForeground(NOTIF_ID, notification());
        } catch (Exception ignored) { }
    }

    /** Ses odağı ister: arama/başka uygulama sesi öne geçebilsin. */
    private void requestFocus() {
        try {
            if (audioManager == null) audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager == null) return;
            if (focusRequest == null) {
                focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                        .setOnAudioFocusChangeListener(
                                new AudioManager.OnAudioFocusChangeListener() {
                                    @Override public void onAudioFocusChange(int change) {
                                        try {
                                            switch (change) {
                                                case AudioManager.AUDIOFOCUS_LOSS:
                                                    // Reklam/video gibi geçici sahiplik: odak geri gelince devam et.
                                                    resumeOnFocus = true;
                                                    pause();
                                                    break;
                                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                                    resumeOnFocus = true;
                                                    pause();
                                                    break;
                                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                                    if (player != null) player.setVolume(0.2f);
                                                    break;
                                                case AudioManager.AUDIOFOCUS_GAIN:
                                                    if (player != null) player.setVolume(1f);
                                                    if (resumeOnFocus) { resumeOnFocus = false; resume(); }
                                                    break;
                                            }
                                        } catch (Exception ignored) { }
                                    }
                                }).build();
            }
            audioManager.requestAudioFocus(focusRequest);
        } catch (Throwable ignored) { }
    }

    private void abandonFocus() {
        try {
            if (audioManager != null && focusRequest != null)
                audioManager.abandonAudioFocusRequest(focusRequest);
        } catch (Throwable ignored) { }
    }

    /** Kulaklık çıkınca yayını duraklat (hoparlörden yüksek sesle devam etmesin). */
    private void registerNoisyReceiver() {
        if (noisyReceiver != null) return;
        noisyReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                if (i != null && AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(i.getAction())) {
                    resumeOnFocus = false;
                    pause();
                }
            }
        };
        try {
            registerReceiver(noisyReceiver,
                    new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        } catch (Throwable ignored) { }
    }

    private int attempt = 0;
    /** Zayıf internette kaç kez yeniden bağlanılacağı. */
    private static final int MAX_ATTEMPTS = 3;
    /** Yavaş bağlantıda ilk bağlanma için tanınan süre. */
    private static final long START_GRACE_MS = 30000L;
    private boolean preparedOk = false;
    private boolean retryPending = false;
    private int passiveTries = 0;
    private long playStartedAt = 0;
    private final android.os.Handler ui = new android.os.Handler(android.os.Looper.getMainLooper());
    private long sleepAt = 0;
    private static long pendingSleep = 0;
    private final Runnable sleepStop = new Runnable() {
        @Override public void run() {
            sleepAt = 0;
            notifyText("Uyku zamanlayıcı: yayın durduruldu");
            try { if (player != null) player.stop(); } catch (Throwable ignored) { }
            stopSelf();
        }
    };

    private void armSleep() {
        ui.removeCallbacks(sleepStop);
        if (sleepAt <= 0) return;
        long left = sleepAt - System.currentTimeMillis();
        if (left <= 0) { ui.post(sleepStop); return; }
        ui.postDelayed(sleepStop, left);
    }
    private long lastPos = -1;
    private long lastRx = -1;
    private int stallTicks = 0;
    // --- M-25: radyo/arka plan veri sayacı ---
    private long pendingBytes = 0;
    private long usageStartedAt = 0;
    private boolean limitWarned = false;

    /**
     * Yayın gerçekten ilerliyor mu? MediaPlayer internet kesilince de isPlaying()=true
     * diyebiliyor; bu yüzden konum ilerlemesini izliyoruz (12 sn ilerlemezse takıldı).
     */
    private final Runnable monitor = new Runnable() {
        @Override public void run() {
            androidx.media3.exoplayer.ExoPlayer p = player;
            try {
                if (p != null && state == S_PLAYING && p.isPlaying()) {
                    long pos = p.getCurrentPosition();
                    // Veri sayımı artık monitor'den bağımsız: usageTick() (9.87).
                    if (pos == lastPos) {
                        stallTicks++;
                        if (stallTicks >= 3) {
                            stallTicks = 0;
                            lastPos = -1;
                            state = S_BUFFERING;
                            CrashLog.event(AudioService.this, "radio", "yayin takildi (12 sn): " + currentUrl);
                            notifyText("Bağlantı sorunu · yeniden bağlanıyorum");
                            scheduleRetry("Yayın takıldı");
                            return;
                        }
                    } else {
                        stallTicks = 0;
                        lastPos = pos;
                    }
                } else {
                    stallTicks = 0;
                    lastPos = -1;
                }
            } catch (Throwable ignored) { }
            ui.postDelayed(this, 4000L);
        }
    };

    private final Runnable watchdog = new Runnable() {
        @Override public void run() {
            androidx.media3.exoplayer.ExoPlayer p = player;
            if (p == null) return;
            boolean playing = false;
            try { playing = p.isPlaying(); } catch (Exception ignored) { }
            if (playing) { preparedOk = true; return; }
            long waited = android.os.SystemClock.elapsedRealtime() - playStartedAt;
            // Yavaş internet: yayın henüz hazırlanıyorsa hemen "başarısız" sayma.
            if (!preparedOk && waited < START_GRACE_MS) {
                android.util.Log.i("UmayRadio", "yavas baglanti, bekleniyor (" + waited + " ms)");
                notifyText("Bağlantı yavaş · bekleniyor…");
                ui.postDelayed(this, 5000L);
                return;
            }
            android.util.Log.w("UmayRadio", "yayin takildi, waited=" + waited);
            scheduleRetry("Bağlantı zayıf");
        }
    };

    /** Biriken radyo baytlarını/saniyelerini kaydet ve günlük limiti uygula (M-25). */
    private void flushUsage() {
        long bytes = pendingBytes;
        pendingBytes = 0;
        int sec = 0;
        if (usageStartedAt > 0) {
            sec = (int) ((System.currentTimeMillis() - usageStartedAt) / 1000);
            usageStartedAt = System.currentTimeMillis();
        }
        try {
            if ((bytes > 0 || sec > 2) && currentUrl != null)
                Prefs.addUsage(this, currentUrl, bytes, sec);
        } catch (Throwable ignored) { }
        checkLimit();
    }

    /** Günlük limit dolduysa radyoyu durdur ve kullanıcıya söyle. */
    private void checkLimit() {
        try {
            final int lim = Prefs.limitMb(this);
            if (lim <= 0 || limitWarned) return;
            if (Prefs.todayBytes(this) < lim * 1048576L) return;
            limitWarned = true;
            CrashLog.event(this, "limit", "radyo: günlük veri limiti doldu (" + lim + " MB)");
            notifyText("Günlük veri limiti doldu · radyo durduruldu");
            try { if (player != null) player.pause(); } catch (Throwable ignored) { }
            paused = true;
            state = S_PAUSED;
        } catch (Throwable ignored) { }
    }

    private void playUrl(String url) {
        flushUsage();
        usageStartedAt = System.currentTimeMillis();
        limitWarned = false;
        ui.removeCallbacks(watchdog);
        attempt = 0;
        passiveTries = 0;
        retryPending = false;
        httpsTried = url != null && url.startsWith("https://");
        attemptPlay(url);
    }

    /** http:// adres bir kez de https:// olarak denendi mi? */
    private boolean httpsTried = false;

    /**
     * Zayıf bağlantı: istasyonu hemen bırakmak yerine aynı adrese geri dön.
     * 3 deneme sonunda vazgeçip kullanıcıya bilgi ver (radyo ekranı sıradakine geçer).
     */
    private void scheduleRetry(final String reason) {
        if (retryPending) return;
        if (attempt < MAX_ATTEMPTS - 1) {
            attempt++;
            notifyText(reason + " · yeniden bağlanılıyor (" + attempt + "/" + (MAX_ATTEMPTS - 1) + ")");
            state = S_BUFFERING;
            long delay = 8000L;
            android.util.Log.w("UmayRadio", "yeniden baglanma " + attempt + " gecikme=" + delay);
            retryPending = true;
            ui.postDelayed(new Runnable() {
                @Override public void run() { retryPending = false; attemptPlay(currentUrl); }
            }, delay);
        } else {
            // İnternet geri gelebilir: istasyonu "ölü" işaretlemeden sessizce beklemeye devam et.
            if (passiveTries < 12) {
                passiveTries++;
                notifyText("Bağlantı bekleniyor · otomatik yeniden denenecek");
                retryPending = true;
                ui.postDelayed(new Runnable() {
                    @Override public void run() {
                        retryPending = false;
                        attempt = 0;
                        attemptPlay(currentUrl);
                    }
                }, 15000L);
            } else {
                notifyText("Yayın açılamadı (bağlantı) · ⏭ ile başka istasyon");
            }
        }
    }

        /**
     * Yayını açar. Takılırsa **aynı (güvenli) adres** bir kez daha denenir; v9.26'dan
     * itibaren şifresiz http:// adresine düşülmüyor (P0-5g: içerik bütünlüğü).
     */
    /**
     * 9.87: Radyo Media3 ExoPlayer ile calar. MediaPlayer bu cihazda AAC+ (HE-AAC)
     * akislarinda -1004 verip hic acmiyordu; ExoPlayer AAC/MP3/ICY destekliyor.
     */
    private void attemptPlay(final String url) {
        final String candidate = url;
        releasePlayer();
        paused = false;
        preparedOk = false;
        state = S_BUFFERING;
        stateUrl = candidate;
        playStartedAt = android.os.SystemClock.elapsedRealtime();
        ui.removeCallbacks(watchdog);
        CrashLog.event(this, "radio", "baglaniyor: " + candidate);
        try {
            final androidx.media3.exoplayer.ExoPlayer ex =
                    new androidx.media3.exoplayer.ExoPlayer.Builder(this).build();
            player = ex;
            ex.setAudioAttributes(new androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC).build(), true);
            ex.addListener(new androidx.media3.common.Player.Listener() {
                @Override public void onPlaybackStateChanged(int st) {
                    if (player != ex) return;
                    if (st == androidx.media3.common.Player.STATE_READY) {
                        onPreparedOnce();
                    } else if (st == androidx.media3.common.Player.STATE_BUFFERING) {
                        if (!preparedOk) state = S_BUFFERING;
                    } else if (st == androidx.media3.common.Player.STATE_ENDED) {
                        scheduleRetry("Yayın bitti");
                    }
                }
                @Override public void onPlayerError(androidx.media3.common.PlaybackException e) {
                    if (player != ex) return;
                    state = S_ERROR;
                    CrashLog.event(AudioService.this, "radio",
                            "hata " + e.errorCode + " " + candidate);
                    scheduleRetry("Yayın kesildi");
                }
            });
            ex.setMediaItem(androidx.media3.common.MediaItem.fromUri(candidate));
            ex.prepare();
            ex.play();
            android.util.Log.i("UmayRadio", "deneme " + attempt + ": " + candidate);
            ui.removeCallbacks(watchdog);
            ui.postDelayed(watchdog, 8000L);
        } catch (Exception e) {
            android.util.Log.e("UmayRadio", "kurulamadi: " + e);
            stopSelf();
        }
    }

    /** Yayin ilk kez hazir oldu (tek sefer calisir). */
    private void onPreparedOnce() {
        if (preparedOk) return;
        preparedOk = true;
        state = S_PLAYING;
        attachEq();
        usageStartedAt = System.currentTimeMillis();
        lastPos = -1;
        stallTicks = 0;
        armSleep();
        updateState();
        try { startForeground(NOTIF_ID, notification()); } catch (Exception ignored) { }
        ui.removeCallbacks(monitor);
        ui.postDelayed(monitor, 4000L);
        CrashLog.event(this, "radio", "hazir: " + currentUrl);
    }

    // --- M-25: radyo veri sayaci. Durumdan bagimsiz calisir; arka planda da artar (9.87).
    private long lastUsageRx = -1;
    private final Runnable usageTick = new Runnable() {
        @Override public void run() {
            try {
                long rx = android.net.TrafficStats.getUidRxBytes(android.os.Process.myUid());
                if (lastUsageRx < 0 || rx < lastUsageRx) lastUsageRx = rx;
                long delta = rx - lastUsageRx;
                if (delta > 0) {
                    lastUsageRx = rx;
                    pendingBytes += delta;
                    if (pendingBytes > 262144L) flushUsage();
                }
            } catch (Throwable ignored) { }
            ui.postDelayed(this, 5000L);
        }
    };

    @SuppressWarnings("deprecation")
    private void locks() {
        try {
            if (wakeLock == null) {
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "umay:audio");
                wakeLock.setReferenceCounted(false);
                // P0-5f: süresiz kilit pili bitiriyordu; en fazla 3 saat tut.
                wakeLock.acquire(3L * 60L * 60L * 1000L);
            }
            if (wifiLock == null) {
                WifiManager wm = (WifiManager) getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
                if (wm != null) {
                    wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "umay:wifi");
                    wifiLock.setReferenceCounted(false);
                    wifiLock.acquire();   // servis kapanınca onDestroy'da bırakılır
                }
            }
        } catch (Exception ignored) { }
    }

    private void createChannel() {
        locks();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Umay ses yayını",
                    NotificationManager.IMPORTANCE_LOW);
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
    }

    private PendingIntent action(String act, int code) {
        Intent i = new Intent(this, AudioService.class);
        i.putExtra("action", act);
        return PendingIntent.getService(this, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private Notification notification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent prev = action("prev", 11);
        PendingIntent next = action("next", 12);
        PendingIntent toggle = action("toggle", 13);
        PendingIntent stop = action("stop", 14);

        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        Notification.Action aPrev = new Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_media_previous), "Önceki", prev).build();
        Notification.Action aNext = new Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_media_next), "Sonraki", next).build();
        Notification.Action aToggle = new Notification.Action.Builder(
                Icon.createWithResource(this, paused ? android.R.drawable.ic_media_play
                        : android.R.drawable.ic_media_pause), paused ? "Çal" : "Duraklat", toggle).build();
        Notification.Action aStop = new Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel), "Durdur", stop).build();

        b.addAction(aPrev).addAction(aToggle).addAction(aNext).addAction(aStop);

        Notification.MediaStyle style = new Notification.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2);
        if (session != null) style.setMediaSession(session.getSessionToken());
        b.setStyle(style);

        b.setContentTitle(name)
                .setContentText("Ekran kapalıyken de çalıyor · ⏮ ⏭ ile kanal değiştir")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setOngoing(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC);
        return b.build();
    }

    private void notifyText(String text) {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder b = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(AudioService.this, CHANNEL_ID)
                    : new Notification.Builder(AudioService.this);
            nm.notify(NOTIF_ID, b.setContentTitle(name)
                    .setContentText(text)
                    .setSmallIcon(R.mipmap.ic_launcher).build());
        } catch (Exception ignored) { }
    }

    private void attachEq() {
        try {
            if (eq != null) { eq.release(); eq = null; }
            eq = new android.media.audiofx.Equalizer(0, 0);
            eq.setEnabled(true);
            short n = eq.getNumberOfBands();
            for (short i = 0; i < n && i < LEVELS.length; i++) {
                if (LEVELS[i] != 0) eq.setBandLevel(i, LEVELS[i]);
            }
        } catch (Throwable ignored) { }
    }

    private void releasePlayer() {
        try {
            if (player != null) { player.release(); player = null; }
        } catch (Exception ignored) { }
    }

    @Override
    public void onDestroy() {
        INSTANCE = null;
        state = S_ERROR;
        flushUsage();
        ui.removeCallbacks(monitor);
        ui.removeCallbacks(usageTick);
        abandonFocus();
        try { if (noisyReceiver != null) { unregisterReceiver(noisyReceiver); noisyReceiver = null; } }
        catch (Throwable ignored) { }
        try { if (eq != null) { eq.release(); eq = null; } } catch (Throwable ignored) { }
        releasePlayer();
        try { if (session != null) { session.setActive(false); session.release(); session = null; } } catch (Exception ignored) { }
        try { if (wakeLock != null) { wakeLock.release(); wakeLock = null; } } catch (Exception ignored) { }
        try { if (wifiLock != null) { wifiLock.release(); wifiLock = null; } } catch (Exception ignored) { }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
