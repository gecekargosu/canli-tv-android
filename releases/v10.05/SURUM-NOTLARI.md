# v10.05 (versionCode 114) — 27 Kasım 2026

Önceki arşiv: v10.03 (112) · `releases/v10.03/`
Boyut: APK 3.958.448 bayt · AAB 6.752.749 bayt · İmza `f3996c67…b8d5` (değişmedi)

Bu sürümün ana konusu **8 dil**, **temiz kanal adları** ve **tablet/telefon dayanıklılığı**.
Yeni özellik eklenmedi, var olan arayüz tamamlandı.

## 1. Arayüz 8 dilde (tr · en · de · fr · es · ru · ar · pt)
- `assets/i18n.tsv` **290 → 424 satır**; ayraç `§`, anahtar Türkçe metin, `I18n.t(ctx, "...")` ile okunuyor.
- Bu turda çevrilenler:
  - Oynatıcı alt barı (Kapat / Ana sayfa / Favori / Çoklu / Kalite / Kaydet) ve ipuçları,
  - Oynatıcı diyalogları (veri limiti, uyku zamanlayıcısı, mobil veri uyarısı, hata ekranı),
  - Çoklu ekran çipleri ve toast'ları,
  - Ana ekran: 24 kadar toast/açıklama (reklam ödülü, numarayla açma, sıralama, silme, boş liste),
  - **Durum listesi**: 24 başlık + 21 açıklama (`StatusActivity` çizim sırasında çeviriyor).
- Sekme kısa etiketleri düzeltildi: TR "Tümü / Favori / Son / Gizli", diğer dillerde tam etiket.
- Oynatıcıdaki "Kayıt" etiketi "Kaydet" anahtarına taşındı (FR "Enr.", DE "Aufn.") — uzun çeviri iki satıra kırılıyordu.
- Arapça RTL, Fransızca ve Almanca ekran görüntüleriyle doğrulandı.

## 2. Kanal adlarındaki ülke/çözünürlük etiketleri kaldırıldı
- Veri: `site/public/good/*.json` içinde **3.416 kayıttan 2.516'sı** temizlendi —
  `ATV (Turkiye)` → `ATV`, `TRT 1 (1080p)` → `TRT 1`. `assets/good_tr.json` yenilendi.
- Kod: `CountryRepo.stripCountry()` artık `Good.key()` içinde de çalışıyor; indirilen yeni
  listelerde etiket geri gelmiyor. `Lang.fold()` sayesinde "Turkiye" = "Türkiye".
- Sonuç (cihaz): 1 ATV · 2 TV8 · 3 Show TV · 4 Kanal D · 5 TRT Haber — hepsi sade.

## 3. Tablet/telefon dayanıklılığı
- `DefaultRenderersFactory.setEnableDecoderFallback(true)` (Player + Multi) ve
  `android:largeHeap="true"` — donanım çözücü hatasında yayın kapanmıyor, yedek çözücüye geçiyor.
- Tablet: TV8 40 sn kesintisiz oynadı, `logcat` FATAL/ANR yok; ağ kesme testinde uygulama
  kapanmıyor, "İnternet bağlantısı yok / Bağlanınca dokun" ekranı geliyor.
- Telefon: TV8 ve kanal geçişleri sorunsuz.

## Doğrulama
- `build.sh` → BUILD SUCCESSFUL · `make-aab.sh` → BUILD SUCCESSFUL.
- `verify-apk.py CanliTV.apk --prev releases/v10.03/CanliTV.apk --expect-version 10.05` → **TEMİZ**
  (395 dosya, 7044 sınıf, imza aynı).
- İki cihaza `install -r` → Success; her ikisi de 10.05 (114) çalışıyor.

## Bilinen açıklar (sonraki sürümler)
- Radyo/Hakkında alt paragrafları ve birkaç yardımcı ekran metni hâlâ TR.
- Teknik borç: RecyclerView, R8 küçültme ayarlarının gözden geçirilmesi, otomatik test,
  MainActivity bölme, TV/leanback + D-pad desteği, `good_tr.json` otomatik eşleşme.
