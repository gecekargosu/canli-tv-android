# UMAY Multi TV 10.10 (119) — Emülatör Testi ve Düzeltmeler

Tarih: 27–28 Kasım 2026 · Cihaz: emülatör `Medium_Phone_API_37.0` (Android 17 / SDK 37, 1080×2400, 420 dpi)
Telefon ve tablet bağlı değildi; bu rapor **yalnız emülatör** üzerinde yapılan testi anlatır.

## 1. Otomatik kontroller (hepsi geçti)

| Kontrol | Komut | Sonuç |
|---|---|---|
| Akış teşhisi | `java -cp _tmp/stdtest com.cengiz.canlitv.StreamDiagTest` | `test: 26 · hata: 0 · SONUC: TEMIZ` |
| Kalite kontrolü | `bash tools/check-all.sh --skip-build` | `SONUC: TEMIZ (10.10)` · 429 dosya · 7866 sınıf · imza `f3996c67…b8d5` |
| i18n tablosu | `python _tmp/i18n-denetim.py` | 3 uyarı: `ad_thanks` / `ad_free_left` / `ad_watch` — **yanlış alarm**, kodda `%d` var (tabloda anahtar Türkçe cümle) |

## 2. Elle (emülatör) test edilen akışlar

Test yöntemi: `adb` + `uiautomator` dökümü (metin bazlı dokunma), `logcat -s UmayEvent`,
ekran görüntüleri `_tmp/shots/`. Yardımcı: `_tmp/ui.py`.

- **İlk açılış / ülke bulma**: cihaz yereli ABD → dil `en`, ülke `us`, 271 kanal. Sonra Ayarlar → Ülke → Türkiye: 127 kanal, ATV/TV8/Show/Kanal D başta (pin çalışıyor).
- **Sekmeler**: Tümü / Favori / Son / Gizli — TR'de boş sekme mesajı doğru.
- **Arama**: `trt` → 1 sonuç, `news` → gruplar dahil eşleşme, sonuçsuz arama → "Bu filtrede kanal yok" (TR).
- **Numara + Git**: №5 → onay penceresi → açılış. **Bulgu:** klavye kapanmıyordu.
- **Oynatıcı**: açılış 5–9,5 sn, `ilk kare doğrulandı: 10 sn · 720p`; kalite 360p→720p geçişi ve "≈1.2 GB/h" uyarısı; kanal değiştirme; favori; uyku zamanlayıcısı (15 dk, sayaç saatte görünüyor); PiP düğmesi; kilit; sadece ses (arka plan servisi `[radio] hazır`); ev düğmesi.
- **Reklam**: banner + geçiş reklamı emülatörde doldu (test reklamı, ✕ ile kapandı). `[ad] banner yuklenemedi: Ad size will not fit on screen. a_w=411, a_h=64, s_w=228, s_h=128` → Ayarlar ekranındaki banner yuvası 228×128 dp olduğu için 411×64 isteği reddediliyor (o ekranda banner çıkmıyor).
- **Çoklu ekran**: 3 pencere aynı anda TR kanallarıyla oynadı, veri şeridi güncellendi, çökme yok.
- **Radyo**: TR'de 32 istasyon; ABD'de **"0 stations"** ve tamamen boş liste (aşağıdaki düzeltme).
- **Kayıtlarım**: boş durum metni doğru.
- **Ayarlar**: tüm bölümler (abonelik, veri, dil 8 seçenek, kalite, ekonomi, limit, liste, kayıt, radyo, hakkında, telif, hata günlüğü, puanla, paylaş).
- **Hata günlüğü**: cihazda tutulan olaylar okunuyor, "Gönder" ve "Temizle" var, günlük gönderim kapalı.
- **Dil**: EN → TR geçişinde tüm ana ekran/ayarlar çevrildi.
- **Tablet düzeni**: 1200×1920 @ 240 dpi (sw800dp) → başlık 560dp'de ortalanıyor, liste tam genişlik, sekme kutuları taşmıyor.
- **Uçak modu**: oynatıcıda "İnternet bağlantısı yok · dokun → tekrar dene", **bildirim düğmesi görünmüyor** (doğru davranış).
- **Çökme/ANR**: test boyunca `FATAL EXCEPTION` / `ANR in com.cengiz.canlitv` **yok**.

## 3. Bulunan hatalar ve yapılan düzeltmeler

