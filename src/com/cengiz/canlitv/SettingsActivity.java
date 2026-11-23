package com.cengiz.canlitv;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Ayarlar: acilir pencere yerine tam sayfa. Her satirda ikon, baslik, kisa aciklama
 * ve sagda o anki deger var; satira dokununca ilgili ayar acilir.
 */
public class SettingsActivity extends Activity {

    private static final int BG = Color.parseColor("#11151C");
    private static final int CARD = Color.parseColor("#1B2230");
    private static final int ACCENT = Color.parseColor("#FF7A18");
    private static final int TEXT = Color.parseColor("#F5F7FA");
    private static final int MUTED = Color.parseColor("#9AA4B2");
    private static final int ICONBG = Color.parseColor("#2A3444");

    private LinearLayout body;
    private ProgressDialog scan;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(12), dp(12), dp(18));

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = chip("‹ Geri");
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        head.addView(back);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(12), 0, 0, 0);
        TextView t = new TextView(this);
        t.setText("Ayarlar");
        t.setTextColor(TEXT);
        t.setTextSize(21);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        TextView sub = new TextView(this);
        sub.setText("Dokunduğun satır açılır · değer sağda yazar");
        sub.setTextColor(MUTED);
        sub.setTextSize(10);
        titles.addView(t);
        titles.addView(sub);
        head.addView(titles, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(head);

        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0, dp(10), 0, 0);
        root.addView(body);

        scroll.addView(root);
        AdManager.wrap(this, scroll);

        // durum cubugunun altindan basla (edge-to-edge)
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public android.view.WindowInsets onApplyWindowInsets(View v, android.view.WindowInsets insets) {
                v.setPadding(dp(12), dp(12) + insets.getSystemWindowInsetTop(), dp(12), dp(18));
                return insets;
            }
        });
        render();
    }

    private void render() {
        body.removeAllViews();
        long used = Prefs.todayBytes(this);
        int lim = Prefs.limitMb(this);
        int pref = Prefs.maxHeight(this);

        // --- ozet karti ---
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(rounded(ICONBG, 14));
        TextView c1 = new TextView(this);
        c1.setText("Bugün " + Net.mb(used) + " internet harcandı");
        c1.setTextColor(TEXT);
        c1.setTextSize(13);
        c1.setTypeface(c1.getTypeface(), android.graphics.Typeface.BOLD);
        card.addView(c1);
        TextView c2 = new TextView(this);
        c2.setText("Kalite: " + QualityDialog.label(this)
                + (lim > 0 ? "  ·  Limit: " + lim + " MB/gün" : "  ·  Limit yok")
                + "\nKalite düştükçe harcama azalır: 360p ≈ 0,3 GB/saat, 720p ≈ 1,2 GB/saat.");
        c2.setTextColor(MUTED);
        c2.setTextSize(10);
        c2.setPadding(0, dp(3), 0, 0);
        card.addView(c2);
        body.addView(card, marginBottom(10));

        // veri gostergesi: her ekranin altinda bugunku kullanim yazsin mi?
        row("📶", "Veri göstergesi",
                "Her ekranın altında bugün harcanan interneti gösterir",
                Prefs.showUsage(this) ? "Açık" : "Kapalı",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Prefs.setShowUsage(SettingsActivity.this, !Prefs.showUsage(SettingsActivity.this));
                        recreate();
                    }
                });

        // --- hesap (istege bagli uyelik) ---
        section(Lang.t(this, "account"));
        row("👤", Lang.t(this, "account"), Lang.t(this, "account_why"),
                Account.signedIn(this)
                        ? (Account.name(this).length() > 0 ? Account.name(this) : Account.email(this))
                        : Lang.t(this, "guest_short"),
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        startActivity(new Intent(SettingsActivity.this, AccountActivity.class));
                    }
                });

        // --- genel: ulke + dil ---
        section(Lang.t(this, "general"));
        row("🌍", Lang.t(this, "country"), Lang.t(this, "country_sub"),
                Countries.flag(Prefs.country(this)) + " " + Countries.name(Prefs.country(this), false),
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        startActivity(new Intent(SettingsActivity.this, CountryActivity.class));
                    }
                });
        row("🗣", Lang.t(this, "language"), Lang.t(this, "app_language"),
                Lang.name(Prefs.uiLang(this)), new View.OnClickListener() {
                    @Override public void onClick(View v) { langDialog(); }
                });

        // --- yayin ve internet ---
        section("Yayın ve internet");
        row("🎚", "Yayın kalitesi", "Kanalın çözünürlüğü · veri tüketimini belirler",
                QualityDialog.label(this), new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        QualityDialog.show(SettingsActivity.this, new Runnable() {
                            @Override public void run() { render(); }
                        });
                    }
                });
        row("💾", "Veri tasarrufu", pref < 0
                        ? "Açık: bütün yayınlar en düşük çözünürlükte açılır"
                        : "Kapalı: kanalın en iyi kalitesi kullanılır",
                pref < 0 ? "Açık" : "Kapalı", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Prefs.setDataSaver(SettingsActivity.this, !Prefs.dataSaver(SettingsActivity.this));
                        render();
                    }
                });
        row("🎧", "Mobil veride sadece ses",
                "Wi-Fi yokken görüntü inmez, sadece ses çalar (en az veri)",
                Prefs.audioOnlyMobile(this) ? "Açık" : "Kapalı",
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Prefs.setAudioOnlyMobile(SettingsActivity.this,
                                !Prefs.audioOnlyMobile(SettingsActivity.this));
                        render();
                    }
                });
        row("📶", "Günlük veri limiti",
                "Limit dolunca yayın açmadan önce uyarır",
                lim == 0 ? "Yok" : lim + " MB", new View.OnClickListener() {
                    @Override public void onClick(View v) { askLimit(); }
                });
        row("📊", "Bugünkü kullanım", "Sayaç her gün otomatik sıfırlanır",
                Net.mb(used), new View.OnClickListener() {
                    @Override public void onClick(View v) { usageDialog(); }
                });

        // --- kanal listesi ---
        section("Kanal listesi");
        row("✏️", "Listeyi düzenle",
                "Çoklu seç, yukarı/aşağı taşı, numarayı değiştir, sil",
                "Aç ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { openMain("edit", true); }
                });
        row("⇅", "Bu sekmeyi sırala",
                "Çok izlediklerini üste al · ada veya numaraya göre diz",
                "Aç ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { openMain("sort", true); }
                });
        row("🗑", "Silinen / gizlenen kanallar",
                "Sildiklerini buradan listeye geri ekleyebilirsin",
                "Aç ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { openMain("tab", "🗑 Gizlenenler"); }
                });
        row("🩺", "Ölü kanalları gizle",
                "3 kez açılmayan kanallar listede görünmez",
                Prefs.hideDead(this) ? "Açık" : "Kapalı", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Prefs.setHideDead(SettingsActivity.this, !Prefs.hideDead(SettingsActivity.this));
                        render();
                    }
                });
        row("🧪", "Tüm kanalları test et",
                "Her yayını dener, açılmayanları işaretler (yavaş sürer)",
                "Başlat ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { healthScan(); }
                });

        // --- kayit ---
        section("Kayıt");
        row("🎬", "Kayıtlarım", "Telefona kaydettiğin yayınlar",
                "Aç ›", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        startActivity(new Intent(SettingsActivity.this, RecordingsActivity.class));
                    }
                });
        row("📁", "Kayıt yeri", "Kayıtlar nereye gidiyor?",
                "Movies/TV-Kayitlari", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        new AlertDialog.Builder(SettingsActivity.this)
                                .setTitle("Kayıtlar nereye gidiyor?")
                                .setMessage("Kayıtlar galeride:\n\nMovies / TV-Kayitlari\n\n"
                                        + "Dosya biçimi .ts (ses + görüntü birlikte).\n"
                                        + "Uygulama içinden: Ayarlar → Kayıtlarım")
                                .setPositiveButton("Tamam", null).show();
                    }
                });

        // --- radyo ---
        section("Radyo");
        row("📻", "Radyo", "49 internet radyosu · ekran kapalıyken de çalar",
                "Aç ›", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        startActivity(new Intent(SettingsActivity.this, RadioActivity.class));
                    }
                });

        // --- hakkinda ---
        section("Hakkında");
        row("ℹ️", "Ne var, ne yok?", "İstenen özelliklerin durumu: oldu / olmadı / neden",
                "Aç ›", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        startActivity(new Intent(SettingsActivity.this, StatusActivity.class));
                    }
                });
        row("📱", "Sürüm", "UMAY Multi TV", version(), new View.OnClickListener() {
            @Override public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("UMAY Multi TV")
                        .setMessage("Sürüm " + version() + "\n\nCanlı TV + radyo.\n"
                                + "Yayınlar halka açık kaynaklardan gelir; kanallar zaman zaman yayınını kaldırabilir.")
                        .setPositiveButton("Tamam", null).show();
            }
        });
        int freeLeft = AdManager.adFreeMinutesLeft(this);
        row("📢", "Reklamlar",
                freeLeft > 0
                        ? "Reklamsız sürüyor · " + freeLeft + " dakika kaldı"
                        : "Liste altında banner · video izleyip 60 dk reklamsız",
                freeLeft > 0 ? (freeLeft + " dk") : "Reklamsız ›",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { adDialog(); }
                });
        row("🐞", "Hata bildir", "Bir sorun olursa hata kaydını gönder, düzelteyim",
                "Gönder ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { shareCrash(); }
                });
        row("⭐", "Bizi değerlendir",
                "Play'de puan ver · uygulama daha çok kişiye ulaşsın",
                "Play ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { openStore(); }
                });
        row("📤", "Uygulamayı paylaş",
                "Arkadaşına gönder · Play bağlantısıyla",
                "Paylaş ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { shareApp(); }
                });
    }

    private String builtAccount = null;

    @Override
    protected void onResume() {
        super.onResume();
        // hesap durumu degistiyse (uyelik/giris/cikis) satirlari yenile
        String now = Account.email(this) + "|" + Account.verified(this);
        if (builtAccount == null) builtAccount = now;
        else if (!builtAccount.equals(now)) recreate();
    }

    /** Cihazda kayitli hata logunu paylasir (WhatsApp/mail ile gonderilebilir). */
    private void shareCrash() {
        String log = CrashLog.last(this);
        if (log.length() == 0) {
            toast("Kayıtlı hata yok. Bir sorun olursa buradan gönderebilirsin.");
            return;
        }
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "UMAY Multi TV hata kaydı");
        i.putExtra(Intent.EXTRA_TEXT, log);
        startActivity(Intent.createChooser(i, "Hata kaydını gönder"));
    }

    /** Reklam bilgisi + odullu "60 dk reklamsiz" + uyelik kisayolu. */
    private void adDialog() {
        int left = AdManager.adFreeMinutesLeft(this);
        String msg = "Reklamlar sadece şuralarda: kanal/radyo listesinin altında küçük bir banner "
                + "ve kanal değiştirirken 10 dakikada en fazla bir geçiş reklamı.\n\n"
                + "Yayının üstüne reklam binmez, oynatma kesilmez."
                + (left > 0 ? "\n\nŞu an reklamsız: " + left + " dakika kaldı." : "")
                + "\n\nKısa bir video izleyip " + Ads.REWARD_MINUTES + " dakika reklamsız kullanabilirsin.\n\n"
                + "Üyelik isteğe bağlı: e-posta veya Google ile girebilir, misafir olarak da her şeyi kullanabilirsin.";
        new AlertDialog.Builder(this)
                .setTitle("Reklamlar ve üyelik")
                .setMessage(msg)
                .setPositiveButton("🎬 " + Ads.REWARD_MINUTES + " dk reklamsız",
                        new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int w) { watchRewarded(); }
                        })
                .setNeutralButton("Üyelik", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        startActivity(new Intent(SettingsActivity.this, AccountActivity.class));
                    }
                })
                .setNegativeButton("Kapat", null).show();
    }

    private void watchRewarded() {
        if (AdManager.isAdFree(this)) {
            toast("Zaten reklamsız: " + AdManager.adFreeMinutesLeft(this) + " dakika kaldı");
            return;
        }
        AdManager.showRewarded(this, new Runnable() {
            @Override public void run() {
                toast("Teşekkürler! " + Ads.REWARD_MINUTES + " dakika reklamsız.");
                recreate();
            }
        }, new Runnable() {
            @Override public void run() {
                toast("Reklam henüz hazır değil, birkaç saniye sonra tekrar dene.");
            }
        });
    }

    private void toast(String s) {
        android.widget.Toast.makeText(this, s, android.widget.Toast.LENGTH_LONG).show();
    }

    /** Arayuz dili secimi. */
    private void langDialog() {
        final String[] names = new String[Lang.CODES.length];
        int cur = 0;
        for (int i = 0; i < Lang.CODES.length; i++) {
            names[i] = Lang.name(Lang.CODES[i]);
            if (Lang.CODES[i].equals(Prefs.uiLang(this))) cur = i;
        }
        new AlertDialog.Builder(this)
                .setTitle("Dil / Language")
                .setSingleChoiceItems(names, cur, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        Prefs.setUiLang(SettingsActivity.this, Lang.CODES[which]);
                        d.dismiss();
                        recreate();
                    }
                })
                .setNegativeButton("✕", null)
                .show();
    }

    /** Play magaza sayfasini acar (market://, yoksa web). */
    private void openStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("market://details?id=" + getPackageName())));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://play.google.com/store/apps/details?id="
                                + getPackageName())));
            } catch (Exception e2) {
                Toast.makeText(this, "Play Store açılamadı", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Uygulamayi Play baglantisiyla paylasir. */
    private void shareApp() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "UMAY Multi TV — 305 canlı TV kanalı ve 49 radyo:\n"
                + "https://play.google.com/store/apps/details?id=" + getPackageName());
        try {
            startActivity(Intent.createChooser(i, "Uygulamayı paylaş"));
        } catch (Exception ignored) { }
    }

    private void usageDialog() {
        long used = Prefs.todayBytes(this);
        new AlertDialog.Builder(this)
                .setTitle("Bugünkü kullanım")
                .setMessage("Bugün: " + Net.mb(used)
                        + "\nKalite: " + QualityDialog.label(this)
                        + "\n\nSayaç her gece otomatik sıfırlanır.")
                .setPositiveButton("Bugünü sıfırla", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        Prefs.resetToday(SettingsActivity.this);
                        render();
                    }
                })
                .setNeutralButton("Tüm sayaçları sıfırla", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        Prefs.resetAllUsage(SettingsActivity.this);
                        render();
                        Toast.makeText(SettingsActivity.this, "Sayaçlar sıfırlandı", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Kapat", null).show();
    }

    private void askLimit() {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setHint("örn: 2000 (0 = sınırsız)");
        et.setText(String.valueOf(Prefs.limitMb(this)));
        new AlertDialog.Builder(this)
                .setTitle("Günlük mobil veri limiti (MB)")
                .setMessage("Limit dolunca kanal açmadan önce uyarı verir.")
                .setView(et)
                .setPositiveButton("Kaydet", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        try {
                            Prefs.setLimitMb(SettingsActivity.this,
                                    Integer.parseInt(et.getText().toString().trim()));
                        } catch (Exception e) {
                            Prefs.setLimitMb(SettingsActivity.this, 0);
                        }
                        render();
                    }
                }).setNegativeButton("Vazgeç", null).show();
    }

    private void healthScan() {
        scan = new ProgressDialog(this);
        scan.setMessage("Kanallar test ediliyor...");
        scan.setCancelable(false);
        scan.show();
        new Thread(new Runnable() {
            @Override public void run() {
                int ok = 0, bad = 0;
                for (Channel c : ChannelRepo.all(SettingsActivity.this)) {
                    if (probe(c.url)) {
                        Prefs.clearFails(SettingsActivity.this, c.url);
                        ok++;
                    } else {
                        Prefs.addFail(SettingsActivity.this, c.url);
                        Prefs.addFail(SettingsActivity.this, c.url);
                        Prefs.addFail(SettingsActivity.this, c.url);
                        bad++;
                    }
                }
                final int fok = ok, fbad = bad;
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (scan != null && scan.isShowing()) scan.dismiss();
                        render();
                        Toast.makeText(SettingsActivity.this, "Tarama bitti: " + fok
                                + " çalışıyor, " + fbad + " ölü", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private boolean probe(String url) {
        java.net.HttpURLConnection con = null;
        try {
            con = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            con.setConnectTimeout(6000);
            con.setReadTimeout(6000);
            con.setRequestProperty("User-Agent", "VLC/3.0");
            int code = con.getResponseCode();
            return code >= 200 && code < 400;
        } catch (Exception e) {
            return false;
        } finally {
            if (con != null) try { con.disconnect(); } catch (Exception ignored) { }
        }
    }

    private void openMain(String key, Object value) {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (value instanceof Boolean) i.putExtra(key, ((Boolean) value).booleanValue());
        else if (value instanceof String) i.putExtra(key, (String) value);
        startActivity(i);
        finish();
    }

    private void section(String title) {
        TextView t = new TextView(this);
        t.setText(title.toUpperCase(java.util.Locale.getDefault()));
        t.setTextColor(ACCENT);
        t.setTextSize(10);
        t.setLetterSpacing(0.08f);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        t.setPadding(dp(4), dp(14), 0, dp(6));
        body.addView(t);
    }

    private void row(String icon, String title, String desc, String value,
                     View.OnClickListener click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(9), dp(10), dp(9));
        row.setBackground(rounded(CARD, 14));
        row.setClickable(true);

        TextView ic = new TextView(this);
        ic.setText(icon);
        ic.setTextSize(16);
        ic.setGravity(Gravity.CENTER);
        ic.setBackground(rounded(ICONBG, 11));
        ic.setLayoutParams(new LinearLayout.LayoutParams(dp(38), dp(38)));
        row.addView(ic);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        clp.setMargins(dp(10), 0, dp(6), 0);
        TextView n = new TextView(this);
        n.setText(title);
        n.setTextColor(TEXT);
        n.setTextSize(13);
        n.setTypeface(n.getTypeface(), android.graphics.Typeface.BOLD);
        TextView m = new TextView(this);
        m.setText(desc);
        m.setTextColor(MUTED);
        m.setTextSize(10);
        m.setPadding(0, dp(2), 0, 0);
        col.addView(n);
        col.addView(m);
        row.addView(col, clp);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextColor(ACCENT);
        v.setTextSize(11);
        v.setGravity(Gravity.END);
        row.addView(v);

        if (click != null) row.setOnClickListener(click);
        body.addView(row, marginBottom(6));
    }

    private LinearLayout.LayoutParams marginBottom(int d) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(d));
        return lp;
    }

    private String version() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "?";
        }
    }

    private TextView chip(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(TEXT);
        t.setTextSize(12);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(8), dp(12), dp(8));
        t.setBackground(rounded(ICONBG, 16));
        return t;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
