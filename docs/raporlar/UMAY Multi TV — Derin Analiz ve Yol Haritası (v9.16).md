# Dokümanları okudum, şimdi kaynak kodu (reklam, oynatıcı, radyo servisi, sunucu) inceliyorum.

# 

# Oynatıcı ve reklam kodunu bitirdim; şimdi çoklu ekran, ülke/kanal yükleme ve sunucu (worker.js) tarafına geçiyorum.

# 

# Hesap/sunucu tarafını bitirdim; şimdi gizlilik politikası, ekran görüntüleri ve kalan dosyalara (Main, Prefs, radyo, kayıt) hızlıca bakıp raporu yazacağım.

# 

# Kaynak kodu, sunucu, mağaza dokümanları ve kanal listesini baştan sona okudum; şimdi raporu yazıyorum.

# 

# Doküman tek seferde sığmadı; iki parçaya bölüp gönderiyorum (önce bölüm 0–5, sonra 6–9).

# 

# Raporu hazırladım; kod, sunucu, mağaza belgeleri ve kanal listesini baştan sona okudum. Ben uygulamayı cihazda çalıştırmadım, bu yüzden ses servisi maddelerinden bir kısmı kod okumasına dayanıyor ve raporda "doğrula" diye işaretli.

# 

# Raporda üç ana vaadin (3 ekran, radyo + kilit ekranı, ülkeye göre kanal) teknik analizi, reklam geliri önerileri, mağaza sıralaması için strateji ve ajan için 20 maddelik sıralı görev listesi var.

# 

# \*\*Şu haliyle Play'e göndermeyin.\*\* En önemli bulgular:

# 

# 1\. \*\*Telif riski (en büyük risk):\*\*

# &#x20;  - Kanal listesinde Cinemax, AXN, Star Action, BBC First gibi ücretli kanalların yetkisiz yeniden yayınları var; çoğu çıplak IP adreslerinden geliyor.

# &#x20;  - 305 kanalın 137'si "Sinema" grubunda.

# &#x20;  - Play uygulamayı kaldırabilir, AdMob da hesabı kapatıp birikmiş kazancı iptal edebilir. Reklam geliri hedefleyen bir proje için bu en tehlikeli nokta.

# &#x20;  - Önerim: yalnızca resmi, ücretsiz akışları tutmak ve kullanıcının kendi listesini ekleyebildiği "oynatıcı" modeline geçmek.

# 2\. \*\*Gizlilik politikası yanlış:\*\* "Uygulamada reklam yok, reklam kimliği yok" yazıyor, ama AdMob var. IP'den ülke tespiti için `ipapi.co`'ya da istek atılıyor. Bu tek başına ret sebebi.

# 3\. \*\*AB rıza ekranı (UMP) yok:\*\* Almanya, İsveç ve İngiltere'yi hedefliyorsunuz.

# 4\. \*\*Üyelik sistemi bozuk ve gereksiz:\*\*

# &#x20;  - Başka cihazda giriş yapılamıyor.

# &#x20;  - Sunucuda herhangi bir kodla doğrulama geçiyor.

# &#x20;  - Google ile açılan hesap ele geçirilebiliyor.

# &#x20;  - Hesabın uygulamaya kattığı bir şey yok. İlk sürümde tamamen kaldırın.

# 5\. \*\*Ses servisi hataları:\*\*

# &#x20;  - Bildirimdeki Durdur düğmesi çalışmıyor.

# &#x20;  - Radyoda ⏭ TV kanalına geçiyor.

# &#x20;  - Ses odağı (audio focus) hiç yok.

# &#x20;  - Oynatıcıyı kapatınca ses devam ediyor olabilir.

# 6\. \*\*Mağaza kaydı:\*\*

# &#x20;  - Başlık 32 karakter, Play sınırı 30.

# &#x20;  - Açıklamada listede olmayan kanallar sayılıyor.

# 7\. \*\*İmza şifresi:\*\* `app/build.gradle` içinde açık yazılı, depo da halka açık. Şifreyi hemen değiştirin.

# 

# Çoklu ekran uygulamanın asıl farkı; ama sesi kapalı pencereler de tam kalitede iniyor ve yeniden bağlanma yok. Raporda Media3 ExoPlayer'a geçiş ve çözüm adımları var.

# 

# Gelir kısmında, içerik derecesini `G`'ye kısmak doldurma oranını ve kazancı düşürüyor. Mediation, liste içi native reklam ve Pro satın alma en büyük kaldıraçlar. Sıralama için kesin söz veremem: Play, indirme hızı, 7. gün tutundurma, puan ve çökme oranına bakıyor.

# 

# Ajana raporun 7. bölümündeki görevleri sırayla yaptırın ve her görevin kabul kriterini sağlamadan sonrakine geçirmeyin. Telif konusunda ben hukuki görüş vermiyorum; kesin karar için bir fikri mülkiyet avukatına danışın.

# 

# Raporu okuyabilir, doğrudan düzenleyebilir veya yorum bırakabilirsiniz; istediğiniz bölümü genişletirim.

# 

Rapor: https://claude.ai/artifact/QAaQzv1cQUZdci8HA4Sb5o


UMAY Multi TV — Derin Analiz ve Yol Haritası (v9.16)
===

*23 Kasım 2026 · Kaynak: UMAY-MULTI-TV-Claude-v9\_16.zip (kod, sunucu, mağaza belgeleri, kanal listesi tek tek okundu). Bu belge geliştirici ajana verilmek üzere "bulgu → neden önemli → ne yapılacak" biçiminde yazıldı. Cihazda çalıştırarak doğrulamadığım maddeleri "doğrula" diye işaretledim.*

## 0\. Yönetici özeti

Ürün fikri güçlü: tek uygulamada çoklu ekran TV, radyo, kilit ekranında ses, ülkeye göre kanal listesi. Teknik iskelet de çalışıyor. Ancak **şu haliyle Play'de yayına çıkarsa ya reddedilir ya da çıkıp kısa sürede AdMob/Play hesabı sorun yaşar.** Reklam geliri hedefin olduğu için en kritik 8 madde:

