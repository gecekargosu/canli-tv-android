# UMAY Multi TV — v9.21 (versionCode 30)

23 Kasım 2026 · `CanliTV-9.21.apk` · `CanliTV-9.21.aab`

## P0-4 — Üyelik sistemi tamamen kaldırıldı (senin onayınla)

Claude'un tespit ettiği açıklar (cihaza bağlı hash, `/api/verify` her kodla geçiyor, hesap ele geçirme,
HTML enjeksiyonu) artık **kodda yok**, çünkü sistem kaldırıldı:

| Kaldırılan | Ne oldu |
|---|---|
`Account.java`, `AccountActivity.java`, `Api.java` | **silindi** (27 Java dosyası kaldı) |
`AndroidManifest` → `.AccountActivity` | kayıt silindi |
Ayarlar → **Hesap** bölümü + "Hesap" satırı | kaldırıldı |
Reklam diyaloğundaki **"Üyelik"** düğmesi | kaldırıldı, metin "Üyelik gerekmez" oldu |
Worker `/api/register|verify|resend|login|google/*` | artık **410 Gone** döndürüyor (uçlar kapalı) |

**Kalanlar aynen çalışıyor:** kanallar, radyo, favoriler, kayıt, uyku zamanlayıcı, reklamlar, 20 dk reklamsız süresi.
Favoriler/ayarlar zaten cihazda tutuluyordu; hiçbir şey kaybolmadı.

### Neden doğru karar
- Play'in "hesap açan uygulamalarda uygulama içi hesap silme" zorunluluğu **ortadan kalktı**.
- Data Safety formu ve gizlilik politikası sadeleşti (kişisel veri toplanmıyor).
- Resend/e-posta maliyeti ve spam riski bitti.
- İleride istersek: Firebase Auth / Credential Manager ile Google girişi + **aylık abonelik** (Play Billing) eklenebilir — altyapı buna açık.

## Cihazda doğrulama
| Test | Sonuç |
|---|---|
| Açılış | ✅ çökme yok, hesap sorulmuyor |
| Ana liste | ✅ 157 kanal, ATV 1. sırada |
| Ayarlar | ✅ **Hesap bölümü yok** (Kayıt/Radyo/Hakkında duruyor) |
| Banner | ✅ reklam gelmeye devam ediyor |
