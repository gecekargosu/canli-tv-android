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

    /** Ülke satırının değeri: CountryActivity'den dönünce tazelenir. */
    private TextView countryValue;

    private static final int BG = Color.parseColor("#11151C");
    private static final int CARD = Color.parseColor("#1B2230");
    private static final int ACCENT = Color.parseColor("#FF7A18");
    private static final int TEXT = Color.parseColor("#F5F7FA");
    private static final int MUTED = Color.parseColor("#9AA4B2");
    private static final int ICONBG = Color.parseColor("#2A3444");

    private static final int REQ_M3U = 1001;
    private static final String LEGAL_MAIL = "cengizkilic1980@gmail.com";

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
        sub.setText("Sürüm " + version() + " · dokunduğun satır açılır");
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
                + "\nTahmini tüketim: 360p saatte ~300 MB, 720p saatte ~1,2 GB. "
                + "Sayaç yalnız yayın verisini ölçer (reklamlar hariç).");
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

        // --- P0-4: üyelik sistemi kaldırıldı (v9.21) ---

        // --- genel: ulke + dil ---
        section(Lang.t(this, "general"));
        countryValue = row("🌍", Lang.t(this, "country"), Lang.t(this, "country_sub"),
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
        row("🪙", "Ekonomi modu",
                "Her zaman en düşük kalite + kapakta sadece ses · en az veri",
                Prefs.economy(this) ? "Açık" : "Kapalı", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Prefs.setEconomy(SettingsActivity.this, !Prefs.economy(SettingsActivity.this));
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
        row("🧰", "Ölü kanalları onar",
                "Açılmayanları yeniden dener, yayını döneni listeye geri ekler",
                "Onar ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { repair(); }
                });
        row("🧪", "Tüm kanalları test et",
                "Her yayını dener, açılmayanları işaretler (yavaş sürer)",
                "Başlat ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { healthScan(); }
                });
        row("💰", "Reklam sayacım",
                "Kaç reklam gördün · tahmini kazanç (bu cihaz)",
                AdStats.shortText(this), new View.OnClickListener() {
                    @Override public void onClick(View v) { adStatsDialog(); }
                });
        // PDF-06: test modu yayinlanan APK'da gorunmez (yalniz debug derlemede).
        // Metot uzerinden bakiliyor ki derleyici blogu tamamen atmasin (surum denetimi
        // sinif sayisini karsilastiriyor; atilan blok "kod kaybi" gibi gorunuyordu).
        if (adTestUiVisible()) {
            row("🧪", "Reklam test modu",
                    "Kendi cihazında test reklamı · para yazmaz, hesap riski olmaz",
                    Prefs.adTestMode(this) ? "Açık" : "Kapalı", new View.OnClickListener() {
                        @Override public void onClick(View v) { adTestDialog(); }
                    });
        }

        // #102: kullanıcının kendi kanalları (M3U adresi veya dosyadan)
        row("➕", "Kendi kanalımı ekle",
                "M3U adresi veya kanal linki · listenin en başına gelir",
                "Ekle ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { addCustomChannel(); }
                });
        row("📄", "M3U dosyasından ekle",
                "Telefondaki .m3u listesini içe aktar",
                "İçe aktar ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { importM3u(); }
                });
        row("🗂", "Kendi kanallarım",
                "Eklediğin kanalları gör, listeden kaldır",
                "Yönet ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { manageCustom(); }
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
        row("📻", "Radyo", "Seçili ülkenin radyoları · ekran kapalıyken de çalar",
                "Aç ›", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        startActivity(new Intent(SettingsActivity.this, RadioActivity.class));
                    }
                });

        // --- hakkinda ---
        section("Hakkında");
        row("📖", "Program tanıtımı ve özellikleri",
                "A'dan Z'ye ne yapar, sorun olursa ne yapmalı, iletişim",
                "Oku ›", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        startActivity(new android.content.Intent(SettingsActivity.this,
                                AboutActivity.class));
                    }
                });
        row("ℹ️", "Ne var, ne yok?", "İstenen özelliklerin durumu: oldu / olmadı / neden",
                "Aç ›", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        startActivity(new Intent(SettingsActivity.this, StatusActivity.class));
                    }
                });
        row("©", "Telif / içerik bildir",
                "Yayın hakkı sende olan kanalı bildir · 24-48 saatte kaldırılır",
                "Bildir ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { copyrightDialog(); }
                });
        row("📱", "Sürüm " + version(), "UMAY Multi TV", "Ayrıntı ›",
                new View.OnClickListener() {
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
                        : "Banner her zaman · 15 dakikada bir ve her 3 kanal değişiminde reklam",
                freeLeft > 0 ? (freeLeft + " dk") : "Reklamsız ›",
                new View.OnClickListener() {
                    @Override public void onClick(View v) { adDialog(); }
                });
        row("🐞", "Hata günlüğü",
                "Ne zaman ne oldu: bağlantı kopması, açılmayan kanal, çökme",
                "Aç ›", new View.OnClickListener() {
                    @Override public void onClick(View v) { errorLogDialog(); }
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

    /**
     * Hata günlüğü: cihazda biriken olaylar (açılış, bağlantı kopması, ölü kanal,
     * oynatıcı hata kodu, reklam yükleme hatası) zaman damgasıyla okunur.
     */
    private void errorLogDialog() {
        String log = CrashLog.report(this);
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText(log);
        tv.setTextSize(11);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        tv.setTextIsSelectable(true);
        int pad = (int) (14 * getResources().getDisplayMetrics().density);
        tv.setPadding(pad, pad, pad, pad);
        android.widget.ScrollView sv = new android.widget.ScrollView(this);
        sv.addView(tv);
        new AlertDialog.Builder(this)
                .setTitle("Hata günlüğü")
                .setView(sv)
                .setPositiveButton("📧 Gönder", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { mailCrash(CrashLog.report(SettingsActivity.this)); }
                })
                .setNeutralButton("Temizle", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        CrashLog.clear(SettingsActivity.this);
                        CrashLog.clearEvents(SettingsActivity.this);
                        Toast.makeText(SettingsActivity.this, "Hata günlüğü temizlendi", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Kapat", null)
                .show();
    }

    /** Cihazda kayitli hata logunu paylasir (WhatsApp/mail ile gonderilebilir). */
    private void shareCrash() {
        final String log = CrashLog.report(this);
        new AlertDialog.Builder(this)
                .setTitle("Hata kaydı")
                .setMessage("Uygulama sürümü, cihaz bilgisi ve son bağlantı hataları hazır.\n\n"
                        + "E-posta ile gönderirsen doğrudan bana ulaşır; istersen WhatsApp/mesajla da "
                        + "paylaşabilirsin.\n\nKayıt: " + Math.min(log.length(), 6000) + " karakter")
                .setPositiveButton("📧 E-posta", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { mailCrash(log); }
                })
                .setNeutralButton("Paylaş", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { shareText(log); }
                })
                .setNegativeButton("Kapat", null).show();
    }

    /** Hata kaydını destek e-postasına gönderir (kullanıcı sadece Gönder'e basar). */
    private void mailCrash(String log) {
        try {
            Intent i = new Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:"));
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{Ads.SUPPORT_EMAIL});
            i.putExtra(Intent.EXTRA_SUBJECT, "UMAY Multi TV hata kaydı · sürüm " + version());
            i.putExtra(Intent.EXTRA_TEXT, log);
            startActivity(Intent.createChooser(i, "E-posta ile gönder"));
        } catch (Throwable t) {
            shareText(log);
        }
    }

    private void shareText(String log) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "UMAY Multi TV hata kaydı · sürüm " + version());
        i.putExtra(Intent.EXTRA_TEXT, log);
        startActivity(Intent.createChooser(i, "Hata kaydını paylaş"));
    }

    /** Reklam bilgisi + odullu "60 dk reklamsiz" + uyelik kisayolu. */
    private void adDialog() {
        int left = AdManager.adFreeMinutesLeft(this);
        String msg = "Reklamlar şuralarda: listelerin altında küçük bir banner (her zaman görünür), "
                + "izlerken 15 dakikada bir ve her 3 kanal değişiminde bir geçiş reklamı.\n\n"
                + "Kanal başına ayrı reklamsız süre yok; süre uygulama genelidir."
                + (left > 0 ? "\n\nŞu an reklamsız: " + left + " dakika kaldı." : "")
                + "\n\nİstersen kısa bir video izleyip " + Ads.REWARD_MINUTES
                + " dakika reklamsız izleyebilirsin.\n\n"
                + "Üyelik gerekmez: hiçbir özellik için giriş istemiyoruz.";
        new AlertDialog.Builder(this)
                .setTitle("Reklamlar ve üyelik")
                .setMessage(msg)
                .setPositiveButton("🎬 " + Ads.REWARD_MINUTES + " dk reklamsız",
                        new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int w) { watchRewarded(); }
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

    /** #100: telif / içerik kaldırma bildirimi (24-48 saat süreç). */
    private void copyrightDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Telif / içerik bildirimi")
                .setMessage("Bir kanalın yayın haklarına sahipsen ve listeden kaldırılmasını\n"
                        + "istiyorsan bize yaz.\n\n"
                        + "• Kanallar halka açık yayın adreslerinden listelenir; uygulama yayını barındırmaz.\n"
                        + "• Bildirimden sonra en geç 24-48 saat içinde ilgili kanal listeden kaldırılır.\n"
                        + "• E-postaya kanal adı + yayın adresi + hak sahipliği bilgisini ekle.\n\n"
                        + LEGAL_MAIL)
                .setPositiveButton("E-posta gönder", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { mailLegal(); }
                })
                .setNeutralButton("Adresi kopyala", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        android.content.ClipboardManager cm = (android.content.ClipboardManager)
                                getSystemService(CLIPBOARD_SERVICE);
                        if (cm != null) cm.setPrimaryClip(
                                android.content.ClipData.newPlainText("e-posta", LEGAL_MAIL));
                        toast("Kopyalandı: " + LEGAL_MAIL);
                    }
                })
                .setNegativeButton("Kapat", null)
                .show();
    }

    private void mailLegal() {
        Intent i = new Intent(Intent.ACTION_SENDTO,
                android.net.Uri.parse("mailto:" + LEGAL_MAIL));
        i.putExtra(Intent.EXTRA_SUBJECT, "UMAY Multi TV — Telif / içerik bildirimi");
        i.putExtra(Intent.EXTRA_TEXT,
                "Kanal adı:\nYayın adresi:\nHak sahipliği:\n\nKanalın listeden kaldırılmasını talep ediyorum.");
        try {
            startActivity(i);
        } catch (Exception e) {
            toast("E-posta uygulaması bulunamadı. Adres: " + LEGAL_MAIL);
        }
    }

    /** #102: tek kanal (M3U adresi) ekleme. */
    private void addCustomChannel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(8), dp(16), 0);

        final EditText name = new EditText(this);
        name.setHint("Kanal adı (örn: Kendi kanalım)");
        name.setSingleLine(true);
        box.addView(name);

        final EditText url = new EditText(this);
        url.setHint("https://... .m3u8");
        url.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        url.setSingleLine(true);
        box.addView(url);

        TextView note = new TextView(this);
        note.setText("Adres .m3u/.m3u8 listesi veya tek bir yayın linki olabilir. "
                + "Eklediğin kanal listenin en başında görünür. Kaldırmak için:\n"
                + "Ayarlar → Kendi kanallarım.");
        note.setTextColor(MUTED);
        note.setTextSize(10);
        note.setPadding(0, dp(6), 0, 0);
        box.addView(note);

        new AlertDialog.Builder(this)
                .setTitle("Kendi kanalımı ekle")
                .setView(box)
                .setPositiveButton("Ekle", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        String u = url.getText().toString().trim();
                        if (!u.startsWith("http")) {
                            toast("Adres http:// veya https:// ile başlamalı");
                            return;
                        }
                        Prefs.addCustomTv(SettingsActivity.this,
                                name.getText().toString().trim(), u);
                        ChannelRepo.reload(SettingsActivity.this);
                        toast("Kanal eklendi · listenin başında");
                    }
                })
                .setNegativeButton("Vazgeç", null)
                .show();
    }

    /** #102: eklenen kanalları listele / kaldır. */
    private void manageCustom() {
        final java.util.List<String[]> mine = Prefs.customTv(this);
        if (mine.isEmpty()) {
            toast("Henüz kendi kanalın yok. \"Kendi kanalımı ekle\" ile başla.");
            return;
        }
        final String[] names = new String[mine.size()];
        for (int i = 0; i < mine.size(); i++) names[i] = mine.get(i)[0] + "\n" + mine.get(i)[1];
        new AlertDialog.Builder(this)
                .setTitle("Kendi kanallarım (" + mine.size() + ")")
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, final int which) {
                        new AlertDialog.Builder(SettingsActivity.this)
                                .setTitle(mine.get(which)[0])
                                .setMessage("Bu kanalı kendi listenden kaldıralım mı?")
                                .setPositiveButton("Kaldır", new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface dd, int w) {
                                        Prefs.removeCustomTv(SettingsActivity.this, mine.get(which)[1]);
                                        ChannelRepo.reload(SettingsActivity.this);
                                        render();
                                        toast("Kaldırıldı");
                                    }
                                })
                                .setNegativeButton("Vazgeç", null).show();
                    }
                })
                .setNegativeButton("Kapat", null)
                .show();
    }

    /** #102: telefondaki .m3u dosyasından toplu içe aktarma. */
    private void importM3u() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        try {
            startActivityForResult(i, REQ_M3U);
        } catch (Exception e) {
            toast("Dosya seçici açılamadı");
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != REQ_M3U || res != RESULT_OK || data == null || data.getData() == null) return;
        final android.net.Uri uri = data.getData();
        toast("Liste okunuyor…");
        new Thread(new Runnable() {
            @Override public void run() {
                int added = 0;
                try {
                    java.io.BufferedReader r = new java.io.BufferedReader(
                            new java.io.InputStreamReader(getContentResolver().openInputStream(uri), "UTF-8"));
                    String line, name = null;
                    while ((line = r.readLine()) != null) {
                        line = line.trim();
                        if (line.length() == 0) continue;
                        if (line.startsWith("#EXTINF")) {
                            int comma = line.lastIndexOf(',');
                            name = comma >= 0 ? line.substring(comma + 1).trim() : "Kanal";
                        } else if (!line.startsWith("#") && line.startsWith("http")) {
                            Prefs.addCustomTv(SettingsActivity.this,
                                    name == null || name.length() == 0 ? "Kendi kanalım" : name, line);
                            added++;
                            name = null;
                        }
                    }
                    r.close();
                } catch (Exception ignored) { }
                final int n = added;
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        ChannelRepo.reload(SettingsActivity.this);
                        toast(n == 0 ? "Listede uygun kanal bulunamadı"
                                : n + " kanal eklendi · listenin başında");
                    }
                });
            }
        }, "m3u-import").start();
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
        int tv = ChannelRepo.all(this).size();
        i.putExtra(Intent.EXTRA_TEXT, "UMAY Multi TV — " + tv + " canlı TV kanalı ve radyo:\n"
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

    private void repair() {
        scan = new ProgressDialog(this);
        scan.setMessage("Ölü kanallar yeniden deneniyor…");
        scan.setCancelable(false);
        scan.show();
        Repair.run(this, new Repair.Callback() {
            @Override public void done(final int tested, final int revived) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (scan != null && scan.isShowing()) scan.dismiss();
                        render();
                        String msg = tested == 0
                                ? "Onarılacak ölü kanal yok — hepsi sağlam görünüyor."
                                : tested + " ölü kanal denendi, " + revived + " tanesi geri geldi.";
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void adStatsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reklam sayacım (tahmini)")
                .setMessage(AdStats.summary(this))
                .setPositiveButton("Kapat", null)
                .setNeutralButton("Sıfırla", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        AdStats.reset(SettingsActivity.this);
                        render();
                    }
                }).show();
    }

    /**
     * AdMob test cihazı: reklam kimliğini gösterir, tek dokunuşla test modunu açar.
     * Test modunda reklamlar açılır ve ödüller tetiklenir; gelir yazmaz, geçersiz trafik riski olmaz.
     */
    private void adTestDialog() {
        final AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("Reklam test modu")
                .setMessage("Reklam kimliği okunuyor…")
                .setPositiveButton("Kapat", null)
                .setNegativeButton("Kimliği kopyala", null)
                .setNeutralButton(Prefs.adTestMode(this) ? "Testi kapat" : "Testi aç", null)
                .create();
        dlg.show();
        new Thread(new Runnable() {
            @Override public void run() {
                final String id = AdsTest.gaid(SettingsActivity.this);
                final String hash = id == null ? null : AdsTest.md5(id.toUpperCase());
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (id == null) {
                            dlg.setMessage("Reklam kimliği okunamadı.\n\n"
                                    + "Android Ayarlar → Google → Reklamlar bölümünden "
                                    + "kişiselleştirilmiş reklamları açıp tekrar dene.");
                            dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                            return;
                        }
                        String msg = "Açıkken reklamlar TEST reklamı olur: reklam açılır, "
                                + "ödül çalışır, ama para yazmaz ve hesabın risk almaz.\n\n"
                                + "Reklam kimliğin (AdMob konsoluna eklemek için):\n" + id + "\n\n"
                                + "AdMob → Ayarlar → Test cihazları → Android → bu kimliği yapıştır.\n\n"
                                + "Uygulama içi test için aşağıdaki düğme yeterli.";
                        dlg.setMessage(msg);
                        dlg.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(
                                new View.OnClickListener() {
                                    @Override public void onClick(View v) {
                                        try {
                                            android.content.ClipboardManager cm =
                                                    (android.content.ClipboardManager)
                                                            getSystemService(CLIPBOARD_SERVICE);
                                            cm.setPrimaryClip(android.content.ClipData
                                                    .newPlainText("advertising id", id));
                                            Toast.makeText(SettingsActivity.this,
                                                    "Reklam kimliği kopyalandı",
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (Exception ignored) { }
                                    }
                                });
                        dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                                new View.OnClickListener() {
                                    @Override public void onClick(View v) {
                                        boolean yeni = !Prefs.adTestMode(SettingsActivity.this);
                                        Prefs.setAdTestMode(SettingsActivity.this, yeni);
                                        if (hash != null) {
                                            Prefs.setAdTestHashed(SettingsActivity.this, hash);
                                        }
                                        Toast.makeText(SettingsActivity.this,
                                                yeni ? "Test modu açık · reklamlar test reklamı "
                                                        + "olarak gelir (para yazmaz)"
                                                        : "Test modu kapalı · gerçek reklamlar",
                                                Toast.LENGTH_LONG).show();
                                        dlg.dismiss();
                                        render();
                                        // RequestConfiguration yeniden okunsun
                                        AdManager.resetForTestMode();
                                        recreate();
                                    }
                                });
                    }
                });
            }
        }, "adtest").start();
    }

    private void healthScan() {
        scan = new ProgressDialog(this);
        scan.setMessage("Kanallar test ediliyor...");
        scan.setCancelable(false);
        scan.show();
        new Thread(new Runnable() {
            @Override public void run() {
                int ok = 0, bad = 0;
                // M-33: tarama arka planda; canlı liste yerine kopya.
                for (Channel c : ChannelRepo.snapshot(SettingsActivity.this)) {
                    if (Repair.alive(c.url)) {
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

    /** Satırı ekler ve sağdaki değer TextView'ini döndürür (onResume'da tazelemek için). */
    private TextView row(String icon, String title, String desc, String value,
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
        return v;
    }

    @Override protected void onResume() {
        super.onResume();
        // HATA: ülke ekranından dönünce satır hâlâ eski ülkeyi gösteriyordu
        // (değer yalnız onCreate'de yazılıyordu).
        if (countryValue != null) {
            String cc = Prefs.country(this);
            countryValue.setText(Countries.flag(cc) + " " + Countries.name(cc, false));
        }
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

    @Override
    protected void onDestroy() {
        // HATA (tur 2): tarama sürerken geri çıkılırsa ProgressDialog pencere sızıntısı
        // (WindowLeaked) ve kapanmış ekrana Toast/dismiss → BadTokenException oluyordu.
        if (scan != null && scan.isShowing()) {
            try { scan.dismiss(); } catch (Exception ignored) { }
        }
        scan = null;
        super.onDestroy();
    }

    /** Yayinlanan APK'da test modu satiri yok; debug derlemede var. */
    private static boolean adTestUiVisible() {
        return BuildConfig.DEBUG;
    }
}
