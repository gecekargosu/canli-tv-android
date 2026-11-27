package com.cengiz.canlitv;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/** Radyo calmа ekrani: analog radyo goruntusu, istasyon adi, animasyonlu ekolayzir ve bant ayarlari. */
public class RadioPlayerActivity extends Activity {

    private static final int BG = Color.parseColor("#11151C");
    private static final int CARD = Color.parseColor("#1B2230");
    private static final int ORANGE = Color.parseColor("#FF7A18");
    private static final int NAVY = Color.parseColor("#1F3A93");
    private static final int TEXT = Color.parseColor("#F5F7FA");
    private static final int MUTED = Color.parseColor("#9AA4B2");

    private static List<String> names, metas, urls;
    private static boolean loaded = false;

    private int index = 0;
    private TextView nameView, metaView, statusView, sleepLabel, numView;
    private RingGauge ringGauge;
    private SpectrumView eqView;
    private EqFader[] faders = new EqFader[5];
    private final Handler ui = new Handler();
    private boolean playing = true;

    public static void open(Context ctx, int i) {
        android.content.Intent it = new android.content.Intent(ctx, RadioPlayerActivity.class);
        it.putExtra("index", i);
        ctx.startActivity(it);
    }

    private void load() {
        // secilen ulkenin radyolari (TR gomulu, digerleri onbellekten)
        RadioRepo.ensure(this);
        // kopya: ülke değişince RadioRepo listeleri boşalır (çökme koruması)
        names = new java.util.ArrayList<String>(RadioRepo.NAMES);
        metas = new java.util.ArrayList<String>(RadioRepo.META);
        urls = new java.util.ArrayList<String>(RadioRepo.URLS);
    }

