package com.cengiz.canlitv;

/**
 * v10.09: Yayın hatalarını TEK yerde sınıflandırır.
 *
 * ChatGPT'nin "Diagnostic Engine" planının gerçekten faydalı kısmı: aynı hata kodunu
 * uygulamanın dört ayrı yerinde farklı yorumlamak yerine burada bir kez karara bağlıyoruz.
 * AI yok — 20 satır kural, 0 ms, çevrimdışı çalışır (bkz. docs/AI-DEGERLENDIRME).
 *
 * Android'e BAĞIMLI DEĞİLDİR (bilerek): girdi, PlaybackException'ın hata kodunun ADI ve
 * mesaj metnidir. Böylece tools/StreamDiagTest.java ile masaüstünde javac+java ile
 * test edilebilir.
 */
public final class StreamDiag {

    public static final int UNKNOWN = 0;
    /** Cihazın interneti yok — kanalın suçu değil. */
    public static final int OFFLINE = 1;
    /** İnternet var ama ad çözümlenemedi (DNS) — kanalın suçu değil. */
    public static final int DNS = 2;
    public static final int NET_TIMEOUT = 3;
    public static final int NET_FAIL = 4;
    public static final int HTTP_403 = 5;
    public static final int HTTP_404 = 6;
    public static final int HTTP_5XX = 7;
    public static final int HTTP_OTHER = 8;
    public static final int DECODER = 9;
    public static final int BAD_MEDIA = 10;
    public static final int DRM = 11;
    public static final int RATE_LIMIT = 12;
    public static final int LIVE_WINDOW = 13;

    private StreamDiag() { }

    /**
     * Hatayı sınıflandırır.
     *
     * @param errorName PlaybackException.getErrorCodeName() (örn. "ERROR_CODE_IO_BAD_HTTP_STATUS")
     * @param message   PlaybackException.getMessage() (HTTP durum kodu buradadır)
     * @param online    cihazın o an interneti var mı
     */
    public static int classify(String errorName, String message, boolean online) {
        if (!online) return OFFLINE;
        String n = up(errorName);
        String m = up(message);
        if (isDns(m)) return DNS;
        int http = httpStatus(m);
        if (http == 403) return HTTP_403;
        if (http == 404 || http == 410) return HTTP_404;
        if (http == 429) return RATE_LIMIT;
        if (http >= 500 && http <= 599) return HTTP_5XX;
        if (http > 0) return HTTP_OTHER;
        if (n.contains("BEHIND_LIVE_WINDOW")) return LIVE_WINDOW;
        if (n.contains("DRM")) return DRM;
        if (n.contains("DECODER") || n.contains("DECODING") || n.contains("CODEC")) return DECODER;
        if (n.contains("PARSING") || n.contains("CONTAINER") || n.contains("MALFORMED")
                || n.contains("UNSUPPORTED")) return BAD_MEDIA;
        if (n.contains("TIMEOUT")) return NET_TIMEOUT;
        if (n.contains("NETWORK") || n.contains("IO_") || n.contains("REMOTE")) return NET_FAIL;
        return UNKNOWN;
    }

    /** Kanala yazılacak kısa sebep etiketi (günlük ve sunucu için). Örn. "403". */
    public static String code(int kind) {
        switch (kind) {
            case OFFLINE: return "offline";
            case DNS: return "dns";
            case NET_TIMEOUT: return "timeout";
            case NET_FAIL: return "net";
            case HTTP_403: return "403";
            case HTTP_404: return "404";
            case HTTP_5XX: return "5xx";
            case HTTP_OTHER: return "http";
            case DECODER: return "codec";
            case BAD_MEDIA: return "media";
            case DRM: return "drm";
            case RATE_LIMIT: return "429";
            case LIVE_WINDOW: return "live-window";
            default: return "?";
        }
    }

    /** Kullanıcıya gösterilecek başlık — Türkçe metin (I18n anahtarı), tabloda yoksa Türkçe kalır. */
    public static String title(int kind) {
        switch (kind) {
            case OFFLINE: return "İnternet bağlantısı yok";
            case DNS: return "Kanal adresi bulunamadı (DNS)";
            case NET_TIMEOUT: return "Yayın zamanında yanıt vermedi";
            case NET_FAIL: return "Bağlantı kurulamadı";
            case HTTP_403: return "Yayıncı bu yayını engelliyor (403)";
            case HTTP_404: return "Kanal adresi değişmiş olabilir (404)";
            case HTTP_5XX: return "Yayıncının sunucusu şu an arızalı";
            case HTTP_OTHER: return "Yayın sunucusu isteği reddetti";
            case DECODER: return "Bu kanalın görüntü formatı cihazında desteklenmiyor";
            case BAD_MEDIA: return "Yayın akışı bozuk geldi";
            case DRM: return "Bu yayın korumalı (DRM), oynatılamıyor";
            case RATE_LIMIT: return "Yayıncı çok istek aldı, geçici olarak kapattı (429)";
            case LIVE_WINDOW: return "Canlı yayının gerisinde kalındı";
            default: return "Kanal açılamadı";
        }
    }

    /** Tek satır öneri — Türkçe metin (I18n anahtarı). */
    public static String advice(int kind) {
        switch (kind) {
            case OFFLINE: return "Wi-Fi / mobil veriyi kontrol et";
            case DNS: return "İnternet var ama adres çözümlenemedi · biraz sonra tekrar dene";
            case NET_TIMEOUT:
            case NET_FAIL: return "Bağlantı yavaş olabilir · tekrar dene";
            case HTTP_403:
            case HTTP_404:
            case HTTP_5XX:
            case HTTP_OTHER:
            case RATE_LIMIT: return "Kanal kaynaklı · biraz sonra tekrar dene veya başka kanal dene";
            case DECODER: return "Cihaz bu formatı açamıyor · başka kanal dene";
            case BAD_MEDIA: return "Yayıncı bozuk akış veriyor · başka kanal dene";
            case DRM: return "Bu yayın uygulamada izlenemez";
            default: return "Tekrar dene";
        }
    }

    /** Bu sınıf için "kanal çalışmıyor" bildirimi anlamlı mı? (İnternet/DNS kanalın suçu değil.) */
    public static boolean reportable(int kind) {
        return kind != OFFLINE && kind != DNS;
    }

    // ------------------------------------------------------------------ yardımcılar

    private static String up(String s) {
        return s == null ? "" : s.toUpperCase(java.util.Locale.ROOT);
    }

    private static boolean isDns(String m) {
        return m.contains("UNABLE TO RESOLVE HOST") || m.contains("UNKNOWNHOST")
                || m.contains("NO ADDRESS ASSOCIATED") || m.contains("NODENAME NOR SERVNAME")
                || m.contains("NAME OR SERVICE NOT KNOWN");
    }

    /**
     * Metindeki HTTP durum kodunu bulur ("Response code: 403", "HTTP 404"…).
     * 400-599 arası ilk üç haneli sayı durum kodudur; bulunamazsa 0.
     */
    static int httpStatus(String m) {
        if (m == null) return 0;
        int i = 0;
        while (i < m.length()) {
            if (!Character.isDigit(m.charAt(i))) { i++; continue; }
            int j = i;
            while (j < m.length() && Character.isDigit(m.charAt(j))) j++;
            if (j - i == 3) {
                int v = Integer.parseInt(m.substring(i, j));
                if (v >= 400 && v <= 599) return v;
            }
            i = j;
        }
        return 0;
    }
}
