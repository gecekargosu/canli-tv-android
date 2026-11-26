# UMAY Multi TV 9.95 (sürüm kodu 104)

Play zorunluluğu nedeniyle **Play Faturalandırma Kitaplığı 6.2.1 → 8.0.0** yükseltildi.
Play, 6.2.1 ile 9.93 (102) paketini reddediyordu:
"Play Faturalandırma Kitaplığı'nın 6.2.1 sürümü kullanılıyor, en az 8.0.0 olmalı."

## Kod değişiklikleri

| Dosya | Değişiklik |
|---|---|
| `app/build.gradle` | `com.android.billingclient:billing:8.0.0`, `versionCode 104`, `versionName '9.95'` |
| `src/com/cengiz/canlitv/Billing.java` | Billing 8 API'leri: `enablePendingPurchases(PendingPurchasesParams)`, `queryPurchasesAsync(QueryPurchasesParams)`, `onProductDetailsResponse(BillingResult, QueryProductDetailsResult)` |

Davranış aynı: ürün kimliği `umay_reklamsiz_aylik`, ürün yoksa "şu an satışta değil" mesajı.

## Dosyalar

- `UMAY-MULTI-TV-9.95.aab` — Play'e yüklendi (104)
- `CanliTV.apk` — telefona kurulabilir (aynı upload imzası)
- Kapasite denetimi: 393 dosya / 7032 sınıf / imza `f3996c67…b8d5` → **TEMİZ**

## Notlar

- 9.94 (103) paketi Play kütüphanesine yüklendi ama sürüm kodu tüketildiği için
  release'ten çıkarıldı; yerine 104 kullanıldı. `releases/v9.94/` yalnız arşivdir.
- Play mağaza girişi görselleri 9.93 cihaz görüntüleridir (arayüz aynı).