    @Override
    protected void onCreate(Bundle b) {
        // P0-5i: radyodan arka plan çalmaya geçerken de bildirim izni istensin.
        if (android.os.Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 13);
        }
        super.onCreate(b);
        load();
        // HATA (tur 1): ülkenin radyo listesi henüz inmemişse names.get(0) çöküyordu.
        if (names.isEmpty()) {
            Toast.makeText(this, I18n.t(this, "Radyo listesi hazır değil. Radyo ekranından yeniden dene."),
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        index = Math.max(0, Math.min(names.size() - 1, getIntent().getIntExtra("index", 0)));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        // durum cubugunun altindan basla (edge-to-edge bosluk duzeltmesi)
        scroll.setFitsSystemWindows(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(14), dp(16), dp(16));

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView close = chip("◀ Liste");
        close.setTextSize(12);
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        head.addView(close);
        // Ana sayfa: radyo calmaya devam eder (arka plan), liste ekrani kapanir
        TextView home = chip("🏠 Ana");
        home.setTextSize(12);
        home.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                android.content.Intent m = new android.content.Intent(
                        RadioPlayerActivity.this, MainActivity.class);
                m.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(m);
            }
        });
        head.addView(home);

        // Uygulamadan tamamen cik (radyo da durur)
        TextView quit = chip("Çık");
        quit.setTextSize(12);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioService.stop(RadioPlayerActivity.this);
                finishAffinity();
            }
        });
        head.addView(quit);

        // uyku zamanlayici: gece dinlerken belirli sure sonra durdur
        final TextView sleepBtn = chip("Uyku");
        sleepLabel = sleepBtn;
        sleepBtn.setTextSize(12);
        sleepBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { sleepDialog(); }
        });
        head.addView(sleepBtn);
        TextView stopBtn = chip("Durdur");
        stopBtn.setTextSize(12);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioService.stop(RadioPlayerActivity.this);
                statusView.setText(I18n.t(RadioPlayerActivity.this, "durduruldu"));
                playing = false;
            }
        });
        head.addView(stopBtn);
        root.addView(head);

        // ---------- v10.02: radyo paneli (tasarım 2 — cam + neon halka) ----------
        LinearLayout glass = new LinearLayout(this);
        glass.setOrientation(LinearLayout.VERTICAL);
        glass.setGravity(Gravity.CENTER_HORIZONTAL);
        GradientDrawable glassBg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#16243A"), Color.parseColor("#0B1422")});
        glassBg.setCornerRadius(dp(22));
        glassBg.setStroke(dp(1), Color.parseColor("#22314A"));
        glass.setBackground(glassBg);
        glass.setPadding(dp(14), dp(14), dp(14), dp(12));

        TextView live = new TextView(this);
        live.setText(I18n.t(this, "● CANLI YAYIN"));
        live.setTextColor(ORANGE);
        live.setTextSize(9);
        live.setLetterSpacing(0.18f);
        live.setTypeface(live.getTypeface(), android.graphics.Typeface.BOLD);
        live.setGravity(Gravity.CENTER);
        glass.addView(live);

        android.widget.FrameLayout ringBox = new android.widget.FrameLayout(this);
        ringGauge = new RingGauge(this);
        ringBox.addView(ringGauge, new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout ringTexts = new LinearLayout(this);
        ringTexts.setOrientation(LinearLayout.VERTICAL);
        ringTexts.setGravity(Gravity.CENTER);
        ringTexts.setPadding(dp(34), 0, dp(34), 0);

        numView = new TextView(this);
        numView.setTextColor(TEXT);
        numView.setTextSize(36);
        numView.setGravity(Gravity.CENTER);
        numView.setTypeface(numView.getTypeface(), android.graphics.Typeface.BOLD);
        numView.setShadowLayer(10f, 0f, 2f, Color.parseColor("#000000"));
        ringTexts.addView(numView);

        TextView numCap = new TextView(this);
        numCap.setText(I18n.t(this, "KANAL"));
        numCap.setTextColor(MUTED);
        numCap.setTextSize(8);
        numCap.setLetterSpacing(0.3f);
        numCap.setGravity(Gravity.CENTER);
        ringTexts.addView(numCap);

        nameView = new TextView(this);
        nameView.setTextColor(TEXT);
        nameView.setTextSize(19);
        nameView.setGravity(Gravity.CENTER);
        nameView.setTypeface(nameView.getTypeface(), android.graphics.Typeface.BOLD);
        nameView.setPadding(0, dp(6), 0, 0);
        nameView.setSingleLine(true);
        nameView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        ringTexts.addView(nameView);

        metaView = new TextView(this);
        metaView.setTextColor(Color.parseColor("#8FA0B5"));
        metaView.setTextSize(10);
        metaView.setGravity(Gravity.CENTER);
        metaView.setSingleLine(true);
        metaView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        ringTexts.addView(metaView);

        ringBox.addView(ringTexts, new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        glass.addView(ringBox, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(214)));

        // canlı ses seviyesi (halkanın altında ince şerit)
        SpectrumView vu = new SpectrumView(this);
        LinearLayout.LayoutParams vulp = new LinearLayout.LayoutParams(dp(132), dp(30));
        vulp.gravity = Gravity.CENTER_HORIZONTAL;
        vulp.topMargin = dp(2);
        glass.addView(vu, vulp);

        statusView = new TextView(this);
        statusView.setTextColor(Color.parseColor("#4ADE80"));
        statusView.setTextSize(10.5f);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, dp(8), 0, dp(2));
        glass.addView(statusView);
        root.addView(glass);

        LinearLayout fixRow = new LinearLayout(this);
        fixRow.setGravity(Gravity.CENTER);
        fixRow.setPadding(0, dp(8), 0, 0);
        TextView retryBtn = chip(I18n.t(this, "🔄 Tekrar dene"));
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioService.start(RadioPlayerActivity.this, urls.get(index), names.get(index));
                ui.removeCallbacks(radioWatchdog);
                ui.postDelayed(radioWatchdog, 15000);
            }
        });
        TextView nextBtn = chip(I18n.t(this, "⏭ Sonraki kanal"));
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { changeStation(1); }
        });
        fixRow.addView(retryBtn);
        fixRow.addView(nextBtn);
        root.addView(fixRow);

        // ---------- genişletilmiş ekolayzır ----------
        LinearLayout eqHead = new LinearLayout(this);
        eqHead.setGravity(Gravity.CENTER_VERTICAL);
        eqHead.setPadding(0, dp(14), 0, dp(6));
        TextView eqTitle = new TextView(this);
        eqTitle.setText(I18n.t(this, "SES DENGESİ"));
        eqTitle.setTextColor(TEXT);
        eqTitle.setTextSize(11);
        eqTitle.setLetterSpacing(0.16f);
        eqTitle.setTypeface(eqTitle.getTypeface(), android.graphics.Typeface.BOLD);
        eqTitle.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        eqHead.addView(eqTitle);
        TextView eqLive = new TextView(this);
        eqLive.setText(I18n.t(this, "● canlı ölçüm"));
        eqLive.setTextColor(Color.parseColor("#4ADE80"));
        eqLive.setTextSize(9);
        eqHead.addView(eqLive);
        root.addView(eqHead);

        LinearLayout eqCard = new LinearLayout(this);
        eqCard.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable eqBg = new GradientDrawable();
        eqBg.setColor(CARD);
        eqBg.setCornerRadius(dp(18));
        eqBg.setStroke(dp(1), Color.parseColor("#1B2942"));
        eqCard.setBackground(eqBg);
        eqCard.setPadding(dp(12), dp(12), dp(12), dp(10));

        eqView = new SpectrumView(this);
        eqCard.addView(eqView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(96)));

        LinearLayout bands = new LinearLayout(this);
        bands.setOrientation(LinearLayout.HORIZONTAL);
        bands.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(150));
        blp.topMargin = dp(8);
        eqCard.addView(bands, blp);

        int[] range = AudioService.eqRange();
        short[] freqs = AudioService.eqFreqs();
        short[] cur = AudioService.eqLevels();
        String[] hzNames = {"60 Hz", "230 Hz", "910 Hz", "3 kHz", "14 kHz"};
        for (int i = 0; i < 5; i++) {
            int hz = (freqs != null && i < freqs.length) ? freqs[i] : (60 + i * 500);
            String label = i < hzNames.length ? hzNames[i]
                    : (hz >= 1000 ? (hz / 1000) + " kHz" : hz + " Hz");
            short lv = (cur != null && i < cur.length) ? cur[i] : 0;
            EqFader f = new EqFader(this, i, range[0], range[1], lv, label);
            f.setOnChange(new EqFader.OnChange() {
                @Override public void onChanged(int band, short level) {
                    AudioService.eqSet(band, level);
                }
            });
            faders[i] = f;
            bands.addView(f, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        }

        TextView eqNote = new TextView(this);
        eqNote.setText(I18n.t(this, "Tut ve sürükle · kalın ses ← → ince ses · ölçüm yayının kendi sesinden"));
        eqNote.setTextColor(Color.parseColor("#5A6B80"));
        eqNote.setTextSize(8.5f);
        eqNote.setGravity(Gravity.CENTER);
        eqNote.setPadding(0, dp(6), 0, 0);
        eqCard.addView(eqNote);
        root.addView(eqCard);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, dp(8), 0, 0);
        TextView speech = chip(I18n.t(this, "🎙 Konuşma"));
        speech.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyPreset(faders, "speech"); }
        });
        TextView music = chip(I18n.t(this, "🎵 Müzik"));
        music.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyPreset(faders, "music"); }
        });
        TextView bass = chip(I18n.t(this, "🔊 Bas"));
        bass.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyPreset(faders, "bass"); }
        });
        TextView flat = chip(I18n.t(this, "➖ Düz"));
        flat.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyPreset(faders, "flat"); }
        });
        row.addView(speech); row.addView(music); row.addView(bass); row.addView(flat);
        root.addView(row);

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(0, dp(12), 0, dp(4));
        TextView prev = bigButton("⏮");
        prev.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { changeStation(-1); }
        });
        TextView play = bigButton("⏸");
        play.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioService.toggleStatic();
                playing = !playing;
                ((TextView) v).setText(playing ? "⏸" : "▶");
            }
        });
        TextView next = bigButton("⏭");
        next.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { changeStation(1); }
        });
        nav.addView(prev); nav.addView(play); nav.addView(next);
        root.addView(nav);

        scroll.addView(root);
        AdManager.wrap(this, scroll, urls.get(index));

        // ONEMLI: istasyonu hemen baslat (AudioService) + ekolayzir animasyonunu calistir
        show();
        ui.post(anim);

        // 40 dakika sonra nazik teklif: "reklamsiz devam etmek ister misin?" (zorlamasiz)
        ui.postDelayed(new Runnable() {
            @Override public void run() { offerAdFree(); }
        }, Ads.ADFREE_OFFER_MINUTES * 60000L);
    }

    /** Uyku zamanlayici: 15/30/60/90 dakika sonra radyoyu durdurur (servis tarafında). */
    private void sleepDialog() {
        final String[] opts = {I18n.t(this, "15 dakika"), I18n.t(this, "30 dakika"),
                I18n.t(this, "60 dakika"), I18n.t(this, "90 dakika"), I18n.t(this, "Kapat")};
        new android.app.AlertDialog.Builder(this)
                .setTitle(I18n.t(this, "Uyku zamanlayıcı"))
                .setItems(opts, new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int w) {
                        if (w == 4) {
                            AudioService.sleepIn(RadioPlayerActivity.this, 0);
                            Toast.makeText(RadioPlayerActivity.this, I18n.t(RadioPlayerActivity.this, "Zamanlayıcı kapalı"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int min = new int[]{15, 30, 60, 90}[w];
                        AudioService.sleepIn(RadioPlayerActivity.this, min * 60000L);
                        Toast.makeText(RadioPlayerActivity.this, String.format(java.util.Locale.ROOT,
                                I18n.t(RadioPlayerActivity.this, "%d dakika sonra radyo duracak"), min), Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    /** Uzun dinlemeden sonra zorlamayan odullu reklam teklifi. */
    private void offerAdFree() {
        if (isFinishing() || AdManager.isAdFreeFor(this, urls.get(index))) return;
        new android.app.AlertDialog.Builder(this)
                .setTitle(I18n.t(this, "Reklamsız devam?"))
                .setMessage(String.format(java.util.Locale.ROOT,
                        I18n.t(this, "Uzun süredir dinliyorsun. İstersen kısa bir video izle, %d dakika boyunca hiç reklam görme. İstemezsen dokunma, dinlemeye aynen devam eder."),
                        Ads.REWARD_MINUTES))
                .setPositiveButton(I18n.t(this, "🎬 Video izle"), new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int w) {
                        AdManager.showRewarded(RadioPlayerActivity.this, urls.get(index), new Runnable() {
                            @Override public void run() {
                                Toast.makeText(RadioPlayerActivity.this, String.format(java.util.Locale.ROOT,
                                        I18n.t(RadioPlayerActivity.this, "Teşekkürler! %d dakika reklamsız."),
                                        Ads.REWARD_MINUTES), Toast.LENGTH_LONG).show();
                            }
                        }, new Runnable() {
                            @Override public void run() {
                                Toast.makeText(RadioPlayerActivity.this,
                                        I18n.t(RadioPlayerActivity.this, "Reklam henüz hazır değil, biraz sonra tekrar dene."),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(I18n.t(this, "Şimdi değil"), null)
                .show();
    }

    private void applyPreset(EqFader[] bars, String kind) {
        int[] range = AudioService.eqRange();
        short[] target;
        if ("speech".equals(kind)) target = new short[]{-300, 300, 500, 300, -200};
        else if ("music".equals(kind)) target = new short[]{200, 100, 0, 150, 250};
        else if ("bass".equals(kind)) target = new short[]{600, 400, 100, 0, -100};
        else target = new short[]{0, 0, 0, 0, 0};
        for (int i = 0; i < bars.length && i < target.length; i++) {
            short lv = target[i];
            if (lv < range[0]) lv = (short) range[0];
            if (lv > range[1]) lv = (short) range[1];
            if (bars[i] != null) bars[i].setLevel(lv);
            AudioService.eqSet(i, lv);
        }
        Toast.makeText(this, I18n.t(this, "Ekolayzır ayarı uygulandı"), Toast.LENGTH_SHORT).show();
    }

    private void changeStation(int d) {
        if (names.isEmpty()) return;
        index = (index + d + names.size()) % names.size();
        show();
    }

    /** Üst üste kaç istasyon açılmadı (sonsuz döngü olmasın). */
    private int radioSkips = 0;

    /** Aynı istasyona kaç kez yeniden bağlanıldı (zayıf internet). */
    private int stationRetries = 0;

    private void show() {
        if (numView != null) numView.setText(String.valueOf(index + 1));
        if (ringGauge != null) {
            ringGauge.setProgress(names.isEmpty() ? 0f : (index + 1f) / names.size());
        }
        nameView.setText(names.get(index));
        metaView.setText(metas.get(index));
        statusView.setText(I18n.t(this, "bağlanıyor…"));
        AudioService.start(this, urls.get(index), names.get(index));
        playing = true;
        stationRetries = 0;
        customStatus = false;
        if (sleepLabel != null)
            sleepLabel.setText(AudioService.sleepLeft() > 0 ? I18n.t(this, "Uyku ✓") : I18n.t(this, "Uyku"));
        // ilk denemede ses gelince durum yazisi "bağlanıyor…" da kalıyordu (9.83)
        ui.removeCallbacks(statusSync);
        ui.post(statusSync);
        ui.removeCallbacks(radioWatchdog);
        ui.postDelayed(radioWatchdog, 15000);
    }

    /**
     * Ses gelmezse istasyonu hemen bırakmıyoruz: zayıf internetde ilk bağlanma 20 sn'yi
     * bulabiliyor. Aynı yayına 2 kez daha bağlanılır, ancak ondan sonra sıradakine geçilir.
     * "Ölü istasyon" işaretlemesi bu yüzden kaldırıldı (yavaş ağ istasyonu öldürmesin).
     */
    private boolean customStatus = false;

    /** Yayin gercekten calmaya baslayinca durum yazisini gunceller. */
    private final Runnable statusSync = new Runnable() {
        @Override public void run() {
            if (!playing) return;
            if (!customStatus && AudioService.isAudibleStatic()) {
                statusView.setText(I18n.t(RadioPlayerActivity.this, "● Canlı yayın"));
                return; // caliyor: izlemeyi birak
            }
            if (!customStatus && stationRetries == 0)
                ui.postDelayed(this, 1000);
        }
    };

    private final Runnable radioWatchdog = new Runnable() {
        @Override public void run() {
            if (names.isEmpty() || index < 0 || index >= urls.size()) return;
            if (AudioService.isAudibleStatic()) {
                radioSkips = 0;
                if (stationRetries > 0) {
                    customStatus = true;
                    statusView.setText(I18n.t(RadioPlayerActivity.this, "✅ Bağlantı iyi · çalıyor"));
                    ui.postDelayed(new Runnable() {
                        @Override public void run() { customStatus = false; stationRetries = 0; }
                    }, 4000);
                }
                return;
            }
            // 9.90: istasyonu kullanıcı istemeden DEĞİŞTİRME.
            // TRT gibi Icecast/AdBreak yayınları reklam kuşağında akışı kesiyor;
            // eski kod 3 denemeden sonra otomatik sıradaki istasyona geçiyordu
            // ("kendiliğinden değişti" şikayeti). Artık AudioService sessizce
            // yeniden bağlanır, ekran yalnız durumu yazar.
            stationRetries++;
            customStatus = true;
            statusView.setText(I18n.t(RadioPlayerActivity.this, "⏳ Yayın bekleniyor · otomatik yeniden bağlanıyor"));
            if (stationRetries == 1 || stationRetries % 10 == 0) {
                CrashLog.event(RadioPlayerActivity.this, "radio",
                        "bekleme " + stationRetries + ": " + urls.get(index));
            }
            ui.postDelayed(this, 4000);
        }
    };

    private final Runnable anim = new Runnable() {
        @Override public void run() {
            boolean playing = AudioService.isPlayingStatic();
            if (ringGauge != null) ringGauge.setLive(playing);
            if (statusView != null && !customStatus) {
                if (AudioService.stateStatic() == AudioService.S_PAUSED) {
                    statusView.setText(I18n.t(RadioPlayerActivity.this, "⏸ Başka uygulama ses çalıyor · kapanınca devam eder"));
                } else if (playing && AudioService.isPlayingStatic()) {
                    statusView.setText(I18n.t(RadioPlayerActivity.this, "✅ Bağlantı iyi · çalıyor"));
                } else if (playing) {
                    statusView.setText(I18n.t(RadioPlayerActivity.this, "⏳ Bağlanıyor…"));
                }
            }
            ui.postDelayed(this, 150);
        }
    };

    private TextView chip(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(TEXT);
        t.setTextSize(12);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(8), dp(12), dp(8));
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor("#2A3444"));
        d.setCornerRadius(dp(16));
        t.setBackground(d);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(3), 0, dp(3), 0);
        t.setLayoutParams(lp);
        return t;
    }

    private TextView bigButton(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(18);
        t.setGravity(Gravity.CENTER);
        GradientDrawable d = new GradientDrawable();
        d.setColor(ORANGE);
        d.setCornerRadius(dp(26));
        t.setBackground(d);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(70), dp(52));
        lp.setMargins(dp(6), 0, dp(6), 0);
        t.setLayoutParams(lp);
        return t;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }

    @Override
    protected void onDestroy() {
        ui.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
