# Reklamsız abonelik (20 ₺/ay) — kurulum ve kurallar

Uygulama içinde: **Ayarlar → Reklamsız izleme → Reklamsız abonelik**.

## Ne yapıyor?

- Abone olan kullanıcıda **hiç reklam gösterilmez**: banner yuvası, geçiş (interstitial)
  ve ödüllü reklam istekleri kesilir.
- Abone değilse her şey eskisi gibi: banner + 15 dakikada bir geçiş reklamı.
- Ödeme **Google Play Faturalandırma** ile alınır. Kart bilgisi uygulamaya hiç girmez.
- Kullanıcı aboneliği Play Store → Ödemeler ve abonelikler'den iptal edebilir;
  uygulama açılışında durumu Play'den yeniden okur.

## Play Konsol'da yapılması gerekenler (senin işin)

1. Play Console → uygulama → **Para kazanma → Ürünler → Abonelikler → Abonelik oluştur**.
2. **Ürün kimliği:** `umay_reklamsiz_aylik` (kodda bu kimlik sabit; değiştirirsen `Billing.java`).
3. **Ad:** "Reklamsız izleme" · **Açıklama:** "Reklamlar kaldırılır, aylık abonelik".
4. **Temel plan:** aylık, otomatik yenilenen, **20,00 ₺**, Türkiye.
   (Play fiyatı sen belirlersin; kodda fiyat yazmaz, mağazadan gelir.)
5. Planı **etkinleştir**. Ürün "Etkin" olmadan uygulama "satışta değil" uyarısı verir.
6. Test için: Play Console → **Lisans testi** → kendi Google hesabını ekle; test satın
   almaları ücretsizdir ve para yazmaz.

## Politika notu

- Google Play, uygulama içi dijital aboneliklerde **yalnızca Play Faturalandırma**'ya izin verir
  (kendi ödeme sayfası/kart formu yasak). Bu yüzden ödeme akışı Play'de.
- Abonelik ekranında "istediğin zaman iptal" ve "ödeme Google Play üzerinden" bilgisi açıkça
  yazıyor; Play'in abonelik kuralları için gerekli.
- Uygulama mağazada yayınlandıktan sonra abonelik ürünü de incelemeye girer.

## Teknik

- `Billing.java`: Play Billing 6.2.1, ürün sorgusu, satın alma, onaylama (acknowledge),
  açılışta durum tazeleme.
- `Prefs.subActive`: cihazda abonelik durumu (Play'den okunur, çevrimdışıyken korunur).
- `AdManager`: banner / geçiş / ödüllü reklam girişlerinde abonelik kontrolü.
- Sunucu doğrulaması yok (v1). İleride satın alma jetonunu sunucuda doğrulamak için
  `Billing.grant()` içindeki yer hazır.
