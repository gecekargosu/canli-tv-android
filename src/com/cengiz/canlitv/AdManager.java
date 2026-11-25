package com.cengiz.canlitv;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Collections;

/**
 * Reklam yonetimi (tek yer). Kurallar Ads.java icinde.
 *
 * - Banner: her ekranda, listenin/oynaticinin ALTINDA, her zaman gorunur.
 * - Gecis reklami: yalniz kanal degisiminde (her 3. zap, en az 180 sn).
 *   Izlerken tam ekran reklam GOSTERILMEZ (9.88 / K-02): mağaza metni "oynatma
 *   reklamla kesilmez" diyor, kod da buna uyar.
 * - Reklamsiz sure kanal bazli DEGIL: uygulama geneli (odullu video ile 15 dk).
 * - Reklamsiz sure aktifse gecis reklami gosterilmez; banner yine kalir.
 * - Reklam yuklenemezse uygulama normal akisina devam eder.
 */
public final class AdManager {

    private static final String TAG = "UmayAds";

    // --- M-03: tek reklam izin kapısı -------------------------------------------------
    /** Reklam istenebilir mi? Başlangıçta HAYIR (fail-closed). */
    private static volatile boolean adsAllowed = false;
    /** Sahip modunda "reklamları kapat" açık mı? (yalnız bu cihaz) */
    private static volatile boolean adsOffFlag = false;
    /** Rıza gelmeden oluşturulan banner'lar; izin gelince yüklenir. */
    private static final java.util.List<Runnable> pendingBanners =
            new java.util.ArrayList<Runnable>();

    /** Reklam istenebilir mi? Diğer sınıflar da bunu kullanır. */
    public static boolean adsAllowed() {
        return adsAllowed && !adsOffFlag;
    }

    private static boolean inited = false;
    private static InterstitialAd interstitial = null;
    private static boolean loadingInterstitial = false;
    private static RewardedAd rewarded = null;
    private static boolean loadingRewarded = false;
    private static long lastInterstitial = 0L;
    private static int opens = 0;
    private static boolean showingAd = false;   // ayni anda iki tam ekran reklam acilmasin
    // Yükleme denemeleri arasında en az bekleme: eskiden saniyede bir istek gidiyordu
    // (oynatıcı her saniye reklam durumunu soruyordu, reklam yoksa yeniden yüklüyordu).
    private static long nextInterstitialTry = 0L;
    private static long nextRewardedTry = 0L;
    private static long interstitialBackoff = 30000L;
    private static long rewardedBackoff = 30000L;
    private static String lastInterstitialError = "";
    private static String lastRewardedError = "";
    private static String lastBannerError = "";
    /** Ekranda duran banner'lar: "reklamları kapat" açılınca hemen kaldırılır. */
    private static final java.util.List<java.lang.ref.WeakReference<View>> liveBanners =
            new java.util.ArrayList<java.lang.ref.WeakReference<View>>();

    /** Tam ekran reklam (geçiş/zamanlayıcı) açık mı? Oynatıcı bu sırada beklemede kalır. */
    public static boolean isShowingFullScreen() { return showingAd; }

    private AdManager() { }

    /**
     * Reklamsız süre aktif mi? v9.25: süre artık **uygulama geneli**dir; kanal başına ayrı
     * reklamsız pencere yok (kullanıcı her kanalda yeniden reklam sorusu görmesin).
     */
    public static boolean isAdFree(Context c) {
        return System.currentTimeMillis() < Prefs.adFreeUntil(c);
    }

    /** Eski imza korunur: parametre yok sayılır, süre uygulama genelidir. */
    public static boolean isAdFreeFor(Context c, String key) {
        return isAdFree(c);
    }

    /** Reklamsiz kalan dakika (0 = yok). */
    public static int adFreeMinutesLeft(Context c) {
        long ms = Prefs.adFreeUntil(c) - System.currentTimeMillis();
        if (ms <= 0) return 0;
        return (int) Math.ceil(ms / 60000.0);
    }

    /** Test modu değişince RequestConfiguration'ın yeniden uygulanması için. */
    public static void resetForTestMode() { inited = false; }

