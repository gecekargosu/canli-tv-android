package com.cengiz.canlitv;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ulke bazli kanal listesi.
 * - tr: uygulamanin icine gomulu, elden gecirilmis liste (assets/channels.m3u)
 * - digerleri: iptv-org ulke listesi indirilir ve cihazda saklanir (files/country_xx.m3u)
 */
public class CountryRepo {

    public static String playlistUrl(String cc) {
        return "https://iptv-org.github.io/iptv/countries/" + cc + ".m3u";
    }

    public static File cacheFile(Context ctx, String cc) {
        return new File(ctx.getFilesDir(), "country_" + cc + ".m3u");
    }

    /** Liste cihazda var mi (TR her zaman gomulu). */
    public static boolean isReady(Context ctx, String cc) {
        if ("tr".equals(cc)) return true;
        return cacheFile(ctx, cc).length() > 200;
    }

    public static List<Channel> load(Context ctx, String cc) {
        List<Channel> out = new ArrayList<Channel>();
        try {
            InputStream in;
            if ("tr".equals(cc)) {
                in = ctx.getAssets().open("channels.m3u");
            } else {
                File f = cacheFile(ctx, cc);
                if (!f.exists()) return out;
                in = new FileInputStream(f);
            }
            out = parse(new BufferedReader(new InputStreamReader(in, "UTF-8")));
            in.close();
        } catch (Exception ignored) { }
        return out;
    }

    /** m3u icerigini kanal listesine cevirir; numaralar sirayla verilir. */
    static List<Channel> parse(BufferedReader r) throws IOException {
        List<Channel> out = new ArrayList<Channel>();
        Set<String> seen = new HashSet<String>();
        String line, name = null, group = "Genel", country = "", logo = "";
        while ((line = r.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) continue;
            if (line.startsWith("#EXTINF")) {
                int comma = line.lastIndexOf(',');
                name = comma >= 0 ? line.substring(comma + 1).trim() : "Kanal";
                group = attr(line, "group-title", "Genel");
                if (group.trim().length() == 0) group = "Genel";
                country = attr(line, "tvg-country", "");
                logo = attr(line, "tvg-logo", "");
            } else if (!line.startsWith("#")) {
                if (name != null && !seen.contains(line)) {
                    seen.add(line);
                    out.add(new Channel(out.size() + 1, name, group, country, logo, line));
                }
                name = null;
            }
        }
        return out;
    }

    /** Ulke listesini indirir. Basarili olursa true. */
    public static boolean download(Context ctx, String cc) {
        File tmp = new File(ctx.getFilesDir(), "country_" + cc + ".tmp");
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(playlistUrl(cc)).openConnection();
            con.setConnectTimeout(12000);
            con.setReadTimeout(20000);
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (con.getResponseCode() != 200) return false;
            InputStream in = con.getInputStream();
            FileOutputStream out = new FileOutputStream(tmp);
            byte[] buf = new byte[16384];
            int n;
            long total = 0;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                total += n;
            }
            out.close();
            in.close();
            if (total < 200) { tmp.delete(); return false; }
            // icerik gercekten liste mi?
            List<Channel> check = parse(new BufferedReader(new InputStreamReader(
                    new FileInputStream(tmp), "UTF-8")));
            if (check.isEmpty()) { tmp.delete(); return false; }
            File target = cacheFile(ctx, cc);
            if (target.exists()) target.delete();
            boolean ok = tmp.renameTo(target);
            if (ok) fetchGood(ctx, cc);   // "calisan kanallar" listesi (varsa)
            return ok;
        } catch (Exception e) {
            tmp.delete();
            return false;
        } finally {
            if (con != null) try { con.disconnect(); } catch (Exception ignored) { }
        }
    }

    /** Sunucudaki doğrulanmış kanal listesini indirir (site/public/good/<cc>.json). */
    public static void fetchGood(Context ctx, String cc) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(
                    "https://umay-multi-tv-gq3zer.freebuff.site/good/" + cc + ".json").openConnection();
            con.setConnectTimeout(8000);
            con.setReadTimeout(8000);
            con.setRequestProperty("User-Agent", "UMAYMultiTV/9.11");
            if (con.getResponseCode() != 200) return;
            InputStream in = con.getInputStream();
            File tmp = new File(ctx.getFilesDir(), "good_" + cc + ".tmp");
            FileOutputStream out = new FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.close();
            in.close();
            File target = new File(ctx.getFilesDir(), "good_" + cc + ".json");
            if (target.exists()) target.delete();
            tmp.renameTo(target);
        } catch (Exception ignored) { }
        finally { if (con != null) try { con.disconnect(); } catch (Exception ignored) { } }
    }

    public static void clear(Context ctx, String cc) {
        cacheFile(ctx, cc).delete();
        new File(ctx.getFilesDir(), "good_" + cc + ".json").delete();
    }

    private static String attr(String line, String key, String def) {
        String k = key + "=\"";
        int i = line.indexOf(k);
        if (i < 0) return def;
        int start = i + k.length();
        int end = line.indexOf('"', start);
        if (end < 0) return def;
        return line.substring(start, end);
    }
}