1. **Telif/yetkisiz yayın riski (en büyük risk).** Gömülü listede Cinemax, AXN, Star Action, Discovery LatAm, BBC First gibi ücretli kanalların ham IP adreslerinden yeniden yayınları var. Bu, hem Play politikasını hem AdMob "telifli içerik" kuralını ihlal eder; AdMob hesabı kapanırsa kazanç da kaybolur.
2. **Gizlilik politikası gerçeği anlatmıyor.** Politika "uygulamada reklam yok, reklam kimliği yok, sunucuya veri gitmez" diyor; uygulamada AdMob var, IP'den ülke tespiti için ipapi.co'ya istek atılıyor. Bu tek başına ret sebebi.
3. **AB/UK rıza ekranı (UMP) yok.** Almanya, İsveç, İngiltere hedefliyorsun; rıza olmadan reklam sunumu kısıtlanır ve yasal sorun doğar.
4. **Üyelik sistemi bozuk ve gereksiz.** Başka cihazda giriş yapılamıyor, sunucuda doğrulama ve hesap ele geçirme açıkları var, Play hesap silme zorunluluğu getiriyor. Hesabın uygulamaya kattığı bir şey de yok. **v1'de tamamen kaldır.**
5. **Arka plan ses servisinde hatalar:** bildirimdeki Durdur düğmesi çalışmıyor, radyoda ⏮⏭ TV listesinde gezinir, oynatıcıyı kapatınca ses devam ediyor olabilir, ses odağı (audio focus) hiç yok.
6. **Android 15/16'da ekran düzeni** (targetSdk 36, zorunlu edge-to-edge): üst bar sistem çubuğunun altında kalabilir. Testler yalnızca Android 13'te yapılmış.
7. **Mağaza kaydı hatalı:** başlık 32 karakter (sınır 30), açıklamada listede olmayan kanallar (Show TV, Now TV) sayılıyor, "305 kanal/50 radyo" gerçekle uyuşmuyor, README "reklamsız, hesapsız" diyor.
8. **İmza şifresi açık yazılı** (app/build.gradle) ve halka açık GitHub deposu kullanılıyor.

Bunların hepsi çözülebilir. Aşağıda önce engelleyicileri, sonra üç ana vaadini (3 ekran, radyo+kilit, çok dilli ülke listesi), sonra reklam gelirini, sonra "üst sıralara çıkma" stratejisini ve en sonda ajan için sıralı görev listesini veriyorum.

## 1\. Projenin mevcut durumu (kısa envanter)

