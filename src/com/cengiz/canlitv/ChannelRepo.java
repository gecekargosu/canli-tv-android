package com.cengiz.canlitv;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aktif ulkenin kanal listesi. Ulke degisince liste bastan yuklenir.
 * Liste kaynagi: tr icin gomulu assets/channels.m3u, digerleri icin CountryRepo (indirilip saklanir).
 */
public class ChannelRepo {

    /**
     * #42: yedek kaynak (mirror). Bir kanalın asıl adresi açılmazsa sırayla buradakiler denenir.
     * Anahtar: kanalın asıl URL'si. Değer: sırayla denenecek yedek URL'ler.
     */
    private static final Map<String, String[]> FALLBACK = new HashMap<String, String[]>();
    static {
        // ATV: cihazda açıldığı doğrulanan 720p adres birincil; master ve 480p yedek.
        // (Master varyant listesi bazı cihazlarda hazırlanmıyor — bu yüzden ana adres 720p.)
        String atv720 = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv_720p.m3u8";
        String atvMain = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv.m3u8";
        String atv480 = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv_480p.m3u8";
        FALLBACK.put(atv720, new String[]{atv480, atvMain});
        FALLBACK.put(atv480, new String[]{atv720, atvMain});
        FALLBACK.put(atvMain, new String[]{atv720, atv480});

        // TV8: daioncdn adresi başka bir uygulamanın `app=` anahtarını taşıyordu; listeden de
        // yedeklerden de kaldırıldı. Yayın TürkNet/ERC yansısından, yedek olarak 480p yansısı.
        mirror("https://rkhubpaomb.turknet.ercdn.net/fwjkgpasof/tv8/tv8_720p.m3u8",
                "https://rkhubpaomb.turknet.ercdn.net/fwjkgpasof/tv8/tv8_480p.m3u8");
        // Büyük ulusal kanallar için TürkNet/ERC yansıları (#103).
        mirror("https://rmtftbjlne.turknet.ercdn.net/bpeytmnqyp/showtv/showtv.m3u8",
                "https://rmtftbjlne.turknet.ercdn.net/bpeytmnqyp/showtv/showtv_720p.m3u8");
        mirror("https://ackaxsqacw.turknet.ercdn.net/ozfkfbbjba/kanald/kanald.m3u8",
                "https://ackaxsqacw.turknet.ercdn.net/ozfkfbbjba/kanald/kanald_720p.m3u8");
        mirror("https://uycyyuuzyh.turknet.ercdn.net/nphindgytw/nowtv/nowtv.m3u8",
                "https://uycyyuuzyh.turknet.ercdn.net/nphindgytw/nowtv/nowtv_720p.m3u8");
    }

    /** Aynı kanalın başka yansısını yedek olarak ekler. */
    private static void mirror(String a, String b) {
        FALLBACK.put(a, new String[]{b});
        FALLBACK.put(b, new String[]{a});
    }

    /**
     * PDF-07: kayıt yalnız kamu yayıncılarında açık. Özel kanalların yayını telifli;
     * kaydedilen .ts dosyası galeriden paylaşılabildiği için yayın öncesi kapatıldı.
     */
    public static boolean canRecord(String name) {
        if (name == null) return false;
        String n = name.toUpperCase(java.util.Locale.ROOT);
        return n.startsWith("TRT") || n.contains("TBMM") || n.contains("DIYANET");
    }

    /** Yedek URL listesi (yoksa null). */
    public static String[] fallbacks(String url) {
        return url == null ? null : FALLBACK.get(url);
    }

    private static final List<Channel> CHANNELS = new ArrayList<Channel>();
    private static String loadedCountry = "";

    /**
     * 9.75: Türkiye'de büyük ulusal kanallar her zaman listenin başında durur.
     * Doğrulanmış liste (Good) sırası değişse de bu beş kanal 1-5 numaraya alınır;
     * ayrıca "ölü kanal" sayacı bunları gizleyemez (kullanıcı TV8'i listede bulamıyordu).
     */
    private static final String[] PINNED_TR = {
            "ATV", "TV8", "SHOW TV", "STAR TV", "KANAL D"
    };

