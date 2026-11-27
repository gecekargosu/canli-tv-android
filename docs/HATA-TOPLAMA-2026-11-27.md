# Hata Toplama ve Gönderme — Mevcut Durum (v10.08)

Sorunun cevabı: **evet, var.** Uygulama hem çökme hem de "kanal açılmadı" gibi ölümcül
olmayan olayları topluyor, paketliyor ve iki yolla bize ulaştırıyor.

## 1. Ne toplanıyor

`CrashLog.java` üç ayrı kayıt tutar (hepsi cihazın kendi `files` klasöründe):

| Kayıt | Dosya | İçerik |
|---|---|---|
| Çökme | `crash.txt` | tam yığın izi (stack trace) + sürüm, tarih, cihaz modeli, Android sürümü, iş parçacığı |
| Olaylar | `events.txt` | satır satır: saat + etiket + mesaj (`[tv]`, `[radio]`, `[net]`, `[ad]`, `[log]`, `[guard]`, `[multi]`) |
| Yedek | `*.1.txt` | 512 KB'a ulaşınca önceki dosya silinmez, yedek olarak saklanır |

Kanal arızalarında yazılan gerçek örnekler (kodda mevcut):

```
[tv] oynatıcı hatası: ERROR_CODE_IO_BAD_HTTP_STATUS
[tv] hazır (1830 ms): TRT 1
[tv] yavaş açılış · yedek kaynağa geçiliyor: https://…
[tv] ölü: Show TV · HTTP 403 · denenen 3 adres
[net] bağlantı koptu
[radio] yedek adres deneniyor: https://…
[radio] yayın takıldı (12 sn): https://…
[ad] ödüllü reklam dolmadı: No fill
```

Yani "kanal neden açılmadı" sorusunun cevabı (hata kodu, HTTP durumu, kaç adres denendi,
açılış süresi, ağ durumu) günlükte zaten var.

## 2. Bize nasıl ulaşıyor

1. **Ayarlar → Hata günlüğü** (kullanıcı ekranı):
   - "Gönder" → e-posta/WhatsApp/Telegram ile paylaş (`ACTION_SEND`),
   - "Kopyala" → panoya alıp kullanıcı bize yazabiliyor,
   - "Temizle".
2. **Sunucuya otomatik gönderim** (`CrashLog.uploadIfDue`): **varsayılan KAPALI**;
   kullanıcı ayardan açarsa 20 saatte en fazla bir kez `POST /api/log`
   (`https://umay-multi-tv-gq3zer.freebuff.site/api/log`) adresine gider.
   Gövde: cihaz kimliği (ANDROID_ID'nin ilk 16 karakteri — kişisel veri değil),
   tarih, sürüm/kod, cihaz modeli, son 6000 karakter olay, son 4000 karakter çökme.
3. **Play Console** Android vitals: çökme/ANR istatistikleri (ayrıca).

## 3. Bilinen boşluklar (istersen sıradaki tur)

- **Kanala özel "bu kanal açılmıyor" düğmesi yok.** Kullanıcı kanal menüsünden
  tek tuşla "bu kanalı bildir" diyemiyor; Ayarlar'daki genel günlüğe güveniyoruz.
  (Kanal menüsüne 1 satır eklemek yeterli: `CrashLog.event("tv","kullanıcı bildirimi: …")` + paylaş.)
- Sunucuya gönderim **opt-in**; kapalı olduğu için sahadaki arızaların çoğu bize hiç gelmiyor.
  Play politikası gereği varsayılanı açmak için gizlilik metninde açık rıza gerekir.
- Günlükte tam yayın adresi var (kanal listesi herkese açık olduğu için sorun değil,
  ama gizlilik metninde "yayın adresleri" geçmiyor).
- `events.txt` 512 KB'ı aşınca eski dosya tek yedekle sınırlı; haftalarca açık kalan
  cihazlarda eski olaylar kayboluyor (kabul edilebilir).

## 4. Özet

Hata toplama ve paketleme altyapısı **var ve çalışıyor**: çökme + olay günlüğü,
sürüm/cihaz bilgisi, paylaş/kopyala, isteğe bağlı otomatik yükleme. Eksik olan tek şey
kullanıcının tek dokunuşla "bu kanal çalışmıyor" diyebilmesi; onu istersen 15 dakikalık
iş olarak eklerim.

## 5. v10.09: tek dokunuşla "bu kanal çalışmıyor" (eklendi)

Eksik olan son parça tamamlandı: kullanıcı artık kanalı açmadan da bildirebiliyor.

- Hata ekranında `📣 Bu kanal çalışmıyor · bildir` düğmesi (yalnız kanal kaynaklı
  hatalarda; internet/DNS hatasında çıkmaz).
- Kanal listesinde uzun bas → `📣 Çalışmıyor mu? Bildir`.
- Aynı kanal 10 dakikada bir (kazara çift dokunma).
- İnternet yoksa cihazda kuyruğa yazılır, sonraki açılışta otomatik gönderilir.
- Sunucu tarafı değişmedi: mevcut `/api/log` kaydının başına `📣 BİLDİRİM` satırı
  ekleniyor; otomatik gönderim ayarından bağımsız (kullanıcı kendi bastı).
- Ayrıntı: `docs/AI-DEGERLENDIRME-2026-11-27.md` sonundaki "v10.09'da uygulananlar".
