package com.cengiz.canlitv;

/**
 * AdMob kimlikleri ve reklam kurallari (tek yerden yonetilir).
 *
 * NOT: Bu surumde reklam GOSTERILMIYOR cunku Google Mobile Ads SDK'si bir Gradle
 * bagimliligidir (com.google.android.gms:play-services-ads) ve bu proje Gradle'siz
 * (aapt2 + javac + d8) derleniyor. Asagidaki kimlikler AdMob konsolunda olusturuldu;
 * Gradle gecisi yapildiginda SDK'yi baglayip sadece bu siniftaki degerleri kullanmak yeterli.
 *
 * Konsol durumu (23 Kasim 2026):
 *   - Uygulama: UMAY Multi TV (Android) · 5042287034 · "Inceleme gerekli"
 *   - Hesap dogrulamasi: bekliyor (Google 24 saat - 2 hafta diyor)
 *   - Magazada listeli degil: Play'de uygulama yayinlanana kadar reklam sunumu sinirli
 */
public class Ads {

    /** AdMob uygulama kimligi (manifest meta-data APPLICATION_ID). */
    public static final String APP_ID = "ca-app-pub-7629051535337276~5042287034";

    /** Kanal listesinin altinda banner (video ekraninda GOSTERILMEZ). */
    public static final String BANNER = "ca-app-pub-7629051535337276/6107956695";

    /** Kanal degistirirken tam ekran gecis reklami (frekans sinirli). */
    public static final String INTERSTITIAL = "ca-app-pub-7629051535337276/6126022588";

    /** "60 dakika reklamsiz izle" odullu reklami (kullanici kendi ister). */
    public static final String REWARDED = "ca-app-pub-7629051535337276/4812940915";

    /** SDK baglandiginda true yapilir; o zamana kadar hicbir reklam kodu calismaz. */
    public static final boolean SDK_READY = false;

    // --- politika kurallari (AdMob "yaniltici/zorlayici tik" ihlali olmasin diye) ---
    /** Iki gecis reklami arasi en az sure. */
    public static final long INTERSTITIAL_GAP_MS = 10L * 60L * 1000L;
    /** Kac kanal degisiminden sonra bir gecis reklami. */
    public static final int ZAPS_BEFORE_INTERSTITIAL = 4;
    /** Odullu reklam karsiligi reklamsiz sure. */
    public static final int REWARD_MINUTES = 60;
    /** Uygulama acilisinda reklam gosterilmez (acilis reklami kapali). */
    public static final boolean APP_OPEN_AD = false;
}
