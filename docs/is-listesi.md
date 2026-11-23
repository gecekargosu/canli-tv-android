# UMAY Multi TV — ana iş listesi (oturum oturum)

Durum: ✅ bitti · 🔄 sürüyor · ⏳ sırada · ⛔ engel (dışarıya bağlı) · ❌ yapılmayacak
Son güncelleme: 23 Kasım 2026 (v9.4)

## A. Bitti

| # | İş | Durum |
|---|---|---|
1 | 305 kanal, numaralı kumanda, arama, filtreler, favoriler | ✅ |
2 | Çoklu ekran (3'lü/4'lü), kayıt, PiP, uyku zamanlayıcı | ✅ |
3 | 50 ülke kanal listesi + 4 dil arayüz | ✅ |
4 | İsteğe bağlı üyelik (e-posta + kod doğrulama), misafir modu | ✅ |
5 | Üyelik sunucusu (Cloudflare Worker + D1) + gizlilik politikası | ✅ |
6 | AdMob: hesap onayı, uygulama + 3 birim, app-ads.txt, SDK entegrasyonu | ✅ |
7 | Reklamlar: liste banner, oynatıcı altı banner, geçiş (4. zap / 10 dk), ödüllü 60 dk reklamsız | ✅ |
8 | Radyo ülkeye göre (radio-browser, önbellekli) | ✅ |
9 | Radyo sınıfları, favoriler, kapat/çık, uyku zamanlayıcı | ✅ |
10 | Hata kaydı (`crash.txt`) + Ayarlar → Hata bildir | ✅ |
11 | Gradle geçişi, imzalı APK/AAB, sürüm klasörleri, GitHub release | ✅ |
12 | E-posta gönderimi (Resend API anahtarı sunucuda, `mail:true`) | ✅ |
13 | Pazarlama klasörü + çekim planı + metinler + ham ekran kaydı | ✅ |
14 | **v9.4: radyonun hiç çalmaması + ekolayzır donukluğu + üyelik sonrası ana sayfa** | ✅ |

## B. Sırada (öncelik sırası)

| # | İş | Not | Durum |
|---|---|---|---|
15 | İlk açılışta **telefonun ülkesi/dili otomatik** (Rus telefon → Rus kanal+radyo) | Ülke listesi indirme asenkron yapılmalı | ⏳ |
16 | **Widget**: ana ekrandan tek dokunuş favori kanal | `AppWidgetProvider` + kanal seçici | ⏳ |
17 | **Chromecast**: yayını TV'ye gönderme | Google Cast SDK (Gradle bağımlılığı) | ⏳ |
18 | **EPG** (şu an ne var) | Güvenilir XMLTV kaynağı bulunmalı | ⏳ |
19 | Yavaş internette otomatik 360p + daha görünür "yükleniyor" göstergesi | Senin onayın var | ⏳ |
20 | Google ile giriş | Cloud Console'da OAuth client + 4 secret | ⛔ (senin hesabın) |
21 | Gerçek kullanıcılara mail | Domain alımı ya da tek-gönderen doğrulayan sağlayıcı | ⛔ |
22 | Play'de yayın | Geliştirici kimliği doğrulaması (adres belgesi: fatura) | ⛔ |
23 | Kapalı test 12 tester / 14 gün | Play kaydı açılınca | ⏳ |
24 | Reklam gelirini artırma: App Open (açılış) reklamı, mediation | Politika sınırları içinde, ölçerek | ⏳ |

## C. Teknik borç / iyileştirme

- İlk açılış deneyimi: ülke indirmesi sürerken iskelet liste (shimmer) göster.
- Kanalların ölü yayınlarını otomatik temizleme (haftalık sağlık taraması arka planda).
- Radyo: "son dinlenenler" ve uyku sayacı kalıcılığı.
- Yedekleme: favoriler/ayarlar için dışa/içe aktarma.
- Erişilebilirlik: içerik açıklamaları (TalkBack), yazı boyutu büyütme.

## D. Yapılmayacaklar (bilinçli)

- Zorunlu reklam (yayın kesme), video üstüne banner → AdMob politikası ihlali.
- Uygulama açılışında tam ekran reklam (şimdilik) → ilk izlenim bozulur.
- **Radyo ekranındaki "TV alanında sürekli video reklam" fikri** → AdMob'da reklamın içerik gibi
  görünmesi ("reklamı yayın sanma") yanıltıcı tıklama sayılır, hesap askıya alınabilir. Yerine
  banner (yapıldı) + ödüllü video kullanıyoruz.

## E. Konuşmalardan çıkan, unutulmuş maddeler (23 Kasım 2026 taraması)

