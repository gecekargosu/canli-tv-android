package com.cengiz.canlitv;

import android.content.Context;

/**
 * Arayuz dili. Su an ana ekran, sekme adlari, ulke ekrani ve ayarlar satirlari cevriliyor.
 * Yeni bir metin eklerken: Lang.t(...) kullan, Java icine sabit yazma.
 */
public class Lang {

    public static final String[] CODES = {"tr", "en", "ru", "ar"};

    public static String name(String code) {
        if ("en".equals(code)) return "English";
        if ("ru".equals(code)) return "Русский";
        if ("ar".equals(code)) return "العربية";
        return "Türkçe";
    }

    private static String pick(Context c, String tr, String en, String ru, String ar) {
        String l = Prefs.uiLang(c);
        if ("en".equals(l)) return en;
        if ("ru".equals(l)) return ru;
        if ("ar".equals(l)) return ar;
        return tr;
    }

    public static String t(Context c, String key) {
        if ("search".equals(key)) return pick(c, "Kanal ara...", "Search channels...", "Поиск каналов...", "ابحث عن قناة...");
        if ("edit".equals(key)) return pick(c, "✏️ Düzenle", "✏️ Edit", "✏️ Изменить", "✏️ تعديل");
        if ("sort".equals(key)) return pick(c, "⇅ Sırala", "⇅ Sort", "⇅ Сортировать", "⇅ ترتيب");
        if ("tools_hint".equals(key)) return pick(c, "taşı · sil · numara değiştir", "move · delete · renumber", "переместить · удалить · номер", "نقل · حذف · تغيير الرقم");
        if ("settings".equals(key)) return pick(c, "Ayarlar", "Settings", "Настройки", "الإعدادات");
        if ("country".equals(key)) return pick(c, "Ülke", "Country", "Страна", "الدولة");
        if ("language".equals(key)) return pick(c, "Dil", "Language", "Язык", "اللغة");
        if ("countries_title".equals(key)) return pick(c, "Ülke seç", "Choose a country", "Выберите страну", "اختر الدولة");
        if ("countries_sub".equals(key)) return pick(c, "Seçtiğin ülkenin kanalları listeye gelir", "Channels of the selected country load into the list", "Каналы выбранной страны появятся в списке", "ستظهر قنوات الدولة المختارة في القائمة");
        if ("downloading".equals(key)) return pick(c, "Kanallar indiriliyor...", "Downloading channels...", "Загрузка каналов...", "جارٍ تنزيل القنوات...");
        if ("download_failed".equals(key)) return pick(c, "İndirilemedi. İnterneti kontrol edip tekrar dene.", "Download failed. Check your connection and try again.", "Не удалось загрузить. Проверьте интернет.", "تعذّر التنزيل. تحقّق من الإنترنت.");
        if ("ready".equals(key)) return pick(c, "hazır", "ready", "готово", "جاهز");
        if ("download".equals(key)) return pick(c, "indir", "download", "скачать", "تنزيل");
        if ("reloading".equals(key)) return pick(c, "Kanal listesi yenilendi", "Channel list refreshed", "Список каналов обновлён", "تم تحديث قائمة القنوات");
        if ("refresh_list".equals(key)) return pick(c, "Kanalları yenile", "Refresh channels", "Обновить каналы", "تحديث القنوات");
        if ("country_sub".equals(key)) return pick(c, "Kanalların geldiği ülke", "Country the channels come from", "Страна каналов", "دولة القنوات");
        if ("app_language".equals(key)) return pick(c, "Arayüz dili", "App language", "Язык интерфейса", "لغة الواجهة");
        if ("back".equals(key)) return pick(c, "‹ Geri", "‹ Back", "‹ Назад", "‹ رجوع");
        if ("general".equals(key)) return pick(c, "Genel", "General", "Общие", "عام");

        // --- hesap / uyelik ---
        if ("account".equals(key)) return pick(c, "Hesap", "Account", "Аккаунт", "الحساب");
        if ("account_sub".equals(key)) return pick(c, "İsteğe bağlı üyelik · misafir kullanım açık", "Optional account · guest mode always available", "Аккаунт по желанию · гость всегда доступен", "حساب اختياري · الوضع الزائري متاح");
        if ("account_why".equals(key)) return pick(c, "Üye olursan favorilerin ve ayarların hesabına bağlanır, yeni bölümlerden ilk sen haberdar olursun. Zorunlu değil.", "With an account your favourites and settings are tied to it and you hear about new features first. Not required.", "С аккаунтом избранное и настройки привязаны к нему, а о новинках узнаёте первыми. Необязательно.", "بحساب تُربط مفضّلتك وإعداداتك به وتعلم بالميزات الجديدة أولًا. غير إلزامي.");
        if ("account_intro".equals(key)) return pick(c, "Hesabın yoksa misafir olarak devam edebilirsin: kanallar, kayıt ve favoriler aynen çalışır.", "No account? Continue as a guest: channels, recording and favourites work the same.", "Нет аккаунта? Продолжайте гостем: каналы, запись и избранное работают так же.", "لا حساب؟ تابع كزائر: القنوات والتسجيل والمفضلة تعمل كما هي.");
        if ("signin".equals(key)) return pick(c, "Giriş yap", "Sign in", "Войти", "تسجيل الدخول");
        if ("signup".equals(key)) return pick(c, "Üye ol", "Create account", "Регистрация", "إنشاء حساب");
        if ("fullname".equals(key)) return pick(c, "Ad Soyad", "Full name", "Имя и фамилия", "الاسم الكامل");
        if ("email".equals(key)) return pick(c, "E-posta", "E-mail", "Эл. почта", "البريد الإلكتروني");
        if ("password".equals(key)) return pick(c, "Şifre (en az 6 karakter)", "Password (min 6 characters)", "Пароль (мин. 6 символов)", "كلمة المرور (٦ أحرف على الأقل)");
        if ("pass_hint".equals(key)) return pick(c, "Şifren cihazda PBKDF2 ile özetlenir; düz metin saklanmaz.", "Your password is hashed with PBKDF2 on the device; plain text is never stored.", "Пароль хешируется PBKDF2 на устройстве; открытый текст не хранится.", "تُشفّر كلمة المرور بـ PBKDF2 على الجهاز؛ لا يُحفظ النص الصريح.");
        if ("forgot_hint".equals(key)) return pick(c, "Şifreni mi unuttun? Yeni bir hesapla devam edebilirsin; misafir modu her zaman açık.", "Forgot your password? You can continue with a new account; guest mode is always available.", "Забыли пароль? Можно продолжить с новым аккаунтом; режим гостя всегда доступен.", "نسيت كلمة المرور؟ يمكنك المتابعة بحساب جديد؛ وضع الزائر متاح دائمًا.");
        if ("verified".equals(key)) return pick(c, "E-posta doğrulandı", "E-mail verified", "Почта подтверждена", "تم تأكيد البريد");
        if ("unverified".equals(key)) return pick(c, "E-posta doğrulanmadı", "E-mail not verified", "Почта не подтверждена", "البريد غير مؤكد");
        if ("verified_note".equals(key)) return pick(c, "Doğrulama isteğe bağlıdır; uygulamanın tüm özellikleri doğrulanmadan da açık.", "Verification is optional; every feature works without it.", "Подтверждение необязательно; все функции доступны и без него.", "التأكيد اختياري؛ كل الميزات تعمل بدونه.");
        if ("signout".equals(key)) return pick(c, "Çıkış yap", "Sign out", "Выйти", "تسجيل الخروج");
        if ("signout_q".equals(key)) return pick(c, "Hesaptan çıkılsın mı? Favoriler ve ayarlar bu telefonda kalır.", "Sign out of the account? Favourites and settings stay on this phone.", "Выйти из аккаунта? Избранное и настройки останутся.", "تسجيل الخروج؟ ستبقى المفضلة والإعدادات على هذا الهاتف.");
        if ("cancel".equals(key)) return pick(c, "Vazgeç", "Cancel", "Отмена", "إلغاء");
        if ("google".equals(key)) return pick(c, "Google ile devam et", "Continue with Google", "Продолжить с Google", "المتابعة عبر Google");
        if ("have_code".equals(key)) return pick(c, "Google'dan aldığım kodla gireceğim", "I have a code from Google", "У меня есть код от Google", "لدي رمز من Google");
        if ("no_browser".equals(key)) return pick(c, "Tarayıcı açılamadı.", "Could not open a browser.", "Не удалось открыть браузер.", "تعذّر فتح المتصفح.");
        if ("guest_go".equals(key)) return pick(c, "Şimdi değil, misafir olarak devam et", "Not now, continue as guest", "Не сейчас, продолжить гостем", "ليس الآن، المتابعة كزائر");
        if ("guest_short".equals(key)) return pick(c, "Misafir", "Guest", "Гость", "زائر");
        if ("code_title".equals(key)) return pick(c, "E-postana gelen kodu gir", "Enter the code sent to your e-mail", "Введите код из письма", "أدخل الرمز المُرسل إلى بريدك");
        if ("code_sub".equals(key)) return pick(c, "6 haneli kod şu adrese gitti:", "6-digit code sent to:", "6-значный код отправлен на:", "الرمز المكوّن من ٦ أرقام أُرسل إلى:");
        if ("code".equals(key)) return pick(c, "Kod (6 hane)", "Code (6 digits)", "Код (6 цифр)", "الرمز (٦ أرقام)");
        if ("verify".equals(key)) return pick(c, "✔ Doğrula", "✔ Verify", "✔ Подтвердить", "✔ تأكيد");
        if ("resend".equals(key)) return pick(c, "Tekrar gönder", "Send again", "Отправить снова", "إعادة الإرسال");
        if ("back_to_form".equals(key)) return pick(c, "‹ Forma dön", "‹ Back to form", "‹ К форме", "‹ العودة للنموذج");
        if ("working".equals(key)) return pick(c, "İşleniyor…", "Working…", "Обработка…", "جارٍ المعالجة…");
        if ("err_email".equals(key)) return pick(c, "Geçerli bir e-posta yaz.", "Enter a valid e-mail.", "Введите корректную почту.", "أدخل بريدًا صحيحًا.");
        if ("err_pass".equals(key)) return pick(c, "Şifre en az 6 karakter olmalı.", "Password must be at least 6 characters.", "Пароль не короче 6 символов.", "كلمة المرور ٦ أحرف على الأقل.");
        if ("err_code".equals(key)) return pick(c, "Kod hatalı veya süresi geçmiş.", "Wrong or expired code.", "Неверный или истёкший код.", "رمز خاطئ أو منتهي.");
        if ("err_generic".equals(key)) return pick(c, "İşlem tamamlanamadı, sonra tekrar dene.", "Could not complete, try again later.", "Не удалось, попробуйте позже.", "تعذّر الإتمام، حاول لاحقًا.");
        if ("err_login".equals(key)) return pick(c, "E-posta veya şifre hatalı.", "Wrong e-mail or password.", "Неверная почта или пароль.", "البريد أو كلمة المرور خطأ.");
        if ("code_sent".equals(key)) return pick(c, "Kod e-postana gönderildi.", "Code sent to your e-mail.", "Код отправлен на почту.", "أُرسل الرمز إلى بريدك.");
        if ("offline_saved".equals(key)) return pick(c, "Sunucuya ulaşılamadı: hesap bu telefonda oluşturuldu, internet gelince doğrula.", "Server unreachable: account created on this phone, verify once you are online.", "Сервер недоступен: аккаунт создан на телефоне, подтвердите при связи.", "تعذّر الوصول للخادم: أُنشئ الحساب على هذا الهاتف، أكّده عند الاتصال.");
        if ("offline_login".equals(key)) return pick(c, "Çevrimdışı giriş yapıldı.", "Signed in offline.", "Вход выполнен офлайн.", "تم الدخول دون اتصال.");
        if ("offline_only".equals(key)) return pick(c, "Sunucuya ulaşılamadı ve bu telefonda kayıtlı bir hesap yok.", "Server unreachable and no account stored on this phone.", "Сервер недоступен, аккаунта на телефоне нет.", "تعذّر الوصول ولا يوجد حساب محفوظ على هذا الهاتف.");
        if ("login_ok".equals(key)) return pick(c, "Giriş yapıldı", "Signed in", "Вход выполнен", "تم تسجيل الدخول");
        if ("verified_thanks".equals(key)) return pick(c, "E-posta doğrulandı, teşekkürler!", "E-mail verified, thank you!", "Почта подтверждена, спасибо!", "تم تأكيد البريد، شكرًا!");
        return key;
    }

