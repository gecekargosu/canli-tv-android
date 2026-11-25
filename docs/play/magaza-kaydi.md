# Play mağaza kaydı — kopyala/yapıştır metinler

Play Console'da **Uygulama oluştur** ekranı açıldığında bu dosyadaki metinleri aynen kullan.
Hepsi Play karakter sınırlarına uygundur (kısa açıklama 80, tam açıklama 4000).

## 1. Temel bilgiler

| Alan | Değer |
|---|---|
| Uygulama adı (30) | `UMAY Multi TV: Canlı TV Radyo` |
| Paket adı | `com.cengiz.canlitv` (AAB'den otomatik gelir, değiştirilemez) |
| Varsayılan dil | Türkçe |
| Kategori | Eğlence (Video oynatıcılar ve editörler de olabilir) |
| Etiketler | canlı tv, tv, radyo, haber, spor |
| Geliştirici adı | YABANARISI |
| E-posta | cengizkilic1980@gmail.com |
| Gizlilik politikası | https://umay-multi-tv-gq3zer.freebuff.site/gizlilik-politikasi |
| Web sitesi | https://umay-multi-tv-gq3zer.freebuff.site |

## 2. Kısa açıklama (80 karakter)

```
145 canlı TV kanalı ve 32 radyo: kanal numarası, favoriler, 3'lü izleme, kayıt.
```

## 3. Tam açıklama (kopyala)

```
UMAY Multi TV, Türkiye'nin ve dünyanın canlı TV kanallarını ve popüler radyolarını tek
uygulamada toplar. Kumanda alışkanlığına uygun numaralı kanal sistemi, favoriler ve
çoklu izleme ile hızlı bir TV deneyimi sunar.

🔢 NUMARALI KANAL (KUMANDA GİBİ)
Üstteki No kutusuna kanal numarasını yaz, Git'e bas: kanal hemen açılır. 145 kanal
Türkçe/yabancı ve tür olarak gruplanmıştır.

📺 145 CANLI KANAL
TRT 1, TRT 2, TRT Spor, TV8, ATV, Star TV, Kanal D, Haber Global,
NTV, A Haber, Halk TV, Kanal 7, TRT Müzik, TRT Çocuk ve çok daha fazlası.
🌍 Ülke seç: 50 ülkenin kanalları listeye eklenir (Türkiye gömülü, internet gerekmez).

📻 RADYO
32 Türk radyosu: TRT, Kral, Süper FM, Metro FM, Power, Joy FM, Radyo 7, Karadeniz FM...
Ülke değiştirince o ülkenin en çok dinlenen istasyonları listelenir.
Ekran kilitliyken de çalar (arka plan sesi + bildirim).

▦ 3'LÜ / 4'LÜ İZLEME
Aynı anda 3 veya 4 kanalı izle; her pencerenin sesini ayrı aç/kapat, pencereye dokununca
o kanal solo olur. Setlerini kaydet, sonra tek dokunuşla geri yükle.

✏️ LİSTE SENİN
Uzun bas → listeye gizle, sürükle-bırakla sırala, numara değiştir, favorilere ekle.
Kalite seç (360p/480p/720p/1080p), veri tasarrufu ve günlük internet limiti.

⏺ KAYIT
Yayını sesiyle birlikte telefona kaydet (Movies/TV-Kayitlari), uygulama içinden izle.

😴 UYKU ZAMANLAYICISI
15/30/45/60/90/120 dakika veya saat gir; süre dolunca uygulama kapanır.

🌐 4 DİL
Türkçe, English, Русский, العربية. Menüler seçtiğin dilde.

👤 HESAP GEREKMEZ
Ne e-posta ne Google hesabı istenir; bütün özellikler açıktır.

NEDEN ÜCRETSİZ?
Uygulama reklamla ayakta kalır: liste altında küçük bir banner ve seyrek geçiş reklamı.
Video izleyip 15 dakika reklamsız (video reklamı kalkar, banner kalır) kullanabilirsin. Yayının üstüne reklam binmez,
oynatma reklamla kesilmez.

NOT: Kanallar yayıncıların halka açık akışlarıdır; yayınlar zaman zaman kaldırılabilir.
Uygulamada kanal barındırılmaz. Geri bildirimin için: cengizkilic1980@gmail.com
```

## 4. Görseller (hazır dosyalar)

| Play alanı | Ölçü | Dosya |
|---|---|---|
| Uygulama ikonu | 512×512 | `docs/play/ikon-512.png` |
| Öne çıkan görsel | 1024×500 | `docs/play/one-cikan-1024x500.png` |
| Telefon ekran görüntüsü (en az 2) | 1080×1920 önerilir | `docs/play/v9-reklam.png`, `docs/play/v8-oynatici.png`, `docs/play/v7-ana.png`, `docs/play/v7-ana2.png` |

Ekran görüntüsü sayısı 2–8 arası; ilk ikisi en çok indirme getirir
(1: ana ekran + kanal listesi, 2: oynatıcı).

## 5. Mağaza formu cevapları

- **Reklam içeriyor mu?** Evet (banner + geçiş + ödüllü).
- **Uygulama içi satın alma?** Hayır.
- **Hedef kitle:** 13+ (çocuklara özel değil). "Çocuklara yönelik" seçilmezse
  Aile politikası ek formu gerekmez.
- **İçerik derecelendirmesi anketi:** Şiddet/cinsellik/kumar yok; kullanıcı etkileşimi yok;
  "kullanıcılar arası iletişim" yok. Sonuç genelde "3+ / Herkes".
- **Veri güvenliği (Data safety):**
  - Toplanan: **E-posta adresi** (isteğe bağlı hesap) — "Uygulama işlevi" + "Hesap yönetimi".
  - Şifre: cihazda PBKDF2 ile özetlenir, sunucuya düz gitmez → "Toplanmıyor" olarak beyan edilir.
  - Reklam kimliği: AdMob SDK kullanıldığı için "Reklam veya pazarlama" amaçlı **cihaz kimlikleri**
    toplanır → evet.
  - Veriler şifreli iletişimle (HTTPS) aktarılır, silme talebi e-posta ile karşılanır.
- **Hükümet uygulaması / finans / sağlık:** Hayır.
- **Haber uygulaması mı?** Hayır (canlı TV/radyo).
- **TV cihazı desteği:** İsteğe bağlı; şimdilik telefon/tablet yeterli.

## 6. Üst sıralarda çıkmak için (ASO) — sırayla

1. **Başlıkta anahtar kelime:** "UMAY Multi TV: Canlı TV Radyo" (marka + 2 kelime).
2. **Kısa açıklamada sayı:** "145 kanal", "32 radyo" tıklama oranını artırır.
3. **İlk 2 ekran görüntüsü:** ana ekran ve oynatıcı; üzerine kısa yazı eklenirse
   (Canva'da 1080×1920) dönüşüm yükselir.
4. **Yorum isteme:** Ayarlar → ⭐ Bizi değerlendir satırı hazır; ilk hafta 10+ yorum
   sıralamayı belirgin etkiler. Yorumlara 48 saat içinde cevap ver.
5. **Güncelleme sıklığı:** 2–4 haftada bir sürüm (Play yeni sürümleri üste taşır).
6. **ANAhtar kelimeler:** açıklamada doğal geçsin: canlı tv, tv izle, radyo dinle,
   kanal listesi, haber, spor, çocuk kanalları, ücretsiz tv.
7. **Kırılmayı önle:** kanal listesini güncel tut (⚙ → Tüm kanalları test et).
