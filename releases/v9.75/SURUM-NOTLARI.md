# UMAY Multi TV 9.75

**versionCode 84 · versionName 9.75**

Bu sürüm, 9.74'ten sonra bildirilen üç sorunu kapatır: ana sayfanın üstündeki boşluk,
TV8'in listeden kaybolması ve Show TV'nin hiç olmaması.

## 1. Ana sayfa kayması (üst boşluk) giderildi

- Belirti: durum çubuğu ile başlık arasında ~55 dp boşluk; liste 9.74'e göre bir satır
  aşağıdan başlıyordu.
- Neden: kök yerleşimde `fitsSystemWindows(true)` durum çubuğu payını **ikinci kez**
  ekliyordu (pencere zaten güvenli alanın altında başlıyor).
- Çözüm: `root.setFitsSystemWindows(false)`; tek pay kalıyor.
- Cihazda doğrulandı (1280×2772): başlık durum çubuğunun hemen altında başlıyor,
  aradaki boşluk yok. Önce/sonra: `_build/home_now.png` → `_build/v975_home2.png`.

## 2. TV8 geri geldi

- Belirti: kanal listesinde 1 numara yoktu; "tv8" araması "Bu filtrede kanal yok" diyordu.
- Neden: TV8 iki geçici yayın hatası aldığı için "ölü kanal" sayacı (`hideDead`) onu
  listeden gizlemişti (Gizlenenler sekmesi boştu, yani elle silinmemişti).
- Çözüm: sabit kanallar (ATV, TV8, Show TV, Star TV, Kanal D) otomatik gizlemeden muaf.

## 3. Show TV listeye eklendi

- Kaynak: TürkNet/ERC yansısı (`showtv.m3u8` master + 720p yedek), 25.11.2026'da
  HTTP 200 ve 4 kalite varyantı (1080/720/480/360) ile doğrulandı.
- Yedek zinciri `ChannelRepo.FALLBACK` içinde; logo Wikipedia'daki güncel Show TV logosu.

## 4. Kanal sırası ve numaralar

- Türkiye listesinde ilk beş sabit: **1 ATV · 2 TV8 · 3 Show TV · 4 Star TV · 5 Kanal D**;
  kullanıcının kendi eklediği kanallar yine en başta durur.
- Eski sürümlerden kalan numara/el sırası kayıtları bir kerelik temizlenir
  (`Prefs.migrateNumbers`), böylece "iki kanal №1", "boş №1" gibi çakışmalar bitiyor.
- Cihazda doğrulandı: 1 ATV, 2 TV8, 3 Show TV, 4 Star TV, 5 Kanal D, 6 kendi kanalı,
  7 TRT Haber… (toplam 128 kanal).

## 5. Kapasite denetimi

```
✅ Kapasite denetimi TEMİZ
sürüm      : 9.75 (kod 84)
dosya      : 400 | sınıf: 17.530 | imza f3996c67…b8d5
```

## Bilinen açık işler

- **ATV**: kullanıcı tarafından sonraya bırakıldı; adres 9.75'te değişmedi.
- Site `/indir` ve GitHub release bu sürüm için henüz güncellenmedi (yerel arşiv hazır).
