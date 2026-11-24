# UMAY Multi TV v9.34 (versionCode 43) — medya düzeltmeleri

## M-22 — Hızlı kanal değiştirme yarışı
- Her kanal isteğine artan bir numara (`playRequest`) verildi.
- Kullanıcı ATV'den Kanal D'ye hızlı geçerse, ATV'nin geç gelen çözümleme cevabı artık
  yeni kanalı ezmiyor (eski istek sessizce atılıyor).

## M-23 — Tamponlamada kör kalan donma kontrolü
- `checkStall()` önceden `isPlaying()` false ise hiç bakmıyordu; ExoPlayer tamponlarken
  tam donma anı kaçırılıyordu. Artık `STATE_BUFFERING` de izleniyor, 12 sn ilerleme
  olmazsa yeniden bağlanma devreye giriyor.

## M-28 — Günlük liste yenileme damgası
- `Good.refreshIfNeeded` gün damgasını indirme başarılı olmadan yazıyordu; internet bir kez
  kopunca o gün bir daha denenmiyordu. Damga artık yalnız başarılı indirme + doğrulama sonrası.

## Doğrulama
- Derleme başarılı. Cihaz kurulumu MIUI "USB ile yükleme" engeli yüzünden hâlâ yapılamıyor;
  bu maddeler cihazda denenmedi.
