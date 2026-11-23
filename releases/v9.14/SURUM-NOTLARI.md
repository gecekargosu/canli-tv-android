# UMAY Multi TV — v9.14 (versionCode 23)

23 Kasım 2026 · `CanliTV-9.14.apk` · `CanliTV-9.14.aab`

## Düzeltmeler

### 📻 Açılmayan radyo istasyonları listeden kalkıyor
- TV'de olan kural radyoda da geçerli: bir istasyon **2 kez** açılmazsa listeden gizleniyor
  (Ayarlar → "Ölü gizle" açıksa, varsayılan açık).
- v9.8'de eklenen "9 saniyede açılmazsa sıradakine geç" davranışıyla birlikte:
  ölü istasyon ne listede kalıyor ne de ekranda asılı bırakıyor.
- Kaynak: `RadioRepo.readStream` içinde `Prefs.fails(url) >= 2` filtresi.

### 🔎 TR listesi kontrolü (iş listesi #60)
- `TRT Çocuk` ve `TRT Müzik` **zaten listede** çıktı (ek yapılmadı).
- `Show TV`, `Now TV`, `TV100`, `Teve2` iptv-org Türkiye listesinde **yok** — uydurma link
  koymamak için eklenmedi (kaynak bulunursa eklenecek).
- Doğrulama denemesi: Beyaz TV / Kanal 7 / TVNET linkleri ölü çıktı; TRT Çocuk + TRT Müzik çalışıyor.

## Test
| Test | Sonuç |
|---|---|
Derleme (APK + AAB) | ✅ |
Kurulum (versionCode 23) | ✅ |
