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
            if (f.exists() && f.length() > 96 * 1024) f.delete();
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
