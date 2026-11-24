# UMAY Multi TV v9.30 (versionCode 39)

Tarih: 23 Kasım 2026

## Bu sürümde yeni

### 1. TRT Haber eklendi (#154)
- Liste 144 → **145 kanal**. TRT Haber (`tv-trthaber.medya.trt.com.tr`) haber grubunun başına eklendi.
- Telefonda açıldı ve oynadı (ExoPlayer).

### 2. Yabancı radyolarda ölü istasyon temizliği (#153)
- Ülke radyoları indirilirken her adres **tek tek yoklanıyor** (ilk bayt okunabiliyor mu).
- Ölü istasyonlar listeye hiç girmiyor; böylece "açılmadı, sıradakine geçiliyor" durumu azalıyor.
- Ölçüm: Almanya listesinde 80 istasyonun **36'sı ölüydü** (SWR3, MANGORADIO, WDR4, N-JOY, Bayern 1…);
  yeni sürümde bunlar listelenmeyecek, ~44 çalışan istasyon kalacak.
- Aşırı eleme olursa (ağ kısıtlı) liste boş kalmasın diye güvenlik var.

### 3. Ayarlar → "Program tanıtımı ve özellikleri" (#151)
- Yeni tam sayfa tanıtım: program ne yapar, tüm özellikler, sorun giderme rehberi
  (ölü kanal, dijital reklam kuşağı, donma, reklam gelmemesi, bildirim izni, veri tüketimi),
  gizlilik/reklam politikası, telif süreci ve iletişim.
- **E-posta düğmesi** `cengizkilic1980@gmail.com` adresine yazı açar.
- WhatsApp düğmesi hazır; numara tanımlanınca tek dokunuşla sohbet açacak.

## Doğrulama
- 145 kanal; TRT Haber oynadı; tanıtım ekranı açıldı, e-posta/WhatsApp düğmeleri göründü.
- Almanya radyo listesi taraması: 80 → 44 çalışan (36 ölü çıkarıldı).
- Çökme yok.

## Not
- 3'lü ızgara ve radyo servisi hâlâ MediaPlayer (#147).
