# UMAY Multi TV — Konuşma Analizi ve Yeni İş Listesi

**Kaynak:** `FREEBUFF İLE YAPTIĞIMIZ KONUŞMA CANLI TV.txt` (2.953 satır, 183 KB, 91 kullanıcı isteği)
**Analiz tarihi:** 23 Kasım 2026 · **Uygulama sürümü:** v9.25 (versionCode 34)

Bu dosya üç listeyi tek yerde tutar: **(A) istediklerin**, **(B) yapılanlar**, **(C) yapılmayanlar / yeni iş listesi**.
Ayrıca Claude'un P0 risk listesinin son durumu (D) ve TV8/ATV düzeltmelerinin diğer kanallara uygulanması (E) burada.

---

## 0. Özet

- Konuşmada **91 ayrı istek** var; bunların büyük kısmı TV/radyo oynatma, liste düzenleme, veri tasarrufu, reklam düzeni ve Play Store hazırlığı.
- Bugüne kadar **v5.0 → v9.25** arası 30'dan fazla sürüm çıktı; her sürüm telefona kuruldu, `releases/` altında saklandı, GitHub'a ve siteye yayınlandı.
- **Kapanmayan ana işler:** ExoPlayer geçişi (#39/#107), sunucu allowlist (#99/#108), ses servisi güvenliği (P0-5 kalanları), keystore şifre rotasyonu, widget/Chromecast/EPG ve Play yayını (dışarıya bağlı).

---

## A) BENDEN İSTEDİKLERİN (konuşmadaki tüm istekler)

| # | Konu | İstek | Durum |
|---|---|---|---|
| A1 | Klasör | Telefondaki kurulu APK ile klasörün aynı olduğunu doğrula, programı analiz et | ✅ v5.0 (SHA-256 aynı) |
| A2 | Liste | Kanal listesini düzenle: çoklu seçim, yukarı/aşağı taşı, kumanda numarası değiştir, sil; her sekmede | ✅ v6.0 |
| A3 | Oynatıcı | Üst bardaki küçük ikonları büyüt, yazılı ve anlaşılır buton yap | ✅ v6.0 |
| A4 | Oynatıcı | Tekli ve çoklu ekranda **ana sayfa** butonu; TV'yi kapatma | ✅ v6.0 / v9.18 |
| A5 | Veri | TV ve radyoda veri tüketimini azalt: 360p gibi seçenekler, saatlik GB bilgisi, uyarı | ✅ v6.0 → v9.23 (varsayılan 360p + onay) |
| A6 | Reklam | Banner + video reklam; 20 dk'da bir, kullanıcıyı boğmadan | ✅ v9.0 → v9.25 (**15 dk + her kanal değişiminde**) |
| A7 | Üyelik | Google/mail ile üyelik, hoş geldin maili, zorlamadan | ✅ yapıldı (v7-v9.20) → ❌ **v9.21'de kaldırıldı** (senin onayınla) |
| A8 | Ayarlar | Ayarlar listesini tam sayfa yap, her satırda ikon + açıklama + değer, hepsi çalışsın | ✅ v6.0 |
| A9 | Dil/ülke | Çok dilli arayüz + kullanıcının ülkesine göre kanal listesi | ✅ v9.2-v9.3 (4 dil, 50 ülke) |
| A10 | Play Store | Play için gerekenleri araştır, listele, formları doldur | ⚠️ Kısmen — kimlik/adres onayı sende (⛔) |
| A11 | Geliştirici adı | Geliştirici adı **YABANARISI**, uygulama adı **UMAY Multi TV** | ✅ v9.1 |
| A12 | Sürüm arşivi | Her revizyonu numaralı klasöre kaydet, üstüne yazma | ✅ `releases/vX.Y/` |
| A13 | Ekran boyutu | Tüm Android telefon/tablet boyutlarına uyum | ⚠️ Kısmen (dp + inset var; tablet/katlanabilir testi yok) |
| A14 | Hata bildirimi | Kullanıcıdan hata kaydı alınabilsin | ✅ v9.3 (`crash.txt` + Ayarlar → Hata bildir) |
| A15 | Radyo | Radyo TV gibi ülkeye göre değişsin (Rusya → Rus radyolar) | ✅ v9.3 (doğrulandı) |
| A16 | Radyo | Radyoda ses gelmiyor + ekolayzır donuk | ✅ v9.4 |
| A17 | Radyo | Sınıflar (spor/haber/müzik), favoriler, kapatma, uyku zamanlayıcı | ✅ v9.5-v9.7 |
| A18 | Radyo | Her radyoda reklam barı görünsün (Radyo 7 örneği) | ✅ v9.8 |
| A19 | Radyo | Listede olmayan istasyonu internette ara + kendi listesine ekle | ✅ v9.17 |
| A20 | Radyo | Popülerliğe göre sırala, ölü istasyonları listeden kaldır | ✅ v9.14 / v9.16 |
| A21 | Veri | Hangi ekranda olursa olsun toplam MB görünsün, Ayarlar'dan kapatılabilsin | ✅ v9.5-v9.6 |
| A22 | Uyku | Uyku zamanlayıcısı / kapanış saati | ✅ v6.0 |
| A23 | İçerik | Açılmayan kanalları gizle, otomatik sonraki kanala geç | ✅ v9.7 |
| A24 | Arama | Listede olmayan TV kanalını arayıp bulma | ⚠️ Radyoda var (#102 ile TV'de kendi URL/M3U eklendi), **internette TV arama yok** |
| A25 | Reklam içeriği | Erotik/pornografik reklam gelmesin (Afterglow örneği) | ✅ v9.13 (G derece + UMP, `MaxAdContentRating(G)`) |
| A26 | Görsel | Yazıların görünmemesi / taşma hataları her yerde düzeltilsin | ✅ v9.15-v9.16 |
| A27 | Kazanç | AdMob kazancı ne zaman ödenir | ℹ️ Bilgi verildi (₺0,93 · eşik 100 $) |
| A28 | Claude | Claude'un v9.16 raporunu oku, hataları düzelt, önerileri listeye ekle | ✅ v9.17-v9.25 (P0'ların çoğu) |
| A29 | Claude P0 | 8 P0 maddesini tek tek bitir, her birini doğrulamadan sonrakine geçme | ⚠️ 7/8 bitti; **P0-5'in alt maddeleri açık** (bkz. D) |
| A30 | Yedek kaynak | Ölü kanal için ikinci kaynak (#42/#103) | ◐ TV8, ATV, Show, Kanal D, NOW |
| A31 | Izgara | Çoklu ekran düğmesi kaybolmasın, tekli kanalda da erişilebilsin | ✅ v9.23 |
| A32 | Kalite | Tüm kanallar en düşük kalitede açılsın; yükseltmek isteyene veri uyarısı | ✅ v9.23 |
| A33 | TV8/ATV | 3'lü ekranda TV8 başlamıyor, ATV ortada boş — kalıcı düzelt | ✅ v9.25 |
| A34 | Reklam kuralı | Kanal başına ayrı reklam olmasın; 15 dk'da bir + her kanal değişiminde video | ✅ v9.25 |
| A35 | Pazarlama | TikTok/Instagram için tanıtım videosu ve klasör | ⚠️ `pazarlama/` + ham kayıt var; **2-5 dk'lık kurgu video yok** |
| A36 | Test | Uygulamayı A'dan Z'ye test edecek platform bul, birlikte dene | ✅ Appetize.io ile yapıldı |
| A37 | Paylaşım | Arkadaşa WhatsApp'tan indirme linki | ✅ Site `/indir` + GitHub release |
| A38 | Claude zip | Her sürümde Claude için zip üret, adını yaz, klasöre koy | ✅ `UMAY-MULTI-TV-Claude-vX.Y.zip` |
| A39 | Toplu iş | Konuşmadaki unutulanları çıkar, iş listesiyle birleştir, ekrana yaz | ✅ Bu dosya + `docs/is-listesi.md` |
| A40 | Bu oturum | İstenen ama bitmeyen işleri yeni iş listesi yap, önem sırasına koy, tamamla | 🔄 Bu dosyanın C bölümü |

---

## B) SENİN İÇİN YAPILANLAR

| Sürüm | Yapılan |
|---|---|
| v5.0 | Klasör/telefon APK karşılaştırması, kod analizi, eksiklerin raporu |
| v6.0 | Liste düzenleme (çoklu seçim, taşıma, numara, sil, sırala), büyük oynatıcı butonları, ana sayfa/kapat, kalite seçici (360p-1080p + GB/saat), tam sayfa Ayarlar, targetSdk 36 |
| v7.0 | Derleme/imza düzeni, sürüm klasörleri, ilk Play hazırlıkları |
| v9.0 | **Gradle + AdMob SDK**: gerçek banner/geçiş/ödüllü reklam |
| v9.1-v9.2 | Ödüllü video, radyo reklamları, radyo sınıfları/favoriler/kapatma, zorlamayan reklamsız teklifi, geliştirici adı YABANARISI |
| v9.3 | Radyonun ülkeye göre değişmesi (senin en kritik isteğin, doğrulandı), hata kaydı |
| v9.4 | Radyoda ses gelmemesinin kök nedeni + ekolayzır |
| v9.5-v9.6 | Tüm ekranlarda MB göstergesi + kapatma, ABD üzerinden uçtan uca test, ülke sıralaması |
| v9.7 | Ölü kanal filtresi, otomatik sonraki kanal, radyo üst barı |
| v9.8 | Her radyoda banner, Pixel 7 + diğer profillerde test |
| v9.9-v9.10 | Ödüllü video tek dokunuş, 20 dk reklamsız, GitHub + site yayını |
| v9.11 | "Çalışan kanallar önce" doğrulanmış liste (`good/<cc>.json`) |
| v9.12 | Banner her zaman görünür, reklamsız süre kanal başına |
| v9.13 | Erotik/yetişkin reklam engeli (G derece), UMP hazırlığı |
| v9.14 | Açılmayan radyoların listeden kalkması, TR listesi kontrolü |
| v9.15 | Oynatıcıda görüntü gelmemesi (reklamsız şerit hatası), banner gezinme çubuğu üstüne |
| v9.16 | Radyo popüler sıralama, radyo sayısı düzeltmesi |
| v9.17 | İnternette radyo arama + kendi listesine kalıcı ekleme |
| v9.18 | Bildirimdeki Durdur, radyoda ⏭ TV'ye atlama, kapanışta ses, arama klavyesi, keystore şifresi repodan çıktı, gizlilik metni |
| v9.19 | **P0-1 telif temizliği:** 305 → 156 kanal, çıplak IP 0, çalma listesi temizleyici (`_build/temizle.py`) |
| v9.20 | **P0-3 UMP/AB rıza ekranı**, ATV + TRT Çocuk/TRT Müzik geri (157 kanal) |
| v9.21 | **P0-4 üyelik sistemi tamamen kaldırıldı** (kod + manifest + Ayarlar; sunucu `/api/*` → 410) |
| v9.22 | **P0-6** üst+alt inset, **P0-8** mağaza başlığı "UMAY Multi TV: Canlı TV Radyo", ATV 720p |
| v9.23 | Varsayılan **360p** + veri onayı diyaloğu, ▦ çoklu ekran düğmesi barın başına |
| v9.24 | **#42 yedek kaynak altyapısı** (ATV mirror), P0-2 yaş tutarlılığı (13+) |
| v9.25 | **Izgarada boş pencere kök nedeni** (paylaşılan `reloadGen`), 3 sn sağlık denetimi + yedek kaynak zinciri, TV8 reklam kuşağı toparlanması, tarayıcı UA'sı, **reklam kuralı 15 dk + her zap**, çift "Reklam izle" şeridi kaldırıldı, #100 telif bildirimi, #101 günlük good-listesi, #102 kendi M3U/URL ekleme, #103 TV8/Show/KanalD/NOW yedekleri |
| v9.26 | **Bu oturum:** ölü kanal temizliği (157 → 145), TRT Müzik + Kral Pop kaynak düzeltmesi, ses servisi güvenliği (audio focus, kulaklık çıkarma, wake lock timeout, http'ye düşme kaldırıldı), manifest sertleştirme (allowBackup=false, supportsRtl) |

---

## C) YAPILMAYAN / ERTELENEN / UNUTULAN İŞLER — YENİ İŞ LİSTESİ

Öncelik sırası: **P0 = risk veya vaat**, **P1 = büyüme/kalite**, **P2 = sonra**.

### P0 — Risk ve vaat (önce bunlar)

| # | İş | Neden | Durum |
|---|---|---|---|
| 107 | **ExoPlayer (Media3) geçişi** — PlayerActivity + MultiActivity + AudioService | "Sürekli takılıyor" şikâyetinin kök çözümü; HLS canlı kenar, yeniden bağlanma, audio focus, kilit ekranı hazır gelir. Claude bunu P0'a yükseltmeni önerdi | ⏳ |
| 108 | **Sunucuda allowlist (#99)** — ülke listeleri ham iptv-org yerine filtrelenmiş JSON olarak gelsin | P0-1 telif riskinin kalıcı çözümü; uygulama içi filtre tek başına yeterli değil | ⏳ |
| 109 | **Ses servisi P0-5 kalanları** | a) audio focus yok, b) `ACTION_AUDIO_BECOMING_NOISY` yok (kulaklık çıkınca hoparlörden ses), c) WakeLock/WifiLock süresiz, d) https başarısızsa http'ye düşüyor (güvenlik), e) `POST_NOTIFICATIONS` sadece oynatıcıda isteniyor | ◐ v9.26'da a/b/c/d yapıldı, e kaldı |
| 110 | **Keystore şifre rotasyonu** | Şifre v9.18'de repodan çıktı **ama değiştirilmedi**; eski şifre hâlâ public git geçmişinde. Play App Signing ile upload anahtarı sıfırlanmalı | ⏳ (senin Play Console erişimin) |
| 111 | **Manifest sertleştirme** | `allowBackup` yok (varsayılan açık → kullanıcı verisi yedekleniyor), `supportsRtl` yok | ✅ v9.26 |
| 112 | **Kanal sağlık otomasyonu** | 157 kanalın 14'ü ölüydü; her sürümde otomatik tarama + rapor + ölüyü çıkar | ◐ v9.26 taraması yapıldı; `_build/saglik.tsv` |
| 113 | **Kendi alan adı + app-ads.txt kök alan adında** | AdMob app-ads.txt alt alan adında; Google kök alan adını tarar | ⏳ (alan adı satın alma) |
| 114 | **Play Veri Güvenliği formu** | Gizlilik metni güncel ama form Play Console'da doldurulacak | ⏳ (senin işin) |
| 115 | **Telif/hukuk görüşü** | Hangi yayınların tutulacağına dair kesin karar | ⏳ (avukat) |
| 116 | **Play'de yayın** | Geliştirici kimliği/adres belgesi onayı | ⛔ (Google/sen) |

