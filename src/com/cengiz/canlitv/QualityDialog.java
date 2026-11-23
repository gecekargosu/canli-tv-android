package com.cengiz.canlitv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Tum ekranlarda ayni kalite secimi: Otomatik / 360p / 480p / 720p / 1080p / en dusuk.
 * Secim tüm kanallarda gecerlidir ve internet harcamasini dogrudan belirler.
 */
public class QualityDialog {

    private static final int[] HEIGHTS = {0, 360, 480, 720, 1080, -1};

    public static String label(Context c) {
        int h = Prefs.maxHeight(c);
        if (h < 0) return "En düşük";
        if (h == 0) return "Otomatik";
        return h + "p";
    }

    private static String gb(int height) {
        int mb = Quality.hourlyMb(height);
        return "≈" + String.format(java.util.Locale.getDefault(), "%.1f", mb / 1000.0) + " GB/saat";
    }

    public static void show(final Activity a, final Runnable after) {
        final int cur = Prefs.maxHeight(a);
        final String[] items = new String[HEIGHTS.length];
        items[0] = mark(cur, 0) + "Otomatik · kanalın en iyi kalitesi";
        items[1] = mark(cur, 1) + "360p · " + gb(360) + " · en tasarruflu";
        items[2] = mark(cur, 2) + "480p · " + gb(480) + " · dengeli";
        items[3] = mark(cur, 3) + "720p · " + gb(720) + " · net görüntü";
        items[4] = mark(cur, 4) + "1080p · " + gb(1080) + " · en çok veri";
        items[5] = mark(cur, 5) + "En düşük kalite · veri tasarrufu";

        new AlertDialog.Builder(a)
                .setTitle("Yayın kalitesi · " + label(a) + " (az veri → çok veri)")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        final int h = HEIGHTS[which];
                        d.dismiss();
                        // 360p üstü = daha fazla veri: kullanıcıdan onay al
                        if (h > 360) {
                            new AlertDialog.Builder(a)
                                    .setTitle("Daha yüksek kalite · daha çok veri")
                                    .setMessage(h + "p yayın yaklaşık " + gb(h)
                                            + " harcar ve mobil veride kotanı hızlı tüketebilir.\n\n"
                                            + "Mobil verideysen 360p önerilir; Wi-Fi'de yükseltmek mantıklı.")
                                    .setPositiveButton("Yükselt", new DialogInterface.OnClickListener() {
                                        @Override public void onClick(DialogInterface dd, int w) {
                                            Prefs.setMaxHeight(a, h);
                                            if (after != null) after.run();
                                        }
                                    })
                                    .setNegativeButton("360p'de kal", new DialogInterface.OnClickListener() {
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
                .setNegativeButton("Kapat", null)
                .show();
    }

    private static String mark(int cur, int index) {
        return HEIGHTS[index] == cur ? "● " : "○ ";
    }
}
