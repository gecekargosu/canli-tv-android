package com.cengiz.canlitv;

import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.security.MessageDigest;

/**
 * AdMob test cihazı desteği.
 *
 * Google, bir cihazı "test cihazı" olarak tanımak için reklam kimliğinin (Advertising ID)
 * MD5 özetini ister. Bu sınıf kimliği okur ve AdMob'un beklediği biçime çevirir.
 *
 * Test cihazı olarak kaydedilen cihazda **gerçek reklam birimleri test reklamı** döndürür:
 * reklam açılır, ödül tetiklenir, ama para yazmaz ve geçersiz trafik riski oluşmaz.
 *
 * Not: getAdvertisingIdInfo() ağ/disk erişimi yapar; ana iş parçacığında çağrılmamalı.
 */
public class AdsTest {

    /** Reklam kimliği (ör. "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"); yoksa null. */
    public static String gaid(Context c) {
        try {
            AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(c);
            if (info == null) return null;
            if (info.isLimitAdTrackingEnabled()) return null;   // kullanıcı kişiselleştirmeyi kapatmış
            return info.getId();
        } catch (Throwable e) {
            return null;
        }
    }

    /** AdMob'un setTestDeviceIds için beklediği MD5 (büyük harf) özet. */
    public static String hashed(Context c) {
        String id = gaid(c);
        return id == null ? null : md5(id.toUpperCase());
    }

    public static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest(s.getBytes("UTF-8"));
            StringBuilder b = new StringBuilder();
            for (byte x : d) b.append(String.format("%02X", x));
            return b.toString();
        } catch (Throwable e) {
            return null;
        }
    }
}
