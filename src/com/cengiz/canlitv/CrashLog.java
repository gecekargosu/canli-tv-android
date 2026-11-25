package com.cengiz.canlitv;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Hata gunlugu: uygulama cokerse yigin izini cihaza yazar.
 * Kullanici Ayarlar -> "Hata bildir" ile bunu kopyalayip bize yollayabilir.
 */
public final class CrashLog {

    private static final String TAG = "UmayCrash";
    private static final String FILE = "crash.txt";
    private static Thread.UncaughtExceptionHandler previous;

    private CrashLog() { }

    public static void install(Context c) {
        if (previous != null) return;
        previous = Thread.getDefaultUncaughtExceptionHandler();
        final Context app = c.getApplicationContext();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override public void uncaughtException(Thread t, Throwable e) {
                try { write(app, t, e); } catch (Throwable ignored) { }
                if (previous != null) previous.uncaughtException(t, e);
            }
        });
    }

    private static void write(Context c, Thread t, Throwable e) throws Exception {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String head = "UMAY Multi TV " + BuildConfig.VERSION_NAME
                + " (" + BuildConfig.VERSION_CODE + ")\n"
                + "Tarih: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "\n"
                + "Cihaz: " + Build.MANUFACTURER + " " + Build.MODEL + " · Android " + Build.VERSION.RELEASE
                + " (SDK " + Build.VERSION.SDK_INT + ")\n"
                + "Is parcacigi: " + (t == null ? "?" : t.getName()) + "\n\n";
        File f = new File(c.getFilesDir(), FILE);
        // K-17: çökme dosyası da 512 KB'ı aşarsa eskisini yedek olarak bırak.
        if (f.exists() && f.length() > 512 * 1024) {
            File old = new File(c.getFilesDir(), "crash.1.txt");
            if (old.exists()) old.delete();
            f.renameTo(old);
        }
        FileOutputStream fos = new FileOutputStream(f);
        fos.write((head + sw.toString()).getBytes("UTF-8"));
        fos.close();
        Log.e(TAG, "hata kaydedildi: " + f.getAbsolutePath());
    }

    private static final String EVENTS = "events.txt";

    /**
     * Ölümcül olmayan olayları da yaz (bağlantı hatası, yeniden bağlanma...).
     * Böylece "kanal açılmadı" gibi şikâyetlerde kullanıcı tek tuşla kayıt gönderebilir.
     */
    public static void event(Context c, String tag, String msg) {
        try {
            File f = new File(c.getApplicationContext().getFilesDir(), EVENTS);
            // K-17: 512 KB'a ulaşınca dosyayı silmek yerine bir öncekini sakla (tek yedek).
            if (f.exists() && f.length() > 512 * 1024) {
                File old = new File(c.getApplicationContext().getFilesDir(), "events.1.txt");
                if (old.exists()) old.delete();
                f.renameTo(old);
            }
            String line = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(new Date())
                    + " [" + tag + "] " + msg + "\n";
            FileOutputStream fos = new FileOutputStream(f, true);
            fos.write(line.getBytes("UTF-8"));
            fos.close();
        } catch (Throwable ignored) { }
    }

    /** Son olay kayıtları (en fazla 6000 karakter). */
    public static String events(Context c) {
        try {
            File f = new File(c.getApplicationContext().getFilesDir(), EVENTS);
            if (!f.exists()) return "";
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    new java.io.FileInputStream(f), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = r.readLine()) != null) sb.append(l).append('\n');
            r.close();
            String s = sb.toString();
            if (s.length() > 6000) s = "…" + s.substring(s.length() - 6000);
            return s;
        } catch (Exception e) {
            return "";
        }
    }

    private static final java.util.HashMap<String, Long> lastThrottle =
            new java.util.HashMap<String, Long>();

    /**
     * Aynı olayın günlüğü doldurmasını engeller: anahtar başına en sık {@code minGapMs}.
     * (Örn. reklam dolmama hatası her ekranda tekrar yazılmasın.)
     */
    public static void eventThrottled(Context c, String tag, String key, String msg, long minGapMs) {
        try {
            long now = System.currentTimeMillis();
            synchronized (lastThrottle) {
                Long last = lastThrottle.get(tag + ":" + key);
                if (last != null && now - last.longValue() < minGapMs) return;
                lastThrottle.put(tag + ":" + key, Long.valueOf(now));
            }
            event(c, tag, msg);
        } catch (Throwable ignored) { }
    }

    /** Log + cihaz bilgisi: e-posta/WhatsApp ile gönderilecek tam metin. */
    public static String report(Context c) {
        String crash = last(c);
        String ev = events(c);
        StringBuilder sb = new StringBuilder();
        sb.append("UMAY Multi TV ").append(BuildConfig.VERSION_NAME)
          .append(" (").append(BuildConfig.VERSION_CODE).append(")\n")
          .append("Cihaz: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
          .append(" · Android ").append(Build.VERSION.RELEASE).append(" (SDK ")
          .append(Build.VERSION.SDK_INT).append(")\n")
          .append("Tarih: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                  .format(new Date())).append("\n\n");
        if (ev.length() > 0) sb.append("--- Olaylar ---\n").append(ev).append('\n');
        if (crash.length() > 0) sb.append("--- Son çökme ---\n").append(crash);
        if (ev.length() == 0 && crash.length() == 0) sb.append("Kayıtlı hata yok.\n");
        return sb.toString();
    }

    public static void clearEvents(Context c) {
        new File(c.getApplicationContext().getFilesDir(), EVENTS).delete();
    }

    /** Hata günlüğü sunucusu (site/worker.js → POST /api/log). */
    public static final String ENDPOINT =
            "https://umay-multi-tv-gq3zer.freebuff.site/api/log";

    /**
     * Günlüğü günde bir kez sunucuya gönderir — yalnız kullanıcı Ayarlar → Hata günlüğü
     * ekranından açtıysa (varsayılan KAPALI). Gönderim arka planda, uygulama akışını durdurmaz.
     */
    public static void uploadIfDue(final Context c) {
        try {
            if (c == null || !Prefs.autoLogSend(c)) return;
            long now = System.currentTimeMillis();
            if (now - Prefs.lastLogUpload(c) < 20L * 60L * 60L * 1000L) return;
            final String ev = events(c);
            final String crash = last(c);
            if (ev.length() == 0 && crash.length() == 0) return;
            // Aynı anda iki gönderim olmasın: zaman damgasını hemen işaretle.
            Prefs.setLastLogUpload(c, now);
            final Context app = c.getApplicationContext();
            new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        org.json.JSONObject o = new org.json.JSONObject();
                        o.put("id", deviceId(app));
                        o.put("ts", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                                .format(new Date()));
                        o.put("ver", BuildConfig.VERSION_NAME);
                        o.put("code", BuildConfig.VERSION_CODE);
                        o.put("device", Build.MANUFACTURER + " " + Build.MODEL
                                + " · Android " + Build.VERSION.RELEASE
                                + " (SDK " + Build.VERSION.SDK_INT + ")");
                        o.put("events", ev.length() > 6000 ? ev.substring(ev.length() - 6000) : ev);
                        o.put("crash", crash.length() > 4000 ? crash.substring(0, 4000) : crash);
                        java.net.HttpURLConnection h = (java.net.HttpURLConnection)
                                new java.net.URL(ENDPOINT).openConnection();
                        h.setConnectTimeout(12000);
                        h.setReadTimeout(12000);
                        h.setRequestMethod("POST");
                        h.setDoOutput(true);
                        h.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                        byte[] body = o.toString().getBytes("UTF-8");
                        h.setFixedLengthStreamingMode(body.length);
                        h.getOutputStream().write(body);
                        h.getOutputStream().close();
                        int code = h.getResponseCode();
                        h.disconnect();
                        if (code >= 200 && code < 300) {
                            event(app, "log", "günlük sunucuya gönderildi");
                        } else {
                            Prefs.setLastLogUpload(app, 0L);   // yarın tekrar dene
                            event(app, "log", "günlük gönderilemedi: HTTP " + code);
                        }
                    } catch (Throwable t) {
                        Prefs.setLastLogUpload(app, 0L);
                        event(app, "log", "günlük gönderilemedi: " + t.getClass().getSimpleName());
                    }
                }
            }, "log-upload").start();
        } catch (Throwable ignored) { }
    }

    /** Cihaz başına sabit, kişisel bilgi içermeyen kimlik (günlükleri ayırmak için). */
    public static String deviceId(Context c) {
        try {
            String s = android.provider.Settings.Secure.getString(
                    c.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            if (s == null || s.length() == 0) s = "unknown";
            return s.length() > 16 ? s.substring(0, 16) : s;
        } catch (Throwable t) {
            return "unknown";
        }
    }

    /** Son hata metni (yoksa bos string). */
    public static String last(Context c) {
        try {
            File f = new File(c.getFilesDir(), FILE);
            if (!f.exists()) return "";
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    new java.io.FileInputStream(f), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = r.readLine()) != null) sb.append(l).append('\n');
            r.close();
            if (sb.length() > 4000) return sb.substring(0, 4000);
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static void clear(Context c) {
        new File(c.getFilesDir(), FILE).delete();
    }
}
