# UMAY Multi TV 9.45

**Tarih:** 24 Kasım 2026 · versionCode 54

## Alt reklam barı artık her ekranda sabit
- Bar, içeriğin altında **sabit** duruyor; liste kaydırılınca kaybolmuyor (ana ekran, radyo,
  ayarlar, ülke, kayıtlar, çoklu ekran, radyo çalar).
- Bar yüksekliği **her zaman 56 dp**; reklam yüklenirken/yüklenemezken kaybolmuyor.
  Reklam **gerçekten yüklenince** kendi tanıtım barımız gizleniyor (önceden erken gizlenip
  bar boş kalabiliyordu).
- Reklam dolmazsa ("No fill") **60 saniye sonra** bir kez daha deneniyor.

## Radyo ekranı yeni tasarım
- Boş çerçeve yerine **istasyon paneli**: "● CANLI YAYIN" etiketi, istasyon adı (büyük),
  "MP3 · 128 kbps · Türkiye" bilgisi ve "UMAY RADYO · canlı akış" satırı.
- Anlık bağlantı durumu panelin altında: "✅ Bağlantı iyi · çalıyor" / "📶 Bağlantı sorunu ·
  yeniden deneniyor (n/3)".

## Web sitesi: hukuki sayfalar
- **Kullanım Koşulları**: `/kullanim-kosullari.html`
- **Telif ve İçerik Kaldırma**: `/telif-bildirimi.html` (24–48 saatte kaldırma taahhüdü)
- Play yayın hazırlık kontrol listesi: `docs/play/PLAY-YAYIN-HAZIRLIK.md`
  (Data safety tablosu, beyanlar, mağaza metinleri, sıralı adımlar).

## Notlar
- İmza aynı; üzerine kurulabilir. Yeni izin yok.
