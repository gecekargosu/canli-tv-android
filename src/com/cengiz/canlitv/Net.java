package com.cengiz.canlitv;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.os.Process;

/** Mobil veri tespiti ve yayin basina internet kullanimi sayaci (uid bazli). */
public class Net {

    private static long sessionStart = 0;

    public static boolean mobile(Context c) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            Network n = cm.getActiveNetwork();
            if (n == null) return false;
            NetworkCapabilities cap = cm.getNetworkCapabilities(n);
            if (cap == null) return false;
            return cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean online(Context c) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            Network n = cm.getActiveNetwork();
            if (n == null) return false;
            NetworkCapabilities cap = cm.getNetworkCapabilities(n);
            return cap != null && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Exception e) {
            return false;
        }
    }

    private static long rx() {
        long v = TrafficStats.getUidRxBytes(Process.myUid());
        return v < 0 ? 0 : v;
    }

    /** Izleme basinda cagir. */
    public static void begin() {
        sessionStart = rx();
        if (sessionStart == 0) sessionStart = -1;
    }

    /** Devam eden oturumda simdiye kadar harcanan bayt. */
    public static long sessionSoFar() {
        if (sessionStart <= 0) return 0;
        long used = rx() - sessionStart;
        return used > 0 ? used : 0;
    }

    /** Izleme bitince: bu oturumda harcanan bayt. */
    public static long end() {
        if (sessionStart == -1) return 0;
        long now = rx();
        long used = now - sessionStart;
        sessionStart = 0;
        return used > 0 ? used : 0;
    }

    /**
     * Oynatıcı isteklerinde kullanılan başlıklar.
     *
     * Bazı yayıncı CDN'leri (ör. TV8/Daion) tarayıcı olmayan istemcilere canlı yayın yerine
     * "dijital reklam kuşağı" klibini veriyor. Android'in varsayılan MediaPlayer UA'sı bu
     * yüzden reklam karesinde takılı kalıyordu. Tarayıcı UA'sı + Accept başlığı ile gerçek
     * canlı yayın geliyor; bu yüzden bütün oynatıcılar bu başlıkları kullanır.
     */
    public static final String PLAYER_UA =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36";

    /**
     * 302/301 yönlendirmelerini kendisi takip eder ve son adresi döndürür.
     * Bazı CDN'ler (ör. DYG/Kral Pop) jetonlu adrese yönlendiriyor; Android
     * MediaPlayer bu zinciri çözemediği için kanal "açılamadı" görünüyordu.
     */
    public static String follow(String url) {
        if (url == null || !url.startsWith("http")) return url;
        String cur = url;
        try {
            for (int i = 0; i < 4; i++) {
                java.net.HttpURLConnection c =
                        (java.net.HttpURLConnection) new java.net.URL(cur).openConnection();
                c.setInstanceFollowRedirects(false);
                c.setConnectTimeout(6000);
                c.setReadTimeout(6000);
                c.setRequestProperty("User-Agent", PLAYER_UA);
                c.setRequestProperty("Accept", "*/*");
                int code = c.getResponseCode();
                String loc = c.getHeaderField("Location");
                c.disconnect();
                if (code >= 300 && code < 400 && loc != null && loc.length() > 0) {
                    cur = loc.startsWith("http") ? loc : new java.net.URL(new java.net.URL(cur), loc).toString();
                } else break;
            }
        } catch (Exception ignored) { }
        return cur;
    }

    public static java.util.Map<String, String> playerHeaders() {
        java.util.Map<String, String> h = new java.util.HashMap<String, String>();
        h.put("User-Agent", PLAYER_UA);
        h.put("Accept", "*/*");
        return h;
    }

    public static String mb(long bytes) {
        if (bytes < 1048576L) return (bytes / 1024) + " KB";
        if (bytes < 1073741824L) return String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / 1048576.0);
        return String.format(java.util.Locale.getDefault(), "%.2f GB", bytes / 1073741824.0);
    }
}
