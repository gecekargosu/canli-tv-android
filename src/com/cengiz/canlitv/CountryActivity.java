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

/**
 * Ulke secimi: secilen ulkenin kanal listesi indirilir (veya hazirsa acilir) ve ana liste o ulkeye gecer.
 * Listeye uzun basmak, o ulkenin listesini yeniden indirir (guncelleme icin).
 */
public class CountryActivity extends Activity {

    private static final int BG = Color.parseColor("#11151C");
    private static final int CARD = Color.parseColor("#1B2230");
    private static final int TEXT = Color.parseColor("#F5F7FA");
    private static final int MUTED = Color.parseColor("#9AA4B2");
    private static final int ACCENT = Color.parseColor("#FF7A18");

    private BaseAdapter adapter;
    private String selected;

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

        ListView list = new ListView(this);
        list.setDivider(null);
        list.setDividerHeight(dp(6));
        list.setPadding(0, dp(4), 0, dp(8));
        list.setClipToPadding(false);
        adapter = new BaseAdapter() {
            @Override public int getCount() { return Countries.LIST.length; }
            @Override public Object getItem(int i) { return Countries.LIST[i][0]; }
            @Override public long getItemId(int i) { return i; }
            @Override public View getView(int i, View v, ViewGroup p) {
                String code = Countries.LIST[i][0];
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
                boolean ready = CountryRepo.isReady(CountryActivity.this, code);
                s.setText(ready ? Lang.t(CountryActivity.this, "ready") : Lang.t(CountryActivity.this, "download"));
                s.setTextColor(ready ? MUTED : ACCENT);
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
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                pick(Countries.LIST[pos][0], false);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> p, View v, int pos, long id) {
                String code = Countries.LIST[pos][0];
                if ("tr".equals(code)) return true;
                pick(code, true);
                return true;
            }
        });
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        AdManager.wrap(this, root);
    }

    private void pick(final String code, final boolean force) {
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
                final boolean ok = CountryRepo.download(CountryActivity.this, code);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (dlg.isShowing()) dlg.dismiss();
                        if (!ok) {
                            Toast.makeText(CountryActivity.this, Lang.t(CountryActivity.this, "download_failed"),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
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
