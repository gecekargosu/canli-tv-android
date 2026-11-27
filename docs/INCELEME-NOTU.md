# İnceleme Notu (kod paketi)

Bu paket, **UMAY Multi TV / Canlı TV** Android uygulamasının 10.08 (versionCode 117)
sürümünün kaynak kodudur. Amaç: dışarıdan bir gözle mimari, hata riski ve öneriler.

## Paket içeriği

- `src/com/cengiz/canlitv/*.java` — tüm Java kaynakları (yaklaşık 40 sınıf, ~16.000 satır)
- `res/`, `assets/` — kaynaklar; `assets/i18n.tsv` 8 dilli çeviri tablosu (537 anahtar)
- `app/build.gradle`, `AndroidManifest.xml` — yapılandırma
- `tools/` — derleme, doğrulama, Play yükleme, i18n denetim scriptleri
- `docs/` — hata analiz raporu, teknik borç raporu, AI değerlendirmesi, bu not

Pakette **yok**: imza anahtarı (`upload-keystore.jks`), `keystore.properties`,
`local.properties`, derleme çıktıları (build/, *.apk, *.aab), görseller.

## Uygulama hakkında kısa bilgi

- Java (Android SDK), Gradle, minify açık (R8), tek modül `app`.
- Yayın kaynağı: uzak kanal kataloğu (`backup_tr.json`, `backup_radio.json` + ülke listeleri),
  ExoPlayer/Media3 ile canlı yayın.
- Reklam: AdMob 25.5.0 (banner + geçiş + ödüllü) + abonelik (Play Billing).
- Öne çıkan sınıflar:
  - `PlayerActivity` (1492 satır) — TV oynatıcı, yedek kaynak, hata yakalama
  - `MainActivity` (1240 satır) — kanal listesi (RecyclerView), filtre, düzenleme
  - `MultiActivity` (1130 satır) — 3'lü mozaik izleme
  - `AudioService` (972 satır) — arka plan radyo, yedek adres, veri limiti
  - `SettingsActivity` (1294 satır) — ayarlar, hata günlüğü ekranı
  - `CrashLog` — çökme + olay günlüğü, isteğe bağlı sunucuya yükleme
- Son turlarda yapılanlar: MainActivity bölündü (`MainUi`, `MainHeader`, `ChannelRow`,
  `TvFocus`), D-pad/kumanda odak desteği ve rakamla kanal açma eklendi,
  çeviriler 8 dile tamamlandı, AdMob 24.4.0 → 25.5.0.

## Özellikle bakılmasını istediğimiz başlıklar

1. **PlayerActivity**: hata/kurtarma akışı, bellek sızıntısı riski, yaşam döngüsü.
2. **MainActivity**: hâlâ 1240 satır — hangi parçalar daha temiz ayrılır?
3. **AudioService**: ön plan servisi + MediaSession + veri limiti etkileşimi.
4. **CrashLog**: kişisel veri / gizlilik açısından sorun var mı?
5. **Genel**: R8 sonrası riskli yansıma, thread/Handler kullanımı, ana iş parçacığı işleri.
6. **AI önerisi**: `docs/AI-DEGERLENDIRME-2026-11-27.md` — bu değerlendirmeye katılıyor musunuz,
   eksik gördüğünüz fikir var mı?

Not: Proje Türkçe yorumlarla yazılmıştır; sınıf/metot adları İngilizce, kullanıcı metinleri
`I18n` üzerinden 8 dilde tutulur (anahtar = Türkçe metin, `assets/i18n.tsv`, `§` ayraç).
