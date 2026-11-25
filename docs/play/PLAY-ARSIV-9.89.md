# Play'e yükleme — 9.89 (kod 98) adım adım

**Paket:** `com.cengiz.canlitv` · **versionCode 97** · **versionName 9.89**
**AAB:** `releases/v9.89/UMAY-MULTI-TV-9.89.aab` (**6,61 MB**) · **APK (test):** `releases/v9.89/CanliTV-9.89.apk` (**3,85 MB**)
**İmza SHA-256:** `f3996c67ef0e0e0420f442fe3fcf6303a17caa88dcba8a2b19a10cd95c92b8d5`
**targetSdk 36 · minSdk 26 · 64-bit · hassas izin yok**

> 9.89 derlendi ve cihazda açıldı (R8 açık, çökme yok). 9.87'ye göre: 120 kanal
> (5 çocuk kanalı ve parametreli 3 kanal çıktı), `app=` 0, harici logo 0, izlerken
> tam ekran reklam yok, abonelik beyanı ve 13+ hedef kitle, R8 ile APK %48 küçük.

> 9.89'de ayrıca: ölü radyo (Slow Türk) çıkarıldı, Süper Show Radyo adresi düzeltildi,
> oynatıcı hata sınıflandırması (canlı kenar / ağ / kalıcı), çoklu ekranda pencere
> durum katmanı ve sessiz pencerelere kalite tavanı, açılış süresi ölçümü.

## 1) Uygulama oluştur / mevcut uygulamaya sürüm yükle
1. Play Console → **Test ve yayınlama → Kapalı test → Yeni sürüm oluştur**.
2. **App bundle** olarak `releases/v9.89/UMAY-MULTI-TV-9.89.aab` dosyasını yükle.
3. Sürüm notları (Türkçe):
   ```
   Radyo oynatma yenilendi: AAC+ istasyonlar (TRT FM, TRT Radyo 1) artık açılıyor.
   Arka planda dinlerken veri sayacı doğru artıyor. Ayarlar'da reklamsız abonelik en üstte.
   ```

## 2) Mağaza kaydı (kopyala-yapıştır)
Kaynak: `02-play/magaza-kaydi.md`
- Ad (30): `UMAY Multi TV: Canlı TV Radyo`
- Kısa açıklama (80): `120 TV kanalı + radyo: numaralı kanal, favoriler, 3'lü izleme, uyku modu.`
- Tam açıklama: aynı dosyadaki blok (4000 sınırı içinde).
- Görseller: `03-gorseller/` → `ikon-512.png`, `one-cikan-1024x500.png`, `ekran-*.png` (Play oranına göre hazır).

## 3) Uygulama içeriği beyanları
| Soru | Cevap |
|---|---|
| Reklam içeriyor mu? | **Evet** (AdMob banner + geçiş + ödüllü) |
| Uygulama içi satın alma | **Evet** — aylık reklamsız abonelik (`umay_reklamsiz_aylik`, 20 ₺) |
| Uygulama erişimi | Tüm işlevler kısıtlama olmadan kullanılabilir (hesap yok) |
| Hedef kitle | **13+** / çocuklara yönelik değil (listede çocuk kanalı yok) |
| Haber uygulaması | Hayır (yalnız yayın izleme) |
| Veri güvenliği | Reklam kimliği: toplanıyor + paylaşılıyor (Google, reklam/ölçüm). Cihaz kimliği: yalnız kullanıcı "günlük gönderimi" açarsa, paylaşılmıyor. Konum: hayır. E-posta: hayır. Ödeme: uygulama toplamaz, Google Play işler. |
| Gizlilik politikası | https://umay-multi-tv-gq3zer.freebuff.site/gizlilik-politikasi |
| Kullanım koşulları | https://umay-multi-tv-gq3zer.freebuff.site/kullanim-kosullari |
| Telif / kaldırma | https://umay-multi-tv-gq3zer.freebuff.site/telif-bildirimi |

## 4) Abonelik (reklamsız izleme)
- Ürün kimliği: **`umay_reklamsiz_aylik`** · aylık · **20 ₺**
- Play Console → **Para kazanma → Ürünler → Abonelikler** → oluştur, fiyatı Türkiye için 20 ₺ yap.
- Test için: **Lisans testi** hesabını ekle (Ayarlar → Lisans testi), uygulamada Ayarlar → Reklamsız abonelik.
- Fiyat koda gömülü değil; Play'den gelir.

## 5) Kapalı test
- **12 tester** e-postası ekle (Gmail adresleri), ülke Türkiye.
- 14 gün kapalı test şartı; tester'lar uygulamayı açıp en az bir kez kanal/radyo oynatmalı.
- Test sonunda **Üretime yayınla** → ülke seçimi + içerik derecelendirmesi onayı.

## 6) Yüklemeden önce hızlı kontrol
- [x] AAB versionCode 97 (Play'deki 96'dan büyük) — `bundletool dump manifest` ile doğrulandı
- [ ] İmza parmak izi yukarıdakiyle aynı
- [ ] Gizlilik politikası URL'si tarayıcıda açılıyor
- [ ] app-ads.txt yayında: https://umay-multi-tv-gq3zer.freebuff.site/app-ads.txt
- [ ] Ekran görüntüleri güncel arayüzü gösteriyor (03-gorseller)
- [ ] Abonelik ürünü "Etkin"
