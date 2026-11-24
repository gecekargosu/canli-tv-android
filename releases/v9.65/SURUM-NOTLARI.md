# UMAY Multi TV 9.65 — sürüm notları

Tarih: 24 Kasım 2026 · versionCode 74 · versionName 9.65

Bu sürüm yeni özellik getirmez; **hata analizi turunda bulunan çökme ve yanlış
davranışların düzeltmesidir**. Amaç: kod "çalışıyor" değil, **doğru çalışsın**.

## Kritik düzeltmeler (kullanıcıyı doğrudan etkileyen)

1. **Radyo 12 saniyede bir kendini yeniden başlatıyordu.**
   `AudioService.monitor` içinde önce `lastRx` güncelleniyor, sonra `rx - lastRx`
   ölçülüyordu; fark her zaman 0 çıkıyor ve yayın "takıldı" sanılıp yeniden
   bağlanılıyordu. Fark artık güncellemeden önce alınıyor. (Cihazda 26 saniye
   log izlendi: tek bir yeniden bağlanma yok.)
2. **Ülke indirmesi bitince uygulama çöküyordu.**
   `CountryActivity.pick()` indirme sonrası `CountryRepo.fetchGood()` ağ çağrısını
   UI iş parçacığında yapıyordu → `NetworkOnMainThreadException`. Çağrı arka
   plana alındı. (ABD 263 kanal, Almanya 113 kanal indirildi; çökme yok.)
3. **Ülke değiştirince arkadaki oynatıcı çökebiliyordu.**
   `ChannelRepo.all()` statik listeyi döndürüyordu; ülke değişince liste
   boşalıyor, `PlayerActivity`/`MultiActivity`/`RadioPlayerActivity`
   `get(index)` ile çöküyordu. Ekranlar artık kendi kopyasını tutuyor.
4. **Kanal/radyo listesi boşsa çökme.** `list.get(0)`, `idx % list.size()`
   (ArithmeticException) ve `Slot.setChannel(list.size()-1)` negatif indeks
   durumları korumaya alındı; boş liste için anlaşılır mesaj gösteriliyor.
5. **Radyo uyku zamanlayıcısı ekran kapanınca kayboluyordu.**
   Sayaç ekranın Handler'ında tutuluyordu, "◀ Liste" deyince `onDestroy`
   hepsini siliyordu → radyo sabaha kadar çalıyordu. Sayaç `AudioService`'e
   taşındı; ekran kapansa da süre sonunda yayın duruyor.
6. **Bildirimdeki ⏮ ⏭ radyoda hiçbir şey yapmıyordu.** `step()` yalnız TV
   listesinde arıyordu; radyo listesinde de arama eklendi. (Cihazda bildirimden
   "Sonraki" → GENÇ KRAL FM → Süper FM doğrulandı.)
7. **Android 8/9'da kayıtlar listede görünmüyordu.** `RELATIVE_PATH` sütunu
   API 29+; eski sürümlerde sorgu hata veriyordu. API<29 için `DATA LIKE`
   kullanılıyor.
8. **TV uyku zamanlayıcısı yeniden açılışta kayboluyordu.** `sleepUntil` artık
   `Prefs`'te saklanıyor; ekran yeniden kurulsa da sayaç devam ediyor.

## Diğer düzeltmeler

- Reklam rızası yokken reklam yüklenmesi/gösterilmesi tamamen kapatıldı
  (`loadInterstitial`, `loadRewarded`, `maybeTimerAd` → fail-closed).
- Master playlist adresindeki `?token=...` sorgusu varyant adresine taşınıyor
  (`Quality`, `Repair`, `HlsRecorder`) — jetonlu CDN'lerde kalite seçimi ve
  kayıt 404 veriyordu.
- "Bilinen iyi kanallar" gün damgası ülke başına tutuluyor; ülke değişince
  aynı gün yeni listenin "çalışan önce" sırası da kuruluyor.
