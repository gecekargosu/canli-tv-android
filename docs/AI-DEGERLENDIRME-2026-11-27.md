# AI / Gemini Önerisi — Benim Değerlendirmem

Soru: ChatGPT'nin yazdığı "AI Powered Self-Healing Stream Recovery Engine" planı mantıklı mı,
3 ücretsiz API alıp kota bitince diğerine geçen bir otomasyon kurmalı mıyız, AI'yı nerede kullanmalıyız?

Kısa cevap: **planın %70'i zaten uygulamada var, AI kısmı yanlış yerde duruyor,
3 anahtar fikri ise teknik olarak işlemez.** Aşağıda gerekçeleri ve benim önerim.

---

## 1. Planın "yeni" sandığı şeyler bizde zaten var

| Plandaki madde | Bizde karşılığı |
|---|---|
| Playback error, HTTP 403/404/5xx, timeout, decoder hatası algılama | `PlayerActivity`: `ERROR_CODE_IO_BAD_HTTP_STATUS` vb. yakalanıyor, `CrashLog.event("tv", "ölü: KANAL · HTTP 403 · denenen 3 adres")` |
| Yedek adres deneme | Var: "yavaş açılış · yedek kaynağa geçiliyor", radyoda `AudioService` yedek adres |
| Bağlantı kopunca yeniden başlatma | Var: `[net] bağlantı koptu` + canlı pencere yeniden hizalama |
| Kontrollü yeniden deneme, sonsuz döngü yok | Var: `Prefs.fails` sayacı, 2 hatadan sonra kanal listeden düşer (sabit kanallar muaf), `Good.refresh` günlük doğrulama |
| Log ve analiz | Var: `CrashLog` olay günlüğü + `/api/log` uç noktası + Ayarlar → Hata günlüğü |
| Kullanıcıya açıklama | Kısmen: hata sonrası "kanal açılamadı" mesajı var, **neden** açıklaması zayıf |
| Oynatma doğrulama (READY + buffer + kararlı süre) | Kısmen: hazır olma süresi ölçülüyor (`hazır (1830 ms)`), "gerçekten kare geliyor mu" ölçümü yok |

Yani "Diagnostic Engine + Recovery Engine" için sıfırdan sistem yazmak yerine **mevcut
kodu tek yerde toplamak** gerekiyor. ChatGPT mevcut kodu görmediği için planı bizim
uygulamayı tanımıyor; birebir uygularsak çalışan mekanizmaları ikinci kez yazmış oluruz.

## 2. AI'yı canlı kurtarma döngüsüne koymak zararlı

Plan "hata olunca Gemini'ye sor, önerdiği adımı uygula" diyor. Pratikte:

1. **Ağ zaten bozuk olabilir.** Yayın açılmadığında ilk şüpheli internet. AI çağrısı da
   aynı interneti kullanır → tam ihtiyaç anında çalışmaz.
2. **Gecikme.** Flash-Lite ile bile 400–1500 ms. Kullanıcı kanalın açılmasını beklerken
   biz modele "sence neden olmadı" diye soruyoruz. Kullanıcı bunu hissetmez, kapatır.
3. **AI bizden fazla bilgi bilmiyor.** Elimizde hata kodu, HTTP durumu, kaç adres denendi
   bilgisi var; model bunlardan ancak kural tabanlı çıkarım yapar. "403 → yayıncı engelledi",
   "404 → adres değişti", "timeout + ping yok → internet", "decoder → codec uyumsuz".
   Bunlar 20 satır `switch` ile aynı sonucu **0 ms'de, bedava, çevrimdışı** verir.
4. **Güvenlik yüzeyi büyür.** AI'nın önerdiği eylemleri "güvenli listeden seç" desek bile
   liste zaten bizim yazdığımız 5 eylem; yani AI'ya ihtiyaç yok.

Sonuç: kurtarma **yerel ve deterministik** kalmalı. AI karar verici değil, en fazla
açıklama üreticisi olabilir — o da gerekli değil.

## 3. "3 ücretsiz API, kota bitince diğerine geç" fikri çalışmaz

Bu en net teknik itiraz:

