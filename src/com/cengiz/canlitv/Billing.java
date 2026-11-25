package com.cengiz.canlitv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;

import java.util.Collections;
import java.util.List;

/**
 * Reklamsız abonelik — Google Play Faturalandırma (Play Billing).
 *
 * Play politikası: uygulama içi dijital abonelik Play Faturalandırma ile satılmak zorunda.
 * Bu yüzden kendi ödeme sayfamız yok; ürün Play Konsol'da tanımlanır, fiyatı (20 ₺/ay)
 * orada belirlenir, satın alma Google hesabı üzerinden yapılır.
 *
 * Ürün kimliği: umay_reklamsiz_aylik  (Play Konsol → Para kazanma → Abonelikler)
 * Ürün henüz tanımlı değilse "şu an satışta değil" mesajı çıkar; kod bozulmaz.
 */
public final class Billing {

    /** Play Konsol'da oluşturulacak abonelik ürün kimliği. */
    public static final String SUB_ID = "umay_reklamsiz_aylik";

    private static BillingClient client;
    private static boolean connecting;
    private static Runnable pending;

    private Billing() { }

    /** Abonelik ekranını Play'de açar (iptal için). */
    public static void manage(Context c) {
        try {
            c.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/account/subscriptions"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Throwable ignored) { }
    }

    public static void init(final Context ctx) {
        if (client != null) { connect(ctx); return; }
        client = BillingClient.newBuilder(ctx.getApplicationContext())
                .setListener(new PurchasesUpdatedListener() {
                    @Override public void onPurchasesUpdated(BillingResult r, List<Purchase> list) {
                        if (r.getResponseCode() == BillingClient.BillingResponseCode.OK
                                && list != null) {
                            for (Purchase p : list) grant(ctx, p);
                        }
                    }
                })
                .enablePendingPurchases()
                .build();
        connect(ctx);
    }

    private static void connect(final Context ctx) {
        connect(ctx, null);
    }

    private static void connect(final Context ctx, final Runnable then) {
        if (client == null) return;
        if (client.isReady()) { if (then != null) then.run(); return; }
        if (connecting) { if (then != null) pending = then; return; }
        connecting = true;
        pending = then;
        client.startConnection(new BillingClientStateListener() {
            @Override public void onBillingSetupFinished(BillingResult r) {
                connecting = false;
                Runnable p = pending;
                pending = null;
                if (r.getResponseCode() == BillingClient.BillingResponseCode.OK && p != null) p.run();
            }

            @Override public void onBillingServiceDisconnected() {
                connecting = false;
            }
        });
    }

    /** Play'den abonelik durumunu okur. Cevap gelmezse eski durum korunur. */
    public static void refresh(final Activity a) {
        refresh(a, null);
    }

    /** then: durum okunduktan sonra UI thread'inde çalışır (ekranı tazelemek için). */
    public static void refresh(final Activity a, final Runnable then) {
        init(a);
        connect(a, new Runnable() {
            @Override public void run() {
                if (client == null || !client.isReady()) return;
                client.queryPurchasesAsync(BillingClient.ProductType.SUBS,
                        new PurchasesResponseListener() {
                            @Override public void onQueryPurchasesResponse(BillingResult r,
                                                                           List<Purchase> list) {
                                if (r.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                                boolean active = false;
                                if (list != null) {
                                    for (Purchase p : list) {
                                        if (p.getPurchaseState() == Purchase.PurchaseState.PURCHASED
                                                && p.getProducts() != null
                                                && p.getProducts().contains(SUB_ID)) {
                                            active = true;
                                            if (!p.isAcknowledged()) grant(a, p);
                                        }
                                    }
                                }
                                Prefs.setSubActive(a, active);
                                Prefs.setSubChecked(a, System.currentTimeMillis());
                                if (then != null) then.run();
                            }
                        });
            }
        });
    }

    /** Satın alma ekranını açar. */
    public static void purchase(final Activity a) {
        init(a);
        connect(a, new Runnable() {
            @Override public void run() {
                if (client == null || !client.isReady()) {
                    toast(a, "Google Play'e bağlanılamadı, biraz sonra tekrar dene");
                    return;
                }
                QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                        .setProductList(Collections.singletonList(
                                QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(SUB_ID)
                                        .setProductType(BillingClient.ProductType.SUBS)
                                        .build()))
                        .build();
                client.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
                    @Override public void onProductDetailsResponse(BillingResult r,
                                                                   List<ProductDetails> list) {
                        if (r.getResponseCode() != BillingClient.BillingResponseCode.OK
                                || list == null || list.isEmpty()) {
                            toast(a, "Abonelik şu an satışta değil (mağazada henüz tanımlı değil)");
                            return;
                        }
                        ProductDetails pd = list.get(0);
                        String offer = null;
                        try {
                            if (pd.getSubscriptionOfferDetails() != null
                                    && !pd.getSubscriptionOfferDetails().isEmpty()) {
                                offer = pd.getSubscriptionOfferDetails().get(0).getOfferToken();
                            }
                        } catch (Throwable ignored) { }
                        if (offer == null) {
                            toast(a, "Abonelik teklifi bulunamadı");
                            return;
                        }
                        BillingFlowParams flow = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(Collections.singletonList(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                                .setProductDetails(pd)
                                                .setOfferToken(offer)
                                                .build()))
                                .build();
                        try {
                            client.launchBillingFlow(a, flow);
                        } catch (Throwable t) {
                            toast(a, "Satın alma ekranı açılamadı");
                        }
                    }
                });
            }
        });
    }

    private static void grant(Context c, Purchase p) {
        if (p.getPurchaseState() != Purchase.PurchaseState.PURCHASED) return;
        if (p.getProducts() == null || !p.getProducts().contains(SUB_ID)) return;
        Prefs.setSubActive(c, true);
        Prefs.setSubChecked(c, System.currentTimeMillis());
        if (!p.isAcknowledged() && client != null && client.isReady()) {
            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(p.getPurchaseToken())
                    .build(), new AcknowledgePurchaseResponseListener() {
                @Override public void onAcknowledgePurchaseResponse(BillingResult r) { }
            });
        }
        toast(c, "Reklamsız abonelik açıldı · teşekkürler!");
    }

    private static void toast(Context c, String s) {
        try {
            Toast.makeText(c.getApplicationContext(), s, Toast.LENGTH_LONG).show();
        } catch (Throwable ignored) { }
    }
}