### H-1 (yüksek) Ölü kanalda sonsuz siyah ekran
`30A Lionel Nation` açıldığında 4 dakika boyunca siyah ekran + "Stream is opening slowly"
kaldı; hata ekranı ve **"📣 Bu kanal çalışmıyor · bildir"** düğmesi hiç gelmedi
(`UmayEvent` günlüğünde ne `hazır` ne `ölü` kaydı vardı). Kullanıcı kanalın bozuk olduğunu
öğrenemiyor, bildiremiyor; oynatıcı 13 sn'de bir yeniden bağlanıp veri harcıyor.
**Düzeltme** (`PlayerActivity`): `checkFirstFrameDeadline()` — kanal açılıştan sonra
**60 saniye** içinde `READY` olmazsa `markDead("İlk kare 60 sn içinde gelmedi")` çağrılır;
hata ekranı + bildirim düğmesi çıkar ve olay günlüğe yazılır. Her kanal değişiminde sıfırlanır.
Ölçüt `everPlayed` değil `ready` yapıldı: ilk sürümde kanal bir kez "hazır" görünüp kare
gelmediğinde kilit yine kapanmıyordu (emülatörde aynı kanal 3 dakika "Yayın durakladı ·
yeniden bağlanıyorum (1/2)" döngüsünde kaldı). Ek olarak reklam nedeniyle duraklatma
**180 saniyeden uzun** sürerse oynatıcı kendini toparlar (reklam kapanışı kaçarsa siyah ekran).

**Kısmen doğrulandı (dürüst durum):** eski mekanizma cihaz günlüğünde çalışıyor —
`11-27 23:02:02 [tv] ölü: 30A Lionel Nation · Zaman aşımı` + `[diag] teşhis`. Yeni 60 sn'lik
ikinci kilit, tam ekran test reklamı ile kanal açılışı iç içe girdiği koşulda emülatörde
60–120 s içinde tetiklenmedi; kod derleniyor ve kapasite denetimi temiz, ama **bu yolun
tetiklenmesi gerçek cihazda tekrar ölçülmeli**. Aynı kanalın bir kez "hazır" göründükten
sonra kare üretmediği durum (23:52 gözlemi) için `ready` ölçütüne geçildi.

### H-2 (orta) Boş sekmede/aramada yanlış "ülke listesi hazır değil" ekranı
Türkiye dışı bir ülkede Favoriler/Son/Gizli sekmesi ya da sonuçsuz arama boşken,
"🇺🇸 … listesi henüz hazır değil · Dokun → tekrar indirmeyi dene" yazıyordu — oysa liste
271 kanalla doluydu ve dokunma gereksiz yere yeniden indirme başlatıyordu.
**Düzeltme** (`MainActivity.refresh`): koşul `shown` yerine **filtresiz listenin** boş olması
(`all.isEmpty()`) oldu.

### H-3 (orta) Oynatıcı üst barında 3 düğme ekran dışında
9 düğme × 68 dp = 666 dp; 411 dp'lik telefonda "Sadece ses", "PiP", "Kilit" görünmüyor,
kaydırılabilir olduğu da anlaşılmıyordu.
**Düzeltme**: düğme 56 dp, kenar 2 dp, etiket 9 sp → 7 düğme görünür, kalanı kaydırmayla.

### H-4 (düşük) İngilizce arayüzde Türkçe ülke adı / erişilebilirlik etiketi
Boş liste uyarısı "Amerika Birleşik Devletleri" diyordu; üst çubuktaki 📻 ve ⚙ düğmelerinin
ekran okuyucu etiketleri sabit "Radyo"/"Ayarlar" idi.
**Düzeltme**: `Countries.name(cc, uiLang != tr)` ve `I18n.t(a, "Radyo"/"Ayarlar")`.

### H-5 (düşük) Radyo listesi boşsa hiçbir açıklama yok
Türkiye dışı ülkede radyo indirilemeyince ekran "0 stations" ile boş kalıyordu.
**Düzeltme** (`RadioActivity`): `ListView.setEmptyView(...)` ile 8 dilde açıklama
("Radyo bulunamadı · aramanı temizle ya da Ayarlar → Ülke"), yeni i18n satırı eklendi.
Not: aynı ülke için ikinci denemede liste **37 istasyon** olarak indi — yani ilk seferdeki
boşlık geçici bir indirme hatasıydı; mesaj tam bu durum için.

### H-6 (düşük) "Git"e basınca klavye kapanmıyor
Onay penceresi klavyenin üstüne sıkışıyordu.
**Düzeltme**: `gotoNumber()` başında `hideIme()`.

## 4. Düzeltilmeyen / sonraki tura kalanlar

- **Ayarlar banner'ı hiç dolmuyor**: `a_w=411, a_h=64, s_w=228, s_h=128` — o ekrandaki banner
  yuvası 64 dp yüksekliğe izin vermiyor. Reklam birimini/yuvayı yeniden boyutlamak gerekiyor
  (gelir kaybı). Bu turda dokunulmadı: reklam yerleşimi ayrı bir iş.
- **Kapalı test incelemesi**: 10.10 (119) Play'de inceleniyor. Bu yüzden **sürüm numarası
  artırılmadı** (10.10/119 kaldı); düzeltmeler kaynakta duruyor, bir sonraki sürümde yayınlanır.
- Emülatörde doğrulanamayanlar: gerçek cihazda donanım çözücü davranışı, MIUI bildirimleri,
  mobil veri uyarısı, AdMob gerçek doluluk.
