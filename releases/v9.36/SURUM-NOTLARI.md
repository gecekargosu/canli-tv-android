# UMAY Multi TV 9.36

**Tarih:** 24 Kasım 2026 · versionCode 45

## Zayıf internet için dayanıklılık
Kullanıcı geri bildirimi: "kanal/radyo açılıyor ama wifi zayıf, kesiliyor". Bu sürüm kopmalarda
hemen pes etmiyor:

- **TV kanalı (PlayerActivity):** hata alınca kanalı bırakmadan önce **aynı yayına 2 kez** yeniden
  bağlanıyor (1,5 sn ve 3,5 sn aralıkla, ekranda "📡 Bağlantı zayıf · yeniden bağlanıyorum (n/2)").
  Ancak ikisi de olmazsa yedek kaynak → sonraki kanal sırası işliyor.
- **Radyo (AudioService):** kesilme/takılmada aynı istasyona **3 denemeye kadar** dönüyor
  (2 sn, 5 sn aralık), bildirimde "Bağlantı zayıf · yeniden bağlanılıyor (n/2)" yazıyor.
- **https yedeği:** şifresiz `http://` yayın açılmazsa aynı adresin `https://` sürümü otomatik
  deneniyor (bazı ağlar 80 portunu engelliyor). `Joy FM Istanbul` adresi doğrudan https'e alındı.

## Notlar
- "Reklam izle · 15 dk reklamsız" ve sayaç davranışı değişmedi.
- Yeni izin yok; imza aynı (`CN=UMAY Multi TV`), üzerine kurulabilir.