| # | İş | Neden önemli | Durum |
|---|---|---|---|
25 | **Hesap silme** (uygulama içinden: Ayarlar → Hesabımı sil) | Play, hesap açan uygulamalarda zorunlu tutuyor; yoksa ret | ⏳ |
26 | **UMP / AB rıza ekranı** (AdMob Consent SDK) | AB + İsveç trafiğinde reklam kişiselleştirme için yasal zorunluluk | ⏳ |
27 | **Mağaza ekran görüntüleri** (en az 4: ana sayfa, oynatıcı, radyo, çoklu ekran) | Play en az 2, önerilen 8; yükleme için hazır olmalı | ⏳ |
28 | **Watch-count'a göre otomatik sıralama** (çok izlenen üstte) | İlk günden beri istediğin madde; şu an yalnızca elle sıralama var | ⏳ |
29 | **Vektör ikon seti** (emoji yerine) | Emoji cihaza göre değişiyor, mağaza görünümünü bozuyor | ⏳ |
30 | **Arapça RTL düzeni** (yalnızca metin çevrildi, yerleşim soldan sağa) | Arap pazarı hedefi | ⏳ |
31 | **Kayıtlar ekranı uçtan uca test** | Hiç denenmedi (izin kaldırıldıktan sonra) | ⏳ |
32 | **"Reklamsız / Pro" uygulama içi satın alma** (tek seferlik) | AdMob'a ek gelir kanalı, Gemini önerisi | ⏳ |
33 | **İngilizce mağaza metni + ASO anahtar kelimeleri** | Global hedef (ABD/Hindistan/Brezilya) | ⏳ |
34 | **Geliştirici adı "Yabanarisi" incelemesi** | Google tarafında, sonuç bekleniyor | ⛔ |
35 | **AdMob app-ads.txt doğrulaması** | Mağaza linki eklenince kendiliğinden doğrulanır | ⛔ |
36 | **Play formları (12 adım)** | Kimlik onayı açılınca birlikte doldurulacak | ⛔ |
37 | **Worker cron: günlük yayın sağlık taraması** → `known-good` listesi | Açılmayan kanal sorununun kök çözümü; istemci ölü link indirmez | ⏳ (sıradaki #1) |
38 | **IP'den ülke tespiti** (`ipapi.co/json`) + ilk açılışta öneri/otomatik seçim | Yurt dışındaki kullanıcı doğru listeyi görsün | ⏳ |
39 | **Oynatıcıyı ExoPlayer'a taşıma denemesi** | VideoView/MediaPlayer bazı HLS+AAC yayınlarını açmıyor; ExoPlayer uyumluluğu çok daha yüksek | ⏳ |
40 | **Radyo listesini popülerliğe göre sıralama** (TR gömülü liste) | TV'de yapıldı, radyoda kaldı | ⏳ |
41 | **Radyo çalarda otomatik sonraki istasyon** | İstasyon açılmazsa 2 sn sonra sıradakine geçsin | ⏳ |
42 | **Yedek (mirror) URL alanı** | Aynı kanalın 2-3 kaynağı; biri ölünce sıradaki denenir | ⏳ |
49 | **Otomatik A'dan Z'ye test: Firebase Test Lab (Roboflow)** | Gerçek cihazda kendi kendine tıklar, çökme/ekran görüntüsü raporu verir; Google hesabı + faturalama ister | ⏳ |
50 | **Appetize.io** ile tarayıcıda APK çalıştırma | ✅ v9.7 Pixel 7 / Android 13'te açıldı, liste + banner + veri satırı göründü (23 Kas 2026). Tarayıcıdan tıklama sadece kullanıcı faresiyle yapılabiliyor | ✅ |
51 | **MobSF** (yerel, açık kaynak) güvenlik/statik analiz | APK'yı izinler, sertifika, gömülü anahtar açısından tarar (Docker) | ⏳ |
52 | **Her sürümde Claude paketi (`UMAY-MULTI-TV-Claude-vX.Y.zip`)** | Kaynak + doküman + sürüm notu + tanıtım videosu tek zip'te | ✅ (v9.7) |
53 | **Yerel otomatik gezinme testi** (`_build/crawl.py`) | Telefonda tüm ekranları/duğmeleri kendisi gezip çökme + ekran görüntüsü raporu üretir | ⏳ (sıradaki) |

## F. v9.7'de tamamlananlar (23 Kasım 2026)

| # | İş | Durum |
|---|---|---|
43 | Üst üste iki banner hatası (radyo listesi) | ✅ |
44 | Ülkeye göre "önce tanınmış kanallar" sırası (TR/US/GB/DE/RU/IN/BR/MX/SA/FR/ES/IT/NL/SE) | ✅ |
45 | Ölü kanal filtresi (2 hatada gizle, varsayılan açık) | ✅ |
46 | Açılmayan kanalda otomatik sonraki kanal (en fazla 3 kez) | ✅ |
47 | Radyo çalar üst barı yazılı/büyük + dikey başlık kaldırıldı | ✅ |
48 | Banner sistem gezinme çubuğunun üstünde | ✅ |
