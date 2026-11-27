package com.cengiz.canlitv;

import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * v10.09: "Bu kanal çalışmıyor" bildirimi.
 *
 * Kullanıcı düğmeye bastığında çalışır (otomatik gönderim değil). Bildirim önce cihaza
 * yazılır, sonra gönderilir; internet yoksa **kuyrukta bekler** ve bir sonraki açılışta
 * ya da bir sonraki başarılı gönderimde tekrar denenir. Böylece "kullanıcı bildirdi ama
 * kayboldu" durumu olmaz.
 *
 * Sunucu tarafı DEĞİŞMEDİ: mevcut POST /api/log uç noktası kullanılır. Bildirim, günlük
 * olay metninin başına "📣 BİLDİRİM" satırı olarak eklenir; böylece site yeniden
 * yayınlanmadan da veri toplanmaya başlar.
 */
public final class ChannelReport {

    private static final String FILE = "reports.txt";
    private static final String LAST = "report-last.txt";
    /** Aynı kanal için iki bildirim arası en az süre (kazara çift dokunma). */
    private static final long SAME_GAP_MS = 10L * 60L * 1000L;
    private static final int MAX_QUEUE = 40;

    private ChannelReport() { }

    /** Bildirim sonucu: uygulama arayüzüne döner. */
    public interface Done {
        /**
         * @param ok     sunucu 2xx döndü
         * @param queued gönderilemedi, cihazda kuyruğa alındı
         */
        void onResult(boolean ok, boolean queued);
    }

    /** Aynı kanal az önce bildirildi mi? (10 dakika) */
    public static boolean tooSoon(Context c) { return tooSoon(c, null); }

