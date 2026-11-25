package com.cengiz.canlitv;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.security.MessageDigest;

/**
 * 9.92: yeniden paketlenmiş (kopyalanmış) uygulama koruması.
 *
 * Nasıl çalışır?
 *  - Play Store'dan kurulan kopya **her zaman** resmi sayılır: Google Play, APK'yı
 *    kendi uygulama imzalama anahtarıyla yeniden imzalar; bizim yükleme sertifikamızla
 *    karşılaştırmak Play kullanıcılarını yanlışlıkla bloklar. Bu yüzden kurulum
 *    kaynağı `com.android.vending` ise kontrol atlanır.
 *  - Play dışından (APK olarak) kurulan kopya, **bizim yükleme sertifikamızla**
 *    imzalı olmak zorunda. Birisi APK'yı açıp logo/ad/URL değiştirip yeniden
 *    imzalarsa sertifika değişir ve uygulama açılmaz.
 *
 * Sınırı: Kendi anahtarıyla imzalayıp bu kontrolü yamalayan biri tamamen
 * engellenemez; asıl caydırıcılık Play App Signing + Play Integrity API +
 * marka/telif bildirimidir (docs/play/KURULUM-SERVISLER.md).
 */
public final class Guard {

    /** Yükleme (upload) sertifikamızın SHA-256 özeti. */
    private static final String OUR_CERT =
            "f3996c67ef0e0e0420f442fe3fcf6303a17caa88dcba8a2b19a10cd95c92b8d5";

    private static Boolean cached = null;

    private Guard() { }

    /** Uygulama resmi kopya mı? */
    public static synchronized boolean official(Context c) {
        if (cached != null) return cached;
        cached = check(c);
        if (!cached) {
            try {
                CrashLog.event(c, "guard",
                        "resmi olmayan imza · kurulum kaynağı: " + installer(c));
            } catch (Throwable ignored) { }
        }
        return cached;
    }

    private static boolean check(Context c) {
        try {
            // Play'den gelen kurulum: Google imzalı, güvenilir.
            if ("com.android.vending".equals(installer(c))) return true;

            String mine = certSha256(c);
            if (mine == null) return false;
            return OUR_CERT.equalsIgnoreCase(mine);
        } catch (Throwable t) {
            return true;   // bilinmeyen durumda kullanıcıyı engelleme
        }
    }

    private static String installer(Context c) {
        try {
            PackageManager pm = c.getPackageManager();
            if (Build.VERSION.SDK_INT >= 30) {
                android.content.pm.InstallSourceInfo i =
                        pm.getInstallSourceInfo(c.getPackageName());
                return i == null ? null : i.getInstallingPackageName();
            }
            return pm.getInstallerPackageName(c.getPackageName());
        } catch (Throwable t) {
            return null;
        }
    }

    private static String certSha256(Context c) {
        try {
            PackageManager pm = c.getPackageManager();
            PackageInfo pi;
            Signature[] sigs;
            if (Build.VERSION.SDK_INT >= 28) {
                pi = pm.getPackageInfo(c.getPackageName(),
                        PackageManager.GET_SIGNING_CERTIFICATES);
                if (pi.signingInfo == null) return null;
                sigs = pi.signingInfo.hasMultipleSigners()
                        ? pi.signingInfo.getApkContentsSigners()
                        : pi.signingInfo.getSigningCertificateHistory();
            } else {
                pi = pm.getPackageInfo(c.getPackageName(), PackageManager.GET_SIGNATURES);
                sigs = pi.signatures;
            }
            if (sigs == null || sigs.length == 0) return null;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(sigs[0].toByteArray());
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Throwable t) {
            return null;
        }
    }
}
