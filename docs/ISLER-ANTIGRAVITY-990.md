# İş listesi — "Antigravity Derin Analiz Raporu – UMAY‑MULTI‑TV"

Kaynak: `Antigravity Derin Analiz Raporu – UMAY‑MULTI‑TV.txt` (86 satır, 9.70 ZIP'i üzerinde).
Rapor cihazda denemiş görünüyor ancak **birkaç iddiası eski sürümden** (hesap sistemi, tek oynatıcı).
Aşağıda her madde koda bakılarak doğrulandı: ✅ doğru · ❌ yanlış/eski · ❓ veriye bağlı.

| # | İş | Raporun gerekçesi | Doğrulama | Öncelik |
|---|----|-------------------|-----------|---------|
| ANT-01 | Çoklu ekran MediaPlayer yerine ExoPlayer kullansın | Üçlü ekranda kalite/veri sayacı tutarsız | ✅ doğru: `MultiActivity` `android.media.MediaPlayer` | P1 |
| ANT-02 | Radyo kapanınca kayıt (HlsRecorder) kapanmıyor, dosya birikiyor | Hafıza/alan sızıntısı | ✅ `HlsRecorder` radyo kapanışına bağlı değil | P1 |
| ANT-03 | Çoklu ekranda geri tuşu ana listeye dönmeli | Kullanıcı karışıklığı | ✅ `MultiActivity`de özel geri davranışı yok | P1 |
| ANT-04 | Kalite 720p/1080p seçilince bazı kanallarda "stream bulunamadı" | Varyant gerçekten yok | ✅ kısmen: M-96 sonrası etiket doğru ama otomatik düşüş yok | P1 |
| ANT-05 | Kalite seçilince veri tasarrufu otomatik kapansın/açılsın (tutarlılık) | Tasarruf modu kalıcı kapanıyor | ✅ `setDataSaver` yalnız Ayarlar'dan değişiyor | P1 |
| ANT-06 | Liste düzenlemede "kaydet" bazen yanıt vermiyor; silinen geri eklenince UI donuyor | Kullanılabilirlik | ❓ cihazda yeniden üretilecek | P1 |
| ANT-07 | Üst bar ikonları 48dp dokunma alanı | Erişilebilirlik | ✅ ikonlar küçük | P1 |
| ANT-08 | Ayarlar'da "kullanım verisi" satırı ekranı taşırıyor | Okunabilirlik | ✅ uzun satır | P2 |
| ANT-09 | `READ_MEDIA_VIDEO` izni manifestten kaldırılsın | Play izin beyanı | ❌ **yanlış**: manifestte bu izin yok, READ/WRITE_EXTERNAL_STORAGE `maxSdkVersion=28` | — |
| ANT-10 | Hard-coded stringler → `strings.xml` | Çoklu dil | ✅ (PDF-11 ile aynı iş) | P1 |
| ANT-11 | MainActivity 49 KB monolit → MVP/MVVM refactor | Bakım | ✅ doğru ama büyük iş | P3 |
| ANT-12 | ListView → RecyclerView | 300+ öğede kaydırma | ✅ doğru | P2 |
| ANT-13 | Keystore şifresi düz metin (`keystore.properties`) → ortam değişkeni | Sızıntı riski | ✅ doğru | P2 |
| ANT-14 | Reklam sıklığına frekans sınırı (frequency capping) | Agresif reklam | ✅ (PDF-05 ile aynı iş) | P0 |
| ANT-15 | Rewarded "60 dk reklamsız" bağlantısı çalışmıyor | Gelir kaybı | ❌ **yanlış**: `AdManager.showRewarded` bağlı; 9.70'te ödüllü akış var | — |
| ANT-16 | Test reklamı ekran görüntüleri mağazada görünmesin | Play reddi | ✅ (PDF-15 ile aynı) | Kullanıcı |

## Raporun yanlış/eski iddiaları (dikkat)
1. **"MediaPlayer doğrudan kullanılmış"** → tek ekran `media3`/ExoPlayer; MediaPlayer yalnız çoklu ekran ve radyoda (ANT-01, gerçek iş).
2. **"Hesap sistemi yok / Google Sign-In eksik"** → hesap sistemi **kaldırıldı** (9.21, P0-4). Rapordaki "OAuth yapılandırması bulunamadı" hatası eski sürüme ait; `Account`/`Api` sınıfları yok.
3. **"İndirme sayfası eski 9.0 APK gösteriyor"** → `/indir` şu an `v9.70/CanliTV-9.70.apk` veriyor (curl ile doğrulandı).
4. **"Backend `http://`"** → tüm adresler `https://umay-multi-tv-gq3zer.freebuff.site`.
5. **"`READ_MEDIA_VIDEO` isteniyor"** → istenmiyor (manifest yorumu bunu açıkça yazıyor).
6. **"Kaydet düğmesi yanıt vermiyor / UI donuyor"** → gerçek olabilir, cihazda üretilmesi gerekiyor (ANT-06).

## Raporda doğru çıkan "kullanıcı olarak karşılaşılabilecek" maddeler
- Kanal listesi boş görünmesi (ülke değişiminde) → M-33 sonrası risk azaldı, izlenecek.
- Kalite geçişinde "stream not found" → ANT-04.
- Çoklu ekran geri tuşu → ANT-03.
- AdMob "limited ad serving" → Play yayını olmadan normal (gelir maddesi, kod işi değil).
- Kayıt durdurulmazsa dosya birikmesi → ANT-02.
