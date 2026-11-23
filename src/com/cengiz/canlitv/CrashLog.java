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
