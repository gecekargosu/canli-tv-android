package com.cengiz.canlitv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ulke secimi: secilen ulkenin kanal listesi indirilir (veya hazirsa acilir) ve ana liste o ulkeye gecer.
 * Listeye uzun basmak, o ulkenin listesini yeniden indirir (guncelleme icin).
 *
 * M-85: sirala → listesi hazir ulkeler (Türkiye ilk), sonra indirilebilenler, en sonda "yakında".
 */
public class CountryActivity extends Activity {
    private ListView list;

    private static final int BG = Color.parseColor("#11151C");
    private static final int CARD = Color.parseColor("#1B2230");
    private static final int TEXT = Color.parseColor("#F5F7FA");
    private static final int MUTED = Color.parseColor("#9AA4B2");
    private static final int ACCENT = Color.parseColor("#FF7A18");

    private BaseAdapter adapter;
    private String selected;

    /** Gorunen sira ve ulke basina kanal sayisi (her satirda dosya okumayalim). */
    private final List<String> order = new ArrayList<String>();
    private final Map<String, Integer> counts = new HashMap<String, Integer>();

    private TextView infoBody;
    private boolean infoOpen;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        selected = Prefs.country(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setFitsSystemWindows(true);
        root.setPadding(dp(14), dp(14), dp(14), dp(8));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setPadding(0, 0, 0, dp(6));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = chip("‹ " + Lang.t(this, "back"));
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        top.addView(back);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(12), 0, 0, 0);
        TextView t = new TextView(this);
        t.setText(Lang.t(this, "countries_title"));
        t.setTextColor(TEXT);
        t.setTextSize(21);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        TextView sub = new TextView(this);
        sub.setText(Lang.t(this, "countries_sub"));
        sub.setTextColor(MUTED);
        sub.setTextSize(10);
        col.addView(t);
        col.addView(sub);
        top.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(top);
        root.addView(head);

        addInfoCard(root);