- Gemini'de **kota anahtar başına değil, proje başına**. Kotayı "proje" belirler; aynı
  projede 3 anahtar üretirsen **toplam kotan artmaz** (Google'ın rate-limit sayfası bunu
  açıkça yazıyor: "Rate limits are applied per project, not per API key").
- Gerçekten 3 kat kota istiyorsan **3 ayrı Google Cloud projesi + 3 ayrı faturalama hesabı**
  açman gerekir. Bu, kotadan kaçınmak için çoklu proje kullanmak demektir; Google şartlarında
  hoş karşılanmaz, ayrıca 3 projeyi ayrı ayrı izlemek/bakımını yapmak zorundasın.
- Ücretsiz katman rakamları (Kasım 2026 itibarıyla yaygın okuma): **Flash-Lite ≈ 500 istek/gün,
  10–15 istek/dakika**. 3 projeyle 1500/gün olur. 10.000 kullanıcı günde bir kez hata görürse
  kota ilk saatte biter.
- Kotayı "uyarı ver + diğerine geç" mantığıyla yönetmek yerine **isteği sunucuda önbelleğe
  almak** çok daha etkili: aynı hata türü + aynı kanal + aynı saat dilimi için tek çağrı,
  cevabı 24 saat cache'le. Gerçek çağrı sayısı 100 kat düşer.

Yani: **tek anahtar + sunucu tarafı önbellek + günlük bütçe + AI yoksa kural tabanlı yedek.**
Üç anahtarlı otomasyon, çözdüğünden fazla sorun üretir.

## 4. Peki AI nerede gerçekten fark yaratır?

Fark yaratacak sıra (benim önerim):

