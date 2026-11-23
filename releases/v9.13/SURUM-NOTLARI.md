# UMAY Multi TV — v9.13 (versionCode 22)

23 Kasım 2026 · `CanliTV-9.13.apk` · `CanliTV-9.13.aab`

## Yeni: reklamlarda yetişkin/erotik içerik engeli

- Uygulama AdMob'a artık **"yalnızca genel izleyiciye uygun (G) reklam"** talebi gönderiyor:
  `RequestConfiguration.setMaxAdContentRating(MAX_AD_CONTENT_RATING_G)`.
- Bu ayar **kodun içinde**, her istekte geçerli (yalnızca telefonda değil, tüm kurulumlarda).
- "Afterglow" gibi öpüşme/yetişkin temalı reklamlar bu sınıra takıldığında **gösterilmez**;
  AdMob yerine uygun reklam verir, uygun reklam yoksa bar boş kalmaz (kendi tanıtım barımız devreye girer).
- Ayrıca çocuklara yönelik/yaş altı işaretleri "belirtilmemiş" olarak bırakıldı (yanlış beyan olmasın).

> Ek katman (AdMob panelinden, hesap sahibi tarafından): **Blocking controls → Sensitive categories**
> altında *Dating* ve *Sexual content* kapatılabilir. Panel tarafı da yapılacak (iş listesi #68).

## Test
| Test | Sonuç |
|---|---|
Derleme (APK + AAB) | ✅ |
Kurulum (versionCode 22) | ✅ |
Reklam isteği yapılandırması | ✅ kod içinde (setMaxAdContentRating G) |