    public static void init(final Activity activity) {
        if (!Ads.SDK_READY) return;
        // Sahip modunda reklamlar kapatıldıysa SDK'ya hiç istek gitmez.
        adsOffFlag = Prefs.adsOff(activity);
        if (adsOffFlag) {
            adsAllowed = false;
            inited = true;
            Log.i(TAG, "reklamlar kapalı (sahip modu) — istek gönderilmeyecek");
            return;
        }
        if (inited) return;
        inited = true;
        try {
            // Aile dostu: yetiskin/erotik icerik istenmez. PG secildi cunku G
            // neredeyse hic reklam doldurmuyor (gelir kaybi), PG hala aile sinirinda.
            RequestConfiguration.Builder rc = new RequestConfiguration.Builder()
                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
                    .setTagForChildDirectedTreatment(
                            RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
                    .setTagForUnderAgeOfConsent(
                            RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED);
            // Test modu yalnız debug derlemede. Release APK'da kullanıcı test moduna
            // geçemez: test reklamı para yazmadığı için gelir sessizce sıfırlanıyordu.
            if (Ads.TEST_MODE) {
                java.util.List<String> ids = new java.util.ArrayList<String>();
                if (Ads.TEST_DEVICE != null && Ads.TEST_DEVICE.length() > 0) ids.add(Ads.TEST_DEVICE);
                String h = Prefs.adTestHashed(activity);
                if (h != null && h.length() > 0) ids.add(h);
                rc.setTestDeviceIds(ids);
            }
            MobileAds.setRequestConfiguration(rc.build());

            // --- P0-3: AB/UK/CH rıza ekranı (UMP). Rıza yoksa reklam istenmez. ---
            com.google.android.ump.ConsentInformation ci =
                    com.google.android.ump.UserMessagingPlatform.getConsentInformation(activity);
            com.google.android.ump.ConsentRequestParameters.Builder params =
                    new com.google.android.ump.ConsentRequestParameters.Builder();
            if (Ads.TEST_MODE) {
                // Test cihazında rıza formunu görebilmek için (yalnız test modunda)
                try {
                    params.setConsentDebugSettings(
                            new com.google.android.ump.ConsentDebugSettings.Builder(activity)
                                    .setDebugGeography(com.google.android.ump.ConsentDebugSettings
                                            .DebugGeography.DEBUG_GEOGRAPHY_EEA)
                                    .addTestDeviceHashedId(Ads.TEST_DEVICE)
                                    .build());
                } catch (Throwable ignored) { }
            }
            ci.requestConsentInfoUpdate(activity, params.build(),
                    new com.google.android.ump.ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                        @Override public void onConsentInfoUpdateSuccess() {
                            com.google.android.ump.UserMessagingPlatform
                                    .loadAndShowConsentFormIfRequired(activity,
                                            new com.google.android.ump.ConsentForm.OnConsentFormDismissedListener() {
                                                @Override public void onConsentFormDismissed(
                                                        com.google.android.ump.FormError error) {
                                                    startAds(activity);
                                                }
                                            });
                        }
                    },
                    new com.google.android.ump.ConsentInformation.OnConsentInfoUpdateFailureListener() {
                        @Override public void onConsentInfoUpdateFailure(com.google.android.ump.FormError error) {
                            Log.w(TAG, "riza bilgisi alinamadi: " + error);
                            startAds(activity);
                        }
                    });
        } catch (Throwable t) {
            Log.w(TAG, "baslatma hatasi: " + t);
            startAds(activity);
        }
    }

    /** Rıza varsa SDK'yı başlatır (yoksa reklam istenmez). */
    private static void startAds(final Activity activity) {
        // M-03: rıza bilgisi alınamazsa reklam İSTEME (fail-closed).
        boolean can = false;
        try {
            can = com.google.android.ump.UserMessagingPlatform
                    .getConsentInformation(activity).canRequestAds();
        } catch (Throwable t) {
            Log.w(TAG, "riza durumu okunamadi, reklam istenmeyecek: " + t);
        }
        adsAllowed = can;
        if (!can) {
            Log.i(TAG, "riza yok — reklam istenmeyecek");
            return;
        }
        try {
            MobileAds.initialize(activity, new OnInitializationCompleteListener() {
                @Override public void onInitializationComplete(InitializationStatus status) {
                    Log.i(TAG, "SDK hazir");
                    loadInterstitial(activity);
                    loadRewarded(activity);
                    flushPendingBanners(activity);
                }
            });
        } catch (Throwable t) {
            Log.w(TAG, "SDK baslatma: " + t);
        }
    }

