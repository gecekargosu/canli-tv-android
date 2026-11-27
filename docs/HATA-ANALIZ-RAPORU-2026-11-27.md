# Hata Analizi Raporu — 27 Kasım 2026 (v10.06 çalışma derlemesi)

Senaryo: `docs/HATA-ANALIZI-SENARYOSU-2026-11-27.md`
Bu turda **Faz 0–1** (envanter + statik analiz) ve **kısmi Faz 2** (cihaz doğrulaması) çalıştırıldı.

## Ölçümler (kanıt)
| Ölçüm | Değer |
|---|---|
| Sürüm | 10.06 (115) — 10.05 Play alpha'da, bu derleme cihazda |
| `build.sh` | BUILD SUCCESSFUL |
| `verify-apk.py` | TEMİZ · 3.959.108 bayt · 395 dosya · 7044 sınıf · imza `f3996c67…b8d5` |
| Android Lint | **89 error / 106 warning** (rapor: `app/build/reports/lint-results-publicApk.txt`) |
| i18n tablosu | 425 anahtar · 0 bozuk satır · **0 tekrar** · 0 boş çeviri |
| i18n kapsamı | **126** görünür metin hâlâ `I18n.t` dışında |
| Cihaz | Telefon + tablet 10.06 kurulu; logcat'te FATAL/ANR yok |

## Bulgular ve durum

### Kapandı (bu turda düzeltildi)

**B-01 · P1 · Çift çeviri anahtarı diyalogları bozuyordu — DÜZELTİLDİ**
`assets/i18n.tsv` içinde `Kaydet` iki kez vardı (biri "Save", biri alt bar için kısa "Rec").
Haritada son satır kazandığı için **Kaydet düğmesi 7 dilde "Rec/Aufn./Enr." görünüyordu**.
Düzeltme: kısa satır silindi, alt bar `Kayıt` anahtarına alındı, Ayarlar bölümü yeni
`Kayıt ve Yayın` anahtarına taşındı. Doğrulama: `Kaydet→Save`, `Kayıt→Rec`, `Kayıt ve Yayın→Recording & streaming`.

**B-02 · P1 · Locale'e bağlı `toLowerCase/toUpperCase` (13 yer) — DÜZELTİLDİ**
Türkçe cihazda `"IT".toLowerCase()` → `"ıt"`: **İtalya kanallarının bayrağı/ülke kodu bozuluyordu**;
Romence/Rusça gibi dillerde arama da etkilenebiliyordu. `Locale.ROOT` eklendi
(`Channel`, `Countries`, `RadioRepo`, `RadioActivity`, `AdsTest`, `SettingsActivity`).

**B-03 · P1 · Çoklu ekran arama filtresi — DÜZELTİLDİ**
`MultiActivity` filtresi `toLowerCase()` ile karşılaştırıyordu (TR'de "İ" araması tutmuyordu);
`Lang.fold()` kullanılıyor (ı/i, ş/s, ğ/g eşitlemesi).

**B-04 · P2 · Saat biçimi Arapça locale'de Arap rakamı — DÜZELTİLDİ**
`String.format("%02d:%02d")` → `Locale.ROOT`.

**B-05 · P2 · `SpectrumView` her karede allocation — DÜZELTİLDİ**
`onDraw` içinde çubuk başına `new LinearGradient`, `new RectF`, `Color.parseColor`. 8 çubuk × 60 fps ≈ 480 allocation/s → GC duraklamaları. Gradyanlar önbelleklendi (6 px eşiği), tepe çubuğu tek `RectF` ile çiziliyor.

**B-06 · P2 · `RingGauge` her karede allocation/parse — DÜZELTİLDİ**
Renkler `static final`, gradyan yalnız boyut değişince üretiliyor, iç halka `RectF` yeniden kullanılıyor.

**B-07 · P2 · Android 12+ PiP geçişi — DÜZELTİLDİ**
`setAutoEnterEnabled(true)` + `setSourceRectHint(video)` eklendi (yumuşak PiP animasyonu).

**B-08 · P2 · Diyalog "Kaydet" sabit metindi — DÜZELTİLDİ**
`MultiActivity` ve `SettingsActivity` içindeki `setPositiveButton("Kaydet")` → `I18n.t(this, "Kaydet")`.

### Açık (öncelik sırası)

| No | Önem | Bulgu | Kanıt | Plan |
|---|---|---|---|---|
| B-09 | P2 | **126 görünür metin çevrilmemiş**: MultiActivity 22, RadioPlayerActivity 16, PlayerActivity 15, MainActivity 13, QualityDialog 6, AboutActivity 1, AdManager 1 | `tools/i18n-kapsam.py` | Tabloya satır ekle + `I18n.t` sarması (mekanik, 2 oturum) |
| B-10 | P2 | `EqFader.onDraw` gradyanı knob konumuna bağlı → her karede allocation | lint `DrawAllocation` | Kabul edildi (küçük nesne); istenirse Path tabanlı çizime geçilir |
| B-11 | P3 | Lint `SetTextI18n` 54, `NotifyDataSetChanged` 6, `ObsoleteSdkInt` 7 | lint | B-09 ile birlikte temizlenir |
| B-12 | P3 | `onBackPressed` (GestureBackNavigation), sabit yön (Android 16'da yok sayılır), `ANDROID_ID` (HardwareIds), `navigation_bar_height` internal resource | lint | AndroidX back dispatcher + WindowInsets |
| B-13 | P2 | Bağımlılık: `play-services-ads 24.4.0` → **25.5.0** var; lint `OldTargetApi` | lint `GradleDependency` | Yükseltme ayrı sürümde denenmeli (reklam SDK'sı davranış değiştirebilir) |
| B-14 | P1 | Tablet kapanmasının **kesin kök nedeni** yok (korumalar eklendi, repro yok) | dropbox/logcat temiz | Kullanıcıdan "Hata bildir" günlüğü gelince kök neden |
| B-15 | P2 | R8/minify: release AAB küçültme kuralları yalnız reklam SDK'sı için doğrulandı | `proguard-rules.pro` | AAB'yi test cihazına kurup satın alma/PiP/kayıt dumanı |

## Sıradaki adım (öneri)
1. **B-09** (126 metin) — çeviri borcunu kapatır, aynı anda B-11'i de azaltır.
2. **B-15** (R8 duman testi) — Play'e giden AAB'nin davranışını doğrular.
3. **B-13** (AdMob 25.5.0) — ayrı sürümde, ödüllü reklam ve UMP akışı elle test edilerek.
4. Teknik borç (RecyclerView, MainActivity bölme, TV/D-pad) — ayrı paket.
