package com.cengiz.canlitv;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
 * - Banner: kanal listesi, radyo listesi ve oynatici ekraninin ALTINDA (videonun uzerine binmez).
 * - Gecis reklami: 4. acilista bir, iki reklam arasi en az 10 dakika.
 * - Odullu: kullanici "60 dakika reklamsiz" icin kendi ister.
 * - Reklamsiz sure aktifse (Prefs.adFreeUntil) hic reklam gosterilmez.
 * - Reklam yuklenemezse uygulama normal akisina devam eder.
 */
public final class AdManager {

    private static final String TAG = "UmayAds";

    private static boolean inited = false;
    private static InterstitialAd interstitial = null;
    private static boolean loadingInterstitial = false;
    private static RewardedAd rewarded = null;
    private static boolean loadingRewarded = false;
    private static long lastInterstitial = 0L;
    private static int opens = 0;

    private AdManager() { }

    /** Reklamsiz sure aktif mi? */
    public static boolean isAdFree(Context c) {
        return System.currentTimeMillis() < Prefs.adFreeUntil(c);
    }

    /** Reklamsiz kalan dakika (0 = yok). */
    public static int adFreeMinutesLeft(Context c) {
        long ms = Prefs.adFreeUntil(c) - System.currentTimeMillis();
        if (ms <= 0) return 0;
        return (int) Math.ceil(ms / 60000.0);
    }

    public static void init(final Activity activity) {
        if (!Ads.SDK_READY || inited) return;
        inited = true;
        try {
            if (Ads.TEST_MODE) {
                MobileAds.setRequestConfiguration(new RequestConfiguration.Builder()
                        .setTestDeviceIds(Collections.singletonList(Ads.TEST_DEVICE))
                        .build());
            }
            MobileAds.initialize(activity, new OnInitializationCompleteListener() {
                @Override public void onInitializationComplete(InitializationStatus status) {
                    Log.i(TAG, "SDK hazir");
                    loadInterstitial(activity);
                    loadRewarded(activity);
                }
            });
        } catch (Throwable t) {
            Log.w(TAG, "baslatma hatasi: " + t);
        }
    }

    /** Liste/oynatici altindaki banner. Reklamsiz surede veya SDK yoksa null doner. */
    public static AdView banner(Activity activity) {
        if (!Ads.SDK_READY || isAdFree(activity)) return null;
        try {
            AdView view = new AdView(activity);
            view.setAdUnitId(Ads.BANNER);
            float density = activity.getResources().getDisplayMetrics().density;
            int widthDp = (int) (activity.getResources().getDisplayMetrics().widthPixels / density);
            AdSize size = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp);
            view.setAdSize(size != null ? size : AdSize.BANNER);
            view.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.loadAd(new AdRequest.Builder().build());
            return view;
        } catch (Throwable t) {
            Log.w(TAG, "banner hatasi: " + t);
            return null;
        }
    }

    /**
     * Kanal/radyo acilmadan once cagrilir: sira gelen gecis reklami varsa gosterir,
     * yoksa isi hemen calistirir. Reklamsiz surede hic beklemez.
     */
    public static void maybeInterstitial(final Activity activity, final Runnable then) {
        if (!Ads.SDK_READY || isAdFree(activity)) {
            then.run();
            return;
        }
        opens++;
        boolean due = (opens % Ads.ZAPS_BEFORE_INTERSTITIAL) == 0
                && (System.currentTimeMillis() - lastInterstitial) >= Ads.INTERSTITIAL_GAP_MS;
        final InterstitialAd ad = interstitial;
        if (!due || ad == null) {
            then.run();
            return;
        }
        interstitial = null;
        lastInterstitial = System.currentTimeMillis();
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                loadInterstitial(activity);
                then.run();
            }

            @Override public void onAdFailedToShowFullScreenContent(AdError error) {
                loadInterstitial(activity);
                then.run();
            }
        });
        try {
            ad.show(activity);
        } catch (Throwable t) {
            Log.w(TAG, "gecis reklami gosterilemedi: " + t);
            loadInterstitial(activity);
            then.run();
        }
    }

    /** "60 dakika reklamsiz" odullu reklami hazir mi? */
    public static boolean rewardedReady() {
        return Ads.SDK_READY && rewarded != null;
    }

    /**
     * Odullu reklami gosterir. Kullanici sonuna kadar izlerse onReward calisir ve
     * reklamsiz sure baslar. Hazir degilse onFail calisir (kullaniciya bilgi verilir).
     */
    public static void showRewarded(final Activity activity, final Runnable onReward,
                                    final Runnable onFail) {
        final RewardedAd ad = rewarded;
        if (!Ads.SDK_READY || ad == null) {
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
        if (!Ads.SDK_READY || interstitial != null || loadingInterstitial) return;
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
        if (!Ads.SDK_READY || rewarded != null || loadingRewarded) return;
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
