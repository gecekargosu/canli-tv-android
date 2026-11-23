package com.cengiz.canlitv;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "Bilinen iyi" kanal sırası.
 *
 * Sunucuda (site/public/good/<cc>.json) her ülke için gerçekten açılan kanalların listesi durur
 * (`_build/good.py` üretir). Uygulama bu dosyayı ülke listesiyle birlikte indirir ve
 * **çalışan kanalları listenin en başına** alır; böylece kullanıcı ilk sıralarda ölü yayın görmez.
 *
 * Dosya yoksa hiçbir şey değişmez (çevrimdışı güvenli).
 */
public final class Good {

    private Good() { }

    private static File file(Context c, String cc) {
        return new File(c.getFilesDir(), "good_" + cc + ".json");
    }

    private static String read(Context c, String cc) {
        try {
            InputStream in;
            File cached = file(c, cc);
            if (cached.exists() && cached.length() > 20) {
                in = new FileInputStream(cached);        // günlük tazelenmiş liste (varsa)
            } else if ("tr".equals(cc)) {
                in = c.getAssets().open("good_tr.json");   // TR listesi gömülü
            } else {
                return null;
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            r.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * #101: doğrulanmış kanal listesini günde bir kez sunucudan tazeler.
     * Açılışta arka planda çalışır; başarısız olursa gömülü/eski liste kullanılmaya devam eder.
     */
    public static void refreshIfNeeded(final Context ctx) {
        final String cc = Prefs.country(ctx);
        String day = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date());
        if (day.equals(Prefs.goodDay(ctx))) return;
        Prefs.setGoodDay(ctx, day);
        new Thread(new Runnable() {
            @Override public void run() { download(ctx, cc); }
        }, "good-refresh").start();
    }

    private static void download(Context ctx, String cc) {
        java.net.HttpURLConnection con = null;
        try {
            con = (java.net.HttpURLConnection) new java.net.URL(
                    "https://umay-multi-tv-gq3zer.freebuff.site/good/" + cc + ".json").openConnection();
            con.setConnectTimeout(8000);
            con.setReadTimeout(10000);
            con.setRequestProperty("User-Agent", "Mozilla/5.0 UMAYMultiTV");
            if (con.getResponseCode() != 200) return;
            InputStream in = con.getInputStream();
            File tmp = new File(ctx.getFilesDir(), "good_" + cc + ".tmp");
            java.io.FileOutputStream out = new java.io.FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int n;
            long total = 0;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                total += n;
            }
            out.close();
            in.close();
            if (total < 30) { tmp.delete(); return; }
            // gerçekten kanal listesi mi? (bozuk dosya gömülü listeyi ezmesin)
            String txt = readFile(tmp);
            JSONArray items = new JSONObject(txt).optJSONArray("items");
            if (items == null || items.length() == 0) { tmp.delete(); return; }
            File target = file(ctx, cc);
            if (target.exists()) target.delete();
            tmp.renameTo(target);
        } catch (Exception ignored) {
        } finally {
            if (con != null) try { con.disconnect(); } catch (Exception ignored) { }
        }
    }

    private static String readFile(File f) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        r.close();
        return sb.toString();
    }

    /**
     * Sıralama: **çalışan kanallar önce** (JSON sırasıyla), sonra kalanlar popülerliğe göre.
     * @return true = doğrulanmış liste bulundu ve uygulandı
     */
    public static boolean apply(Context ctx, String cc, List<Channel> list) {
        if (list == null || list.size() < 2) return false;
        try {
            String json = read(ctx, cc);
            if (json == null) return false;
            JSONArray items = new JSONObject(json).optJSONArray("items");
            if (items == null || items.length() == 0) return false;

            Map<String, Integer> rank = new HashMap<String, Integer>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject o = items.optJSONObject(i);
                if (o == null) continue;
                String u = o.optString("url", "");
                if (u.length() > 0 && !rank.containsKey(u)) rank.put(u, rank.size());
            }
            if (rank.isEmpty()) return false;

            final Map<String, Integer> r = rank;
            final String country = cc;
            java.util.Collections.sort(list, new java.util.Comparator<Channel>() {
                @Override public int compare(Channel a, Channel b) {
                    boolean va = r.containsKey(a.url);
                    boolean vb = r.containsKey(b.url);
                    if (va != vb) return va ? -1 : 1;          // çalışan önce
                    if (va) {
                        int ra = r.get(a.url), rb = r.get(b.url);
                        return ra < rb ? -1 : (ra == rb ? 0 : 1);
                    }
                    int pa = Popular.rank(country, a.name);
                    int pb = Popular.rank(country, b.name);
                    return pa < pb ? -1 : (pa == pb ? 0 : 1);
                }
            });
            for (int i = 0; i < list.size(); i++) {
                list.get(i).baseNo = i + 1;
                list.get(i).no = i + 1;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
