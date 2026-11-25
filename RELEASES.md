# Sürümler

APK, tablet APK ve AAB dosyaları repoda **tutulmaz** (depoyu şişirmemek için).
Hepsi GitHub Releases sayfasında:

**https://github.com/gecekargosu/canli-tv-android/releases**

- En son sürüm: `v9.93` — `CanliTV-9.93.apk`, `UMAY-MULTI-TV-9.93.aab` (Play).
- İndirme sayfası: https://umay-multi-tv-gq3zer.freebuff.site/indir
- Sürüm notları: `releases/vX.YY/SURUM-NOTLARI.md`

**Dağıtım durumu (2026-11-25):** GitHub `v9.88` release'i yayınlandı (APK + AAB).
Site production güncellendi: gizlilik/kullanım metinleri yeni abonelik bölümüyle,
`/indir` artık **9.88**'i indiriyor. APK telefona kuruldu (97 / 9.88).
Kaynak yedeği: `YEDEK/UMAY-MULTI-TV-9.88-2026-11-25/` ve aynı adlı `.tar.gz`
(imza anahtarı `upload-keystore.jks` + `keystore.properties` dahil).

## v9.93 — ödüllü reklam sunucu doğrulaması + güvenlik denetimi

- Ödüllü reklama `ServerSideVerificationOptions` eklendi (cihaz kimliği `custom_data`).
- Worker: `GET /api/adssv` — AdMob imzasını ECDSA/P-256 ile doğrular, `ad_rewards`
  tablosuna yazar; imza geçmezse 403.
- `docs/play/DENETIM-GUVENLIK-9.93.md`: yayın öncesi güvenlik/politika/lisans denetimi.

## v9.92 — kopya (klon) koruması

- `Guard.java`: Play dışından kurulan APK bizim yükleme sertifikamızla imzalı değilse
  uygulama açılmıyor ("Resmi olmayan sürüm" uyarısı). Play kurulumları etkilenmez.
- `docs/play/KURULUM-SERVISLER.md`: Play servis hesabı, Firebase Analytics ve
  AdMob native/mediation için adım adım konsol rehberi (+ Play Integrity planı).

## v9.91 — RecyclerView, tablet düzeni

- **K-16: kanal listesi RecyclerView'e taşındı.** `ListView`+`BaseAdapter` yerine
  `RecyclerView`+`LinearLayoutManager`+`ViewHolder`; satır aralığı `ItemDecoration` ile.
  Dokunma/uzun basma davranışı korundu (cihazda test edildi).
- **K-18/K-20 tablet düzeni:** geniş ekranda içerik 560dp ile sınırlandı; üst bar
  kutularının gerilmesi, ikonların ortada kalması ve alt şeridin taşması düzeldi
  (1280x800 dp160 ölçekte doğrulandı).
- Uygulama bilinçli olarak **portre kilitli** (TV listesi tek elle kullanım için).

## v9.90 — radyo kalıcı onarım, büyük istasyonlar önce

- **Radyo artık kendiliğinden istasyon değiştirmiyor:** 3 başarısız denemeden sonra
  sıradaki istasyona atlıyordu ("kendiliğinden değişti" şikayeti) — kaldırıldı.
- **Kesintide sürekli yeniden bağlanma:** TRT gibi Icecast/AdBreak sunucuları reklam
  kuşrağında akışı kesiyor; 1,2/2,4/3,6/4/8 sn artan aralıkla ~10 dk boyunca sessizce
  yeniden bağlanılıyor, hata ekranı gösterilmiyor.
- **Radyo sırası:** TRT FM, TRT Radyo 1, Kral FM, Süper FM, Metro FM, Joy FM,
  Power FM, NTV Radyo, Virgin, Show Radyo… büyük istasyonlar başta.
- **Durum metinleri netleşti:** "⏳ Yayın bekleniyor · otomatik yeniden bağlanıyor",
  "⏸ Başka uygulama ses çalıyor · kapanınca devam eder".
- Denetim raporu: `docs/play/DENETIM-9.90.md` (Play mühendisi / yazılım / kullanıcı).

## v9.89 — yedek kaynaklar, radyo listesi düzeltmesi, sade satır

- **Yedek kaynak altyapısı:** `assets/backup_tr.json` ile **25 kanalın 94 doğrulanmış yedek
  adresi** (360p/480p/720p/1080p varyantları) uygulamaya gömüldü. Asıl adres açılmazsa
  sırayla yedekler denenir (`ChannelRepo.fallbacks`, tekli + çoklu ekran).
- **Radyo yedeği:** `assets/backup_radio.json` — 13 istasyon için 19 yedek adres;
  `AudioService` artık denemeler bitince yedek adrese geçiyor.
- **Radyo listesi 24'e düşüyordu:** 2 kez açılmayan istasyon listeden tamamen kalkıyordu
  (TRT Radyo 1 bu yüzden görünmüyordu). Kural kaldırıldı; istasyonlar ⚠ ile listede kalır,
  yalnız kullanıcı "listeden gizle" derse kalkar.
- **Ölü TV adresleri değiştirildi:** 13 kanalın ölü yayını (socialsmart/rocketcdn) yerine
  doğrulanmış kaynaklar eklendi: TRT 3, TRT Arabi, TRT World, ATV Alanya, Bengütürk TV,
  Bi Kanal, Cine 1, Hunat TV, KRT, Pati TV, Power TV, TV 41, Grand Cinema.
