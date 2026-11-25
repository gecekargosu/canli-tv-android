# Veri güvenliği (Data safety) formu — birebir cevaplar

Play Console → Politika → Uygulama içeriği → Veri güvenliği

### Veri güvenliği tablosu (gerçek davranış)

| Veri türü | Toplanıyor mu? | Paylaşılıyor mu? | Amaç | Not |
|---|---|---|---|---|
| Reklam kimliği (AD_ID) | Evet (Google AdMob SDK) | Evet (Google) | Reklam/ölçüm | Uygulama kendi sunucusuna göndermez |
| Cihaz veya diğer kimlikler | Evet (isteğe bağlı) | Hayır | Uygulama işlevi | Yalnız "günlük gönderimi" açılırsa; rastgele Android ID (16 karakter) |
| Yaklaşık konum | Hayır | — | — | Ülke yalnız SIM/ağ kodu + dil, sunucuya gitmez |
| Hassas konum | Hayır | — | — | — |
| Kişisel bilgiler (ad/e-posta) | Hayır | — | — | Hesap yok |
| Uygulama etkinliği (izleme geçmişi) | Hayır (yalnız cihazda) | — | — | Favoriler/geçmiş cihazda |
| Uygulama içi aramalar | Hayır | — | — | — |
| Çökme günlükleri | Evet (isteğe bağlı) | Hayır | Hata ayıklama | Varsayılan KAPALI; kullanıcı açarsa günde en fazla 1 kez |
| Finansal bilgi | Hayır | — | — | — |
| Sağlık | Hayır | — | — | — |
| Mesajlar | Hayır | — | — | — |
| Dosyalar ve belgeler | Hayır | — | — | — |

Ayrıca işaretle: **"Veriler aktarım sırasında şifrelenir" → Evet**; **"Kullanıcı verilerinin silinmesini
talep edebilir" → Evet** (e-posta ile; hesap olmadığı için cihaz verisi kullanıcıda).

Ayrıca: **Veriler aktarım sırasında şifrelenir → Evet**
**Kullanıcı silme talebi → Evet** (cengizkilic1980@gmail.com)
