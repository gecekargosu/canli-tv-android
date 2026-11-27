# UMAY Multi TV — Tam Hata Analizi Senaryosu (v10.05 · 27 Kasım 2026)

Amaç: "Bir yerde hata/program/eksiklik varsa **kanıtla** bulmak ve düzeltmek".
Bu doküman analizin **nasıl** yapılacağını tanımlar; her fazın çıktısı bir bulgu listesidir.

Kural: **Kanıtsız bulgu yok.** Her bulgu şu 4 şeyi taşır:
`ne oldu` · `nasıl tekrar üretilir (adım adım)` · `kanıt (log/ekran/ölçüm)` · `kök neden + düzeltme`.

Önem sırası: **P0** veri kaybı/çökme/yayın kesintisi · **P1** görünür hata, iş yapamama ·
**P2** yavaşlık/kötü deneyim/çeviri · **P3** kozmetik, teknik borç.

---

## 0. Faz 0 — Envanter ve hazırlık (analiz başlamadan)
1. Sürüm matrisi: `versionName/versionCode`, imza SHA-256, APK/AAB boyutu, dosya/sınıf sayısı (`verify-apk.py`).
2. Cihaz matrisi: en az **3 Android sürümü** (8/minSdk 26 · 12 · 16/target 36), 1 telefon + 1 tablet, 1 düşük RAM cihaz.
3. Log toplama düzeneği: `adb logcat -c` → test → `adb logcat -d > test.log`; `dumpsys dropbox --print` (crash/ANR), `dumpsys meminfo`, `dumpsys batterystats`, `screencap`.
4. Test hesabı/hazırlık: dil listesi (8), favoriler, gizli kanal, kayıt, radyo, ödüllü reklam sayacı sıfırlanmış hâli.
5. Baz çizgi (baseline): soğuk açılış süresi, ilk liste dolma süresi, bellek, pil tüketimi — düzeltme sonrası karşılaştırmak için.

## 1. Faz 1 — Statik analiz (kod okumadan makineyle)
1. `bash gradle-build.sh lintPublicApk` → **lint raporu**: hatalar (error), şüpheli API, izin, hardcoded text, eksik çeviri, kullanılmayan kaynak, `NewApi` ihlalleri.
2. **R8/minify denetimi**: `bundleRelease` sonrası `mapping.txt` var mı? Proguard keep kuralları AdMob/UMP/Billing/Media3 için doğru mu? Release'te `NoSuchMethodException` riski.
3. **Manifest denetimi**: izin listesi (gereksiz izin = Play reddi), `exported` alanları, `largeHeap`, `networkSecurityConfig` cleartext, `allowBackup`, `usesCleartextTraffic`.
4. **i18n kapsam testi (otomatik)**: kaynak koddaki her Türkçe metin `assets/i18n.tsv` içinde var mı, 8 sütun dolu mu, `I18n.t` ile sarılmış mı? (script: `tools/i18n-kapsam.py`).
5. **Bağımlılık/CVE**: AdMob, Billing, Media3 sürümleri güncel mi; bilinen güvenlik açığı var mı.
6. **Kod kokusu taraması**: `catch (Exception ignored)`, ana thread'de ağ/disk, `Thread` sızıntısı, `Handler`/`Runnable` ekran kapandıktan sonra çalışıyor mu, statik `Context` tutan alan, `BroadcastReceiver` kaydı kaldırılmıyor mu.
7. **Varlık denetimi**: `assets/` içinde kullanılmayan büyük dosya, ölü URL'ler (kanal/radyo listesi HTTP durum kodu taraması).

## 2. Faz 2 — Dinamik (cihazda) fonksiyonel matris
Her satır ayrı ayrı denenir; hata varsa log + ekran görüntüsü alınır.

