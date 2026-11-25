# -*- coding: utf-8 -*-
"""P0 maddeleri: MP-02..MP-05, MP-17, MP-18 + surum 9.71."""
import io, re, sys

def rd(p):
    return io.open(p, encoding='utf-8', newline='').read()

def wr(p, s):
    io.open(p, 'w', encoding='utf-8', newline='').write(s)

def rep(path, old, new, count=1):
    s = rd(path)
    if s.count(old) != count:
        print('HATA: %s icinde %d kez bulundu (beklenen %d): %r' % (path, s.count(old), count, old[:70]))
        sys.exit(1)
    wr(path, s.replace(old, new))
    print('ok  %s' % path)

# ---- MP-03: reklam sikligi (Ads) ----
A = 'src/com/cengiz/canlitv/Ads.java'
rep(A,
    '    /** Art arda hızlı kanal değişiminde reklamlar üst üste binmesin (en az süre). */\r\n'
    '    public static final long INTERSTITIAL_GAP_MS = 20L * 1000L;\r\n'
    '    /** Her kanal değişiminde bir geçiş reklamı gösterilir. */\r\n'
    '    public static final int ZAPS_BEFORE_INTERSTITIAL = 1;',
    '    /** Art arda hızlı kanal değişiminde reklamlar üst üste binmesin (en az süre). */\r\n'
    '    public static final long INTERSTITIAL_GAP_MS = 180L * 1000L;\r\n'
    '    /** Kaç kanal değişiminde bir geçiş reklamı gösterilir (3 zap = 1 reklam). */\r\n'
    '    public static final int ZAPS_BEFORE_INTERSTITIAL = 3;')

# ---- MP-03: zap sayaci + MP-18: PG + MP-04: release'te test modu yok ----
M = 'src/com/cengiz/canlitv/AdManager.java'
rep(M,
    '        if (now - lastInterstitial < Ads.INTERSTITIAL_GAP_MS) {\r\n'
    '            then.run();\r\n'
    '            return;\r\n'
    '        }',
    '        if (now - lastInterstitial < Ads.INTERSTITIAL_GAP_MS) {\r\n'
    '            then.run();\r\n'
    '            return;\r\n'
    '        }\r\n'
    '        // PDF-05: her zap\'ta reklam cikmasin; kac zap\'ta bir gosterilecegi Ads sabitinde.\r\n'
    '        if (opens % Ads.ZAPS_BEFORE_INTERSTITIAL != 0) {\r\n'
    '            then.run();\r\n'
    '            return;\r\n'
    '        }')

rep(M,
    '            // Aile dostu reklam: yetiskin/erotik/"afterglow" turu icerikler istenmez.\r\n'
    '            // MAX_AD_CONTENT_RATING_G = yalnizca genel izleyiciye uygun reklamlar.\r\n'
    '            RequestConfiguration.Builder rc = new RequestConfiguration.Builder()\r\n'
    '                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)',
    '            // Aile dostu: yetiskin/erotik icerik istenmez. PG secildi cunku G\r\n'
    '            // neredeyse hic reklam doldurmuyor (gelir kaybi), PG hala aile sinirinda.\r\n'
    '            RequestConfiguration.Builder rc = new RequestConfiguration.Builder()\r\n'
    '                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)')

rep(M,
    '            // Test modu: debug derlemede her zaman, dağıtılan APK\'da ise yalnızca\r\n'
    '            // kullanıcı Ayarlar\'dan açtıysa. Test cihazında gerçek birimler test reklamı\r\n'
    '            // döndürür: reklam/ödül çalışır, para yazmaz, hesap riski olmaz.\r\n'
    '            if (Ads.TEST_MODE || Prefs.adTestMode(activity)) {',
    '            // Test modu yalnız debug derlemede. Release APK\'da kullanıcı test moduna\r\n'
    '            // geçemez: test reklamı para yazmadığı için gelir sessizce sıfırlanıyordu.\r\n'
    '            if (Ads.TEST_MODE) {')

# ---- MP-04: Ayarlar'da test modu satiri yalniz debug ----
S = 'src/com/cengiz/canlitv/SettingsActivity.java'
rep(S,
    '        row("🧪", "Reklam test modu",\r\n'
    '                "Kendi cihazında test reklamı · para yazmaz, hesap riski olmaz",\r\n'
    '                Prefs.adTestMode(this) ? "Açık" : "Kapalı", new View.OnClickListener() {\r\n'
    '                    @Override public void onClick(View v) { adTestDialog(); }\r\n'
    '                });',
    '        // PDF-06: test modu yayinlanan APK\'da gorunmez (yalniz debug derlemede).\r\n'
    '        if (BuildConfig.DEBUG) {\r\n'
    '            row("🧪", "Reklam test modu",\r\n'
    '                    "Kendi cihazında test reklamı · para yazmaz, hesap riski olmaz",\r\n'
    '                    Prefs.adTestMode(this) ? "Açık" : "Kapalı", new View.OnClickListener() {\r\n'
    '                        @Override public void onClick(View v) { adTestDialog(); }\r\n'
    '                    });\r\n'
    '        }')

