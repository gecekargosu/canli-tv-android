package com.cengiz.canlitv;

import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Rational;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;

public class PlayerActivity extends android.app.Activity {

    private static final int ACCENT = Color.parseColor("#FF7A18");
    private static final int TEXT = Color.parseColor("#FFFFFF");
    private static final int MUTED = Color.parseColor("#B9C0CE");

    private List<Channel> list;
    /** Ust uste kac olu kanal otomatik atlandi (sonsuz dongu olmasin). */
    private int autoSkips = 0;
    /** #42: ayni kanal icin denenen yedek kaynak sayisi. */
    private int fbTried = 0;
    private int index;
    private androidx.media3.ui.PlayerView video;
    private androidx.media3.exoplayer.ExoPlayer player;
    /** Ekonomi modunda ekran kapanınca görüntüyü bırakıp sadece sese geçmek için. */
    private android.content.BroadcastReceiver screenOff;
    /** İlk READY bir kez işlensin (yeniden bağlanmada tekrar tetiklenir). */
    private boolean ready = false;
    private ProgressBar progress;
    private TextView nameView, errorView, favButton, playButton, counter, recButton, clockBar;
    private LinearLayout top, bottom;
    private HlsRecorder recorder = new HlsRecorder();
    /** MP-15: bu kanalda tercih edilen çözünürlük yok uyarısı bir kez gösterilsin. */
    private int warnedNoVariant = -1;
    private final Handler ui = new Handler();
    private boolean barsVisible = true;
    private long sleepUntil = 0;
    private AudioManager audio;
    private boolean tracking = false;
    /** Bu oturumda oynatıcının ağdan okuduğu bayt (gerçek ölçüm). */
    private final java.util.concurrent.atomic.AtomicLong sessionBytes =
            new java.util.concurrent.atomic.AtomicLong();
    /** Sayaçların yazılacağı kanal adresi. */
    private String usageUrl = null;
    /** M-22: her kanal isteğinde artar; eski isteğin geç gelen cevabı yok sayılır. */
    private volatile int playRequest = 0;
    private long openedAt = 0;
    private boolean limitWarned = false;
    private boolean locked = false;
    private boolean screenOffAudio = false;
    private boolean muted = false;
    /** Donma denetimi: son bilinen konum ve son yeniden başlatma zamanı. */
    private int lastPos = -1;
    private long lastPosAt = 0;
    private long lastRestart = 0;

    private TextView lockHint;
    private TextView muteBtn;
    private TextView qualityBtn;
    /** Yayın duraklama / reklam kuşağı bilgi şeridi. */
    private TextView noticeView;
    private int stallCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        audio = (AudioManager) getSystemService(AUDIO_SERVICE);

