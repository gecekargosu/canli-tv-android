# UMAY Multi TV 9.80 (versionCode 89)

Tarih: 25 Kasım 2026

## Üst bar (3 sabit satır, aynı hizada)

1. **Marka kartı:** logo + "Umay Multi: Canlı TV & Radyo" artık **tek kutunun** içinde (56dp).
   Logo 38dp, **hep tam parlak** (solmuyor); çevresinde 5dp taşan turuncu ışık 1,3 sn'de
   bir nabız gibi atıyor. Önceki sürümde logo sönüyor ve arkasındaki ışık görünmüyordu.
2. **İkon satırı:** 5 kutu, **44dp** yükseklik, içindeki ikonlar **28sp (28dp)** — yazı yok:
   ▦ (3'lü) · 📻 · bayrak · ⏺ (kayıt) · ⚙.
3. **Sekme satırı:** 6 kutu, **44dp** — 2. satırla aynı yükseklik, aynı 12dp kenarlar,
   aralar 4dp. Tümü · ★ Favori · Son · Gizli · 🎚 Oto · ✏️.

Arama + No + Git satırı 44dp, Git kutusu küçültüldü.

## Diğer

- "Kendi kanalımı ekle" → **"Kendi yayın adresimi ekle"**, "Kendi kanallarım" →
  **"Eklediğim yayınlar"**. Bu bölüm favori işaretlemek için değil: elinde m3u8/stream
  adresi olan bir yayını listeye eklemek için.
- Reklam yuvası ana sayfa, tekil oynatıcı, çoklu ekran ve radyoda yerinde duruyor;
  AdMob dolmadığında turuncu "Reklam izle · 15 dk reklamsız" şeridi görünüyor.
- Not: ekranın solundaki ince gri dikey çizgi uygulamadan değil; MIUI'nin kenar çubuğu
  tutamacı (Chrome'da da aynı yerde çıkıyor).
