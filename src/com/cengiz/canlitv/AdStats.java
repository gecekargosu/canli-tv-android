package com.cengiz.canlitv;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Reklam sayacı (yalnız bu cihazda tutulur).
 *
 * Amaç: "kaç reklam gördüm, kaç ödüllü izledim, yaklaşık ne kadar kazandım?"
 * sorusunu cevaplamak. AdMob'un kesin tutarı SDK'dan okunamaz; bu yüzden
 * sayaç **tahmini** kazanç gösterir (gösterim sayısı × varsayılan eCPM).
 * Ayrıca her gösterim `reklam-sayaci.csv` dosyasına yazılır; AdMob raporuyla
 * karşılaştırıp eCPM'i kalibre edebilirsin.
 */
public class AdStats {

    private static final String P = "adstats";
    /** Türkiye için kaba eCPM varsayımları (TL / 1000 gösterim). */
    public static final double ECPM_BANNER = 6.0;
    public static final double ECPM_INTERSTITIAL = 35.0;
    public static final double ECPM_REWARDED = 55.0;

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE);
    }

    /** type: "banner", "gecis", "odullu" */
    public static void note(Context c, String type) {
        if (c == null || type == null) return;
        try {
            SharedPreferences s = sp(c);
            int total = s.getInt("total", 0) + 1;
            s.edit()
                    .putInt("total", total)
                    .putInt(type, s.getInt(type, 0) + 1)
                    .putLong("last", System.currentTimeMillis())
                    .apply();
            log(c, type);
        } catch (Throwable ignored) { }
    }

    private static void log(Context c, String type) {
        try {
            File dir = c.getExternalFilesDir(null);
            if (dir == null) return;
            File f = new File(dir, "reklam-sayaci.csv");
            boolean yeni = !f.exists();
            FileWriter w = new FileWriter(f, true);
            if (yeni) w.write("tarih;tur\n");
            String t = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            w.write(t + ";" + type + "\n");
            w.close();
        } catch (Throwable ignored) { }
    }

    public static int total(Context c) { return sp(c).getInt("total", 0); }
    public static int count(Context c, String type) { return sp(c).getInt(type, 0); }

    /** Tahmini kazanç (TL). */
    public static double earned(Context c) {
        return count(c, "banner") / 1000.0 * ECPM_BANNER
                + count(c, "gecis") / 1000.0 * ECPM_INTERSTITIAL
                + count(c, "odullu") / 1000.0 * ECPM_REWARDED;
    }

    /** Ayarlar satırındaki kısa değer. */
    public static String shortText(Context c) {
        int n = total(c);
        if (n == 0) return "Henüz yok";
        return n + " reklam · ~" + tl(earned(c));
    }

    public static String tl(double v) {
        return String.format(Locale.getDefault(), "%.2f TL", v);
    }

    /** Ayrıntı penceresi. */
    public static String summary(Context c) {
        int banner = count(c, "banner");
        int gecis = count(c, "gecis");
        int odul = count(c, "odullu");
        StringBuilder b = new StringBuilder();
        b.append("Gösterim: ").append(total(c)).append("\n");
        b.append("• Banner: ").append(banner).append("\n");
        b.append("• Geçiş reklamı: ").append(gecis).append("\n");
        b.append("• Ödüllü: ").append(odul).append("\n\n");
        b.append("Tahmini kazanç: ").append(tl(earned(c))).append("\n");
        b.append("(banner ").append(ECPM_BANNER).append(" · geçiş ")
                .append(ECPM_INTERSTITIAL).append(" · ödüllü ")
                .append(ECPM_REWARDED).append(" TL/1000 gösterim)\n\n");
        b.append("Not: AdMob'un gerçek tutarı ülke, reklamveren ve doluluk oranına göre\n");
        b.append("değişir; buradaki değer tahmindir. Ayrıntı dosyası:\n");
        b.append("Android/data/com.cengiz.canlitv/files/reklam-sayaci.csv");
        return b.toString();
    }

    public static void reset(Context c) {
        sp(c).edit().clear().apply();
    }
}