    /** Alt baslik: "305 kanal · no ile aç, 3'lü izle, kayıt" */
    public static String subtitle(Context c, int count) {
        String l = Prefs.uiLang(c);
        if ("en".equals(l)) return count + " channels · open by number, watch 3 at once, record";
        if ("ru".equals(l)) return count + " каналов · по номеру, 3 экрана, запись";
        if ("ar".equals(l)) return count + " قناة · بالرقم، ٣ شاشات، تسجيل";
        return count + " kanal · no ile aç, 3'lü izle, kayıt";
    }

    /** Sekme etiketi. Sekme ici anahtar Turkce sabit kalir (mantik onunla calisiyor), sadece gorunen ad cevrilir. */
    public static String tab(Context c, String key) {
        if ("Tümü".equals(key)) return pick(c, "Tümü", "All", "Все", "الكل");
        if ("★ Favoriler".equals(key)) return pick(c, "★ Favoriler", "★ Favourites", "★ Избранное", "★ المفضلة");
        if ("🕘 Son".equals(key)) return pick(c, "🕘 Son", "🕘 Recent", "🕘 Недавние", "🕘 الأخيرة");
        if ("🗑 Gizlenenler".equals(key)) return pick(c, "🗑 Gizlenenler", "🗑 Hidden", "🗑 Скрытые", "🗑 المخفية");
        if ("🇹🇷 Türkçe".equals(key)) return pick(c, "🇹🇷 Türkçe", "🇹🇷 Turkish", "🇹🇷 Турецкие", "🇹🇷 تركية");
        if ("🌐 Yabancı".equals(key)) return pick(c, "🌐 Yabancı", "🌐 Foreign", "🌐 Зарубежные", "🌐 أجنبية");
        return key;
    }
}
