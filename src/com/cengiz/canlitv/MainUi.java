package com.cengiz.canlitv;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * v10.08 (teknik borç): MainActivity'den çıkarılan görsel yardımcılar.
 * Renkler ve kutu üreticileri tek yerde; ana ekran, başlık ve kanal satırı buradan beslenir.
 */
public final class MainUi {

    public static final int BG = Color.parseColor("#11151C");
    public static final int CARD = Color.parseColor("#1B2230");
    public static final int ACCENT = Color.parseColor("#FF7A18");
    public static final int TEXT = Color.parseColor("#F5F7FA");
    public static final int MUTED = Color.parseColor("#9AA4B2");
    public static final int SOFT = Color.parseColor("#2A3444");
    /** Odaklanan kutunun zemini (kumanda/D-pad ile gezinirken). */
    public static final int FOCUS = Color.parseColor("#26303F");

    private final Activity a;

    public MainUi(Activity a) { this.a = a; }

    public Activity activity() { return a; }

    public int dp(int v) {
        return (int) (v * a.getResources().getDisplayMetrics().density);
    }

    public GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    /** Odak çerçevesi: turuncu 2dp kenar + açık zemin. */
    public GradientDrawable ring(int color, int radius) {
        GradientDrawable d = rounded(color, radius);
        d.setStroke(dp(2), ACCENT);
        return d;
    }

    /** Satır içinde eşit genişlikte kutu: ilk kutu kenara yapışık, aralarında 4dp boşluk. */
    public LinearLayout.LayoutParams slot(int index) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        if (index > 0) lp.setMargins(dp(4), 0, 0, 0);
        return lp;
    }

    public TextView pill(String s, int color) {
        TextView t = new TextView(a);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        // 9.80: kutu 44dp sabit, içindeki ikon 28sp (28dp). Yazı yok — sadece ikon.
        t.setTextSize(s.codePointCount(0, s.length()) <= 1 ? 28 : 13);
        t.setGravity(Gravity.CENTER);
        t.setIncludeFontPadding(false);
        t.setPadding(0, 0, 0, 0);
        t.setBackground(rounded(color, 16));
        t.setMinHeight(dp(44));
        t.setMinWidth(0);
        return t;
    }

    public TextView mini(String s) {
        TextView t = new TextView(a);
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

    public TextView arrow(String s) {
        TextView t = new TextView(a);
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

    /** Sekme kutusu: üstte ikon, altında küçük yazı. Kutu 44dp (satır yüksekliği). */
    public LinearLayout tabBox(String icon, String label, TextView[] labelOut) {
        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(0, dp(1), 0, dp(1));
        TextView ic = new TextView(a);
        ic.setText(icon);
        ic.setTextSize(15);
        ic.setTextColor(MUTED);
        ic.setGravity(Gravity.CENTER);
        ic.setIncludeFontPadding(false);
        ic.setSingleLine(true);
        TextView lb = new TextView(a);
        lb.setText(label);
        lb.setTextSize(8.5f);
        lb.setTextColor(MUTED);
        lb.setGravity(Gravity.CENTER);
        lb.setSingleLine(true);
        lb.setEllipsize(android.text.TextUtils.TruncateAt.END);
        lb.setIncludeFontPadding(false);
        box.addView(ic);
        box.addView(lb);
        if (labelOut != null) labelOut[0] = lb;
        return box;
    }
}
