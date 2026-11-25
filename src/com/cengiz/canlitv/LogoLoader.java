package com.cengiz.canlitv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Kanal logolarini arka planda indirip onbellege alan yukleyici.
 *
 * 9.82: logolarin bir kismi gorunmuyordu. Sebep: Wikimedia (upload.wikimedia.org)
 * ayni IP'den art arda gelen istekleri 429 ile reddediyor ve uygulama acilirken
 * 40+ logo birden isteniyordu. Cozum:
 *   - ayni anda en fazla 2 indirme (bant genisligi degil, 429 siniri icin),
 *   - basarisiz istekte 2 kez yeniden deneme (600 ms, 1400 ms ara),
 *   - inen logo diske yazilir; sonraki acilista aga hic cikilmaz,
 *   - tanimlayici User-Agent (Wikimedia politikasi genel tarayici UA'sini engelliyor).
 */
public class LogoLoader {

    /** Sonucu UI thread'inde bildirir (false = logo gelmedi). */
    public interface Cb {
        void done(boolean ok);
    }

    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(200);
    private static final ExecutorService POOL = Executors.newFixedThreadPool(2);
    private static final String UA =
            "UmayMultiTV/9.82 (Android; +https://umay-multi-tv-gq3zer.freebuff.site; cengizkilic1980@gmail.com)";

    public static void load(final ImageView view, final String url) {
        load(view, url, null);
    }

    public static void load(final ImageView view, final String url, final Cb cb) {
        if (url == null || url.length() == 0) {
            view.setImageDrawable(null);
            if (cb != null) cb.done(false);
            return;
        }
        Bitmap cached = CACHE.get(url);
        if (cached != null) {
            view.setImageBitmap(cached);
            if (cb != null) cb.done(true);
            return;
        }
        view.setImageDrawable(null);
        view.setTag(url);
        final Context app = view.getContext().getApplicationContext();
        POOL.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = fromDisk(app, url);
                if (bmp == null) bmp = download(url);
                if (bmp != null) {
                    CACHE.put(url, bmp);
                    toDisk(app, url, bmp);
                }
                final Bitmap f = bmp;
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean same = url.equals(view.getTag());
                        if (same && f != null) view.setImageBitmap(f);
                        if (same && cb != null) cb.done(f != null);
                    }
                });
            }
        });
    }

    private static Bitmap download(String url) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                if (attempt > 0) Thread.sleep(attempt == 1 ? 600L : 1400L);
                HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
                c.setConnectTimeout(7000);
                c.setReadTimeout(10000);
                c.setRequestProperty("User-Agent", UA);
                c.setRequestProperty("Accept", "image/*");
                int code = c.getResponseCode();
                if (code == 429 || code >= 500) {   // sinir / gecici sunucu hatasi: bekle, dene
                    c.disconnect();
                    continue;
                }
                if (code != 200) break;
                InputStream in = c.getInputStream();
                Bitmap bmp = BitmapFactory.decodeStream(in);
                in.close();
                c.disconnect();
                if (bmp != null) return bmp;
            } catch (Exception ignored) {
                // sonraki deneme
            }
        }
        return null;
    }

    private static File file(Context c, String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] h = md.digest(url.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            File dir = new File(c.getCacheDir(), "logos");
            if (!dir.exists()) dir.mkdirs();
            return new File(dir, sb.toString() + ".img");
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap fromDisk(Context c, String url) {
        try {
            File f = file(c, url);
            if (f == null || !f.exists() || f.length() < 300) return null;
            return BitmapFactory.decodeFile(f.getAbsolutePath());
        } catch (Throwable t) {
            return null;
        }
    }

    private static void toDisk(Context c, String url, Bitmap bmp) {
        try {
            File f = file(c, url);
            if (f == null) return;
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 92, out);
            out.close();
        } catch (Throwable ignored) { }
    }
}
