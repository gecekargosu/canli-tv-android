# Kurulum notları — MIUI (Xiaomi) telefonda adb

## Yaşanan sorun
- `adb install` / `pm install` sürekli `INSTALL_FAILED_USER_RESTRICTED: Install canceled by user` veriyordu.
- Farklı APK'lar (v9.32 dahil), install-create/write/commit, farklı intent'ler denendi; hepsi aynı hatayı verdi.
- **Kök neden:** MIUI, bir paketin **ilk kurulumunu** adb üzerinden kabul etmiyor; kullanıcının
  telefonun kendi paket yükleyicisiyle bir kez onaylaması gerekiyor. Paket yüklü olduktan sonra
  **güncellemeler adb ile çalışıyor** (doğrulandı: `adb install -r` → `Success`).
- "USB ile yükleme" ve hata ayıklama açık olsa da MIUI ilk kurulumda kendi güvenlik penceresini
  açıp adb isteğini sessizce iptal ediyor.

## Çözüm / iş akışı
1. Yeni paket adı veya kaldırılmış uygulama varsa: APK'yı telefona kopyala
   (`adb push CanliTV.apk /sdcard/Download/`), Dosyalar → Download → APK'ya dokun → Kur.
2. Paket yüklüyken sonraki sürümler: `adb install -r -d CanliTV.apk` (test edildi, çalışıyor).
3. Kurulum reddedilirse Dosyalar uygulaması değiştirilerek denenebilir; telefonda 3 dosya
   yöneticisi var (MIUI Dosyalar, Google Files, üçüncü parti). APK `Download` klasöründe.

## İmza
- v9.33'ten itibaren `publicApk` **üretim (upload) anahtarıyla** imzalı: `CN=UMAY Multi TV`.
- v9.32 ve öncesi debug anahtarıyla imzalıydı; o kurulumdan geçiş için **bir kez kaldırıp kurmak** gerekir.