# ---- MP-02: TV8 app= anahtarli adres yedekten cikti ----
C = 'src/com/cengiz/canlitv/ChannelRepo.java'
rep(C,
    '        // TV8: asıl yayın (daioncdn) reklam kuşağında kilitlenirse TürkNet/ERC yansısına geç.\r\n'
    '        String tv8Main = "https://tv8.daioncdn.net/tv8/tv8.m3u8?app=7ddc255a-ef47-4e81-ab14-c0e5f2949788&ce=3";\r\n'
    '        String tv8Mirror = "https://rkhubpaomb.turknet.ercdn.net/fwjkgpasof/tv8/tv8_720p.m3u8";\r\n'
    '        String tv8Mirror2 = "https://rkhubpaomb.turknet.ercdn.net/fwjkgpasof/tv8/tv8_480p.m3u8";\r\n'
    '        FALLBACK.put(tv8Main, new String[]{tv8Mirror, tv8Mirror2});\r\n'
    '        FALLBACK.put(tv8Mirror, new String[]{tv8Mirror2, tv8Main});\r\n'
    '        FALLBACK.put(tv8Mirror2, new String[]{tv8Mirror, tv8Main});',
    '        // TV8: daioncdn adresi başka bir uygulamanın `app=` anahtarını taşıyordu; listeden de\r\n'
    '        // yedeklerden de kaldırıldı. Yayın TürkNet/ERC yansısından, yedek olarak 480p yansısı.\r\n'
    '        mirror("https://rkhubpaomb.turknet.ercdn.net/fwjkgpasof/tv8/tv8_720p.m3u8",\r\n'
    '                "https://rkhubpaomb.turknet.ercdn.net/fwjkgpasof/tv8/tv8_480p.m3u8");')

# kayit kapisi yardimcisi
rep(C,
    '    /** Yedek URL listesi (yoksa null). */',
    '    /**\r\n'
    '     * PDF-07: kayıt yalnız kamu yayıncılarında açık. Özel kanalların yayını telifli;\r\n'
    '     * kaydedilen .ts dosyası galeriden paylaşılabildiği için risk yayın öncesi kapatıldı.\r\n'
    '     */\r\n'
    '    public static boolean canRecord(String name) {\r\n'
    '        if (name == null) return false;\r\n'
    '        String n = name.toUpperCase(java.util.Locale.ROOT).replace(\'İ\', \'I\');\r\n'
    '        return n.startsWith("TRT") || n.contains("TBMM") || n.contains("DIYANET");\r\n'
    '    }\r\n'
    '\r\n'
    '    /** Yedek URL listesi (yoksa null). */')

# ---- MP-05: kayit kapisi (tek ekran + coklu ekran) ----
P = 'src/com/cengiz/canlitv/PlayerActivity.java'
rep(P,
    '        Channel c = list.get(index);\r\n'
    '        recorder.start(this, c.url, c.name,',
    '        Channel c = list.get(index);\r\n'
    '        if (!ChannelRepo.canRecord(c.name)) {\r\n'
    '            Toast.makeText(this, "Kayıt yalnız kamu kanallarında açık (TRT, TBMM, Diyanet) · "\r\n'
    '                    + "özel kanallarda telif nedeniyle kapalı", Toast.LENGTH_LONG).show();\r\n'
    '            return;\r\n'
    '        }\r\n'
    '        recorder.start(this, c.url, c.name,')

MU = 'src/com/cengiz/canlitv/MultiActivity.java'
rep(MU,
    '        Channel c = list.get(slots[s].chan);\r\n'
    '        btn.setText("⏹ durdur");',
    '        Channel c = list.get(slots[s].chan);\r\n'
    '        if (!ChannelRepo.canRecord(c.name)) {\r\n'
    '            Toast.makeText(this, "Kayıt yalnız kamu kanallarında açık (TRT, TBMM, Diyanet)",\r\n'
    '                    Toast.LENGTH_LONG).show();\r\n'
    '            return;\r\n'
    '        }\r\n'
    '        btn.setText("⏹ durdur");')

# ---- MP-17: olu hesap metinleri ----
L = 'src/com/cengiz/canlitv/Lang.java'
s = rd(L)
for key in ('account', 'account_sub', 'guest_short', 'code_sub'):
    m = re.search(r'^[ \t]*if \("%s"\.equals\(key\)\) return pick\([^\n]*\n' % re.escape(key), s, re.M)
    if not m:
        print('HATA: Lang anahtari bulunamadi: ' + key); sys.exit(1)
    s = s[:m.start()] + s[m.end():]
wr(L, s)
print('ok  %s (4 olu hesap metni silindi)' % L)

# ---- surum 9.71 ----
G = 'app/build.gradle'
rep(G, 'versionCode 79', 'versionCode 80')
rep(G, "versionName '9.70'", "versionName '9.71'")
print('BITTI')
