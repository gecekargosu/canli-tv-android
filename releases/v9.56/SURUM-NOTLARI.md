# UMAY Multi TV 9.56 — sürüm notları

versionCode **65** · versionName **9.56**

## 1. Radyo aramasında boş ekran (tekrar gelmişti)

Belirti: klavye açılınca liste ve kategori çipleri kayboluyor, ekranda yalnızca arama
kutusu kalıyordu.

Kök neden: radyo ekranının kök yerleşimi klavye açılınca yeniden ölçülüyordu; üst blok
(başlık + arama + çipler) alanı doldurduğunda listeye **0 yükseklik** kalıyordu.

Düzeltme: pencere artık klavyeden yeniden boyutlanmıyor (`SOFT_INPUT_ADJUST_NOTHING`);
liste ve çipler klavyenin üstünde tam görünüyor. Ayrıca:

- arama Türkçe karakterden bağımsız ("ntv", "super", "radyo" hepsi bulur),
- arama sırasında gizlenmiş istasyonlar da listelenir.

**Cihazda doğrulandı (9.56):** "ntv" yazıldı → NTV Radyo + diğer sonuçlar klavye açıkken
göründü; çipler yerinde.

## 2. Alttaki reklam/veri şeridi gezinme çubuğunun altına kayıyordu

Belirti: ana ekranda banner'ın alt yarısı sistem gezinme çubuğunun arkasında kalıyordu.

Kök neden: MIUI'de jest çubuğu kullanılırken `systemWindowInsetBottom` **0** geliyor;
kod da yalnızca inset > 0 ise alt boşluk bırakıyordu.

Düzeltme: alt boşluk, inset ile sistemin bildirdiği `navigation_bar_height` değerinin
**büyüğü** kadar ayrılıyor. Tüm ekranlarda (ana, radyo, oynatıcı) geçerli.

**Cihazda doğrulandı (9.56):** Paycell banner'ı gezinme çubuğunun tam üstünde, kırpılmadan.

## 3. Güncelleme kapasite (bit/bayt) denetimi — yeni zorunlu adım

`tools/verify-apk.py` eklendi. Her sürümde:

- APK bayt, zip dosya sayısı, açılmış bayt, `classes*.dex` **sınıf sayısı**, imza SHA-256,
  versionName/versionCode ölçülür;
- önceki sürümle karşılaştırılır; **sınıf veya dosya sayısı düştüyse sürüm yayınlanmaz**;
- sonuç `releases/apk-kapasite.json` defterine yazılır.

Bu sürümün ölçümü: **7.112.417 bayt · 400 dosya · 17.504 sınıf · imza f3996c67…**
(9.55'e göre +124 bayt, sınıf/dosya kaybı yok). Kural `AGENTS.md` içine yazıldı.

## 4. Tablet

Tablet için ayrı derleme gerekmiyor: aynı APK (minSdk 26, `resizeableActivity=true`,
dp tabanlı arayüz). Klasörde **`UMAY-MULTI-TV-9.56-tablet.apk`** adıyla kopyası var;
binary telefon APK'sıyla birebir aynıdır (SHA-256 `4e53003…7aeb8c`), yalnız adı farklı.

## Doğrulama özeti

| Ne | Sonuç |
|---|---|
| Kapasite denetimi (9.55 → 9.56) | ✅ sınıf 17504 → 17504, dosya 400 → 400 |
| Ana ekran banner konumu | ✅ gezinme çubuğunun üstünde |
| Radyo araması (klavye açık) | ✅ sonuçlar görünüyor |
| Radyo çipleri (Tümü/…) | ✅ klavye açıkken görünüyor |
| Telefonda kurulum | ✅ `versionName=9.56` |
| Tablet kurulumu | ⏳ kullanıcıda (aynı APK) |