* Saf Android framework Java (AndroidX/Media3 yok), `VideoView` + `MediaPlayer`, minSdk 26, targetSdk 36, versionName 9.16 (versionCode 25). `minifyEnabled false`.
* 12+ ekran: Main, Player, Multi (1–4 pencere), Radio, RadioPlayer, Settings, Account, Country, Status, Recordings. Kayıt (HLS→.ts), PiP, uyku zamanlayıcı, veri sayacı.
* Kanal kaynağı: TR için gömülü 305 kanal (iptv-org'dan türetilmiş), diğer 49 ülke için çalışma anında `iptv-org.github.io` indirmesi. Doğrulanmış "known-good" listesi yalnızca 10 ülke için var (TR, US, DE, GB, IN, BR, MX, RU, SA, SE).
* Radyo: radio-browser.info; TR gömülü liste şu an 25 istasyon.
* Reklam: AdMob banner + geçiş + ödüllü; sunucu: Cloudflare Worker + D1 (üyelik, e-posta kodu), Resend ile e-posta.
* Play durumu: geliştirici kimliği onayı bekliyor; uygulama henüz mağazada değil.
* Dokümanlar kendi içinde tutarsız: `CLAUDE-README.md` v9.7/16 diyor, manifest 9.16/25; README "reklamsız, hesapsız" diyor.

## 2\. Yayın engelleyiciler (P0 — bunlar bitmeden Play'e gönderme)

### P0-1. Kanal içeriği ve telif riski

**Bulgu (assets/channels.m3u içinden):**

* Ücretli/lisanslı kanalların yetkisiz yeniden yayını görünüyor: Cinemax (ABD West, Classics, Hits, Çekya), AXN Movies/AXN Crime, Star Action, Discovery Science Latin America, BBC First, Film+ ve benzerleri. Bunların çoğu `http://88.212.x.x`, `http://23.237.x.x:8080`, `http://50.7.x.x:8080` gibi **çıplak IP sunucularda**, yani yayıncının resmi dağıtımı değil.
* 305 kanalın **137'si "Sinema"** grubunda; bu grup en riskli bölüm.
* 59 kanal `http://` (şifresiz), bir kısmı kişisel GitHub deposundan (`raw.githubusercontent.com/...`) geliyor; ömrü belirsiz.
* Pazarlama metinlerinde "Kablolu TV'ye para vermeyi bıraktım" gibi cümleler var; bu, ücretli yayıncılar ve Play incelemesi açısından yanlış mesaj.

**Neden önemli:** Google Play "Fikri Mülkiyet" politikası, telif hakkı sahibinin şikâyetiyle uygulamayı kaldırır ve tekrarında geliştirici hesabını kapatır. AdMob, telifli içeriği izinsiz sunan uygulamalarda reklam sunumunu durdurabilir ve **birikmiş kazancı iptal edebilir**. Reklam gelirine dayanan bir proje için bu, tek hamlede her şeyi kaybetmek demek.

**Ne yapılacak (güvenli model):**

1. **"Oynatıcı + açık dizin" modeline geç.** Gömülü liste yalnızca yayıncısının kendisinin halka açık ve ücretsiz sunduğu resmi akışlardan oluşsun (TRT'nin kendi alan adındaki akışları, resmi ücretsiz FAST/AVOD kanalları, belediye ve kamu yayıncıları, resmi haber kanalları). Ücretli kanallar ve çıplak IP kaynakları **tamamen çıksın**.
2. Ülke listelerini (iptv-org) **uygulamada ham haliyle gösterme.** Sunucuda bir "izin listesi" (allowlist) tut: yalnızca alan adı/yayıncı doğrulanmış kanallar uygulamaya gitsin. Uygulama sunucudan *filtrelenmiş* JSON alsın; doğrudan GitHub m3u'sunu indirmesin.
3. **Kullanıcı kendi M3U/URL'sini ekleyebilsin** ("Kendi listeni ekle"). Uygulama içerik dağıtan değil oynatıcı olur; bu, Play'deki birçok oynatıcının kullandığı yöntem. Ama kendi başına koruma sağlamaz; yine de 1 ve 2 şart.
4. **Bildirim ve kaldırma süreci:** Ayarlar'da "Telif/İçerik bildir" e-postası, 24–48 saatte kaldırma, haftalık otomatik kontrol. Mağaza açıklamasındaki "kanallar yayıncıların halka açık akışlarıdır" notu doğru kalacak şekilde listeyi gerçekten buna uydur.
5. Yetişkin/XXX grup başlıklı girdileri **kod seviyesinde filtrele** (şu an `CountryRepo.parse` hiçbir grubu elemiyor). Kanal adlarındaki `\[Geo-blocked]`, `\[Not 24/7]`, `(1080p)` ekleri de temizlenmeli.
6. Hukuki olarak kesin görüş için Türkiye'de bir fikri mülkiyet avukatına kısa bir danışma yap; ben hukuki tavsiye vermiyorum, risk haritasını çıkardım.

**Kabul kriteri:** Listede çıplak IP veya ücretli yayıncı kanalı kalmaması; her kanalın yanında kaynak alan adı ve "resmi mi" bayrağı bulunan bir sunucu tablosu.

### P0-2. Gizlilik politikası ve Play Veri Güvenliği formu

**Bulgular:**

* `gizlilik-politikasi.html` (22 Kasım) "bu sürümde reklam yoktur, reklam kimliği oluşturulmaz, analiz/izleme yazılımı yoktur" diyor. Uygulamada AdMob SDK'sı ve AD\_ID izni var.
* Politika "sunucuya hiçbir şey gitmez" derken uygulama ilk açılışta `https://ipapi.co/json/` çağırıyor (IP'n üçüncü tarafa gidiyor), her ülke seçiminde `freebuff.site/good/\*.json` indiriyor.
* Politika "18 yaş ve üzeri" diyor, `magaza-kaydi.md` "13+" diyor. Listede çocuk kanalları ve çizgi film var; kitle karışık.
* Hesap silme yalnızca "e-posta yaz" ile; Play, hesap açan uygulamalarda uygulama içi silme ve web'den silme bağlantısı ister.

**Yapılacak:** Politikayı gerçeğe göre yeniden yaz (AdMob, reklam kimliği, cihaz bilgileri, IP, Cloudflare, Resend, üçüncü taraf yayın sunucuları; KVKK ve GDPR başlıkları; kitle tek bir yaş grubu olarak tutarlı). Data Safety formunu aynı metinle doldur (reklam kimliği: *toplanır, reklam amacıyla*). Hesap sistemi kaldırılırsa bu bölümün yarısı kendiliğinden sadeleşir.

### P0-3. UMP / AB rıza ekranı

APK içinde `user-messaging-platform` kütüphanesi zaten var (AdMob getirmiş) ama kullanılmıyor. **AB, UK ve İsviçre kullanıcılarına rıza formu gösterilmeden reklam isteği yapma.** Akış: `ConsentInformation.requestConsentInfoUpdate` → gerekirse form → `canRequestAds()` true ise `MobileAds.initialize`. Ayarlar'a "Reklam gizlilik tercihleri" satırı ekle. Şu an `AdManager.init` doğrudan başlatıyor.

### P0-4. Üyelik sistemi — kaldır

**Bulgular (kod + sunucu):**

* **Başka cihazda giriş imkânsız:** `Account.hash` her cihazda *rastgele tuz* üretiyor ve sunucuya bu cihaza özel özet gidiyor. Aynı e-posta/şifre başka telefonda farklı özet üretir, "şifre hatalı" alırsın. Uygulamayı silip yeniden kurunca da giriş kaybolur.
* **Özet = şifre:** Sunucu gelen özeti olduğu gibi saklıyor; veritabanı sızarsa özetle doğrudan giriş yapılır.
* **/api/verify açığı:** Doğrulanmış bir e-postaya *herhangi bir kodla* `ok:true` dönüyor (kod kontrolünden önce erken dönüş). Uygulama `ok` görünce oturum açıyor. Yani Google akışındaki "6 haneli kod" hiç doğrulanmıyor ve başkasının e-postasıyla "giriş" yapılabiliyor.
* **Hesap ele geçirme:** Google ile açılan hesabın `hash` alanı NULL. `/api/register` bu durumda hash'i üzerine yazıyor; saldırgan o e-postaya şifre koyup giriş yapabilir.
* **Posta kötüye kullanımı:** `/api/register` hız sınırı olmadan istenen adrese e-posta gönderiyor ve `name` alanı HTML olarak maile gömülüyor (HTML enjeksiyonu). Alan adın ve Resend hesabın spam/phishing için kullanılabilir, hesap askıya alınabilir.
* Her istekte `CREATE TABLE IF NOT EXISTS` çalışıyor (gereksiz yük). Gönderen `onboarding@resend.dev`, yani gerçek kullanıcılara posta zaten gitmiyor.
* Hesabın uygulamaya sağladığı işlev yok: favoriler ve ayarlar zaten yerel; bulut senkronu yok.

**Karar:** v1'de hesap, e-posta, Google girişi ve Worker'ın üyelik uçlarını **tamamen kaldır**. Worker'da yalnızca statik site, `app-ads.txt` ve `good/\*.json` kalsın. Bu; Play hesap silme zorunluluğunu, Data Safety karmaşasını, e-posta maliyetini ve tüm güvenlik açıklarını bir hamlede ortadan kaldırır. Gelecekte hesap gerekirse Firebase Auth veya Credential Manager ile Google girişi kullan, kendi şifre sistemini yazma.

### P0-5. Arka plan ses servisi (kilit ekranında dinleme vaadi)

`AudioService.java` ve onPause akışları için bulgular:

|#|Bulgu|Etki|Çözüm|
|-|-|-|-|
|a|Bildirimdeki **Durdur** düğmesi `action="stop"` gönderiyor, servis ise `getBooleanExtra("stop")` bekliyor|Düğme çalışmıyor; kullanıcı sesi kapatamaz|`"stop".equals(action)` işle|
|b|**Radyoda ⏮⏭** `step()` içinde `ChannelRepo.all()` yani **TV listesi** kullanılıyor; çalan radyo URL'si orada yok, indeks 0'a düşüyor|Radyo dinlerken ⏭ ile TV kanalı #1 sesi başlar|Servise "kaynak türü" (tv/radyo) ve kendi listesini ver|
|c|`PlayerActivity.onPause` ve `MultiActivity.onPause`, `isFinishing()` bakmadan servisi başlatıyor|✕ Kapat/geri ile çıkınca ses arka planda sürebilir; Durdur da bozuk (a)|`if (isFinishing()) return;` ekle; doğrula|
|d|**Audio focus yok**, `ACTION\_AUDIO\_BECOMING\_NOISY` yok|Arama gelince/başka uygulama açılınca üst üste ses; kulaklık çıkınca hoparlörden yüksek ses|Focus + duck/pause + noisy receiver|
|e|PiP'e girerken `onPause` PiP modundan önce çalışabilir; hem PiP hem servis sesi başlayabilir|Çift ses|PiP durumunu `onUserLeaveHint` bayrağıyla yönet; doğrula|
|f|WakeLock ve WifiLock **zaman aşımsız**|Servis takılırsa pil boşalır|Timeout + sadece oynarken tut|
|g|https başarısızsa **http'ye düşürme**|Güvenlik ve içerik bütünlüğü|Kaldır veya sadece kullanıcı onayıyla|
|h|Bildirim ikonu `android.R.drawable.ic\_media\_play`, bildirim metni sabit Türkçe|Amatör görünüm; dil tutarsızlığı|Kendi vektör ikon + Lang|
|i|`POST\_NOTIFICATIONS` yalnızca oynatıcıdan, "arka planda dinle" anında isteniyor; radyo ve çoklu ekran yolu istemiyor|Android 13+'da bildirim görünmeyebilir, kilit ekranı kontrolleri kaybolur|Radyo/çoklu yolunda da iste|

**Önerilen asıl çözüm:** `MediaPlayer` yerine **Media3 ExoPlayer + `MediaSessionService`**. Audio focus, noisy, bildirim, kilit ekranı kontrolleri, Bluetooth/araç tuşları, HLS uyumluluğu ve yeniden bağlanma hazır gelir. `is-listesi.md`'de #39 olarak zaten planlı; bunu P1 değil **P0** yap, çünkü "kanal sürekli takılıyor" şikâyetinin kök nedeni `VideoView/MediaPlayer`.

### P0-6. Android 15/16 ekran düzeni

targetSdk 36'da pencere zorunlu olarak kenardan kenara çizilir. Kodda yalnızca `AccountActivity` ve `SettingsActivity` **üst** inset'i uyguluyor; `AdManager.wrap` ve `PlayerActivity` yalnızca alt inset'i. Ana ekranda başlık/durum çubuğu ve kamera çentiği altında kalabilir. Tüm ekranlara ortak bir `Insets` yardımcısı (üst+alt+yan) ekle; test: Pixel emülatör Android 15 ve 16, çentikli ekran, katlanabilir.

### P0-7. İmza anahtarı ve depo hijyeni

* `app/build.gradle` içinde `storePassword`/`keyPassword` düz yazı (`*** (keystore.properties) ***!`), dokümanda "şifresi make-aab.sh içinde" yazıyor, depo **halka açık**. Anahtar dosyası .gitignore'da ama şifre depoda. Şifreyi ortam değişkeni/`keystore.properties`'e taşı, **mevcut şifreyi de değiştir** (upload anahtarı Play App Signing ile sıfırlanabilir; şimdi yap).
* `CanliTV.apk` **debug anahtarıyla** imzalı. Kullanıcılar bu APK'yı yan yükleyip sonra Play'den indirirse imza uyuşmazlığı yüzünden güncelleme/kurulum çakışır. Play'e çıkınca sitedeki "APK indir" sayfasını kaldır veya "Play'den indir" yap.
* Zip'te 5 MB'lık APK'dan iki kopya, 12 MB'lık `ham-kayit.mp4`, `build\_aab/` çıktıları var; depoya girmesin (Git LFS veya Releases).
* `local.properties` kişisel yol içeriyor; `android:allowBackup` belirtilmemiş (varsayılan açık); `android:supportsRtl` yok. Manifest'te bunları düzelt.
* APK'nın manifestinde `debuggable` ifadesi geçiyor; `aapt2 dump badging` ile **false** olduğunu doğrula.

### P0-8. Mağaza kaydındaki hatalar

* Başlık: "UMAY Multi TV — Canlı TV \& Radyo" = **32 karakter**; Play sınırı 30. Öneri: `UMAY Multi TV: Canlı TV Radyo` veya `Multi TV – Live TV \& Radio`.
* Açıklamada "Show TV, Now TV" sayılıyor ama `is-listesi.md` #71 bu kanalların kaynakta olmadığını söylüyor. Yanıltıcı meta veri ret sebebidir; yalnızca gerçekten çalışan kanalları yaz.
* "305 kanal, 50 radyo" → radyo listesi 25 istasyona düştü (v9.16 notları). Sayıları otomatik üret veya yuvarlak ve doğru yaz.
* "Ücretsiz" doğru, ama "60 dakika reklamsız" ifadesi artık 20 dk ve yalnızca *video* reklamı kaldırıyor (banner kalıyor); metni düzelt.
* AdMob app-ads.txt, `umay-multi-tv-gq3zer.freebuff.site` alt alan adında. Google genelde geliştirici web sitesinin **kök alan adındaki** dosyayı tarar. **Kendi alan adını al** (hem app-ads.txt hem e-posta hem marka için), Play'deki web sitesi alanını ona yönlendir.

## 3\. Üç ana vaat: derin teknik analiz

### 3.1. Aynı anda 3 (4) ekran

**Bulgular (`MultiActivity`):**

* Her pencere ayrı `MediaPlayer` + `SurfaceView`. Birçok telefon donanım video çözücüsünü 2–4 eşzamanlı oturumla sınırlar; düşük/orta segmentte 3–4 HLS 720p–1080p açmak çözücü hatası, takılma ve ısınma demek.
* **Kalite her pencerede aynı** (`Quality.resolve`). Sesi kapalı pencereler de tam kalitede iniyor: veri ve CPU israfı. 4 pencere × 1080p ≈ saatte 6–8 GB'a çıkabilir.
* Hatada yalnızca etiket ("açılamadı"); **yeniden deneme yok**. Kopan yayın siyah kalır.
* Ekran kilidinden dönüşte `surfaceDestroyed` tüm oynatıcıları bırakıyor, `surfaceCreated` hepsini yeniden çözümlüyor (4 ağ isteği, hepsi aynı anda yeniden tampon). Yavaş ve gözle görünür.
* Kontrol paneli + veri satırı + sarı ödül şeridi + banner, dikey ekranda 4 pencerenin alanını belirgin küçültüyor.
* Sesi açık olmayan pencereler de ses kodlayıcı çalıştırıyor.

**Öneri:**

1. Media3 ile her pencerede ExoPlayer; **sesi açık olmayan pencereye otomatik ≤480p/360p tavan** (`setMaxVideoSize` / track selection), sesli pencereye seçili kalite.
2. Cihaz yeteneği tespiti: `MediaCodecList` ile eşzamanlı çözücü sayısını bul; sınırı aşarsa 4'lü modu "2×2 düşük kalite"ye düşür veya uyar.
3. Pencere başına **otomatik yeniden bağlanma** (3 deneme, artan bekleme) + "yedek URL" (#42).
4. Yatayda 2×2, dikeyde 1+2 yerleşim; katlanabilir ve tablet için büyük ekran düzeni (aşağıya bak).
5. Reklam şeridini çoklu ekranda küçült veya yalnızca kontrol çubuğuyla birleştir.
6. Bu **ürünün ayırt edici özelliği**; mağaza görsellerinde ve ilk açılışta öne çıkar (aşağıda ASO).

### 3.2. Radyo ve "tuş/ekran kilidi aktifken çalışma"

İki farklı kullanım var, ikisini ayrı ele al:

* **Ekran kilitliyken ses:** Mevcut mimari (ön plan servisi + MediaSession) doğru fikir; P0-5'teki hatalar giderilince çalışır. Android 14+ için `foregroundServiceType="mediaPlayback"` zaten manifestte; `POST\_NOTIFICATIONS` akışını tamamla.
* **Dokunma kilidi (çocuk/cep dokunuşu):** `PlayerActivity` içinde var (🔒). Çoklu ekranda ve radyoda yok; ekle. "Kilit aktifken de çalışsın" ifadeni bu iki anlamla birlikte mağaza metnine yaz.
* **Kilit ekranında *görüntü*:** Telefon kilitliyken video akışı platform gereği durur; ancak kullanıcı isterse `setShowWhenLocked(true)` + `setTurnScreenOn(true)` ile aktivite kilit ekranının üstünde gösterilebilir (opt-in, "yataktaki TV" kullanımı). Varsayılan kapalı; açıklamayla.
* Radyo: sonraki istasyon otomatiği, "son dinlenenler", uyku zamanlayıcının kalıcılığı, yayın kopunca yeniden bağlanma, Bluetooth/araç tuşları (Media3 ile geliyor). ICY metadata (şu an çalan şarkı) göster; kullanıcı bağlılığını belirgin artırır.

### 3.3. Çok dilli, ülkeye göre kanal listesi

**Bulgular:**

* **Dil:** 4 arayüz dili, 65 çeviri anahtarı, ama kodda **70'ten fazla `Toast`, bildirim, diyalog, reklam şeridi metni sabit Türkçe** (örn. `AdManager` "▶ Reklam izle · 20 dk reklamsız", "Bugün kullanılan veri", `AudioService` bildirimi, `PlayerActivity` diyalogları). İngilizce/Rusça/Arapça kullanıcı karışık dil görür.
* **Arapça RTL yok:** manifestte `supportsRtl` yok, düzen soldan sağa. Arap pazarı (SA) hedefliyorsan bu ciddi kalite sorunu.
* **Dil dosyaları kodda** (`Lang.java`, `pick(c, tr, en, ru, ar)`); Android `strings.xml` + `values-xx` kaynaklarına taşı. Bu, Play mağaza yerelleştirmesiyle de uyumlu çalışır ve yeni dil eklemeyi kolaylaştırır. Hedef diller: en, tr, ru, ar, de, es, pt-BR, hi (kanal listen zaten bu ülkelerde).
* **Ülke tespiti:** `ipapi.co` ile IP'den. Ücretsiz katman sınırlı ve ticari kullanımda sorunlu; ölçekte 429 döner. Üstelik gizlilik politikasıyla çelişiyor. **Yerine:** `TelephonyManager.getNetworkCountryIso/SimCountryIso` + `Locale.getDefault().country` (ağ isteği ve izin gerektirmez); ilk açılışta sor/öner.
* **Liste kalitesi:** Doğrulanmış liste yalnızca 10 ülke için; yayın sağlık raporuna göre örnekte US 34/45, DE 34/45 açılıyor, **DE radyo 11/25**. Kalan 40 ülke ham iptv-org listesi (çoğu ölü/geo-kilitli). Bir ülkeyi "hazır" saymak için kural koy: *en az 30 doğrulanmış çalışan kanal* yoksa o ülke listede "yakında" görünsün, kullanıcıya ölü liste gösterilmesin.
* **Bayatlama:** `fetchGood` yalnızca ülke indirilirken çağrılıyor; ülke bir kez indirilince bir daha yenilenmiyor. TR'nin `good\_tr.json`'u APK'ya gömülü, güncellenemez. **Çözüm:** uygulama açılışta (Wi-Fi'de, günde bir) `good/<cc>.json` ve kanal listesini ETag ile yenilesin; sunucu tarafında günlük cron ile sağlık taraması (#37) çalışsın.
* **Grup adları ASCII:** "Muzik", "Cocuk", "Cizgi Film", "Kultur" olarak gösteriliyor; Türkçe karakterli ve çevrilmiş etiketlere çevir.
* Kanal numarası mantığı ülke değişince sıfırlanıyor; kullanıcı numara özelleştirmesi ülke bazında saklanmalı (kontrol et).

## 4\. Reklam geliri analizi ve optimizasyonu

### 4.1. Mevcut kurulum ve bulgular

|Öğe|Durum|Değerlendirme|
|-|-|-|
|Banner|Her ekranda altta, adaptif|İyi; ama video ekranında banner + sarı şerit + veri satırı üç katman|
|Geçiş reklamı|4. kanal açılışı + 10 dk aralık|Frekans makul; kanal *açılırken* gösteriliyor, kullanıcı beklenmedik sürede kesilir. Doğal geçiş anı olarak "çıkışta/kapanışta" daha güvenli|
|Ödüllü|"20 dk reklamsız", **kanal bazlı**|Kullanıcıya zayıf teklif (kanal değişince biter), ama gelir açısından iyi; metinde sadece video reklamın kalktığı yazmalı|
|İçerik derecesi|`MAX\_AD\_CONTENT\_RATING\_G`|**Fill ve eCPM'i ciddi düşürür.** Şikâyet edilen reklamı engellemek için tepeden kısmak yerine panelde "Hassas kategoriler" ve "Reklam inceleme merkezi" kullan; derecelendirmeyi PG veya T'ye çıkar|
|App Open|Kapalı|İlk izlenimi bozmadığı sürece (soğuk açılışta ve günde 1–2 kez) iyi gelir kaynağı|
|Mediation|Yok|En büyük kaldıraç (aşağıda)|
|Yerleşim güvenliği|Sarı şerit banner'ın hemen üstünde, sistem çubuğuna yakın|Yanlışlıkla tıklama riski; AdMob "yanıltıcı yerleşim" kuralı için şeridi banner'dan ayır|
|Ödül sonrası|`a.recreate()`|Aktiviteyi yeniden yaratıp **yayını baştan başlatıyor**; kullanıcı kanalı kaybeder. Yeniden yaratma yerine şeridi güncelle|
|`Ads.TEST\_DEVICE`|Koda gömülü|Gerçek cihaz kimliği; sürümde sorun değil ama temizle|

### 4.2. Gelir nasıl artar (öncelik sırasıyla)

1. **Mediation (AdMob Mediation + AppLovin MAX/Unity/Meta/Pangle/Mintegral):** Aynı gösterim için rekabeti artırır; tipik olarak eCPM'de belirgin artış (yüzde 20–60 aralığı yaygın, garanti değil). Önce UMP şart (P0-3).
2. **Native advanced reklam (liste içi):** Her \~8–10 kanal satırında bir native; banner'dan genellikle yüksek eCPM, kullanıcıyı rahatsız etmez. Satır tasarımına uyan, "Reklam" etiketi belirgin olacak şekilde.
3. **Ödüllü geçiş (Rewarded Interstitial) ve doğal geçiş noktaları:** Radyo ekranından TV'ye geçiş, çoklu ekran çıkışı, uyku zamanlayıcı sonu.
4. **App Open:** soğuk açılışta, günde en çok 2, en az 4 saat arayla; önce ana ekran yüklensin.
5. **Banner yenileme:** AdMob panelinde birim yenileme süresini 30–60 sn tut (açıkken uzun süre izleyen TV kullanıcıları için gösterim sayısını katlar). Video ekranında sürekli görünen banner'ın **görünürlük/etkileşim kalitesini** izle.
6. **Pro (reklamsız) abonelik/tek seferlik satın alma (Play Billing):** Reklam gelirinin yanında düzenli gelir. Öneri: ücretsiz sürümde 2–3 ekran; **Pro'da 4 ekran, kayıt, reklamsız, ek temalar**. Ödüllü reklam "geçici Pro denemesi" gibi sunulabilir.
7. **Coğrafya:** ABD, UK, DE, SE gibi ülkelerde eCPM, TR/IN/BR/MX/SA'ya göre çoğu zaman kat kat yüksektir. Mağaza metnini ve ilk sayfa kanallarını bu pazarlara göre yerelleştirmek (İngilizce ve Almanca listing) gelir/kullanıcıyı doğrudan artırır.

### 4.3. Gerçekçi gelir beklentisi (varsayımlı örnek)

Formül: **Gelir = DAU × kullanıcı başı gösterim × eCPM / 1000.** Örnek varsayım: 5.000 DAU; günde kullanıcı başı 30 banner, 2 geçiş, 0,3 ödüllü gösterimi; eCPM'ler karma coğrafyada banner 0,25 $, geçiş 2 $, ödüllü 6 $: 5.000 × (30×0,25 + 2×2 + 0,3×6) / 1000 = 5.000 × 13,3 / 1000 ≈ **66 $/gün ≈ 2.000 $/ay.** Bunlar tahmindir; gerçek değerler ülke karmasına, içerik derecesine, doldurma oranına ve politika uyumuna göre 3–5 kat sapabilir. Ana ders: **gelir DAU ve süreye bağlı**; bu yüzden aşağıdaki "tutundurma" maddeleri reklam ayarlarından da önemli.

### 4.4. Kitle ve politika

Uygulama çocuk kanalları barındırıyor ve çocuklar kullanabilir. Ya net "13+/genel kitle" olarak kal ve çocuk kanallarını "Aile" altında ebeveyn kilidiyle sun, ya da çocuk modu ekle. Aynı politika tüm belgelerde (gizlilik, Data Safety, içerik derecelendirme, AdMob `tagForChildDirectedTreatment`) tutarlı olmalı; şu an üç farklı yaş beyanı var.

## 5\. Mağazada üst sıralara çıkma ve çok tercih edilme stratejisi

**Dürüst çerçeve:** "Canlı TV" kategorisi kalabalık; hiçbir uygulama bir ayarla en üste çıkmaz. Play sıralaması esas olarak **indirme hızı, yükleme sonrası tutundurma (D1/D7/D30), puan ve yorum, kaldırma oranı, Android vitals (çökme/ANR) ve güncelleme sıklığı** ile belirlenir. Dolayısıyla strateji: (1) *aranan kelimelerde görünür ol* (ASO), (2) *ilk 7 günde tutundurma yüksek olsun* (ürün), (3) *niş farkını net anlat* (konumlandırma), (4) *çökme ve ölü kanal olmasın* (kalite).

### 5.1. Konumlandırma: "Çoklu ekran + arka planda radyo"

Rakipler tek ekran oynatıcı. Senin farkın: **aynı anda 3–4 kanal, ayrı ses kontrolü, kilit ekranında çalma, ülke ve dil seçimi.** Tüm mağaza görselleri ve ilk 80 karakter bunu söylesin. Örnek kısa açıklama (EN): *"Watch up to 4 live TV channels at once. Radio plays with the screen locked."* Örnek TR: *"Aynı anda 4 canlı TV kanalı izle. Ekran kilitliyken radyo çalar."*

### 5.2. ASO (Mağaza optimizasyonu)

1. **Varsayılan dili İngilizce yap**, `tr, ru, ar, de, es-419, pt-BR, hi` için ayrı yerelleştirilmiş listing ekle. Şu an yalnızca Türkçe hazır (`#33`).
2. **Başlık (30 karakter):** marka + en güçlü kelime. Ör. `UMAY Multi TV: Live TV \& Radio` (30'u aşmıyor mu kontrol et).
3. **Anahtar kelimeler (açıklama içinde doğal geçsin):** live tv, multi screen tv, watch tv online, split screen tv, radio background, news channels, world tv, free tv, sleep timer. Her dil için yerel arama terimlerini Play Console arama ve Google Trends ile çıkar. "IPTV" kelimesini dikkatle kullan; politika incelemesinde dikkat çeker.
4. **Görseller:** İlk 2 ekran görüntüsü karar belirler. 1. görsel *3 pencere aynı anda* (ürünün farkı), 2. *kilit ekranı kontrolleri + radyo*, 3. *ülke seçimi*, 4. *kayıt/uyku zamanlayıcı*. Üstüne tek cümlelik, büyük yazı; her dilde ayrı set. Mevcut görsellerde test reklamı ("Nice job! This is a 320x50 test ad") ve gerçek kanal logoları/yayınları görünüyor; **mağaza görsellerinde test reklamı olmasın, üçüncü taraf logo ve yayın görüntüleri mümkünse çıkarılsın** (telif).
5. **Tanıtım videosu (30 sn):** `pazarlama/ham-kayit.mp4` var; sesli/yazılı kes, ilk 3 saniyede çoklu ekranı göster.
6. **Özel mağaza sayfaları ve Mağaza Denemeleri (Store Listing Experiments):** başlık, ikon ve ilk görsel için A/B test aç.

### 5.3. Ürün tarafında tutundurma (ASO kadar önemli)

* **İlk açılış < 5 sn, sıfır kayıt:** ülke/dil otomatik, ilk popüler kanal kendiliğinden başlasın; kullanıcı 1 dokunuşta yayın görsün. (Hesap kaldırılınca zaten kolaylaşır.)
* **Ölü kanal sıfır:** İlk 20 kanalın %100 açıldığından emin ol (sunucu cron + otomatik atlama + yedek URL). Kullanıcı kaybının ilk nedeni budur.
* **Çoklu ekranı ilk günde göster:** "3 kanalı aynı anda dene" ipucu; kayıtlı hazır setler (Haber seti, Spor seti, Müzik seti) tek dokunuşla.
* **Widget ve favori kısayolları (#16), Chromecast (#17), Android TV/Fire TV desteği** (`leanback` + TV banner; manifestte `touchscreen required=false` zaten var). TV ve büyük ekranlar bu kategoride çok kullanılıyor.
* **Tablet/katlanabilir düzeni:** çoklu ekran için en iyi cihaz sınıfı; "büyük ekran kalitesi" Play'de ayrı öne çıkarma sinyali.
* **Yeniden etkileşim:** bildirimle değil, widget ve "son izlenen kanaldan devam et" ile.
* **Değerlendirme isteme:** Link yerine **In-App Review API**, 3. oturum + en az 10 dk izlemeden sonra, hata/çökme yaşamamış kullanıcıya. İlk haftada 10+ gerçek yorum sıralamayı belirgin etkiler; yorumlara 48 saatte cevap ver.
* **Kalite eşikleri:** Android vitals'ta çökme oranı ve ANR'yi Play'in "kötü davranış" eşiğinin çok altında tut; Crashlytics ekle (gizlilik metnine yaz).
* **Ölçüm yok = körlük:** Firebase Analytics (rıza ile) veya en az AdMob + Play Console raporları; D1/D7 tutundurma, kanal başına oynatma başarı oranı, oturum süresi, reklam gösterimi/DAU izle.

### 5.4. Yayın öncesi ve sonrası büyüme

* **12 test kullanıcısı / 14 gün** kuralını Play Console'daki güncel metinle doğrula; gerçek kişiler, her gün kullanan testerlar, geri bildirim topla.
* **Güncelleme ritmi:** 2–4 haftada bir (Play yeni sürümleri öne çıkarır).
* **Topluluk ve içerik pazarlaması:** TikTok/Reels'te "3 kanalı aynı anda izle" tarzı kısa videolar; ülkeye göre Reddit, XDA, Telegram grupları, Rusça/Arapça/Hintçe yerel topluluklar. Tanıtımda üçüncü taraf yayın görüntüsünü uzun gösterme; kendi arayüzün ağırlıkta olsun.
* **Gerçek bir web sitesi + alan adı:** `/indir` yerine marka sayfası, SSS, telif bildirim formu, gizlilik. Hem güven hem arama görünürlüğü.
* **Sezonluk fırsatlar:** büyük spor/haber dönemlerinde (lig, kupa, seçim) "çoklu ekran maç + haber" kampanyası ve ilgili kanal setleri.

## 6\. Diğer teknik hata ve iyileştirme listesi (P1/P2)

|Şiddet|Dosya|Bulgu|Çözüm|
|-|-|-|-|
|Yüksek|`PlayerActivity`|`VideoView/MediaPlayer`; sabit 13 sn watchdog; yayın koparsa otomatik yeniden bağlanma yok; HLS+AAC uyumsuzlukları|Media3 ExoPlayer, tampon/yeniden deneme, otomatik kalite düşürme (#39, #56)|
|Yüksek|`ChannelRepo`|Statik, kilitsiz liste; `all()` içinde `Collections.sort`; servis ve UI iş parçacığından erişim|Değişmez liste + tek yükleme noktası; sıralamayı arka planda yap|
|Yüksek|`CountryRepo`|Ham m3u'yu doğrudan GitHub'dan indirir; hiçbir grup/içerik filtresi yok; yenileme politikası yok|Sunucu tarafı filtreli JSON, ETag, günlük yenileme|
|Orta|`Good`|Yalnızca 10 ülkeyi kapsıyor; bayat dosya kontrolü yok|Tüm desteklenen ülkeler için cron; JSON'a `generatedAt` ekle, 7 günden eskiyse yok say|
|Orta|`AdManager`|`wrap()` her çağrıda `ChannelRepo.all()` ve `Prefs` okur; `houseBar` ile `rewardStrip` kodu kopya|Tek bileşen; ödülde `recreate()` yerine şerit güncelle|
|Orta|`Net`|`TrafficStats.getUidRxBytes` uygulamanın *tüm* trafiğini sayar (reklam, indirme dahil)|Oynatıcıdan gelen bayt sayacı (ExoPlayer `BandwidthMeter`)|
|Orta|`RadioPlayerActivity/RadioRepo`|Ölü istasyonlar için tek tek tarama; DE'de 11/25 açılıyor|Sunucuda radyo doğrulaması, ICY metadata, otomatik sonraki|
|Orta|`HlsRecorder`|Yalnızca HLS, şifreli yayını reddediyor; uçtan uca test edilmemiş (#31); uzun kayıtta depolama/kota kontrolü yok|Boş alan kontrolü, süre/limit; telif nedeniyle kaydı yalnızca kişisel kullanım olarak sun|
|Orta|`AndroidManifest`|`usesCleartextTraffic=true` tüm uygulamada; `allowBackup`; `supportsRtl` yok; portre kilitli `MainActivity` (tablet/katlanabilir)|`networkSecurityConfig`, backup kuralları, RTL, büyük ekran düzeni|
|Orta|Build|`minifyEnabled false`, `shrinkResources` yok; dex \~12 MB|R8 + `shrinkResources`; AAB boyutu ve başlatma süresi iyileşir|
|Düşük|`Lang` / UI|Emoji ikonlar cihaza göre değişiyor (#29)|Vektör ikon seti|
|Düşük|Belgeler|`CLAUDE-README.md` v9.7, README "reklamsız, hesapsız", `yayin-saglik-raporu.md` 25 Kasım tarihli, radyo sayıları tutarsız|Tek doğru kaynak (`docs/DURUM.md`), sürüm başına güncelle|
|Düşük|Test|Yalnızca Appetize Pixel 7/Android 13|Android 8, 10, 12, 14, 15, 16, düşük RAM, tablet, katlanabilir; Firebase Test Lab|

## 7\. Ajan için sıralı görev listesi

> Ajana şöyle ver: "Aşağıdaki görevleri sırayla yap; her görevin \*\*kabul kriterini\*\* sağlamadan sonrakine geçme; her görev sonunda `docs/DURUM.md`'yi güncelle ve neyi değiştirdiğini kısaca yaz."

### Faz 1 — Yayın engelleyicileri (öncelik: önce bunlar)

1. **İçerik temizliği:** Çıplak IP, ücretli yayıncı ve `raw.githubusercontent.com` kaynaklı kanalları gömülü listeden çıkar; Sinema grubunu yalnızca resmi ücretsiz kaynaklarla yeniden kur. *Kabul:* listede `^https?://\\d+\\.\\d+\\.\\d+\\.\\d+` eşleşmesi yok; her kanalın kaynağı `kanallar.json`'da alan adı + "resmi" bayrağıyla belgelenmiş.
2. **Kullanıcı playlist'i:** "Kendi listeni ekle" (URL/dosya) ekle; yetişkin grup başlıklarını (`xxx`, `adult`) kod seviyesinde filtrele.
3. **Hesap sistemini söküp at:** `Account\*`, `Api`, Worker `/api/\*` uçları, ayar satırları, ilgili `Lang` metinleri. *Kabul:* uygulamada e-posta/şifre alanı yok; Worker yalnızca statik dosya + `good/\*.json` + `app-ads.txt` sunuyor.
4. **UMP:** Consent SDK'yı entegre et; `canRequestAds()` olmadan reklam isteme; ayarlarda gizlilik tercihi satırı.
5. **Gizlilik politikası + Data Safety:** gerçeğe göre yaz (AdMob, reklam kimliği, cihaz bilgileri, Cloudflare); ipapi.co çağrısını kaldır, yerine SIM/Locale kullan.
6. **AudioService düzeltmeleri:** P0-5 tablosundaki a–i. *Kabul:* Durdur çalışır, radyoda ⏭ radyo istasyonuna geçer, ✕/geri sonrası ses devam etmez, arama gelince ses durur, kulaklık çıkınca durur.
7. **Edge-to-edge:** Tüm ekranlara ortak inset yardımcısı; Android 15/16 emülatör ekran görüntüleri.
8. **Anahtar güvenliği:** şifreleri `keystore.properties`'e taşı, şifreyi yenile, deponun halka açık geçmişini kontrol et; sitedeki APK bağlantısını Play çıkınca kaldır.
9. **Mağaza kaydı:** başlık ≤30, yanlış kanal adlarını sil, sayıları düzelt, EN/TR listing, test reklamı içermeyen yeni ekran görüntüleri.

### Faz 2 — Çekirdek kalite

10. **Media3 ExoPlayer geçişi** (tek ekran, çoklu ekran, ses servisi `MediaSessionService`); otomatik yeniden bağlanma ve kalite düşürme.
11. **Çoklu ekran kalitesi:** sessiz pencerelere kalite tavanı, çözücü sınırı tespiti, 2×2 yatay/1+2 dikey, pencere başına yeniden deneme.
12. **Sunucu cron:** günlük kanal ve radyo sağlık taraması, 50 ülke `good/<cc>.json`, `generatedAt`; uygulama günlük yenileme; "ülke hazır" eşiği (≥30 çalışan kanal).
13. **Yerelleştirme:** `strings.xml` + `values-xx` (en, tr, ru, ar, de, es, pt-BR, hi), sabit metinlerin tamamı kaynağa; Arapça RTL; grup adlarını çevir.
14. **Android vitals hazırlığı:** R8/shrink, Crashlytics (rıza ile), çökme ve ANR sıfırlama turu.

### Faz 3 — Gelir ve büyüme

15. **Reklam optimizasyonu:** içerik derecesini PG/T'ye çıkar, panelde hassas kategorileri kapat, native liste reklamı, App Open (soğuk açılış, sınırlı), ödüllü akışta `recreate()` kaldır, ödül metnini düzelt, yerleşim güvenliği (şerit–banner aralığı).
16. **Mediation:** AppLovin MAX veya AdMob Mediation ile en az 2 ek ağ; test ve karşılaştırma.
17. **Pro satın alma:** Play Billing; 4 ekran, kayıt, reklamsız; ücretsiz 14 gün deneme.
18. **Widget, Chromecast, Android TV** desteği; tablet/katlanabilir düzen.
19. **In-App Review**, olay ölçümü (D1/D7, kanal başarısı, reklam/DAU), mağaza deneme (A/B) kurulumu.
20. **Alan adı + site:** marka sayfası, telif bildirim formu, `app-ads.txt` kök alan adında; Play'de web sitesi güncel.

## 8\. Hedef metrikler (ilk 90 gün)

|Metrik|Hedef|
|-|-|
|Çökmesiz oturum|≥ %99,5|
|ANR oranı|< %0,3|
|İlk 20 kanal açılma başarısı|≥ %98|
|Yayının 5 sn içinde başlama oranı|≥ %90|
|D1 / D7 tutundurma|≥ %35 / ≥ %15|
|Oturum başına izleme süresi|≥ 12 dk|
|Mağaza puanı|≥ 4,3 (ilk 100 yorumda)|
|Reklam doldurma oranı|≥ %85|
|DAU başına reklam geliri|izle; mediation sonrası ≥ %25 artış|

## 9\. Son söz

En çok kazandıran şey reklam ayarı değil, **kullanıcının ilk dakikada çalışan bir yayın görüp 7 gün sonra hâlâ uygulamayı tutması.** Sıra şu: (1) içeriği ve gizliliği temizle ki hesap ve yayın güvende olsun, (2) oynatıcıyı sağlamlaştır (Media3, çoklu ekran kalitesi, ölü kanalsız liste), (3) çoklu ekran + kilit ekranı farkını mağazada ve ilk açılışta öne çıkar, (4) ondan sonra reklam mediation ve Pro ile geliri büyüt. Faz 1 bitmeden Play'e gönderme; reddedilme veya yayın sonrası kapanma riski, kaybedilecek haftalardan çok daha pahalı.

