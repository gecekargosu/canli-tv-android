# UMAY Multi TV 10.09 (118) — Sürüm Notları

Tarih: 28 Kasım 2026 · Play: **alpha (kapalı test)**, versionCode 118

## Ne geldi

### 1. "Bu kanal çalışmıyor · bildir" düğmesi
Kullanıcı artık iki yerden tek dokunuşla bildirebiliyor:

- **Kanal açılmadığında**: hata ekranının üstünde turuncu düğme.
- **Listeden**: kanala uzun bas → menüde `📣 Çalışmıyor mu? Bildir`.

Kurallar (bilerek böyle):

- Düğme **yalnızca kanal kaynaklı** hatalarda çıkar. İnternet yokken veya DNS
  çözümlenemediğinde çıkmaz — o durumda sorun kanalda değil.
- Aynı kanal için **10 dakikada bir** bildirim (kazara çift dokunma koruması).
- **İnternet yoksa kaybolmaz**: bildirim cihazda kuyruğa yazılır, uygulama bir
  sonraki açılışında veya bir sonraki başarılı gönderimde kendiliğinden yollar.
- Sunucu tarafı **değişmedi**: mevcut `POST /api/log` uç noktası kullanılıyor,
  bildirim günlük kaydının başına `📣 BİLDİRİM · kanal=… · sebep=… · detay=…`
  satırı olarak ekleniyor. Site yeniden yayınlanmadan çalışır.
- Rıza: düğmeye basmak kullanıcının kendi eylemi olduğu için otomatik gönderim
  ayarından (varsayılan kapalı) bağımsız çalışır.

### 2. `StreamDiag` — hata teşhisi tek yerde
PlaybackException kodları 13 sınıfa ayrılıyor (403 / 404 / 429 / 5xx / DNS /
timeout / codec / DRM / bozuk akış / canlı kenar / internet yok / bilinmiyor) ve
hata ekranında **sebep + öneri** gösteriliyor:

> Bu kanal şu an açılamadı.
> Yayıncı bu yayını engelliyor (403)
> Kanal kaynaklı · biraz sonra tekrar dene veya başka kanal dene

8 dilde metinler `assets/i18n.tsv`'ye eklendi (27 yeni satır).

### 3. "İlk kare" ölçümü
"Hazır" demek "kare geldi" demek değildi. Artık ilk kare ancak şu üçü birlikte
olursa doğrulanıyor: `READY` + gerçek video izi (yükseklik > 0) + 10 saniye
kesintisiz oynatma. Doğrulanırsa günlüğe `ilk kare doğrulandı: 10 sn · 720p · KANAL`
yazılır. 15 saniyede doğrulanamazsa `video izi yok` / `kare gelmedi (dondu)` olarak
kaydedilir ve bildirim düğmesi açılır. **Hiçbir durumda kanal ölü işaretlenmez**
(yanlış eleme olmasın).

### 4. Teşhis kolaylığı
`CrashLog.event` satırları artık logcat'e de düşüyor: `adb logcat -s UmayEvent`.
Cihazda hata ayıklarken dosya okumak gerekmiyor.

## Doğrulama (cihazda, gerçek senaryolar)

| Senaryo | Sonuç |
|---|---|
| Menüden bildir (internet var) | `bildirildi` → `bildirim gönderildi` (HTTP 2xx) |
| Aynı kanal 10 dk içinde tekrar | `atlandı: 10 dk içinde bildirilmişti` + uyarı |
| Uçak modu + Wi-Fi kapalı, kanal aç | "İnternet bağlantısı yok" ekranı, **bildirim düğmesi yok** |
| Uçak modunda menüden bildir | `bildirim kuyrukta (gönderilemedi)` |
| İnternet geri gelince uygulama açılışı | `kuyruk gönderildi: 1` → `bildirim gönderildi` |
| 403 hata ekranı (test kancası) | düğme göründü, basınca gönderildi |
| Kanal açılışı | `ilk kare doğrulandı: 10 sn · 720p · ATV` |
| `StreamDiagTest` (masaüstü, 26 test) | `SONUC: TEMIZ` |
| Telefon + tablet kurulum | 10.09, açılışta FATAL/ANR yok |
| `tools/check-all.sh --skip-build` | `SONUC: TEMIZ (10.09)` |

## Bilinen sınırlar

- Play'de "İncelemeye gönder" adımı API ile yapılamıyor, Console'dan yapılmalı.
- Oynatıcıdaki düğme gerçek bir 403/404 kanalında değil, geçici test kancasıyla
  doğrulandı (test kancası sürümden çıkarıldı).
- `MainActivity` hâlâ 1240 satır; `refresh()`/filtre ve düzenleme modu ayrımı
  bir sonraki tura kaldı.

## Teknik

- APK 4.476.055 bayt · AAB 7.733.535 bayt · imza `f3996c67…b8d5`
- 429 dosya, 7866 sınıf (10.08'e göre +3 sınıf, +10.172 bayt)
- Yeni dosyalar: `StreamDiag.java`, `ChannelReport.java`, `tools/StreamDiagTest.java`
