# -*- coding: utf-8 -*-
"""9.72: hata/kopma gunlugu guclendirme + Ayarlar'da okunabilir hata gunlugu."""
import io, re, sys

def rd(p): return io.open(p, encoding='utf-8', newline='').read()
def wr(p, s): io.open(p, 'w', encoding='utf-8', newline='').write(s)

def rep(p, old, new, count=1):
    s = rd(p)
    if s.count(old) != count:
        print('HATA %s: %d kez (beklenen %d)\n%r' % (p, s.count(old), count, old[:80])); sys.exit(1)
    wr(p, s.replace(old, new)); print('ok', p)

# --- 1) MainActivity: acilis kaydi ---
M = 'src/com/cengiz/canlitv/MainActivity.java'
rep(M,
    '        CrashLog.install(this);   // cokme olursa cihaza log yaz (Ayarlar -> Hata bildir)',
    '        CrashLog.install(this);   // cokme olursa cihaza log yaz (Ayarlar -> Hata günlüğü)\r\n'
    '        // Hata günlüğü: hangi sürüm, hangi ülke, hangi mod ile açıldı.\r\n'
    '        CrashLog.event(this, "app", "açılış " + BuildConfig.VERSION_NAME\r\n'
    '                + " · ülke=" + Prefs.country(this)\r\n'
    '                + " · ekonomi=" + (Prefs.economy(this) ? "1" : "0")\r\n'
    '                + " · dil=" + Prefs.uiLang(this));')

# --- 2) PlayerActivity: acilis / hazir / hata kodu / olu / ag kaybi ---
P = 'src/com/cengiz/canlitv/PlayerActivity.java'
rep(P,
    '            @Override public void onPlayerError(androidx.media3.common.PlaybackException e) {\r\n'
    '                onPError();\r\n'
    '            }',
    '            @Override public void onPlayerError(androidx.media3.common.PlaybackException e) {\r\n'
    '                // Hata günlüğü: teknik kod, kullanıcının gördüğü mesajdan bağımsız.\r\n'
    '                lastError = e == null ? "?" : (e.errorCodeName + " · " + e.getMessage());\r\n'
    '                CrashLog.event(PlayerActivity.this, "tv", "oynatıcı hatası: " + lastError);\r\n'
    '                onPError();\r\n'
    '            }')

rep(P,
    '    /** Aynı yayına kaç kez yeniden bağlanıldı (zayıf internet). */',
    '    /** Son oynatıcı hatası (hata günlüğüne yazılır). */\r\n'
    '    private String lastError = "";\r\n'
    '\r\n'
    '    /** Aynı yayına kaç kez yeniden bağlanıldı (zayıf internet). */')

rep(P,
    '        everPlayed = true;   // M-99: ilk kare geldi, bu kanal ölü sayılmayacak',
    '        everPlayed = true;   // M-99: ilk kare geldi, bu kanal ölü sayılmayacak\r\n'
    '        CrashLog.event(this, "tv", "hazır: " + list.get(index).name);')

# markDead: nedeni + adres + mod yaz
rep(P,
    '    private void markDead(String neden) {\r\n'
    '        try { Prefs.addFail(this, list.get(index).url); } catch (Exception ignored) { }',
    '    private void markDead(String neden) {\r\n'
    '        String nm = "?", u = "";\r\n'
    '        try { nm = list.get(index).name; u = list.get(index).url; } catch (Exception ignored) { }\r\n'
    '        // Hata günlüğü: kanal neden ve ne kadar sürede ölü sayıldı.\r\n'
    '        CrashLog.event(this, "tv", "ölü: " + nm + " · " + neden\r\n'
    '                + (lastError.length() > 0 ? " · son hata=" + lastError : "")\r\n'
    '                + " · " + u);\r\n'
    '        try { Prefs.addFail(this, list.get(index).url); } catch (Exception ignored) { }')

# ag kaybi
rep(P,
    '            netCb = new android.net.ConnectivityManager.NetworkCallback() {',
    '            netCb = new android.net.ConnectivityManager.NetworkCallback() {\r\n'
    '                @Override public void onLost(android.net.Network n) {\r\n'
    '                    CrashLog.event(PlayerActivity.this, "net", "bağlantı koptu");\r\n'
    '                }')

