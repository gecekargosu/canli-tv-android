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
