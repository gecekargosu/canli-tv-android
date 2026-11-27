package com.cengiz.canlitv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Tum ekranlarda ayni kalite secimi: Otomatik / 360p / 480p / 720p / 1080p / en dusuk.
 * Secim tüm kanallarda gecerlidir ve internet harcamasini dogrudan belirler.
 * v10.06: butun metinler I18n ile 8 dilde.
 */
public class QualityDialog {

    private static final int[] HEIGHTS = {0, 360, 480, 720, 1080, -1};

    public static String label(Context c) {
        int h = Prefs.maxHeight(c);
        if (h < 0) return Lang.t(c, "q_lowest");
        if (h == 0) return Lang.t(c, "q_auto");
        return h + "p";
    }

    private static String gb(Context c, int height) {
        int mb = Quality.hourlyMb(height);
        return "≈" + String.format(java.util.Locale.getDefault(), "%.1f", mb / 1000.0)
                + I18n.t(c, " GB/saat");
    }

    public static void show(final Activity a, final Runnable after) {
        final int cur = Prefs.maxHeight(a);
        final String[] items = new String[HEIGHTS.length];
        items[0] = mark(cur, 0) + I18n.t(a, "Otomatik · kanalın en iyi kalitesi");
        items[1] = mark(cur, 1) + String.format(java.util.Locale.ROOT,
                I18n.t(a, "360p · %s · en tasarruflu"), gb(a, 360));
        items[2] = mark(cur, 2) + String.format(java.util.Locale.ROOT,
                I18n.t(a, "480p · %s · dengeli"), gb(a, 480));
        items[3] = mark(cur, 3) + String.format(java.util.Locale.ROOT,
                I18n.t(a, "720p · %s · net görüntü"), gb(a, 720));
        items[4] = mark(cur, 4) + String.format(java.util.Locale.ROOT,
                I18n.t(a, "1080p · %s · en çok veri"), gb(a, 1080));
        items[5] = mark(cur, 5) + I18n.t(a, "En düşük kalite · veri tasarrufu");

        new AlertDialog.Builder(a)
                .setTitle(String.format(java.util.Locale.ROOT,
                        I18n.t(a, "Yayın kalitesi · %s (az veri → çok veri)"), label(a)))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        final int h = HEIGHTS[which];
                        d.dismiss();
                        // 360p üstü = daha fazla veri: kullanıcıdan onay al
                        if (h > 360) {
                            new AlertDialog.Builder(a)
                                    .setTitle(I18n.t(a, "Daha yüksek kalite · daha çok veri"))
                                    .setMessage(String.format(java.util.Locale.ROOT,
                                            I18n.t(a, "%dp yayın yaklaşık %s harcar ve mobil veride kotanı hızlı tüketebilir.\n\nMobil verideysen 360p önerilir; Wi-Fi'de yükseltmek mantıklı."),
                                            h, gb(a, h)))
                                    .setPositiveButton(I18n.t(a, "Yükselt"), new DialogInterface.OnClickListener() {
                                        @Override public void onClick(DialogInterface dd, int w) {
                                            Prefs.setMaxHeight(a, h);
                                            if (after != null) after.run();
                                        }
                                    })
                                    .setNegativeButton(I18n.t(a, "360p'de kal"), new DialogInterface.OnClickListener() {
                                        @Override public void onClick(DialogInterface dd, int w) {
                                            Prefs.setMaxHeight(a, 360);
                                            if (after != null) after.run();
                                        }
                                    })
                                    .show();
                            return;
                        }
                        Prefs.setMaxHeight(a, h);
                        if (after != null) after.run();
                    }
                })
                .setNegativeButton(I18n.t(a, "Kapat"), null)
                .show();
    }

    private static String mark(int cur, int index) {
        return HEIGHTS[index] == cur ? "● " : "○ ";
    }
}
