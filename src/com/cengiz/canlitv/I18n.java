package com.cengiz.canlitv;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * v10.04 (Paket C-3): 8 dilli arayüz metinleri.
 *
 * Kaynak: assets/i18n.tsv — sütunlar:
 * Türkçe § English § Deutsch § Français § Español § Русский § العربية § Português
 *
 * Anahtar TÜRKÇE metindir. Böylece ekranlardaki mevcut çağrı yerleri
 * (ornek: de("Reklamsız abonelik")) hiç değişmeden 8 dilde çalışır.
 * Tabloda olmayan metin çağıran tarafta aynen kalır (Türkçe).
 */
public final class I18n {

    private static final String[] COLS = {"tr", "en", "de", "fr", "es", "ru", "ar", "pt"};
    private static final int COL_TR = 0;
    private static final int COL_EN = 1;

    private static HashMap<String, String[]> MAP;

    private I18n() { }

    /** Dosyayı bir kez okur. Paket 200-300 KB değil; ~40 KB, ilk ekranda birkaç ms. */
    public static synchronized void ensure(Context c) {
        if (MAP != null || c == null) return;
        HashMap<String, String[]> m = new HashMap<String, String[]>(512);
        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(c.getAssets().open("i18n.tsv"), "UTF-8"));
            String line;
            while ((line = r.readLine()) != null) {
                if (line.length() == 0 || line.charAt(0) == '#') continue;
                String[] p = line.split("§", -1);
                if (p.length < 8) continue;          // bozuk satır: sessizce atla
                if (p[0].length() == 0) continue;
                String[] v = new String[8];
                for (int i = 0; i < 8; i++) v[i] = unesc(p[i]);
                m.put(v[0], v);          // anahtar da kaçışı çözülmüş olmalı
            }
        } catch (Throwable t) {
            android.util.Log.w("UMAY", "i18n okunamadi: " + t);
        } finally {
            try { if (r != null) r.close(); } catch (Exception ignored) { }
        }
        MAP = m;
    }

    /**
     * Tablodaki satır sonu kaçışı: "\n" (tek veya çift ters bölü ile yazılmış olsa da)
     * gerçek satır sonuna çevrilir; diğer ters bölüler aynen kalır.
     */
    private static String unesc(String s) {
        if (s == null || s.indexOf('\\') < 0) return s;
        StringBuilder b = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char ch = s.charAt(i);
            if (ch != '\\') { b.append(ch); i++; continue; }
            int j = i;
            while (j < s.length() && s.charAt(j) == '\\') j++;
            if (j < s.length() && s.charAt(j) == 'n') {
                b.append('\n');
                i = j + 1;
            } else {
                for (int k = i; k < j; k++) b.append('\\');
                i = j;
            }
        }
        return b.toString();
    }

    /** Dil kodunun sütun indeksi; bilinmeyen dilde -1. */
    public static int index(String lang) {
        for (int i = 0; i < COLS.length; i++) if (COLS[i].equals(lang)) return i;
        return -1;
    }

    /** Tablodaki karşılık; metin tabloda yoksa null. */
    public static String s(Context c, String tr) {
        if (tr == null || tr.length() == 0) return null;
        ensure(c);
        if (MAP == null) return null;
        String[] v = MAP.get(tr);
        if (v == null) return null;
        int i = index(Prefs.uiLang(c));
        String out = (i >= 0) ? v[i] : null;
        if (out == null || out.length() == 0) {
            if (i == COL_TR) return tr;
            out = v[COL_EN];                      // eksik çeviri → İngilizce
        }
        if (out == null || out.length() == 0) return null;
        return out;
    }

    /** Kısayol: tabloda yoksa Türkçe metni döndürür. */
    public static String t(Context c, String tr) {
        String v = s(c, tr);
        return (v == null || v.length() == 0) ? tr : v;
    }
}
