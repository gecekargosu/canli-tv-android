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

import java.util.Collections;

/**
 * Reklam yonetimi (tek yer). Kurallar Ads.java icinde.
 *
 * - Banner yalnizca kanal listesinin altinda.
 * - Gecis reklami: 4. kanal acilisinda bir, iki reklam arasi en az 10 dakika.
 * - Oynatma ekraninda reklam yok.
 * - Reklam yuklenemezse uygulama normal akisina devam eder (hicbir sey bloklanmaz).
 */
public final class AdManager {

    private static final String TAG = "UmayAds";

    private static boolean inited = false;
    private static InterstitialAd interstitial = null;
    private static boolean loadingInterstitial = false;
    private static long lastInterstitial = 0L;
    private static int opens = 0;

    private AdManager() { }

    /** Uygulama acilisinda bir kez cagrilir. */
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
                    Log.i(TAG, "SDK hazir, gecis reklami isteniyor");
                    loadInterstitial(activity);
                }
            });
        } catch (Throwable t) {
            Log.w(TAG, "baslatma hatasi: " + t);
        }
    }

    /** Liste altindaki banner. Reklam kapaliysa null doner. */
    public static AdView banner(Activity activity) {
        if (!Ads.SDK_READY) return null;
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
     * Kanal acilmadan once cagrilir: sira gelen gecis reklami varsa gosterir,
     * reklam yoksa/sure dolmadiysa isi hemen calistirir.
     */
    public static void maybeInterstitial(final Activity activity, final Runnable then) {
        if (!Ads.SDK_READY) {
            then.run();
            return;
        }
        opens++;
        boolean due = (opens % Ads.ZAPS_BEFORE_INTERSTITIAL) == 0
                && (System.currentTimeMillis() - lastInterstitial) >= Ads.INTERSTITIAL_GAP_MS;
        final InterstitialAd ad = interstitial;
        Log.i(TAG, "acilis #" + opens + " sira=" + due + " hazir=" + (ad != null));
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
}
