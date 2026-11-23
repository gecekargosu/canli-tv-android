package com.cengiz.canlitv;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Aktif ulkenin kanal listesi. Ulke degisince liste bastan yuklenir.
 * Liste kaynagi: tr icin gomulu assets/channels.m3u, digerleri icin CountryRepo (indirilip saklanir).
 */
public class ChannelRepo {

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

    public static int indexOfUrl(String url) {
        for (int i = 0; i < CHANNELS.size(); i++) if (CHANNELS.get(i).url.equals(url)) return i;
        return 0;
    }
}
