# Sürüm 9.96 (sürüm kodu 105) — 26 Kasım 2026

Bu klasörde o sürümün son hâli duruyor: `CanliTV.apk`, `app-release.aab`.

## 1. Uygulama düzeltmesi: 🗑 Gizlenenler sekmesi boştu

**Belirti:** Listeden silinen kanal "Gizlenenler" sekmesinde görünmüyordu, yani silen
kullanıcı kanalı geri getiremiyordu. Uygulama "Gizlenenler'den geri alabilirsin" diyordu
ama sekme her zaman boştu.

**Neden:** `MainActivity.refresh()` gizli sekmede de kategori filtresi uyguluyordu
(`c.group.equals("🗑 Gizlenenler")` hiçbir kanalda tutmuyor).

**Çözüm:** Filtre gizli sekmede atlanır (`if (!"Tümü".equals(group) && !onlyHidden)`).

**Doğrulama:** ATV + TV8 seçilip silindi → Gizlenenler'de ikisi göründü → geri eklendi
→ liste yine 120 kanal. Kapasite denetimi temiz (sınıf +0, dosya +0, boyut +40 bayt).
9.95'te aynı adımda liste boş kalıyordu.

| Dosya | Değişiklik |
|---|---|
| `src/com/cengiz/canlitv/MainActivity.java` | gizli sekmede kategori filtresi kaldırıldı |
| `app/build.gradle` | `versionCode 105`, `versionName '9.96'` |
| `src/com/cengiz/canlitv/Billing.java` | Play Faturalandırma 8.0.0 API'leri (9.95'te başlayan zorunlu geçiş) |

## 2. Yayın

- **GitHub:** https://github.com/gecekargosu/canli-tv-android/releases/tag/v9.96
  (`CanliTV-9.96.apk` ve `UMAY-MULTI-TV-9.96.aab`)
- **Commit:** `4a6d54c` — kod + sürüm notları `main` dalında
- **Play:** AAB hazır, Play Console → Kapalı test → Sürüm oluştur → incelemeye gönder (sende)
- Telefona `adb install -r -d` ile kuruldu (105 / 9.96)

## 3. Tanıtım videosu v3 (yazı + ok + çember)

`tanitim/UMAY-MULTI-TV-tanitim-v3.mp4` — 3:33, 1280×2600, ses yok (müzik sen ekleyeceksin).

- 22 noktada video **duruyor**, ekranda "DUR" rozetli panel + açıklama çıkıyor,
  okuma süresi kadar kalıyor, sonra kaldığı yerden devam ediyor.
- Sıralama: kanal listesi/arama → favoriler → listeyi düzenle (sırala, sil, geri al)
  → çoklu ekran (pencereye kanal seç) → kayıt → sadece ses → kilitli ekran
  → kayıtlarım → radyo/ekolayzır → ekonomi modu → reklamsız abonelik → ülke değiştir → kapanış.
- Turuncu **ok + çember** yalnız koordinatı kesin bilinen düğmelerde: listedeki oynat oku,
  "Favorilere ekle" menü satırı, düzenleme çubuğu, Gizli sekmesi, "Kanal seç" listesi.
- Eski çekimlerdeki "Sürüm 9.95" yazısı kapatıldı (uygulama artık 9.96).
- 4'lü ekran bölümü, reklamların pane'leri kapladığı eski çekim yerine **"Kanal seç"
  diyaloğunun** temiz görüntüsünden yeniden kesildi.
- Üretim betiği: `_tmp/vid/build3.py` (ipuçları `CUES` sözlüğünde, tek yerden değişir).

## 4. Play Integrity API — durum

Henüz açık değil. Cloud proje numarası + Play Console → App integrity → Play Integrity
adımı senden gerekiyor; açıldığında sunucuda "gerçek cihaz + gerçek uygulama" doğrulaması
yapılabilir (kopya/emülatör trafiğini ayıklamak için).
