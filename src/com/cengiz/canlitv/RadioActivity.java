package com.cengiz.canlitv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** İnternet radyosu: kilit ekranında da çalar, istenmeyen istasyonlar gizlenip geri eklenebilir. */
public class RadioActivity extends Activity {

    private static final int BG = Color.parseColor("#11151C");
    private static final int CARD = Color.parseColor("#1B2230");
    private static final int ACCENT = Color.parseColor("#FF7A18");
    private static final int TEXT = Color.parseColor("#F5F7FA");
    private static final int MUTED = Color.parseColor("#9AA4B2");

    private String onlineQuery = "";
    private boolean onlineBusy = false;
    private final java.util.Set<String> onlineUrls = new java.util.HashSet<String>();

    private static final List<String> NAMES = new ArrayList<String>();
    private static final List<String> META = new ArrayList<String>();
    private static final List<String> URLS = new ArrayList<String>();
    private TextView subView;

    private final List<Integer> shown = new ArrayList<Integer>();
    private String query = "";
    private boolean showHidden = false;
    private String cat = "Tümü";
    private LinearLayout chipsRow;
    private RadioAdapter adapter;

    /** Radyo turu: isimden cikarilir (haber / spor / dini / muzik). */
    static String category(String name) {
        String n = name.toLowerCase(java.util.Locale.US);
        if (n.contains("haber") || n.contains("news") || n.contains("cnn")) return "📰 Haber";
        if (n.contains("spor") || n.contains("sport") || n.contains("goal")) return "⚽ Spor";
        if (n.contains("kuran") || n.contains("mekke") || n.contains("diyanet")
                || n.contains("islam") || n.contains("moral") || n.contains("risale")
                || n.contains("ilahi")) return "🕌 Dini";
        return "🎵 Müzik";
    }

