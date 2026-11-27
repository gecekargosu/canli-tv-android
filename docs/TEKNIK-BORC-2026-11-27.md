# Teknik Borç Turu — v10.08 (117)

Tarih: 27 Kasım 2026 · Kapsam: MainActivity bölme, D-pad (kumanda) odak desteği.

## 1. MainActivity bölündü

| Dosya | Satır | Ne yapar |
|---|---|---|
| `MainActivity.java` | 1460 → **1240** | ekran akışı, listeleme, filtre, düzenleme mantığı |
| `MainUi.java` (yeni) | 127 | renkler, `dp`, `rounded`, `ring`, `pill`, `mini`, `arrow`, `slot`, `tabBox` |
| `MainHeader.java` (yeni) | 189 | üst çubuk: marka kutusu + 6 ikon kutusu (marka, radyo, ülke, kayıt, ayarlar, ödüllü reklam) |
| `ChannelRow.java` (yeni) | 118 | tek kanal satırının görünümü (numara, ad, kategori/meta, ▲▼ veya ▶) |
| `TvFocus.java` (yeni) | 62 | kumanda/D-pad odak çerçevesi |

Yöntem: davranış değiştirmeden taşıma. Üst çubuk ve satır yalnızca görünüm üretir; tıklama
işleri `MainHeader.Host` / `ChannelRow.Host` arayüzleriyle `MainActivity`'ye döner
(`headerMulti`, `headerGift`, `moveChannel` …). Renk sabitleri `MainUi.BG` gibi tek
kaynağa bağlandı; `MainActivity` içindeki eski adlar (`CARD`, `ACCENT`…) korundu,
bu yüzden kalan kod hiç değişmedi.

`dp()`, `rounded()`, `pill()` gibi eski çağrılar da duruyor ama artık `ui.*`'a delege
eden tek satır. Böylece 220 satırlık görünüm kodu tek yerden yönetiliyor.

RecyclerView: **zaten vardı** (K-16 turunda `ListView` → `RecyclerView` + `ChannelAdapter`
geçişi yapılmıştı). Bu turda yeniden yazılmadı; yalnızca satır görünümü ayrı sınıfa taşındı.

## 2. D-pad / kumanda desteği

Öncesi: uygulamada hiç odak (focus) kodu yoktu. Harici kumanda veya klavyeyle gezinince
hangi kutunun seçili olduğu görünmüyordu.

Eklenenler:

1. **Odak çerçevesi** (`TvFocus.attach`): odaklanan kutuda turuncu 2dp kenar + açık zemin +
   %2 büyüme; odak çıkınca eski zemin. Üst çubuktaki 6 kutu ve kanal satırlarının hepsinde.
2. **Rakamla kanal açma** (`onKeyDown`): kumandadan `5` → 1,2 sn içinde `12` yazılırsa 12. kanal.
   Süre dolarsa numara doğrudan açılır (onay diyaloğu yok). Arama kutusu odaklıyken devre dışı.
3. **Açılışta odak**: kumanda/klavye cihazında ilk kanal satırı odaklanır, OK hemen çalışır.
4. Süs öğeleri (numara rozeti, ▶ işareti) odaklanamaz yapıldı; kumanda bunlara takılmıyor.
5. `TvFocus.isTv`: leanback / television özelliği veya dokunmatik yok → TV kabul edilir.
   Dokunmatik telefonda odak halkası görünmez (parmakla gezinirken rahatsız etmesin).

**Manifest'e leanback bildirimi bilerek eklenmedi.** `<uses-feature android.software.leanback>`
ve `LEANBACK_LAUNCHER` eklemek Play dağıtımını ve mağaza listesi gereksinimlerini (TV banner,
ekran görüntüleri) değiştirir; bu senin kararın. D-pad desteği bundan bağımsız çalışır
(harici kumanda, USB klavye, Android TV box). İstersen ayrı bir turda ekleriz.

## 3. Doğrulama

- `bash build.sh` → BUILD SUCCESSFUL, imza `f3996c67…b8d5`.
- `verify-apk.py --prev releases/v10.07 --expect-version 10.08` → ✅ TEMİZ
  (4.465.883 bayt, 429 dosya, 7863 sınıf; 10.07'ye göre +3 sınıf, +1204 bayt).
- Telefon (Z5R87DU8V4LBSOVG): kuruldu, açıldı, **FATAL/ANR yok**, crash buffer boş.
- Ekran dökümü: başlık + 6 ikon + sekmeler + **127 kanal listelendi**, veri şeridi doğru.
- Kumanda testi: `input keyevent 20` (aşağı) ile odak satıra geçti,
  `input keyevent 12` (5) ile 5. kanal açıldı (PlayerActivity), çökme yok.

## 4. Yapılmayanlar / açık kalanlar

- Odak çerçevesi **gözle** doğrulanmadı (ekran görüntüsü alınmadı); çalıştığı ölçümle
  (odaklanan düğüm `focusable=true focused=true`) ve çökme olmamasıyla sınırlı.
- Kanal düzenleme modunda ▲▼ okları da odaklanabilir; kumandayla sıralama akışı denenmedi.
- MainActivity hâlâ 1240 satır: sırada `refresh()`/filtre mantığını `ChannelFilter`'a,
  düzenleme modunu `ChannelEditController`'a taşımak var (davranış aynı kalacak).
- B-14 (tablet kapanması) ve R8 duman testi senaryoları bu turda tekrar denenmedi.

## 5. v10.09'da kapatılanlar (28 Kasım 2026)

| Madde | Durum |
|---|---|
| `StreamDiag`: hata kodlarını tek yerde sınıflandırma + kullanıcıya 8 açıklama | ✅ yapıldı (13 sınıf, 8 dil) |
| "Bu kanal çalışmıyor" bildir düğmesi | ✅ yapıldı (oynatıcı + liste menüsü, çevrimdışı kuyruk) |
| "Gerçek kare geldi mi" ölçümü (`onRenderedFirstFrame` yerine READY + video izi + 10 sn) | ✅ yapıldı |
| `CrashLog.event` → logcat | ✅ yapıldı (`adb logcat -s UmayEvent`) |
| MainActivity'yi `ChannelFilter` / `ChannelEdit` olarak bölme | ✅ v10.10'da yapıldı (1272 → 1128 satır) |

Yeni test: `tools/StreamDiagTest.java` (26 senaryo, `javac`+`java`, Android gerekmez).

## 6. v10.10 (28 Kasım 2026)

- `ChannelFilter`: filtre + sıralama (sekme, arama, gizlenenler, ölü kanal eleme, kayıtlı
  sıra, "🕘 Son"). Arayüzden bağımsız.
- `ChannelEdit`: düzenleme modu (seçim, taşıma, en üste/en alta, silme, sıra kaydı) +
  `Host` arayüzü. `MainActivity` 1272 → **1128 satır**.
- `YEDEK/` altındaki 30 dosyada düz yazı keystore şifresi maskelendi (ana
  `keystore.properties` değişmedi).
