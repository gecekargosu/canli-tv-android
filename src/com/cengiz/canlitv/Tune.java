package com.cengiz.canlitv;

import android.content.Context;

import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy;

/**
 * 9.97 — zayıf Wi-Fi / yavaş ağ ayarları.
 *
 * İlk şikâyet "Wi-Fi'de takılma" üzerine yapılan ayarlar. Media3 varsayılanı:
 * soket okuma 8 sn, hata tekrarı 3. Zayıf bir ağda bir HLS parçası 8 sn'de
 * gelmeyince oynatıcı HATA veriyor, ekran "yeniden bağlanıyorum"a düşüyor ve
 * 60 sn'lik tampon çöpe gidiyordu. Bu sınıf:
 *  - soket beklemesini 25 sn'ye çıkarır (kısa boşluklar hata sayılmaz),
 *  - yükleme hatasını 6 kez kendi içinde tekrar eder (kullanıcı hiç görmez),
 *  - tamponu büyütür (dalgalanmayı yutar),
 *  - o anki ağ hızını ölçüp ekrana/loga yazar (ağ mı, uygulama mı ayırt etmek için).
 */
public class Tune {

    /** Soket bağlanma beklemesi. */
    private static final int CONNECT_MS = 12000;
    /** Parça okuma beklemesi: 8 sn çok kısaydı, yavaş ağda her seferinde hata veriyordu. */
    private static final int READ_MS = 25000;
    /** Aynı parçayı kaç kez sessizce tekrar denesin. */
    private static final int RETRIES = 6;

    private Tune() { }

    /** Oynatıcı HTTP katmanı: tarayıcı UA'sı + başlıklar + sabırlı zaman aşımları. */
    public static DefaultHttpDataSource.Factory http() {
        return new DefaultHttpDataSource.Factory()
                .setUserAgent(Net.PLAYER_UA)
                .setDefaultRequestProperties(Net.playerHeaders())
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(CONNECT_MS)
                .setReadTimeoutMs(READ_MS);
    }

    /** Veri kaynağını saran media source: hata tekrarı kullanıcıya yansımadan yapılır. */
    public static DefaultMediaSourceFactory source(DataSource.Factory ds) {
        try {
            return new DefaultMediaSourceFactory(ds)
                    .setLoadErrorHandlingPolicy(new DefaultLoadErrorHandlingPolicy(RETRIES));
        } catch (Throwable t) {
            return new DefaultMediaSourceFactory(ds);
        }
    }

    /**
     * Canlı video tamponu: 25 sn dolu tut, en fazla 90 sn ileri yükle.
     * Oynatma 3 sn'de başlar, takılmadan sonra 8 sn dolunca devam eder.
     */
    public static DefaultLoadControl liveVideo() {
        return new DefaultLoadControl.Builder()
                .setBufferDurationsMs(25000, 90000, 3000, 8000)
                .build();
    }

    /** Canlı radyo (ses) tamponu: ses küçük, daha uzun dayanıklılık mantıklı. */
    public static DefaultLoadControl liveAudio() {
        return new DefaultLoadControl.Builder()
                .setBufferDurationsMs(30000, 180000, 2500, 6000)
                .build();
    }

    /** O an ölçülen ağ hızı (bit/sn). 0 = henüz ölçüm yok. */
    public static long bps(Context c) {
        try {
            return DefaultBandwidthMeter.getSingletonInstance(c).getBitrateEstimate();
        } catch (Throwable t) {
            return 0;
        }
    }

    /** "2,4 Mbps" gibi okunur hız. Ölçüm yoksa "?". */
    public static String mbps(Context c) {
        long b = bps(c);
        if (b <= 0) return "?";
        double m = b / 1000000.0;
        return String.format(java.util.Locale.getDefault(), "%.1f Mbps", m);
    }
}
