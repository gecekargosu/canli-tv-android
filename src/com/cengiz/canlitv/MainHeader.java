package com.cengiz.canlitv;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * v10.08 (teknik borç): ana ekranın üst çubuğu (marka kutusu + 6 ikon kutusu)
 * MainActivity'den çıkarıldı. Tıklamalar Host üzerinden ana ekrana gider.
 */
final class MainHeader {

    /** Üst çubuktaki düğmelerin eylemleri. */
    interface Host {
        void headerMulti();
        void headerMultiLong();
        void headerRadio();
        void headerCountry();
        void headerRecordings();
        void headerSettings();
        void headerGift();
    }

    private MainHeader() { }

    static LinearLayout build(Activity a, final MainUi ui, String countryFlag,
                              int sidePad, final Host host) {
        // --- baslik: 3 sabit satir, hepsi ayni 12dp kenardan baslar/biter ---
        LinearLayout head = new LinearLayout(a);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setPadding(ui.dp(12) + sidePad, ui.dp(8), ui.dp(12) + sidePad, 0);

        // 1. satir: marka TEK kutunun icinde. Logo 40dp ve hep tam parlak; solmuyor.
        // Arkasindaki turuncu isik nabiz gibi atiyor, parlayan logo gibi duruyor.
        LinearLayout brandCard = new LinearLayout(a);
        brandCard.setOrientation(LinearLayout.HORIZONTAL);
        brandCard.setGravity(Gravity.CENTER_VERTICAL);
        brandCard.setPadding(ui.dp(8), ui.dp(6), ui.dp(12), ui.dp(6));
        brandCard.setBackground(ui.rounded(MainUi.CARD, 18));
        brandCard.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ui.dp(56)));
        head.addView(brandCard);

        // Logo kutusu 48dp: turuncu ışık logonun çevresinden 5dp taşar, böylece
        // "parlıyor" net görünür (önce ışık logonun tam arkasında kalıp kayboluyordu).
        FrameLayout brandBox = new FrameLayout(a);
        LinearLayout.LayoutParams bbp = new LinearLayout.LayoutParams(ui.dp(48), ui.dp(48));
        bbp.setMargins(0, 0, ui.dp(10), 0);
        brandBox.setLayoutParams(bbp);

        // Halka: içi boş, 2dp parlak turuncu çerçeve. Dolu hâli logonun arkasında
        // kahverengi leke gibi duruyordu; halka gerçek bir ışık gibi görünüyor.
        final View halo = new View(a);
        android.graphics.drawable.GradientDrawable ringBg =
                new android.graphics.drawable.GradientDrawable();
        ringBg.setCornerRadius(ui.dp(16));
        ringBg.setColor(Color.TRANSPARENT);
        ringBg.setStroke(ui.dp(2), Color.parseColor("#FF9A3C"));
        halo.setBackground(ringBg);
        halo.setAlpha(0.95f);
        brandBox.addView(halo, new FrameLayout.LayoutParams(ui.dp(46), ui.dp(46), Gravity.CENTER));

        ImageView brand = new ImageView(a);
        brand.setLayoutParams(new FrameLayout.LayoutParams(ui.dp(38), ui.dp(38), Gravity.CENTER));
        brand.setScaleType(ImageView.ScaleType.FIT_CENTER);
        try {
            brand.setImageDrawable(a.getPackageManager().getApplicationIcon(a.getPackageName()));
        } catch (Exception ignored) { }
        brandBox.addView(brand);
        brandCard.addView(brandBox);

        // Sadece isik nabiz atiyor: alfa 0,50 ↔ 0,14 ve hafif buyuyup kuculuyor.
        // Logonun kendisi hep tam parlak (eskiden soluyordu, gri gorunuyordu).
        try {
            android.animation.ObjectAnimator glow = android.animation.ObjectAnimator
                    .ofFloat(halo, "alpha", 0.95f, 0.3f);
            glow.setDuration(1300);
            glow.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            glow.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            glow.start();
            android.animation.ObjectAnimator gx = android.animation.ObjectAnimator
                    .ofFloat(halo, "scaleX", 1.02f, 0.96f);
            gx.setDuration(1400);
            gx.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            gx.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            gx.start();
            android.animation.ObjectAnimator gy = android.animation.ObjectAnimator
                    .ofFloat(halo, "scaleY", 1.02f, 0.96f);
            gy.setDuration(1400);
            gy.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            gy.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            gy.start();
        } catch (Throwable ignored) { }

        TextView title = new TextView(a);
        // 9.81: iki nokta kaldırıldı; başlık "UMAY CANLI MULTI TV VE RADYO".
        // Yazı, kutuya sığan en büyük puntoya kendisi büyür (12–22sp).
        title.setText(I18n.t(a, "UMAY CANLI MULTI TV VE RADYO"));
        title.setTextColor(MainUi.TEXT);
        // 18sp büyük harf: kart genişliğine tam sığıyor (autosize bu cihazda küçültmüyordu).
        title.setTextSize(18);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        brandCard.addView(title, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        // 2. satir: 6 ikon kutusu, 44dp. 3. satirla ayni kenarlardan baslar/biter.
        LinearLayout actionRow = new LinearLayout(a);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams arlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ui.dp(44));
        arlp.topMargin = ui.dp(6);
        actionRow.setLayoutParams(arlp);
        head.addView(actionRow);

        // 9.80: yazı yerine ikon — kutular 44dp, ikon 28sp (istedigi gibi).
        // 9.82: buton seçili değil — turuncu "seçili" gibi duruyordu, diğerleriyle aynı renk.
        TextView multi = ui.pill("▦", MainUi.SOFT);
        multi.setContentDescription(I18n.t(a, "3'lü izle"));
        multi.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { host.headerMulti(); }
        });
        multi.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                host.headerMultiLong();
                return true;
            }
        });
        TvFocus.attach(ui, multi, MainUi.SOFT, 16);
        actionRow.addView(multi, ui.slot(0));

        TextView radio = ui.pill("📻", MainUi.SOFT);
        // v10.11: etiket sabit "Radyo" idi; İngilizce arayüzde TalkBack Türkçe okuyordu.
        radio.setContentDescription(I18n.t(a, "Radyo"));
        radio.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { host.headerRadio(); }
        });
        TvFocus.attach(ui, radio, MainUi.SOFT, 16);
        actionRow.addView(radio, ui.slot(1));

        // --- ulke secimi: secilen ulkenin kanallari listeye gelir ---
        TextView countryBtn = ui.pill(countryFlag, MainUi.SOFT);
        // Bayrak emojisi aynı punto ile daha küçük çiziliyor; diğer ikonlarla eşitlensin.
        countryBtn.setTextSize(40);
        countryBtn.setContentDescription(I18n.t(a, "Ülke seç"));
        countryBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { host.headerCountry(); }
        });
        TvFocus.attach(ui, countryBtn, MainUi.SOFT, 16);
        actionRow.addView(countryBtn, ui.slot(2));

        // 9.78: kayıt düğmesi eksikti (alt başlıkta "kayıt" yazıyor ama giriş yoktu).
        TextView recordings = ui.pill("⏺", MainUi.SOFT);
        recordings.setContentDescription(I18n.t(a, "Kayıtlarım"));
        recordings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { host.headerRecordings(); }
        });
        TvFocus.attach(ui, recordings, MainUi.SOFT, 16);
        actionRow.addView(recordings, ui.slot(3));

        TextView settings = ui.pill("⚙", MainUi.SOFT);
        settings.setContentDescription(I18n.t(a, "Ayarlar"));
        settings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { host.headerSettings(); }
        });
        TvFocus.attach(ui, settings, MainUi.SOFT, 16);
        actionRow.addView(settings, ui.slot(4));

        // Ödüllü reklam her zaman görünür bir giriş: 15 dk reklamsız.
        // Eskiden yalnız banner dolmadığında alt şeritte çıkıyordu; kullanıcı bulamıyordu.
        TextView gift = ui.pill("🎁", MainUi.SOFT);
        gift.setContentDescription(I18n.t(a, "Ödüllü reklam: 15 dakika reklamsız"));
        gift.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { host.headerGift(); }
        });
        TvFocus.attach(ui, gift, MainUi.SOFT, 16);
        actionRow.addView(gift, ui.slot(5));

        return head;
    }
}
