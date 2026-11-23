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
        String atv720 = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv_720p.m3u8";
        String atvMain = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv.m3u8";
        String atv1080 = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv_1080p.m3u8";
        FALLBACK.put(atv720, new String[]{atvMain, atv1080});
        FALLBACK.put(atvMain, new String[]{atv720, atv1080});
        FALLBACK.put(atv1080, new String[]{atv720, atvMain});

        // TV8: asıl yayın (daioncdn) reklam kuşağında kilitlenirse TürkNet/ERC yansısına geç.
        String tv8Main = "https://tv8.daioncdn.net/tv8/tv8.m3u8?app=7ddc255a-ef47-4e81-ab14-c0e5f2949788&ce=3";
        String tv8Mirror = "https://rkhubpaomb.turknet.ercdn.net/fwjkgpasof/tv8/tv8_720p.m3u8";
        String tv8Mirror2 = "https://rkhubpaomb.turknet.ercdn.net/fwjkgpasof/tv8/tv8_480p.m3u8";
        FALLBACK.put(tv8Main, new String[]{tv8Mirror, tv8Mirror2});
        FALLBACK.put(tv8Mirror, new String[]{tv8Mirror2, tv8Main});
        FALLBACK.put(tv8Mirror2, new String[]{tv8Mirror, tv8Main});

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

    /** Yedek URL listesi (yoksa null). */
    public static String[] fallbacks(String url) {
        return url == null ? null : FALLBACK.get(url);
    }

    private static final List<Channel> CHANNELS = new ArrayList<Channel>();
    private static String loadedCountry = "";

    public static String loadedCountry() {
        return loadedCountry;
    }

    public static List<Channel> all(Context ctx) {
        String cc = Prefs.country(ctx);
        if (!cc.equals(loadedCountry)) {
            CHANNELS.clear();
            CHANNELS.addAll(CountryRepo.load(ctx, cc));
            // once gercekten acilan kanallar (sunucudan gelen dogrulanmis liste), sonra populerlik sirasi
            if (!Good.apply(ctx, cc, CHANNELS)) Popular.arrange(cc, CHANNELS);
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
    public static void reload(Context ctx) {
        loadedCountry = "";
        all(ctx);
    }

    /** Kullanicinin degistirdigi kumanda numaralarini uygular. */
    public static void applyNumbers(Context ctx) {
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
    public static int indexOfUrl(String url) {
        for (int i = 0; i < CHANNELS.size(); i++) if (CHANNELS.get(i).url.equals(url)) return i;
        return 0;
    }
}