        list = new ListView(this);
        list.setDivider(null);
        list.setDividerHeight(dp(6));
        list.setPadding(0, dp(4), 0, dp(8));
        list.setClipToPadding(false);
        adapter = new BaseAdapter() {
            @Override public int getCount() { return order.size(); }
            @Override public Object getItem(int i) { return order.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View v, ViewGroup p) {
                String code = order.get(i);
                boolean active = code.equals(selected);
                LinearLayout row = new LinearLayout(CountryActivity.this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(12), dp(10), dp(12), dp(10));
                GradientDrawable d = new GradientDrawable();
                d.setColor(active ? Color.parseColor("#2A2118") : CARD);
                d.setCornerRadius(dp(14));
                if (active) d.setStroke(dp(1), ACCENT);
                row.setBackground(d);

                TextView fl = new TextView(CountryActivity.this);
                fl.setText(Countries.flag(code));
                fl.setTextSize(20);
                row.addView(fl);

                LinearLayout cc = new LinearLayout(CountryActivity.this);
                cc.setOrientation(LinearLayout.VERTICAL);
                cc.setPadding(dp(10), 0, dp(6), 0);
                TextView n = new TextView(CountryActivity.this);
                n.setText(Countries.name(code, "en".equals(Prefs.uiLang(CountryActivity.this))));
                n.setTextColor(TEXT);
                n.setTextSize(14);
                TextView s = new TextView(CountryActivity.this);
                int cnt = countOf(code);
                boolean ready = "tr".equals(code) || cnt > 0;
                if (ready) {
                    s.setText(Lang.channels(CountryActivity.this, cnt)
                            + " · " + Lang.t(CountryActivity.this, "ready"));
                    s.setTextColor(MUTED);
                } else if (Prefs.isCountryMiss(CountryActivity.this, code)) {
                    // M-83: sunucuda bu ülkenin listesi yok (404) → boşuna indirme denemesi yok
                    s.setText(Lang.t(CountryActivity.this, "soon_no_list"));
                    s.setTextColor(MUTED);
                } else {
                    s.setText(Lang.t(CountryActivity.this, "download"));
                    s.setTextColor(ACCENT);
                }
                s.setTextSize(10);
                cc.addView(n);
                cc.addView(s);
                row.addView(cc, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                if (active) {
                    TextView ok = new TextView(CountryActivity.this);
                    ok.setText("✓");
                    ok.setTextColor(ACCENT);
                    ok.setTextSize(16);
                    row.addView(ok);
                }
                return row;
            }
        };
        rebuildOrder();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                if (pos < 0 || pos >= order.size()) return;
                pick(order.get(pos), false);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> p, View v, int pos, long id) {
                if (pos < 0 || pos >= order.size()) return true;
                String code = order.get(pos);
                if ("tr".equals(code)) return true;
                pick(code, true);
                return true;
            }
        });
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        AdManager.wrap(this, root);
        // M-84/M-85: hangi ülkelerin listesi var, sunucudan bir kez öğren (~1 KB).
        // Cevap gelince sıra ve etiketler tazelenir (yeni liste "indir" olarak görünür).
        CountryRepo.syncIndex(this, new Runnable() {
            @Override public void run() {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        rebuildOrder();
                        if (adapter != null) adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    /**
     * "Kanal neden açılmıyor?" bilgi kartı — kullanıcının kendi dilinde.
     * Kısa metin her zaman görünür; dokununca ayrıntı ve çözüm açılır.
     */
    private void addInfoCard(LinearLayout root) {
        final LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(9), dp(12), dp(9));
        card.setBackground(infoBg(false));

        TextView title = new TextView(this);
        title.setText("ⓘ " + Lang.t(this, "info_title"));
        title.setTextColor(ACCENT);
        title.setTextSize(11);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

        infoBody = new TextView(this);
        infoBody.setText(Lang.t(this, "info_short"));
        infoBody.setTextColor(MUTED);
        infoBody.setTextSize(10);
        infoBody.setLineSpacing(dp(2), 1f);

        card.addView(title);
        card.addView(infoBody);
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                infoOpen = !infoOpen;
                infoBody.setText(Lang.t(CountryActivity.this, infoOpen ? "info_full" : "info_short"));
                card.setBackground(infoBg(infoOpen));
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(6);
        root.addView(card, lp);
    }

    private GradientDrawable infoBg(boolean open) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor(open ? "#1E2A38" : "#17202B"));
        d.setCornerRadius(dp(14));
        d.setStroke(dp(1), Color.parseColor(open ? "#3A4A5E" : "#28333F"));
        return d;
    }

    /** Listesi hazır ülkeler üstte (Türkiye ilk), sonra indirilebilenler, en sonda "yakında". */
    private void rebuildOrder() {
        counts.clear();
        final List<String> ready = new ArrayList<String>();
        final List<String> open = new ArrayList<String>();
        final List<String> soon = new ArrayList<String>();
        for (String[] row : Countries.LIST) {
            String code = row[0];
            int cnt = CountryRepo.isReady(this, code) ? CountryRepo.count(this, code) : 0;
            counts.put(code, cnt);
            if ("tr".equals(code) || cnt > 0) ready.add(code);
            else if (Prefs.isCountryMiss(this, code)) soon.add(code);
            else open.add(code);
        }
        Comparator<String> byName = new Comparator<String>() {
            @Override public int compare(String a, String b) {
                return Countries.name(a, false).compareToIgnoreCase(Countries.name(b, false));
            }
        };
        Collections.sort(ready, new Comparator<String>() {
            @Override public int compare(String a, String b) {
                if ("tr".equals(a)) return -1;
                if ("tr".equals(b)) return 1;
                int ca = countOf(a), cb = countOf(b);
                if (ca != cb) return cb - ca;
                return Countries.name(a, false).compareToIgnoreCase(Countries.name(b, false));
            }
        });
        Collections.sort(open, byName);
        Collections.sort(soon, byName);
        order.clear();
        order.addAll(ready);
        order.addAll(open);
        order.addAll(soon);
    }

    private int countOf(String code) {
        Integer v = counts.get(code);
        return v == null ? 0 : v;
    }

    private void pick(final String code, final boolean force) {
        if (!force && !CountryRepo.isReady(this, code) && Prefs.isCountryMiss(this, code)) {
            Toast.makeText(this, Lang.t(this, "no_list_toast"), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!force && CountryRepo.isReady(this, code)) {
            Prefs.setCountry(this, code);
            ChannelRepo.reload(this);
            finish();
            return;
        }
        final ProgressDialog dlg = new ProgressDialog(this);
        dlg.setMessage(Lang.t(this, "downloading"));
        dlg.setCancelable(false);
        dlg.show();
        new Thread(new Runnable() {
            @Override public void run() {
                final int st = CountryRepo.downloadStatus(CountryActivity.this, code);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (dlg.isShowing()) dlg.dismiss();
                        if (st != CountryRepo.DL_OK) {
                            // 404 → "yakında" işaretle. Ağ hatası → işaretleme, tekrar denenebilsin.
                            if (st == CountryRepo.DL_GONE) {
                                Prefs.markCountryMiss(CountryActivity.this, code);
                            }
                            rebuildOrder();
                            if (adapter != null) adapter.notifyDataSetChanged();
                            Toast.makeText(CountryActivity.this,
                                    st == CountryRepo.DL_GONE
                                            ? Lang.t(CountryActivity.this, "no_list_toast")
                                            : Lang.t(CountryActivity.this, "download_failed"),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        // M-85: indirilen ülkenin "çalışan kanallar önce" sırası hemen kurulsun
                        CountryRepo.fetchGood(CountryActivity.this, code);
                        Prefs.setCountry(CountryActivity.this, code);
                        ChannelRepo.reload(CountryActivity.this);
                        finish();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        selected = Prefs.country(this);
        rebuildOrder();
        if (adapter != null) adapter.notifyDataSetChanged();
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
        return t;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
