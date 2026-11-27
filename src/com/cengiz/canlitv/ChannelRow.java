package com.cengiz.canlitv;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * v10.08 (teknik borç): kanal listesi satırı MainActivity'den çıkarıldı.
 * Satır yalnızca görünüm üretir; açma / menü / sıra değiştirme işleri Host'a gider.
 */
final class ChannelRow {

    /** Satırdan çıkan eylemler (açma/menü tıklaması MainActivity'nin adapter'ında). */
    interface Host {
        void moveChannel(int pos, int delta);
    }

    private ChannelRow() { }

    static LinearLayout build(Activity a, MainUi ui, final Channel c, final int pos,
                              boolean editMode, boolean selected, final Host host) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(ui.dp(8), ui.dp(8), ui.dp(10), ui.dp(8));

        final int bg = selected ? Color.parseColor("#33405A") : MainUi.CARD;
        row.setBackground(ui.rounded(bg, 14));
        // v10.08: kumanda ile satır seçilebilsin (odaklanınca turuncu çerçeve).
        TvFocus.attach(ui, row, bg, 14);

        if (editMode) {
            TextView check = new TextView(a);
            check.setText(selected ? "☑" : "☐");
            check.setTextColor(selected ? MainUi.ACCENT : MainUi.MUTED);
            check.setTextSize(18);
            check.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(ui.dp(28), ui.dp(28));
            clp.setMargins(0, 0, ui.dp(4), 0);
            check.setLayoutParams(clp);
            row.addView(check);
        }

        TextView no = new TextView(a);
        no.setText(String.valueOf(c.no));
        no.setTextColor(Color.WHITE);
        no.setTextSize(11);
        no.setGravity(Gravity.CENTER);
        no.setBackground(ui.rounded(MainUi.SOFT, 10));
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(ui.dp(34), ui.dp(28));
        nlp.setMargins(0, 0, ui.dp(8), 0);
        no.setLayoutParams(nlp);
        no.setFocusable(false);
        row.addView(no);

        // 9.89: logo alanı kaldırıldı. Harici logolar (P0-5) ve yer tutucu harf
        // rozetleri kullanıcıya bilgi vermiyordu; satır numara + kanal adıyla kalıyor.

        LinearLayout texts = new LinearLayout(a);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = new TextView(a);
        name.setText(c.name);
        name.setTextColor(MainUi.TEXT);
        name.setTextSize(14);
        name.setTypeface(name.getTypeface(), android.graphics.Typeface.BOLD);
        texts.addView(name);

        TextView meta = new TextView(a);
        boolean fav = Prefs.favs(a).contains(c.url);
        String use = "";
        long ub = Prefs.usage(a, c.url);
        if (ub > 0) {
            int us = Prefs.secs(a, c.url);
            use = "  ·  " + Net.mb(ub) + (us > 0 ? " (" + us / 60 + " dk)" : "");
        }
        meta.setText(Lang.group(a, c.group) + (fav ? "  ★" : "")
                + (Prefs.fails(a, c.url) > 0 ? "  ⚠" : "") + use);
        meta.setTextColor(MainUi.MUTED);
        meta.setTextSize(11);
        texts.addView(meta);
        row.addView(texts);

        if (editMode) {
            TextView up = ui.arrow("▲");
            up.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { host.moveChannel(pos, -1); }
            });
            row.addView(up);

            TextView down = ui.arrow("▼");
            down.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { host.moveChannel(pos, 1); }
            });
            row.addView(down);
        } else {
            TextView play = new TextView(a);
            play.setText("▶");
            play.setTextColor(Color.WHITE);
            play.setTextSize(13);
            play.setGravity(Gravity.CENTER);
            play.setBackground(ui.rounded(MainUi.ACCENT, 18));
            play.setLayoutParams(new LinearLayout.LayoutParams(ui.dp(32), ui.dp(32)));
            // Süs: satırın kendisi açılır, kumanda bu rozete takılmasın.
            play.setFocusable(false);
            play.setClickable(false);
            row.addView(play);
        }

        return row;
    }
}