    public static boolean tooSoon(Context c, String url) {
        try {
            if (url == null) return false;
            long now = System.currentTimeMillis();
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(c.getApplicationContext().getFilesDir(), LAST)), "UTF-8"));
            String line = r.readLine();
            r.close();
            if (line == null) return false;
            int bar = line.indexOf('|');
            if (bar <= 0) return false;
            long when = Long.parseLong(line.substring(0, bar));
            String u = line.substring(bar + 1);
            return u.equals(url) && now - when < SAME_GAP_MS;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Bildirimi kuyruğa yazar ve arka planda göndermeye çalışır.
     *
     * @param channel kanal adı
     * @param url     kanal adresi
     * @param kind    StreamDiag sınıfı
     * @param detail  kullanıcının gördüğü sebep / ek bilgi
     */
    public static void send(final Context c, final String channel, final String url,
                            final int kind, final String detail, final Done done) {
        final Context app = c.getApplicationContext();
        final String line = System.currentTimeMillis() + "|"
                + clean(channel, 60) + "|" + clean(url, 300) + "|"
                + StreamDiag.code(kind) + "|" + clean(detail, 200);
        enqueue(app, line);
        markLast(app, url);
        CrashLog.event(app, "report", "bildirildi: " + clean(channel, 60) + " · " + StreamDiag.code(kind));
        new Thread(new Runnable() {
            @Override public void run() {
                boolean ok = flushBlocking(app);
                // Test ve kullanıcı desteği için sonucu da günlüğe yaz.
                CrashLog.event(app, "report", ok ? "bildirim gönderildi" : "bildirim kuyrukta (gönderilemedi)");
                final boolean sent = ok;
                if (done != null) {
                    done.onResult(sent, !sent);
                }
            }
        }, "report-send").start();
    }

    /** Kuyrukta bekleyen bildirimleri göndermeyi dener (açılışta / ağ gelince çağrılabilir). */
    public static void flush(final Context c) {
        if (c == null) return;
        final Context app = c.getApplicationContext();
        new Thread(new Runnable() {
            @Override public void run() {
                flushBlocking(app);
            }
        }, "report-flush").start();
    }

    /** Kuyruktaki tüm bildirimleri sırayla gönderir; başarısız olanı kuyrukta bırakır. */
    private static boolean flushBlocking(Context app) {
        synchronized (ChannelReport.class) {
            List<String> lines = read(app);
            if (lines.isEmpty()) return true;
            int bekleyen = lines.size();
            boolean all = true;
            List<String> kalan = new ArrayList<String>();
            for (int i = 0; i < lines.size(); i++) {
                String l = lines.get(i);
                if (all && post(app, l)) continue;   // gönderildi
                all = false;
                kalan.add(l);                        // sonrakileri de sonra deneyeceğiz
            }
            write(app, kalan);
            CrashLog.event(app, "report", all
                    ? "kuyruk gönderildi: " + bekleyen
                    : "kuyruk gönderilemedi: " + kalan.size() + "/" + bekleyen);
            return all;
        }
    }

    private static boolean post(Context app, String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 5) return true;               // bozuk satır: at
        try {
            String channel = p[1], url = p[2], code = p[3], detail = p[4];
            org.json.JSONObject o = new org.json.JSONObject();
            // Ayrı kimlik: günlük otomatik kaydın üzerine yazmasın (tablo (id, day) anahtarlı).
            o.put("id", (CrashLog.deviceId(app) + "-r"
                    + Long.toString(System.currentTimeMillis() / 1000L % 100000L)));
            o.put("ts", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            o.put("ver", BuildConfig.VERSION_NAME);
            o.put("code", BuildConfig.VERSION_CODE);
            o.put("device", Build.MANUFACTURER + " " + Build.MODEL
                    + " · Android " + Build.VERSION.RELEASE
                    + " (SDK " + Build.VERSION.SDK_INT + ")");
            String ev = "📣 BİLDİRİM · kanal=" + channel + " · sebep=" + code
                    + " · detay=" + detail + " · url=" + url + "\n" + CrashLog.events(app);
            o.put("events", ev.length() > 6000 ? ev.substring(0, 6000) : ev);
            String crash = CrashLog.last(app);
            o.put("crash", crash.length() > 1500 ? crash.substring(0, 1500) : crash);

            java.net.HttpURLConnection h = (java.net.HttpURLConnection)
                    new java.net.URL(CrashLog.ENDPOINT).openConnection();
            h.setConnectTimeout(12000);
            h.setReadTimeout(12000);
            h.setRequestMethod("POST");
            h.setDoOutput(true);
            h.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            byte[] body = o.toString().getBytes("UTF-8");
            h.setFixedLengthStreamingMode(body.length);
            h.getOutputStream().write(body);
            h.getOutputStream().close();
            int rc = h.getResponseCode();
            h.disconnect();
            return rc >= 200 && rc < 300;
        } catch (Throwable t) {
            return false;
        }
    }

    // ------------------------------------------------------------------ kuyruk dosyası

    private static void enqueue(Context app, String line) {
        try {
            List<String> lines = read(app);
            lines.add(line);
            while (lines.size() > MAX_QUEUE) lines.remove(0);
            write(app, lines);
        } catch (Throwable ignored) { }
    }

    private static List<String> read(Context app) {
        List<String> out = new ArrayList<String>();
        try {
            File f = new File(app.getFilesDir(), FILE);
            if (!f.exists()) return out;
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    new FileInputStream(f), "UTF-8"));
            String l;
            while ((l = r.readLine()) != null) {
                if (l.length() > 6) out.add(l);
            }
            r.close();
        } catch (Throwable ignored) { }
        return out;
    }

    private static void write(Context app, List<String> lines) {
        try {
            File f = new File(app.getFilesDir(), FILE);
            if (lines.isEmpty()) { f.delete(); return; }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.size(); i++) sb.append(lines.get(i)).append('\n');
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(sb.toString().getBytes("UTF-8"));
            fos.close();
        } catch (Throwable ignored) { }
    }

    private static void markLast(Context app, String url) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(app.getFilesDir(), LAST));
            fos.write((System.currentTimeMillis() + "|" + clean(url, 300)).getBytes("UTF-8"));
            fos.close();
        } catch (Throwable ignored) { }
    }

    /** Kayıt dosyasını bozmasın: ayraç ve satır sonu temizlenir. */
    private static String clean(String s, int max) {
        if (s == null) return "";
        String v = s.replace('|', ' ').replace('\n', ' ').replace('\r', ' ').trim();
        return v.length() > max ? v.substring(0, max) : v;
    }
}
