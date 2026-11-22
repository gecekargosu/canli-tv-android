package com.cengiz.canlitv;

import android.content.Context;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Master m3u8 icindeki cozunurluk varyantlarini (360p/720p/1080p) listeler ve
 * kullanicinin veri tasarrufu tercihine gore dogru varyanti secer.
 */
public class Quality {

    public static class Variant {
        public final String label;
        public final String url;
        public final int height;
        Variant(String label, String url, int height) {
            this.label = label;
            this.url = url;
            this.height = height;
        }
        @Override public String toString() { return label; }
    }

    public interface Callback { void onResult(List<Variant> variants); }
    public interface UrlCallback { void onUrl(String url); }

    /** Kaba tahmin: saat basina MB. Kullaniciya "ne kadar yer" fikri vermek icin. */
    public static int hourlyMb(int height) {
        if (height <= 0) return 0;
        if (height <= 240) return 150;
        if (height <= 360) return 300;
        if (height <= 480) return 550;
        if (height <= 576) return 700;
        if (height <= 720) return 1200;
        return 2200;
    }

    public static void load(final String playlistUrl, final Callback cb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Variant> out = new ArrayList<Variant>();
                try {
                    String txt = fetch(playlistUrl);
                    if (txt != null && txt.contains("#EXT-X-STREAM-INF")) {
                        String base = playlistUrl.substring(0, playlistUrl.lastIndexOf('/') + 1);
                        String[] lines = txt.split("\n");
                        for (int i = 0; i < lines.length; i++) {
                            if (!lines[i].startsWith("#EXT-X-STREAM-INF")) continue;
                            String res = "";
                            int height = 0;
                            java.util.regex.Matcher m = java.util.regex.Pattern
                                    .compile("RESOLUTION=(\\d+)x(\\d+)").matcher(lines[i]);
                            if (m.find()) {
                                height = Integer.parseInt(m.group(2));
                                res = height + "p";
                            }
                            for (int j = i + 1; j < lines.length; j++) {
                                String c = lines[j].trim();
                                if (c.length() == 0 || c.startsWith("#")) continue;
                                out.add(new Variant(
                                        res.length() == 0 ? ("varyant " + (out.size() + 1)) : res,
                                        c.startsWith("http") ? c : base + c,
                                        height));
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) { }
                cb.onResult(out);
            }
        }, "quality").start();
    }

    /**
     * Kanal icin oynatilacak URL'i sec: kanala ozel secim > genel kalite tercihi > orijinal.
     * Callback arka planda gelebilir; UI'a runOnUiThread ile don.
     */
    public static void resolve(final Context ctx, final String url, final UrlCallback cb) {
        if (cb == null) return;
        String custom = Prefs.quality(ctx, url);
        if (custom != null) { cb.onUrl(custom); return; }
        final int pref = Prefs.maxHeight(ctx);
        if (pref == 0) { cb.onUrl(url); return; }
        load(url, new Callback() {
            @Override public void onResult(List<Variant> variants) {
                cb.onUrl(variants.isEmpty() ? url : pick(variants, pref));
            }
        });
    }

    /** Her zaman en dusuk cozunurluk: mobil veride "sadece ses" icin. */
    public static void resolveLowest(final Context ctx, final String url, final UrlCallback cb) {
        if (cb == null) return;
        String custom = Prefs.quality(ctx, url);
        if (custom != null) { cb.onUrl(custom); return; }
        load(url, new Callback() {
            @Override public void onResult(List<Variant> variants) {
                cb.onUrl(variants.isEmpty() ? url : lowest(variants));
            }
        });
    }

    /** Hedef yukseklige en yakin (tercihen ustunu) varyant. */
    public static String pick(List<Variant> list, int pref) {
        if (list == null || list.isEmpty()) return null;
        if (pref < 0) return lowest(list);
        Variant best = null;
        for (Variant v : list) {
            if (v.height <= 0) continue;
            if (v.height >= pref && (best == null || v.height < best.height)) best = v;
        }
        if (best == null) {
            for (Variant v : list) {
                if (v.height <= 0) continue;
                if (best == null || v.height > best.height) best = v;
            }
        }
        return best == null ? list.get(0).url : best.url;
    }

    /** En dusuk cozunurluklu varyant (veri tasarrufu icin). */
    public static String lowest(List<Variant> list) {
        if (list == null || list.isEmpty()) return null;
        Variant best = null;
        for (Variant v : list) {
            if (v.height <= 0) continue;
            if (best == null || v.height < best.height) best = v;
        }
        if (best != null) return best.url;
        return list.get(list.size() - 1).url;
    }

    private static String fetch(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(7000); c.setReadTimeout(7000);
            c.setRequestProperty("User-Agent", "Mozilla/5.0");
            InputStream in = c.getInputStream();
            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close();
            return new String(bo.toByteArray(), "UTF-8");
        } catch (Exception e) { return null; }
    }
}
