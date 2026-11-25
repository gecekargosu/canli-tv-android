# UMAY Multi TV 9.72 — sürüm notları

Tarih: 25 Kasım 2026 · versionCode 81 · versionName 9.72
APK 7.122.869 bayt · 400 dosya · 17.520 sınıf · imza `f3996c67…b8d5`
AAB 6.779.293 bayt.

## Bu sürümde ne değişti

### Hata ve kopma günlüğü (yeni)
1. **Ayarlar → "Hata günlüğü".** Uygulama artık şunları zaman damgasıyla kaydediyor:
   - `[app] açılış 9.72 · ülke=tr · ekonomi=1 · dil=tr` — hangi sürüm, hangi mod.
   - `[tv] hazır: <kanal>` — hangi kanal ne zaman açıldı.
   - `[tv] oynatıcı hatası: ERROR_CODE_… · açıklama` — ExoPlayer'ın teknik hata kodu.
   - `[tv] hata, tekrar deneniyor n/3` ve `15 sn ses yok` — zayıf bağlantıda kaç kez denendiği.
   - `[tv] ölü: <kanal> · <neden> · son hata=… · <adres>` — kanal ölü sayıldığında nedeni.
   - `[net] bağlantı koptu` — ağ koptuğunda.
   - `[ad] geçiş reklamı yüklenemedi: <kod> <mesaj>` — reklam dolmadığında (ör. `3 No fill`).
   - Çökme olursa tam yığın izi (`crash.txt`).
2. Ekranda okunabilir, seçilebilir, kaydırılabilir günlük; **📧 Gönder** (destek e-postası),
   **Temizle**, **Kapat**. Günlük 96 KB'ı geçince kendini sıfırlar, en fazla 6000 karakter gösterilir.

### Repo/depo temizliği (uygulamayı etkilemez)
- Ajan sohbet dosyaları (`FREEBUFF İLE YAPTIĞIMIZ KONUŞMA…`, `chatgpt önerileri…`, `CLAUDE-README.md`) silindi.
- Raporlar `docs/raporlar/` altına taşındı.
- Derleme çöpü (`_build/`, `build/`) ve büyük ham video (`pazarlama/ham-kayit.mp4`) takipten çıkarıldı.
- Sürüm ikilileri (APK/AAB) artık repoda tutulmuyor; **GitHub Releases**'te duruyor.
  Bu klasördeki dosyalar yerel arşiv olarak kalır.

## Doğrulama

- `verify-apk --prev releases/v9.71/CanliTV-9.71.apk --expect-version 9.72` → ✅ temiz
  (sınıf +2, dosya +0, imza aynı).
- Cihazda (Xiaomi 2506BPN68G, Android 16, **ekonomi modu açık**, mobil veri/zayıf Wi-Fi):
  - Uygulama 9.72 açıldı; günlükte `[app] açılış 9.72 · ülke=tr · ekonomi=1 · dil=tr` göründü.
  - TRT 1 açıldı; `[tv] hazır: TRT 1` yazıldı.
  - Reklam dolmadığı için `[ad] geçiş reklamı yüklenemedi: 3 No fill.` kaydı düştü —
    reklam sayacının neden artmadığını da bu satır açıklıyor.
  - `docs`'taki eski kayıtlar da okunabildi (11-24 BLK tekrar döngüsü 30 satır, 11-25 RFO denemeleri).

## Bilinen eksikler
- Ölü kanal satırının (`[tv] ölü: …`) cihazda yeni sürümle yeniden üretilmesi bu turda
  yapılmadı; aynı yol 9.71'de RFO/BLK ile doğrulanmıştı.
- Reklam sıklığı (3 zap/180 sn) hâlâ gözle doğrulanamıyor: AdMob "No fill" dönüyor.
- Play Console adımları (AAB yükleme, mağaza metni, 12 tester) kullanıcıda.
