package com.cengiz.canlitv;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.View;

/**
 * v10.08: kumanda / D-pad odak desteği.
 *
 * <p>Dokunmatik ekranda odak halkası görünmez (parmakla gezinirken rahatsız etmesin);
 * TV kutusu, klavye veya harici kumandada odaklanan kutuda turuncu çerçeve çıkar,
 * böylece OK tuşuyla neyin açılacağı belli olur.</p>
 *
 * <p>Manifest'e leanback (Android TV) bildirimi bilerek EKLENMEDİ: bu, Play'de
 * dağıtımı ve mağaza listesi gereksinimlerini değiştirir. D-pad desteği bundan
 * bağımsız çalışır (harici kumanda, klavye, TV box).</p>
 */
public final class TvFocus {

    private static Boolean tv;

    private TvFocus() { }

    /** Cihaz kumanda/klavye ile mi kullanılıyor (dokunmatik yok veya TV). */
    public static boolean isTv(Context c) {
        if (tv != null) return tv.booleanValue();
        boolean v = false;
        try {
            PackageManager pm = c.getPackageManager();
            v = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                    || pm.hasSystemFeature("android.hardware.type.television")
                    || pm.hasSystemFeature("android.software.leanback_only");
            if (!v) v = !pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN);
        } catch (Throwable ignored) { }
        tv = Boolean.valueOf(v);
        return v;
    }

    /**
     * Kutuya odak desteği ekler: odaklanınca turuncu çerçeve + hafif büyüme,
     * odak çıkınca eski zemin geri gelir.
     */
    public static void attach(final MainUi ui, final View v, final int baseColor, final int radius) {
        try {
            v.setFocusable(true);
            v.setFocusableInTouchMode(false);
            v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override public void onFocusChange(View view, boolean has) {
                    try {
                        if (has) {
                            view.setBackground(ui.ring(MainUi.FOCUS, radius));
                            view.animate().scaleX(1.02f).scaleY(1.02f).setDuration(110).start();
                        } else {
                            view.setBackground(ui.rounded(baseColor, radius));
                            view.animate().scaleX(1f).scaleY(1f).setDuration(110).start();
                        }
                    } catch (Throwable ignored) { }
                }
            });
        } catch (Throwable ignored) { }
    }
}