    /** Secilen ulkenin radyolarini yukler (TR gomulu, digerleri indirilip onbelleklenir). */
    private static void load(Activity a) {
        RadioRepo.ensure(a);
        NAMES.clear();
        META.clear();
        URLS.clear();
        NAMES.addAll(RadioRepo.NAMES);
        META.addAll(RadioRepo.META);
        URLS.addAll(RadioRepo.URLS);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Klavye açıkken liste 0 yüksekliğe çöküyordu (arama sonuçları görünmüyordu).
        getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        load(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        // durum cubugunun altindan basla (edge-to-edge bosluk duzeltmesi)
        root.setFitsSystemWindows(true);
        root.setPadding(dp(12), dp(12), dp(12), dp(6));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        ImageView logo = new ImageView(this);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(dp(38), dp(38));
        llp.setMargins(0, 0, dp(10), 0);
        logo.setLayoutParams(llp);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        try { logo.setImageDrawable(getPackageManager().getApplicationIcon(getPackageName())); }
        catch (Exception ignored) { }
        head.addView(logo);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(this);
        t.setText("Radyo");
        t.setTextColor(TEXT);
        t.setTextSize(21);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        subView = new TextView(this);
        subView.setText(NAMES.size() + " istasyon · kilit ekranında da çalar");
        subView.setTextColor(MUTED);
        subView.setTextSize(11);
        titles.addView(t);
        titles.addView(subView);
        head.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // Kapat: calan radyoyu durdurur ve ana sayfaya doner
        TextView close = pill("Kapat");
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioService.stop(RadioActivity.this);
                finish();
            }
        });
        head.addView(close);

        // Uygulamadan tamamen cik
        TextView quit = pill("⏻");
        quit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AudioService.stop(RadioActivity.this);
                finishAffinity();
            }
        });
        LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        qlp.setMargins(dp(6), 0, 0, 0);
        quit.setLayoutParams(qlp);
        head.addView(quit);
        root.addView(head);

        EditText search = new EditText(this);
        search.setHint("Radyo ara...");
        search.setHintTextColor(MUTED);
        search.setTextColor(TEXT);
        search.setTextSize(14);
        search.setSingleLine(true);
        GradientDrawable sbg = rounded(CARD, 14);
        search.setBackground(sbg);
        search.setPadding(dp(12), dp(8), dp(12), dp(8));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.setMargins(0, dp(10), 0, dp(6));
        root.addView(search, slp);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                query = Lang.fold(s.toString().trim());
                refresh();
            }
            public void afterTextChanged(Editable s) { }
        });

        // --- tur siniflari (TV tarafindaki gibi): favoriler, haber, spor, muzik, dini ---
        android.widget.HorizontalScrollView chipScroll = new android.widget.HorizontalScrollView(this);
        chipScroll.setHorizontalScrollBarEnabled(false);
        chipsRow = new LinearLayout(this);
        chipsRow.setOrientation(LinearLayout.HORIZONTAL);
        chipScroll.addView(chipsRow);
        LinearLayout.LayoutParams chipsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chipsLp.setMargins(0, 0, 0, dp(6));
        root.addView(chipScroll, chipsLp);
        buildChips();

        ListView list = new ListView(this);
        list.setDivider(null);
        list.setDividerHeight(dp(6));
        list.setPadding(0, dp(4), 0, dp(8));
        list.setClipToPadding(false);
        adapter = new RadioAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override public void onItemClick(android.widget.AdapterView<?> p, View v, int pos, long id) {
                play(shown.get(pos));
            }
        });
        list.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(android.widget.AdapterView<?> p, View v, int pos, long id) {
                menu(shown.get(pos));
                return true;
            }
        });
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView foot = new TextView(this);
        foot.setText("İpucu: bir radyoya uzun bas → listeden gizle. Gizlediklerini "
                + "\"🗑 Gizlenenler\" sekmesinden geri ekleyebilirsin.");
        foot.setTextColor(MUTED);
        foot.setTextSize(10);
        foot.setPadding(0, dp(4), 0, 0);
        root.addView(foot);

        // reklam: AdManager.wrap ekliyor (burada ikinci kez eklenirse ust uste iki bar olur)
        AdManager.wrap(this, root);
        AdManager.init(this);
        refresh();

        // secilen ulke icin radyo listesi yoksa indir (radio-browser.info)
        if (RadioRepo.needsDownload(this)) {
            Toast.makeText(this, "Bu ülkenin radyoları indiriliyor…", Toast.LENGTH_SHORT).show();
            RadioRepo.download(this, new Runnable() {
                @Override public void run() {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            load(RadioActivity.this);
                            if (subView != null) {
                                subView.setText(NAMES.size() + " istasyon · kilit ekranında da çalar");
                            }
                            if (chipsRow != null) buildChips();
                            refresh();
                            Toast.makeText(RadioActivity.this,
                                    NAMES.size() + " radyo hazır", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void play(final int i) {
        // Radyo secimi reklamsiz acilir: gecis reklami sesi kesiyordu (kullanici sikayeti).
        // Gelir, liste banner'i + izlerken cikan reklamlar + 40 dk sonraki odullu teklifle surer.
        RadioPlayerActivity.open(this, i);
    }

    /** Tur cipleri: Tümü, ★ Favoriler, haber/spor/müzik/dini, Gizlenenler. */
    private void buildChips() {
        chipsRow.removeAllViews();
        final String[] cats = new String[]{"Tümü", "★ Favoriler", "📰 Haber", "⚽ Spor",
                "🎵 Müzik", "🕌 Dini", "🗑 Gizlenenler"};
        for (final String c : cats) {
            TextView chip = pill(c);
            if (c.equals(cat)) {
                GradientDrawable on = rounded(ACCENT, 16);
                chip.setBackground(on);
                chip.setTextColor(Color.parseColor("#111111"));
                chip.setTypeface(chip.getTypeface(), android.graphics.Typeface.BOLD);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dp(8), 0);
            chip.setLayoutParams(lp);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    cat = c;
                    showHidden = "🗑 Gizlenenler".equals(c);
                    buildChips();
                    refresh();
                }
            });
            chipsRow.addView(chip);
        }
    }

    private void menu(final int i) {
        final boolean hidden = Prefs.hiddenRadios(this).contains(URLS.get(i));
        final boolean fav = Prefs.favs(this).contains(URLS.get(i));
        final boolean mine = Prefs.hasCustomRadio(this, URLS.get(i));
        String[] items = new String[]{
                fav ? "★ Favorilerden çıkar" : "★ Favorilere ekle",
                hidden ? "➕ Listeye geri ekle" : "🗑 Listeden gizle",
                mine ? "🗑 Kendi listemden çıkar" : "➕ Kendi listeme ekle (kalıcı)",
                "▶ Şimdi çal"
        };
        new AlertDialog.Builder(this).setTitle(NAMES.get(i)).setItems(items,
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == 3) { play(i); return; }
                        if (which == 2) {
                            if (mine) {
                                Prefs.removeCustomRadio(RadioActivity.this, URLS.get(i));
                                Toast.makeText(RadioActivity.this, "Kendi listemden çıkarıldı",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Prefs.addCustomRadio(RadioActivity.this, NAMES.get(i), URLS.get(i));
                                Toast.makeText(RadioActivity.this,
                                        "★ Kendi listeme eklendi — her açılışta en üstte",
                                        Toast.LENGTH_LONG).show();
                            }
                            refresh();
                            return;
                        }
                        if (which == 0) {
                            Prefs.toggleFav(RadioActivity.this, URLS.get(i));
                            Toast.makeText(RadioActivity.this,
                                    fav ? "Favorilerden çıkarıldı" : "★ Favorilere eklendi",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            if (hidden) Prefs.unhideRadio(RadioActivity.this, URLS.get(i));
                            else Prefs.hideRadio(RadioActivity.this, URLS.get(i));
                        }
                        refresh();
                    }
                }).show();
    }

    private void refresh() {
        shown.clear();
        java.util.Set<String> hid = Prefs.hiddenRadios(this);
        java.util.Set<String> favs = Prefs.favs(this);
        for (int i = 0; i < NAMES.size(); i++) {
            boolean isHidden = hid.contains(URLS.get(i));
            // Arama yapılırken gizli istasyonlar da sonuçta görünsün (kullanıcı arıyorsa bulmalı).
            if (showHidden != isHidden && query.length() == 0) continue;
            // internetten gelen sonuclar: isim eslesmese de gosterilir
            if (onlineUrls.contains(URLS.get(i))) { shown.add(i); continue; }
            if ("★ Favoriler".equals(cat) && !favs.contains(URLS.get(i))) continue;
            if (!"Tümü".equals(cat) && !"★ Favoriler".equals(cat) && !showHidden
                    && !cat.equals(category(NAMES.get(i)))) continue;
            if (query.length() > 0) {
                String nm = NAMES.get(i).toLowerCase();
                String mt = META.get(i).toLowerCase();
                String qs = query.replace(" ", "");
                nm = Lang.fold(nm); mt = Lang.fold(mt);
                boolean hit = nm.contains(query) || mt.contains(query)
                        || nm.replace(" ", "").contains(qs);   // "radyofenomen" = "Radyo Fenomen"
                if (!hit) continue;
            }
            shown.add(i);
        }
        adapter.notifyDataSetChanged();
        // listede yok: internette ara (en az 3 harf), ayni sorgu icin bir kez
        if (shown.isEmpty() && query.length() >= 3 && !query.equals(onlineQuery) && !onlineBusy) {
            searchOnline(query);
        }
    }

    /** Arama cubuguna yazilan istasyon listede yoksa radio-browser'da arar. */
    private void searchOnline(final String q) {
        onlineQuery = q;
        onlineBusy = true;
        Toast.makeText(this, "Listede yok — internette aranıyor: " + q, Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override public void run() {
                int found = 0;
                String err = null;
                try {
                    String u = "https://de1.api.radio-browser.info/json/stations/search"
                            + "?hidebroken=true&order=clickcount&reverse=true&limit=15&name="
                            + java.net.URLEncoder.encode(q, "UTF-8");
                    java.net.HttpURLConnection con =
                            (java.net.HttpURLConnection) new java.net.URL(u).openConnection();
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    con.setRequestProperty("User-Agent", "UMAY-MultiTV/9.17");
                    java.io.BufferedReader r = new java.io.BufferedReader(
                            new java.io.InputStreamReader(con.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) sb.append(line);
                    r.close();
                    con.disconnect();
                    org.json.JSONArray arr = new org.json.JSONArray(sb.toString());
                    for (int k = 0; k < arr.length(); k++) {
                        org.json.JSONObject o = arr.getJSONObject(k);
                        String nm = o.optString("name", "").trim();
                        String su = o.optString("url_resolved", o.optString("url", "")).trim();
                        if (nm.length() == 0 || su.length() < 8) continue;
                        if (!su.startsWith("http")) continue;
                        if (URLS.contains(su)) continue;
                        String cc = o.optString("country", "");
                        int br = o.optInt("bitrate", 0);
                        NAMES.add(nm);
                        META.add((cc.length() > 0 ? cc : "internet") + " · "
                                + (br > 0 ? br + " kbps" : "internet"));
                        URLS.add(su);
                        onlineUrls.add(su);
                        found++;
                        if (found >= 15) break;
                    }
                } catch (Throwable e) {
                    err = e.toString();
                }
                final int n = found;
                final String e2 = err;
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        onlineBusy = false;
                        refresh();   // internet sonuclari listeye girsin
                        if (n > 0) {
                            // klavyeyi kapat: sonuclar ekranda gorunsun
                            try {
                                android.view.inputmethod.InputMethodManager im =
                                        (android.view.inputmethod.InputMethodManager)
                                                getSystemService(INPUT_METHOD_SERVICE);
                                View f = getCurrentFocus();
                                if (im != null && f != null) im.hideSoftInputFromWindow(f.getWindowToken(), 0);
                            } catch (Throwable ignored) { }
                            Toast.makeText(RadioActivity.this,
                                    "İnternetten " + n + " istasyon bulundu — uzun basıp \"kendi listeme ekle\"",
                                    Toast.LENGTH_LONG).show();
                        } else if (e2 == null) {
                            Toast.makeText(RadioActivity.this,
                                    "Bu istasyon zaten listende (ya da internet sonucu yok)",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RadioActivity.this,
                                    "İnternete ulaşılamadı: " + e2, Toast.LENGTH_LONG).show();
                            android.util.Log.w("UMAY", "online arama hatasi: " + e2);
                        }
                    }
                });
            }
        }).start();
    }

    private class RadioAdapter extends BaseAdapter {
        @Override public int getCount() { return shown.size(); }
        @Override public Object getItem(int i) { return NAMES.get(shown.get(i)); }
        @Override public long getItemId(int i) { return i; }

        @Override public View getView(int pos, View v, ViewGroup p) {
            int i = shown.get(pos);
            LinearLayout row = new LinearLayout(RadioActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(10), dp(10), dp(10));
            GradientDrawable d = rounded(CARD, 14);
            row.setBackground(d);

            LinearLayout texts = new LinearLayout(RadioActivity.this);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            TextView n = new TextView(RadioActivity.this);
            n.setText((Prefs.favs(RadioActivity.this).contains(URLS.get(i)) ? "★ " : "📻  ")
                    + NAMES.get(i));
            n.setTextColor(TEXT);
            n.setTextSize(14);
            n.setTypeface(n.getTypeface(), android.graphics.Typeface.BOLD);
            TextView m = new TextView(RadioActivity.this);
            m.setText(META.get(i));
            m.setTextColor(MUTED);
            m.setTextSize(11);
            texts.addView(n);
            texts.addView(m);
            row.addView(texts);

            TextView play = new TextView(RadioActivity.this);
            play.setText("▶");
            play.setTextColor(Color.WHITE);
            play.setTextSize(13);
            play.setGravity(Gravity.CENTER);
            play.setBackground(rounded(ACCENT, 18));
            play.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(34)));
            row.addView(play);
            return row;
        }
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView pill(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(12);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(8), dp(12), dp(8));
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor("#2A3444"));
        d.setCornerRadius(dp(16));
        t.setBackground(d);
        return t;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
