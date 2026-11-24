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
 * - Gecis reklami: her kanal degisiminde bir + izlerken 15 dakikada bir.
 * - Reklamsiz sure kanal bazli DEGIL: uygulama geneli (odullu video ile 15 dk).
 * - Reklamsiz sure aktifse gecis reklami gosterilmez; banner yine kalir.
 * - Reklam yuklenemezse uygulama normal akisina devam eder.
 */
public final class AdManager {

    private static final String TAG = "UmayAds";

    // --- M-03: tek reklam izin kapısı -------------------------------------------------
    /** Reklam istenebilir mi? Başlangıçta HAYIR (fail-closed). */
    private static volatile boolean adsAllowed = false;
    /** Rıza gelmeden oluşturulan banner'lar; izin gelince yüklenir. */
    private static final java.util.List<Runnable> pendingBanners =
            new java.util.ArrayList<Runnable>();

    /** Reklam istenebilir mi? Diğer sınıflar da bunu kullanır. */
    public static boolean adsAllowed() {
        return adsAllowed;
    }

    private static boolean inited = false;
    private static InterstitialAd interstitial = null;
    private static boolean loadingInterstitial = false;
    private static RewardedAd rewarded = null;
    private static boolean loadingRewarded = false;
    private static long lastInterstitial = 0L;
    private static int opens = 0;
    private static boolean showingAd = false;   // ayni anda iki tam ekran reklam acilmasin

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
        if (!Ads.SDK_READY || inited) return;
        inited = true;
        try {
            // Aile dostu reklam: yetiskin/erotik/"afterglow" turu icerikler istenmez.
            // MAX_AD_CONTENT_RATING_G = yalnizca genel izleyiciye uygun reklamlar.
            RequestConfiguration.Builder rc = new RequestConfiguration.Builder()
                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                    .setTagForChildDirectedTreatment(
                            RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
                    .setTagForUnderAgeOfConsent(
                            RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED);
            // Test modu: debug derlemede her zaman, dağıtılan APK'da ise yalnızca
            // kullanıcı Ayarlar'dan açtıysa. Test cihazında gerçek birimler test reklamı
            // döndürür: reklam/ödül çalışır, para yazmaz, hesap riski olmaz.
            if (Ads.TEST_MODE || Prefs.adTestMode(activity)) {
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
        try {
            LinearLayout holder = new LinearLayout(activity);
            holder.setOrientation(LinearLayout.VERTICAL);
            holder.setBackgroundColor(Color.parseColor("#060B14"));
            content.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            holder.addView(content);
            if (Prefs.showUsage(activity)) holder.addView(usageBar(activity));
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
        try {
            final android.widget.FrameLayout holder = new android.widget.FrameLayout(activity);
            holder.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            // Yükseklik her zaman dursun: reklam yüklenirken/yüklenemezken bar kaybolmasın.
            holder.setMinimumHeight(dp(activity, 56));
            holder.setBackgroundColor(Color.parseColor("#0B1220"));

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
        t.setTextColor(Color.parseColor("#0B1220"));
        t.setBackgroundColor(Color.parseColor("#FFB020"));
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
        if (!Ads.SDK_READY || !adsAllowed || isAdFree(activity) || showingAd) {
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
            AdStats.note(activity, "gecis");
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

    /**
     * İzlerken 15 dakikada bir çıkan reklam. PlayerActivity her saniye çağırır;
     * reklam hazır değilse sessizce geçer, uygulama akışı durmaz.
     */
    public static void maybeTimerAd(final Activity activity) {
        // HATA (tur 3): rıza yokken de zamanlanmış reklam gösterilebiliyordu.
        if (!Ads.SDK_READY || !adsAllowed || isAdFree(activity) || showingAd) return;
        long now = System.currentTimeMillis();
        long last = Prefs.lastAdAt(activity);
        if (last <= 0) { Prefs.setLastAdAt(activity, now); return; }
        if (now - last < Ads.AD_INTERVAL_MS) return;
        final InterstitialAd ad = interstitial;
        if (ad == null) { loadInterstitial(activity); return; }
        interstitial = null;
        lastInterstitial = now;
        Prefs.setLastAdAt(activity, now);
        showingAd = true;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                showingAd = false;
                loadInterstitial(activity);
            }

            @Override public void onAdFailedToShowFullScreenContent(AdError error) {
                showingAd = false;
                loadInterstitial(activity);
            }
        });
        try {
            ad.show(activity);
            AdStats.note(activity, "gecis");
        } catch (Throwable t) {
            showingAd = false;
            loadInterstitial(activity);
        }
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
            AdStats.note(activity, "odullu");
            ad.show(activity, new com.google.android.gms.ads.OnUserEarnedRewardListener() {
                @Override public void onUserEarnedReward(
                        com.google.android.gms.ads.rewarded.RewardItem item) {
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

    private static void loadInterstitial(final Activity activity) {
        if (!Ads.SDK_READY || !adsAllowed || interstitial != null || loadingInterstitial) return;
        loadingInterstitial = true;
        try {
            InterstitialAd.load(activity, Ads.INTERSTITIAL, new AdRequest.Builder().build(),
                    new InterstitialAdLoadCallback() {
                        @Override public void onAdLoaded(InterstitialAd ad) {
                            loadingInterstitial = false;
                            interstitial = ad;
                            Log.i(TAG, "gecis reklami hazir");
                        }

                        @Override public void onAdFailedToLoad(LoadAdError error) {
                            loadingInterstitial = false;
                            Log.w(TAG, "gecis reklami yuklenemedi: " + error.getMessage());
                        }
                    });
        } catch (Throwable t) {
            loadingInterstitial = false;
            Log.w(TAG, "gecis yukleme hatasi: " + t);
        }
    }

    private static void loadRewarded(final Activity activity) {
        if (!Ads.SDK_READY || !adsAllowed || rewarded != null || loadingRewarded) return;
        loadingRewarded = true;
        try {
            RewardedAd.load(activity, Ads.REWARDED, new AdRequest.Builder().build(),
                    new RewardedAdLoadCallback() {
                        @Override public void onAdLoaded(RewardedAd ad) {
                            loadingRewarded = false;
                            rewarded = ad;
                            Log.i(TAG, "odullu reklam hazir");
                        }

                        @Override public void onAdFailedToLoad(LoadAdError error) {
                            loadingRewarded = false;
                            Log.w(TAG, "odullu reklam yuklenemedi: " + error.getMessage());
                        }
                    });
        } catch (Throwable t) {
            loadingRewarded = false;
            Log.w(TAG, "odullu yukleme hatasi: " + t);
        }
    }
}
