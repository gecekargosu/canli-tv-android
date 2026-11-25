# Play uygunluk denetimi — 25 Kasım 2026 (9.73)

Bu dosya, Play'e yükleme öncesi **program tarafında** doğrulanan maddeleri ve **yalnızca Play
Console'da yapılabilecek** işleri ayırır. Kod/veri tarafında doğrulanmayan hiçbir madde ✅ sayılmadı.

## 1. Teknik uygunluk (doğrulandı)

| Madde | Durum | Kanıt |
|---|---|---|
| Paket adı | ✅ | `com.cengiz.canlitv` |
| versionCode / versionName | ✅ | 82 / 9.73 |
| minSdk / targetSdk | ✅ | 26 / 36 (Play'in 2026 zorunluluğu karşılanıyor) |
| İmza | ✅ | `verify-apk` upload anahtarı `f3996c67…b8d5` |
| `debuggable` | ✅ | publicApk/release derlemesinde false |
| `allowBackup` | ✅ | false |
| Cleartext trafiği | ✅ | `usesCleartextTraffic=false` + `network_security_config` |
| Gereksiz izinler | ✅ | Rehber/kamera/mikrofon/konum yok; depolama yalnız Android ≤9 |
| `QUERY_ALL_PACKAGES` | ✅ | Yok |
| Reklam kimliği beyanı | ✅ | Yalnız AdMob SDK kullanır; uygulama kendi sunucusuna göndermez |
| UMP / AB rızası | ✅ | Rıza yoksa reklam isteği gönderilmiyor (fail-closed) |
| Test reklamları | ✅ | Release'te gerçek birimler; test modu yalnız gizli geliştirici modunda |
| Reklam sıklığı | ✅ | 3. kanal değişimi + 180 sn ara + 15 dk; ilk açılışta reklam yok |
| Ödüllü reklam | ✅ | Ödül yalnız `onUserEarnedReward` ile; erken kapatmada ödül yok |
| Kayıt (telif) | ✅ | Yalnız TRT/TBMM/Diyanet |
| Kanal listesi | ✅ | 126 kanal; lisanslı yabancılar ve alakasız kanallar çıkarıldı |
| TV8 tokenlı adres | ✅ | Kaldırıldı; parametresiz TürkNet yansısı |
| Gizlilik politikası | ✅ | `/gizlilik-politikasi` — reklam, hata günlüğü, veri saklama yazılı |
| app-ads.txt | ✅ | `pub-7629051535337276` yayında |
| Hata günlüğü | ✅ | Cihazda; otomatik gönderim **varsayılan kapalı**, kullanıcı açar |
| Reklam geliri raporlaması | ✅ | SDK gösterimi kendisi raporlar; ayrı istemci kodu gerekmez |

## 2. Mağaza metni ile uygulama davranışı tutarlılığı

| Metinde yazan | Uygulamada | Durum |
|---|---|---|
| "Hesap gerekmez" | Hesap/üyelik kodu yok | ✅ |
| "Banner + seyrek geçiş reklamı" | 3. zap / 180 sn / 15 dk | ✅ |
| "Ödüllü reklamla 15 dk reklamsız" | `REWARD_MINUTES = 15`, banner kalır | ✅ |
| "Kayıt yalnız kamu kanallarında" | `canRecord()` | ✅ |
| "Hata günlüğü kullanıcıda, isteğe bağlı gönderim" | Ayarlar → Hata günlüğü | ✅ |

`README.md`, `docs/play/magaza-kaydi.md` ve `AboutActivity` metinleri bu sürümde güncellendi
(eski "reklamsız", "hesap aç", "302 kanal" ifadeleri kaldırıldı).

## 3. Play Console'da yapılacaklar (yalnızca sende)

1. **AAB yükle**: `releases/v9.73/UMAY-MULTI-TV-9.73.aab` → Kapalı test.
2. **Data Safety**: aşağıdaki tabloyu birebir gir.
3. **Reklamlar**: "Uygulama reklam içeriyor" → Evet.
4. **İçerik derecelendirmesi**: 13+ / şiddet-cinsellik-kumar yok; kullanıcı etkileşimi yok.
5. **Hedef kitle**: 13 yaş ve üzeri (çocuklara yönelik değil).
6. **Haber/kanal beyanı**: Uygulama yayın barındırmıyor, yalnızca halka açık akışları oynatıyor;
   telif iletişim adresi mağaza metninde.
7. **Ekran görüntüleri**: reklamsız/az reklamlı set (banner görünürse "reklam var" beyanıyla tutarlı).
8. **12 tester / 14 gün** kapalı test şartını Console'dan doğrula.
9. Play Console → AdMob bağlantısı: uygulama Play'e çıktıktan sonra AdMob'daki "inceleme gerekli /
   sınırlı reklam sunumu" uyarısının kalkması için mağaza bağlantısını ekle.

### Data Safety cevap tablosu

| Veri türü | Toplanıyor | Paylaşılıyor | Amaç | Not |
|---|---|---|---|---|
| Reklam kimliği (AD_ID) | Evet (AdMob SDK) | Evet (Google) | Reklam/ölçüm | Uygulama kendi sunucusuna göndermez |
| Cihaz veya diğer kimlikler | Evet (isteğe bağlı) | Hayır | Uygulama işlevi / hata ayıklama | Yalnız kullanıcı "günlük gönderimi"ni açarsa; rastgele Android ID (16 karakter) |
| Çökme günlükleri | Evet (isteğe bağlı) | Hayır | Hata ayıklama | Varsayılan kapalı; günde en fazla 1 gönderim |
| Uygulama etkinliği (izleme geçmişi) | Hayır | — | — | Cihazda |
| Kişisel bilgiler (ad/e-posta) | Hayır | — | — | Hesap yok |
| Konum | Hayır | — | — | — |
| Dosyalar | Hayır | — | — | Kayıtlar cihazda |

Ayrıca: **"Veriler aktarım sırasında şifrelenir" → Evet**;
**"Kullanıcı verilerinin silinmesini talep edebilir" → Evet** (e-posta; ayrıca uygulamada
Ayarlar → Hata günlüğü → Temizle).

## 4. Bilinen açık noktalar

- AdMob tarafında **NO_FILL** görülebilir: hesap/uygulama Play'e bağlanana kadar Google sınırlı
  reklam sunuyor. Bu bir kod hatası değildir; uygulama hatasız çalışmaya devam eder.
- Reklam gelirinin kesin tutarı yalnız AdMob konsolunda görünür (1–2 gün gecikme).
- 12 tester / 14 gün ve mağaza görselleri kullanıcı tarafındadır.
