# UMAY Multi TV 9.39

**Tarih:** 24 Kasım 2026 · versionCode 48

## Zayıf internet: kopmada pes etmiyor
Kullanıcı geri bildirimi: "kanal/radyo açılıyor ama wifi zayıf, kesiliyor". Cihazda wifi kapatılıp
açılarak test edildi; radyo kopmadan sonra kendini toparladı.

- **Radyo ekranı:** 9 sn'de "sıradakine geç" kaldırıldı. Artık aynı istasyona **2 kez** yeniden
  bağlanıyor (her biri 12 sn bekler), ancak ondan sonra sıradakine geçiyor. Yavaş ağ yüzünden
  istasyon "ölü" olarak işaretlenmiyor (liste temiz kalıyor).
- **Radyo servisi:** yayın kesilince 2 sn ve 5 sn arayla aynı adrese dönüyor; 3 denemeden sonra
  da **15 sn'de bir** internet geri gelene kadar sessizce yeniden deniyor (12 kez).
- **TV kanalı:** "13 saniyede açılmadı" durumunda kanalı bırakmadan önce aynı yayına 2 kez
  yeniden bağlanıyor; ancak sonra yedek kaynak/sonraki kanal devreye giriyor.
- **Yavaş ilk bağlanma:** radyo 25 sn'ye kadar "Bağlantı yavaş · bekleniyor…" diyerek bekliyor
  (önceden 7 sn'de başarısız sayıyordu).
- **https yedeği:** şifresiz `http://` yayın açılmazsa aynı adresin `https://` sürümü otomatik
  deneniyor. `Joy FM Istanbul` adresi doğrudan https'e alındı.

## Notlar
- Sayaç/limit, reklam rıza kapısı ve imza değişmedi.
- Yeni izin yok; üzerine kurulabilir.