- **Liste satırı sadeleşti:** harf rozeti (A/T/S…) kaldırıldı; satırda numara + kanal adı
  + oynat düğmesi kaldı.

## v9.88 — Play kontrol listesi kapatıldı, R8, 120 kanal

- **Abonelik beyanı:** mağaza metni, gizlilik politikası ve kullanım koşulları artık
  aylık reklamsız aboneliği anlatıyor (P0-1).
- **İzlerken reklam kaldırıldı:** 15 dakikalık zamanlayıcı reklam silindi; reklam yalnız
  kanal değişiminde (3. zap / 180 sn). Mağaza metni "oynatma reklamla kesilmez" artık doğru (P0-2).
- **Hedef kitle 13+**, 5 çocuk kanalı çıkarıldı → **120 kanal** (P0-3).
- **`app=` parametresi 0:** NTV parametresiz, Kanal D + Show TV TürkNet yansısı;
  Star TV, HT Spor, TRT Spor Yıldız çıkarıldı, TRT Spor eklendi (P0-4).
- **Harici logo 0** (`tvg-logo` temizlendi); arayüz baş harf rozeti gösteriyor (P0-5).
- **Oynatıcı (P1):** canlı kenar hatasında kanal ölmeden yeniden hizalanma, geçici ağ
  hatasında 2-4-8 sn yeniden deneme, 8 sn "yavaş açılıyor" ipucu + 20 sn yedek kaynak,
  açılış süresi `CrashLog`'a ms olarak yazılıyor. Ölü kanal sayacı 24 saatte sıfırlanıyor.
- **Çoklu ekran:** pencere başına "açılamadı · dokun → tekrar dene"; sesi kapalı pencereye
  640×360 / 700 kbps tavan; 4'lü modda çözücü uyarısı.
- **Radyo:** ölü istasyon (Slow Türk) çıkarıldı, Süper Show Radyo adresi düzeltildi → 31 istasyon.
- **R8 + shrinkResources** açıldı: APK 7,38 MB → **3,85 MB**, AAB 7,04 MB → **6,61 MB**.
- Sürüm tek kaynakta (`app/build.gradle`); kök manifestten kaldırıldı.
- Metinlerin bir bölümü `Lang`'e taşındı (oynatıcı/çoklu ekran mesajları); mağaza metni
  "diller kısmen" olarak düzeltildi.

## v9.76 — kompakt üst bar, sekme sadeleştirme, tek veri şeridi

- Üst kısım tek satıra indi (logo + başlık + düğmeler); liste ekranın ~%37 yerine ~%27'sinden başlıyor.
- Kanal sekmeleri dörde indi: Tümü · Favoriler · Son · Gizlenenler; ekran genişliğine eşit dağılıyor.
- Veri bilgisi tek yerde: listenin altında sabit şerit (üstteki kopya kaldırıldı).
- Düzenle/Sırala satırının sağında kaç kanal göründüğü yazıyor.
- Oynatıcı üst bar düğmeleri ve ikonlar büyütüldü; bilgi şeridi barın altına alındı.
- Gizli kanal hatası: eski toplu "listeden sil" kaydı ve süresiz ölü sayacı listeyi 128'den 32'ye düşürüyordu; temizlendi.

## v9.75 — ana sayfa kayması, TV8 geri, Show TV eklendi

- Ana sayfada durum çubuğu payı iki kez uygulanıyordu (üstte ~55 dp boşluk); tek paya indirildi.
- TV8 "ölü kanal" sayacı yüzünden gizleniyordu; sabit kanallar artık otomatik gizlenmiyor.
- Show TV listeye eklendi (TürkNet/ERC yansısı, 1080/720/480/360 doğrulandı).
- TR ilk beş sabit: ATV, TV8, Show TV, Star TV, Kanal D; eski numara/sıra kayıtları temizlendi.
- ATV bu sürümde değişmedi (ayrı iş).

## v9.74 — kalite/veri tutarlılığı, çoklu ekran geri, kategori adları

- Veri tasarrufu kapanınca 360p'ye döner; Ayarlar gerçek seçimi yazar.
- Varyantı olmayan kanalda kalite kısıtı kalkar, tek satır bilgi gösterilir.
- Çoklu ekranda geri tuşu ana listeye döner; açık kayıt durdurulur.
- "Eglence/Muzik/Cocuk" → "Eğlence/Müzik/Çocuk".
- Üst bar düğmeleri 48dp; Ayarlar satırları taşmıyor.
- ATV bu sürümde değişmedi (ayrı iş).

## v9.73 — reklam güvenilirliği, hata günlüğü, ATV

- Reklam istek fırtınası (saniyede 1 istek) düzeltildi; backoff + ödüllü otomatik yeniden deneme.
- Banner hatası doğru etiketle ve 30 dakikada bir kaydediliyor; NO_FILL nedeni yazılı.
- Hata günlüğü: satır sarma + en yeniye kaydırma + isteğe bağlı günlük gönderim (`/api/log`).
- ATV 720p birincil (cihazda açıldı), 480p/master yedek.
- İnternet/DNS yokken kanal ölü işaretlenmiyor.
- Gizli geliştirici modu: reklam sayacı, reklamları kapat, reklam teşhisi (kullanıcıya görünmez).
- README ve Play uygunluk/veri beyanı metinleri gerçek davranışa göre güncellendi.

Yerel arşivde aynı dosyalar `releases/` altında durur ama git'e eklenmez.