    static boolean pinned(String name) {
        if (name == null) return false;
        String n = name.trim().toUpperCase(java.util.Locale.ROOT);
        for (String p : PINNED_TR) if (n.startsWith(p)) return true;
        return false;
    }

    /** Sabit kanalları istenen sırayla listenin başına taşır. */
    private static void pinTop() {
        List<Channel> front = new ArrayList<Channel>();
        for (String p : PINNED_TR) {
            for (Channel c : CHANNELS) {
                if (!front.contains(c) && c.name != null
                        && c.name.trim().toUpperCase(java.util.Locale.ROOT).startsWith(p)) {
                    front.add(c);
                    break;
                }
            }
        }
        if (front.isEmpty()) return;
        CHANNELS.removeAll(front);
        CHANNELS.addAll(0, front);
    }

    public static String loadedCountry() {
        return loadedCountry;
    }

    /**
     * M-33: liste canlı tutuluyor; arka plan işleri (onarım, tarama, radyo servisi)
     * dolaşırken ülke değişirse liste boşalıp yeniden doluyor ve
     * ConcurrentModificationException / yanlış indeks çıkıyordu. Arka plan işleri
     * {@link #snapshot(Context)} ile kopya üzerinde çalışır.
     */
    public static List<Channel> snapshot(Context ctx) {
        return new ArrayList<Channel>(all(ctx));
    }

    public static synchronized List<Channel> all(Context ctx) {
        String cc = Prefs.country(ctx);
        if (!cc.equals(loadedCountry)) {
            CHANNELS.clear();
            CHANNELS.addAll(CountryRepo.load(ctx, cc));
            // once gercekten acilan kanallar (sunucudan gelen dogrulanmis liste), sonra populerlik sirasi
            if (!Good.apply(ctx, cc, CHANNELS)) Popular.arrange(cc, CHANNELS);
            if ("tr".equals(cc)) pinTop();
            // #102: kullanicinin kendi ekledigi kanallar listenin en basinda durur
            List<Channel> mine = new ArrayList<Channel>();
            for (String[] r : Prefs.customTv(ctx)) {
                Channel c = new Channel(0, r[0], "⭐ Kendi kanallarım", cc, "", r[1]);
                if (!hasUrl(CHANNELS, c.url)) mine.add(c);
            }
            if (!mine.isEmpty()) CHANNELS.addAll(0, mine);
            for (int i = 0; i < CHANNELS.size(); i++) CHANNELS.get(i).baseNo = i + 1;
            loadedCountry = cc;
        }
        applyNumbers(ctx);
        return CHANNELS;
    }

    /** Ulke veya dil degisince listeyi sifirdan yukler. */
    public static synchronized void reload(Context ctx) {
        loadedCountry = "";
        all(ctx);
    }

    /** Kullanicinin degistirdigi kumanda numaralarini uygular. */
    public static synchronized void applyNumbers(Context ctx) {
        for (Channel c : CHANNELS) {
            int ov = Prefs.noOverride(ctx, c.url);
            c.no = ov > 0 ? ov : c.baseNo;
        }
    }

    public static Channel byUrl(Context ctx, String url) {
        for (Channel c : all(ctx)) if (c.url.equals(url)) return c;
        return null;
    }

    public static List<String> groups(Context ctx) {
        List<String> out = new ArrayList<String>();
        for (Channel c : all(ctx)) if (!out.contains(c.group)) out.add(c.group);
        java.util.Collections.sort(out);
        return out;
    }

    public static Channel byNumber(Context ctx, int no) {
        for (Channel c : all(ctx)) if (c.no == no) return c;
        return null;
    }

    private static boolean hasUrl(List<Channel> l, String url) {
        for (Channel c : l) if (c.url.equals(url)) return true;
        return false;
    }

    /** Kanal listesinde URL'yi arar. */
    public static synchronized int indexOfUrl(String url) {
        for (int i = 0; i < CHANNELS.size(); i++) if (CHANNELS.get(i).url.equals(url)) return i;
        return -1;   // HATA (tur 1): 0 dönünce bulunamayan kanal 1. kanalı açıyordu
    }
}
