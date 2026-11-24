# UMAY Multi TV 9.59 — sürüm notları

versionCode **68** · versionName **9.59**

## 1. İnternet geri gelince yayın kendiliğinden açılıyor (M-80)

**Belirti:** Wi-Fi/mobil veri 15–30 sn kesilince yayın duruyor; internet geri gelse bile
tek oynatıcıda **dönen simge** ekranda kalıyor, çoklu ekranda pencereler
“ulaşılamadı” yazısıyla donuk kalıyordu. Kullanıcı elle kanal değiştirmek zorundaydı.

**Kök neden (üç ayrı hata):**

1. Uygulama ağın geri geldiğini **dinlemiyordu**; yalnızca 15 sn'lik sayaç ve 3 deneme vardı.
   Denemeler tükendikten sonra hiçbir şey olmuyordu.
2. Yeniden bağlanma yolları `ready` bayrağını sıfırlamıyordu; yayın aslında açılsa bile
   “hazır” işleyicisi erken çıkıp **spinner ve uyarı yazısını ekranda bırakıyordu**.
3. Çoklu ekranın pencerelerinde ağ dönüşünde yeniden deneme yoktu.

**Düzeltme:**

- `PlayerActivity` ve `MultiActivity` artık `ConnectivityManager` ile varsayılan ağı dinliyor.
- Ağ gelince yayın hazır değilse `sameRetries` sıfırlanıp **canlıya yeniden bağlanılıyor**;
  ekranda “📶 İnternet geri geldi · yeniden bağlanıyorum” yazıyor, açılınca kayboluyor.
- Çoklu ekranda her pencere kendi nesliyle yeniden bağlanıyor (“internet geri geldi · bağlanıyor…”).
- Yeniden bağlanma yollarında `ready=false` → spinner/uyarı artık takılı kalmıyor.

## 2. Cihazda doğrulananlar (telefon 2506BPN68G)

| Test | Yöntem | Sonuç |
|---|---|---|
| Tek oynatıcı | Wi-Fi 22 sn kapalı → açık, 22 sn bekle | ✅ yayın kendiliğinden açıldı, spinner gitti, veri 102 → 125,9 MB aktı |
| Çoklu ekran (3 pencere) | Wi-Fi 30 sn kapalı → açık, 28 sn bekle | ✅ üç pencere de canlıya döndü (önceden “ulaşılamadı”da kalıyordu) |
| Ayarlar → sürüm | Ekran görüntüsü | ✅ “Sürüm 9.59 · dokunduğun satır açılır” |
| Radyo araması “ntv” | Klavye açık | ✅ yalnız NTV Radyo sonuçları (Kentville elendi) |

## Kapasite denetimi

| Ölçüm | 9.57 | 9.59 |
|---|---|---|
| APK | 7.112.773 B | 7.113.493 B (+720 B) |
| dosya | 400 | 400 |
| sınıf | 17.504 | 17.508 (+4) |
| imza | f3996c67… | f3996c67… |

✅ Kayıp yok. (9.58 ara sürümü yalnız ağ dinleyicisi denemesiydi; 9.59 tam düzeltme.)

## Tablet

`releases/v9.59/UMAY-MULTI-TV-9.59-tablet.apk` + `İndirilenler/UMAY-MULTI-TV-9.59-tablet.apk`.
Aynı binary. HyperOS adb kurulumunu engellediği için tablette dosyadan açıp kurulur.
