package com.cengiz.canlitv;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Canli HLS yayinini segment segment indirip tek .ts dosyasina yazar.
 * Kayit yeri: Movies/TV-Kayitlari (galeride gorunur). Ses de kayda dahildir (TS akisi ses+goruntu).
 */
public class HlsRecorder {

    public interface Listener {
        void onInfo(String msg);
        void onError(String msg);
        void onStopped(String path, long bytes, boolean audioIncluded);
    }

    private volatile boolean running = false;
    private Thread worker;
    private Listener listener;
    private String savedPath = "";

    /**
     * M-26: eski kaydın iş parçacığı kapanırken (son segmenti yazıyor) yeni kayıt
     * isteği sessizce yutuluyordu; düğme kırmızı kalıp kullanıcı kayıtta sanıyordu.
     */
    public boolean isRunning() {
        return running || (worker != null && worker.isAlive());
    }

    public void start(final Context ctx, final String playlistUrl, final String baseName, Listener l) {
        listener = l;
        if (isRunning()) {
            if (l != null) l.onError("Önceki kayıt kapanıyor, bir saniye sonra tekrar dene");
            return;
        }
        running = true;
        listener = l;

        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream out = null;
                long total = 0;
                Uri pendingUri = null;
                try {
                    String media = resolveMediaPlaylist(ctx, playlistUrl);
                    if (media == null) { fail("Yayin adresi okunamadi"); return; }
                    if (media.contains("METHOD=AES-128")) { fail("Sifreli yayin kaydedilemiyor"); return; }
                    // M-26: fMP4 (EXT-X-MAP) ve byte-range yayinlar .ts konteynerine
                    // yazilamaz; eskiden dosya "kaydedildi" gorunup acilmiyordu.
                    if (media.contains("#EXT-X-MAP")) {
                        fail("Bu kanal parcali (fMP4) yayin veriyor; kayit desteklenmiyor"); return;
                    }
                    if (media.contains("#EXT-X-BYTERANGE")) {
                        fail("Bu kanal byte-range yayin veriyor; kayit desteklenmiyor"); return;
                    }
                    // M-26: yer kontrolu - kayit ortasinda "yer yok" ile kesilmesin.
                    long free = freeBytes();
                    if (free >= 0 && free < 500L * 1048576L) {
                        fail("Yer yok: " + (free / 1048576L) + " MB bos, en az 500 MB gerekli"); return;
                    }

                    String safe = baseName.replaceAll("[^A-Za-z0-9]+", "_");
                    String fname = safe + "_" + System.currentTimeMillis() + ".ts";

                    if (Build.VERSION.SDK_INT >= 29) {
                        ContentValues cv = new ContentValues();
                        cv.put(MediaStore.Video.Media.DISPLAY_NAME, fname);
                        cv.put(MediaStore.Video.Media.MIME_TYPE, "video/mp2t");
                        cv.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/TV-Kayitlari");
                        cv.put(MediaStore.Video.Media.IS_PENDING, 1);
                        ContentResolver cr = ctx.getContentResolver();
                        pendingUri = cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cv);
                        if (pendingUri == null) { fail("Kayit dosyasi olusturulamadi"); return; }
                        out = cr.openOutputStream(pendingUri);
                        savedPath = "Movies/TV-Kayitlari/" + fname;
                    } else {
                        File dir = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_MOVIES), "TV-Kayitlari");
                        if (!dir.exists()) dir.mkdirs();
                        File f = new File(dir, fname);
                        out = new FileOutputStream(f, true);
                        savedPath = f.getAbsolutePath();
                    }

                    List<String> seen = new ArrayList<String>();
                    String base = media.substring(0, media.lastIndexOf('/') + 1);
                    String token = media.contains("?")
                            ? media.substring(media.indexOf('?')) : "";
                    int tokenLen = token.length();
                    info("Kayit basladi (ses dahil)");

                    while (running) {
                        String pl = fetch(media);
                        if (pl == null) { sleep(3000); continue; }
                        if (pl.contains("METHOD=AES-128")) { fail("Sifreli yayin kaydedilemiyor"); return; }
                        if (pl.contains("#EXT-X-MAP")) {
                            fail("Yayin fMP4'e gecti; kayit desteklenmiyor"); return;
                        }
                        if (pl.contains("#EXT-X-BYTERANGE")) {
                            fail("Yayin byte-range'e gecti; kayit desteklenmiyor"); return;
                        }

                        int target = 6;
                        java.util.regex.Matcher m = java.util.regex.Pattern
                                .compile("#EXT-X-TARGETDURATION:(\\d+)").matcher(pl);
                        if (m.find()) target = Integer.parseInt(m.group(1));

                        for (String line : pl.split("\n")) {
                            line = line.trim();
                            if (line.length() == 0 || line.startsWith("#")) continue;
                            if (seen.contains(line)) continue;
                            seen.add(line);
                            String segUrl = line.startsWith("http") ? line : base + line;
                            // M-26: master "?token=..." tasiyorsa segment de tasimali
                            // (jetonlu CDN'lerde segment 403 veriyordu).
                            if (!line.contains("?") && tokenLen > 0) segUrl += token;
                            byte[] data = fetchBytes(segUrl);
                            if (data != null) {
                                out.write(data);
                                out.flush();
                                total += data.length;
                                if (seen.size() % 5 == 0) info("Kaydediliyor: " + (total / 1048576) + " MB");
                            }
                            if (!running) break;
                        }
                        if (seen.size() > 3000) seen = new ArrayList<String>(seen.subList(1500, seen.size()));
                        sleep(target * 1000L / 2);
                    }
                } catch (Exception e) {
                    fail("Kayit hatasi: " + e.getMessage());
                    return;
                } finally {
                    try { if (out != null) out.close(); } catch (Exception ignored) { }
                    if (pendingUri != null) {
                        ContentValues fin = new ContentValues();
                        fin.put(MediaStore.Video.Media.IS_PENDING, 0);
                        try { ctx.getContentResolver().update(pendingUri, fin, null, null); } catch (Exception ignored) { }
                    }
                }
                if (listener != null) listener.onStopped(savedPath, total, true);
            }
        }, "hls-recorder");
        worker.start();
    }

    public void stop() { running = false; }

    private void info(String s) { if (listener != null) listener.onInfo(s); }

    private void fail(String s) {
        running = false;
        if (listener != null) listener.onError(s);
    }

    /**
     * Master playlist ise **kalite tercihine uygun** varyanti secer (M-26).
     * Eskiden her zaman ilk varyant (genelde 1080p) indiriliyordu; 360p tercih eden
     * kullanici kayitta saatte GB'larca veri harciyordu.
     */
    private String resolveMediaPlaylist(Context ctx, String url) {
        String txt = fetch(url);
        if (txt == null) return null;
        if (!txt.contains("#EXT-X-STREAM-INF")) return url;
        String base = url.substring(0, url.lastIndexOf('/') + 1);
        String token = url.contains("?") ? url.substring(url.indexOf('?')) : "";
        String[] lines = txt.split("\n");
        List<Quality.Variant> variants = new ArrayList<Quality.Variant>();
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].startsWith("#EXT-X-STREAM-INF")) continue;
            int height = 0;
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("RESOLUTION=(\\d+)x(\\d+)").matcher(lines[i]);
            if (m.find()) height = Integer.parseInt(m.group(2));
            for (int j = i + 1; j < lines.length; j++) {
                String c = lines[j].trim();
                if (c.length() == 0 || c.startsWith("#")) continue;
                String v = c.startsWith("http") ? c : base + c;
                if (!c.contains("?") && token.length() > 0) v += token;
                variants.add(new Quality.Variant(height > 0 ? height + "p" : "varyant", v, height));
                break;
            }
        }
        if (variants.isEmpty()) return url;
        int pref = Prefs.economy(ctx) ? -1 : Prefs.maxHeight(ctx);
        String chosen = Quality.pick(variants, pref);
        return chosen == null ? url : chosen;
    }

    /** Kayit hedefinde kalan bos alan (bayt); bilinmiyorsa -1. */
    private long freeBytes() {
        try {
            return new android.os.StatFs(
                    Environment.getExternalStorageDirectory().getAbsolutePath())
                    .getAvailableBytes();
        } catch (Throwable t) { return -1; }
    }

    private String fetch(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(8000); c.setReadTimeout(8000);
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

    private byte[] fetchBytes(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(8000); c.setReadTimeout(8000);
            c.setRequestProperty("User-Agent", "Mozilla/5.0");
            InputStream in = c.getInputStream();
            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close();
            return bo.toByteArray();
        } catch (Exception e) { return null; }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { }
    }
}
