package com.cengiz.canlitv;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Radyo listesi: secilen ulkeye gore.
 *
 * - "tr"  -> uygulamanin icindeki radios.m3u (internet gerekmez)
 * - diger -> radio-browser.info acik API'sinden o ulkenin en cok dinlenen istasyonlari
 *            indirilir, files/radio_<cc>.m3u olarak onbelleklenir (bir kez indirilir).
 *
 * Sunucu kapali/ag yoksa: onbellek varsa o, yoksa bos liste (uygulama cokmez).
 */
public final class RadioRepo {

    private static final String TAG = "UmayRadio";
    private static final String API = "https://de1.api.radio-browser.info/json/stations/search";

    public static final List<String> NAMES = new ArrayList<String>();
    public static final List<String> META = new ArrayList<String>();
    public static final List<String> URLS = new ArrayList<String>();

    private static String loadedCountry = null;
    public static boolean downloading = false;

    private RadioRepo() { }

    public static String country(Context c) {
        return Prefs.country(c);
    }

    private static File cacheFile(Context c, String cc) {
        return new File(c.getFilesDir(), "radio_" + cc + ".m3u");
    }

    /** Listeyi (onbellek veya gomulu) yukler. */
    public static synchronized void ensure(Context c) {
        String cc = country(c);
        if (cc.equals(loadedCountry) && !NAMES.isEmpty()) return;
        loadedCountry = cc;
        NAMES.clear();
        META.clear();
        URLS.clear();

        if ("tr".equals(cc)) {
            readStream(c, openAsset(c));
            return;
        }
        File f = cacheFile(c, cc);
        if (f.exists()) {
            try {
                readStream(c, new java.io.FileInputStream(f));
                return;
            } catch (Exception e) {
                Log.w(TAG, "onbellek okunamadi: " + e);
            }
        }
        // onbellek yok: gomulu TR listesini gecici goster, indirme baslatilabilir
    }

    /** O ulke icin indirme gerekiyor mu? */
    public static boolean needsDownload(Context c) {
        String cc = country(c);
        return !"tr".equals(cc) && !cacheFile(c, cc).exists();
    }

    /** Ayni ulke icin tekrar yukle (indirme sonrasi). */
    public static synchronized void reload(Context c) {
        loadedCountry = null;
        ensure(c);
    }

    private static java.io.InputStream openAsset(Context c) {
        try {
            return c.getAssets().open("radios.m3u");
        } catch (Exception e) {
            return null;
        }
    }

    private static void readStream(Context c, java.io.InputStream in) {
        if (in == null) return;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line, info = null;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                if (line.startsWith("#EXTINF")) {
                    info = line.substring(line.indexOf(',') + 1).trim();
                } else if (!line.startsWith("#") && info != null) {
                    // 2 kez açılmayan istasyon listeden kalkar (TV ile aynı kural)
                    if (Prefs.hideDead(c) && Prefs.fails(c, line) >= 2) { info = null; continue; }
                    String[] p = info.split("\\|");
                    NAMES.add(p.length > 0 ? p[0].trim() : "Radyo");
                    META.add(p.length > 1 ? (p[1].trim() + (p.length > 2 ? " · " + p[2].trim() : ""))
                            : "internet");
                    URLS.add(line);
                    info = null;
                }
            }
            r.close();
        } catch (Exception e) {
            Log.w(TAG, "liste okunamadi: " + e);
        }
    }

    /**
     * Ulkenin radyolarini indirir (arka planda cagrilmali).
     * radio-browser.info: ucretsiz, API anahtari gerektirmez.
     */
    public static void download(final Context c, final Runnable onDone) {
        if (downloading) return;
        downloading = true;
        final String cc = country(c);
        new Thread(new Runnable() {
            @Override public void run() {
                boolean ok = false;
                try {
                    String q = API + "?countrycode=" + URLEncoder.encode(cc.toUpperCase(), "UTF-8")
                            + "&hidebroken=true&order=clickcount&reverse=true&limit=80";
                    HttpURLConnection con = (HttpURLConnection) new URL(q).openConnection();
                    con.setConnectTimeout(15000);
                    con.setReadTimeout(20000);
                    con.setRequestProperty("User-Agent", "UMAY-Multi-TV/1.0");
                    con.setRequestProperty("Accept", "application/json");
                    if (con.getResponseCode() == 200) {
                        BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                        StringBuilder sb = new StringBuilder();
                        String l;
                        while ((l = r.readLine()) != null) sb.append(l);
                        r.close();
                        ok = save(cc, new JSONArray(sb.toString()), c);
                    }
                    con.disconnect();
                } catch (Throwable t) {
                    Log.w(TAG, "radyo indirme hatasi: " + t);
                }
                downloading = false;
                if (ok) reload(c);
                if (onDone != null) onDone.run();
            }
        }, "radio-dl").start();
    }

    private static boolean save(String cc, JSONArray arr, Context c) throws Exception {
        StringBuilder out = new StringBuilder("#EXTM3U\n");
        int added = 0;
        for (int i = 0; i < arr.length() && added < 80; i++) {
            JSONObject o = arr.getJSONObject(i);
            String url = o.optString("url_resolved", "");
            if (url.length() == 0) url = o.optString("url", "");
            String name = o.optString("name", "").trim();
            if (url.length() == 0 || name.length() == 0) continue;
            String codec = o.optString("codec", "MP3");
            String bitrate = o.optString("bitrate", "");
            String city = o.optString("state", "");
            if (city.length() == 0) city = o.optString("country", cc.toUpperCase());
            String meta = codec.toUpperCase() + (bitrate.length() > 0 ? " · " + bitrate + " kbps" : "")
                    + " | " + city;
            out.append("#EXTINF:-1,").append(name.replace("\n", " ")).append(" | ").append(meta).append('\n')
               .append(url).append('\n');
            added++;
        }
        if (added == 0) return false;
        File f = cacheFile(c, cc);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(out.toString().getBytes("UTF-8"));
        fos.close();
        Log.i(TAG, cc + " icin " + added + " radyo kaydedildi");
        return true;
    }
}
