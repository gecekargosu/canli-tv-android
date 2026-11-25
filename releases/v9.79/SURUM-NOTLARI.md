# UMAY Multi TV 9.79 (versionCode 88)

Tarih: 25 Kasım 2026

## Üst bar: 3 sabit satır

1. **Satır — kimlik:** solda logo, sağında tek satır **"Umay Multi: Canlı TV & Radyo"**.
   Logonun altındaki "no ile aç, 3'lü izle, kayıt" yazısı **kaldırıldı** (alakasız duruyordu,
   satırı da şişiriyordu). Başlık yüksekliği logo ile aynı (46dp).
2. **Satır — düğmeler:** 3'lü · radyo · bayrak · kayıt · ayarlar. Kutular 56dp → **44dp**,
   içindeki ikonlar **21sp** (önce 15sp idi). İstenen buydu: kutu küçük, ikon büyük.
3. **Satır — sekmeler:** Tümü · ★ Favori · Son · Gizli · 🎚 Oto · ✏️. 2. ve 3. satır aynı
   yükseklikte (44dp içerik + eşit boşluk); satır içerikleri ekranın iki kenarına eşit hizalı
   (12dp). "Favoriler" sekme genişliğine sığmadığı için "Favori" olarak kısaltıldı.

Arama kutusu + No + Git **tek satır ve 44dp**; Git kutusu küçültüldü (56dp min yükseklik kaldırıldı).

## Logo parlaması

Önceki parlama (alfa 1→0,72) neredeyse görünmüyordu. Artık logonun arkasında turuncu bir
parlama katmanı 1,1 sn'de bir nabız gibi atıyor (alfa 0,42→0,05 + hafif ölçek), logo da
1→0,65 arası yumuşak sönüyor. Üç satır sabit kalıyor, liste altında kayıyor.

## Diğer

- İkon/başlık rengi **değişmedi**: üst bardaki logo uygulamanın kendi simgesi (@mipmap/ic_launcher).
- Reklam yuvası, veri şeridi ve kanal listesi bu sürümde değişmedi.