    /** Rıza geldikten sonra bekleyen banner'ları yükler (ana iş parçacığında). */
    private static void flushPendingBanners(final Activity activity) {
        try {
            activity.runOnUiThread(new Runnable() {
                @Override public void run() {
                    java.util.List<Runnable> copy;
                    synchronized (pendingBanners) {
                        copy = new java.util.ArrayList<Runnable>(pendingBanners);
                        pendingBanners.clear();
                    }
                    for (Runnable r : copy) {
                        try { r.run(); } catch (Throwable ignored) { }
                    }
                }
            });
        } catch (Throwable ignored) { }
    }

    /** Ayarlar → "Reklam gizlilik tercihleri": rıza formunu yeniden açar. */
    public static boolean hasPrivacyOptions(final Activity a) {
        try {
            return com.google.android.ump.UserMessagingPlatform.getConsentInformation(a)
                    .getPrivacyOptionsRequirementStatus()
                    == com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void showPrivacyOptions(final Activity a) {
        try {
            com.google.android.ump.UserMessagingPlatform.showPrivacyOptionsForm(a,
                    new com.google.android.ump.ConsentForm.OnConsentFormDismissedListener() {
                        @Override public void onConsentFormDismissed(com.google.android.ump.FormError error) {
                            Toast.makeText(a, "Tercihler kaydedildi", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Throwable t) {
            Log.w(TAG, "gizlilik tercihleri acilamadi: " + t);
        }
    }

    /**
     * Her ekranin altina "veri gostergesi + banner" koyar: activity.setContentView yerine
     * AdManager.wrap(this, icerik) cagrilir. Icerik kalan yuksekligi alir, banner en altta durur
     * (videonun uzerine binmez). Reklamsiz surede banner eklenmez, veri gostergesi kalir.
     */
    public static void wrap(final Activity activity, View content) {
        wrap(activity, content, null);
    }

    /** key: bu ekranda gecerli kanal/istasyon adresi (reklamsiz serit icin). */
    public static void wrap(final Activity activity, View content, final String key) {
        wrap(activity, content, key, Prefs.showUsage(activity));
    }

    /**
     * usage=false: bu ekran kendi veri şeridini gösteriyor (ana ekran) —
     * aynı bilgi iki yerde yazılmasın.
     */
    public static void wrap(final Activity activity, View content, final String key,
                            final boolean usage) {
        try {
            LinearLayout holder = new LinearLayout(activity);
            holder.setOrientation(LinearLayout.VERTICAL);
            holder.setBackgroundColor(Color.parseColor("#060B14"));
            content.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            holder.addView(content);
            if (usage) holder.addView(usageBar(activity));
            View ad = banner(activity);
            if (ad != null) holder.addView(ad);
            // sistem gezinme cubugu (jest cubugu) banner'in uzerine binmesin:
            // ekranin altina cubuk yuksekligi kadar bosluk birak
            holder.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override public android.view.WindowInsets onApplyWindowInsets(View v,
                                                                                  android.view.WindowInsets insets) {
                    // P0-6: Android 15/16 zorunlu edge-to-edge → üst barı çentiğin/status barın
                    // altında bırakma. Alt inset de sistem çubuğu için.
                    int top = insets.getSystemWindowInsetTop();
                    int bottom = insets.getSystemWindowInsetBottom();
                    // M-78: bazı cihazlarda (MIUI, jest çubuğu) inset 0 geliyor ve banner
                    // gezinme çubuğunun altında kalıyordu. Sistemin bildirdiği çubuk
                    // yüksekliğini de alt sınır olarak kullan.
                    try {
                        int id = v.getResources().getIdentifier(
                                "navigation_bar_height", "dimen", "android");
                        if (id > 0) {
                            bottom = Math.max(bottom,
                                    v.getResources().getDimensionPixelSize(id));
                        }
                    } catch (Throwable ignored) { }
                    if (top > 0 || bottom > 0) v.setPadding(0, top, 0, bottom);
                    return insets;
                }
            });
            activity.setContentView(holder);
        } catch (Throwable t) {
            Log.w(TAG, "wrap hatasi: " + t);
            activity.setContentView(content);
        }
    }

    private static int dp(Activity a, int v) {
        return (int) (v * a.getResources().getDisplayMetrics().density);
    }

    /** Ayarlar ve ana ekranla aynı biçim (Türkçe ondalık virgül) — #155. */
    private static String human(long bytes) {
        return Net.mb(bytes);
    }

    /** Ekranin altinda kalan kucuk veri satiri: bugun toplam ne kadar internet harcandi. */
    private static TextView usageBar(final Activity a) {
        final TextView t = new TextView(a);
        t.setTextSize(11f);
        t.setTextColor(Color.parseColor("#8FA0B5"));
        t.setPadding(dp(a, 10), dp(a, 4), dp(a, 10), dp(a, 4));
        t.setText("📶 Bugün kullanılan veri: " + human(Prefs.todayBytes(a)));
        final Handler h = new Handler(Looper.getMainLooper());
        final Runnable r = new Runnable() {
            @Override public void run() {
                if (!t.isAttachedToWindow()) return;
                t.setText("📶 Bugün kullanılan veri: " + human(Prefs.todayBytes(a)));
                h.postDelayed(this, 5000);
            }
        };
        h.postDelayed(r, 5000);
        return t;
    }

    /**
     * Liste/oynatici altindaki banner. Reklamsiz surede veya SDK yoksa null doner.
     * Reklam dolmazsa (no-fill / internet yok) yerine **kendi tanitim barimiz** gosterilir;
     * boylece ekranin altinda bosluk kalmaz.
     */
    public static View banner(final Activity activity) {
        // Banner her zaman gosterilir: 20 dk reklamsiz sure yalnizca video (gecis) reklamini kaldirir.
        if (!Ads.SDK_READY) return null;
        adsOffFlag = Prefs.adsOff(activity);
        if (adsOffFlag) return null;
        // 9.82: reklamsız abone ise hiçbir banner/şerit gösterilmez.
        if (Prefs.subActive(activity)) return null;
        try {
            final android.widget.FrameLayout holder = new android.widget.FrameLayout(activity);
            holder.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            // Yükseklik her zaman dursun: reklam yüklenirken/yüklenemezken bar kaybolmasın.
            holder.setMinimumHeight(dp(activity, 56));
            holder.setBackgroundColor(Color.parseColor("#0B1220"));
            liveBanners.add(new java.lang.ref.WeakReference<View>(holder));

            final TextView house = houseBar(activity);
            // M-43: şerit banner alanına yapışıktı; banner yüklenirken/başarısızken
            // kullanıcı banner yerine şeride dokunup farkında olmadan reklam açıyordu.
            android.widget.FrameLayout.LayoutParams houseLp =
                    new android.widget.FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            houseLp.topMargin = dp(activity, 6);
            holder.addView(house, houseLp);

            // M-03: rıza yoksa reklam isteği GÖNDERİLMEZ. Yerine kendi tanıtım barımız kalır;
            // rıza sonradan gelirse AdManager bekleyen banner'ı yükler.
            final Runnable loader = new Runnable() {
                @Override public void run() {
                    if (!adsAllowed || holder.getChildCount() == 0) return;
                    try {
                        final AdView view = new AdView(activity);
                        view.setAdUnitId(Ads.BANNER);
                        float density = activity.getResources().getDisplayMetrics().density;
                        int widthDp = (int) (activity.getResources()
                                .getDisplayMetrics().widthPixels / density);
                        AdSize size = AdSize
                                .getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp);
                        view.setAdSize(size != null ? size : AdSize.BANNER);
                        holder.addView(view, 0, new android.widget.FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                size != null ? size.getHeightInPixels(activity) : dp(activity, 56)));
                        // Kendi barımız reklam GERÇEKTEN yükleninceye kadar kalsın.
                        house.setVisibility(View.VISIBLE);
                        view.setAdListener(new com.google.android.gms.ads.AdListener() {
                            @Override public void onAdLoaded() {
                                house.setVisibility(View.GONE);
                                view.setVisibility(View.VISIBLE);
                            }

                            @Override public void onAdImpression() {
                                AdStats.note(activity, "banner");
                            }

                            @Override public void onAdFailedToLoad(LoadAdError error) {
                                // Etiket yanlıştı: banner hatası "geçiş reklamı" diye yazılıyordu.
                                lastBannerError = reason(error);
                                // Aynı hata her ekranda tekrar yazılmasın: 30 dakikada bir kaydet.
                                CrashLog.eventThrottled(activity, "ad", "banner",
                                        "banner yüklenemedi: " + reason(error), 30L * 60L * 1000L);
                                Log.w(TAG, "banner dolmadi, kendi barimiz: " + error.getMessage());
                                view.setVisibility(View.GONE);
                                house.setVisibility(View.VISIBLE);
                            }
                        });
                        view.loadAd(new AdRequest.Builder().build());
                    } catch (Throwable t) {
                        Log.w(TAG, "banner yuklenemedi: " + t);
                        house.setVisibility(View.VISIBLE);
                    }
                }
            };

            if (adsAllowed) {
                loader.run();
            } else {
                synchronized (pendingBanners) {
                    if (pendingBanners.size() < 8) pendingBanners.add(loader);
                }
            }
            return holder;
        } catch (Throwable t) {
            Log.w(TAG, "banner hatasi: " + t);
            return null;
        }
    }

    /** Reklam dolmadiginda gosterilen kendi barimiz (bos yer birakmamak icin). */
    private static TextView houseBar(final Activity a) {
        TextView t = new TextView(a);
        t.setTextSize(12f);
        // 9.77: şerit sarıydı, uygulamanın geri kalanı turuncu (ACCENT).
        t.setTextColor(Color.WHITE);
        t.setBackgroundColor(Color.parseColor("#FF7A18"));
        t.setPadding(dp(a, 12), dp(a, 10), dp(a, 12), dp(a, 10));
        t.setGravity(android.view.Gravity.CENTER);
        t.setText("▶  Reklam izle · " + Ads.REWARD_MINUTES + " dk reklamsız");
        t.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showRewarded(a, new Runnable() {
                    @Override public void run() {
                        // M-42: recreate() yayını/ekranı baştan başlatıyordu; şerit yerinde güncellenir.
                        t.setText("✅ Reklamsız · " + adFreeMinutesLeft(a) + " dk");
                        android.widget.Toast.makeText(a,
                                "Teşekkürler! " + Ads.REWARD_MINUTES + " dakika reklamsız.",
                                android.widget.Toast.LENGTH_LONG).show();
                    }
                }, new Runnable() {
                    @Override public void run() {
                        android.widget.Toast.makeText(a,
                                "Reklam şu an hazır değil, biraz sonra tekrar dene",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return t;
    }

    /**
     * Kanal/radyo acilmadan once cagrilir: sira gelen gecis reklami varsa gosterir,
     * yoksa isi hemen calistirir. Reklamsiz surede hic beklemez.
     */
    public static void maybeInterstitial(final Activity activity, final Runnable then) {
        maybeInterstitial(activity, null, then);
    }

    /** Kanal bazli: key (URL) icin 20 dk reklamsiz sure varsa gecis reklami gosterilmez. */
    public static void maybeInterstitial(final Activity activity, final String key,
                                         final Runnable then) {
        if (!Ads.SDK_READY || !adsAllowed || isAdFree(activity) || showingAd
                || Prefs.subActive(activity)) {
            then.run();
            return;
        }
        long now = System.currentTimeMillis();
        // M-44: ilk kanal açılışında reklam gösterilmez; sonraki açılışlarda normal akış.
        int opens = Prefs.channelOpens(activity);
        Prefs.setChannelOpens(activity, opens + 1);
        if (opens < 1) {
            then.run();
            return;
        }
        // Aynı anda/art arda zap yapılırsa reklamlar üst üste binmesin.
        if (now - lastInterstitial < Ads.INTERSTITIAL_GAP_MS) {
            then.run();
            return;
        }
        // PDF-05: her zap'ta reklam cikmasin; kac zap'ta bir gosterilecegi Ads sabitinde.
        if (opens % Ads.ZAPS_BEFORE_INTERSTITIAL != 0) {
            then.run();
            return;
        }
        final InterstitialAd ad = interstitial;
        if (ad == null) {
            loadInterstitial(activity);
            then.run();
            return;
        }
        interstitial = null;
        lastInterstitial = now;
        Prefs.setLastAdAt(activity, now);
        showingAd = true;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            // Sayaç yalnız gerçekten gösterildiğinde artar: eskiden ad.show() çağrısında
            // sayılıyordu, doluluk yokken/uygulama öndeyken de "reklam gösterildi" yazıyordu.
            @Override public void onAdImpression() { AdStats.note(activity, "gecis"); }

            @Override public void onAdDismissedFullScreenContent() {
                showingAd = false;
                loadInterstitial(activity);
                then.run();
            }

            @Override public void onAdFailedToShowFullScreenContent(AdError error) {
                showingAd = false;
                loadInterstitial(activity);
                then.run();
            }
        });
        try {
            ad.show(activity);
            // Radyo arkada çalıyorsa ses odağı reklama geçer; reklam kapanınca radyo kendiliğinden döner.
            toastRadioHint(activity);
        } catch (Throwable t) {
            showingAd = false;
            Log.w(TAG, "gecis reklami gosterilemedi: " + t);
            loadInterstitial(activity);
            then.run();
        }
    }

    /** Reklam açılırken radyo dinleyicisine ne olacağını söyle (ses kesilmesi kafa karıştırmasın). */
    private static void toastRadioHint(final Activity activity) {
        try {
            if (AudioService.isPlayingStatic() || AudioService.isConnectingStatic()) {
                android.widget.Toast.makeText(activity,
                        "Reklam sırasında radyo duraklatıldı · reklamı kapatınca devam eder",
                        android.widget.Toast.LENGTH_LONG).show();
            }
        } catch (Throwable ignored) { }
    }

    /** "15 dakika reklamsız" odullu reklami hazir mi? */
    public static boolean rewardedReady() {
        return Ads.SDK_READY && adsAllowed && rewarded != null;
    }

    /**
     * Odullu reklami gosterir. Kullanici sonuna kadar izlerse onReward calisir ve
     * reklamsiz sure baslar. Hazir degilse onFail calisir (kullaniciya bilgi verilir).
     */
    public static void showRewarded(final Activity activity, final Runnable onReward,
                                    final Runnable onFail) {
        showRewarded(activity, null, onReward, onFail);
    }

    /** key = hangi kanal/istasyon icin reklamsiz sure verilecek. */
    public static void showRewarded(final Activity activity, final String key,
                                    final Runnable onReward, final Runnable onFail) {
        if (Prefs.subActive(activity)) {   // abone zaten reklamsız
            onFail.run();
            return;
        }
        final RewardedAd ad = rewarded;
        if (!Ads.SDK_READY || !adsAllowed || ad == null) {
            loadRewarded(activity);
            onFail.run();
            return;
        }
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                loadRewarded(activity);
            }

            @Override public void onAdFailedToShowFullScreenContent(AdError error) {
                loadRewarded(activity);
            }
        });
        rewarded = null;
        try {
            ad.show(activity, new com.google.android.gms.ads.OnUserEarnedRewardListener() {
                @Override public void onUserEarnedReward(
                        com.google.android.gms.ads.rewarded.RewardItem item) {
                    // Ödül gerçekten kazanıldı: erken kapatılan ödüllü reklam gelir yazmaz.
                    AdStats.note(activity, "odullu");
                    long until = System.currentTimeMillis() + Ads.REWARD_MINUTES * 60000L;
                    Prefs.setAdFreeUntil(activity, until);
                    onReward.run();
                }
            });
        } catch (Throwable t) {
            Log.w(TAG, "odullu reklam gosterilemedi: " + t);
            loadRewarded(activity);
            onFail.run();
        }
    }

    /** AdMob hata kodunu kullanıcıya/dosyaya anlaşılır biçimde yazar. */
    private static String reason(LoadAdError e) {
        if (e == null) return "bilinmeyen hata";
        int code = e.getCode();
        String what;
        switch (code) {
            case 1: what = "geçersiz istek (birim kimliği/ayar)"; break;
            case 2: what = "ağ hatası (internet yok veya zayıf)"; break;
            case 3: what = "NO_FILL — Google şu an bu birime reklam vermiyor"; break;
            case 0: what = "SDK iç hatası"; break;
            case 4: what = "reklam henüz hazır değil"; break;
            default: what = "hata"; break;
        }
        String msg = e.getMessage();
        if (msg != null && msg.length() > 160) msg = msg.substring(0, 160);
        return code + " " + what + (msg == null || msg.length() == 0 ? "" : " · " + msg);
    }

    private static void retryLater(final Activity a, final boolean rewardedType, long delay) {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override public void run() {
                    try {
                        if (a == null || a.isFinishing()) return;
                        if (rewardedType) loadRewarded(a); else loadInterstitial(a);
                    } catch (Throwable ignored) { }
                }
            }, delay);
        } catch (Throwable ignored) { }
    }

    private static void loadInterstitial(final Activity activity) {
        if (!Ads.SDK_READY || !adsAllowed() || interstitial != null || loadingInterstitial) return;
        long now = System.currentTimeMillis();
        if (now < nextInterstitialTry) return;   // istek fırtınasını engelle
        nextInterstitialTry = now + interstitialBackoff;
        loadingInterstitial = true;
        try {
            InterstitialAd.load(activity, Ads.INTERSTITIAL, new AdRequest.Builder().build(),
                    new InterstitialAdLoadCallback() {
                        @Override public void onAdLoaded(InterstitialAd ad) {
                            loadingInterstitial = false;
                            interstitial = ad;
                            interstitialBackoff = 30000L;
                            lastInterstitialError = "";
                            Log.i(TAG, "gecis reklami hazir");
                        }

                        @Override public void onAdFailedToLoad(LoadAdError error) {
                            loadingInterstitial = false;
                            lastInterstitialError = reason(error);
                            // Doluluk yoksa sık sık denemek boşa istek; geri çekilerek bekle.
                            interstitialBackoff = Math.min(interstitialBackoff * 2, 300000L);
                            nextInterstitialTry = System.currentTimeMillis() + interstitialBackoff;
                            CrashLog.eventThrottled(activity, "ad", "gecis",
                                    "geçiş reklamı yüklenemedi: " + lastInterstitialError,
                                    30L * 60L * 1000L);
                            Log.w(TAG, "gecis reklami yuklenemedi: " + lastInterstitialError);
                        }
                    });
        } catch (Throwable t) {
            loadingInterstitial = false;
            Log.w(TAG, "gecis yukleme hatasi: " + t);
            nextInterstitialTry = System.currentTimeMillis() + 60000L;
        }
    }

    private static void loadRewarded(final Activity activity) {
        if (!Ads.SDK_READY || !adsAllowed() || rewarded != null || loadingRewarded) return;
        long now = System.currentTimeMillis();
        if (now < nextRewardedTry) return;
        nextRewardedTry = now + rewardedBackoff;
        loadingRewarded = true;
        try {
            RewardedAd.load(activity, Ads.REWARDED, new AdRequest.Builder().build(),
                    new RewardedAdLoadCallback() {
                        @Override public void onAdLoaded(RewardedAd ad) {
                            loadingRewarded = false;
                            // 9.93: odulun sunucuda dogrulanabilmesi icin SSV verisi.
                            // AdMob bu degeri odul callback'ine ekler; Worker imzayi
                            // kontrol edip D1'e yazar (sahte odul girisimleri gorunur olur).
                            try {
                                ad.setServerSideVerificationOptions(
                                        new com.google.android.gms.ads.rewarded.ServerSideVerificationOptions.Builder()
                                                .setCustomData("u=" + CrashLog.deviceId(activity))
                                                .build());
                            } catch (Throwable ignored) { }
                            rewarded = ad;
                            rewardedBackoff = 30000L;
                            lastRewardedError = "";
                            Log.i(TAG, "odullu reklam hazir");
                        }

                        @Override public void onAdFailedToLoad(LoadAdError error) {
                            loadingRewarded = false;
                            lastRewardedError = reason(error);
                            rewardedBackoff = Math.min(rewardedBackoff * 2, 300000L);
                            nextRewardedTry = System.currentTimeMillis() + rewardedBackoff;
                            CrashLog.eventThrottled(activity, "ad", "odullu",
                                    "ödüllü reklam yüklenemedi: " + lastRewardedError,
                                    30L * 60L * 1000L);
                            Log.w(TAG, "odullu reklam yuklenemedi: " + lastRewardedError);
                            // Kendiliğinden tekrar dene (kullanıcı "hazır değil" görmesin).
                            retryLater(activity, true, rewardedBackoff);
                        }
                    });
        } catch (Throwable t) {
            loadingRewarded = false;
            Log.w(TAG, "odullu yukleme hatasi: " + t);
            nextRewardedTry = System.currentTimeMillis() + 60000L;
        }
    }

    /** Sahip modu: reklamları bu cihazda tamamen kapat/aç. */
    public static void setAdsOff(Activity a, boolean off) {
        adsOffFlag = off;
        if (a != null) Prefs.setAdsOff(a, off);
        inited = false;          // yeniden açılınca init baştan çalışsın
        if (off) {
            adsAllowed = false;
            interstitial = null;
            rewarded = null;
            // Ekranda kalan banner'ı hemen kaldır (ana ekran arkada açık kalıyor).
            try {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override public void run() {
                        for (java.lang.ref.WeakReference<View> r : liveBanners) {
                            View v = r == null ? null : r.get();
                            if (v == null) continue;
                            ViewGroup p = (ViewGroup) v.getParent();
                            if (p != null) p.removeView(v);
                        }
                        liveBanners.clear();
                    }
                });
            } catch (Throwable ignored) { }
        }
        Log.i(TAG, off ? "reklamlar kapatildi" : "reklamlar acildi");
    }

    /**
     * Sahip modu teşhisi: "reklam gerçekten para yazacak şekilde mi çalışıyor?"
     * SDK, rıza, birim kimlikleri, sayaç ve son hata tek ekranda.
     */
    public static String diagnostics(Activity a) {
        StringBuilder b = new StringBuilder();
        b.append("SDK: ").append(Ads.SDK_READY ? "bağlı" : "YOK").append("\n");
        b.append("Reklam istenebilir (rıza): ").append(adsAllowed() ? "EVET" : "HAYIR").append("\n");
        b.append("Reklamlar kapalı (sahip): ").append(adsOffFlag ? "EVET" : "hayır").append("\n");
        b.append("Test modu (para yazmaz): ").append(Ads.TEST_MODE ? "AÇIK" : "kapalı").append("\n");
        b.append("İçerik sınıfı: PG (aile dostu)\n\n");
        b.append("Banner birimi: ").append(Ads.BANNER).append("\n");
        b.append("Geçiş birimi: ").append(Ads.INTERSTITIAL).append("\n");
        b.append("Ödüllü birimi: ").append(Ads.REWARDED).append("\n\n");
        b.append("Hazır reklam: geçiş ").append(interstitial != null ? "var" : "yok")
                .append(" · ödüllü ").append(rewarded != null ? "var" : "yok").append("\n");
        b.append("Son hatalar:\n");
        b.append("· banner: ").append(lastBannerError.length() == 0 ? "—" : lastBannerError).append("\n");
        b.append("· geçiş: ").append(lastInterstitialError.length() == 0 ? "—" : lastInterstitialError).append("\n");
        b.append("· ödüllü: ").append(lastRewardedError.length() == 0 ? "—" : lastRewardedError).append("\n\n");
        b.append("Bu cihazda sayılan gösterim: ").append(AdStats.total(a)).append("\n");
        b.append("Gelir AdMob konsolunda (hesap geneli) 1-2 gün gecikmeyle kesinleşir;\n");
        b.append("uygulama Google'a ayrıca bir şey bildirmez — gösterimi SDK kendisi raporlar.\n");
        b.append("NO_FILL, o an reklamveren talebi olmadığı anlamına gelir; uygulama hatası değildir.");
        return b.toString();
    }
}