# --- 3) AdManager: reklam yukleme hatasi ---
A = 'src/com/cengiz/canlitv/AdManager.java'
s = rd(A)
m = re.search(r'onAdFailedToLoad\(LoadAdError error\) \{\r?\n(\s*)', s)
if not m:
    print('HATA: onAdFailedToLoad bulunamadi'); sys.exit(1)
crlf = '\r\n' if '\r\n' in s else '\n'
ins = 'CrashLog.event(activity, "ad", "geçiş reklamı yüklenemedi: " + error.getCode() + " " + error.getMessage());' + crlf + m.group(1)
s = s[:m.end()] + ins + s[m.end():]
wr(A, s)
print('ok', A)

# --- 4) Ayarlar: okunabilir hata gunlugu ---
S = 'src/com/cengiz/canlitv/SettingsActivity.java'
rep(S,
    '        row("🐞", "Hata bildir", "Bir sorun olursa hata kaydını gönder, düzelteyim",\r\n'
    '                "Gönder ›", new View.OnClickListener() {\r\n'
    '                    @Override public void onClick(View v) { shareCrash(); }\r\n'
    '                });',
    '        row("🐞", "Hata günlüğü",\r\n'
    '                "Ne zaman ne oldu: bağlantı kopması, açılmayan kanal, çökme",\r\n'
    '                "Aç ›", new View.OnClickListener() {\r\n'
    '                    @Override public void onClick(View v) { errorLogDialog(); }\r\n'
    '                });')

rep(S,
    '    /** Cihazda kayitli hata logunu paylasir (WhatsApp/mail ile gonderilebilir). */',
    '    /**\r\n'
    '     * Hata günlüğü: cihazda biriken olaylar (açılış, bağlantı kopması, ölü kanal,\r\n'
    '     * oynatıcı hata kodu, reklam yükleme hatası) zaman damgasıyla okunur.\r\n'
    '     */\r\n'
    '    private void errorLogDialog() {\r\n'
    '        String log = CrashLog.report(this);\r\n'
    '        android.widget.TextView tv = new android.widget.TextView(this);\r\n'
    '        tv.setText(log);\r\n'
    '        tv.setTextSize(11);\r\n'
    '        tv.setTypeface(android.graphics.Typeface.MONOSPACE);\r\n'
    '        tv.setTextIsSelectable(true);\r\n'
    '        int pad = (int) (14 * getResources().getDisplayMetrics().density);\r\n'
    '        tv.setPadding(pad, pad, pad, pad);\r\n'
    '        android.widget.ScrollView sv = new android.widget.ScrollView(this);\r\n'
    '        sv.addView(tv);\r\n'
    '        new AlertDialog.Builder(this)\r\n'
    '                .setTitle("Hata günlüğü")\r\n'
    '                .setView(sv)\r\n'
    '                .setPositiveButton("📧 Gönder", new DialogInterface.OnClickListener() {\r\n'
    '                    @Override public void onClick(DialogInterface d, int w) { mailCrash(CrashLog.report(SettingsActivity.this)); }\r\n'
    '                })\r\n'
    '                .setNeutralButton("Temizle", new DialogInterface.OnClickListener() {\r\n'
    '                    @Override public void onClick(DialogInterface d, int w) {\r\n'
    '                        CrashLog.clear(SettingsActivity.this);\r\n'
    '                        CrashLog.clearEvents(SettingsActivity.this);\r\n'
    '                        Toast.makeText(SettingsActivity.this, "Hata günlüğü temizlendi", Toast.LENGTH_SHORT).show();\r\n'
    '                    }\r\n'
    '                })\r\n'
    '                .setNegativeButton("Kapat", null)\r\n'
    '                .show();\r\n'
    '    }\r\n'
    '\r\n'
    '    /** Cihazda kayitli hata logunu paylasir (WhatsApp/mail ile gonderilebilir). */')

# --- surum 9.72 ---
rep('app/build.gradle', 'versionCode 80', 'versionCode 81')
rep('app/build.gradle', "versionName '9.71'", "versionName '9.72'")
print('BITTI')
