# GÜNCELLEMELER — sürüm arşivi

Her sürüm için bu klasörde **sürüm numarasıyla** bir alt klasör açılır:

```
GUNCELLEMELER/
  v9.96/
    OZET.md              ← o sürümde ne değişti (okunur özet + dosya listesi)
    CanliTV.apk          ← sürümün telefon APK'sı (git'e girmez, yerelde durur)
    app-release.aab      ← Play'e yüklenecek AAB (git'e girmez, yerelde durur)
    SURUM-NOTLARI.md     ← aynı notların uzun hâli (Play sürüm notu için)
```

## Kural

1. Bir hata düzeltmesi/özellik bitince `build.sh` + `make-aab.sh` çalıştırılır.
2. `releases/vX.YY/` altına APK + AAB + `SURUM-NOTLARI.md` konur (zorunlu, `AGENTS.md`).
3. Aynı ikili dosyalar buraya da kopyalanır ve `OZET.md` yazılır.
4. GitHub'da `vX.YY` release'i açılır (APK + AAB indirilebilir).
5. Play Console'a AAB yüklenir (kapalı test → incelemeye gönder).

## Yayın ritmi

Haftada **1 güncelleme** hedefi: Cuma günü toplanan düzeltmeler tek sürümde çıkar.
Sürüm numarası `app/build.gradle` içindedir: `versionCode` her yayında +1,
`versionName` görünen addır (9.96 → 9.97 …).

> Not: Takvimi ben (asistan) kendiliğimden hatırlatamam; her hafta "güncelleme
> zamanı" dediğinde ya da yeni bir sohbet açtığında bu klasöre bakıp
> "son sürüm v9.96, şu düzeltmeler birikmiş" diye özetlerim.
