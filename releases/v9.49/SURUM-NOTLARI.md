# UMAY Multi TV 9.49

**Tarih:** 24 Kasım 2026 · versionCode 58

## TV'de reklam artık yayını düşürmüyor (M-74)
Kullanıcı geri bildirimi: "TV'de reklam gelince bağlantı kopuyor, kapatınca zor bağlanıyor".
Kök neden: tam ekran reklam açılırken oynatıcı çalışmaya devam ediyor, ses odağını kaybediyor ve
reklam kapanınca eski (geride kalmış) tamponla devam etmeye çalışıyordu; 15 sn donma sayacı da
reklam sırasında işlediği için kanal "açılmadı" sayılabiliyordu.

- Reklam açıkken yayın **duraklatılıyor**, donma/zaman aşımı sayaçları **durduruluyor**.
- Reklam kapanınca kaldığı yerden devam etmek yerine **canlı yayına yeniden bağlanılıyor**
  (ekranda "✅ Reklam kapandı · yayına dönülüyor").
- Radyoda aynı davranış zaten vardı (reklam kapanınca kendiliğinden devam).

## Doğrulama listesi
- `docs/DOGRULANACAKLAR.md` eklendi: cihazda/hesapta doğrulanmayı bekleyen **12 cihaz testi**,
  **8 hesap/Play işi** ve kodda bitip üründe yarım kalan **5 iş** tek listede.

## Notlar
- İmza aynı; üzerine kurulabilir. Yeni izin yok.
