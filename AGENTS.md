# UMAY Multi TV — çalışma kuralları (her oturumda uyulacak)

Bu dosya, bu klasörde çalışan herkes (insan veya ajan) için **zorunlu** kuralları yazar.

## 1. Her güncellemede: kapasite (bit/bayt) denetimi — KESİN KURAL

Sürüm derledikten sonra, yayınlamadan **önce** şu komut çalıştırılır:

```bash
cd CANLI-TV-APP
python tools/verify-apk.py CanliTV.apk --prev releases/v<önceki>/CanliTV.apk --expect-version <yeni>
```

Bu araç şunları yazar ve karşılaştırır:

| Ölçüm | Neden |
|---|---|
| APK bayt | Dosya gerçekten büyüdü/küçüldü mü |
| zip dosya sayısı | Kaynak/varlık kaybı (ör. bir ekran, bir ses dosyası) |
| açılmış (uncompressed) bayt | Sıkıştırma yanıltmasın |
| `classes*.dex` sınıf sayısı | **Kod kaybı** — düşerse hata verir |
| imza SHA-256 | Yanlış anahtar / farklı APK karışması |
| versionName / versionCode | Yanlış sürüm numarası |

- **Sınıf sayısı veya dosya sayısı düştüyse sürüm yayınlanmaz.** Önce neden düştüğü bulunur.
- Sonuçlar `releases/apk-kapasite.json` defterine otomatik yazılır; sürümler arası
  fark oradan da izlenebilir.
- "Güncelleme sonrası veri kaybı" şüphesinde ilk bakılacak yer bu defterdir.

## 2. Her sürümde: dört parça birlikte

1. `releases/vX.YY/` → `CanliTV.apk`, `app-release.aab`, `SURUM-NOTLARI.md`,
   (istenirse) `UMAY-MULTI-TV-X.YY-tablet.apk`
2. GitHub release `vX.YY` (APK eki)
3. Site `/indir` bağlantısı yeni sürüme güncel + deploy
4. `docs/MASTER-IS-LISTESI-2026-11-24.md` → madde satırı + oturum raporu

## 3. Her oturum sonunda: liste disiplini

- Bitirdiğin her maddenin **altına ne yaptığını yaz** (dosya, davranış, test).
- Doğrulayamadıklarını `docs/DOGRULANACAKLAR.md` içine **kanıtıyla** yaz;
  "geçti" deme, "kısmen" veya "yapılamadı + neden" yaz.
- Oturum raporunda: kaç madde bitti, toplam kaç madde kaldı, sonraki oturum ne yapacak.

## 4. Cihaz testi

- Telefon: MIUI `Z5R87DU8V4LBSOVG`, `adb install -r -d CanliTV.apk`.
- Release APK'da `run-as` yoktur; tercihler ekrandan/günlükten doğrulanır.
- MIUI `adb shell svc wifi disable` komutunu yok sayar; ağ kesme testi **elle uçak moduyla**.
- Reklam davranışı 15 dakikalık döngüde olduğu için reklam testleri ayrı oturumda yapılır.

## 5. Derleme

```bash
MSYS_NO_PATHCONV=1 bash build.sh      # CanliTV.apk (debug anahtarı, üzerine kurulabilir)
MSYS_NO_PATHCONV=1 bash make-aab.sh   # Play için CanliTV.aab
MSYS_NO_PATHCONV=1 bash tools/claude-zip.sh
```
