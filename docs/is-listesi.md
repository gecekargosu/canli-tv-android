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
54 | **Reklam dolmadığında kendi barımız (house bar)** | No-fill'de ekranın altı boş kalmıyor; her radyo/TV sayfasında bar var | ✅ (v9.8) |
55 | **Ölü radyo istasyonunda otomatik sonraki istasyon** | 9 sn'de ses yoksa işaretle + geç (max 3) — "Radyo 7" gibi kayıtlar asılı kalmıyor | ✅ (v9.8) |
56 | **Takılma (buffering) için oynatıcı tamponu** | Tampon süresini artır + takılınca otomatik kalite düşür; ExoPlayer (#39) ile birlikte | ⏳ |
57 | **Cihaz matrisi testi** (Pixel 7 dışında: Android 8/10/12, küçük ekran 720×1280, tablet) | Appetize'da her cihaz 3 dk ücretsiz; sırayla denenmeli | ⏳ |
58 | **TR listesine eksik ana kanalları ekle** (ATV, Show, Now, TRT 2, TRT Belgesel) | Sıralamada ilk 20 gerçekten "en çok izlenen" olsun | ⏳ |
59 | **Appetize yerine Firebase Test Lab** (gerçek cihaz, 3 dk sınırı yok) | A'dan Z'ye otomatik tur + çökme videosu | ⏳ |
60 | **TR listesine gerçek yayın URL'siyle eksik kanallar** (Show, Now, TRT Müzik, TRT Çocuk, TV100, TVNET, Beyaz TV) | İsimler hazır ama doğrulanmış m3u8 linki gerekiyor | ⏳ |

## G. v9.9'da tamamlananlar (23 Kasım 2026)

| # | İş | Durum |
|---|---|---|
61 | Popüler sıralamada parantezli asıl kanal öne (ATV, ATV Alanya'nın önünde) | ✅ |
62 | **IP'den ülke önerisi** (ilk açılışta tek sefer sorar, desteklenen 50 ülke) — iş listesi #38 | ✅ |
63 | **Tek dokunuşla ödüllü video şeridi** ("▶ Reklam izle · 20 dk reklamsız", her ekranda) + süre 20 dk | ✅ (v9.10) |
64 | **Doğrulanmış "çalışan kanallar" listesi (known-good)** — #37 | ✅ (v9.11): `_build/good.py` ülke listelerini dener, `site/public/good/<cc>.json` yayınlanır, uygulama indirir ve çalışanları başa alır. TR 89/90, DE 73/90, US 67/90 |
65 | **Haftalık otomatik tazeleme** (bu taramayı cron'a bağla) | ⏳: şimdilik elle `python _build/good.py ...` + deploy |
66 | **Banner her zaman görünür** (reklamsız sürede de) + **20 dk ödül kanal/istasyon bazlı** | ✅ (v9.12) |
67 | **Yetişkin/erotik reklam engeli** — kod: `setMaxAdContentRating(G)` (v9.13) | ✅ kod tarafı; her istekte geçerli |
68 | **AdMob paneli: Hassas kategoriler** → Flört + Cinsel içerik kapat | ⏳ (panel adımı; "Engelleme kontrolleri" sayfası açık) |
69 | **Reklam inceleme merkezi** ile şikayet edilen reklamı tek tek engelle | ⏳ (Afterglow gibi spesifik reklamlar için)
70 | **Radyoda ölü istasyonu listeden kaldır** — #41 | ✅ (v9.14)
71 | **TR eksik kanallar** — #60 | ⚠️ kısmen: TRT Çocuk/Müzik zaten var; Show TV, Now TV, TV100, Teve2 kaynakta yok (doğrulanmış link gerekli) |

## H. ÖNCELİK SIRASI (revize — 23 Kasım 2026, v9.10 sonrası)

> Aşağıdaki sıra geçerlidir; B bölümündeki eski numaralar aynı işleri gösterir.

| Sıra | # | İş | Neden bu sırada |
|---|---|---|---|
1 | 37 | **Worker cron: günlük yayın sağlık taraması → known-good** | "Kanal açılmıyor"un kök çözümü; kullanıcı ölü link görmez |
2 | 39 | **ExoPlayer (Media3) geçişi** | "Sürekli takılıyor"un asıl çözümü |
3 | 56 | Tampon + otomatik kalite düşürme | ExoPlayer ile birlikte yapılır |
4 | 60 | TR listesine Show/Now/TRT Müzik/TRT Çocuk/TV100/TVNET/Beyaz (doğrulanmış link) | İlk 20 gerçekten "en çok izlenen" olsun |
5 | 41 | Radyoda ölü istasyon filtresi (listeden de kalksın) | Radyo şikâyeti |
6 | 40 | Radyo listesini popülerliğe göre sırala (TR) | Aynı mantık radyoda |
7 | 42 | Yedek (mirror) URL | Tek kaynak ölünce kanal tamamen düşmesin |
8 | 16 | **Widget** | Onaylanmış özellik, kullanıcıyı geri getirir |
9 | 17 | **Chromecast** | Onaylanmış özellik, TV deneyimi |
10 | 19 | Yavaş internette otomatik 360p | Veri dostu |
11 | 15 | Otomatik ülke (IP) — temel hali #62 ile geldi; kalan: dil eşleme | Global büyüme |
12 | 18 | EPG (şu an ne var) | Güvenilir kaynak bulunursa |
13 | 25 | **Hesap silme** | Play zorunluluğu |
14 | 26 | **UMP/AB rıza ekranı** | AB trafiği için yasal |
15 | 27 | Mağaza ekran görüntüleri (4-8) | Yükleme için hazır olmalı |
16 | 33 | İngilizce mağaza metni + ASO | Global |
17 | 28 | İzleme sayısına göre otomatik sıra | Kişiselleştirme |
18 | 29-30 | Vektör ikonlar · Arapça RTL | Görsel kalite |
19 | 31 | Kayıtlar ekranı testi | Test borcu |
20 | 32 | "Reklamsız/Pro" uygulama içi satın alma | Ek gelir |
21 | 20-23 | Google girişi · domain/mail · Play yayını · kapalı test | ⛔ dışarıya bağlı |
22 | 34-36 | Geliştirici adı · app-ads.txt · Play formları | ⛔ Google tarafı |

## F. v9.7'de tamamlananlar (23 Kasım 2026)

| # | İş | Durum |
|---|---|---|
43 | Üst üste iki banner hatası (radyo listesi) | ✅ |
44 | Ülkeye göre "önce tanınmış kanallar" sırası (TR/US/GB/DE/RU/IN/BR/MX/SA/FR/ES/IT/NL/SE) | ✅ |
45 | Ölü kanal filtresi (2 hatada gizle, varsayılan açık) | ✅ |
46 | Açılmayan kanalda otomatik sonraki kanal (en fazla 3 kez) | ✅ |
47 | Radyo çalar üst barı yazılı/büyük + dikey başlık kaldırıldı | ✅ |
48 | Banner sistem gezinme çubuğunun üstünde | ✅ |