| Alan | Senaryolar |
|---|---|
| Açılış | soğuk açılış, sıcak açılış, uçak modunda açılış, ilk kurulum, güncelleme sonrası açılış |
| Liste | 127 kanal kaydırma, arama (Türkçe karakter), sekme geçişleri, ülke değiştirme, sıralama, gizle/geri al, numara ile açma |
| Oynatma | kanal aç, 20 kanal hızlı geçiş, 30 dk kesintisiz izleme, kalite değiştir, ses/alt yazı, PiP, tam ekran yön değişimi |
| Ağ | Wi-Fi kapat/aç, veri kapat/aç, yavaş ağ (throttle), DNS hatası, sunucu 403/404, yayın ortasında kesinti |
| Çoklu ekran | 3'lü ve 2x2, pencere başına ses, ağ kesintisinde toparlanma, 4 pencere + reklam |
| Kayıt | başlat/durdur, ekran kilidi sırasında, depolama dolu, uzun kayıt, Kayıtlarım'dan oynat/sil |
| Radyo | liste, arama, çalma, kilit ekranı kontrolü, her yerden kapat, arka planda 30 dk |
| Reklam | banner, geçiş reklamı, ödüllü reklam (izle/iptal et), reklamsız süre sayacı, abonelikte reklam çıkmaması |
| Abonelik | satın alma, geri yükleme, iptal sonrası durum, uçak modunda satın alma denemesi |
| Ayarlar | 8 dil, ülke, kalite, veri tasarrufu, veri sayacı/limit, uyku zamanlayıcısı, hata bildir |
| Durum/Hakkında | tüm satırlar okunur, taşma yok, sürüm doğru |
| Sistem | düşük pil/tasarruf modu, büyük yazı tipi, gece modu, saat dilimi değişimi, bildirim izni reddi, geri tuşu davranışı |

## 3. Faz 3 — Hata enjeksiyonu (bozuk dünya testi)
- Kanal listesine **ölü URL** koy, uygulamanın atlama/uyarı davranışına bak.
- İndirmeyi yarıda kes (uçak modu), yarım dosya kalıyor mu, tekrar deneme çalışıyor mu.
- `assets/good_*.json` bozuk JSON → çökme yerine boş liste + mesaj.
- Disk dolu (küçük bölüm) → kayıt/log yazımı hata veriyor mu.
- Tarih/saat ileri-geri al → ödüllü reklam sayacı, günlük limit, uyku zamanlayıcısı.

## 4. Faz 4 — Çökme/ANR/performans ölçümü
1. `logcat` filtreleri: `FATAL EXCEPTION`, `ANR in`, `Force finishing`, `MediaCodec`, `OutOfMemory`, `SecurityException`.
2. `dumpsys dropbox --print | grep -i canlitv` (sistem çökme kayıtları), `tombstone` var mı.
3. Bellek: `dumpsys meminfo com.cengiz.canlitv` — PSS hedefi **< 250 MB** (largeHeap açık, dikkat).
4. Açılış: `adb shell am start -W` → toplam süre hedefi **< 2 sn**.
5. Pil: `dumpsys batterystats` — 30 dk oynatma sonrası uygulama payı.
6. Ağ: `dumpsys netstats` — reklam/logo istekleri gereksiz trafik üretiyor mu.

## 5. Faz 5 — Çeviri ve erişilebilirlik
1. Her ekran 8 dilde açılır (TR, EN, DE, FR, ES, RU, AR, PT); **AR RTL** ayrı kontrol.
2. Metin taşması/kırpılma: Almanca uzun kelimeler, Rusça, ekran okuyucu (TalkBack) etiketleri.
3. `contentDescription` eksik ikonlar, dokunma hedefi < 48dp olan öğeler, kontrast.

## 6. Faz 6 — Yayın/gizlilik uyumu
- İzinler gerçekten gerekli mi (Play reddi riski), Data Safety formu kodla uyumlu mu.
- Cleartext HTTP kaldı mı, `network_security_config` doğru mu.
- Lisanssız içerik taraması (spor/dizi/sinema yok), marka/telif.
- Reklam kimlikleri: test/production ayrımı doğru mu (test reklamı canlıya çıkmasın).

## 7. Faz 7 — Regresyon ve sürüm
- `verify-apk.py --prev` ile boyut/sınıf/imza karşılaştırması.
- Önceki sürümden `install -r` (veri korunuyor mu), sürüm düşürme engeli.
- 10.05'te düzeltilen her bulgu için **tekrar üretim adımı** çalıştırılır (kapandı kanıtı).

## 8. Faz 8 — Raporlama
Her bulgu: `B-01 | P1 | Oynatıcı | Ağ kesilince 3 sn siyah ekran | adımlar | log satırı | kök neden | düzeltme | doğrulama`.
Kapanış: P0/P1 = bu sürümde bitmeli, P2 = planlanır, P3 = teknik borç listesine yazılır.

---

## Otomatikleştirilebilen kısımlar (bu depoda)
- `tools/verify-apk.py` — sürüm/imza/boyut/kapasite.
- `tools/i18n-kapsam.py` — çeviri kapsamı ve sütun bütünlüğü.
- `tools/hata-tarama.sh` — logcat/dropbox toplama + lint raporu özeti.
- `tools/play-upload.py` — Play kanal durumu ve yükleme.