### P1 — Vaat edilen özellikler ve kalite

| # | İş | Not |
|---|---|---|
| 117 | **Widget** (ana ekrandan tek dokunuş favori kanal) | Onaylanmış özellik, hiç başlanmadı |
| 118 | **Chromecast** | Google Cast SDK bağımlılığı |
| 119 | **EPG** (şu an ne var) | Güvenilir XMLTV kaynağı bulunmalı |
| 120 | **İnternette TV kanalı arama** | Radyoda var; TV'de yalnızca kendi URL/M3U ekleme var |
| 121 | **Tablet/katlanabilir/çentik testi** | A13'ün kapanması için |
| 122 | **2-5 dk tanıtım videosu** | `pazarlama/` klasöründe ham kayıt var, kurgu yok |
| 123 | **Mağaza ekran görüntüleri (4-8)** | v9.25'te ızgara/TV8/Ayarlar görüntüleri hazır; mağaza boyutlarına kırpılacak |
| 124 | **İngilizce mağaza metni + ASO** | Global büyüme |
| 125 | **Yedek kaynakları tüm kanallara yay (#103 kalanı)** | Şu an 5 kanalda; kalanlar için kaynak toplama |

### P2 — Sonra

| # | İş |
|---|---|
| 126 | Native reklam / mediation ile gelir artırma |
| 127 | "Pro" abonelik (senin fikrin: zamanla aylık abonelik) |
| 128 | Favoriler/ayarlar için dışa-içe aktarma |
| 129 | TalkBack / erişilebilirlik, yazı boyutu |
| 130 | Açılışta iskelet liste (shimmer), ülke indirmesi beklerken |

---

## D) CLAUDE'UN P0 UYARILARI — SON DURUM

| P0 | Konu | Durum | Kalan risk |
|---|---|---|---|
| P0-1 | Telif riski (ücretli kanallar, çıplak IP, Sinema grubu) | ✅ v9.19 (305→156, IP 0) + v9.26 (12 ölü çıkarıldı) | **Devam:** kaynaklar hâlâ halka açık yayın adresleri; hukuki görüş (#115) ve sunucu allowlist (#108) gerekli |
| P0-2 | Gizlilik politikası + Veri Güvenliği | ✅ metinler gerçekle uyumlu (AdMob, ipapi.co, 13+) | Play formu doldurulmadı (#114) |
| P0-3 | UMP / AB rıza ekranı | ✅ v9.20 | — |
| P0-4 | Üyelik sistemi | ✅ v9.21 kaldırıldı | — |
| P0-5 | Arka plan ses servisi | ◐ a,b,c,e,g düzeltildi (v9.18 + v9.26); d (audio focus + noisy) v9.26'da eklendi | **Kalan:** bildirim ikonu/dili (h), radyo ve çoklu ekranda `POST_NOTIFICATIONS` (i) → #109 |
| P0-6 | Android 15/16 edge-to-edge | ✅ v9.22 + v9.25 (oynatıcı) | Yan/çentik testi yok (#121) |
| P0-7 | İmza anahtarı ve depo hijyeni | ◐ şifre repodan çıktı; `CanliTV.apk` hâlâ debug anahtarıyla imzalı (bilinçli: üstüne kurulum için) | **Kalan:** şifre rotasyonu (#110), app-ads.txt kök alan adı (#113) |
| P0-8 | Mağaza kaydı | ✅ başlık 29 karakter, açıklama sayıları düzeltildi (v9.22) | Reklamsız süre metni 15 dk'ya çekilecek; app-ads.txt (#113) |

**Kısa cevap:** Bizi **Play'den attıracak** seviyede açık bir madde kalmadı; ama **hukuki risk (P0-1)** ve **şifre rotasyonu (P0-7)** hâlâ açık. Bu ikisi kapanmadan "risksiz" denemez.

---

## E) TV8/ATV DÜZELTMELERİ DİĞER KANALLARA UYGULANACAK MI?

**Evet — iki katman var:**

1. **Oynatıcı katmanı (kod):** v9.25'te eklenen 3 saniyelik sağlık denetimi (hazırlanmayan / görüntü vermeyen / donan yayını yeniden başlat) ve yedek kaynak zinciri **tüm kanallar ve tüm pencereler için** çalışıyor. Kanala özel kod yok; ATV'ye özel düzeltme sadece kök hatanın (paylaşılan sayaç) ortaya çıktığı yer.
2. **Kaynak katmanı (veri):** Yedek/alternatif URL şu an yalnızca **TV8, ATV, Show TV, Kanal D, NOW** için tanımlı. Bu oturumda tüm liste tarandı: **143/157 çalışıyor, 14 ölü** bulundu; TRT Müzik ve Kral Pop yeni resmi kaynakla düzeltildi, 12 ölü kanal (TRT 3 kapandı, TJK TV 2 sadece YouTube'da, 7 coğrafi kilitli yabancı, ATV Alanya/ETV Manisa/TGRT Belgesel kaynaksız) listeden çıkarıldı → **145 kanal**.
3. **Yayıncı reklam kuşağı (TV8'e özel):** TV8'in "dijital reklam kuşağı" karesi yayıncının kendi reklam aralığı; uygulama atlayamaz. Diğer kanallarda bu davranış görülmedi (tarama `slate` sonucu 0).

**Sıradaki adım:** Kalan kanallar için ikinci kaynak toplama (#125) ve bunu otomatikleştirme (#112).

---

## F) BU OTURUMDA TAMAMLANANLAR (v9.26, versionCode 35)

- **#119 Kanal listesi:** Tüm kanallar gerçek oynatma zinciriyle (master → en düşük çalışan varyant) yeniden tarandı:
  143 ok / 13 ölü / 1 şüpheli. 12 ölü kanal çıkarıldı (ATV Alanya, ETV Manisa, TGRT Belgesel, TJK TV 2,
  TRT 3, Rakuten ×4, HOT Drama, .sci-fi, 4ever Drama) → **157'den 144'e** (TRT Muzik yinelenen kayıt da silindi).
  Önceki taramada "ölü" görünen **TRT Muzik** ve **Kral Pop** aslında çalışıyordu; hata tarama yöntemindeydi
  (yalnız son varyant deneniyordu / 302 takip edilmiyordu).
- **#120 Kral Pop:** kaynak `dygvideo.dygdigital.com/live/hls/kralpop` olarak değişti. `Net.follow()` eklendi:
  oynatmadan önce 301/302 zinciri çözülüyor, kalite seçimi son adrese göre yapılıyor. Telefonda açıldı.
- **#109 Ses servisi:** audio focus + kulaklık çıkarma + 3 saat wake lock + http düşürmesinin kaldırılması +
  `MultiActivity.onPause` `isFinishing` + Android 13 bildirim izni. Kalan tek parça: bildirim ikonu/dili (h).
- **#117/#118:** `POST_NOTIFICATIONS` isteği (ızgara + radyo), `allowBackup="false"`, `supportsRtl="true"`.
- **Doğrulama:** 144 kanal listelendi; TRT Muzik, Kral Pop TV ve TRT 1 telefonda açıldı; çökme yok.

**Risk durumu (kısa):** Play'den attıracak yeni bir açık yok. Açık kalan riskler dışarıya bağlı:
hukuki görüş (#115), keystore şifre rotasyonu (#110), app-ads.txt alan adı (#113), Data Safety (#114), Play yayını (#116).
Kod tarafında kalan en büyük risk **#107 (ExoPlayer'a geçiş)** ve **#108 (sunucu allowlist)**.
