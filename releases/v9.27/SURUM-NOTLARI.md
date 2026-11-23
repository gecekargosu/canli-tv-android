# UMAY Multi TV v9.27 (versionCode 36)

Tarih: 23 Kasım 2026

## Bu sürümde yeni

### 1. Yayın durunca bilgi şeridi (dijital reklam kuşağı)
Star TV / TV8 gibi kanallarda yayıncının kendi "dijital reklam kuşağı" karesi gelince
uygulama artık boş ekran bırakmıyor:

- 12 sn ilerlemeyen yayında: `⏳ Yayın durakladı · yeniden bağlanıyorum (1/2)`
- 2 denemeden sonra: `📺 Yayıncı reklam aralığında olabilir · yayın dönünce kendiliğinden gelecek`
- 3'lü ızgarada pencere içi etiket: `yayıncı reklam aralığında olabilir · bekleniyor`

Not: bu reklam yayıncının sunucusunda videoya gömülüyor; uygulama **atlayamaz**.
Yayın dönünce oynatıcı kendiliğinden devam eder (yeniden bağlanma döngüsü sürer).

### 2. Ayarlar → Ölü kanalları onar
- Daha önce 2+ kez açılmayıp gizlenen kanallar gerçek oynatma zinciriyle (master + varyant) yeniden denenir.
- Yayını geri gelen kanal işareti temizlenip **listeye geri eklenir**.
- "Tüm kanalları test et" de artık aynı doğru testi kullanıyor (302 + HLS varyantı).

### 3. Ayarlar → Reklam sayacım
- Banner / geçiş / ödüllü reklam sayıları, toplam gösterim ve **tahmini kazanç**.
- Her gösterim `Android/data/com.cengiz.canlitv/files/reklam-sayaci.csv` dosyasına yazılır
  (AdMob raporuyla karşılaştırıp eCPM kalibrasyonu için).
- Tahmin: banner 6 · geçiş 35 · ödüllü 55 TL / 1000 gösterim.

### 4. Tüm ülke listeleri tarandı
Sunucudaki ülke listeleri (`good/<cc>.json`) gerçek oynatma testinden geçirildi,
ölü URL'ler çıkarıldı: br 55→49 · gb 28→24 · in 75→71 · mx 59→57 · ru 56→53 · us 67→66 ·
tr 145→144 (Kanal 3 ölü) · de/se/sa temiz. Liste günlük sunucudan geldiği için
uygulama güncellemesi beklemeden geçerli.

### 5. Küçük düzeltmeler
- Ses bildirimi simgesi uygulama simgesi oldu (#109 kalanı).
- `Repair`/tarama için `Net.readText` yardımcısı.

## Bilinen sınırlar
- Yayıncı SSAI reklamı atlanamaz; sadece bilgi verilip yeniden bağlanılır.
- Hangi reklamın çıkacağını AdMob belirler; uygulama seçemez (bloklama AdMob konsolundan).
- 360p varsayılan (en düşük çalışan varyant). 600 MB ≈ 2 saat 360p.
- ExoPlayer geçişi (#107) ve sunucu allowlist (#108) hâlâ sırada.

## Doğrulama
- 144 kanal listelendi, kalite 360p.
- Kral Pop, TRT Muzik, TRT 1 oynatıldı (v9.26'da), v9.27'de açılış + ayarlar + onarım + sayaç test edildi.
- Çökme yok.
