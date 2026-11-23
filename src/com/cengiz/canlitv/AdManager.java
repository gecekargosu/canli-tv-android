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
    private static boolean showingAd = false;   // ayni anda iki tam ekran reklam acilmasin

    private AdManager() { }

    /** Reklamsiz sure (herhangi bir kanal icin) aktif mi? — bilgi/ayar ekranlari icin. */
    public static boolean isAdFree(Context c) {
        return System.currentTimeMillis() < Prefs.adFreeUntil(c);
    }

    /**
     * Bu kanal/istasyon icin reklamsiz sure aktif mi?
     * 20 dakikalik odul **kanal bazlidir**: baska kanala gecince yeniden video gerekir.
     */
    public static boolean isAdFreeFor(Context c, String key) {
        if (System.currentTimeMillis() >= Prefs.adFreeUntil(c)) return false;
        String k = Prefs.adFreeKey(c);
        if (k == null || k.length() == 0) return false;
        String want = key != null ? key : lastKey(c);
        return want != null && want.equals(k);
    }

    /** Ekran bir kanal belirtmiyorsa "en son izlenen kanal" esas alinir. */
    private static String lastKey(Context c) {
        try {
            java.util.List<Channel> l = ChannelRepo.all(c);
            int i = Prefs.lastIndex(c);
            if (i >= 0 && i < l.size()) return l.get(i).url;
        } catch (Exception ignored) { }
        return null;
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
            // Aile dostu reklam: yetiskin/erotik/"afterglow" turu icerikler istenmez.
            // MAX_AD_CONTENT_RATING_G = yalnizca genel izleyiciye uygun reklamlar.
            RequestConfiguration.Builder rc = new RequestConfiguration.Builder()
                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                    .setTagForChildDirectedTreatment(
                            RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED)
                    .setTagForUnderAgeOfConsent(
                            RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED);
            if (Ads.TEST_MODE) {
                rc.setTestDeviceIds(Collections.singletonList(Ads.TEST_DEVICE));
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
        boolean can = true;
        try {
            can = com.google.android.ump.UserMessagingPlatform
                    .getConsentInformation(activity).canRequestAds();
        } catch (Throwable ignored) { }
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
                }
            });
        } catch (Throwable t) {
            Log.w(TAG, "SDK baslatma: " + t);
        }
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
            holder.addView(rewardStrip(activity, key));
            View ad = banner(activity);
            if (ad != null) holder.addView(ad);
            // sistem gezinme cubugu (jest cubugu) banner'in uzerine binmesin:
            // ekranin altina cubuk yuksekligi kadar bosluk birak
            holder.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override public android.view.WindowInsets onApplyWindowInsets(View v,
                                                                                  android.view.WindowInsets insets) {
                    int bottom = insets.getSystemWindowInsetBottom();
                    if (bottom > 0) v.setPadding(0, 0, 0, bottom);
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

    private static String human(long bytes) {
        if (bytes >= 1073741824L) return String.format(java.util.Locale.US, "%.2f GB", bytes / 1073741824.0);
        return String.format(java.util.Locale.US, "%.1f MB", bytes / 1048576.0);
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

            final AdView view = new AdView(activity);
            view.setAdUnitId(Ads.BANNER);
            float density = activity.getResources().getDisplayMetrics().density;
            int widthDp = (int) (activity.getResources().getDisplayMetrics().widthPixels / density);
            AdSize size = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp);
            view.setAdSize(size != null ? size : AdSize.BANNER);
            holder.addView(view, new android.widget.FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            final TextView house = houseBar(activity);
            house.setVisibility(View.GONE);
            holder.addView(house);

            view.setAdListener(new com.google.android.gms.ads.AdListener() {
                @Override public void onAdFailedToLoad(LoadAdError error) {
                    Log.w(TAG, "banner dolmadi, kendi barimiz gosteriliyor: " + error.getMessage());
                    view.setVisibility(View.GONE);
                    house.setVisibility(View.VISIBLE);
                }
            });
            view.loadAd(new AdRequest.Builder().build());
            return holder;
        } catch (Throwable t) {
            Log.w(TAG, "banner hatasi: " + t);
            return null;
        }
    }

    /**
     * Her ekranin altinda duran, tek dokunusla odullu videoyu baslatan serit.
     * Kullanici menu aramaz: "▶ Reklam izle · 20 dk reklamsız" yazar, dokunur, izler.
     */
    public static TextView rewardStrip(final Activity a) {
        return rewardStrip(a, null);
    }

    /**
     * @param key kanal/istasyon URL'si; 20 dk reklamsiz sure bu kanal icin gecerlidir.
     *            null ise "herhangi bir kanal" anlaminda calisir.
     */
    public static TextView rewardStrip(final Activity a, final String rawKey) {
        final String key = rawKey != null ? rawKey : lastKey(a);
        final TextView t = new TextView(a);
        t.setTextSize(13f);
        t.setTextColor(Color.parseColor("#0B1220"));
        t.setBackgroundColor(Color.parseColor("#FFB020"));
        t.setPadding(dp(a, 10), dp(a, 9), dp(a, 10), dp(a, 9));
        t.setGravity(android.view.Gravity.CENTER);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);

        if (isAdFreeFor(a, key)) {
            int left = adFreeMinutesLeft(a);
            t.setBackgroundColor(Color.parseColor("#2A3340"));
            t.setTextColor(Color.parseColor("#8FE3A0"));
            t.setText("✓  Bu kanalda reklamsız · " + left + " dk kaldı");
            return t;
        }

        t.setText("▶  Reklam izle · " + Ads.REWARD_MINUTES + " dk reklamsız");
        t.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showRewarded(a, key, new Runnable() {
                    @Override public void run() {
                        android.widget.Toast.makeText(a,
                                "Teşekkürler! Bu kanalda " + Ads.REWARD_MINUTES + " dakika reklamsız.",
                                android.widget.Toast.LENGTH_LONG).show();
                        a.recreate();
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
                        android.widget.Toast.makeText(a,
                                "Teşekkürler! " + Ads.REWARD_MINUTES + " dakika reklamsız.",
                                android.widget.Toast.LENGTH_LONG).show();
                        a.recreate();
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
        if (!Ads.SDK_READY || isAdFreeFor(activity, key) || showingAd) {
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
        } catch (Throwable t) {
            showingAd = false;
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
        showRewarded(activity, null, onReward, onFail);
    }

    /** key = hangi kanal/istasyon icin reklamsiz sure verilecek. */
    public static void showRewarded(final Activity activity, final String key,
                                    final Runnable onReward, final Runnable onFail) {
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
                    Prefs.setAdFreeUntil(activity, until, key);
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
