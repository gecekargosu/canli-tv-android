# UMAY Multi TV — Play Kontrol Paketi 9.90

**Paket:** `com.cengiz.canlitv` · **9.90 (versionCode 97)** · minSdk 26 / targetSdk 36
**AAB:** `01-uygulama/UMAY-MULTI-TV-9.90.aab` (6,61 MB) · **APK:** `01-uygulama/CanliTV-9.90.apk` (3,85 MB)
**İmza SHA-256:** `f3996c67ef0e0e0420f442fe3fcf6303a17caa88dcba8a2b19a10cd95c92b8d5`

## Nerede ne var?

| Klasör | İçerik |
|---|---|
| `01-uygulama` | Play'e yüklenecek AAB, test APK'sı, sha256'lar |
| `02-play` | Mağaza kaydı, abonelik, uygunluk, yayın hazırlık, 9.90 yükleme rehberi |
| `03-gorseller` | 9.90 ekran görüntüleri (liste, oynatıcı, radyo) + ikon ve öne çıkan görsel |
| `04-politikalar` | Canlı sitedeki gizlilik / kullanım / telif metinleri + `app-ads.txt` |
| `05-analiz` | Claude raporu karşılaştırması (`IS-LISTESI-PLAY-KONTROL-2026-11-25.md`), test ve hata analizleri |
| `06-kaynak` | Manifest, `build.gradle`, `proguard-rules.pro`, `assets/`, `src/` |

## 9.90'de kapanan maddeler (özet)

- P0-1 abonelik beyanı üç yerde tutarlı (mağaza + gizlilik + kullanım koşulları).
- P0-2 izlerken 15 dakikalık tam ekran reklam **kaldırıldı** (yalnız 3. zap / 180 sn).
- P0-3 hedef kitle 13+, 5 çocuk kanalı çıkarıldı → **120 kanal**.
- P0-4 `app=` parametresi **0**; P0-5 harici logo **0** (baş harf rozeti).
- P1 oynatıcı hata sınıflandırması, çoklu ekran pencere durumu, açılış ölçümü.
- P2 R8 + shrinkResources: APK 7,38 → **3,85 MB**; CrashLog 512 KB döndürme.
- Site: gizlilik/kullanım metinleri ve `/indir` **9.90** olarak yayında.

## Bilinçli açık bırakılanlar

- RecyclerView dönüşümü (liste dokunma mantığı yeniden yazılmalı) — yayın sonrası.
- 80 sabit Türkçe metin (mağaza metni "diller kısmen" diyor).
- Tablet / Android 14-15 / yatay düzen testi, Firebase Analytics, mediation.
- Abonelik doğrulaması hâlâ istemcide (sunucu yok).
