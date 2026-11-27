package com.cengiz.canlitv;

/**
 * v10.09: StreamDiag sınıflandırmasının masaüstü testi (Android gerekmez).
 *
 *   javac -encoding UTF-8 -d _tmp/stdtest tools/StreamDiagTest.java src/com/cengiz/canlitv/StreamDiag.java
 *   java -cp _tmp/stdtest com.cengiz.canlitv.StreamDiagTest
 */
public final class StreamDiagTest {

    private static int hata = 0;
    private static int test = 0;

    public static void main(String[] a) {
        // --- internet yok: her şeyden önce gelir
        bekle("offline", StreamDiag.OFFLINE, StreamDiag.classify("ERROR_CODE_IO_NETWORK_CONNECTION_FAILED", "x", false));
        bekle("offline wins over 403", StreamDiag.OFFLINE, StreamDiag.classify("ERROR_CODE_IO_BAD_HTTP_STATUS", "response code: 403", false));

        // --- HTTP durum kodları
        bekle("403", StreamDiag.HTTP_403, StreamDiag.classify("ERROR_CODE_IO_BAD_HTTP_STATUS", "Invalid response code: 403", true));
        bekle("404", StreamDiag.HTTP_404, StreamDiag.classify("ERROR_CODE_IO_BAD_HTTP_STATUS", "Invalid response code: 404", true));
        bekle("410 → 404 sınıfı", StreamDiag.HTTP_404, StreamDiag.classify("ERROR_CODE_IO_BAD_HTTP_STATUS", "HTTP 410 Gone", true));
        bekle("429", StreamDiag.RATE_LIMIT, StreamDiag.classify("ERROR_CODE_IO_BAD_HTTP_STATUS", "Invalid response code: 429", true));
        bekle("503", StreamDiag.HTTP_5XX, StreamDiag.classify("ERROR_CODE_IO_BAD_HTTP_STATUS", "status code 503", true));
        bekle("401 → http", StreamDiag.HTTP_OTHER, StreamDiag.classify("ERROR_CODE_IO_BAD_HTTP_STATUS", "response code: 401", true));

        // --- ağ
        bekle("dns", StreamDiag.DNS, StreamDiag.classify("ERROR_CODE_IO_UNSPECIFIED", "Unable to resolve host \"cdn.x.com\": No address associated with hostname", true));
        bekle("timeout", StreamDiag.NET_TIMEOUT, StreamDiag.classify("ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT", "timeout", true));
        bekle("net", StreamDiag.NET_FAIL, StreamDiag.classify("ERROR_CODE_IO_NETWORK_CONNECTION_FAILED", "connect failed", true));
        bekle("io", StreamDiag.NET_FAIL, StreamDiag.classify("ERROR_CODE_IO_UNSPECIFIED", "generic", true));

        // --- codec / akış
        bekle("codec", StreamDiag.DECODER, StreamDiag.classify("ERROR_CODE_DECODER_INIT_FAILED", "init", true));
        bekle("codec2", StreamDiag.DECODER, StreamDiag.classify("ERROR_CODE_DECODING_FORMAT_UNSUPPORTED", "fmt", true));
        bekle("media", StreamDiag.BAD_MEDIA, StreamDiag.classify("ERROR_CODE_PARSING_CONTAINER_MALFORMED", "bad", true));
        bekle("drm", StreamDiag.DRM, StreamDiag.classify("ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED", "drm", true));
        bekle("canlı kenar", StreamDiag.LIVE_WINDOW, StreamDiag.classify("ERROR_CODE_BEHIND_LIVE_WINDOW", "behind", true));
        bekle("bilinmeyen", StreamDiag.UNKNOWN, StreamDiag.classify("ERROR_CODE_UNSPECIFIED", "?", true));
        bekle("null güvenli", StreamDiag.UNKNOWN, StreamDiag.classify(null, null, true));

        // --- metin / rapor kararı
        bekle("403 metni", true, StreamDiag.advice(StreamDiag.HTTP_403).length() > 0);
        bekle("her sınıfın başlığı var", true, basliklarDolu());
        bekle("internet yok → bildirilemez", false, StreamDiag.reportable(StreamDiag.OFFLINE));
        bekle("dns → bildirilemez", false, StreamDiag.reportable(StreamDiag.DNS));
        bekle("403 → bildirilebilir", true, StreamDiag.reportable(StreamDiag.HTTP_403));
        bekle("kod 403", "403", StreamDiag.code(StreamDiag.HTTP_403));
        bekle("kod offline", "offline", StreamDiag.code(StreamDiag.OFFLINE));

        System.out.println("----");
        System.out.println("test: " + test + " · hata: " + hata);
        System.out.println(hata == 0 ? "SONUC: TEMIZ" : "SONUC: HATALI");
        if (hata != 0) System.exit(1);
    }

    private static boolean basliklarDolu() {
        for (int k = 0; k <= 13; k++) {
            if (StreamDiag.title(k) == null || StreamDiag.title(k).length() == 0) return false;
            if (StreamDiag.advice(k) == null || StreamDiag.advice(k).length() == 0) return false;
            if (StreamDiag.code(k) == null || StreamDiag.code(k).length() == 0) return false;
        }
        return true;
    }

    private static void bekle(String ad, int beklenen, int gelen) { bekle(ad, beklenen, gelen, true); }

    private static void bekle(String ad, int beklenen, int gelen, boolean say) {
        test++;
        if (beklenen != gelen) {
            hata++;
            System.out.println("HATA  " + ad + " · beklenen=" + beklenen + " gelen=" + gelen);
        } else {
            System.out.println("ok    " + ad);
        }
    }

    private static void bekle(String ad, boolean beklenen, boolean gelen) {
        test++;
        if (beklenen != gelen) {
            hata++;
            System.out.println("HATA  " + ad + " · beklenen=" + beklenen + " gelen=" + gelen);
        } else {
            System.out.println("ok    " + ad);
        }
    }

    private static void bekle(String ad, String beklenen, String gelen) {
        test++;
        if (beklenen == null ? gelen != null : !beklenen.equals(gelen)) {
            hata++;
            System.out.println("HATA  " + ad + " · beklenen=" + beklenen + " gelen=" + gelen);
        } else {
            System.out.println("ok    " + ad);
        }
    }
}
