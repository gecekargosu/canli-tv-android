package com.cengiz.canlitv;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Tum kucuk tercihleri tek yerde toplar: favoriler, son izlenenler, olu kanal sayaci, setler. */
public class Prefs {

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("canlitv", Context.MODE_PRIVATE);
    }

    // --- favoriler ---
    public static Set<String> favs(Context c) {
        return new HashSet<String>(sp(c).getStringSet("favs", new HashSet<String>()));
    }

    public static void toggleFav(Context c, String url) {
        Set<String> f = favs(c);
        if (f.contains(url)) f.remove(url); else f.add(url);
        sp(c).edit().putStringSet("favs", f).apply();
    }

    // --- son izlenenler ---
    public static List<String> recent(Context c) {
        String s = sp(c).getString("recent", "");
        List<String> out = new ArrayList<String>();
        for (String u : s.split("\n")) if (u.trim().length() > 0) out.add(u.trim());
        return out;
    }

    public static void addRecent(Context c, String url) {
        List<String> r = recent(c);
        r.remove(url);
        r.add(0, url);
        while (r.size() > 20) r.remove(r.size() - 1);
        StringBuilder b = new StringBuilder();
        for (String u : r) b.append(u).append("\n");
        sp(c).edit().putString("recent", b.toString()).apply();
    }

    // --- olu kanal sayaci ---
    /** Ölü sayacı bu süreden sonra geçersiz sayılır (9.88 / K-07: 24 saat). */
    private static final long FAIL_TTL_MS = 24L * 60 * 60 * 1000;

    /**
     * 9.76: sayaç hiç sıfırlanmadığı için aylar içinde iki kez tökezleyen kanal
     * kalıcı olarak gizleniyordu (cihazda liste 128'den 32'ye düşmüştü).
     * Artık yalnız son 24 saat içindeki hatalar sayılır; eski kayıtlar yok sayılır.
     */
    public static int fails(Context c, String url) {
        SharedPreferences p = sp(c);
        long at = p.getLong("failAt:" + url.hashCode(), 0L);
        if (at <= 0 || System.currentTimeMillis() - at > FAIL_TTL_MS) return 0;
        return p.getInt("fail:" + url.hashCode(), 0);
    }

    public static void addFail(Context c, String url) {
        SharedPreferences.Editor e = sp(c).edit();
        e.putInt("fail:" + url.hashCode(), fails(c, url) + 1);
        e.putLong("failAt:" + url.hashCode(), System.currentTimeMillis());
        e.apply();
    }

    public static void clearFails(Context c, String url) {
        sp(c).edit().remove("fail:" + url.hashCode())
                .remove("failAt:" + url.hashCode()).apply();
    }

    /**
     * 9.76: "ölü kanal" sayaçları hiç sıfırlanmıyordu; aylar içinde iki kez
     * tökezleyen kanal kalıcı olarak gizleniyor ve liste 128'den 32'ye düşüyordu.
     * Doğrulanmış liste her tazelendiğinde (günlük) sayaçlar sıfırlanır.
     */
    public static void clearAllFails(Context c) {
        SharedPreferences p = sp(c);
        SharedPreferences.Editor e = p.edit();
        boolean any = false;
        for (String k : p.getAll().keySet()) {
            if (k.startsWith("fail:") || k.startsWith("failAt:")) { e.remove(k); any = true; }
        }
        if (any) e.apply();
    }

    // --- AdMob test cihazı (kendi telefonunda test reklamı görmek için) ---
    public static boolean adTestMode(Context c) {
        return sp(c).getBoolean("adTestMode", false);
    }

    public static void setAdTestMode(Context c, boolean v) {
        sp(c).edit().putBoolean("adTestMode", v).apply();
    }

    public static String adTestHashed(Context c) {
        return sp(c).getString("adTestHashed", null);
    }

    public static void setAdTestHashed(Context c, String v) {
        sp(c).edit().putString("adTestHashed", v).apply();
    }

    // --- geliştirici (sahip) modu: yayınlanan APK'da kullanıcıya görünmez ---
    /** Gizli sahip modu (Ayarlar → Sürüm satırına 7 kez dokun). */
    public static boolean ownerMode(Context c) {
        return sp(c).getBoolean("ownerMode", false);
    }

    public static void setOwnerMode(Context c, boolean v) {
        sp(c).edit().putBoolean("ownerMode", v).apply();
    }

    /** Sahip modunda "reklamları kapat": bu telefonda hiç reklam gösterilmez. */
    public static boolean adsOff(Context c) {
        return sp(c).getBoolean("adsOff", false);
    }

    public static void setAdsOff(Context c, boolean v) {
        sp(c).edit().putBoolean("adsOff", v).apply();
    }

    // --- reklamsız abonelik (Google Play aboneliği) ---
    /** Bu cihazda reklamsız abonelik etkin mi? */
    public static boolean subActive(Context c) {
        return sp(c).getBoolean("subActive", false);
    }

    public static void setSubActive(Context c, boolean v) {
        sp(c).edit().putBoolean("subActive", v).apply();
    }

    /** Abonelik durumu en son ne zaman Play'den okundu. */
    public static long subChecked(Context c) {
        return sp(c).getLong("subChecked", 0L);
    }

    public static void setSubChecked(Context c, long v) {
        sp(c).edit().putLong("subChecked", v).apply();
    }

    /** Hata günlüğünü günde bir kez sunucuya gönder (varsayılan KAPALI, kullanıcı açar). */
    public static boolean autoLogSend(Context c) {
        return sp(c).getBoolean("autoLogSend", false);
    }

    public static void setAutoLogSend(Context c, boolean v) {
        sp(c).edit().putBoolean("autoLogSend", v).apply();
    }

    public static long lastLogUpload(Context c) {
        return sp(c).getLong("lastLogUpload", 0L);
    }

    public static void setLastLogUpload(Context c, long v) {
        sp(c).edit().putLong("lastLogUpload", v).apply();
    }

    public static boolean hideDead(Context c) {
        return sp(c).getBoolean("hideDead", true);
    }

    public static void setHideDead(Context c, boolean v) {
        sp(c).edit().putBoolean("hideDead", v).apply();
    }

    // --- kalite secimi (kanal url -> variant url) ---
    public static String quality(Context c, String url) {
        return sp(c).getString("q:" + url.hashCode(), null);
    }

    public static void setQuality(Context c, String url, String variant) {
        if (variant == null) sp(c).edit().remove("q:" + url.hashCode()).apply();
        else sp(c).edit().putString("q:" + url.hashCode(), variant).apply();
    }

    // --- coklu ekran ---
    public static String multiLast(Context c) {
        return sp(c).getString("multiLast", "");
    }

    public static void setMultiLast(Context c, String v) {
        sp(c).edit().putString("multiLast", v).apply();
    }

    public static Set<String> multiSets(Context c) {
        return new LinkedHashSet<String>(sp(c).getStringSet("multiSets", new HashSet<String>()));
    }

    public static void saveMultiSet(Context c, String encoded) {
        Set<String> s = multiSets(c);
        s.add(encoded);
        sp(c).edit().putStringSet("multiSets", s).apply();
    }

    public static void removeMultiSet(Context c, String encoded) {
        Set<String> s = multiSets(c);
        s.remove(encoded);
        sp(c).edit().putStringSet("multiSets", s).apply();
    }

    public static void setLastIndex(Context c, int i) {
        sp(c).edit().putInt("lastIndex", i).apply();
    }

    public static int lastIndex(Context c) {
        return sp(c).getInt("lastIndex", 0);
    }

    // --- listeden gizlenen kanallar / radyolar ---
    public static Set<String> hiddenChannels(Context c) {
        return new HashSet<String>(sp(c).getStringSet("hiddenCh", new HashSet<String>()));
    }

    public static void hideChannel(Context c, String url) {
        Set<String> s = hiddenChannels(c);
        s.add(url);
        sp(c).edit().putStringSet("hiddenCh", s).apply();
    }

    public static void unhideChannel(Context c, String url) {
        Set<String> s = hiddenChannels(c);
        s.remove(url);
        sp(c).edit().putStringSet("hiddenCh", s).apply();
    }

    public static Set<String> hiddenRadios(Context c) {
        return new HashSet<String>(sp(c).getStringSet("hiddenRd", new HashSet<String>()));
    }

    public static void hideRadio(Context c, String url) {
        Set<String> s = hiddenRadios(c);
        s.add(url);
        sp(c).edit().putStringSet("hiddenRd", s).apply();
    }

    public static void unhideRadio(Context c, String url) {
        Set<String> s = hiddenRadios(c);
        s.remove(url);
        sp(c).edit().putStringSet("hiddenRd", s).apply();
    }

    // --- kullanicinin internetten arayip ekledigi radyolar ("ad" + \u0001 + "url") ---
    public static List<String[]> customRadios(Context c) {
        List<String[]> out = new ArrayList<String[]>();
        String all = sp(c).getString("customRd", "");
        if (all.length() == 0) return out;
        for (String row : all.split("\n")) {
            int i = row.indexOf('\u0001');
            if (i <= 0 || i + 1 >= row.length()) continue;
            out.add(new String[]{row.substring(0, i), row.substring(i + 1)});
        }
        return out;
    }

    public static boolean hasCustomRadio(Context c, String url) {
        for (String[] r : customRadios(c)) if (r[1].equals(url)) return true;
        return false;
    }

    public static void addCustomRadio(Context c, String name, String url) {
        if (url == null || url.length() == 0 || name == null) return;
        if (hasCustomRadio(c, url)) return;
        String all = sp(c).getString("customRd", "");
        String row = name.replace("\n", " ").replace("\u0001", " ").trim() + "\u0001" + url.trim();
        sp(c).edit().putString("customRd", all.length() == 0 ? row : row + "\n" + all).apply();
    }

    public static void removeCustomRadio(Context c, String url) {
        List<String[]> cur = customRadios(c);
        StringBuilder sb = new StringBuilder();
        for (String[] r : cur) {
            if (r[1].equals(url)) continue;
            if (sb.length() > 0) sb.append("\n");
            sb.append(r[0]).append('\u0001').append(r[1]);
        }
        sp(c).edit().putString("customRd", sb.toString()).apply();
    }

    // --- internet kullanimi ---
    private static String today() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date());
    }

    /**
     * Kanal basina toplam kullanim (bayt).
     * M-65: anahtar artik tam adres. Eskiden `url.hashCode()` kullaniliyordu; iki farkli
     * adres ayni hash'e dusunce kanallarin verisi birbirine karisiyordu. Eski kayit ilk
     * okumada yeni anahtara tasinir (kullanici verisi kaybolmaz).
     */
    public static long usage(Context c, String url) {
        if (url == null) return 0;
        long v = sp(c).getLong("use:" + url, -1L);
        if (v >= 0) return v;
        long old = sp(c).getLong("use:" + url.hashCode(), -1L);
        if (old > 0) sp(c).edit().putLong("use:" + url, old).apply();
        return Math.max(0, old);
    }

    public static int secs(Context c, String url) {
        if (url == null) return 0;
        int v = sp(c).getInt("useS:" + url, -1);
        if (v >= 0) return v;
        int old = sp(c).getInt("useS:" + url.hashCode(), -1);
        if (old > 0) sp(c).edit().putInt("useS:" + url, old).apply();
        return Math.max(0, old);
    }

    /** Gunluk toplam (bayt); gun degisince sifirlanir. */
    public static long todayBytes(Context c) {
        if (!today().equals(sp(c).getString("day", ""))) return 0;
        return sp(c).getLong("dayBytes", 0);
    }

    public static void addUsage(Context c, String url, long bytes, int seconds) {
        SharedPreferences.Editor e = sp(c).edit();
        e.putLong("use:" + url, usage(c, url) + bytes);
        e.putInt("useS:" + url, secs(c, url) + seconds);
        long t = today().equals(sp(c).getString("day", "")) ? sp(c).getLong("dayBytes", 0) : 0;
        e.putString("day", today());
        e.putLong("dayBytes", t + bytes);
        e.apply();
    }

    /** Bu ülke için sunucuda liste yok (404) — tekrar tekrar denemeyelim. */
    public static void markCountryMiss(Context c, String code) {
        java.util.Set<String> s = new java.util.HashSet<String>(missSet(c));
        s.add(code);
        sp(c).edit().putStringSet("countryMiss", s).apply();
    }

    public static boolean isCountryMiss(Context c, String code) {
        return missSet(c).contains(code);
    }

    /** M-85: liste sunucuya eklendiğinde eski "yakında" işareti kalksın. */
    public static void clearCountryMiss(Context c, String code) {
        java.util.Set<String> s = new java.util.HashSet<String>(missSet(c));
        if (s.remove(code)) sp(c).edit().putStringSet("countryMiss", s).apply();
    }

    private static java.util.Set<String> missSet(Context c) {
        try {
            java.util.Set<String> s = sp(c).getStringSet("countryMiss", null);
            return s == null ? new java.util.HashSet<String>() : s;
        } catch (Throwable t) {
            return new java.util.HashSet<String>();
        }
    }

    public static int limitMb(Context c) {
        return sp(c).getInt("limitMb", 0);
    }

    /** Kaç kez kanal açıldı (ilk açılışta reklam göstermemek için). */
    public static int channelOpens(Context c) {
        return sp(c).getInt("chanOpens", 0);
    }

    public static void setChannelOpens(Context c, int n) {
        sp(c).edit().putInt("chanOpens", n).apply();
    }

    public static void setLimitMb(Context c, int mb) {
        sp(c).edit().putInt("limitMb", mb).apply();
    }

    public static void resetToday(Context c) {
        sp(c).edit().putString("day", today()).putLong("dayBytes", 0).apply();
    }

    public static void resetAllUsage(Context c) {
        SharedPreferences.Editor e = sp(c).edit();
        for (String k : sp(c).getAll().keySet()) {
            if (k.startsWith("use:") || k.startsWith("useS:")) e.remove(k);
        }
        e.putString("day", today()).putLong("dayBytes", 0).apply();
    }

    /** Mobil veride onay: son onay zamani (1 saat gecerli). */
    public static boolean mobileOk(Context c) {
        return System.currentTimeMillis() - sp(c).getLong("mobileOk", 0) < 3600000L;
    }

    public static void setMobileOk(Context c, boolean v) {
        sp(c).edit().putLong("mobileOk", v ? System.currentTimeMillis() : 0).apply();
    }

    // --- kalite tercihi ---
    // 0 = otomatik (kanalin en iyi kalitesi), -1 = en dusuk, 360/480/720/1080 = hedef yukseklik
    /** Varsayılan: 360p (en düşük makul kalite = en az veri). Kullanıcı yükseltirse saklanır. */
    public static int maxHeight(Context c) {
        return sp(c).getInt("maxHeight", 360);
    }

    public static void setMaxHeight(Context c, int h) {
        sp(c).edit().putInt("maxHeight", h).apply();
    }

    /** Veri tasarrufu: yayinlarin en dusuk cozunurlugunu kullan. */
    /** Ekonomi modu: her zaman en düşük kalite + kapakta sadece ses (veri tasarrufu). */
    public static boolean economy(Context c) {
        return sp(c).getBoolean("economy", false);
    }

    public static void setEconomy(Context c, boolean v) {
        sp(c).edit().putBoolean("economy", v).apply();
    }

    public static boolean dataSaver(Context c) {
        return maxHeight(c) < 0;
    }

    public static void setDataSaver(Context c, boolean v) {
        // MP-14: kapatınca "otomatik" (kanalın en iyisi) seçilip veri harcaması
        // artıyordu; 360p'ye dön ki tasarruf düğmesi ile kalite aynı şeyi söylesin.
        setMaxHeight(c, v ? -1 : 360);
    }

    // --- liste sirasi (sekme basina: "Tümü", "★ Favoriler", grup adi...) ---
    public static List<String> order(Context c, String tab) {
        String s = sp(c).getString("ord:" + tab, "");
        List<String> out = new ArrayList<String>();
        for (String u : s.split("\n")) if (u.trim().length() > 0) out.add(u.trim());
        return out;
    }

    public static void setOrder(Context c, String tab, List<String> urls) {
        StringBuilder b = new StringBuilder();
        for (String u : urls) b.append(u).append('\n');
        sp(c).edit().putString("ord:" + tab, b.toString()).apply();
    }

    // --- kumanda numarasi degistirme ---
    /**
     * 9.75: eski sürümlerde kaydedilmiş numara/el sırası kayıtları ("no:" / "ord:")
     * kanal listesi yenilenince çakışıyordu (iki kanal №1, boş №1). Bir kerelik temizlenir;
     * numaralar yeniden 1'den başlar, sabit kanallar ilk beşte durur.
     */
    public static void migrateNumbers(Context c) {
        SharedPreferences p = sp(c);
        if (p.getInt("numbersRev", 0) >= 3) return;
        SharedPreferences.Editor e = p.edit();
        for (String k : p.getAll().keySet()) {
            if (k.startsWith("no:") || k.startsWith("ord:")
                    || k.startsWith("fail:") || k.startsWith("failAt:")) e.remove(k);
        }
        // 9.76: eski sürümlerden kalan toplu "listeden sil" kaydı (cihazda 115 kanal)
        // listeyi 128'den 32'ye düşürüyordu. Elle silinen birkaç kanal korunur;
        // 20'den fazlası kaza/toplu kayıt sayılır ve temizlenir.
        java.util.Set<String> hid = p.getStringSet("hiddenCh", null);
        if (hid != null && hid.size() > 20) e.remove("hiddenCh");
        e.putInt("numbersRev", 3).apply();
    }

    public static int noOverride(Context c, String url) {
        return sp(c).getInt("no:" + url.hashCode(), 0);
    }

    public static void setNoOverride(Context c, String url, int no) {
        if (no <= 0) sp(c).edit().remove("no:" + url.hashCode()).apply();
        else sp(c).edit().putInt("no:" + url.hashCode(), no).apply();
    }

    public static void clearNumbers(Context c) {
        SharedPreferences.Editor e = sp(c).edit();
        for (String k : sp(c).getAll().keySet()) if (k.startsWith("no:")) e.remove(k);
        e.apply();
    }

    // --- ulke ve arayuz dili ---
    /** Aktif ulkenin kanal listesi gosterilir (varsayilan tr). */
    public static String country(Context c) {
        return sp(c).getString("country", "tr");
    }

    public static void setCountry(Context c, String code) {
        sp(c).edit().putString("country", code == null || code.length() != 2 ? "tr" : code).apply();
    }

    /** Arayuz dili: tr / en / ru / ar. */
    public static String uiLang(Context c) {
        return sp(c).getString("uiLang", "tr");
    }

    public static void setUiLang(Context c, String code) {
        sp(c).edit().putString("uiLang", code).apply();
    }

    /** Veri tasarrufu: mobil veride sadece ses cal. */
    public static boolean audioOnlyMobile(Context c) {
        return sp(c).getBoolean("audioOnlyMobile", false);
    }

    public static void setAudioOnlyMobile(Context c, boolean v) {
        sp(c).edit().putBoolean("audioOnlyMobile", v).apply();
    }

    // --- reklamsiz sure (odullu reklam ile kazanilir) ---
    /** Reklamsiz surenin bitis zamani (ms). Gecmisse reklam gosterilir. */
    public static long adFreeUntil(Context c) {
        return sp(c).getLong("adFreeUntil", 0L);
    }

    public static void setAdFreeUntil(Context c, long millis) {
        sp(c).edit().putLong("adFreeUntil", millis).apply();
    }

    /** Reklamsız sürenin geçerli olduğu kanal/istasyon (URL). Boşsa tüm uygulama. */
    public static String adFreeKey(Context c) {
        return sp(c).getString("adFreeKey", "");
    }

    /**
     * 20 dakikalık reklamsız süre **tek kanal için** geçerlidir: kanal/istasyon değişince
     * yeniden ödüllü video gerekir.
     */
    public static void setAdFreeUntil(Context c, long millis, String key) {
        sp(c).edit().putLong("adFreeUntil", millis)
                .putString("adFreeKey", key == null ? "" : key).apply();
    }

    /** Son gösterilen geçiş reklamı (15 dakikalık sayaç için). */
    public static long lastAdAt(Context c) {
        return sp(c).getLong("lastAdAt", 0L);
    }

    public static void setLastAdAt(Context c, long millis) {
        sp(c).edit().putLong("lastAdAt", millis).apply();
    }

    /** "Bilinen iyi kanallar" listesinin son tazelenme günü (ülke başına). */
    public static String goodDay(Context c, String cc) {
        return sp(c).getString("goodDay_" + cc, "");
    }

    public static void setGoodDay(Context c, String cc, String day) {
        sp(c).edit().putString("goodDay_" + cc, day).apply();
    }

    /** Uyku zamanlayıcısı (epoch ms; 0 = kapalı). Ekran yeniden kurulsa da kaybolmaz. */
    public static long sleepUntil(Context c) {
        return sp(c).getLong("sleepUntil", 0L);
    }

    public static void setSleepUntil(Context c, long ms) {
        sp(c).edit().putLong("sleepUntil", ms).apply();
    }

    /** Kullanıcının kendi eklediği TV kanalları: "ad\u0001url" satırları. */
    public static List<String[]> customTv(Context c) {
        List<String[]> out = new ArrayList<String[]>();
        String all = sp(c).getString("customTv", "");
        if (all.length() == 0) return out;
        for (String row : all.split("\n")) {
            int i = row.indexOf('\u0001');
            if (i <= 0 || i + 1 >= row.length()) continue;
            out.add(new String[]{row.substring(0, i), row.substring(i + 1)});
        }
        return out;
    }

    public static void addCustomTv(Context c, String name, String url) {
        if (url == null || url.length() < 6 || name == null) return;
        String clean = name.replace("\n", " ").replace("\u0001", " ").trim();
        if (clean.length() == 0) clean = "Kendi kanalım";
        String all = sp(c).getString("customTv", "");
        for (String[] r : customTv(c)) if (r[1].equals(url.trim())) return;
        String row = clean + "\u0001" + url.trim();
        sp(c).edit().putString("customTv", all.length() == 0 ? row : row + "\n" + all).apply();
    }

    public static void removeCustomTv(Context c, String url) {
        StringBuilder sb = new StringBuilder();
        for (String[] r : customTv(c)) {
            if (r[1].equals(url)) continue;
            if (sb.length() > 0) sb.append("\n");
            sb.append(r[0]).append('\u0001').append(r[1]);
        }
        sp(c).edit().putString("customTv", sb.toString()).apply();
    }

    /** IP'den ulke onerisi bir kez soruldu mu? */
    public static boolean countryAsked(Context c) {
        return sp(c).getBoolean("countryAsked", false);
    }

    public static void setCountryAsked(Context c, boolean v) {
        sp(c).edit().putBoolean("countryAsked", v).apply();
    }

    /** Her ekrandaki veri gostergesi acik mi? (Ayarlardan kapatilabilir) */
    public static boolean showUsage(Context c) {
        return sp(c).getBoolean("showUsage", true);
    }

    public static void setShowUsage(Context c, boolean v) {
        sp(c).edit().putBoolean("showUsage", v).apply();
    }
}
