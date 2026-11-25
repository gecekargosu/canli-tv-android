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
    private TextView nameView, metaView, statusView, sleepLabel;
    private EqView eqView;
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
            Toast.makeText(this, "Radyo listesi hazır değil. Radyo ekranından yeniden dene.",
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
                statusView.setText("durduruldu");
                playing = false;
            }
        });
        head.addView(stopBtn);
        root.addView(head);

        // --- radyo paneli: bos cerceve yerine istasyon bilgisi + canli yayin etiketi ---
        android.widget.FrameLayout panel = new android.widget.FrameLayout(this);
        RadioArt art = new RadioArt(this);
        panel.addView(art, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(dp(26), dp(30), dp(26), dp(18));

        TextView live = new TextView(this);
        live.setText("● CANLI YAYIN");
        live.setTextColor(ORANGE);
        live.setTextSize(10);
        live.setTypeface(live.getTypeface(), android.graphics.Typeface.BOLD);
        live.setLetterSpacing(0.12f);
        live.setGravity(Gravity.CENTER);
        overlay.addView(live);

        nameView = new TextView(this);
        nameView.setTextColor(TEXT);
        nameView.setTextSize(21);
        nameView.setGravity(Gravity.CENTER);
        nameView.setTypeface(nameView.getTypeface(), android.graphics.Typeface.BOLD);
        nameView.setPadding(0, dp(6), 0, dp(2));
        nameView.setShadowLayer(8f, 0f, 2f, Color.parseColor("#000000"));
        overlay.addView(nameView);

        metaView = new TextView(this);
        metaView.setTextColor(Color.parseColor("#C7D2E0"));
        metaView.setTextSize(11);
        metaView.setGravity(Gravity.CENTER);
        overlay.addView(metaView);

        TextView freq = new TextView(this);
        freq.setText("UMAY RADYO · canlı akış");
        freq.setTextColor(MUTED);
        freq.setTextSize(9);
        freq.setGravity(Gravity.CENTER);
        freq.setPadding(0, dp(10), 0, 0);
        overlay.addView(freq);

        panel.addView(overlay, new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(panel, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(210)));

        statusView = new TextView(this);
        statusView.setTextColor(ORANGE);
        statusView.setTextSize(11);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, dp(2), 0, dp(6));
        root.addView(statusView);

        // Baglanti sorunu icin elle mudahale: tekrar dene / sonraki kanal
        LinearLayout fixRow = new LinearLayout(this);
        fixRow.setGravity(Gravity.CENTER);
        TextView retryBtn = chip("🔄 Tekrar dene");
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                statusView.setText("🔄 Yeniden bağlanıyorum…");
                customStatus = true;
                stationRetries = 0;
                AudioService.start(RadioPlayerActivity.this, urls.get(index), names.get(index));
                ui.removeCallbacks(radioWatchdog);
                ui.postDelayed(radioWatchdog, 15000);
            }
        });
        TextView nextBtn = chip("⏭ Sonraki kanal");
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { changeStation(1); }
        });
        fixRow.addView(retryBtn);
        fixRow.addView(nextBtn);
        root.addView(fixRow);

        eqView = new EqView(this);
        root.addView(eqView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(60)));

        // --- bant ayarlari (gercek Equalizer) ---
        TextView eqTitle = new TextView(this);
        eqTitle.setText("Ekolayzır ayarları");
        eqTitle.setTextColor(TEXT);
        eqTitle.setTextSize(13);
        eqTitle.setPadding(0, dp(10), 0, dp(4));
        root.addView(eqTitle);

        LinearLayout bands = new LinearLayout(this);
        bands.setOrientation(LinearLayout.HORIZONTAL);
        bands.setGravity(Gravity.CENTER);
        root.addView(bands, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(170)));

        final int bandCount = 5;
        final SeekBar[] bars = new SeekBar[bandCount];
        final TextView[] labels = new TextView[bandCount];
        int[] range = AudioService.eqRange();
        short[] freqs = AudioService.eqFreqs();
        short[] cur = AudioService.eqLevels();
        for (int i = 0; i < bandCount; i++) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView f = new TextView(this);
            int hz = (freqs != null && i < freqs.length) ? freqs[i] : (60 + i * 500);
            f.setText(hz >= 1000 ? (hz / 1000) + "k" : String.valueOf(hz));
            f.setTextColor(MUTED);
            f.setTextSize(10);
            f.setGravity(Gravity.CENTER);
            labels[i] = f;
            col.addView(f);

            SeekBar sb = new SeekBar(this);
            sb.setMax(range[1] - range[0]);
            int level = (cur != null && i < cur.length) ? cur[i] : 0;
            sb.setProgress(level - range[0]);
            sb.setRotation(270);
            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                    if (!fromUser) return;
                    int band = indexOf(s, bars);
                    if (band >= 0) AudioService.eqSet(band, (short) (p + AudioService.eqRange()[0]));
                }
                @Override public void onStartTrackingTouch(SeekBar s) { }
                @Override public void onStopTrackingTouch(SeekBar s) { }
            });
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(dp(46), dp(120));
            col.addView(sb, slp);
            bars[i] = sb;
            bands.addView(col, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }

        TextView presets = new TextView(this);
        presets.setText("Hazır ayarlar:  Konuşma · Müzik · Bas · Düz");
        presets.setTextColor(MUTED);
        presets.setTextSize(10);
        presets.setGravity(Gravity.CENTER);
        presets.setPadding(0, dp(4), 0, dp(10));
        root.addView(presets);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        TextView speech = chip("🎙 Konuşma");
        speech.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyPreset(bars, "speech"); }
        });
        TextView music = chip("🎵 Müzik");
        music.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyPreset(bars, "music"); }
        });
        TextView bass = chip("🔊 Bas");
        bass.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyPreset(bars, "bass"); }
        });
        TextView flat = chip("➖ Düz");
        flat.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { applyPreset(bars, "flat"); }
        });
        row.addView(speech); row.addView(music); row.addView(bass); row.addView(flat);
        root.addView(row);

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(0, dp(14), 0, 0);
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
        final String[] opts = {"15 dakika", "30 dakika", "60 dakika", "90 dakika", "Kapat"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Uyku zamanlayıcı")
                .setItems(opts, new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int w) {
                        if (w == 4) {
                            AudioService.sleepIn(RadioPlayerActivity.this, 0);
                            Toast.makeText(RadioPlayerActivity.this, "Zamanlayıcı kapalı",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int min = new int[]{15, 30, 60, 90}[w];
                        AudioService.sleepIn(RadioPlayerActivity.this, min * 60000L);
                        Toast.makeText(RadioPlayerActivity.this,
                                min + " dakika sonra radyo duracak", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    /** Uzun dinlemeden sonra zorlamayan odullu reklam teklifi. */
    private void offerAdFree() {
        if (isFinishing() || AdManager.isAdFreeFor(this, urls.get(index))) return;
        new android.app.AlertDialog.Builder(this)
                .setTitle("Reklamsız devam?")
                .setMessage("Uzun süredir dinliyorsun. İstersen kısa bir video izle, "
                        + Ads.REWARD_MINUTES + " dakika boyunca hiç reklam görme. İstemezsen dokunma, "
                        + "dinlemeye aynen devam eder.")
                .setPositiveButton("🎬 Video izle", new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int w) {
                        AdManager.showRewarded(RadioPlayerActivity.this, urls.get(index), new Runnable() {
                            @Override public void run() {
                                Toast.makeText(RadioPlayerActivity.this, "Teşekkürler! "
                                        + Ads.REWARD_MINUTES + " dakika reklamsız.", Toast.LENGTH_LONG).show();
                            }
                        }, new Runnable() {
                            @Override public void run() {
                                Toast.makeText(RadioPlayerActivity.this,
                                        "Reklam henüz hazır değil, biraz sonra tekrar dene.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Şimdi değil", null)
                .show();
    }

    private int indexOf(SeekBar s, SeekBar[] arr) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == s) return i;
        return -1;
    }

    private void applyPreset(SeekBar[] bars, String kind) {
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
            bars[i].setProgress(lv - range[0]);
            AudioService.eqSet(i, lv);
        }
        Toast.makeText(this, "Ekolayzır ayarı uygulandı", Toast.LENGTH_SHORT).show();
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
        nameView.setText(names.get(index));
        metaView.setText(metas.get(index));
        statusView.setText("bağlanıyor…");
        AudioService.start(this, urls.get(index), names.get(index));
        playing = true;
        stationRetries = 0;
        customStatus = false;
        if (sleepLabel != null)
            sleepLabel.setText(AudioService.sleepLeft() > 0 ? "Uyku ✓" : "Uyku");
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
                statusView.setText("● Canlı yayın");
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
                    statusView.setText("✅ Bağlantı iyi · çalıyor");
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
            statusView.setText("⏳ Yayın bekleniyor · otomatik yeniden bağlanıyor");
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
            if (eqView != null) {
                eqView.setActive(playing);   // cubuklar sadece ses varken hareket eder
                eqView.invalidate();
            }
            if (statusView != null && !customStatus) {
                if (AudioService.stateStatic() == AudioService.S_PAUSED) {
                    statusView.setText("⏸ Başka uygulama ses çalıyor · kapanınca devam eder");
                } else if (playing && AudioService.isPlayingStatic()) {
                    statusView.setText("✅ Bağlantı iyi · çalıyor");
                } else if (playing) {
                    statusView.setText("⏳ Bağlanıyor…");
                }
            }
            ui.postDelayed(this, 150);
        }
    };

    // ---------- animasyonlu ekolayzir ----------
    private static class EqView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float[] h = new float[22];
        private final float[] v = new float[22];
        private boolean active = false;
        void setActive(boolean a) { active = a; }
        EqView(Context c) {
            super(c);
            for (int i = 0; i < h.length; i++) { h[i] = (float) Math.random(); v[i] = 0.02f; }
        }
        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), ht = getHeight();
            int n = h.length;
            float bw = w / (n * 1.8f);
            for (int i = 0; i < n; i++) {
                float target = active
                        ? 0.15f + (float) Math.abs(Math.sin(System.currentTimeMillis() / 380.0 + i * 0.7)) * 0.8f
                        : 0.04f;
                v[i] += (target - v[i]) * 0.15f;
                float bh = ht * 0.9f * v[i];
                float x = i * (w / n) + bw / 2;
                p.setColor(i % 3 == 0 ? Color.parseColor("#FF7A18")
                        : (i % 3 == 1 ? Color.parseColor("#1F3A93") : Color.parseColor("#F5F7FA")));
                p.setAlpha(230);
                RectF r = new RectF(x, ht - bh, x + bw, ht);
                canvas.drawRoundRect(r, bw / 2, bw / 2, p);
            }
        }
    }

    // ---------- analog radyo cizimi ----------
    private static class RadioArt extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        RadioArt(Context c) { super(c); }
        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            float m = w * 0.04f;
            p.setColor(BG);
            canvas.drawRect(0, 0, w, h, p);
            RectF body = new RectF(m, h * 0.06f, w - m, h * 0.96f);
            p.setColor(Color.parseColor("#1B2230"));
            canvas.drawRoundRect(body, 26, 26, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(NAVY);
            canvas.drawRoundRect(body, 26, 26, p);
            p.setStyle(Paint.Style.FILL);

            // hoparlor izgarasi
            float gx = body.left + body.width() * 0.07f, gy = body.top + body.height() * 0.18f;
            p.setColor(Color.parseColor("#2A3444"));
            for (int r = 0; r < 6; r++)
                for (int c = 0; c < 5; c++)
                    canvas.drawCircle(gx + c * 13, gy + r * 13, 3.4f, p);

            // kadran
            RectF dial = new RectF(body.left + body.width() * 0.34f, body.top + body.height() * 0.16f,
                    body.right - body.width() * 0.08f, body.top + body.height() * 0.46f);
            // kadran: koyu zemin + ince olcek cizgileri + turuncu ibre
            p.setColor(Color.parseColor("#0E1319"));
            canvas.drawRoundRect(dial, 10, 10, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2);
            p.setColor(Color.parseColor("#2A3444"));
            canvas.drawRoundRect(dial, 10, 10, p);
            p.setColor(Color.parseColor("#8C98A8"));
            for (int i = 0; i <= 20; i++) {
                float x = dial.left + 6 + (dial.width() - 12) * i / 20f;
                float len = (i % 5 == 0) ? 12 : 6;
                canvas.drawLine(x, dial.top + 7, x, dial.top + 7 + len, p);
            }
            p.setStrokeWidth(3);
            p.setColor(ORANGE);
            float nx = dial.left + 6 + (dial.width() - 12) * 0.62f;
            canvas.drawLine(nx, dial.top + 5, nx, dial.bottom - 5, p);
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(1);

            // iki dugme
            p.setColor(ORANGE);
            canvas.drawCircle(body.left + body.width() * 0.18f, body.bottom - body.height() * 0.22f, 20, p);
            p.setColor(NAVY);
            canvas.drawCircle(body.left + body.width() * 0.40f, body.bottom - body.height() * 0.22f, 20, p);

            // marka adı panelde yazıyor; çizimde tekrar etmiyoruz.
        }
    }

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
