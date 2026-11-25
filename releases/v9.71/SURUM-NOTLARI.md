# UMAY Multi TV 9.71 — sürüm notları

Tarih: 25 Kasım 2026 · versionCode 80 · versionName 9.71
APK 7.122.465 bayt · 400 dosya · 17.518 sınıf · imza `f3996c67…b8d5`
AAB 6.778.856 bayt (Play Console için aynı klasörde).

## Bu sürümde ne değişti

### Yayın öncesi temizlik (PDF raporu P0)
1. **Kanal listesi 145 → 126.** 12 lisanslı yabancı kanal (Disney Jr., BabyTV,
   Spacetoon, BBC Drama, Doctor Who Classic, Wonder, InWonder, Inside Crime,
   AVA Series, MBC Masr Drama, MBC Drama USA, Persiana Türkiye) ve 7 alakasız/
   siyasi kanal (Al-Zahra TV Turkic, Almahriah TV, Elsharq TV, Mekameleen TV,
   UMSA TVU Internacional, Cosmos TV, Can Tho TV 2) çıkarıldı. Aynı temizlik
   `assets/channels.m3u`, `assets/good_tr.json`, `site/public/good/tr.json` ve
   `site/public/kanallar.json` dosyalarında yapıldı.
2. **TV8 bağlantısı.** `?app=7ddc…&ce=3` anahtarlı daioncdn adresi hem kanaldan
   hem yedek listesinden kaldırıldı; yayın TürkNet/ERC yansısından (720p, yedek 480p).
3. **Reklam sıklığı yumuşatıldı.** Artık her zap'ta değil, **her 3 kanal
   değişiminde** ve iki reklam arası **en az 180 saniye**. İzlerken çıkan reklam
   yine 15 dakikada bir. Ayarlar'daki açıklama metni de buna göre düzeltildi.
4. **Reklam test modu yayınlanan APK'da yok.** Satır ve reklam kimliği kopyalama
   ekranı yalnız debug derlemede; release'te test reklamına geçilemiyor (gelir
   sessizce sıfırlanmıyordu).
5. **Kayıt yalnız kamu kanallarında.** TRT / TBMM / Diyanet dışındaki kanallarda
   kayıt düğmesi "telif nedeniyle kapalı" uyarısı verip başlamıyor. Tek ekran ve
   çoklu ekran aynı kuralı kullanıyor.
6. **Reklam içerik sınıfı G → PG.** G neredeyse hiç reklam doldurmuyordu.

### Hata düzeltmeleri
7. **Ölü kanal sonsuz yükleme (M-99).** Sunucusu hiç cevap vermeyen kanal artık
   ~45 saniyede "Bu kanal şu an açılamadı · Zaman aşımı" gösteriyor, hata olarak
   işaretleniyor ve (ikinci denemede) sonraki kanala geçiyor. Ağ kopması yüzünden
   3'ten fazla yeniden bağlanma da ölü sayılıyor.
8. **Reklam sayacı.** Geçiş reklamı gösterim anında (`onAdImpression`) sayılıyor,
   ödüllü reklam yalnız ödül gerçekten alındığında; test modu sayılmıyor.
   Günlük/aylık kırılım ve "bu cihaz" notu eklendi.
9. **Ölü hesap metinleri** (Rusça "Аккаунт", "6-значный код" vb.) silindi.

## Doğrulama

- `python tools/verify-apk.py CanliTV.apk --prev releases/v9.70/CanliTV-9.70.apk --expect-version 9.71`
  → ✅ temiz (sınıf +0, dosya +0, imza aynı).
- Cihaz (Z5R87DU8V4LBSOVG, Android/HyperOS):
  - Uygulama açılıyor, Türkiye listesi **126 kanal** (başlık 127: kullanıcının
    kendi eklediği 1 kanal listede önde).
  - ATV'de kayıt → "Kayıt yalnız kamu kanallarında açık (TRT, TBMM, Diyanet)…" uyarısı, dosya oluşmadı.
  - TRT 1'de kayıt → `TRT_1_…ts` dosyası oluştu, durdurma çalıştı.
  - Ayarlar'da "Reklam test modu" satırı **yok**, "Reklam sayacım" var (175 reklam · ~₺1,78).
  - Almanya listesinde RFO: 16. saniyede hata ekranı, ardından sıradaki kanal (RiC) açıldı — sonsuz döngü yok.
  - BLK Regional TV: "yeniden bağlanıyorum (1/3)" sonrası hata işaretlenip sonraki kanala geçti.

## Doğrulanmayan / bilinen eksikler

- Reklam sıklığı cihazda reklam doluluğu olmadığı için gözle doğrulanamadı; kod
  yolu (`opens % 3`, 180 sn) derleme ve mantık olarak kontrol edildi.
- R8/minify, çoklu ekranın ExoPlayer'a taşınması, çeviri metinleri, tablet yatay
  düzeni bu sürümde **yok** (P1 listesinde).
- Play Console yüklemesi, 12 tester, mağaza görselleri kullanıcıda.
