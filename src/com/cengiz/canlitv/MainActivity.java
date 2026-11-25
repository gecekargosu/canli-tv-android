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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
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

public class MainActivity extends android.app.Activity {

    private static final int BG = Color.parseColor("#11151C");
    private static final int CARD = Color.parseColor("#1B2230");
    private static final int ACCENT = Color.parseColor("#FF7A18");
    private static final int TEXT = Color.parseColor("#F5F7FA");
    private static final int MUTED = Color.parseColor("#9AA4B2");
    private static final int SOFT = Color.parseColor("#2A3444");

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
    private ListView listView;
    private boolean onlyHidden = false;
    private String shownCountry = "";
    private String shownLang = "";

    // --- liste duzenleme ---
    private boolean editMode = false;
    private final java.util.LinkedHashSet<String> selected = new java.util.LinkedHashSet<String>();
    private LinearLayout editBar;
    private TextView editBtn, selCount, countLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Kanal aramasında da klavye listeyi yutmasın.
        getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        CrashLog.install(this);   // cokme olursa cihaza log yaz (Ayarlar -> Hata günlüğü)
        CrashLog.uploadIfDue(this);   // kullanıcı izin verdiyse günde bir kez sunucuya gönder
        // Hata günlüğü: hangi sürüm, hangi ülke, hangi mod ile açıldı.
        CrashLog.event(this, "app", "açılış " + BuildConfig.VERSION_NAME
                + " · ülke=" + Prefs.country(this)
                + " · ekonomi=" + (Prefs.economy(this) ? "1" : "0")
                + " · dil=" + Prefs.uiLang(this));
        Prefs.migrateNumbers(this);   // 9.75: eski numara/sıra kayıtları bir kez temizlenir
        Good.refreshIfNeeded(this);   // #101: doğrulanmış kanal listesi günde 1 kez tazelenir
        all = ChannelRepo.all(this);
        shownCountry = Prefs.country(this);
        maybeSuggestCountry();
        shownLang = Prefs.uiLang(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        // 9.75: durum çubuğu payı iki kez uygulanıyordu (başlığın üstünde ~55 dp
        // boşluk kalıyordu). Pencere zaten güvenli alanın altında başladığı için
        // fitSystemWindows ek payı ikinci kez ekliyordu; tek pay yeter.
        root.setFitsSystemWindows(false);

        // --- baslik ---
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        // 9.76: üst kısım ekranın ~1/3'ünü kaplıyordu. Başlık + düğmeler tek satıra
        // alındı, arama/sekme/satır araçları sıkıştırıldı; liste daha erken başlıyor.
        head.setPadding(dp(14), dp(10), dp(12), dp(2));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView brand = new ImageView(this);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(34), dp(34));
        blp.setMargins(0, 0, dp(8), 0);
        brand.setLayoutParams(blp);
        brand.setScaleType(ImageView.ScaleType.FIT_CENTER);
        try { brand.setImageDrawable(getPackageManager().getApplicationIcon(getPackageName())); }
        catch (Exception ignored) { }
        topRow.addView(brand);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText("UMAY Multi TV");
        title.setTextColor(TEXT);
        title.setTextSize(15);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        TextView sub = new TextView(this);
        sub.setText(Lang.subtitle(this, all.size()));
        sub.setTextColor(MUTED);
        sub.setTextSize(9);
        sub.setSingleLine(true);
        sub.setEllipsize(android.text.TextUtils.TruncateAt.END);
        titleCol.addView(title);
        titleCol.addView(sub);
        topRow.addView(titleCol, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // 9.76: düğmeler başlığın sağına, tek satıra alındı (ayrı satır ~55 dp yiyordu
        // ve altında boş alan bırakıyordu). Başlık kalan genişliği alır.
        final LinearLayout actionRow = topRow;

        TextView multi = pill("▦ 3'lü", ACCENT);
        multi.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MultiActivity.class));
            }
        });
        multi.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                savedSets();
                return true;
            }
        });
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlp.setMargins(dp(6), 0, 0, 0);
        multi.setLayoutParams(mlp);
        actionRow.addView(multi);

        TextView radio = pill("📻", SOFT);
        radio.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RadioActivity.class));
            }
        });
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.setMargins(dp(6), 0, 0, 0);
        radio.setLayoutParams(rlp);
        actionRow.addView(radio);

        // --- ulke secimi: secilen ulkenin kanallari listeye gelir ---
        TextView countryBtn = pill(Countries.flag(shownCountry), SOFT);
        countryBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CountryActivity.class));
            }
        });
        LinearLayout.LayoutParams cblp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cblp.setMargins(dp(6), 0, 0, 0);
        countryBtn.setLayoutParams(cblp);
        actionRow.addView(countryBtn);

        TextView settings = pill("⚙", SOFT);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.setMargins(dp(6), 0, 0, 0);
        settings.setLayoutParams(slp);
        actionRow.addView(settings);

        head.addView(topRow);

        root.addView(head);

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
        radioStop.setText("⏹ Kapat");
        radioStop.setTextSize(12);
        radioStop.setTextColor(Color.parseColor("#111111"));
        radioStop.setTypeface(radioStop.getTypeface(), android.graphics.Typeface.BOLD);
        radioStop.setPadding(dp(12), dp(5), dp(12), dp(5));
        radioStop.setBackground(rounded(ACCENT, 10));
        radioStop.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioService.stop(MainActivity.this);
                radioNow.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Radyo kapatıldı", Toast.LENGTH_SHORT).show();
            }
        });
        radioNow.addView(radioStop);
        radioNow.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioService.stop(MainActivity.this);
                radioNow.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Radyo kapatıldı", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(radioNow);

        // --- arama + kanal no ---
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams srlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        srlp.setMargins(dp(12), dp(6), dp(12), dp(2));
        searchRow.setLayoutParams(srlp);

        EditText search = new EditText(this);
        search.setHint(Lang.t(this, "search"));
        search.setHintTextColor(MUTED);
        search.setTextColor(TEXT);
        search.setTextSize(13);
        search.setSingleLine(true);
        GradientDrawable sbg = rounded(CARD, 14);
        search.setBackground(sbg);
        search.setPadding(dp(11), dp(7), dp(11), dp(7));
        searchRow.addView(search, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                query = Lang.fold(s.toString().trim());
                refresh();
            }
            public void afterTextChanged(Editable s) { }
        });

        final EditText noInput = new EditText(this);
        noInput.setHint("No");
        noInput.setHintTextColor(MUTED);
        noInput.setTextColor(TEXT);
        noInput.setTextSize(13);
        noInput.setSingleLine(true);
        noInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        noInput.setGravity(Gravity.CENTER);
        noInput.setBackground(rounded(CARD, 14));
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(dp(52),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.setMargins(dp(6), 0, 6, 0);
        noInput.setLayoutParams(nlp);
        searchRow.addView(noInput);

        TextView go = pill("Git", ACCENT);
        go.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    int no = Integer.parseInt(noInput.getText().toString().trim());
                    gotoNumber(no);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Kanal numarası yaz", Toast.LENGTH_SHORT).show();
                }
            }
        });
        searchRow.addView(go);
        root.addView(searchRow);

        // --- cipler: 4 sekme, ekran genişliğine eşit dağılır (sağa kaymaz) ---
        chipsRow = new LinearLayout(this);
        chipsRow.setOrientation(LinearLayout.HORIZONTAL);
        chipsRow.setPadding(dp(12), dp(2), dp(12), dp(2));
        root.addView(chipsRow);
        buildChips();

        // --- liste araclari: duzenle / sirala ---
        LinearLayout tools = new LinearLayout(this);
        tools.setOrientation(LinearLayout.HORIZONTAL);
        tools.setGravity(Gravity.CENTER_VERTICAL);
        tools.setPadding(dp(12), dp(0), dp(12), dp(2));

        editBtn = pill(Lang.t(this, "edit"), SOFT);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setEditMode(!editMode); }
        });
        tools.addView(editBtn);

        TextView sortBtn = pill(Lang.t(this, "sort"), SOFT);
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { sortDialog(); }
        });
        LinearLayout.LayoutParams sortLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sortLp.setMargins(dp(6), 0, 0, 0);
        sortBtn.setLayoutParams(sortLp);
        tools.addView(sortBtn);

        // Sağda boş kalan alan: listede kaç kanal olduğunu gösterir (arama/filtrede değişir).
        tools.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        countLabel = new TextView(this);
        countLabel.setTextSize(11);
        countLabel.setTextColor(MUTED);
        countLabel.setSingleLine(true);
        tools.addView(countLabel);

        root.addView(tools);

        // --- duzenleme cubugu ---
        editBar = new LinearLayout(this);
        editBar.setOrientation(LinearLayout.HORIZONTAL);
        editBar.setGravity(Gravity.CENTER_VERTICAL);
        editBar.setPadding(dp(14), dp(2), dp(14), dp(6));
        editBar.setVisibility(View.GONE);

        TextView doneBtn = mini("✓ Bitti");
        doneBtn.setBackground(rounded(ACCENT, 14));
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setEditMode(false); }
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
            @Override public void onClick(View v) { moveSelected(-1); }
        });
        editBar.addView(upBtn);

        TextView downBtn = mini("▼");
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { moveSelected(1); }
        });
        editBar.addView(downBtn);

        TextView topBtn = mini("⤒");
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { moveSelectedToEdge(true); }
        });
        editBar.addView(topBtn);

        TextView bottomBtn = mini("⤓");
        bottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { moveSelectedToEdge(false); }
        });
        editBar.addView(bottomBtn);

        TextView delBtn = mini("🗑");
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { deleteSelected(); }
        });
        editBar.addView(delBtn);

        root.addView(editBar);

        // --- liste ---
        ListView list = new ListView(this);
        listView = list;
        list.setDivider(null);
        list.setDividerHeight(dp(6));
        list.setPadding(dp(12), dp(4), dp(12), dp(12));
        list.setClipToPadding(false);
        list.setSelector(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        adapter = new ChannelAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                Channel c = shown.get(pos);
                if (editMode) { toggleSelect(c); return; }
                if (onlyHidden) { channelMenu(c); return; }
                open(c);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> p, View v, int pos, long id) {
                channelMenu(shown.get(pos));
                return true;
            }
        });

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
    private void maybeSuggestCountry() {
        if (Prefs.countryAsked(this)) return;
        Prefs.setCountryAsked(this, true);
        final String current = Prefs.country(this);
        // M-05: IP'yi üçüncü tarafa göndermek yerine SIM/ağ ve cihaz dilinden ülke bulunur.
        // Hiçbir ağ isteği yapılmaz, izin gerekmez.
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
        final String found = cc == null ? null : cc.toLowerCase(java.util.Locale.US);
        if (found == null || found.length() != 2) return;
        if (found.equals(current) || !Countries.supported(found)) return;
        if (isFinishing()) return;
        new android.app.AlertDialog.Builder(this)
                .setTitle("Ülkeni bulduk: " + Countries.name(found, false))
                .setMessage("Kanalları ve radyoları bu ülkeye göre yükleyeyim mi?")
                .setPositiveButton("Evet, değiştir", new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int w) {
                        Prefs.setCountry(MainActivity.this, found);
                        ChannelRepo.reload(MainActivity.this);
                        recreate();
                    }
                })
                .setNegativeButton("Şimdi değil", null)
                .show();
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
        for (int i = 0; i < cats.size(); i++) {
            final String c = cats.get(i);
            TextView chip = new TextView(this);
            chip.setText(Lang.tab(this, c));
            chip.setTextSize(11);
            chip.setGravity(Gravity.CENTER);
            chip.setSingleLine(true);
            chip.setEllipsize(android.text.TextUtils.TruncateAt.END);
            chip.setPadding(dp(3), dp(6), dp(3), dp(6));
            chip.setTag(c);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    group = c;
                    onlyHidden = "🗑 Gizlenenler".equals(c);
                    selected.clear();
                    styleChips();
                    refresh();
                }
            });
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i > 0) clp.setMargins(dp(6), 0, 0, 0);
            chip.setLayoutParams(clp);
            chipsRow.addView(chip);
        }
        styleChips();
    }

    private void styleChips() {
        for (int i = 0; i < chipsRow.getChildCount(); i++) {
            TextView chip = (TextView) chipsRow.getChildAt(i);
            boolean sel = chip.getTag().toString().equals(group);
            chip.setBackground(rounded(sel ? ACCENT : CARD, 18));
            chip.setTextColor(sel ? Color.WHITE : MUTED);
        }
    }

    private void gotoNumber(int no) {
        final Channel c = ChannelRepo.byNumber(this, no);
        if (c == null) {
            Toast.makeText(this, "№" + no + " diye kanal yok (1-" + all.size() + ")",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Kanal " + no)
                .setMessage(c.flag() + "  " + c.name + "\n" + c.group + "\n\nAçılsın mı?")
                .setPositiveButton("Aç", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        open(c);
                    }
                })
                .setNegativeButton("Vazgeç", null)
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
                .setTitle("3'lü izleme setleri")
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        Intent i = new Intent(MainActivity.this, MultiActivity.class);
                        if (which > 0) i.putExtra("set", sets.get(which));
                        startActivity(i);
                    }
                })
                .setNeutralButton("Set sil", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { deleteSetDialog(); }
                })
                .show();
    }

    private void deleteSetDialog() {
        final List<String> sets = new ArrayList<String>(Prefs.multiSets(this));
        if (sets.isEmpty()) {
            Toast.makeText(this, "Kayıtlı set yok", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[sets.size()];
        for (int i = 0; i < sets.size(); i++) {
            String s = sets.get(i);
            labels[i] = s.startsWith("ad:") ? s.substring(3, Math.max(3, s.indexOf('|'))) : s;
        }
        new AlertDialog.Builder(this).setTitle("Hangi set silinsin?").setItems(labels,
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        Prefs.removeMultiSet(MainActivity.this, sets.get(which));
                        Toast.makeText(MainActivity.this, "Silindi", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    // ---------- duzenleme modu ----------
    private void setEditMode(boolean on) {
        editMode = on;
        if (!on) selected.clear();
        if (editBar != null) editBar.setVisibility(on ? View.VISIBLE : View.GONE);
        if (editBtn != null) {
            editBtn.setText(on ? "✏️ Düzenleniyor" : "✏️ Düzenle");
            editBtn.setBackground(rounded(on ? ACCENT : SOFT, 18));
        }
        updateSelCount();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void toggleSelect(Channel c) {
        if (selected.contains(c.url)) selected.remove(c.url);
        else selected.add(c.url);
        updateSelCount();
        adapter.notifyDataSetChanged();
    }

    private void updateSelCount() {
        if (selCount != null) {
            selCount.setText(selected.size() == 0
                    ? "kanala dokun → seç"
                    : selected.size() + " kanal seçili");
        }
        if (editBar != null) editBar.setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    private List<Channel> selectedChannels() {
        List<Channel> out = new ArrayList<Channel>();
        for (Channel c : shown) if (selected.contains(c.url)) out.add(c);
        return out;
    }

    private void moveSelected(int delta) {
        if (!canReorder()) return;
        List<Channel> sel = selectedChannels();
        if (sel.isEmpty()) {
            Toast.makeText(this, "Önce kanallara dokunup seç", Toast.LENGTH_SHORT).show();
            return;
        }
        if (delta < 0) {
            for (Channel c : sel) {
                int i = shown.indexOf(c);
                if (i > 0) { shown.remove(i); shown.add(i - 1, c); }
            }
        } else {
            for (int k = sel.size() - 1; k >= 0; k--) {
                Channel c = sel.get(k);
                int i = shown.indexOf(c);
                if (i >= 0 && i < shown.size() - 1) { shown.remove(i); shown.add(i + 1, c); }
            }
        }
        saveOrder();
        adapter.notifyDataSetChanged();
    }

    private void moveSelectedToEdge(boolean top) {
        if (!canReorder()) return;
        List<Channel> sel = selectedChannels();
        if (sel.isEmpty()) {
            Toast.makeText(this, "Önce kanallara dokunup seç", Toast.LENGTH_SHORT).show();
            return;
        }
        shown.removeAll(sel);
        if (top) shown.addAll(0, sel);
        else shown.addAll(sel);
        saveOrder();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, top ? "En üste alındı" : "En alta alındı", Toast.LENGTH_SHORT).show();
    }

    private void deleteSelected() {
        if (!canReorder()) return;
        List<Channel> sel = selectedChannels();
        if (sel.isEmpty()) {
            Toast.makeText(this, "Silmek için kanal seç", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Channel c : sel) {
            if (onlyHidden) Prefs.unhideChannel(this, c.url);
            else Prefs.hideChannel(this, c.url);
        }
        shown.removeAll(sel);
        for (Channel c : sel) selected.remove(c.url);
        saveOrder();
        refresh();
        Toast.makeText(this, onlyHidden
                        ? sel.size() + " kanal listeye geri eklendi"
                        : sel.size() + " kanal listeden silindi · 🗑 Gizlenenler'den geri alabilirsin",
                Toast.LENGTH_LONG).show();
    }

    private boolean canReorder() {
        if (query.length() > 0) {
            Toast.makeText(this, "Sıralamak için arama kutusunu temizle", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveOrder() {
        if ("🕘 Son".equals(group)) return;
        List<String> urls = new ArrayList<String>();
        for (Channel c : shown) urls.add(c.url);
        Prefs.setOrder(this, group, urls);
    }

    /** Tek bir kanali bir adim yukari/asagi tasi (satirdaki oklar). */
    private void moveItem(int pos, int delta) {
        if (!canReorder()) return;
        int j = pos + delta;
        if (pos < 0 || pos >= shown.size() || j < 0 || j >= shown.size()) return;
        Channel c = shown.remove(pos);
        shown.add(j, c);
        saveOrder();
        adapter.notifyDataSetChanged();
    }

    private void askNumber(final Channel c) {
        final boolean filtered = query.length() > 0;
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(c.no));
        new AlertDialog.Builder(this)
                .setTitle("Kumanda numarası")
                .setMessage("Yeni numara hem kumanda numarasını hem listedeki sırasını değiştirir.\n\nDolu bir numara yazarsan o kanalla numaralar yer değiştirir.")
                .setView(et)
                .setPositiveButton("Kaydet", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        int no;
                        try { no = Integer.parseInt(et.getText().toString().trim()); }
                        catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Sadece sayı yaz", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (no < 1 || no > all.size()) {
                            Toast.makeText(MainActivity.this, "1 ile " + all.size() + " arasında bir numara seç",
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
                            saveOrder();
                        }
                        refresh();
                        Toast.makeText(MainActivity.this,
                                c.name + " artık №" + no, Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Vazgeç", null).show();
    }

    private void sortDialog() {
        if (!canReorder()) return;
        final String[] items = {
                "🕘 Çok izlediklerim en üstte",
                "🆕 Hiç izlemediklerim en üstte",
                "🔤 Ada göre A → Z",
                "🔢 Kanal numarasına göre",
                "↩️ Varsayılan sıraya dön"
        };
        new AlertDialog.Builder(this)
                .setTitle("Bu sekme nasıl sıralansın?")
                .setMessage(group + " · " + shown.size() + " kanal")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == 4) {
                            Prefs.setOrder(MainActivity.this, group, new ArrayList<String>());
                            setEditMode(false);
                            refresh();
                            Toast.makeText(MainActivity.this, "Varsayılan sıra geri geldi",
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
                        saveOrder();
                        setEditMode(false);
                        refresh();
                        Toast.makeText(MainActivity.this, "Sıra kaydedildi", Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(this)
                .setTitle("№" + c.no + "  " + c.flag() + "  " + c.name)
                .setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == 0) { open(c); return; }
                        if (which == 1) { Prefs.toggleFav(MainActivity.this, c.url); refresh(); return; }
                        if (which == 2) { askNumber(c); return; }
                        if (which == 3) {
                            setEditMode(true);
                            toggleSelect(c);
                            return;
                        }
                        if (hidden) {
                            Prefs.unhideChannel(MainActivity.this, c.url);
                            refresh();
                        } else {
                            Prefs.hideChannel(MainActivity.this, c.url);
                            refresh();
                            Toast.makeText(MainActivity.this,
                                    c.name + " listeden silindi.\nGeri eklemek: 🗑 Gizlenenler",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }).show();
    }

    private void open(final Channel c) {
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
            setEditMode(true);
        }
        if (it.getBooleanExtra("sort", false)) {
            it.removeExtra("sort");
            refresh();
            sortDialog();
        }
    }

    private void refresh() {
        ChannelRepo.applyNumbers(this);
        shown.clear();
        Set<String> favs = Prefs.favs(this);
        List<String> recent = Prefs.recent(this);
        java.util.Set<String> hidden = Prefs.hiddenChannels(this);
        List<Channel> base = new ArrayList<Channel>();
        for (Channel c : all) {
            if (onlyHidden != hidden.contains(c.url)) continue;
            // 9.75: TV8 gibi büyük kanallar iki geçici hatadan sonra listeden düşüyordu.
            // Sabit (öne alınan) kanallar otomatik gizlemeden muaf.
            if (!onlyHidden && Prefs.hideDead(this) && Prefs.fails(this, c.url) >= 2
                    && !ChannelRepo.pinned(c.name)) continue;
            if (!"Tümü".equals(group)) {
                if ("★ Favoriler".equals(group)) {
                    if (!favs.contains(c.url)) continue;
                } else if ("🇹🇷 Türkçe".equals(group)) {
                    if (!c.turkish()) continue;
                } else if ("🌐 Yabancı".equals(group)) {
                    if (c.turkish()) continue;
                } else if ("🕘 Son".equals(group)) {
                    if (!recent.contains(c.url)) continue;
                } else if (!c.group.equals(group)) {
                    continue;
                }
            }
            if (query.length() > 0
                    && !Lang.fold(c.name).contains(query)
                    && !Lang.fold(c.group).contains(query)
                    && !String.valueOf(c.no).equals(query)) continue;
            base.add(c);
        }

        // Bu sekme icin kaydedilmis kullanici sirasi (surukle/birak yerine kalici sira)
        if (!"🕘 Son".equals(group)) {
            final List<String> ord = Prefs.order(this, group);
            if (!ord.isEmpty()) {
                java.util.Collections.sort(base, new java.util.Comparator<Channel>() {
                    @Override public int compare(Channel a, Channel b) {
                        int ia = ord.indexOf(a.url);
                        int ib = ord.indexOf(b.url);
                        if (ia < 0) ia = Integer.MAX_VALUE;
                        if (ib < 0) ib = Integer.MAX_VALUE;
                        return ia < ib ? -1 : (ia == ib ? 0 : 1);
                    }
                });
            }
        }
        shown.addAll(base);

        if ("🕘 Son".equals(group)) {
            final List<Channel> ordered = new ArrayList<Channel>();
            for (String u : recent) for (Channel c : shown) if (c.url.equals(u)) ordered.add(c);
            for (Channel c : new ArrayList<Channel>(shown)) if (!ordered.contains(c)) ordered.add(c);
            shown.clear();
            shown.addAll(ordered);
        }

        // secimleri yalnizca gorunen kanallarla sinirla
        java.util.Set<String> visible = new java.util.HashSet<String>();
        for (Channel c : shown) visible.add(c.url);
        selected.retainAll(visible);

        updateSelCount();
        adapter.notifyDataSetChanged();
        if (countLabel != null) countLabel.setText(shown.size() + " kanal");
        if (shown.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            final String cc = Prefs.country(this);
            if (base.isEmpty() && !"tr".equals(cc)) {
                // #148: ülke listesi inmediyse/hazır değilse boş ekran yerine açıklama + tekrar dene
                emptyView.setText("" + Countries.flag(cc) + " " + Countries.name(cc, false)
                        + " listesi henüz hazır değil.\n\nBu ülke için kanallar sunucudan indirilemedi "
                        + "ya da henüz doğrulanmadı.\n\nDokun → tekrar indirmeyi dene\n"
                        + "veya Ayarlar → Ülke bölümünden Türkiye'ye dön.");
                emptyView.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        emptyView.setText("İndiriliyor…");
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
                        ? "Gizlenen kanal yok.\n\nBir kanala uzun basıp \"Listeden sil\" dersen burada birikir."
                        : "Bu filtrede kanal yok.\n\nAramayı temizle veya başka bir sekme dene.");
            }
            if (listView != null) listView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            if (listView != null) listView.setVisibility(View.VISIBLE);
        }
        updateDataBar();
    }

    private void updateDataBar() {
        long used = Prefs.todayBytes(this);
        int lim = Prefs.limitMb(this);
        String s = "📶 Bugün kullanılan veri: " + Net.mb(used);
        if (lim > 0) {
            s += " / " + lim + " MB";
            if (used > lim * 1048576L) s += "  ⚠ LİMİT AŞILDI";
        } else {
            s += " · limit yok";
        }
        s += "  · 🎚 " + QualityDialog.label(this);
        s += Prefs.audioOnlyMobile(this) ? "  · 🎧 mobilde sadece ses" : "";
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
            radioNowText.setText("🎧 " + nm + " çalıyor");
        } catch (Throwable ignored) { }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView pill(String s, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(11);
        t.setGravity(Gravity.CENTER);
        // 9.76: 48dp düğmeler + ayrı satır üst kısmı şişiriyordu; 44dp (hâlâ rahat
        // dokunulur) ve tek satır. Başlık kısalmadan sığıyor.
        t.setPadding(dp(7), dp(6), dp(7), dp(6));
        t.setBackground(rounded(color, 16));
        t.setMinHeight(dp(44));
        t.setMinWidth(dp(44));
        return t;
    }

    private TextView mini(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(11);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(6), dp(6), dp(6), dp(6));
        t.setBackground(rounded(SOFT, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(32));
        lp.setMargins(dp(4), 0, 0, 0);
        t.setLayoutParams(lp);
        return t;
    }

    private TextView arrow(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(12);
        t.setGravity(Gravity.CENTER);
        t.setBackground(rounded(SOFT, 9));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(30), dp(30));
        lp.setMargins(dp(3), 0, 0, 0);
        t.setLayoutParams(lp);
        return t;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private class ChannelAdapter extends BaseAdapter {
        @Override public int getCount() { return shown.size(); }
        @Override public Object getItem(int i) { return shown.get(i); }
        @Override public long getItemId(int i) { return i; }

        @Override
        public View getView(int i, View convert, ViewGroup parent) {
            LinearLayout row = new LinearLayout(MainActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(8), dp(8), dp(10), dp(8));

            final Channel c = shown.get(i);
            final int pos = i;
            boolean sel = selected.contains(c.url);
            row.setBackground(rounded(sel ? Color.parseColor("#33405A") : CARD, 14));

            if (editMode) {
                TextView check = new TextView(MainActivity.this);
                check.setText(sel ? "☑" : "☐");
                check.setTextColor(sel ? ACCENT : MUTED);
                check.setTextSize(18);
                check.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(dp(28), dp(28));
                clp.setMargins(0, 0, dp(4), 0);
                check.setLayoutParams(clp);
                row.addView(check);
            }

            TextView no = new TextView(MainActivity.this);
            no.setText(String.valueOf(c.no));
            no.setTextColor(Color.WHITE);
            no.setTextSize(11);
            no.setGravity(Gravity.CENTER);
            no.setBackground(rounded(SOFT, 10));
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(dp(34), dp(28));
            nlp.setMargins(0, 0, dp(8), 0);
            no.setLayoutParams(nlp);
            row.addView(no);

            ImageView logo = new ImageView(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(38), dp(38));
            lp.setMargins(0, 0, dp(10), 0);
            logo.setLayoutParams(lp);
            logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LogoLoader.load(logo, c.logo);
            row.addView(logo);

            LinearLayout texts = new LinearLayout(MainActivity.this);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView name = new TextView(MainActivity.this);
            name.setText(c.flag() + "  " + c.name);
            name.setTextColor(TEXT);
            name.setTextSize(14);
            name.setTypeface(name.getTypeface(), android.graphics.Typeface.BOLD);
            texts.addView(name);

            TextView meta = new TextView(MainActivity.this);
            boolean fav = Prefs.favs(MainActivity.this).contains(c.url);
            String use = "";
            long ub = Prefs.usage(MainActivity.this, c.url);
            if (ub > 0) {
                int us = Prefs.secs(MainActivity.this, c.url);
                use = "  ·  " + Net.mb(ub) + (us > 0 ? " (" + us / 60 + " dk)" : "");
            }
            meta.setText(c.group + (fav ? "  ★" : "")
                    + (Prefs.fails(MainActivity.this, c.url) > 0 ? "  ⚠" : "") + use);
            meta.setTextColor(MUTED);
            meta.setTextSize(11);
            texts.addView(meta);
            row.addView(texts);

            if (editMode) {
                TextView up = arrow("▲");
                up.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { moveItem(pos, -1); }
                });
                row.addView(up);

                TextView down = arrow("▼");
                down.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { moveItem(pos, 1); }
                });
                row.addView(down);
            } else {
                TextView play = new TextView(MainActivity.this);
                play.setText("▶");
                play.setTextColor(Color.WHITE);
                play.setTextSize(13);
                play.setGravity(Gravity.CENTER);
                play.setBackground(rounded(ACCENT, 18));
                play.setLayoutParams(new LinearLayout.LayoutParams(dp(32), dp(32)));
                row.addView(play);
            }

            return row;
        }
    }
}
