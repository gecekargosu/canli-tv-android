package com.cengiz.canlitv;

/**
 * AdMob kimlikleri ve reklam kurallari (tek yerden yonetilir).
 *
 * Konsol durumu (23 Kasim 2026):
 *   - Hesap dogrulamasi: ONAYLANDI - "hesap bilgileriniz dogrulandi ve reklam sunumunuz etkinlestirildi"
 *   - Uygulama: UMAY Multi TV (Android) · 5042287034 · "Inceleme gerekli / sinirli reklam sunumu"
 *     (limiti kaldirmak icin Play magaza baglantisi gerekiyor)
 *   - app-ads.txt yayinda: https://umay-multi-tv-gq3zer.freebuff.site/app-ads.txt
 *
 * TEST_MODE: debug derlemede true -> Google'in resmi TEST birimleri kullanilir.
 * Kendi gercek reklamina tiklamak AdMob hesabini kapatir, bu yuzden test sart.
 */
public class Ads {

    /** Reklam SDK'si bagli mi? (Gradle + play-services-ads) */
    public static final boolean SDK_READY = true;

    /** Debug derlemede test reklamlari gosterilir (release'de false). */
    public static final boolean TEST_MODE = BuildConfig.DEBUG;

    /** Test cihazi kimligi (logcat'te "Test Device Id" olarak cikar). */
    public static final String TEST_DEVICE = "Z5R87DU8V4LBSOVG";

    /** AdMob uygulama kimligi (manifest meta-data APPLICATION_ID). */
    public static final String APP_ID = "ca-app-pub-7629051535337276~5042287034";

    // --- gercek birimler ---
    /** Kanal listesinin altinda banner (video ekraninda GOSTERILMEZ). */
    private static final String BANNER_REAL = "ca-app-pub-7629051535337276/6107956695";
    /** Kanal degistirirken tam ekran gecis reklami (frekans sinirli). */
    private static final String INTERSTITIAL_REAL = "ca-app-pub-7629051535337276/6126022588";
    /** "60 dakika reklamsiz izle" odullu reklami (kullanici kendi ister). */
    private static final String REWARDED_REAL = "ca-app-pub-7629051535337276/4812940915";

    // --- Google'in resmi test birimleri (debug) ---
    private static final String BANNER_TEST = "ca-app-pub-3940256099942544/6300978111";
    private static final String INTERSTITIAL_TEST = "ca-app-pub-3940256099942544/1033173712";
    private static final String REWARDED_TEST = "ca-app-pub-3940256099942544/5224354917";

    public static final String BANNER = TEST_MODE ? BANNER_TEST : BANNER_REAL;
    public static final String INTERSTITIAL = TEST_MODE ? INTERSTITIAL_TEST : INTERSTITIAL_REAL;
    public static final String REWARDED = TEST_MODE ? REWARDED_TEST : REWARDED_REAL;

    // --- politika kurallari (AdMob "yaniltici/zorlayici tik" ihlali olmasin diye) ---
    /** Art arda hızlı kanal değişiminde reklamlar üst üste binmesin (en az süre). */
    public static final long INTERSTITIAL_GAP_MS = 180L * 1000L;
    /** Kaç kanal değişiminde bir geçiş reklamı gösterilir (3 zap = 1 reklam). */
    public static final int ZAPS_BEFORE_INTERSTITIAL = 3;
    /** İzlerken kaç dakikada bir reklam çıkar (15 dakika). */
    public static final long AD_INTERVAL_MS = 15L * 60L * 1000L;
    /** Odullu reklam karsiligi reklamsiz sure (uygulama geneli, kanal bazli değil). */
    public static final int REWARD_MINUTES = 15;
    /** Radyoda kac dakika sonra "reklamsiz devam?" teklifi gosterilir (zorlamasiz). */
    public static final int ADFREE_OFFER_MINUTES = 40;

    /** Hata kaydı / destek e-postası (gizlilik politikasındaki adres). */
    public static final String SUPPORT_EMAIL = "cengizkilic1980@gmail.com";
    /** Uygulama acilisinda reklam gosterilmez (acilis reklami kapali). */
    public static final boolean APP_OPEN_AD = false;
}
