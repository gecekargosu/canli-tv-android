package com.cengiz.canlitv;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Reklam sayacı (yalnız bu cihazda tutulur).
 *
 * Amaç: "kaç reklam gördüm, kaç ödüllü izledim, yaklaşık ne kadar kazandım?"
 * sorusunu cevaplamak. AdMob'un kesin tutarı SDK'dan okunamaz; bu yüzden
 * sayaç **tahmini** kazanç gösterir (gösterim sayısı × varsayılan eCPM).
 *
 * Önemli sınırlar (kullanıcıya pencerede de yazılıyor):
 * - Sayaç cihaz başına tutulur; tablet/ikinci telefon kendi sayacını tutar.
 * - AdMob geliri hesap genelindedir ve 1-2 gün gecikmeyle kesinleşir.
 * - Banner'da AdMob her yenilenen gösterimi sayar; buradaki değer yine tahmindir.
 *
 * Ayrıca her gösterim `reklam-sayaci.csv` dosyasına yazılır; AdMob raporuyla
 * karşılaştırıp eCPM'i kalibre edebilirsin.
 */
public class AdStats {

    private static final String P = "adstats";
    /** Türkiye için kaba eCPM varsayımları (TL / 1000 gösterim). */
    public static final double ECPM_BANNER = 6.0;
    public static final double ECPM_INTERSTITIAL = 35.0;
    public static final double ECPM_REWARDED = 55.0;

    private static final String[] TYPES = {"banner", "gecis", "odullu"};

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(P, Context.MODE_PRIVATE);
    }

    /** type: "banner", "gecis", "odullu" */
    public static void note(Context c, String type) {
        if (c == null || type == null) return;
        // Test reklamı para kazandırmaz; test modunda sayacı şişirmeyelim.
        if (Prefs.adTestMode(c)) return;
        try {
            SharedPreferences s = sp(c);
            s.edit()
                    .putInt("total", s.getInt("total", 0) + 1)
                    .putInt(type, s.getInt(type, 0) + 1)
                    .putInt(dayKey(today(), type), s.getInt(dayKey(today(), type), 0) + 1)
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

    /** Belirli bir günün tahmini kazancı. */
    public static double earnedOn(Context c, String day) {
        double v = 0;
        for (String t : TYPES) v += sp(c).getInt(dayKey(day, t), 0) / 1000.0 * ecpm(t);
        return v;
    }

    /** Belirli bir ayın (yyyy-MM) tahmini kazancı. */
    public static double earnedMonth(Context c, String month) {
        double v = 0;
        for (Map.Entry<String, ?> e : sp(c).getAll().entrySet()) {
            String k = e.getKey();
            if (!k.startsWith("d:") || !(e.getValue() instanceof Integer)) continue;
            // d:yyyy-MM-dd:tur
            String[] p = k.split(":");
            if (p.length != 3 || !p[1].startsWith(month)) continue;
            v += ((Integer) e.getValue()) / 1000.0 * ecpm(p[2]);
        }
        return v;
    }

    private static double ecpm(String type) {
        if ("banner".equals(type)) return ECPM_BANNER;
        if ("gecis".equals(type)) return ECPM_INTERSTITIAL;
        return ECPM_REWARDED;
    }

    private static String dayKey(String day, String type) {
        return "d:" + day + ":" + type;
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private static String shift(int days, String pattern) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        return new SimpleDateFormat(pattern, Locale.US).format(cal.getTime());
    }

    private static String thisMonth() {
        return new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
    }

    private static String lastMonth() {
        return shift(-1, "yyyy-MM");
    }

    /** Ayarlar satırındaki kısa değer. */
    public static String shortText(Context c) {
        int n = total(c);
        if (n == 0) return "Henüz yok";
        return n + " reklam · ~" + tl(earned(c));
    }

    public static String tl(double v) {
        return String.format(new Locale("tr", "TR"), "₺%.2f", v);
    }

    /** Ayrıntı penceresi: toplam + bugün/dün/bu ay/geçen ay kırılımı. */
    public static String summary(Context c) {
        StringBuilder b = new StringBuilder();
        b.append("Toplam tahmini kazanç: ").append(tl(earned(c))).append("\n\n");
        b.append("Bugün şimdiye kadar: ").append(tl(earnedOn(c, today()))).append("\n");
        b.append("Dün: ").append(tl(earnedOn(c, shift(-1, "yyyy-MM-dd")))).append("\n");
        b.append("Bu ay şimdiye kadar: ").append(tl(earnedMonth(c, thisMonth()))).append("\n");
        b.append("Geçen ay: ").append(tl(earnedMonth(c, lastMonth()))).append("\n\n");
        b.append("Gösterim: ").append(total(c)).append("  (banner ")
                .append(count(c, "banner")).append(" · geçiş ")
                .append(count(c, "gecis")).append(" · ödüllü ")
                .append(count(c, "odullu")).append(")\n\n");
        b.append("Tahmin: banner ").append(ECPM_BANNER).append(" · geçiş ")
                .append(ECPM_INTERSTITIAL).append(" · ödüllü ")
                .append(ECPM_REWARDED).append(" TL/1000 gösterim.\n");
        b.append("Bu sayaç yalnız bu cihazı sayar (tablet/ikinci telefon ayrı sayılır).\n");
        b.append("AdMob geliri hesap genelindedir ve 1-2 gün gecikmeyle kesinleşir.\n");
        b.append("Test modu açıkken sayaç işlemez. Ayrıntı dosyası:\n");
        b.append("Android/data/com.cengiz.canlitv/files/reklam-sayaci.csv");
        return b.toString();
    }

    public static void reset(Context c) {
        sp(c).edit().clear().apply();
    }
}
