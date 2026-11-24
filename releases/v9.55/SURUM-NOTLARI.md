# UMAY Multi TV 9.55 — sürüm notları

## Ses / görüntü uyumsuzluğu (düşük kalitede ses 4–7 sn erken)

**Sebep:** Kalite tercihi uygulanırken master listeden **tek bir varyant playlist URL'i**
seçilip oynatıcıya veriliyordu. Düşük varyantlarda ses ayrı rendition'dan geldiği için
oynatıcı sesi görüntüden bağımsız tamponluyor, ses görüntünün önüne geçiyordu. Ayrıca
reklam/bağlantı kopması sonrası aynı varyanta dönüş de bu kaymayı büyütüyordu.

**Düzeltme:** Oynatıcıya artık **master playlist** veriliyor; kalite sınırı ExoPlayer'ın
kendi track seçimiyle uygulanıyor (`Quality.applyPref` → `setMaxVideoSize`). Ses ve
görüntü aynı master'dan, senkron halde geliyor. Kanala özel seçim yapanlar için eski
davranış korunuyor.

## Radyo araması

- Klavye açıkken liste 0 yüksekliğe çöküyor, **arama sonuçları görünmüyordu** (ekran boş
  kalıyordu). Pencere artık klavye ile yeniden boyutlanmıyor (`adjustNothing`); sonuçlar
  klavyenin üstünde görünüyor.
- Arama Türkçe karakterden bağımsız: **"super" → "Süper FM"**, "radyo" → "Radyo 7".
- Arama yapılırken gizlenmiş istasyonlar da sonuçta görünüyor (arayan bulur).

## Diğer

- Kanal aramasında da aynı klavye davranışı düzeltildi.

## Notlar

- APK: `CanliTV.apk` · AAB: `app-release.aab`
- İmza: `CN=UMAY Multi TV`, SHA-256 `f3996c67…c92b8d5`
- Play'e yüklenecek sürüm: **versionCode 64 / versionName 9.55**
