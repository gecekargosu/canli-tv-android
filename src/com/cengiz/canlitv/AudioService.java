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
import android.media.MediaPlayer;
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

    private MediaPlayer player;
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

    public static boolean isPlayingStatic() {
        try {
            return INSTANCE != null && INSTANCE.player != null && INSTANCE.player.isPlaying();
        } catch (Throwable e) { return false; }
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

    @Override
    public void onCreate() {
        super.onCreate();
        registerNoisyReceiver();
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
        ensureSession();
        startForeground(NOTIF_ID, notification());

        if ("next".equals(action)) { step(1); return START_STICKY; }
        if ("prev".equals(action)) { step(-1); return START_STICKY; }
        if ("toggle".equals(action)) { toggle(); return START_STICKY; }

        String url = intent.getStringExtra("url");
        if (url != null) {
            name = intent.getStringExtra("name") == null ? "Umay Multi TV" : intent.getStringExtra("name");
            currentUrl = url;
            requestFocus();
            playUrl(url);
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
        if (channels == null) channels = ChannelRepo.all(this);
        return channels;
    }

    private void step(int d) {
        List<Channel> l = list();
        if (l.isEmpty()) return;
        int idx = -1;
        for (int i = 0; i < l.size(); i++) if (l.get(i).url.equals(currentUrl)) { idx = i; break; }
        // calan sey TV listesinde yoksa (radyo) yanlislikla TV kanalina atlamasin
        if (idx < 0) return;
        idx = (idx + d + l.size()) % l.size();
        Channel c = l.get(idx);
        name = c.name;
        currentUrl = c.url;
        playUrl(c.url);
    }

    private void toggle() {
        try {
            if (player == null) return;
            if (player.isPlaying()) pause(); else resume();
        } catch (Exception ignored) { }
    }

    private void pause() {
        try {
            if (player != null && player.isPlaying()) player.pause();
            paused = true;
            updateState();
            startForeground(NOTIF_ID, notification());
        } catch (Exception ignored) { }
    }

    private void resume() {
        try {
            if (player == null) return;
            requestFocus();
            player.start();
            paused = false;
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
                                                    resumeOnFocus = false;
                                                    pause();
                                                    break;
                                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                                    resumeOnFocus = true;
                                                    pause();
                                                    break;
                                                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                                    if (player != null) player.setVolume(0.2f, 0.2f);
                                                    break;
                                                case AudioManager.AUDIOFOCUS_GAIN:
                                                    if (player != null) player.setVolume(1f, 1f);
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

    private void playUrl(String url) {
        attempt = 0;
        attemptPlay(url);
    }

        /**
     * Yayını açar. Takılırsa **aynı (güvenli) adres** bir kez daha denenir; v9.26'dan
     * itibaren şifresiz http:// adresine düşülmüyor (P0-5g: içerik bütünlüğü).
     */
    private void attemptPlay(final String url) {
        final String candidate = url;
        releasePlayer();
        paused = false;
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            // Tarayıcı UA'sı: bazı CDN'ler aksi hâlde reklam kuşağı klibi döndürüyor.
            player.setDataSource(AudioService.this, android.net.Uri.parse(candidate),
                    Net.playerHeaders());
            player.prepareAsync();
            android.util.Log.i("UmayRadio", "deneme " + attempt + ": " + candidate);
            final MediaPlayer p = player;
            new android.os.Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override public void run() {
                    try {
                        if (p != player) return;
                        boolean playing = false;
                        try { playing = p.isPlaying(); } catch (Exception ignored) { }
                        if (playing) return;
                        if (attempt == 0) {
                            attempt = 1;
                            android.util.Log.w("UmayRadio", "yayin takildi, ayni adres tekrar deneniyor");
                            attemptPlay(currentUrl);
                        } else {
                            android.util.Log.w("UmayRadio", "yayin acilmadi: " + candidate);
                            try { Prefs.addFail(AudioService.this, currentUrl); } catch (Exception ignored) { }
                            notifyText("Yayın açılamadı · başka istasyon dene");
                        }
                    } catch (Exception ignored) { }
                }
            }, 7000);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override public void onPrepared(MediaPlayer mp) {
                        attachEq(mp);
                        mp.start();
                        updateState();
                    try { startForeground(NOTIF_ID, notification()); } catch (Exception ignored) { }
                }
            });
                            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override public boolean onError(MediaPlayer mp, int w, int e) {
                        android.util.Log.e("UmayRadio", "hata what=" + w + " extra=" + e + " " + currentUrl);
                        notifyText("Yayın açılamadı · sonraki kanal için ⏭");
                        return true;
                    }
                });
                android.util.Log.i("UmayRadio", "setDataSource tamam: " + candidate);
        } catch (Exception e) {
            stopSelf();
        }
    }

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

    private void attachEq(MediaPlayer mp) {
        try {
            if (eq != null) { eq.release(); eq = null; }
            eq = new android.media.audiofx.Equalizer(0, mp.getAudioSessionId());
            eq.setEnabled(true);
            short n = eq.getNumberOfBands();
            for (short i = 0; i < n && i < LEVELS.length; i++) {
                if (LEVELS[i] != 0) eq.setBandLevel(i, LEVELS[i]);
            }
        } catch (Throwable ignored) { }
    }

    private void releasePlayer() {
        try {
            if (player != null) { player.reset(); player.release(); player = null; }
        } catch (Exception ignored) { }
    }

    @Override
    public void onDestroy() {
        INSTANCE = null;
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