- Ülke önbelleği "en az 200 bayt" eşiği 20 bayta indirildi; 1–2 kanallı liste
  artık 0 sayılmıyor.
- Kayıtlarım listesinde her kayıt "0 dk" görünüyordu (.ts dosyasında MediaStore
  süresi yok); süre yalnız gerçekten varsa yazılıyor.
- Hakkında ekranındaki yedek sürüm metni `9.30` yerine gerçek sürüm.
- Ayarlar/Paylaş metinlerindeki sabit "305 kanal / 49 radyo" yerine gerçek sayı.
- Tarama sırasında geri çıkınca `ProgressDialog` pencere sızıntısı giderildi
  (Ayarlar ve Ülke ekranı).
- `ChannelRepo.indexOfUrl` bulunamayan URL için `-1` döndürüyor (yanlış kanal
  açılması engellendi).
- Ölü kod (`SettingsActivity.probe`) kaldırıldı.

## Kapasite denetimi

```
APK     : 7.119.765 bayt · 400 dosya · 17.515 sınıf
önceki  : 9.64 · 7.119.005 bayt · 400 dosya · 17.515 sınıf
fark    : sınıf +0 · dosya +0 · boyut +760 bayt
imza    : f3996c67ef0e0e0420f442fe3fcf6303a17caa88dcba8a2b19a10cd95c92b8d5
✅ Kapasite denetimi TEMİZ
```

## Cihazda denenip doğrulananlar (Xiaomi 2506BPN68G, Android 16)

- Temiz kurulum (veri silinmiş) → TR 145 kanal, TRT Haber 360p oynuyor,
  veri sayacı 4,2 MB → çalışıyor.
- Kayıt: 16 sn → `Movies/TV-Kayitlari/TRT_Haber_*.ts` 12,6 MB, Kayıtlarım
  listesinde görünüyor.
- Radyo (TR 25 istasyon): GENÇ KRAL FM çalıyor; 26 sn log taramasında yeniden
  bağlanma yok; listeden çıkınca çalmaya devam ediyor (media_session PLAYING).
- Bildirimden "Sonraki" → Süper FM.
- Radyo uyku: 15 dakika kuruldu, ekran kapandı, sayaç servisde.
- Ülke: ABD indirme 263 kanal, ABC KAAL oynuyor; Almanya 113 kanal, 3sat
  oynuyor; Almanya radyosu 22 istasyon, SWR3 çalıyor.
- Veri sayacı kanal başına: TRT Haber 10,8 MB (2 dk), 3sat 61,4 MB (1 dk).
- Uçak/uçak benzeri kesinti (Wi-Fi + veri kapalı): "Bağlantı sorunu · yeniden
  bağlanıyorum (1/3)" mesajı, çökme yok; ağ gelince kendiliğinden döndü.
- PiP: ana ekrana basınca `mode=pinned` (resim içinde resim) çalışıyor.
- Çoklu ekran (3'lü): 3 pencere açılıyor; bir kanal açılmazsa 8 denemeden sonra
  "açılamadı · uzun bas → başka kanal" yazısı gösteriliyor.
- Favori: 3sat favorilendi, "★ Favoriler" filtresinde görünüyor.

## Bilinen sınırlar (bu sürümde değişmedi)

- 3sat gibi **tek kalite** yayınlarda 360p seçimi veri tüketimini düşürmüyor
  (kaynak tek rendition, ~8 Mbps). Kalite etiketi "kaynağın en iyisi" anlamına
  gelir; böyle kanallarda uygulama tarafında küçültme yapılamıyor.
- Ayrı "sadece ses kaydı" yok: kayıt TS olarak ses+görüntü birlikte yazılıyor.
- Günlük veri limiti diyalogu bu turda canlı denenmedi (kod yolu aynı).
- Play Console işleri (Data Safety, kapalı test, mağaza görselleri) kullanıcıda.
