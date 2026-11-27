package com.cengiz.canlitv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.ads.AdView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends android.app.Activity
        implements MainHeader.Host, ChannelRow.Host, ChannelEdit.Host {

    // v10.08: renkler ve kutu yardımcıları MainUi'de (MainActivity bölündü).
    private static final int BG = MainUi.BG;
    private static final int CARD = MainUi.CARD;
    private static final int ACCENT = MainUi.ACCENT;
    private static final int TEXT = MainUi.TEXT;
    private static final int MUTED = MainUi.MUTED;
    private static final int SOFT = MainUi.SOFT;
    private final MainUi ui = new MainUi(this);

    private final List<Channel> shown = new ArrayList<Channel>();
    private List<Channel> all;
    private ChannelAdapter adapter;
    private String group = "Tümü";
    private String query = "";
    private LinearLayout chipsRow;
    private TextView dataBar;
    // M-73: radyo çalarken ana ekranda kapatma şeridi
    private LinearLayout radioNow;
    private TextView radioNowText;
    private TextView emptyView;
    private RecyclerView listView;
    private boolean onlyHidden = false;
    /** En son bilinen abonelik durumu (değişince ekran yeniden kurulur). */
    private boolean subState;
    private String shownCountry = "";
    private String shownLang = "";

    // --- liste duzenleme (v10.10: secim/siralama mantigi ChannelEdit'te) ---
    private final ChannelEdit edit = new ChannelEdit(this);
    private LinearLayout editBar;
    private TextView editBtn, selCount, qualityBtn, editIcon;
    private LinearLayout editBox, qualityBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Telefonda dikey kal; tablette yatay/dikey serbest olsun. Manifest portre
        // kilitli olduğu için yatay tablete geçince ekran dönmüyordu.
        if (getResources().getConfiguration().smallestScreenWidthDp >= 600) {
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        // Kanal aramasında da klavye listeyi yutmasın.
        getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        CrashLog.install(this);   // cokme olursa cihaza log yaz (Ayarlar -> Hata günlüğü)
        CrashLog.uploadIfDue(this);   // kullanıcı izin verdiyse günde bir kez sunucuya gönder
        ChannelReport.flush(this);    // v10.09: internet yokken kuyrukta kalan kanal bildirimleri
        // Hata günlüğü: hangi sürüm, hangi ülke, hangi mod ile açıldı.
        CrashLog.event(this, "app", "açılış " + BuildConfig.VERSION_NAME
                + " · ülke=" + Prefs.country(this)
                + " · ekonomi=" + (Prefs.economy(this) ? "1" : "0")
                + " · dil=" + Prefs.uiLang(this));
        // 9.92: yeniden paketlenmiş (logo/adresi değiştirilmiş) kopya koruması.
        // Play'den kurulan sürümler etkilenmez; yalnızca bizim sertifikamızla
        // imzalanmamış APK'lar engellenir.
        if (!Guard.official(this)) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle(I18n.t(this, "Resmi olmayan sürüm"))
                    .setMessage(I18n.t(this, "Bu kopya UMAY Multi TV'nin resmi sürümü değil.\n\nUygulama değiştirilmiş olabilir; güvenliğin için çalıştırılmıyor.\nResmi sürümü Google Play'den veya umay-multi-tv-gq3zer.freebuff.site adresinden yükle."))
                    .setCancelable(false)
                    .setPositiveButton(I18n.t(this, "Kapat"), new android.content.DialogInterface.OnClickListener() {
                        @Override public void onClick(android.content.DialogInterface d, int w) { finish(); }
                    })
                    .show();
            return;
        }
        Prefs.migrateNumbers(this);   // 9.75: eski numara/sıra kayıtları bir kez temizlenir
        setupFirstRun();              // v10.04: ilk açılışta cihaz dili + cihaz ülkesi
        Good.refreshIfNeeded(this);   // #101: doğrulanmış kanal listesi günde 1 kez tazelenir
        all = ChannelRepo.all(this);
        subState = Prefs.subActive(this);
        Billing.refresh(this);   // abonelik durumu Play'den tazelenir
        shownCountry = Prefs.country(this);
        shownLang = Prefs.uiLang(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        // 9.75: durum çubuğu payı iki kez uygulanıyordu (başlığın üstünde ~55 dp
        // boşluk kalıyordu). Pencere zaten güvenli alanın altında başladığı için
        // fitSystemWindows ek payı ikinci kez ekliyordu; tek pay yeter.
        root.setFitsSystemWindows(false);
        // K-18: tablet / geniş ekranda içerik 560dp'yi geçmesin.
        // Aksi halde üst bar kutuları ve liste satırları ekran boyunca geriliyor,
        // ikonlar ortada kalıyor, alt durum şeridi taşıyordu.
        // K-18: başlık 560dp'de ortalanır; kanal listesi tablette tam genişliği kullanır.
        // Önceden tüm kök 560dp'ye sıkıştırıldığı için tablette yanlarda büyük boşluk kalıyordu.
        int sidePad = 0;
        try {
            int wDp = getResources().getConfiguration().screenWidthDp;
            if (wDp > 560) {
                sidePad = (int) ((wDp - 560) / 2f * getResources().getDisplayMetrics().density);
            }
        } catch (Throwable ignored) { }

        // v10.08: üst çubuk (marka + 6 ikon) MainHeader sınıfında kurulur.
        root.addView(MainHeader.build(this, ui, Countries.flag(shownCountry), sidePad, this));

        // --- radyo çalarken her zaman görünen kapatma şeridi ---
        radioNow = new LinearLayout(this);
        radioNow.setOrientation(LinearLayout.HORIZONTAL);
        radioNow.setGravity(Gravity.CENTER_VERTICAL);
        radioNow.setPadding(dp(12), dp(6), dp(8), dp(6));
        radioNow.setBackground(rounded(SOFT, 12));
        LinearLayout.LayoutParams rnlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rnlp.setMargins(dp(14), 0, dp(14), dp(6));
        radioNow.setLayoutParams(rnlp);
        radioNow.setVisibility(View.GONE);

        radioNowText = new TextView(this);
        radioNowText.setTextSize(12);
        radioNowText.setTextColor(TEXT);
        radioNow.addView(radioNowText, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView radioStop = new TextView(this);
        radioStop.setText(I18n.t(this, "⏹ Kapat"));
        radioStop.setTextSize(12);
        radioStop.setTextColor(Color.parseColor("#111111"));
        radioStop.setTypeface(radioStop.getTypeface(), android.graphics.Typeface.BOLD);
        radioStop.setPadding(dp(12), dp(5), dp(12), dp(5));
        radioStop.setBackground(rounded(ACCENT, 10));
        radioStop.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioService.stop(MainActivity.this);
                radioNow.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, I18n.t(MainActivity.this, "Radyo kapatıldı"), Toast.LENGTH_SHORT).show();
            }
        });
        radioNow.addView(radioStop);
        radioNow.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioService.stop(MainActivity.this);
                radioNow.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, I18n.t(MainActivity.this, "Radyo kapatıldı"), Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(radioNow);

        // --- arama + kanal no ---
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams srlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        srlp.setMargins(dp(12), dp(6), dp(12), dp(4));
        searchRow.setLayoutParams(srlp);

        EditText search = new EditText(this);
        search.setHint(Lang.t(this, "search"));
        search.setHintTextColor(MUTED);
        search.setTextColor(TEXT);
        search.setTextSize(14);
        search.setSingleLine(true);
        GradientDrawable sbg = rounded(CARD, 14);
        search.setBackground(sbg);
        search.setPadding(dp(10), 0, dp(10), 0);
        searchRow.addView(search, new LinearLayout.LayoutParams(0, dp(44), 1f));
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                query = Lang.fold(s.toString().trim());
                refresh();
            }
            public void afterTextChanged(Editable s) { }
        });

        final EditText noInput = new EditText(this);
        noInput.setHint(I18n.t(this, "No"));
        noInput.setHintTextColor(MUTED);
        noInput.setTextColor(TEXT);
        noInput.setTextSize(14);
        noInput.setSingleLine(true);
        noInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        noInput.setGravity(Gravity.CENTER);
        noInput.setPadding(0, 0, 0, 0);
        noInput.setBackground(rounded(CARD, 14));
        // 9.79: No ve Git kutuları arama kutusuyla aynı 44dp yükseklikte, daha dar.
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(dp(52), dp(44));
        nlp.setMargins(dp(5), 0, dp(5), 0);
        noInput.setLayoutParams(nlp);
        searchRow.addView(noInput);

        TextView go = pill(I18n.t(this, "Git"), ACCENT);
        go.setTextSize(14);
        go.setPadding(dp(14), 0, dp(14), 0);
        go.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(44)));
        go.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    int no = Integer.parseInt(noInput.getText().toString().trim());
                    gotoNumber(no);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, I18n.t(MainActivity.this, "Kanal numarası yaz"), Toast.LENGTH_SHORT).show();
                }
            }
        });
        searchRow.addView(go);

        // 9.78: sıra değişti — sekmeler üstte, arama satırı listeye bitişik altta.
        // Arama kanalla ilgili olduğu için kanal listesinin hemen üstünde duruyor.

        // --- cipler: 4 sekme, ekran genişliğine eşit dağılır (sağa kaymaz) ---
        // 3. satir: 6 kutu, 2. satirla ayni 12dp kenarlar ve ayni 44dp yukseklik.
        chipsRow = new LinearLayout(this);
        chipsRow.setOrientation(LinearLayout.HORIZONTAL);
        chipsRow.setGravity(Gravity.CENTER_VERTICAL);
        chipsRow.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams crlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        crlp.topMargin = dp(6);
        chipsRow.setLayoutParams(crlp);
        root.addView(chipsRow);
        buildChips();
        root.addView(searchRow);

        // 9.77: Düzenle/Sırala satırı kaldırıldı. Sırala işe yaramıyordu; Düzenle
        // sekme satırının sonuna taşındı. Kanal sayısı alt veri şeridinde.

        // --- duzenleme cubugu ---
        editBar = new LinearLayout(this);
        editBar.setOrientation(LinearLayout.HORIZONTAL);
        editBar.setGravity(Gravity.CENTER_VERTICAL);
        editBar.setPadding(dp(14), dp(2), dp(14), dp(6));
        editBar.setVisibility(View.GONE);

        TextView doneBtn = mini("✓ Bitti");
        doneBtn.setBackground(rounded(ACCENT, 14));
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { edit.setMode(false); }
        });
        editBar.addView(doneBtn);

        selCount = new TextView(this);
        selCount.setTextColor(MUTED);
        selCount.setTextSize(10);
        LinearLayout.LayoutParams sclp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        sclp.setMargins(dp(8), 0, dp(4), 0);
        editBar.addView(selCount, sclp);

        TextView upBtn = mini("▲");
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { edit.moveSelected(-1); }
        });
        editBar.addView(upBtn);

        TextView downBtn = mini("▼");
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { edit.moveSelected(1); }
        });
        editBar.addView(downBtn);

        TextView topBtn = mini("⤒");
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { edit.toEdge(true); }
        });
        editBar.addView(topBtn);

        TextView bottomBtn = mini("⤓");
        bottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { edit.toEdge(false); }
        });
        editBar.addView(bottomBtn);

        TextView delBtn = mini("🗑");
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { edit.deleteSelected(); }
        });
        editBar.addView(delBtn);

        root.addView(editBar);

        // --- liste (K-16: RecyclerView) ---
        RecyclerView list = new RecyclerView(this);
        listView = list;
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setHasFixedSize(true);
        list.setPadding(dp(12), dp(4), dp(12), dp(12));
        list.setClipToPadding(false);
        list.setBackgroundColor(Color.TRANSPARENT);
        final int gap = dp(6);
        list.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override public void getItemOffsets(Rect out, View view, RecyclerView parent,
                                                 RecyclerView.State state) {
                out.bottom = gap;
            }
        });
        adapter = new ChannelAdapter();
        list.setAdapter(adapter);

        emptyView = new TextView(this);
        emptyView.setTextColor(MUTED);
        emptyView.setTextSize(13);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setPadding(dp(24), dp(40), dp(24), dp(24));
        emptyView.setVisibility(View.GONE);
        root.addView(emptyView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // Veri şeridi: tek yerde, listenin altında sabit (üstteki kopya kaldırıldı).
        dataBar = new TextView(this);
        dataBar.setTextSize(11);
        dataBar.setTextColor(MUTED);
        dataBar.setPadding(dp(12), dp(6), dp(12), dp(6));
        dataBar.setSingleLine(true);
        dataBar.setEllipsize(android.text.TextUtils.TruncateAt.END);
        root.addView(dataBar);

        // reklam: alt bar sabit dursun (liste kaydırılınca kaybolmasın)
        // usage=false → AdManager kendi veri satırını eklemez, yukarıdaki şerit tek kaynak.
        AdManager.wrap(this, root, null, false);
        AdManager.init(this);
        handleExtras(getIntent());
        refresh();
    }

    /**
     * Ilk acilista IP'den ulke tahmini yapar; desteklenen ve mevcut ulkeden farkliysa
     * "kanallari buna gore yukleyeyim mi?" diye sorar (bir kez).
     */
    /**
     * v10.04 (ilk açılış): ülke sorulmaz, cihazdan bulunur.
     * 1) Arayüz dili cihaz diline göre seçilir (desteklenen dillerden biri ise).
     * 2) Ülke SIM/ağ ülkesine, olmazsa cihaz yereline göre seçilir.
     * 3) O ülkenin listesi yoksa arka planda indirilir; inemezse Türkiye listesinde kalınır.
     * Kullanıcı sonradan Ayarlar → Ülke/Dil ile değiştirebilir (o zaman bu blok çalışmaz).
     */
    private void setupFirstRun() {
        if (Prefs.firstRunDone(this)) return;
        Prefs.setFirstRunDone(this, true);

        // --- 1) Arayüz dili (önce uygulama/cihaz yapılandırması, sonra Locale) ---
        try {
            String lang = getResources().getConfiguration().getLocales().get(0).getLanguage();
            if (lang == null || lang.length() == 0) {
                lang = java.util.Locale.getDefault().getLanguage();
            }
            for (String code : Lang.CODES) {
                if (code.equals(lang)) { Prefs.setUiLang(this, code); break; }
            }
        } catch (Throwable ignored) { }

        // --- 2) Ülke (IP kullanılmaz; SIM/ağ + cihaz yereli) ---
        String cc = null;
        try {
            android.telephony.TelephonyManager tm =
                    (android.telephony.TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (tm != null) {
                cc = tm.getNetworkCountryIso();
                if (cc == null || cc.length() != 2) cc = tm.getSimCountryIso();
            }
        } catch (Throwable ignored) { }
        if (cc == null || cc.length() != 2) {
            try {
                cc = getResources().getConfiguration().getLocales().get(0).getCountry();
            } catch (Throwable ignored) { }
        }
        if (cc == null || cc.length() != 2) return;
        final String found = cc.toLowerCase(java.util.Locale.US);
        final String prev = Prefs.country(this);
        if (!Countries.supported(found) || found.equals(prev)) return;
        Prefs.setCountry(this, found);
        // Liste hazırsa (veya TR gömülü ise) ek bir şey gerekmez: all() yeni ülkeyi okur.
        if ("tr".equals(found) || CountryRepo.isReady(this, found)) return;

        // --- 3) Liste yok: indir, sonra ekranı tazele ---
        new Thread(new Runnable() {
            @Override public void run() {
                final int st = CountryRepo.downloadStatus(MainActivity.this, found);
                if (st == CountryRepo.DL_OK) CountryRepo.fetchGood(MainActivity.this, found);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (st == CountryRepo.DL_OK) {
                            ChannelRepo.reload(MainActivity.this);
                            recreate();
                        } else {
                            Prefs.setCountry(MainActivity.this, prev);   // inemezse eski ülkede kal
                        }
                    }
                });
            }
        }, "first-run-country").start();
    }

    /** Sekme satırına sığsın diye kalite etiketini kısaltır (Otomatik → Oto). */
    private String shortQuality() {
        String l = QualityDialog.label(this);
        if (l == null) return Lang.t(this, "q_auto_short");
        if (l.length() > 6) return Lang.t(this, "q_auto_short");
        return l;
    }

    private void buildChips() {
        chipsRow.removeAllViews();
        // 9.76: grup sekmeleri (Haber/Spor/Belgesel…) kaldırıldı; dört sekme ekran
        // genişliğine eşit dağılır, sağa kaydırma gerekmez.
        List<String> cats = new ArrayList<String>();
        cats.add("Tümü");
        cats.add("★ Favoriler");
        cats.add("🕘 Son");
        cats.add("🗑 Gizlenenler");
        // 9.82: her sekme ikon + altında küçük yazı (kutu yüksekliği 44dp kalıyor).
        String[] tabIcons = { "🗂", "★", "🕘", "🗑" };
        // v10.04 (Paket C): sekme adları arayüz diline bağlı (mantık anahtarı Türkçe kalır)
        String[] tabLabels = {
                Lang.tabShort(this, "Tümü"),
                Lang.tabShort(this, "★ Favoriler"),
                Lang.tabShort(this, "🕘 Son"),
                Lang.tabShort(this, "🗑 Gizlenenler")
        };
        for (int i = 0; i < cats.size(); i++) {
            final String c = cats.get(i);
            LinearLayout box = tabBox(tabIcons[i], tabLabels[i], null);
            box.setTag(c);
            box.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    group = c;
                    onlyHidden = "🗑 Gizlenenler".equals(c);
                    edit.clear();
                    styleChips();
                    refresh();
                }
            });
            box.setLayoutParams(slot(i));
            chipsRow.addView(box);
        }
        // Kalite seçimi: ikon + altında kısa etiket.
        TextView[] qLab = new TextView[1];
        qualityBox = tabBox("🎚", shortQuality(), qLab);
        qualityBtn = qLab[0];
        qualityBox.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                QualityDialog.show(MainActivity.this, new Runnable() {
                    @Override public void run() {
                        if (qualityBtn != null) qualityBtn.setText(shortQuality());
                        updateDataBar();
                    }
                });
            }
        });
        qualityBox.setBackground(rounded(CARD, 16));
        qualityBox.setLayoutParams(slot(4));
        chipsRow.addView(qualityBox);

        // Düzenleme düğmesi: kalem + "Düzen" yazısı.
        TextView[] eLab = new TextView[1];
        editBox = tabBox("✏️", Lang.t(this, edit.mode() ? "ui_done" : "ui_edit"), eLab);
        editBtn = eLab[0];
        editIcon = (TextView) editBox.getChildAt(0);
        editBox.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { edit.setMode(!edit.mode()); }
        });
        editBox.setBackground(rounded(edit.mode() ? ACCENT : CARD, 16));
        editBox.setLayoutParams(slot(5));
        chipsRow.addView(editBox);
        styleChips();
    }

    /** Sekme kutusu: üstte ikon, altında küçük yazı. Kutu 44dp (satır yüksekliği). */
    private LinearLayout tabBox(String icon, String label, TextView[] labelOut) {
        return ui.tabBox(icon, label, labelOut);
    }

    private void styleChips() {
        for (int i = 0; i < chipsRow.getChildCount(); i++) {
            View chip = chipsRow.getChildAt(i);
            if (chip.getTag() == null) continue;   // kalite / düzenleme düğmesi
            boolean sel = chip.getTag().toString().equals(group);
            chip.setBackground(rounded(sel ? ACCENT : CARD, 16));
            if (chip instanceof LinearLayout) {
                LinearLayout box = (LinearLayout) chip;
                for (int k = 0; k < box.getChildCount(); k++) {
                    ((TextView) box.getChildAt(k)).setTextColor(sel ? Color.WHITE : MUTED);
                }
            }
        }
    }

    private void gotoNumber(int no) {
        final Channel c = ChannelRepo.byNumber(this, no);
        if (c == null) {
            Toast.makeText(this, "№" + no + " " + I18n.t(this, "diye kanal yok") + " (1-" + all.size() + ")",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(I18n.t(this, "Kanal") + " " + no)
                .setMessage(c.name + "\n" + Lang.group(this, c.group) + "\n\n"
                        + I18n.t(this, "Açılsın mı?"))
                .setPositiveButton(I18n.t(this, "Aç"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        open(c);
                    }
                })
                .setNegativeButton(I18n.t(this, "Vazgeç"), null)
                .show();
    }

    private void savedSets() {
        final List<String> sets = new ArrayList<String>(Prefs.multiSets(this));
        sets.add(0, "Son kullandığım set");
        // Listede ham kod ("ad:Spor|0,1,2,3|1000|3|u:...") görünüyordu; yalnız set adı yazılır.
        final String[] labels = new String[sets.size()];
        labels[0] = sets.get(0);
        for (int i = 1; i < sets.size(); i++) {
            String v = sets.get(i);
            int bar = v.indexOf('|');
            labels[i] = v.startsWith("ad:") && bar > 3 ? v.substring(3, bar) : v;
        }
        new AlertDialog.Builder(this)
                .setTitle(I18n.t(this, "3'lü izleme setleri"))
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        Intent i = new Intent(MainActivity.this, MultiActivity.class);
                        if (which > 0) i.putExtra("set", sets.get(which));
                        startActivity(i);
                    }
                })
                .setNeutralButton(I18n.t(this, "Set sil"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { deleteSetDialog(); }
                })
                .show();
    }

    private void deleteSetDialog() {
        final List<String> sets = new ArrayList<String>(Prefs.multiSets(this));
        if (sets.isEmpty()) {
            Toast.makeText(this, I18n.t(this, "Kayıtlı set yok"), Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[sets.size()];
        for (int i = 0; i < sets.size(); i++) {
            String s = sets.get(i);
            labels[i] = s.startsWith("ad:") ? s.substring(3, Math.max(3, s.indexOf('|'))) : s;
        }
        new AlertDialog.Builder(this).setTitle(I18n.t(this, "Hangi set silinsin?")).setItems(labels,
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        Prefs.removeMultiSet(MainActivity.this, sets.get(which));
                        Toast.makeText(MainActivity.this, I18n.t(MainActivity.this, "Silindi"), Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    // ---------- v10.10: ChannelEdit.Host (duzenleme modu mantigi ChannelEdit'te) ----------
    @Override public android.content.Context context() { return this; }

    @Override public List<Channel> shownList() { return shown; }

    @Override public String group() { return group; }

    @Override public String query() { return query; }

    @Override public boolean hiddenTab() { return onlyHidden; }

    @Override public void editUi(boolean on, int count) {
        if (editBar != null) editBar.setVisibility(on ? View.VISIBLE : View.GONE);
        if (editBox != null) {
            if (editIcon != null) {
                editIcon.setText(on ? "\u2713" : "\u270f\ufe0f");
                editIcon.setTextColor(on ? Color.WHITE : MUTED);
            }
            if (editBtn != null) {
                editBtn.setText(Lang.t(this, on ? "ui_done" : "ui_edit"));
                editBtn.setTextColor(on ? Color.WHITE : MUTED);
            }
            editBox.setBackground(rounded(on ? ACCENT : CARD, 16));
        }
        if (selCount != null) {
            selCount.setText(count == 0
                    ? I18n.t(this, "kanala dokun \u2192 seç")
                    : count + " " + I18n.t(this, "kanal seçili"));
        }
    }

    @Override public void listChanged() {
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override public void listRebuilt() { refresh(); }

    @Override public void say(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void askNumber(final Channel c) {
        final boolean filtered = query.length() > 0;
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(c.no));
        new AlertDialog.Builder(this)
                .setTitle(I18n.t(this, "Kumanda numarası"))
                .setMessage(I18n.t(this, "Yeni numara hem kumanda numarasını hem listedeki sırasını değiştirir.\n\nDolu bir numara yazarsan o kanalla numaralar yer değiştirir."))
                .setView(et)
                .setPositiveButton(I18n.t(this, "Kaydet"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        int no;
                        try { no = Integer.parseInt(et.getText().toString().trim()); }
                        catch (Exception e) {
                            Toast.makeText(MainActivity.this, I18n.t(MainActivity.this, "Sadece sayı yaz"), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (no < 1 || no > all.size()) {
                            Toast.makeText(MainActivity.this, "1-" + all.size() + " " + I18n.t(MainActivity.this, "arasında bir numara seç"),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Channel other = ChannelRepo.byNumber(MainActivity.this, no);
                        int oldNo = c.no;
                        if (other != null && !other.url.equals(c.url)) {
                            // Numara dolu: iki kanal numaralarini degistirir (kumanda sirasi)
                            Prefs.setNoOverride(MainActivity.this, other.url, oldNo);
                        }
                        Prefs.setNoOverride(MainActivity.this, c.url, no);
                        ChannelRepo.applyNumbers(MainActivity.this);
                        if (!filtered) {
                            // Numara = kumanda sirasi: sekmeyi numaraya gore diz
                            java.util.Collections.sort(shown, new java.util.Comparator<Channel>() {
                                @Override public int compare(Channel a, Channel b) {
                                    return a.no - b.no;
                                }
                            });
                            edit.saveOrder();
                        }
                        refresh();
                        Toast.makeText(MainActivity.this,
                                c.name + " " + I18n.t(MainActivity.this, "artık №") + no, Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(I18n.t(this, "Vazgeç"), null).show();
    }

    private void sortDialog() {
        if (!edit.canReorder()) return;
        final String[] items = {
                I18n.t(this, "🕘 Çok izlediklerim en üstte"),
                I18n.t(this, "🆕 Hiç izlemediklerim en üstte"),
                I18n.t(this, "🔤 Ada göre A → Z"),
                I18n.t(this, "🔢 Kanal numarasına göre"),
                I18n.t(this, "↩️ Varsayılan sıraya dön")
        };
        new AlertDialog.Builder(this)
                .setTitle(I18n.t(this, "Bu sekme nasıl sıralansın?"))
                .setMessage(String.format(java.util.Locale.ROOT,
                        I18n.t(this, "%s · %d kanal"), group, shown.size()))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == 4) {
                            Prefs.setOrder(MainActivity.this, group, new ArrayList<String>());
                            edit.setMode(false);
                            refresh();
                            Toast.makeText(MainActivity.this, I18n.t(MainActivity.this, "Varsayılan sıra geri geldi"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<Channel> sorted = new ArrayList<Channel>(shown);
                        if (which == 0 || which == 1) {
                            final boolean mostFirst = which == 0;
                            java.util.Collections.sort(sorted, new java.util.Comparator<Channel>() {
                                @Override public int compare(Channel a, Channel b) {
                                    long sa = watchScore(a), sb = watchScore(b);
                                    if (sa == sb) return a.no - b.no;
                                    if (mostFirst) return sa > sb ? -1 : 1;
                                    return sa < sb ? -1 : 1;
                                }
                            });
                        } else if (which == 2) {
                            java.util.Collections.sort(sorted, new java.util.Comparator<Channel>() {
                                @Override public int compare(Channel a, Channel b) {
                                    return a.name.compareToIgnoreCase(b.name);
                                }
                            });
                        } else {
                            java.util.Collections.sort(sorted, new java.util.Comparator<Channel>() {
                                @Override public int compare(Channel a, Channel b) {
                                    return a.no - b.no;
                                }
                            });
                        }
                        shown.clear();
                        shown.addAll(sorted);
                        edit.saveOrder();
                        edit.setMode(false);
                        refresh();
                        Toast.makeText(MainActivity.this, I18n.t(MainActivity.this, "Sıra kaydedildi"), Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    /** Izlenme suresi agirlikli puan: cok izlenen kanali one almak icin. */
    private long watchScore(Channel c) {
        return Prefs.secs(this, c.url) * 1000L + Prefs.usage(this, c.url) / 1024L;
    }

    private void channelMenu(final Channel c) {
        final boolean hidden = Prefs.hiddenChannels(this).contains(c.url);
        final boolean fav = Prefs.favs(this).contains(c.url);
        final List<String> items = new ArrayList<String>();
        items.add("▶  Aç");
        items.add(fav ? "★  Favoriden çıkar" : "★  Favorilere ekle");
        items.add("🔢  Numarayı değiştir (№" + c.no + ")");
        items.add("☑  Seçim / sıralama modu");
        items.add(hidden ? "➕  Listeye geri ekle" : "🗑  Listeden sil");
        items.add(I18n.t(this, "📣  Çalışmıyor mu? Bildir"));
        new AlertDialog.Builder(this)
                .setTitle("№" + c.no + "  " + c.name)
                .setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == 0) { open(c); return; }
                        if (which == 1) { Prefs.toggleFav(MainActivity.this, c.url); refresh(); return; }
                        if (which == 2) { askNumber(c); return; }
                        if (which == 3) {
                            edit.setMode(true);
                            edit.toggle(c);
                            return;
                        }
                        // Menü sırası: 0 Aç · 1 Favori · 2 Numara · 3 Seçim · 4 Sil/ekle · 5 Bildir
                        if (which == 5) { reportChannel(c); return; }
                        if (hidden) {
                            Prefs.unhideChannel(MainActivity.this, c.url);
                            refresh();
                        } else {
                            Prefs.hideChannel(MainActivity.this, c.url);
                            refresh();
                            Toast.makeText(MainActivity.this,
                                    c.name + " " + I18n.t(MainActivity.this,
                                            "listeden silindi.\nGeri eklemek: 🗑 Gizlenenler"),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }).show();
    }

    /**
     * v10.09: "Çalışmıyor mu? Bildir" — listeden, kanalı açmadan bildirme.
     * Aynı kanal 10 dakika içinde ikinci kez bildirilemez (kazara çift dokunma).
     */
    private void reportChannel(final Channel c) {
        CrashLog.event(this, "report", "menüden bildir: " + c.name);
        if (ChannelReport.tooSoon(this, c.url)) {
            CrashLog.event(this, "report", "atlandı: 10 dk içinde bildirilmişti · " + c.name);
            Toast.makeText(this, I18n.t(this, "Bu kanalı az önce bildirdin"),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ChannelReport.send(this, c.name, c.url, StreamDiag.UNKNOWN,
                "listedeki menüden bildirildi", new ChannelReport.Done() {
                    @Override public void onResult(final boolean ok, final boolean queued) {
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                Toast.makeText(MainActivity.this,
                                        ok ? I18n.t(MainActivity.this, "Bildirimin alındı, teşekkürler")
                                                : I18n.t(MainActivity.this,
                                                        "Bildirim kaydedildi · internet gelince gönderilecek"),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
    }

    private void open(final Channel c) {
        // klavye acik kalirsa oynaticidan donuste liste sikisiyor (9.83)
        try {
            View f = getCurrentFocus();
            if (f != null) {
                InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (im != null) im.hideSoftInputFromWindow(f.getWindowToken(), 0);
                f.clearFocus();
            }
        } catch (Throwable ignored) { }
        // 4. kanal acilisinda bir, en fazla 10 dakikada 1 gecis reklami
        AdManager.maybeInterstitial(this, new Runnable() {
            @Override public void run() {
                Intent i = new Intent(MainActivity.this, PlayerActivity.class);
                i.putExtra("index", ChannelRepo.indexOfUrl(c.url));
                startActivity(i);
            }
        });
    }

    private void handleExtras(Intent it) {
        if (it == null) return;
        String tab = it.getStringExtra("tab");
        if (tab != null && tab.length() > 0) {
            group = tab;
            onlyHidden = "🗑 Gizlenenler".equals(tab);
            styleChips();
            it.removeExtra("tab");
        }
        if (it.getBooleanExtra("edit", false)) {
            it.removeExtra("edit");
            edit.setMode(true);
        }
        if (it.getBooleanExtra("sort", false)) {
            it.removeExtra("sort");
            refresh();
            sortDialog();
        }
    }

    private void refresh() {
        ChannelRepo.applyNumbers(this);
        // v10.10: filtre + siralama ChannelFilter'da; burada yalniz gorunum kurulur.
        List<Channel> base = ChannelFilter.build(this, all, group, query, onlyHidden);
        shown.clear();
        shown.addAll(base);

        // secimleri yalnizca gorunen kanallarla sinirla (v10.10: ChannelEdit)
        edit.retainVisible();
        edit.publish();
        adapter.notifyDataSetChanged();
        if (shown.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            final String cc = Prefs.country(this);
            if (base.isEmpty() && !"tr".equals(cc)) {
                // #148: ülke listesi inmediyse/hazır değilse boş ekran yerine açıklama + tekrar dene
                emptyView.setText("" + Countries.flag(cc) + " " + Countries.name(cc, false)
                        + I18n.t(this, " listesi henüz hazır değil.\n\nBu ülke için kanallar sunucudan indirilemedi ya da henüz doğrulanmadı.\n\nDokun → tekrar indirmeyi dene\nveya Ayarlar → Ülke bölümünden Türkiye'ye dön."));
                emptyView.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        emptyView.setText(I18n.t(MainActivity.this, "İndiriliyor…"));
                        new Thread(new Runnable() {
                            @Override public void run() {
                                CountryRepo.download(MainActivity.this, cc);
                                runOnUiThread(new Runnable() {
                                    @Override public void run() {
                                        ChannelRepo.reload(MainActivity.this);
                                        refresh();
                                    }
                                });
                            }
                        }, "retry-dl").start();
                    }
                });
            } else {
                emptyView.setOnClickListener(null);
                emptyView.setText(onlyHidden
                        ? I18n.t(this, "Gizlenen kanal yok.\n\nBir kanala uzun basıp \"Listeden sil\" dersen burada birikir.")
                        : I18n.t(this, "Bu filtrede kanal yok.\n\nAramayı temizle veya başka bir sekme dene."));
            }
            if (listView != null) listView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            if (listView != null) listView.setVisibility(View.VISIBLE);
        }
        updateDataBar();
        focusFirstRowIfTv();   // v10.08: kumanda ile açılışta ilk kanal odaklanır
    }

    private void updateDataBar() {
        long used = Prefs.todayBytes(this);
        int lim = Prefs.limitMb(this);
        // v10.04 (Paket C): veri şeridi arayüz diline bağlı
        String s = Lang.t(this, "usage_today") + Net.mb(used);
        if (lim > 0) {
            s += " / " + lim + " MB";
            if (used > lim * 1048576L) s += "  " + Lang.t(this, "limit_over");
        } else {
            s += " · " + Lang.t(this, "limit_none");
        }
        s += "  · 🎚 " + QualityDialog.label(this);
        s += Prefs.audioOnlyMobile(this) ? "  · 🎧 " + Lang.t(this, "audio_only_mobile") : "";
        // 9.77: kanal sayısı artık bu sabit şeritte (ayrı satır kaldırıldı).
        s += "  · 📺 " + Lang.channels(this, shown.size());
        dataBar.setText(s);
        dataBar.setVisibility(Prefs.showUsage(this) ? View.VISIBLE : View.GONE);
        dataBar.setTextColor(lim > 0 && used > lim * 1048576L ? Color.parseColor("#FF5252") : MUTED);
        updateRadioBar();
    }

    /** Radyo arkada çalıyorsa "🎧 X çalıyor · Kapat" şeridi. */
    private void updateRadioBar() {
        try {
            if (radioNow == null) return;
            String nm = AudioService.nowPlayingName();
            if (nm == null) {
                radioNow.setVisibility(View.GONE);
                return;
            }
            radioNow.setVisibility(View.VISIBLE);
            radioNowText.setText("🎧 " + nm + " " + I18n.t(this, "çalıyor"));
        } catch (Throwable ignored) { }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Abonelik Play'de değiştiyse (satın alma/iptal) reklam alanı yeniden kurulsun.
        Billing.refresh(this, new Runnable() {
            @Override public void run() {
                if (Prefs.subActive(MainActivity.this) != subState) {
                    subState = Prefs.subActive(MainActivity.this);
                    recreate();
                }
            }
        });
        // ulke veya arayuz dili degistiyse ekrani bastan kur
        if (!shownCountry.equals(Prefs.country(this)) || !shownLang.equals(Prefs.uiLang(this))) {
            recreate();
            return;
        }
        ChannelRepo.applyNumbers(this);
        refresh();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleExtras(intent);
        refresh();
    }

    private GradientDrawable rounded(int color, int radius) { return ui.rounded(color, radius); }

    /** Satır içinde eşit genişlikte kutu: ilk kutu kenara yapışık, aralarında 4dp boşluk. */
    private LinearLayout.LayoutParams slot(int index) { return ui.slot(index); }

    private TextView pill(String s, int color) { return ui.pill(s, color); }

    private TextView mini(String s) { return ui.mini(s); }

    private TextView arrow(String s) { return ui.arrow(s); }

    private int dp(int v) { return ui.dp(v); }

    /** K-16: RecyclerView ViewHolder'ı — her satır bir FrameLayout kabı. */
    private static class RowVH extends RecyclerView.ViewHolder {
        RowVH(View v) { super(v); }
    }

    private class ChannelAdapter extends RecyclerView.Adapter<RowVH> {
        @Override public int getItemCount() { return shown.size(); }

        @Override public RowVH onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout box = new FrameLayout(MainActivity.this);
            box.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RowVH(box);
        }

        @Override public void onBindViewHolder(final RowVH holder, int position) {
            final FrameLayout box = (FrameLayout) holder.itemView;
            box.removeAllViews();
            final LinearLayout row = buildRow(position);
            box.addView(row);
            final int pos = position;
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (pos < 0 || pos >= shown.size()) return;
                    Channel c = shown.get(pos);
                    if (edit.mode()) { edit.toggle(c); return; }
                    if (onlyHidden) { channelMenu(c); return; }
                    open(c);
                }
            });
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    if (pos < 0 || pos >= shown.size()) return false;
                    channelMenu(shown.get(pos));
                    return true;
                }
            });
        }
    }

    /** L-16: satırın görselini kurar (eski getView gövdesi). */
    /** v10.08: satır görünümü ChannelRow'da (MainActivity bölündü). */
    private LinearLayout buildRow(int i) {
        Channel c = shown.get(i);
        return ChannelRow.build(this, ui, c, i, edit.mode(), edit.isSelected(c.url), this);
    }

    // ---------- v10.08: MainHeader.Host (üst çubuk düğmeleri) ----------
    @Override public void headerMulti() {
        startActivity(new Intent(MainActivity.this, MultiActivity.class));
    }

    @Override public void headerMultiLong() { savedSets(); }

    @Override public void headerRadio() {
        startActivity(new Intent(MainActivity.this, RadioActivity.class));
    }

    @Override public void headerCountry() {
        startActivity(new Intent(MainActivity.this, CountryActivity.class));
    }

    @Override public void headerRecordings() {
        startActivity(new Intent(MainActivity.this, RecordingsActivity.class));
    }

    @Override public void headerSettings() {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    /** 🎁 15 dakika reklamsız (ödüllü reklam) — kutunun eski gövdesi. */
    @Override public void headerGift() {
        if (Prefs.subActive(this)) {
            Toast.makeText(this, I18n.t(this, "Aboneliğin zaten reklamsız"), Toast.LENGTH_SHORT).show();
            return;
        }
        int left = AdManager.adFreeMinutesLeft(this);
        if (left > 0) {
            Toast.makeText(this, I18n.t(this, "Reklamsız süren") + ": " + left + " "
                    + I18n.t(this, "dk"), Toast.LENGTH_SHORT).show();
            return;
        }
        AdManager.showRewarded(this, new Runnable() {
            @Override public void run() {
                Toast.makeText(MainActivity.this, I18n.t(MainActivity.this, "Teşekkürler!") + " "
                        + Ads.REWARD_MINUTES + " " + I18n.t(MainActivity.this, "dakika reklamsız."),
                        Toast.LENGTH_LONG).show();
            }
        }, new Runnable() {
            @Override public void run() {
                Toast.makeText(MainActivity.this,
                        I18n.t(MainActivity.this, "Reklam şu an hazır değil, biraz sonra tekrar dene"),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------- v10.08: ChannelRow.Host (satırdaki ▲▼ okları) ----------
    @Override public void moveChannel(int pos, int delta) { edit.moveRow(pos, delta); }

    // ---------- v10.08: kumanda (D-pad) desteği ----------
    private final StringBuilder digits = new StringBuilder();
    private final android.os.Handler digitHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable digitOpen = new Runnable() {
        @Override public void run() {
            int no = 0;
            try { no = Integer.parseInt(digits.toString()); } catch (Exception ignored) { }
            digits.setLength(0);
            if (no > 0) openByNumber(no);
        }
    };

    /**
     * Harici kumanda / klavye ile rakam yazınca kanal doğrudan açılır
     * (1,2 sn içinde ikinci rakam gelmezse). Arama kutusu odaklıyken karışmaz.
     */
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent e) {
        if (keyCode >= android.view.KeyEvent.KEYCODE_0 && keyCode <= android.view.KeyEvent.KEYCODE_9
                && !(getCurrentFocus() instanceof EditText)) {
            digits.append((char) ('0' + (keyCode - android.view.KeyEvent.KEYCODE_0)));
            if (digits.length() > 3) digits.deleteCharAt(0);
            Toast.makeText(this, "№" + digits, Toast.LENGTH_SHORT).show();
            digitHandler.removeCallbacks(digitOpen);
            digitHandler.postDelayed(digitOpen, 1200);
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    /** Kanal numarasından doğrudan aç (kumanda rakamları için, onay sorulmaz). */
    private void openByNumber(int no) {
        Channel c = ChannelRepo.byNumber(this, no);
        if (c == null) {
            Toast.makeText(this, "№" + no + " " + I18n.t(this, "diye kanal yok")
                    + " (1-" + all.size() + ")", Toast.LENGTH_SHORT).show();
            return;
        }
        open(c);
    }

    @Override
    protected void onDestroy() {
        digitHandler.removeCallbacks(digitOpen);
        super.onDestroy();
    }

    /** İlk kanal odaklansın (kumanda ile açılışta OK hemen çalışsın). */
    private void focusFirstRowIfTv() {
        if (!TvFocus.isTv(this) || listView == null || shown.isEmpty()) return;
        if (getCurrentFocus() != null) return;
        listView.post(new Runnable() {
            @Override public void run() {
                View v = listView.getChildAt(0);
                if (v != null) v.requestFocus();
            }
        });
    }
}