        // HATA (tur 4): ChannelRepo.all() statik listeyi döndürüyor; kullanıcı başka ülkeye
        // geçince liste boşalıyor ve arkadaki oynatıcı list.get(index) ile çöküyordu.
        list = new java.util.ArrayList<Channel>(ChannelRepo.all(this));
        index = getIntent().getIntExtra("index", Prefs.lastIndex(this));
        if (index < 0 || index >= list.size()) index = 0;
        // HATA (tur 1): liste boşsa list.get(index) çöküyordu (ülke listesi inmediyse).
        if (list.isEmpty()) {
            Toast.makeText(this, "Kanal listesi boş. Ayarlar → Ülke bölümünden liste indir.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        sleepUntil = Prefs.sleepUntil(this);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        // #107: MediaPlayer yerine ExoPlayer (media3) — HLS, 302 ve hata toparlama daha güvenli.
        androidx.media3.datasource.DefaultHttpDataSource.Factory httpFactory =
                new androidx.media3.datasource.DefaultHttpDataSource.Factory()
                        .setUserAgent(Net.PLAYER_UA)
                        .setDefaultRequestProperties(Net.playerHeaders())
                        .setAllowCrossProtocolRedirects(true);
        // #155: tüketim artık TrafficStats yerine oynatıcının okuduğu baytlardan sayılıyor.
        CountingDataSource.Sink sink = new CountingDataSource.Sink() {
            @Override public void add(long n) { sessionBytes.addAndGet(n); }
        };
        player = new androidx.media3.exoplayer.ExoPlayer.Builder(this)
                .setMediaSourceFactory(
                        new androidx.media3.exoplayer.source.DefaultMediaSourceFactory(
                                new CountingDataSource.Factory(httpFactory, sink)))
                .build();
        player.setAudioAttributes(new androidx.media3.common.AudioAttributes.Builder()
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC).build(), true);

        video = new androidx.media3.ui.PlayerView(this);
        video.setUseController(false);
        video.setPlayer(player);
        FrameLayout.LayoutParams vlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vlp.gravity = Gravity.CENTER;
        video.setLayoutParams(vlp);
        root.addView(video);

        progress = new ProgressBar(this);
        FrameLayout.LayoutParams plp = new FrameLayout.LayoutParams(dp(52), dp(52));
        plp.gravity = Gravity.CENTER;
        progress.setLayoutParams(plp);
        root.addView(progress);

        // saydam saat (barlar gizlense de kalir)
        clockBar = new TextView(this);
        clockBar.setTextColor(Color.parseColor("#CCFFFFFF"));
        clockBar.setTextSize(13);
        clockBar.setPadding(dp(8), dp(4), dp(8), dp(4));
        FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.gravity = Gravity.BOTTOM | Gravity.START;
        clp.setMargins(dp(10), 0, 0, dp(78));
        root.addView(clockBar, clp);

        // yari saydam ses dugmesi: barlar gizlense de ekranda kalir
        muteBtn = new TextView(this);
        muteBtn.setText("🔊");
        muteBtn.setTextSize(17);
        muteBtn.setGravity(Gravity.CENTER);
        GradientDrawable mbg = new GradientDrawable();
        mbg.setColor(Color.parseColor("#66000000"));
        mbg.setCornerRadius(dp(24));
        muteBtn.setBackground(mbg);
        FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams(dp(46), dp(46));
        mlp.gravity = Gravity.BOTTOM | Gravity.END;
        mlp.setMargins(0, 0, dp(14), dp(74));
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { toggleMute(); }
        });
        root.addView(muteBtn, mlp);

        // --- yayın durunca gösterilen bilgi şeridi (reklam kuşağı / yeniden bağlanma) ---
        noticeView = new TextView(this);
        noticeView.setTextColor(Color.WHITE);
        noticeView.setTextSize(13);
        noticeView.setGravity(Gravity.CENTER);
        noticeView.setPadding(dp(14), dp(8), dp(14), dp(8));
        GradientDrawable nbg = new GradientDrawable();
        nbg.setColor(Color.parseColor("#D9000000"));
        nbg.setCornerRadius(dp(18));
        noticeView.setBackground(nbg);
        noticeView.setVisibility(View.GONE);
        FrameLayout.LayoutParams nlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        // 9.76: düğmeler büyüdü; bilgi şeridi üst barın altında kalsın (üstüne binmesin).
        nlp.setMargins(dp(12), dp(132), dp(12), 0);
        root.addView(noticeView, nlp);

        // --- ust bar: buyuk, yazili dugmeler ---
        top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(dp(8), dp(6), dp(8), dp(4));
        top.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#E6000000"), Color.parseColor("#00000000")}));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        nameView = new TextView(this);
        nameView.setTextColor(TEXT);
        nameView.setTextSize(15);
        nameView.setTypeface(nameView.getTypeface(), android.graphics.Typeface.BOLD);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        titleRow.addView(nameView);

        TextView close = barButton("✕", "Kapat");
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        titleRow.addView(close);
        top.addView(titleRow);

        HorizontalScrollView barScroll = new HorizontalScrollView(this);
        barScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout barRow = new LinearLayout(this);
        barRow.setOrientation(LinearLayout.HORIZONTAL);
        barRow.setPadding(0, dp(4), 0, dp(2));
        barScroll.addView(barRow);
        top.addView(barScroll);

        TextView homeBtn = barButton("🏠", "Ana sayfa");
        hint(homeBtn, "Ana sayfa: kanal listesine dön");
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { goHome(); }
        });
        barRow.addView(homeBtn);

        favButton = barButton("★", "Favori");
        hint(favButton, "Favorilere ekle / çıkar");
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { toggleFav(); }
        });
        barRow.addView(favButton);

        // Çoklu ekran (ız gara/3'lü) — en çok kullanılan özellik, barın başında dursun
        TextView multiBtn = barButton("▦", "Çoklu");
        hint(multiBtn, "Çoklu ekran: 2-4 kanalı aynı anda izle");
        multiBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent i = new Intent(PlayerActivity.this, MultiActivity.class);
                i.putExtra("start", index);
                startActivity(i);
            }
        });
        barRow.addView(multiBtn);

        qualityBtn = barButton("🎚", QualityDialog.label(this));
        hint(qualityBtn, "Yayın kalitesi: 360p / 480p / 720p · veri tasarrufu");
        qualityBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { chooseQuality(); }
        });
        barRow.addView(qualityBtn);

        recButton = barButton("⏺", "Kayıt");
        hint(recButton, "Yayını telefona kaydet (Movies/TV-Kayitlari)");
        recButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { toggleRecording(); }
        });
        barRow.addView(recButton);

        TextView audioBtn = barButton("🎧", "Sadece ses");
        hint(audioBtn, "Arka planda ses: ekran kapalıyken dinle");
        audioBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { listenInBackground(); }
        });
        barRow.addView(audioBtn);

        TextView pipBtn = barButton("⧉", "PiP");
        hint(pipBtn, "Küçük pencere: başka uygulamanın üstünde izle");
        pipBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!enterPip()) Toast.makeText(PlayerActivity.this,
                        "Küçük pencere için önce kanal açılmalı", Toast.LENGTH_SHORT).show();
            }
        });
        barRow.addView(pipBtn);

        TextView lockBtn = barButton("🔒", "Kilit");
        hint(lockBtn, "Dokunma kilidi: yanlış dokunuşları engeller");
        lockBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setLocked(!locked); }
        });
        barRow.addView(lockBtn);

        FrameLayout.LayoutParams toplp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toplp.gravity = Gravity.TOP;
        root.addView(top, toplp);

        // --- alt bar ---
        bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setPadding(dp(8), dp(6), dp(8), dp(10));
        bottom.setBackground(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{Color.parseColor("#CC000000"), Color.parseColor("#00000000")}));

        TextView prev = smallButton("⏮");
        prev.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { step(-1); }
        });
        bottom.addView(prev);

        playButton = smallButton("⏸");
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (player.isPlaying()) { player.pause(); playButton.setText("▶"); }
                else { player.play(); playButton.setText("⏸"); }
            }
        });
        bottom.addView(playButton);

        TextView next = smallButton("⏭");
        next.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { step(1); }
        });
        bottom.addView(next);

        SeekBar vol = new SeekBar(this);
        vol.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        vol.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
        vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                if (fromUser) audio.setStreamVolume(AudioManager.STREAM_MUSIC, p, 0);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { }
            @Override public void onStopTrackingTouch(SeekBar sb) { }
        });
        LinearLayout.LayoutParams volp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        volp.setMargins(dp(6), 0, dp(6), 0);
        bottom.addView(vol, volp);

        TextView timerBtn = smallButton("⏰");
        timerBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { sleepDialog(); }
        });
        bottom.addView(timerBtn);

        counter = new TextView(this);
        counter.setTextColor(MUTED);
        counter.setTextSize(11);
        counter.setPadding(dp(6), 0, 0, 0);
        bottom.addView(counter);

        FrameLayout.LayoutParams bolp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bolp.gravity = Gravity.BOTTOM;
        root.addView(bottom, bolp);

        errorView = new TextView(this);
        errorView.setTextColor(TEXT);
        errorView.setTextSize(14);
        errorView.setGravity(Gravity.CENTER);
        errorView.setPadding(dp(24), dp(24), dp(24), dp(24));
        errorView.setVisibility(View.GONE);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { play(); }
        });
        root.addView(errorView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        video.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (locked) {
                    Toast.makeText(PlayerActivity.this,
                            "Dokunma kilidi açık · açmak için ekrana uzun bas", Toast.LENGTH_SHORT).show();
                    return;
                }
                toggleBars();
            }
        });
        video.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                if (locked) setLocked(false);
                return true;
            }
        });

        lockHint = new TextView(this);
        lockHint.setText("🔒 dokunma kilidi · açmak için uzun bas");
        lockHint.setTextColor(Color.parseColor("#CCFFFFFF"));
        lockHint.setTextSize(11);
        lockHint.setPadding(dp(10), dp(4), dp(10), dp(4));
        FrameLayout.LayoutParams lhlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lhlp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lhlp.setMargins(0, 0, 0, dp(16));
        lockHint.setVisibility(View.GONE);
        root.addView(lockHint, lhlp);

        // --- alt bar: once reklamsiz serit, sonra banner ---
        // ONEMLI: bunlar video katmaninin (FrameLayout root) ICINE eklenmez; ayri bir
        // dikey kapta videonun ALTINDA durur ve sistem gezinme cubugunun ustunde kalir.
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        outer.addView(root);
        // Reklam kuralı (v9.25): kanal başına "20 dk reklamsız" şeridi kaldırıldı.
        // Banner her zaman görünür; geçiş reklamı her kanal değişiminde ve 15 dakikada bir.
        android.view.View adBar = AdManager.banner(this);
        if (adBar != null) outer.addView(adBar);
        outer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public android.view.WindowInsets onApplyWindowInsets(View v,
                                                                              android.view.WindowInsets insets) {
                // P0-6: üst (çentik/status bar) + alt (gezinme çubuğu) inset
                int t = insets.getSystemWindowInsetTop();
                int b = insets.getSystemWindowInsetBottom();
                if (t > 0 || b > 0) v.setPadding(0, t, 0, b);
                return insets;
            }
        });
        setContentView(outer);
        watchNetwork();

        // Ekonomi modu: ekran kapanınca video indirmeyi durdur, sadece sesi sürdür.
        screenOff = new android.content.BroadcastReceiver() {
            @Override public void onReceive(android.content.Context c, android.content.Intent i) {
                try {
                    if (!Prefs.economy(PlayerActivity.this)) return;
                    if (!player.isPlaying()) return;
                    if (index < 0 || index >= list.size()) return;
                    Channel ch = list.get(index);
                    String q = Prefs.quality(PlayerActivity.this, ch.url);
                    AudioService.start(PlayerActivity.this, q != null ? q : ch.url, ch.name);
                    screenOffAudio = true;
                    player.pause();
                } catch (Exception ignored) { }
            }
        };
        try {
            registerReceiver(screenOff,
                    new android.content.IntentFilter(android.content.Intent.ACTION_SCREEN_OFF));
        } catch (Throwable ignored) { }

        player.addListener(new androidx.media3.common.Player.Listener() {
            @Override public void onPlaybackStateChanged(int state) {
                if (state == androidx.media3.common.Player.STATE_READY) onReady();
            }

            @Override public void onPlayerError(androidx.media3.common.PlaybackException e) {
                // Hata günlüğü: teknik kod, kullanıcının gördüğü mesajdan bağımsız.
                lastError = e == null ? "?" : (e.getErrorCodeName() + " · " + e.getMessage());
                CrashLog.event(PlayerActivity.this, "tv", "oynatıcı hatası: " + lastError);
                onPError();
            }

            @Override public void onVideoSizeChanged(androidx.media3.common.VideoSize size) {
                // Etiket yalnız tercihi gösteriyordu ("360p"); tek varyantlı kanalda yayın
                // 1080p geliyor ve etiket yalan söylüyordu. Gerçek çözünürlüğü yaz.
                if (size != null && size.height > 0) {
                    showRealQuality(size.height);
                    noteVariantGap(size.height);
                }
            }
        });

        show();

        ui.post(tick);
    }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            String saat = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    .format(new java.util.Date());
            String s = saat;
            if (sleepUntil > 0) {
                long kalan = sleepUntil - System.currentTimeMillis();
                if (kalan <= 0) {
                    sleepUntil = 0;
                    Prefs.setSleepUntil(PlayerActivity.this, 0);
                    Toast.makeText(PlayerActivity.this, "Uyku zamanlayıcısı: kapatılıyor", Toast.LENGTH_SHORT).show();
                    AudioService.stop(PlayerActivity.this);
                    finish();
                    return;
                }
                long s2 = kalan / 1000;
                s = saat + "   ⏰ " + String.format(java.util.Locale.getDefault(), "%d:%02d", s2 / 60, s2 % 60);
            }
            clockBar.setText(s + "\n📶 " + Net.mb(Prefs.todayBytes(PlayerActivity.this)
                    + sessionBytes.get()) + " bugün");
            if (sessionBytes.get() > 2097152L) flushUsage();
            checkLimit();
            if (AdManager.isShowingFullScreen()) {
                if (!adPaused) {
                    adPaused = true;
                    try { player.pause(); } catch (Throwable ignored) { }
                    ui.removeCallbacks(watchdog);
                }
            } else if (adPaused) {
                // Reklam kapandı: kaldığı yerden devam etmek yerine canlıya yeniden bağlan.
                adPaused = false;
                sameRetries = 0;
                stallCount = 0;
                showNotice("✅ Reklam kapandı · yayına dönülüyor");
                ui.postDelayed(new Runnable() {
                    @Override public void run() { hideNotice(); }
                }, 3000);
                play();
            }
            checkStall();
            AdManager.maybeTimerAd(PlayerActivity.this);
            ui.postDelayed(this, 1000);
        }
    };

    /**
     * Donan yayını kurtar: konum 15 saniye ilerlemezse oynatıcıyı taze playlist ile
     * yeniden başlatır (canlı kenara döner). TV8'in uzun reklam kuşağında da kendini toparlar.
     */
    /** ExoPlayer hazır: kullanıcıya göster, sayaçları sıfırla. */
    private void onReady() {
        if (ready) return;
        ready = true;
        everPlayed = true;   // M-99: ilk kare geldi, bu kanal ölü sayılmayacak
        CrashLog.event(this, "tv", "hazır: " + list.get(index).name);
        progress.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        player.setVolume(muted ? 0f : 1f);
        player.play();
        playButton.setText("⏸");
        Prefs.clearFails(this, list.get(index).url);
        autoSkips = 0;
        fbTried = 0;
        if (sameRetries > 0) showNotice("✅ Bağlantı iyi · yayın açıldı");
        sameRetries = 0;
        netReconnects = 0;
        stallCount = 0;
        ui.postDelayed(new Runnable() {
            @Override public void run() { hideNotice(); }
        }, 4000);
        if (!tracking) { tracking = true; openedAt = System.currentTimeMillis(); }
        ui.removeCallbacks(watchdog);
    }

    /** Son oynatıcı hatası (hata günlüğüne yazılır). */
    private String lastError = "";

    /** Hata adres çözümleme (DNS) kaynaklı mı? O zaman kanal değil bağlantı sorunludur. */
    private boolean isDnsError() {
        String e = lastError == null ? "" : lastError.toLowerCase(java.util.Locale.ROOT);
        return e.contains("unable to resolve host") || e.contains("unknownhost")
                || e.contains("no address associated") || e.contains("nodename nor servname")
                || e.contains("name or service not known");
    }

    /** Aynı yayına kaç kez yeniden bağlanıldı (zayıf internet). */
    private int sameRetries = 0;
    /** Ağ olayı yüzünden kaç kez yeniden bağlanıldı (oynak bağlantı koruması). */
    private int netReconnects = 0;
    /** Yayın hiç hazır oldu mu (ölü kanal zaman aşımı). */
    private boolean everPlayed = false;
    /** Bu kanal için ilk oynatma denemesinin başladığı an. */
    private long tryStartedAt = 0;
    private android.net.ConnectivityManager.NetworkCallback netCb;

    /** M-80: internet geri geldiğinde yayını kendiliğinden yeniden başlat. */
    private void watchNetwork() {
        try {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            netCb = new android.net.ConnectivityManager.NetworkCallback() {
                @Override public void onLost(android.net.Network n) {
                    CrashLog.event(PlayerActivity.this, "net", "bağlantı koptu");
                }
                @Override public void onAvailable(android.net.Network n) {
                    ui.post(new Runnable() {
                        @Override public void run() {
                            try {
                                if (player == null || adPaused) return;
                                boolean ok = player.getPlaybackState()
                                        == androidx.media3.common.Player.STATE_READY
                                        && player.isPlaying();
                                if (ok) return;
                                // M-99: ağ her duyurulduğunda retry sayacı sıfırlanıyordu;
                                // oynak bağlantıda "yeniden bağlanıyorum" sonsuza dönüyordu.
                                if (++netReconnects > 3) {
                                    markDead("Bağlantı kurulamadı");
                                    return;
                                }
                                errorView.setVisibility(View.GONE);
                                progress.setVisibility(View.VISIBLE);
                                showNotice("📶 İnternet geri geldi · yeniden bağlanıyorum");
                                final String cur = list.get(index).url;
                                ready = false;   // onReady() tekrar çalışsın (spinner/uyarı temizlenir)
                                player.stop();
                                player.clearMediaItems();
                                player.setMediaItem(androidx.media3.common.MediaItem.fromUri(cur));
                                player.prepare();
                                player.play();
                                ui.postDelayed(watchdog, 15000);
                            } catch (Throwable ignored) { }
                        }
                    });
                }
            };
            cm.registerDefaultNetworkCallback(netCb);
        } catch (Throwable ignored) { }
    }
    // Reklam açıkken yayın duraklatılır; kapanınca canlı kenara yeniden bağlanılır.
    private boolean adPaused = false;

    /** ExoPlayer hatası: aynı adres → yedek kaynak → sonraki kanal. */
    private void onPError() {
        progress.setVisibility(View.GONE);
        final String cur = list.get(index).url;
        // Zayıf internet: kanalı hemen bırakmak yerine aynı yayına 2 kez daha bağlan.
        if (sameRetries < 3) {
            sameRetries++;
            final int n = sameRetries;
            showNotice("📶 Bağlantı sorunu · yeniden bağlanıyorum (" + n + "/3)");
            CrashLog.event(this, "tv", "hata, tekrar deneniyor " + n + "/3: " + cur);
            progress.setVisibility(View.VISIBLE);
            ready = false;   // yeniden bağlanınca onReady() çalışsın
            ui.postDelayed(new Runnable() {
                @Override public void run() {
                    try {
                        player.stop();
                        player.clearMediaItems();
                        player.setMediaItem(androidx.media3.common.MediaItem.fromUri(cur));
                        player.prepare();
                        player.play();
                        ui.postDelayed(watchdog, 15000);
                    } catch (Throwable ignored) { }
                }
            }, 5000);
            return;
        }
        String[] alts = ChannelRepo.fallbacks(cur);
        if (alts != null && fbTried < alts.length) {
            String alt = alts[fbTried++];
            Toast.makeText(this, "Yedek kaynak deneniyor…", Toast.LENGTH_SHORT).show();
            errorView.setVisibility(View.GONE);
            try {
                player.stop();
                player.clearMediaItems();
                player.setMediaItem(androidx.media3.common.MediaItem.fromUri(alt));
                player.prepare();
                player.play();
                return;
            } catch (Throwable ignored) { }
        }
        // İnternet/DNS sorunu kanalın suçu değil: ölü işaretleme, kullanıcıya doğru mesajı ver.
        if (!Net.online(this) || isDnsError()) {
            errorView.setText("İnternet bağlantısı kurulamadı.\n\nWi-Fi / mobil veriyi kontrol et,\n"
                    + "sonra dokun → tekrar dene");
            errorView.setVisibility(View.VISIBLE);
            return;
        }
        Prefs.addFail(this, cur);
        errorView.setText("Bu kanal şu an açılamadı.\n\nDokun → tekrar dene\nveya ⏭ ile sonraki kanal");
        errorView.setVisibility(View.VISIBLE);
        autoNext();
    }

    /**
     * M-99: kanalı ölü say — hata işaretle, hata ekranını göster, gerekiyorsa sonrakine geç.
     * Tek yerden yapılıyor ki dört ayrı "yeniden bağlanıyorum" yolu farklı davranmasın.
     */
    private void markDead(String neden) {
        String nm = "?", u = "";
        try { nm = list.get(index).name; u = list.get(index).url; } catch (Exception ignored) { }
        // Hata günlüğü: kanal neden ve ne kadar sürede ölü sayıldı.
        CrashLog.event(this, "tv", "ölü: " + nm + " · " + neden
                + (lastError.length() > 0 ? " · son hata=" + lastError : "")
                + " · " + u);
        // Bağlantı yokken "kanal ölü" demek yanlış: kullanıcı internetini düzeltince kanal açılır.
        if (!Net.online(this) || isDnsError()) {
            try {
                progress.setVisibility(View.GONE);
                errorView.setText("İnternet bağlantısı kurulamadı.\n\n" + neden
                        + " · Wi-Fi / mobil veriyi kontrol edip dokun → tekrar dene");
                errorView.setVisibility(View.VISIBLE);
            } catch (Exception ignored) { }
            return;
        }
        try { Prefs.addFail(this, list.get(index).url); } catch (Exception ignored) { }
        try {
            progress.setVisibility(View.GONE);
            errorView.setText("Bu kanal şu an açılamadı.\n\n" + neden
                    + " · Dokun → tekrar dene\nveya ⏭ ile sonraki kanal");
            errorView.setVisibility(View.VISIBLE);
        } catch (Exception ignored) { }
        autoNext();
    }

    /**
     * M-99: 45 sn içinde hiç hazır olmadıysa kanalı ölü say. Sunucusu tamamen erişilemez
     * kanallar onPlayerError üretmediği için eskiden sonsuz "yeniden bağlanıyorum"
     * çemberinde kalıyor, hiç başarısız işaretlenmiyor ve "ölü kanalları onar" hiç
     * devreye giremiyordu.
     */
    private boolean giveUpIfDead() {
        if (everPlayed || tryStartedAt <= 0) return false;
        if (System.currentTimeMillis() - tryStartedAt < 45000) return false;
        markDead("Zaman aşımı");
        return true;
    }

    private void checkStall() {
        try {
            // Erken dönüşlerden (oynatıcı IDLE/BUFFERING) önce bakılmalı.
            if (giveUpIfDead()) return;
            // Reklam açıkken yayın duraklatıldığı için donma saymayalım.
            if (AdManager.isShowingFullScreen() || adPaused) return;
            // M-23: eskiden "isPlaying() false" ise hiç bakmıyordu; tamponlama sırasında
            // tam da donma anını kaçırıyordu. Artık BUFFERING de izleniyor.
            int state = player.getPlaybackState();
            boolean buffering = state == androidx.media3.common.Player.STATE_BUFFERING;
            if (!player.isPlaying() && !buffering) return;
            long now = System.currentTimeMillis();
            int pos = (int) player.getCurrentPosition();
            if (pos != lastPos) {
                lastPos = pos;
                lastPosAt = now;
                if (stallCount > 0) { stallCount = 0; hideNotice(); }
                return;
            }
            if (now - lastPosAt < 12000 || now - lastRestart < 9000) return;
            lastRestart = now;
            lastPosAt = now;
            stallCount++;
            if (stallCount <= 2) {
                showNotice("⏳ Yayın durakladı · yeniden bağlanıyorum (" + stallCount + "/2)");
            } else {
                // Star TV / TV8 gibi kanallarda "dijital reklam kuşağı" karesi yayıncıdan gelir;
                // uygulama atlayamaz, ama boş siyah ekran yerine durumu söyleyip bekler.
                showNotice("📺 Yayıncı reklam aralığında olabilir · yayın dönünce kendiliğinden gelecek");
            }
            play();
        } catch (Exception ignored) { }
    }

    private void showNotice(String text) {
        try {
            noticeView.setText(text);
            noticeView.setVisibility(View.VISIBLE);
        } catch (Exception ignored) { }
    }

    private void hideNotice() {
        try { noticeView.setVisibility(View.GONE); } catch (Exception ignored) { }
    }

    /** Limit dolduysa sorar: devam edilsin mi? */
    private void checkLimit() {
        final int lim = Prefs.limitMb(this);
        if (lim <= 0 || limitWarned) return;
        long used = Prefs.todayBytes(this) + sessionBytes.get();
        if (used < lim * 1048576L) return;
        limitWarned = true;
        new AlertDialog.Builder(this)
                .setTitle("Günlük veri limiti doldu")
                .setMessage("Limit: " + lim + " MB\nKullanılan: " + Net.mb(used)
                        + "\n\nDevam edilsin mi?")
                .setCancelable(false)
                .setPositiveButton("Devam et", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { }
                })
                .setNegativeButton("Durdur", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        Toast.makeText(PlayerActivity.this, "Limit doldu, yayın durduruldu",
                                Toast.LENGTH_LONG).show();
                        finishUsage();
                        finish();
                    }
                })
                .show();
    }

    // ---------- uyku zamanlayicisi ----------
    private void sleepDialog() {
        final String[] items = {"15 dakika", "30 dakika", "45 dakika", "60 dakika", "90 dakika",
                "120 dakika", "Saat gir (örn 23:30)", "Zamanlayıcıyı iptal et"};
        new AlertDialog.Builder(this)
                .setTitle("Ne zaman kapansın?")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == items.length - 1) {
                            sleepUntil = 0;
                            Prefs.setSleepUntil(PlayerActivity.this, 0);
                            Toast.makeText(PlayerActivity.this, "Zamanlayıcı iptal edildi",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (which == 6) { askClockTime(); return; }
                        int[] mins = {15, 30, 45, 60, 90, 120};
                        sleepUntil = System.currentTimeMillis() + mins[which] * 60000L;
                        Prefs.setSleepUntil(PlayerActivity.this, sleepUntil);
                        Toast.makeText(PlayerActivity.this, mins[which] + " dakika sonra kapanacak",
                                Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void askClockTime() {
        final EditText et = new EditText(this);
        et.setHint("23:30");
        et.setInputType(InputType.TYPE_CLASS_DATETIME);
        new AlertDialog.Builder(this).setTitle("Kapanış saati").setView(et)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        try {
                            String[] p = et.getText().toString().trim().split(":");
                            int h = Integer.parseInt(p[0].trim());
                            int mi = Integer.parseInt(p[1].trim());
                            java.util.Calendar c = java.util.Calendar.getInstance();
                            c.set(java.util.Calendar.HOUR_OF_DAY, h);
                            c.set(java.util.Calendar.MINUTE, mi);
                            c.set(java.util.Calendar.SECOND, 0);
                            if (c.getTimeInMillis() <= System.currentTimeMillis())
                                c.add(java.util.Calendar.DAY_OF_MONTH, 1);
                            sleepUntil = c.getTimeInMillis();
                            Prefs.setSleepUntil(PlayerActivity.this, sleepUntil);
                            Toast.makeText(PlayerActivity.this, "Saat " + String.format("%02d:%02d", h, mi)
                                    + " de kapanacak", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(PlayerActivity.this, "Saat formatı: 23:30", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("Vazgeç", null).show();
    }

    // ---------- kalite ----------
    /** Kalite düğmesinde yayının gerçek yüksekliğini göster (tercih değil). */
    private void showRealQuality(int height) {
        if (qualityBtn == null || height <= 0) return;
        qualityBtn.setText(height + "p");
    }

    /**
     * MP-15: kullanıcı 360p seçti ama kanalda 360p varyantı yoksa oynatıcı hiç görüntü
     * seçemiyor / "yayın bulunamadı" görünüyordu. Gerçek yükseklik tercihin üstündeyse
     * kalite kısıtını kaldır, tek satır açıkla ve bir daha tekrarlama.
     */
    private void noteVariantGap(int h) {
        int pref = Prefs.maxHeight(this);
        if (pref <= 0 || h <= pref || index == warnedNoVariant || isFinishing()) return;
        warnedNoVariant = index;
        try {
            androidx.media3.common.TrackSelectionParameters.Builder b =
                    player.getTrackSelectionParameters().buildUpon();
            b.clearOverridesOfType(androidx.media3.common.C.TRACK_TYPE_VIDEO);
            player.setTrackSelectionParameters(b.build());
        } catch (Throwable ignored) { }
        Toast.makeText(this, "Bu kanalda " + pref + "p yok · en düşük " + h
                + "p oynatılıyor (veri artabilir)", Toast.LENGTH_LONG).show();
        CrashLog.event(this, "tv", "varyant yok: tercih " + pref + "p, gelen " + h + "p");
    }

    private void chooseQuality() {
        QualityDialog.show(this, new Runnable() {
            @Override public void run() {
                updateQualityLabel();
                play();
            }
        });
    }

    // ---------- kayit ----------
    private void toggleRecording() {
        if (recorder.isRunning()) {
            recorder.stop();
            recButton.setTextColor(TEXT);
            Toast.makeText(this, "Kayıt durduruluyor...", Toast.LENGTH_SHORT).show();
            return;
        }
        Channel c = list.get(index);
        if (!ChannelRepo.canRecord(c.name)) {
            Toast.makeText(this, "Kayıt yalnız kamu kanallarında açık (TRT, TBMM, Diyanet) · "
                    + "özel kanallarda telif nedeniyle kapalı", Toast.LENGTH_LONG).show();
            return;
        }
        recorder.start(this, c.url, c.name, new HlsRecorder.Listener() {
            @Override public void onInfo(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override public void run() { recButton.setTextColor(Color.parseColor("#FF5252")); }
                });
            }
            @Override public void onError(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        recButton.setTextColor(TEXT);
                        Toast.makeText(PlayerActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override public void onStopped(final String path, final long bytes, final boolean sesli) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        recButton.setTextColor(TEXT);
                        Toast.makeText(PlayerActivity.this, "Kayıt bitti: " + (bytes / 1048576) + " MB"
                                + (sesli ? " (ses dahil)" : "") + "\nGaleri → Movies/TV-Kayitlari", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        // Renk yalnız gerçekten başladığında (onInfo) kırmızıya döner; eskiden istek
        // yutulsa bile düğme kırmızı kalıyordu.
        Toast.makeText(this, "Kayıt başladı → Movies/TV-Kayitlari", Toast.LENGTH_SHORT).show();
    }

    // ---------- arka plan ses ----------
    private void listenInBackground() {
        Channel c = list.get(index);
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 11);
        }
        AudioService.start(this, c.url, c.name);
        // ayni anda iki ses olmasin: TV goruntusunu sessize al
        try { player.setVolume(0f); } catch (Exception ignored) { }
        muted = true;
        muteBtn.setText("🔇");
        Toast.makeText(this, c.name + " arka planda çalıyor. Ekranı kapatabilirsin.", Toast.LENGTH_LONG).show();
    }

    private void show() {
        Channel c = list.get(index);
        nameView.setText("№" + c.no + "  " + c.name);
        counter.setText((index + 1) + "/" + list.size());
        favButton.setTextColor(Prefs.favs(this).contains(c.url) ? Color.parseColor("#FFC107") : MUTED);
        updateQualityLabel();
        if (recorder.isRunning()) { recorder.stop(); recButton.setTextColor(TEXT); }
        Prefs.addRecent(this, c.url);
        Prefs.setLastIndex(this, index);
        play();
        scheduleHide();
    }

    private void play() {
        final Channel c = list.get(index);
        if (!Net.online(this)) {
            errorView.setText("İnternet bağlantısı yok.\n\nBağlanınca dokun → tekrar dene");
            errorView.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            return;
        }
        if (Net.mobile(this) && !Prefs.mobileOk(this)) { askMobile(c); return; }
        if (Net.mobile(this) && Prefs.audioOnlyMobile(this)) { startAudioOnly(c); return; }
        playNow(c);
    }

    /** Wi-Fi yok: mobil veri onayi + limit kontrolu. */
    private void askMobile(final Channel c) {
        int lim = Prefs.limitMb(this);
        long used = Prefs.todayBytes(this);
        StringBuilder msg = new StringBuilder();
        msg.append("Wi-Fi yok, mobil veridesin.\nYayın internet harcar.\n\n");
        msg.append("Bugün: ").append(Net.mb(used));
        if (lim > 0) {
            msg.append(" / ").append(lim).append(" MB");
            if (used > lim * 1048576L) msg.append("\n⚠ Günlük limitin doldu!");
            else msg.append("\nKalan: ").append(Net.mb(lim * 1048576L - used));
        }
        msg.append("\n\nSadece ses → görüntü inmez, çok daha az veri harcar.");
        new AlertDialog.Builder(this)
                .setTitle("Mobil veri uyarısı")
                .setMessage(msg.toString())
                .setPositiveButton("Aç (mobil veri)", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        Prefs.setMobileOk(PlayerActivity.this, true);
                        playNow(c);
                    }
                })
                .setNeutralButton("Sadece ses 🎧", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { startAudioOnly(c); }
                })
                .setNegativeButton("Vazgeç", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { finish(); }
                })
                .show();
    }

    private void startAudioOnly(final Channel c) {
        Toast.makeText(this, c.name + ": ses olarak başlatılıyor…", Toast.LENGTH_SHORT).show();
        Quality.resolveLowest(this, c.url, new Quality.UrlCallback() {
            @Override public void onUrl(final String url) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        AudioService.start(PlayerActivity.this, url, c.name);
                        Toast.makeText(PlayerActivity.this, c.name
                                + ": ses olarak çalıyor (en düşük kalite, en az veri).\n"
                                + "Ekranı kapatabilirsin.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        });
    }

    private void playNow(final Channel c) {
        // M-22: hızlı zap'ta eski isteğin geç gelen cevabı yeni kanalı ezmesin.
        final int req = ++playRequest;
        // önceki kanalın baytlarını eski adrese yaz, sayacı sıfırla
        finishUsage();
        usageUrl = c.url;
        sessionBytes.set(0);
        sameRetries = 0;
        // M-99: sayaçlar her denemede sıfırlandığı için "kaç kez denendi" mantığı sonsuz
        // döngüye açıktı; ölü kanal kararı artık zamana bağlı.
        tryStartedAt = System.currentTimeMillis();
        everPlayed = false;
        tracking = true;
        openedAt = System.currentTimeMillis();
        // TV acilinca arkadaki radyo sussun (tek ses kaynagi kalsin)
        AudioService.stop(this);
        screenOffAudio = false;
        progress.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        // Önce 302 zincirini çöz (Kral Pop gibi jetonlu CDN'ler), SONRA kalite seç:
        // varyant adresleri son adrese göre çözülmeli, yoksa 404 oluyor.
        new Thread(new Runnable() {
            @Override public void run() {
                final String direct = Net.follow(c.url);
                Quality.resolve(PlayerActivity.this, direct, new Quality.UrlCallback() {
                    @Override public void onUrl(final String url) {
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                if (req != playRequest) return;   // kullanıcı başka kanala geçti
                                startVideo(url);
                            }
                        });
                    }
                });
            }
        }, "follow").start();
    }

    private void startVideo(String url) {
        progress.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        ready = false;
        try { player.stop(); player.clearMediaItems(); } catch (Exception ignored) { }
        // Tarayıcı UA'sı: TV8 gibi CDN'ler aksi hâlde reklam kuşağı klibi döndürüyor.
        try {
            // A/V senkron: master playlist + track sınırı (varyant URL'i değil).
            Quality.applyPref(this, player);
            player.setMediaItem(androidx.media3.common.MediaItem.fromUri(url));
            player.prepare();
            player.play();
        } catch (Exception ignored) { }
        video.requestFocus();
        lastPos = -1;
        lastPosAt = System.currentTimeMillis();
        ui.removeCallbacks(watchdog);
        ui.postDelayed(watchdog, 15000);
    }

    private void toggleMute() {
        muted = !muted;
        try { player.setVolume(muted ? 0f : 1f); }
        catch (Exception ignored) { }
        muteBtn.setText(muted ? "🔇" : "🔊");
        Toast.makeText(this, muted ? "TV sesi kapalı (radyo/müzik dinleyebilirsin)"
                : "TV sesi açık", Toast.LENGTH_SHORT).show();
    }

    /** 13 sn icinde goruntu gelmezse siyah ekran yerine aciklama goster. */
    private final Runnable watchdog = new Runnable() {
        @Override public void run() {
            try {
                if (!player.isPlaying()) {
                    if (giveUpIfDead()) return;
                    // Zayıf internet: kanalı "ölü" saymadan önce aynı yayına 2 kez daha bağlan.
                    if (sameRetries < 2) {
                        sameRetries++;
                        final String cur = list.get(index).url;
                        showNotice("📶 Bağlantı sorunu · yeniden bağlanıyorum ("
                                + sameRetries + "/3)");
                        CrashLog.event(PlayerActivity.this, "tv",
                                "15 sn ses yok, tekrar " + sameRetries + "/3: " + cur);
                        ready = false;   // tekrar açılışta onReady() çalışsın
                        try {
                            player.stop();
                            player.clearMediaItems();
                            player.setMediaItem(androidx.media3.common.MediaItem.fromUri(cur));
                            player.prepare();
                            player.play();
                        } catch (Throwable ignored) { }
                        ui.removeCallbacks(this);
                        ui.postDelayed(this, 15000);
                        return;
                    }
                    progress.setVisibility(View.GONE);
                    // M-99: buradan vazgeçen kanal hiç "başarısız" işaretlenmiyordu.
                    markDead("Bağlantı kurulamadı");
                    return;
                }
            } catch (Exception ignored) { }
        }
    };

    /** Biriken baytları hemen kaydet (kanal değişimi, duraklatma, 2 MB eşiği). */
    private void flushUsage() {
        long bytes = sessionBytes.getAndSet(0);
        if (bytes > 0 && usageUrl != null) Prefs.addUsage(this, usageUrl, bytes, 0);
    }

    private void finishUsage() {
        if (!tracking) { flushUsage(); return; }
        tracking = false;
        flushUsage();
        int sec = (int) ((System.currentTimeMillis() - openedAt) / 1000);
        if (usageUrl != null && sec > 2) Prefs.addUsage(this, usageUrl, 0, sec);
    }

    private boolean enterPip() {
        if (Build.VERSION.SDK_INT < 26) return false;
        try {
            if (!player.isPlaying()) return false;
            enterPictureInPictureMode(new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(16, 9)).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Aynı kanal 2 kez üst üste açılmadıysa kullanıcıyı bekletmeden sonraki kanala geçer.
     * En fazla 3 kez üst üste atlar (tüm liste ölüyse döngüye girmez).
     */
    private void autoNext() {
        try {
            if (list.size() < 2 || autoSkips >= 3) return;
            if (Prefs.fails(this, list.get(index).url) < 2) return;
            autoSkips++;
            ui.postDelayed(new Runnable() {
                @Override public void run() {
                    if (isFinishing()) return;
                    Toast.makeText(PlayerActivity.this,
                            "Kanal açılmadı, sonraki kanala geçiliyor", Toast.LENGTH_SHORT).show();
                    step(1);
                }
            }, 2200);
        } catch (Exception ignored) { }
    }

    private void step(int d) {
        if (list.isEmpty()) return;
        finishUsage();
        ui.removeCallbacks(watchdog);
        index = (index + d + list.size()) % list.size();
        warnedNoVariant = -1;
        // v9.25: her kanal değişiminde geçiş reklamı (varsa) gösterilir.
        final Channel next = list.get(index);
        AdManager.maybeInterstitial(this, next.url, new Runnable() {
            @Override public void run() {
                if (!isFinishing()) show();
            }
        });
    }

    private void toggleFav() {
        Channel c = list.get(index);
        Prefs.toggleFav(this, c.url);
        favButton.setTextColor(Prefs.favs(this).contains(c.url) ? Color.parseColor("#FFC107") : MUTED);
    }

    // ---------- yatay / dikey ----------
    @Override
    public void onConfigurationChanged(Configuration cfg) {
        super.onConfigurationChanged(cfg);
        if (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            goImmersive(true);
            scheduleHide();
        } else {
            goImmersive(false);
            showBars();
        }
    }

    private void goImmersive(boolean on) {
        View d = getWindow().getDecorView();
        if (on) {
            d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        } else {
            d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void toggleBars() {
        if (barsVisible) hideBars(); else { showBars(); scheduleHide(); }
    }

    private void showBars() { barsVisible = true; top.setVisibility(View.VISIBLE); bottom.setVisibility(View.VISIBLE); }

    private void hideBars() { barsVisible = false; top.setVisibility(View.INVISIBLE); bottom.setVisibility(View.INVISIBLE); }

    private void scheduleHide() {
        ui.removeCallbacks(hideRunnable);
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) return;
        ui.postDelayed(hideRunnable, 4000);
    }

    private final Runnable hideRunnable = new Runnable() {
        @Override public void run() { hideBars(); }
    };

    // ---------- PiP ----------
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPip();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean inPip, Configuration cfg) {
        super.onPictureInPictureModeChanged(inPip, cfg);
        if (inPip) {
            top.setVisibility(View.GONE);
            bottom.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            clockBar.setVisibility(View.GONE);
        } else {
            showBars();
            clockBar.setVisibility(View.VISIBLE);
        }
    }

    // ---------- ana sayfa / buyuk dugmeler ----------
    private void goHome() {
        AudioService.stop(this);
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private void hint(View v, final String text) {
        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View x) {
                Toast.makeText(PlayerActivity.this, text, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void updateQualityLabel() {
        if (qualityBtn != null) setBarText(qualityBtn, "🎚", QualityDialog.label(this));
    }

    private void setBarText(TextView t, String icon, String label) {
        android.text.SpannableStringBuilder sb = new android.text.SpannableStringBuilder(
                icon + "\n" + label);
        // 9.76: üst bar ikonları çok küçüktü (9sp yazı / 1.6× ikon).
        sb.setSpan(new android.text.style.RelativeSizeSpan(2.2f), 0, icon.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        t.setText(sb);
    }

    /** Ust bardaki buyuk dugme: ikon ustte, ad altta. */
    private TextView barButton(String icon, String label) {
        TextView t = new TextView(this);
        t.setTextColor(TEXT);
        t.setTextSize(10);
        t.setGravity(Gravity.CENTER);
        setBarText(t, icon, label);
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor("#66000000"));
        d.setCornerRadius(dp(14));
        t.setBackground(d);
        t.setPadding(dp(2), dp(6), dp(2), dp(4));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(68), dp(62));
        lp.setMargins(dp(3), 0, dp(3), 0);
        t.setLayoutParams(lp);
        return t;
    }

    private TextView iconButton(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(TEXT);
        t.setTextSize(15);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(8), dp(4), dp(8), dp(4));
        return t;
    }

    private TextView smallButton(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(15);
        t.setGravity(Gravity.CENTER);
        GradientDrawable d = new GradientDrawable();
        d.setColor(ACCENT);
        d.setCornerRadius(dp(22));
        t.setBackground(d);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(46), dp(38));
        lp.setMargins(dp(4), 0, dp(4), 0);
        t.setLayoutParams(lp);
        return t;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }

    @Override
    protected void onPause() {
        super.onPause();
        flushUsage();
        if (isFinishing()) return;   // ✕ Kapat / geri: arkada ses devam etmesin
        if (Build.VERSION.SDK_INT >= 26 && isInPictureInPictureMode()) return;
        // ekran kapaninca / uygulamadan cikinca ses devam etsin (kilit ekrani dinleme)
        try {
            if (player.isPlaying() && index >= 0 && index < list.size()) {
                Channel c = list.get(index);
                String q = Prefs.quality(this, c.url);
                AudioService.start(this, q != null ? q : c.url, c.name);
                screenOffAudio = true;
            }
        } catch (Exception ignored) { }
        try { player.pause(); } catch (Exception ignored) { }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (screenOffAudio) {
            screenOffAudio = false;
            AudioService.stop(this);
            try { player.play(); } catch (Exception ignored) { }
        }
    }

    private void setLocked(boolean v) {
        locked = v;
        lockHint.setVisibility(v ? View.VISIBLE : View.GONE);
        if (v) {
            hideBars();
        } else {
            showBars();
            scheduleHide();
        }
        Toast.makeText(this, v ? "Ekran kilitli: yanlışlıkla dokunmak bir şey bozmaz"
                : "Kilit açıldı", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        finishUsage();
        try {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (netCb != null) cm.unregisterNetworkCallback(netCb);
        } catch (Throwable ignored) { }
        ui.removeCallbacksAndMessages(null);
        if (recorder.isRunning()) recorder.stop();
        try { if (screenOff != null) unregisterReceiver(screenOff); } catch (Throwable ignored) { }
        try { player.release(); } catch (Exception ignored) { }
        super.onDestroy();
    }
}