**A. Sunucu tarafı "kanal sağlık raporu" (AI'lı, en değerli)** — uygulamada API çağrısı yok.
`/api/log`'a düşen olayları haftada bir toplayıp modele özetlet: "şu 12 kanal sürekli 403
veriyor, şu 5 kanal sadece akşam 20:00–22:00 arası timeout, şu ülkede DNS hatası artmış".
Çıktı **bize** gelir, kanal listesini temizleriz, yedek adres ekleriz. Kullanıcıya maliyeti
sıfır, kotayı biz harcarız (haftada 1 çağrı). Gerçek fayda: ölü kanalların kendiliğinden ayıklanması.

**B. Kullanıcıya "neden açılmadı" açıklaması (AI'sız)** — kural tabanlı 8 mesaj:
403 "Yayıncı bu yayını engelliyor", 404 "Kanal adresi değişmiş, listede güncelleme bekleniyor",
timeout+ping yok "İnternet bağlantın yok gibi", decoder "Bu kanalın formatı cihazında desteklenmiyor",
429 "Yayıncı çok istek aldığı için geçici olarak kapattı". Bu, kullanıcı memnuniyetine AI'dan
daha çok katkı yapar ve bedava.

**C. Doğal dille kanal arama (AI'lı, opsiyonel)** — "akşam haber izleyeyim", "çocuk kanalı aç".
Mevcut arama ada/kategoriye bakıyor; AI ile eşleştirme daha akıllı olur. Ama bunun için
kanal kataloğunu (127–1000 satır) her istekte modele göndermek gerekir → maliyet ve gizlilik.
Alternatif: gömülü küçük bir anahtar kelime tablosu (AI'sız) aynı işi %90 yapar.

**D. Yayın içeriğini anlama / özet / altyazı** — yapılamaz. Yayın akışına erişimimiz yok,
telif ve altyapı sorunu var. Planda bu yok, iyi.

## 5. Gizlilik ve Play tarafı

- AI'ya gönderilen şey "kanal adı + yayın adresi + hata kodu + cihaz modeli" olur. Bunlar
  kişisel veri sayılmasa da **gizlilik politikasında** "hata ayıklama için sunucumuza/üçüncü
  tarafa teknik veri gönderilir" maddesi olmalı. Play, veri güvenliği formunda bunu soruyor.
- Şu an otomatik günlük gönderimi **varsayılan kapalı**; AI eklenirse aynı rıza şart.
- API anahtarı APK'ya gömülmemeli (planda doğru yazılmış): anahtar **sunucuda** (`site/worker.js`
  içindeki Worker, secret olarak) durur. Zaten böyle bir Worker'ımız var.

## 6. Benim önerim (net)

1. **Şimdi AI eklemeyelim.** Önce şunları yapalım (hepsi AI'sız, 1–2 günlük iş):
   - Hata kodlarını tek yerde sınıflandıran `StreamDiag` sınıfı (plandaki "Diagnostic Engine"in
     gerçekten faydalı kısmı) ve kullanıcıya gösterilen 8 açıklama.
   - Kanala özel "bu kanal çalışmıyor" bildir düğmesi (veri toplamaya başlarız).
   - "Gerçek kare geldi mi" ölçümü: `onRenderedFirstFrame` + 10 sn kararlılık → başarı kaydı.
2. **2–4 hafta gerçek veri toplayalım.** `/api/log`'da anlamlı hacim oluşsun.
3. **Sonra sunucu tarafında** (uygulamada değil) Gemini Flash-Lite ile haftalık ölü kanal /
   arıza örüntüsü raporu üretelim. Tek anahtar, tek proje, önbellekli, günde ≤ 20 çağrı.
   Fayda görülürse kullanıcıya dönük kullanıma (C maddesi) bakarız.
4. 3 anahtar / kota devri fikrini **bırakalım**: proje başına kota olduğu için işlemez,
   bakım yükü ve şartlar riski getirir.

Kısacası: ChatGPT'nin planı doğru yönde ama **yerinde olmayan bir çözüm** — "AI ile kurtarma"
yerine "iyi teşhis + iyi açıklama + sunucuda toplu analiz" aynı sonucu daha ucuza, daha hızlı
ve daha güvenli verir. AI'yı sonradan, sadece bizim işimizi kolaylaştıran yerde (log analizi)
kullanmak en akıllısı.

> Not: Ücretsiz katman rakamları Google tarafından sık değişiyor ve resmî tabloda model
> başına yayınlanmıyor; kendi projenin gerçek limiti AI Studio → Rate Limit ekranında görünür.
> Bu yüzden "ücretsiz kota"ya kritik bir özelliği bağlamak doğru değil.

---

## 7. v10.09'da uygulananlar (28 Kasım 2026)

Yukarıdaki önerinin AI'sız kısmı uygulandı, AI kısmı bilerek ertelendi:

- **`StreamDiag`** (3. maddedeki "AI'sız teşhis"): 13 hata sınıfı, hata ekranında
  sebep + öneri, 8 dilde metin. AI yok, 0 ms, çevrimdışı. 26 birim testi var.
- **"Bu kanal çalışmıyor" bildir düğmesi**: oynatıcı hata ekranı + liste menüsü.
  Veri toplama artık başlayabilir; sunucu tarafı değişmedi (`/api/log`).
- **İlk kare ölçümü**: READY + video izi + 10 sn kararlılık → "kare geldi" kaydı.
  Ölü kanalları yakalamanın en kesin yolu bu; kanalı öldürmeden sadece kaydeder.

### Sunucu tarafı toplu rapor planı (AI burada, uygulamada değil)

Kararlaştırılan akış — **cron her gün çalışır, AI yalnız eşik aşılırsa çağrılır**:

1. Her gün 04:00 (zamanlanmış Worker): `/api/log` tablosundan son 24 saatlik olayları
   SQL ile grupla. AI yok, maliyet 0.
2. Eşik: yeni olay > 200 **veya** yeni ölü kanal (14 gün üst üste aynı hata) varsa devam,
   yoksa çık. Böylece "2-3 günde bir" ritmi kendiliğinden oluşur, kota boşa gitmez.
3. Eşik aşıldıysa tek Gemini Flash-Lite çağrısı: "şu 12 kanal sürekli 403, şu 5 kanal
   akşam 20:00-22:00 arası timeout" gibi **Türkçe özet** üret. Çıktı bize gelir.
4. Rapor D1'de saklanır, istenirse Telegram/e-posta ile bildirilir.
5. Kota: tek proje, tek anahtar, günde ≤ 20 çağrı. 3 anahtar rotasyonu **yok**
   (kota proje başına olduğu için işlemez).

Sıra: önce 2-4 hafta gerçek bildirim verisi toplansın, sonra 2-3-4 uygulanır.
