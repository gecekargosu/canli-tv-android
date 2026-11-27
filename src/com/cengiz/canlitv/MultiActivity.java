package com.cengiz.canlitv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/** 1-3 pencere canli TV. Her pencerenin sesi ayri acilip kapanabilir, istenen pencere kaydedilebilir. */
public class MultiActivity extends android.app.Activity {

    private static final int ACCENT = Color.parseColor("#FF7A18");
    private static final int TEXT = Color.parseColor("#FFFFFF");
    private static final int PANEL = Color.parseColor("#CC101319");

    private List<Channel> list;
    private final Slot[] slots = new Slot[4];
    private android.net.ConnectivityManager.NetworkCallback netCb;
    private final boolean[] audible = {true, false, false, false};
    private int paneCount = 3;
    private boolean tablet = false;
    private boolean tracking = false;
    private long openedAt = 0;
    private LinearLayout panelRef;
    private boolean screenOffAudio = false;
    private boolean controlsVisible = true;
    private TextView dataRef;
    private LinearLayout col;
    private TextView clock;
    private HlsRecorder recorder = new HlsRecorder();
    private int recSlot = 1;
    private boolean foreground = true;
    /** Kayıtlı sette olup güncel listede bulunamayan kanal sayısı (kullanıcıya söylenir). */
    private int missingChannels = 0;
    private final Handler ui = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        list = new ArrayList<Channel>(ChannelRepo.all(this));   // ülke değişse de bu ekran etkilenmesin
        // HATA (tur 1): boş listede idx % list.size() ArithmeticException veriyordu.
        if (list.isEmpty()) {
            Toast.makeText(this, I18n.t(this, "Kanal listesi boş. Ayarlar → Ülke bölümünden liste indir."),
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int start = getIntent().getIntExtra("start", -1);
        String saved = getIntent().getStringExtra("set");
        if (saved == null || saved.length() == 0) saved = Prefs.multiLast(this);

        int[] idx = new int[]{start >= 0 ? start : 0, 1, 2, 3};
        boolean[] aud = new boolean[]{true, false, false, false};
        int cnt = 3;
        // HATA: isimli setler ("ad:...") bu kosul yuzunden hic geri yuklenmiyordu.
        if (saved != null && saved.contains("|")) {
            try {
                String[] p = saved.replace("ad:", "").split("\\|");
                String[] parts = p[1].split(",");
                for (int i = 0; i < 4 && i < parts.length; i++) idx[i] = Integer.parseInt(parts[i].trim());
                String mask = p[2];
                for (int i = 0; i < 4 && i < mask.length(); i++) aud[i] = mask.charAt(i) == '1';
                cnt = Integer.parseInt(p[3].trim());
                if (p.length > 4) missingChannels = resolveUrls(p[4], idx);
            } catch (Exception ignored) { }
        }
        for (int i = 0; i < 4; i++) audible[i] = aud[i];
        paneCount = Math.max(1, Math.min(4, cnt));
        tablet = getResources().getConfiguration().smallestScreenWidthDp >= 600;

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < 4; i++) slots[i] = new Slot(i);
        root.addView(col, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // --- kontrol paneli ---
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setGravity(Gravity.CENTER_VERTICAL);
        panel.setPadding(dp(8), dp(6), dp(8), dp(6));
        GradientDrawable pb = new GradientDrawable();
        pb.setColor(PANEL);
        pb.setCornerRadius(dp(14));
        panel.setBackground(pb);
        panelRef = panel;
        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        panelLp.gravity = Gravity.BOTTOM;
        panelLp.setMargins(dp(8), 0, dp(8), dp(10));
        // Dar ekranda panel sığmıyordu: "★ set" düğmesi ekran dışında kalıyordu.
        // Panel yatay kaydırılabilir olsun ki tüm düğmeler erişilebilsin.
        android.widget.HorizontalScrollView panelScroll =
                new android.widget.HorizontalScrollView(this);
        panelScroll.setHorizontalScrollBarEnabled(false);
        panelScroll.setFillViewport(true);
        panelScroll.addView(panel);
        root.addView(panelScroll, panelLp);

        TextView home = chip("🏠");
        home.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { goHome(); }
        });
        home.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                Toast.makeText(MultiActivity.this, "Ana sayfa: kanal listesine dön",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        panel.addView(home);

        TextView stopAll = chip("⏹");
        stopAll.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { stopAllAndClose(); }
        });
        stopAll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                Toast.makeText(MultiActivity.this, "TV'yi kapat: yayını durdurup listeye dön",
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        panel.addView(stopAll);

        for (int i = 1; i <= 4; i++) {
            final int n = i;
            TextView b = chip(i == 4 ? "⊞4" : String.valueOf(i) + "□");
            b.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { setPaneCount(n); }
            });
            panel.addView(b);
        }

        final EditText recNo = new EditText(this);
        recNo.setHint("no");
        recNo.setText(String.valueOf(recSlot));
        recNo.setInputType(InputType.TYPE_CLASS_NUMBER);
        recNo.setTextColor(TEXT);
        recNo.setHintTextColor(Color.parseColor("#8890A0"));
        recNo.setTextSize(12);
        recNo.setWidth(dp(46));
        recNo.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                try { recSlot = Math.max(1, Math.min(4, Integer.parseInt(s.toString().trim()))); }
                catch (Exception ignored) { }
            }
            public void afterTextChanged(Editable s) { }
        });
        panel.addView(recNo);

        final TextView rec = chip("⏺ kayıt");
        rec.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { toggleRecord((TextView) v, recSlot); }
        });
        panel.addView(rec);

        TextView saveSet = chip("★ set");
        saveSet.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { saveSetDialog(); }
        });
        panel.addView(saveSet);

        dataRef = new TextView(this);
        dataRef.setTextColor(Color.parseColor("#CCFFFFFF"));
        dataRef.setTextSize(10);
        dataRef.setPadding(dp(4), 0, dp(4), 0);
        panel.addView(dataRef);

        clock = new TextView(this);
        clock.setTextColor(Color.parseColor("#CCFFFFFF"));
        clock.setTextSize(12);
        clock.setPadding(dp(6), 0, dp(4), 0);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        panel.addView(clock, clp);

        // v10.00: çoklu ekranda da en altta ince "reklamsız" şeridi + banner.
        AdManager.wrap(this, root, null, Prefs.showUsage(this), true);

        for (int i = 0; i < 4; i++) slots[i].setChannel(idx[i] % list.size());
        applyAudio();
        setPaneCount(paneCount);
        watchNetwork();
        ui.postDelayed(watchdog, 6000);
        tracking = true;
        if (missingChannels > 0) {
            final int n = missingChannels;
            ui.postDelayed(new Runnable() {
                @Override public void run() {
                    Toast.makeText(MultiActivity.this, "Kayıtlı setteki " + n
                            + " kanal bu listede yok (kaldırılmış olabilir)", Toast.LENGTH_LONG).show();
                }
            }, 1200);
        }
        openedAt = System.currentTimeMillis();
        ui.post(tick);
        hideControlsSoon();
        // P0-5i: ızgaradan arka plan sesine geçerken de bildirim izni istensin.
        if (android.os.Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 12);
        }
        if (Net.mobile(this) && !Prefs.mobileOk(this)) {
            new AlertDialog.Builder(this).setTitle("Mobil veri uyarısı")
                    .setMessage("Wi-Fi yok. Çoklu ekran mobil veride hızlı tüketir.\n\nBugün: "
                            + Net.mb(Prefs.todayBytes(this)) + "\n\nDevam edilsin mi?")
                    .setPositiveButton("Devam", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface d, int w) {
                            Prefs.setMobileOk(MultiActivity.this, true);
                        }
                    })
                    .setNegativeButton("1. ekran sadece ses", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface d, int w) {
                            Prefs.setMobileOk(MultiActivity.this, true);
                            for (int i = 0; i < 4; i++) audible[i] = (i == 0);
                            applyAudio();
                        }
                    })
                    .show();
        }
    }

    /**
     * Her pencerenin sağlığını 3 saniyede bir denetler.
     * - 15 sn içinde hazırlanamayan yayın → yedek kaynak / taze playlist
     * - hazır ama görüntü gelmeyen (siyah ekran) yayın → yeniden başlat
     * - oynarken konumu 12 sn ilerlemeyen (donan) yayın → yeniden bağlan
     * Böylece ATV gibi boş kalan pencere ve TV8 gibi takılan yayın kendini toparlar.
     */
    // 9.77: çoklu ekranda veri İKİ kez sayılıyordu: hem buradaki TrafficStats
    // farkı hem de onPause'daki Net.begin/end oturumu aynı baytları yazıyordu.
    // Tek kaynak kaldı: Net oturumu (begin/end). Ekrandaki değer
    // todayBytes + Net.sessionSoFar() ile canlı gösteriliyor.

    private final Runnable watchdog = new Runnable() {
        @Override public void run() {
            if (foreground) {
                long now = System.currentTimeMillis();
                for (Slot s : slots) if (s != null) s.check(now);
                flushMulti();
            }
            ui.postDelayed(this, 3000);
        }
    };

    /** Pencerelerin okuduğu baytları günlük sayaca yaz (3 sn'de bir + çıkışta). */
    private void flushMulti() {
        for (Slot s : slots) {
            if (s == null) continue;
            long b = s.bytes.getAndSet(0);
            if (b <= 0) continue;
            int ch = s.chan;
            Prefs.addUsage(this, (ch >= 0 && ch < list.size()) ? list.get(ch).url : "multi", b, 0);
        }
    }

    /** Henüz kayda geçmemiş (yazılmakta olan) baytlar. */
    private long pendingBytes() {
        long t = 0;
        for (Slot s : slots) if (s != null) t += s.bytes.get();
        return t;
    }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            clock.setText(new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                    .format(new java.util.Date()));
            if (dataRef != null) dataRef.setText("📶 " + Net.mb(Prefs.todayBytes(MultiActivity.this)
                    + pendingBytes()));
            ui.postDelayed(this, 1000);
        }
    };

    /** Ekrana dokununca kontrol paneli alttan gelir, 4 sniye sonra kendini gizler. */
    private void toggleControls() {
        controlsVisible = !controlsVisible;
        applyControls();
        if (controlsVisible) hideControlsSoon();
    }

    private void applyControls() {
        if (panelRef != null)
            panelRef.setVisibility(controlsVisible ? View.VISIBLE : View.GONE);
        for (Slot s : slots) if (s != null)
            s.speaker.setVisibility(controlsVisible ? View.VISIBLE : View.GONE);
    }

    private final Runnable hideControls = new Runnable() {
        @Override public void run() {
            controlsVisible = false;
            applyControls();
        }
    };

    private void hideControlsSoon() {
        ui.removeCallbacks(hideControls);
        ui.postDelayed(hideControls, 4500);
    }

    /**
     * K-08: cihazın video çözücü sayısı 4'ten azsa 4'lü modda uyar.
     * (Yazılım çözücüler de sayılır; amaç kesin sayı değil, "zorlanabilir" uyarısı.)
     */
    private void warnIfFourPanesUnsafe() {
        try {
            android.media.MediaCodecList list =
                    new android.media.MediaCodecList(android.media.MediaCodecList.REGULAR_CODECS);
            int decoders = 0;
            for (android.media.MediaCodecInfo info : list.getCodecInfos()) {
                if (info.isEncoder()) continue;
                try {
                    info.getCapabilitiesForType("video/avc");
                    decoders++;
                } catch (Throwable ignored) { }
            }
            if (decoders > 0 && decoders < 4) {
                Toast.makeText(this, "Bu cihazda 4 görüntü aynı anda zorlanabilir; "
                        + "takılırsa 3'e düşür.", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable ignored) { }
    }

    private void setPaneCount(int n) {
        paneCount = n;
        for (int i = 0; i < 4; i++) {
            ViewGroup par = (ViewGroup) slots[i].getParent();
            if (par != null) par.removeView(slots[i]);
        }
        col.removeAllViews();
        if (n == 4) {
            warnIfFourPanesUnsafe();
            LinearLayout r1 = new LinearLayout(this);
            LinearLayout r2 = new LinearLayout(this);
            r1.setOrientation(LinearLayout.HORIZONTAL);
            r2.setOrientation(LinearLayout.HORIZONTAL);
            r1.addView(slots[0], new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            r1.addView(slots[1], new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            r2.addView(slots[2], new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            r2.addView(slots[3], new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            col.addView(r1, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            col.addView(r2, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        } else if (n == 3 && tablet) {
            // TABLET 3 EKRAN: dikey 3 tam genişlik şerit yerine 2x2 ızgara.
            // Her pencere 16:9'a yakın kalır; boş kalan hücre ses kontrol paneline ayrılır.
            LinearLayout r1 = new LinearLayout(this);
            r1.setOrientation(LinearLayout.HORIZONTAL);
            r1.addView(slots[0], new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            r1.addView(slots[1], new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            LinearLayout r2 = new LinearLayout(this);
            r2.setOrientation(LinearLayout.HORIZONTAL);
            r2.addView(slots[2], new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            r2.addView(buildTabletPanel(), new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            col.addView(r1, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            col.addView(r2, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        } else {
            for (int i = 0; i < n; i++) {
                col.addView(slots[i], new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            }
        }
        saveState();
    }

    /** Tablet 3 ekran: 2x2 ızgaranın boş hücresi — marka + pencere ses düğmeleri. */
    private View buildTabletPanel() {
        LinearLayout p = new LinearLayout(this);
        p.setOrientation(LinearLayout.VERTICAL);
        p.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        p.setPadding(dp(12), dp(12), dp(12), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#101A29"));
        bg.setCornerRadius(dp(10));
        p.setBackground(bg);

        TextView brand = new TextView(this);
        brand.setText("UMAY MULTI TV");
        brand.setTextColor(Color.parseColor("#FF8A24"));
        brand.setTextSize(13);
        brand.setGravity(Gravity.CENTER);
        brand.setTypeface(brand.getTypeface(), android.graphics.Typeface.BOLD);
        p.addView(brand);

        TextView sub = new TextView(this);
        sub.setText(I18n.t(this, "3 EKRAN · SES KONTROLÜ"));
        sub.setTextColor(Color.parseColor("#C5CEDA"));
        sub.setTextSize(10);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(2), 0, dp(6));
        p.addView(sub);

        final TextView[] btns = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int k = i;
            TextView b = new TextView(this);
            b.setText((audible[i] ? "🔊 " : "🔇 ") + (i + 1) + ". ekran");
            b.setTextColor(Color.WHITE);
            b.setTextSize(12);
            b.setGravity(Gravity.CENTER);
            b.setPadding(dp(8), dp(7), dp(8), dp(7));
            GradientDrawable bb = new GradientDrawable();
            bb.setColor(Color.parseColor("#1E2A3D"));
            bb.setCornerRadius(dp(8));
            b.setBackground(bb);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(5);
            b.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    for (int j = 0; j < 3; j++) audible[j] = (j == k);
                    applyAudio();
                    saveState();
                    for (int j = 0; j < 3; j++) {
                        btns[j].setText((audible[j] ? "🔊 " : "🔇 ") + (j + 1) + ". ekran");
                    }
                }
            });
            btns[i] = b;
            p.addView(b, lp);
        }

        TextView hint = new TextView(this);
        hint.setText(I18n.t(this, "Pencereye dokun → kanal seç"));
        hint.setTextColor(Color.parseColor("#8FA0B5"));
        hint.setTextSize(10);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, dp(7), 0, 0);
        p.addView(hint);
        return p;
    }

    private void saveState() {
        StringBuilder b = new StringBuilder("__last__|");
        for (int i = 0; i < 4; i++) b.append(slots[i].chan).append(i < 3 ? "," : "");
        b.append("|");
        for (int i = 0; i < 4; i++) b.append(audible[i] ? "1" : "0");
        b.append("|").append(paneCount).append("|").append(encodeUrls());
        Prefs.setMultiLast(this, b.toString());
    }

    private void applyAudio() {
        for (int i = 0; i < 4; i++) slots[i].setAudible(audible[i]);
    }

    private void toggleRecord(final TextView btn, int slotNo) {
        if (recorder.isRunning()) {
            recorder.stop();
            btn.setText("⏺ kayıt");
            return;
        }
        int s = Math.max(0, Math.min(3, slotNo - 1));
        if (slots[s].chan < 0 || slots[s].chan >= list.size()) {
            Toast.makeText(this, I18n.t(this, "Bu ekranda kanal yok"), Toast.LENGTH_SHORT).show();
            return;
        }
        Channel c = list.get(slots[s].chan);
        if (!ChannelRepo.canRecord(c.name)) {
            Toast.makeText(this, I18n.t(this, "Kayıt yalnız kamu kanallarında açık (TRT, TBMM, Diyanet)"),
                    Toast.LENGTH_LONG).show();
            return;
        }
        btn.setText("⏹ durdur");
        recorder.start(this, c.url, "ekran" + (s + 1) + "_" + c.name, new HlsRecorder.Listener() {
            @Override public void onInfo(String msg) { }
            @Override public void onError(final String msg) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        btn.setText("⏺ kayıt");
                        Toast.makeText(MultiActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override public void onStopped(final String path, final long bytes, final boolean sesli) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        btn.setText("⏺ kayıt");
                        Toast.makeText(MultiActivity.this, "Kayıt bitti: " + (bytes / 1048576)
                                + " MB (ses dahil) → Movies/TV-Kayitlari", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        Toast.makeText(this, (s + 1) + I18n.t(this, ". ekran kaydediliyor (ses dahil)"),
                Toast.LENGTH_SHORT).show();
    }

    private void saveSetDialog() {
        final EditText et = new EditText(this);
        et.setHint("Set adı (örn: Spor + Haber)");
        new AlertDialog.Builder(this).setTitle("Bu seti favori yap").setView(et)
                .setPositiveButton("Kaydet", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        String name = et.getText().toString().trim();
                        if (name.length() == 0) name = "Set";
                        StringBuilder b = new StringBuilder("ad:" + name + "|");
                        for (int i = 0; i < 4; i++) b.append(slots[i].chan).append(i < 3 ? "," : "");
                        b.append("|");
                        for (int i = 0; i < 4; i++) b.append(audible[i] ? "1" : "0");
                        b.append("|").append(paneCount).append("|").append(encodeUrls());
                        Prefs.saveMultiSet(MultiActivity.this, b.toString());
                        Toast.makeText(MultiActivity.this, I18n.t(MultiActivity.this, "Kaydedildi: ") + name,
                                Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Vazgeç", null).show();
    }

    /**
     * Kaydedilmiş set: kanal adreslerini indekse çevirir.
     * Adres alanı yoksa (eski kayıtlar) indeks kullanılır.
     */
    private int resolveUrls(String field, int[] idx) {
        if (field == null || !field.startsWith("u:")) return 0;
        String[] parts = field.substring(2).split(",");
        int missing = 0;
        for (int i = 0; i < 4 && i < parts.length; i++) {
            String url;
            try { url = java.net.URLDecoder.decode(parts[i], "UTF-8"); }
            catch (Exception e) { continue; }
            if (url.length() == 0) continue;
            boolean found = false;
            for (int k = 0; k < list.size(); k++) {
                if (list.get(k).url.equals(url)) { idx[i] = k; found = true; break; }
            }
            if (!found) missing++;   // setteki kanal listeden kalkmis
        }
        return missing;
    }

    /**
     * Set metninin son alanı: dört pencerenin kanal adresi.
     * Ülke/liste değişince indeks başka kanalı gösteriyordu; adres sabit kalır.
     */
    private String encodeUrls() {
        StringBuilder b = new StringBuilder("u:");
        for (int i = 0; i < 4; i++) {
            if (i > 0) b.append(',');
            try {
                b.append(java.net.URLEncoder.encode(list.get(slots[i].chan).url, "UTF-8"));
            } catch (Exception ignored) { }
        }
        return b.toString();
    }

    private class Slot extends FrameLayout implements SurfaceHolder.Callback {

        final SurfaceView surface;
        final TextView label, speaker;
        // 9.77: MediaPlayer yerine ExoPlayer — pencere başına bayt sayacı
        // (CountingDataSource) böylece çalışıyor. Eski UID/TrafficStats sayacı
        // bu cihazda 0 dönüyordu ve çoklu ekranda veri HİÇ artmıyordu.
        androidx.media3.exoplayer.ExoPlayer ex;
        final java.util.concurrent.atomic.AtomicLong bytes =
                new java.util.concurrent.atomic.AtomicLong();
        int chan = 0;
        boolean surfaceReady = false, on = false;
        final int slotIndex;

        // Her pencere kendi nesil/deneme sayacını tutar. Önceden tek sayaç paylaşılıyordu:
        // bir pencere hazırlanınca diğerlerinin callback'i "eski" sayılıp iptal ediliyor,
        // o pencere sonsuza kadar boş kalıyordu (ATV'nin ortada boş kalması).
        int gen = 0;
        int attempt = 0;
        long startedAt = 0;
        boolean prepared = false, rendered = false, failed = false;
        long lastPos = -1, lastPosAt = 0;

        Slot(int slotIndex) {
            super(MultiActivity.this);
            this.slotIndex = slotIndex;

            surface = new SurfaceView(MultiActivity.this);
            addView(surface, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            surface.getHolder().addCallback(this);

            label = new TextView(MultiActivity.this);
            label.setTextColor(TEXT);
            label.setTextSize(12);
            label.setPadding(dp(42), dp(5), dp(8), dp(5));
            label.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{Color.parseColor("#AA000000"), Color.parseColor("#00000000")}));
            label.setVisibility(View.GONE);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.TOP;
            addView(label, lp);

            speaker = new TextView(MultiActivity.this);
            speaker.setText("🔊");
            speaker.setTextSize(15);
            speaker.setGravity(Gravity.CENTER);
            GradientDrawable sb = new GradientDrawable();
            sb.setColor(Color.parseColor("#99000000"));
            sb.setCornerRadius(dp(16));
            speaker.setBackground(sb);
            FrameLayout.LayoutParams slp = new FrameLayout.LayoutParams(dp(34), dp(34));
            slp.gravity = Gravity.TOP | Gravity.START;
            slp.setMargins(dp(4), dp(4), 0, 0);
            addView(speaker, slp);
            speaker.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    audible[slotIndex] = !audible[slotIndex];
                    applyAudio();
                    saveState();
                }
            });

            speaker.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    for (int i = 0; i < 4; i++) audible[i] = (i == slotIndex);
                    applyAudio();
                    saveState();
                    Toast.makeText(MultiActivity.this,
                            (slotIndex + 1) + ". ekranın sesi öne alındı", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { toggleControls(); }
            });
            setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    pickChannel(Slot.this);
                    return true;
                }
            });
        }

        void setChannel(int i) {
            chan = Math.max(0, Math.min(list.size() - 1, i));
            reload();
        }

        void setAudible(boolean b) {
            on = b;
            try { if (ex != null) ex.setVolume(b ? 1f : 0f); } catch (Throwable ignored) { }
            // K-08: ses geri açılınca kalite tavanı kalkar.
            applyQualityCap(ex);
            speaker.setText(b ? "🔊" : "🔇");
        }

        /**
         * K-08: sesi kapalı pencere tam kalitede indirmesin. Dört pencere aynı anda
         * 1080p çekince hem veri hem çözücü yetmiyordu; sessiz pencere 640×360 tavanlı.
         */
        void applyQualityCap(androidx.media3.exoplayer.ExoPlayer p) {
            if (p == null) return;
            try {
                androidx.media3.common.TrackSelectionParameters.Builder b =
                        p.getTrackSelectionParameters().buildUpon();
                b.clearOverridesOfType(androidx.media3.common.C.TRACK_TYPE_VIDEO);
                // 9.97: sesli pencerede de tavan var. Dört pencere aynı anda 1080p çekince
                // zayıf Wi-Fi'de hiçbiri beslenemiyordu; 720p tavanı takılmayı belirgin azaltır.
                // Sesli pencere artık master playlist ile açılıyor → ABR tavana kadar kendi düşürür.
                if (on) {
                    // 3+ pencere: sesli pencere 480p tavanlı. Üç akış aynı anda 720p
                    // isteyince tablet gibi cihazlarda çözücü/ağ yetmiyordu.
                    if (paneCount >= 3) {
                        b.setMaxVideoSize(854, 480);
                        b.setMaxVideoBitrate(1200000);
                    } else {
                        b.setMaxVideoSize(1280, 720);
                        b.setMaxVideoBitrate(2500000);
                    }
                } else {
                    b.setMaxVideoSize(640, 360);
                    b.setMaxVideoBitrate(700000);
                }
                p.setTrackSelectionParameters(b.build());
            } catch (Throwable ignored) { }
        }

        void reload() {
            gen++;
            attempt = 0;
            startAttempt();
        }

        /** Sıradaki kaynağı dener: asıl yayın → yedek kaynaklar → taze playlist. */
        private void startAttempt() {
            final int my = gen;
            final Channel c = list.get(chan);
            failed = false;
            prepared = false;
            rendered = false;
            startedAt = System.currentTimeMillis();
            lastPos = -1;
            lastPosAt = startedAt;
            releasePlayer();
            label.setVisibility(View.GONE);
            if (!surfaceReady) return;

            if (attempt == 0) {
                Quality.resolveAdaptive(MultiActivity.this, c.url, new Quality.UrlCallback() {
                    @Override public void onUrl(final String url) {
                        runOnUiThread(new Runnable() {
                            @Override public void run() { open(url, my); }
                        });
                    }
                });
                return;
            }
            String[] fb = ChannelRepo.fallbacks(c.url);
            int fi = attempt - 1;
            if (fb != null && fi < fb.length) {
                open(fb[fi], my);
                return;
            }
            if (attempt <= 4) {
                // Aynı kaynağı taze playlist ile dene (CDN oturumu/token yenilenir).
                label.setText(Lang.t(MultiActivity.this, "multi_reconnect"));
                label.setVisibility(View.VISIBLE);
                Quality.resolveAdaptive(MultiActivity.this, c.url, new Quality.UrlCallback() {
                    @Override public void onUrl(final String url) {
                        runOnUiThread(new Runnable() {
                            @Override public void run() { open(url, my); }
                        });
                    }
                });
                return;
            }
            showFail();
        }

        private void open(String url, int my) {
            if (my != gen || !surfaceReady) return;
            releasePlayer();
            try {
                // Tarayıcı UA'sı: TV8 gibi CDN'ler aksi hâlde reklam kuşağı klibi döndürüyor.
                // 9.97: sabırlı soket (25 sn) + 6 sessiz tekrar + büyük tampon (Tune).
                androidx.media3.datasource.DefaultHttpDataSource.Factory http = Tune.http();
                CountingDataSource.Sink sink = new CountingDataSource.Sink() {
                    @Override public void add(long n) { bytes.addAndGet(n); }
                };
                // v10.05: donanım çözücü çökerse yazılım çözücüye düş (üç pencere aynı anda açarken).
                androidx.media3.exoplayer.DefaultRenderersFactory rfx =
                        new androidx.media3.exoplayer.DefaultRenderersFactory(MultiActivity.this)
                                .setEnableDecoderFallback(true);
                final androidx.media3.exoplayer.ExoPlayer player =
                        new androidx.media3.exoplayer.ExoPlayer.Builder(MultiActivity.this, rfx)
                                .setMediaSourceFactory(Tune.source(new CountingDataSource.Factory(http, sink)))
                                .setLoadControl(Tune.liveVideo())
                                .build();
                ex = player;
                player.setVideoSurfaceView(surface);
                player.setVolume(on ? 1f : 0f);
                applyQualityCap(player);
                player.addListener(new androidx.media3.common.Player.Listener() {
                    @Override public void onPlaybackStateChanged(int state) {
                        if (player != ex) return;
                        if (state == androidx.media3.common.Player.STATE_READY) {
                            prepared = true;
                            startedAt = System.currentTimeMillis();
                            stalls = 0;
                            label.setVisibility(View.GONE);
                            AudioService.stop(MultiActivity.this);
                            screenOffAudio = false;
                        }
                    }
                    // "Hazır" görünüp görüntü vermeyen yayınları yakalamak için ilk kare bilgisi.
                    @Override public void onRenderedFirstFrame() {
                        if (player == ex) rendered = true;
                    }
                    @Override public void onPlayerError(androidx.media3.common.PlaybackException e) {
                        if (player == ex) retry();
                    }
                });
                player.setMediaItem(androidx.media3.common.MediaItem.fromUri(url));
                player.prepare();
                player.play();
            } catch (Throwable e) {
                retry();
            }
        }

        /** Bir sonraki kaynağa geç. */
        /** İnternet geri geldi: boş/hatalı pencereyi kendiliğinden yeniden bağla. */
        void networkBack() {
            try {
                if (ex != null && prepared && !failed) {
                    try { if (ex.isPlaying()) return; } catch (Throwable ignored) { }
                }
                gen++;
                attempt = 0;
                label.setText(I18n.t(MultiActivity.this, "internet geri geldi · bağlanıyor…"));
                label.setVisibility(View.VISIBLE);
                startAttempt();
            } catch (Throwable ignored) { }
        }

        void retry() {
            if (!surfaceReady || failed) return;
            attempt++;
            if (attempt > 8) { showFail(); return; }
            if (attempt > 1) {
                label.setText(Lang.t(MultiActivity.this, "multi_reconnect"));
                label.setVisibility(View.VISIBLE);
            }
            startAttempt();
        }

        void showFail() {
            failed = true;
            releasePlayer();
            Channel c = list.get(chan);
            // K-08: pencere başına durum katmanı — dokununca aynı kanal yeniden denenir.
            label.setText("№" + c.no + " " + c.name + "\n"
                    + Lang.t(MultiActivity.this, "multi_fail"));
            label.setVisibility(View.VISIBLE);
            label.setClickable(true);
            label.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    failed = false;
                    gen++;
                    attempt = 0;
                    label.setText(Lang.t(MultiActivity.this, "multi_reconnect"));
                    startAttempt();
                }
            });
        }

        /** 3 saniyede bir çağrılır: hazırlanmayan, görüntü vermeyen veya donan yayını kurtarır. */
        int stalls = 0;
        long lastBuffered = -1;

        void check(long now) {
            if (!surfaceReady || failed) return;
            if (ex == null) {
                if (startedAt > 0 && now - startedAt > 20000) retry();
                return;
            }
            if (!prepared) {
                if (now - startedAt > 15000) retry();
                return;
            }
            if (!rendered) {
                if (now - startedAt > 10000) retry();
                return;
            }
            long pos;
            try { pos = ex.getCurrentPosition(); } catch (Exception e) { return; }
            if (pos != lastPos) {
                lastPos = pos;
                lastPosAt = now;
                if (stalls > 0) { stalls = 0; label.setVisibility(View.GONE); }
                return;
            }
            if (now - lastPosAt > 22000) {
                // 9.97: 12 sn çok erken bir eşikti; zayıf ağda her kısa boşlukta oynatıcı
                // sıfırdan kuruluyor (yeniden bağlanma) ve takılma uzuyordu. Artık 22 sn
                // bekleniyor ve tampon ilerliyorsa (hâlâ indiriyor) müdahale edilmiyor.
                long buffered = 0;
                try { buffered = ex.getBufferedPosition(); } catch (Throwable ignored) { }
                if (buffered != lastBuffered) {
                    lastBuffered = buffered;
                    lastPosAt = now;
                    return;
                }
                stalls++;
                label.setText((stalls >= 2
                        ? Lang.t(MultiActivity.this, "multi_ad")
                        : Lang.t(MultiActivity.this, "multi_stall"))
                        + "\n\uD83D\uDCF6 ağ " + Tune.mbps(MultiActivity.this));
                label.setVisibility(View.VISIBLE);
                CrashLog.event(MultiActivity.this, "multi",
                        "pencere " + (slotIndex + 1) + " 22 sn dondu, ağ "
                                + Tune.mbps(MultiActivity.this) + ": " + list.get(chan).url);
                retry();
            }
        }

        private void releasePlayer() {
            final androidx.media3.exoplayer.ExoPlayer p = ex;
            ex = null;
            try { if (p != null) p.release(); } catch (Throwable ignored) { }
        }

        void release() {
            gen++;
            releasePlayer();
            prepared = false;
            rendered = false;
            failed = false;
        }

        @Override public void surfaceCreated(SurfaceHolder h) { surfaceReady = true; reload(); }
        @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int hh) { }
        @Override public void surfaceDestroyed(SurfaceHolder h) { surfaceReady = false; release(); }
    }

    /** Aranabilir kanal secimi: numara yaz veya isim yaz. */
    private void pickChannel(final Slot slot) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(8), dp(12), 0);

        final EditText filter = new EditText(this);
        filter.setHint("Kanal no veya ad yaz...");
        filter.setSingleLine(true);
        box.addView(filter);

        final List<String> rows = new ArrayList<String>();
        final List<Integer> idxx = new ArrayList<Integer>();
        final ArrayAdapter<String> ad = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, rows);
        final ListView lv = new ListView(this);
        lv.setAdapter(ad);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(380));
        box.addView(lv, lp);

        final Runnable fill = new Runnable() {
            @Override public void run() {
                String q = filter.getText().toString().trim().toLowerCase();
                rows.clear(); idxx.clear();
                for (int i = 0; i < list.size(); i++) {
                    Channel c = list.get(i);
                    if (q.length() > 0
                            && !c.name.toLowerCase().contains(q)
                            && !c.group.toLowerCase().contains(q)
                            && !String.valueOf(c.no).equals(q)) continue;
                                                rows.add("№" + c.no + "  " + c.name + "   ·  " + Lang.group(MultiActivity.this, c.group));
                    idxx.add(i);
                }
                ad.notifyDataSetChanged();
            }
        };
        filter.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { fill.run(); }
            public void afterTextChanged(Editable s) { }
        });
        fill.run();

        final AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("Kanal seç")
                .setView(box)
                .setNegativeButton("Kapat", null)
                .create();

        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override public void onItemClick(android.widget.AdapterView<?> p, View v, int pos, long id) {
                final int picked = idxx.get(pos);
                dlg.dismiss();
                // v9.25: kanal değişiminde geçiş reklamı (varsa)
                AdManager.maybeInterstitial(MultiActivity.this, list.get(picked).url,
                        new Runnable() {
                            @Override public void run() {
                                if (isFinishing()) return;
                                slot.setChannel(picked);
                                saveState();
                            }
                        });
            }
        });

        dlg.show();
    }

    /**
     * MP-13: geri tuşu rastgele bir ekrana değil, her zaman ana kanal listesine dönsün.
     * (Önceden varsayılan davranış uygulamadan çıkabiliyordu.)
     */
    @Override
    public void onBackPressed() {
        goHome();
    }

    private void goHome() {
        stopAllAndClose();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    /** Butun pencereleri ve arkadaki sesi kapatir, kanal listesine doner. */
    private void stopAllAndClose() {
        for (Slot s : slots) if (s != null) s.release();
        // MP-12: çoklu ekran kapanınca açık kayıt oturumu arka planda dosya yazmaya
        // devam ediyordu; buradan da kapat.
        if (recorder.isRunning()) recorder.stop();
        AudioService.stop(this);
        finish();
    }

    private TextView chip(String s) {
        TextView t = new TextView(this);
        t.setText(I18n.t(this, s));   // v10.05: çoklu ekran düğme etiketleri 8 dilde
        t.setTextColor(TEXT);
        t.setTextSize(12);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(10), dp(8), dp(10), dp(8));
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor("#33FFFFFF"));
        d.setCornerRadius(dp(14));
        t.setBackground(d);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(3), 0, dp(3), 0);
        t.setLayoutParams(lp);
        return t;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }

    @Override
    protected void onPause() {
        super.onPause();
        foreground = false;
        // P0-5c: kapatınca/geri basınca arkada ses devam etmesin.
        if (isFinishing()) return;
        if (tracking) {
            tracking = false;
            flushMulti();
            int sec = (int) ((System.currentTimeMillis() - openedAt) / 1000);
            if (sec > 2) {
                for (int i = 0; i < paneCount && i < slots.length; i++) {
                    if (slots[i] == null) continue;
                    int ch = slots[i].chan;
                    if (ch >= 0 && ch < list.size())
                        Prefs.addUsage(this, list.get(ch).url, 0, sec);
                }
            }
        }
        // ekran kapaninca / uygulamadan cikinca sesli devam: sesi acik pencerenin akisi servise devredilir
        boolean wasPlaying = false;
        for (Slot s : slots) {
            try { if (s.ex != null && s.ex.isPlaying()) wasPlaying = true; } catch (Throwable ignored) { }
        }
        if (wasPlaying) {
            int pick = 0;
            for (int i = 0; i < 4; i++) if (audible[i]) { pick = i; break; }
            Channel c = list.get(slots[pick].chan);
            String q = Prefs.quality(this, c.url);
            AudioService.start(this, q != null ? q : c.url, c.name);
            screenOffAudio = true;
        }
        for (Slot s : slots) {
            try { if (s.ex != null && s.ex.isPlaying()) s.ex.pause(); } catch (Throwable ignored) { }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
        for (Slot s : slots) {
            try { if (s.ex != null && !s.ex.isPlaying()) s.ex.play(); } catch (Throwable ignored) { }
        }
        if (screenOffAudio) {
            screenOffAudio = false;
            AudioService.stop(this);
        }
        if (!tracking) { tracking = true; openedAt = System.currentTimeMillis(); }
    }

    /** M-80: ağ geri gelince pencereleri kendiliğinden toparla. */
    private void watchNetwork() {
        try {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            netCb = new android.net.ConnectivityManager.NetworkCallback() {
                @Override public void onAvailable(android.net.Network n) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            for (Slot s : slots) if (s != null) s.networkBack();
                        }
                    });
                }
            };
            cm.registerDefaultNetworkCallback(netCb);
        } catch (Throwable ignored) { }
    }

    @Override
    protected void onDestroy() {
        ui.removeCallbacksAndMessages(null);
        if (recorder.isRunning()) recorder.stop();
        try {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (netCb != null) cm.unregisterNetworkCallback(netCb);
        } catch (Throwable ignored) { }
        for (Slot s : slots) if (s != null) s.release();
        super.onDestroy();
    }
}
