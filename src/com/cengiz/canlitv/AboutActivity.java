package com.cengiz.canlitv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * "Program tanıtımı ve özellikleri" — okuyan kişi uygulamanın A'dan Z'ye ne yaptığını,
 * sorun çıkınca ne yapacağını ve geliştiriciye nasıl ulaşacağını burada bulur.
 */
public class AboutActivity extends Activity {

    /** İletişim e-postası (telif bildirimiyle aynı). */
    public static final String MAIL = "cengizkilic1980@gmail.com";
    /**
     * WhatsApp numarası: ülke kodu + numara, boşluksuz (ör. "905551112233").
     * Boşsa WhatsApp düğmesi "numara henüz eklenmedi" uyarısı verir.
     */
    public static final String WHATSAPP = "";

    private static final int BG = Color.parseColor("#0B1220");
    private static final int CARD = Color.parseColor("#121C2E");
    private static final int TEXT = Color.parseColor("#E8EEF7");
    private static final int MUTED = Color.parseColor("#93A4BD");
    private static final int ACCENT = Color.parseColor("#FF9F1C");

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(14), dp(16), dp(28));
        scroll.addView(body);

        // üst satır: başlık + kapat
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("UMAY Multi TV");
        title.setTextColor(TEXT);
        title.setTextSize(21);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        head.addView(title);

        TextView close = new TextView(this);
        close.setText("✕");
        close.setTextColor(TEXT);
        close.setTextSize(18);
        close.setGravity(Gravity.CENTER);
        close.setPadding(dp(12), dp(6), dp(12), dp(6));
        GradientDrawable cbg = new GradientDrawable();
        cbg.setColor(Color.parseColor("#22304A"));
        cbg.setCornerRadius(dp(18));
        close.setBackground(cbg);
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        head.addView(close);
        body.addView(head);

        p(body, I18n.t(this, "Sürüm ") + version()
                + I18n.t(this, " · Yabanarısı tarafından geliştirildi"), MUTED, 12);

        // v10.04 (Paket C-3): Türkçe dışındaki 7 dilde kısa, çevrilmiş tanıtım gösterilir.
        if (!"tr".equals(Prefs.uiLang(this))) {
            worldBody(body);
            addButtons(body);
            AdManager.wrap(this, scroll);
            return;
        }

        final String cc = Prefs.country(this);
        final boolean tr = "tr".equals(cc);
        final String cname = Countries.flag(cc) + " " + Countries.name(cc, false);

        h(body, "Bu program ne yapar?");
        p(body, "UMAY Multi TV, internet üzerinden canlı televizyon ve radyo izlemek için "
                + "yapılmış bir Android uygulamasıdır. Üyelik istemez, kredi kartı istemez; "
                + "açtığın anda yayın başlar.", TEXT, 13);
        if (tr) {
            p(body, "Şu an Türkiye listesini görüyorsun: ATV, Star TV, TV8, Show TV, Kanal D, NOW, "
                    + "TRT kanalları, haber ve müzik kanalları ile 30'dan fazla Türk radyosu hazır gelir.", TEXT, 13);
        } else {
            p(body, "Şu an " + cname + " listesini görüyorsun. Bu listede o ülkenin yayıncılarının "
                    + "herkese açık kanalları ve en çok dinlenen radyoları yer alır; Türk kanalları "
                    + "görünmez. Listeyi değiştirmek için: Ayarlar → 🌍 Ülke.", TEXT, 13);
        }
        p(body, "Ayarlardan ülke değiştirerek Almanya, ABD, İngiltere, Fransa, Rusya, Hindistan, "
                + "Brezilya, Meksika, Suudi Arabistan, İsveç gibi ülkelerin listelerini indirebilirsin. "
                + "Radyo listesi de seçtiğin ülkeye göre değişir.", TEXT, 13);

        h(body, "Ülkeye göre içerik (önemli)");
        p(body, tr
                ? "Türkiye listesi uygulamanın içinde hazır gelir, internet olmadan da görünür. "
                + "Türk kanalları TRT/ATV/Star gibi ulusal yayıncılar ve onların haber-spor-müzik "
                + "kanallarıdır. Bir kanal listede yoksa ülke ayarının Türkiye olduğundan emin ol."
                : cname + " listesi sunucudan indirilir ve telefonda saklanır; ilk açılışta birkaç "
                + "saniye sürebilir. Bu listede " + cname + " yayıncıları bulunur; ATV, TRT, Star TV "
                + "gibi Türk kanalları burada görünmez — onlar için Ayarlar → Ülke → Türkiye seç."
                + " Ülke listesi hazır değilse ana ekranda \"liste henüz hazır değil\" yazar; "
                + "dokununca yeniden indirilir.", TEXT, 13);
        p(body, "Aynı anda yalnızca bir ülkenin listesi yüklenir; ülke değiştirince önceki liste "
                + "değişir. Favoriler ve kendi eklediğin kanallar silinmez.", TEXT, 13);

        h(body, "Öne çıkan özellikler");
        bullet(body, "Canlı TV: tek dokunuşla yayın, kalite seçimi (240p–1080p), 360p varsayılan.");
        bullet(body, "Radyo: ekran kapalıyken çalar, kilit ekranından durdur/kanal değiştir.");
        bullet(body, "3'lü izleme: aynı ekranda üç kanalı birden aç (spor + haber + müzik).");
        bullet(body, "Kayıt: izlediğin yayını telefona kaydet, Kayıtlarım'dan oynat.");
        bullet(body, "Uyku zamanlayıcısı: 15–120 dakika veya saat gir, uygulama kendini kapatsın.");
        bullet(body, "Küçük pencere (PiP): başka uygulamadayken yayın köşede devam eder.");
        bullet(body, "Favoriler, kanal düzenleme, numara değiştirme, kendi kanalını/M3U dosyanı ekleme.");
        bullet(body, "Arama, ülke ve tür filtreleri (Genel, Haber, Spor, Müzik, Çocuk, Belgesel…).");
        bullet(body, "Veri sayacı: bugün ne kadar internet harcadığını gösterir, günlük limit koyabilirsin.");
        bullet(body, "Ekonomi modu: en düşük kalite + ekran kapanınca yalnız ses → en az veri.");
        bullet(body, "Türkçe, İngilizce, Rusça, Arapça arayüz.");

        h(body, "Sorun olursa ne yapmalı?");
        p(body, "Kanal açılmıyor / siyah ekran:", ACCENT, 13);
        p(body, "1) İnternetini kontrol et (Wi-Fi veya mobil veri). 2) Kanala tekrar dokun; uygulama "
                + "zaten 3 saniyede bir kendini toparlamayı dener. 3) Alt bardaki ⏭ ile bir sonraki kanala "
                + "geç. 4) Ayarlar → \"Ölü kanalları onar\" çalıştır; yayın geri gelmişse kanal listeye döner. "
                + "5) Kanal gerçekten kapanmışsa (yayıncı yayını bitirmişse) uygulama yeni kaynak uyduramaz; "
                + "bir sonraki güncellemede listeden çıkarılır.", TEXT, 13);

        p(body, "\"DİJİTAL REKLAM KUŞAĞINDASIN\" yazısı:", ACCENT, 13);
        p(body, "Bu, yayıncının kendi reklam aralığıdır ve yayının içine gömülüdür; "
                + "uygulama bu kareyi atlayamaz. Ekranda \"Yayıncı reklam aralığında olabilir · yayın dönünce "
                + "kendiliğinden gelecek\" bilgisi çıkar. Genellikle 1–5 dakika içinde yayın döner; dönmezse "
                + "⏭ ile başka kanala geçip sonra geri gel. Bu bir arıza değildir.", TEXT, 13);

        p(body, "Yayın donuyor / ses var görüntü yok:", ACCENT, 13);
        p(body, "Kaliteyi düşür (Ayarlar → Yayın kalitesi → 360p veya Ekonomi modu). Yavaş bağlantıda "
                + "otomatik yeniden bağlanma devreye girer. Sorun sürerse uygulamayı kapatıp aç.", TEXT, 13);

        p(body, "Reklam çıkmıyor / aynı reklam geliyor:", ACCENT, 13);
        p(body, "Hangi reklamın gösterileceğine Google AdMob karar verir; uygulama seçemez. Reklam "
                + "yüklenemezse alt barda kendi \"Reklam izle · 15 dk reklamsız\" şeridimiz görünür. "
                + "Aynı reklamın sürekli gelmesi reklamverenin bütçesiyle ilgilidir; zamanla değişir.", TEXT, 13);

        p(body, "Bildirim gelmiyor (radyo arka planda):", ACCENT, 13);
        p(body, "Android 13+ için ilk açılışta bildirim izni istenir. Reddettiysen: telefon Ayarlar → "
                + "Uygulamalar → UMAY Multi TV → Bildirimler → izin ver.", TEXT, 13);

        p(body, "İnternet çok harcıyor:", ACCENT, 13);
        p(body, "360p yayın saatte yaklaşık 300 MB kullanır; bu normaldir. Daha az istiyorsan Ayarlar → "
                + "Ekonomi modu (en düşük kalite + kapakta ses) veya \"Mobil veride sadece ses\" seçeneğini aç.", TEXT, 13);

        h(body, "Gizlilik ve reklamlar");
        p(body, "Uygulama hesap açmaz, ad/e-posta istemez, yayın izleme geçmişini sunucuya göndermez. "
                + "Tercihlerin (favoriler, kalite, sayaçlar) yalnız telefonunda saklanır. Reklamlar Google "
                + "AdMob üzerinden gösterilir; AdMob kendi politikasına göre cihaz reklam kimliğini "
                + "kullanabilir. AB/İngiltere/İsviçre'de ilk açılışta rıza ekranı çıkar; kabul etmezsen "
                + "kişiselleştirilmiş reklam gösterilmez. Uygulama 13 yaş altına yönelik değildir.", TEXT, 13);
        p(body, "Hata günlüğü: uygulama hangi kanalın açılmadığını, bağlantının ne zaman koptuğunu "
                + "kendi telefonunda tutar. İstersen Ayarlar → Hata günlüğü ekranından bu kaydı bana "
                + "e-postayla gönderebilir ya da \"günlük gönderimi\" seçeneğini AÇABİLİRSİN. Bu seçenek "
                + "varsayılan olarak KAPALIDIR; açtığında günde en fazla bir kez, yalnızca teknik hata "
                + "kayıtları (kanal adı, hata kodu, telefon modeli) sunucuya gönderilir. Ad, e-posta, "
                + "konum veya izleme geçmişi gönderilmez; istediğin an kapatabilirsin.", TEXT, 13);

        h(body, "Telif ve yayın kaynakları");
        p(body, "Kanal yayınları ilgili yayıncı kuruluşlara aittir. Uygulama yayınları yeniden yayınlamaz; "
                + "yalnızca yayıncıların herkese açık olarak yayınladığı internet adreslerini oynatır. "
                + "Bir yayının kaldırılmasını istiyorsan aşağıdaki e-posta ile yaz; talebin 24–48 saat "
                + "içinde değerlendirilir.", TEXT, 13);

        h(body, "Bize ulaş");
        p(body, "Öneri, şikâyet, hata bildirimi ve telif talepleri için doğrudan yazabilirsin. "
                + "Mesajında telefon modelini ve hangi kanalda sorun olduğunu yazarsan daha hızlı çözeriz.", TEXT, 13);

        Button mail = new Button(this);
        mail.setText(I18n.t(this, "✉  E-posta gönder\n") + MAIL);
        mail.setAllCaps(false);
        mail.setTextColor(Color.parseColor("#0B1220"));
        GradientDrawable mbg = new GradientDrawable();
        mbg.setColor(ACCENT);
        mbg.setCornerRadius(dp(14));
        mail.setBackground(mbg);
        mail.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    i.setData(Uri.parse("mailto:" + MAIL));
                    i.putExtra(Intent.EXTRA_SUBJECT, "UMAY Multi TV · geri bildirim");
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(AboutActivity.this, MAIL, Toast.LENGTH_LONG).show();
                }
            }
        });
        body.addView(mail, lp());

        Button wa = new Button(this);
        wa.setAllCaps(false);
        wa.setTextColor(TEXT);
        GradientDrawable wbg = new GradientDrawable();
        wbg.setColor(CARD);
        wbg.setCornerRadius(dp(14));
        wa.setBackground(wbg);
        {
            if (WHATSAPP == null || WHATSAPP.length() < 10) {
                wa.setText(I18n.t(this, "💬  WhatsApp (numara henüz eklenmedi)\n"
                        + "Şimdilik e-posta kullanabilirsin"));
                wa.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        Toast.makeText(AboutActivity.this,
                                I18n.t(AboutActivity.this,
                                        "WhatsApp numarası eklendiğinde buradan tek dokunuşla yazabileceksin."),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                wa.setText(I18n.t(this, "💬  WhatsApp'tan yaz"));
                wa.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/" + WHATSAPP)));
                        } catch (Exception e) {
                            Toast.makeText(AboutActivity.this,
                                    I18n.t(AboutActivity.this, "WhatsApp kurulu değil"), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        body.addView(wa, lp());

        Button close2 = new Button(this);
        close2.setText(I18n.t(this, "Kapat"));
        close2.setAllCaps(false);
        close2.setTextColor(TEXT);
        GradientDrawable c2 = new GradientDrawable();
        c2.setColor(CARD);
        c2.setCornerRadius(dp(14));
        close2.setBackground(c2);
        close2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        body.addView(close2, lp());

        AdManager.wrap(this, scroll);
    }

    /**
     * v10.04 (Paket C-3): Türkçe dışındaki diller için çevrilmiş tanıtım gövdesi.
     * Metinler assets/i18n.tsv'den gelir; anahtar Türkçe cümledir.
     */
    private void worldBody(LinearLayout body) {
        h(body, I18n.t(this, "Bu program ne yapar?"));
        p(body, I18n.t(this, "UMAY Multi TV, internet üzerinden canlı televizyon ve radyo izlemek için yapılmış bir Android uygulamasıdır. Üyelik istemez, kredi kartı istemez; açtığın anda yayın başlar."), TEXT, 13);

        h(body, I18n.t(this, "Ülke ve dil"));
        p(body, I18n.t(this, "Ülke listesi sunucudan indirilir ve telefonda saklanır; ilk açılışta birkaç saniye sürebilir. Aynı anda tek ülkenin listesi yüklenir; ülke değişince liste değişir. Favoriler ve kendi eklediğin kanallar silinmez. Türk kanalları için Ayarlar → Ülke → Türkiye seç."), TEXT, 13);

        h(body, I18n.t(this, "Öne çıkan özellikler"));
        bullet(body, I18n.t(this, "Canlı TV: tek dokunuşla yayın, kalite seçimi (240p–1080p), 360p varsayılan."));
        bullet(body, I18n.t(this, "Radyo: ekran kapalıyken çalar, kilit ekranından durdur/kanal değiştir."));
        bullet(body, I18n.t(this, "3'lü izleme: aynı ekranda üç kanalı birden aç. Kayıt: izlediğin yayını telefona kaydet."));
        bullet(body, I18n.t(this, "Favoriler, kanal düzenleme, numara değiştirme, kendi kanalını/M3U dosyanı ekleme. Veri sayacı ve ekonomi modu."));

        h(body, I18n.t(this, "Kanal açılmıyor mu?"));
        p(body, I18n.t(this, "İnternet yayınları bazen durur: reklam kuşağı, adres (IP) değişikliği veya kanalın kapanması. 10-20 saniye bekleyip yeniden dene, başka kanal seç veya Ayarlar → Ölü kanalları onar. Bu bir arıza değildir."), TEXT, 13);

        h(body, I18n.t(this, "Gizlilik ve reklamlar"));
        p(body, I18n.t(this, "Uygulama hesap açmaz, ad/e-posta istemez, izleme geçmişini sunucuya göndermez. Tercihler yalnız telefonunda saklanır. Reklamlar Google AdMob üzerinden gösterilir; AB/İngiltere/İsviçre'de ilk açılışta rıza ekranı çıkar. Uygulama 13 yaş altına yönelik değildir."), TEXT, 13);

        h(body, I18n.t(this, "Telif ve yayın kaynakları"));
        p(body, I18n.t(this, "Kanal yayınları ilgili yayıncı kuruluşlara aittir. Uygulama yayınları yeniden yayınlamaz; yalnızca yayıncıların herkese açık internet adreslerini oynatır. Kaldırılmasını istiyorsan e-posta ile yaz; 24–48 saat içinde değerlendirilir."), TEXT, 13);

        h(body, I18n.t(this, "Bize ulaş"));
        p(body, I18n.t(this, "Öneri, şikâyet, hata bildirimi ve telif talepleri için doğrudan yazabilirsin. Telefon modelini ve hangi kanalda sorun olduğunu yazarsan daha hızlı çözeriz."), TEXT, 13);
    }

    /** v10.04 (Paket C-3): çevrilmiş gövdenin altındaki iletişim düğmeleri. */
    private void addButtons(LinearLayout body) {
        Button mail = new Button(this);
        mail.setText(I18n.t(this, "✉  E-posta gönder\n") + MAIL);
        mail.setAllCaps(false);
        mail.setTextColor(Color.parseColor("#0B1220"));
        GradientDrawable mbg = new GradientDrawable();
        mbg.setColor(ACCENT);
        mbg.setCornerRadius(dp(14));
        mail.setBackground(mbg);
        mail.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    i.setData(Uri.parse("mailto:" + MAIL));
                    i.putExtra(Intent.EXTRA_SUBJECT, "UMAY Multi TV · geri bildirim");
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(AboutActivity.this, MAIL, Toast.LENGTH_LONG).show();
                }
            }
        });
        body.addView(mail, lp());

        Button close = new Button(this);
        close.setText(I18n.t(this, "Kapat"));
        close.setAllCaps(false);
        close.setTextColor(TEXT);
        GradientDrawable c2 = new GradientDrawable();
        c2.setColor(CARD);
        c2.setCornerRadius(dp(14));
        close.setBackground(c2);
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        body.addView(close, lp());
    }

    private LinearLayout.LayoutParams lp() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(10), 0, 0);
        return p;
    }

    private void h(LinearLayout b, String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(ACCENT);
        t.setTextSize(15);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setPadding(0, dp(16), 0, dp(4));
        b.addView(t);
    }

    private void p(LinearLayout b, String text, int color, int size) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setLineSpacing(dp(3), 1f);
        t.setPadding(0, dp(4), 0, dp(4));
        b.addView(t);
    }

    private void bullet(LinearLayout b, String text) {
        TextView t = new TextView(this);
        t.setText("•  " + text);
        t.setTextColor(TEXT);
        t.setTextSize(13);
        t.setLineSpacing(dp(2), 1f);
        t.setPadding(dp(6), dp(3), 0, dp(3));
        b.addView(t);
    }

    private String version() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return BuildConfig.VERSION_NAME;
        }
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
