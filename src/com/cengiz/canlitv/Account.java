package com.cengiz.canlitv;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hesap durumu (istege bagli uyelik).
 *
 * Sifre hicbir yerde duz metin tutulmaz: PBKDF2-HMAC-SHA256 (120.000 tur) ozeti saklanir ve
 * sunucuya da yalnizca bu ozet gonderilir. Uyelik zorunlu degildir; "misafir" olarak devam eden
 * kullanici uygulamanin tamamini kullanabilir.
 */
public class Account {

    private static final int ITER = 120000;
    private static final int BITS = 256;

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("account", Context.MODE_PRIVATE);
    }

    // --- durum ---
    public static boolean signedIn(Context c) {
        return email(c).length() > 0;
    }

    public static String email(Context c) {
        return sp(c).getString("email", "");
    }

    public static String name(Context c) {
        return sp(c).getString("name", "");
    }

    public static boolean verified(Context c) {
        return sp(c).getBoolean("verified", false);
    }

    public static boolean guest(Context c) {
        return sp(c).getBoolean("guest", false);
    }

    /** Kullanici "misafir olarak devam et" dedi; bir daha sorulmaz. */
    public static void setGuest(Context c) {
        sp(c).edit().putBoolean("guest", true).apply();
    }

    public static void setVerified(Context c, boolean v) {
        sp(c).edit().putBoolean("verified", v).apply();
    }

    public static void save(Context c, String email, String name, boolean verified) {
        sp(c).edit()
                .putString("email", email == null ? "" : email.trim().toLowerCase())
                .putString("name", name == null ? "" : name.trim())
                .putBoolean("verified", verified)
                .putBoolean("guest", false)
                .apply();
    }

    public static void signOut(Context c) {
        sp(c).edit().clear().apply();
    }

    // --- yerel sifre ozeti (cevrimdisi giris icin) ---
    private static String localSalt(Context c) {
        return sp(c).getString("salt", "");
    }

    private static String localHash(Context c) {
        return sp(c).getString("hash", "");
    }

    public static void savePassword(Context c, String password) {
        String salt = newSalt();
        sp(c).edit().putString("salt", salt).putString("hash", hash(password, salt)).apply();
    }

    /** Cevrimdisi giris: bu telefonda daha once kayit olunan sifre ile karsilastirir. */
    public static boolean checkPassword(Context c, String password) {
        String salt = localSalt(c);
        String h = localHash(c);
        if (salt.length() == 0 || h.length() == 0) return false;
        return MessageDigest.isEqual(h.getBytes(StandardCharsets.UTF_8),
                hash(password, salt).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean hasPassword(Context c) {
        return localSalt(c).length() > 0 && localHash(c).length() > 0;
    }

    // --- ozet ---
    public static String newSalt() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }

    /** PBKDF2-HMAC-SHA256; sunucuya giden deger budur (duz sifre aga cikmaz). */
    public static String hash(String password, String saltB64) {
        try {
            byte[] salt = Base64.decode(saltB64, Base64.NO_WRAP);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITER, BITS);
            byte[] out = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.encodeToString(out, Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean validEmail(String s) {
        if (s == null) return false;
        String v = s.trim();
        int at = v.indexOf('@');
        int dot = v.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < v.length() - 1 && v.length() <= 120
                && v.indexOf(' ') < 0;
    }
}
