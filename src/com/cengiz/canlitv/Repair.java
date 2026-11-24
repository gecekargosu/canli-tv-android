package com.cengiz.canlitv;

import android.content.Context;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * "Ölü kanalları onar": daha önce açılmayıp gizlenmiş kanalları yeniden dener,
 * yayın geri gelmişse işareti temizleyip listeye geri koyar.
 *
 * Not: Kaynağı tamamen kapanmış kanalı sihirle canlandıramaz; ama yayıncı
 * yayını geri açtığında (veya CDN düzeldiğinde) kanal kendiliğinden döner.
 * Uygulama açılışta bunu arka planda kendisi de yapar.
 */
public class Repair {

    public interface Callback {
        void done(int tested, int revived);
    }

    private static volatile boolean running = false;

    public static boolean isRunning() { return running; }

    public static void run(final Context ctx, final Callback cb) {
        if (running) return;
        running = true;
        new Thread(new Runnable() {
            @Override public void run() {
                int tested = 0, revived = 0;
                try {
                    List<Channel> all = ChannelRepo.all(ctx);
                    for (Channel c : all) {
                        if (c == null || c.url == null) continue;
                        if (Prefs.fails(ctx, c.url) < 2) continue;   // sağlam kanalları yorma
                        tested++;
                        if (alive(c.url)) {
                            Prefs.clearFails(ctx, c.url);
                            revived++;
                        }
                    }
                } catch (Throwable ignored) { }
                running = false;
                if (cb != null) cb.done(tested, revived);
            }
        }, "repair").start();
    }

    /** Master playlist gerçekten oynatılabilir mi (varyantı da dene). */
    public static boolean alive(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(7000);
            c.setReadTimeout(7000);
            c.setInstanceFollowRedirects(true);
            c.setRequestProperty("User-Agent", Net.PLAYER_UA);
            c.setRequestProperty("Accept", "*/*");
            int code = c.getResponseCode();
            if (code != 200) { c.disconnect(); return false; }
            String body = Net.readText(c, 60000);
            c.disconnect();
            if (body == null) return false;
            if (body.contains("#EXT-X-STREAM-INF")) {
                // varyantlardan en az biri açılmalı
                String[] lines = body.split("\n");
                String base = url.substring(0, url.lastIndexOf('/') + 1);
                int denenen = 0;
                for (int i = 0; i < lines.length && denenen < 4; i++) {
                    String s = lines[i].trim();
                    if (s.length() == 0 || s.startsWith("#")) continue;
                    String v = s.startsWith("http") ? s : base + s;
                    if (!s.contains("?") && url.contains("?"))
                        v += "?" + url.substring(url.indexOf('?') + 1);
                    denenen++;
                    if (alive(v)) return true;
                }
                return false;
            }
            return body.contains("#EXTINF") && segmentOk(body, url);
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * M-29 (3. seviye): playlist "200" dönse bile segment sunucuda olmayabilir.
     * Son segmentin ilk baytlarını isteyip gerçekten veri geldiğini doğrular.
     */
    private static boolean segmentOk(String body, String playlistUrl) {
        HttpURLConnection c = null;
        try {
            String seg = null;
            String[] lines = body.split("\n");
            for (int i = lines.length - 1; i >= 0; i--) {
                String line = lines[i].trim();
                if (line.length() > 0 && !line.startsWith("#")) { seg = line; break; }
            }
            if (seg == null) return false;
            String base = playlistUrl.substring(0, playlistUrl.lastIndexOf('/') + 1);
            String segUrl = seg.startsWith("http") ? seg : base + seg;
            if (!seg.contains("?") && playlistUrl.contains("?"))
                segUrl += "?" + playlistUrl.substring(playlistUrl.indexOf('?') + 1);
            c = (HttpURLConnection) new URL(segUrl).openConnection();
            c.setConnectTimeout(7000);
            c.setReadTimeout(7000);
            c.setInstanceFollowRedirects(true);
            c.setRequestProperty("User-Agent", Net.PLAYER_UA);
            c.setRequestProperty("Range", "bytes=0-2047");
            int code = c.getResponseCode();
            if (code != 200 && code != 206) return false;
            java.io.InputStream in = c.getInputStream();
            byte[] buf = new byte[512];
            int n = in.read(buf);
            in.close();
            return n > 0;
        } catch (Throwable t) {
            return false;
        } finally {
            if (c != null) try { c.disconnect(); } catch (Exception ignored) { }
        }
    }
}
