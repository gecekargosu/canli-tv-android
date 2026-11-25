package com.cengiz.canlitv;

import android.content.Context;

/**
 * Arayuz dili. Su an ana ekran, sekme adlari, ulke ekrani ve ayarlar satirlari cevriliyor.
 * Yeni bir metin eklerken: Lang.t(...) kullan, Java icine sabit yazma.
 */
public class Lang {

    public static final String[] CODES = {"tr", "en", "ru", "ar"};

    /**
     * Arama için Türkçe karakterleri sadeleştir: "super" yazınca "Süper FM" bulunur.
     * (ı→i, İ→i, ş→s, ğ→g, ü→u, ö→o, ç→c)
     */
    public static String fold(String s) {
        if (s == null) return "";
        String t = s.toLowerCase(java.util.Locale.ROOT);
        t = t.replace('ı', 'i').replace('i', 'i')
             .replace('ş', 's').replace('ğ', 'g')
             .replace('ü', 'u').replace('ö', 'o').replace('ç', 'c')
             .replace('â', 'a').replace('î', 'i').replace('û', 'u');
        return t;
    }

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
        if ("channels".equals(key)) return pick(c, "kanal", "channels", "каналов", "قناة");
        if ("soon_no_list".equals(key)) return pick(c, "yakında · liste henüz yok", "coming soon · no list yet", "скоро · списка пока нет", "قريبًا · لا توجد قائمة بعد");
        if ("no_list_toast".equals(key)) return pick(c, "Bu ülke için liste henüz yok; eklendiğinde burada indir olarak görünecek.", "No list for this country yet; it will show download here once added.", "Для этой страны списка пока нет; он появится здесь.", "لا توجد قائمة لهذه الدولة بعد؛ ستظهر هنا قريبًا.");
        if ("info_title".equals(key)) return pick(c, "Kanal açılmıyor mu?", "A channel not opening?", "Канал не открывается?", "القناة لا تفتح؟");
        if ("info_short".equals(key)) return pick(c,
                "İnternet kanalları bazen durur: reklam kuşağı, yayın adresinin (IP) değişmesi veya kanalın kapanması. Bu, uygulamanın bozuk olduğu anlamına gelmez. Dokun → nedeni ve çözümü.",
                "Internet channels sometimes stop: ad break, a changed stream address (IP) or the channel closing. That does not mean the app is broken. Tap → reason and fix.",
                "Интернет-каналы иногда останавливаются: реклама, смена адреса (IP) или закрытие канала. Это не значит, что приложение сломано. Нажмите → причина и решение.",
                "قنوات الإنترنت تتوقف أحيانًا: فاصل إعلاني أو تغيّر عنوان البث (IP) أو إغلاق القناة. هذا لا يعني أن التطبيق معطّل. اضغط ← السبب والحل.");
        if ("info_full".equals(key)) return pick(c,
                "Bunlar normaldir; uygulama bozuk değildir:\n• Reklam kuşağı: yayın 10-60 saniye donabilir, sonra devam eder.\n• Adres (IP) değişikliği: kanal yayın sunucusunu değiştirince bağlantı kopar; uygulama yeniden bağlanmayı dener.\n• Kanal kapanması: bazı yayınlar tamamen biter, liste eskir.\nÇözüm: 10-20 saniye bekleyip kanalı yeniden aç, başka bir kanal dene veya Ayarlar → Ölü kanalları onar ile adresleri güncelle. Ülke listeleri sunucudan düzenli yenilenir; yeni kanallar kendiliğinden gelir.",
                "This is normal; the app is not broken:\n• Ad break: the stream can freeze for 10-60 seconds, then continues.\n• Address (IP) change: when a channel switches servers the connection drops; the app retries.\n• Channel closure: some streams end for good and the list gets stale.\nFix: wait 10-20 seconds and reopen, try another channel, or use Settings → Repair dead channels to refresh addresses. Country lists refresh from the server, so new channels appear by themselves.",
                "Это нормально, приложение не сломано:\n• Реклама: поток может замереть на 10-60 секунд, затем продолжится.\n• Смена адреса (IP): при переключении сервера связь обрывается, приложение переподключается.\n• Закрытие канала: некоторые потоки прекращаются навсегда, список устаревает.\nРешение: подождите 10-20 секунд и откройте снова, выберите другой канал или Настройки → Починить мёртвые каналы. Списки стран обновляются с сервера, новые каналы появляются сами.",
                "هذا طبيعي، التطبيق غير معطّل:\n• فاصل إعلاني: قد يتوقف البث 10-60 ثانية ثم يكمل.\n• تغيّر العنوان (IP): عند تبديل الخادم ينقطع الاتصال ويعيد التطبيق المحاولة.\n• إغلاق القناة: بعض البثوث تنتهي نهائيًا وتصبح القائمة قديمة.\nالحل: انتظر 10-20 ثانية وأعد الفتح، أو جرّب قناة أخرى، أو الإعدادات ← إصلاح القنوات الميتة. قوائم الدول تُحدَّث من الخادم فتظهر القنوات الجديدة تلقائيًا.");
        if ("general".equals(key)) return pick(c, "Genel", "General", "Общие", "عام");

        // --- oynatıcı / çoklu ekran (9.88 / K-10) ---
        if ("tv_no_net".equals(key)) return pick(c, "İnternet bağlantısı yok.\n\nBağlanınca dokun → tekrar dene", "No internet connection.\n\nTap to retry when you are online", "Нет интернета.\n\nПодключитесь и нажмите, чтобы повторить", "لا يوجد اتصال بالإنترنت.\n\nاضغط لإعادة المحاولة بعد الاتصال");
        if ("tv_net_fail".equals(key)) return pick(c, "İnternet bağlantısı kurulamadı.\n\nWi-Fi / mobil veriyi kontrol et,\nsonra dokun → tekrar dene", "Could not connect.\n\nCheck Wi-Fi / mobile data,\nthen tap to retry", "Не удалось подключиться.\n\nПроверьте Wi-Fi / мобильные данные\nи нажмите, чтобы повторить", "تعذّر الاتصال.\n\nتحقّق من Wi-Fi أو بيانات الجوال\nثم اضغط لإعادة المحاولة");
        if ("tv_channel_fail".equals(key)) return pick(c, "Bu kanal şu an açılamadı.\n\nDokun → tekrar dene\nveya ⏭ ile sonraki kanal", "This channel could not open.\n\nTap → retry\nor press ⏭ for the next channel", "Канал не открылся.\n\nНажмите → повторить\nили ⏭ — следующий канал", "لم تُفتح القناة.\n\nاضغط ← إعادة المحاولة\nأو ⏭ للقناة التالية");
        if ("tv_slow".equals(key)) return pick(c, "⏳ Yayın yavaş açılıyor · birkaç saniye daha", "⏳ Stream is opening slowly · a few more seconds", "⏳ Поток открывается медленно · ещё несколько секунд", "⏳ البث يُفتح ببطء · بضع ثوانٍ أخرى");
        if ("tv_backup".equals(key)) return pick(c, "🔁 Yedek kaynak deneniyor…", "🔁 Trying a backup source…", "🔁 Пробуем резервный источник…", "🔁 تجربة مصدر بديل…");
        if ("tv_live_resync".equals(key)) return pick(c, "⏭ Canlı yayına dönülüyor…", "⏭ Returning to the live edge…", "⏭ Возврат к прямому эфиру…", "⏭ العودة إلى البث المباشر…");
        if ("tv_net_retry".equals(key)) return pick(c, "📶 Bağlantı sorunu · %d sn sonra tekrar (%d/3)", "📶 Connection issue · retry in %d s (%d/3)", "📶 Проблема соединения · повтор через %d с (%d/3)", "📶 مشكلة اتصال · إعادة المحاولة بعد %d ث (%d/3)");
        if ("tv_stall".equals(key)) return pick(c, "⏳ Yayın durakladı · yeniden bağlanıyorum (%d/2)", "⏳ Stream stalled · reconnecting (%d/2)", "⏳ Поток завис · переподключение (%d/2)", "⏳ توقف البث · إعادة الاتصال (%d/2)");
        if ("tv_ad_break".equals(key)) return pick(c, "📺 Yayıncı reklam aralığında olabilir · yayın dönünce kendiliğinden gelecek", "📺 The broadcaster may be on an ad break · the stream returns by itself", "📺 Возможно, у вещателя реклама · поток вернётся сам", "📺 قد يكون المذيع في فاصل إعلاني · سيعود البث تلقائيًا");
        if ("tv_ad_done".equals(key)) return pick(c, "✅ Reklam kapandı · yayına dönülüyor", "✅ Ad closed · back to the stream", "✅ Реклама закрыта · возврат к эфиру", "✅ أُغلق الإعلان · العودة إلى البث");
        if ("multi_reconnect".equals(key)) return pick(c, "yeniden bağlanıyor…", "reconnecting…", "переподключение…", "جارٍ إعادة الاتصال…");
        if ("multi_stall".equals(key)) return pick(c, "yayın durakladı · yeniden bağlanıyor…", "stream stalled · reconnecting…", "поток завис · переподключение…", "توقف البث · إعادة الاتصال…");
        if ("multi_ad".equals(key)) return pick(c, "yayıncı reklam aralığında olabilir · bekleniyor", "broadcaster may be on an ad break · waiting", "возможно, реклама у вещателя · ждём", "قد يكون المذيع في فاصل إعلاني · بالانتظار");
        if ("multi_fail".equals(key)) return pick(c, "⚠ Açılamadı · dokun → tekrar dene · uzun bas → başka kanal", "⚠ Failed to open · tap → retry · long press → another channel", "⚠ Не открылось · нажмите → повторить · долгое нажатие → другой канал", "⚠ تعذّر الفتح · اضغط ← إعادة المحاولة · ضغط مطوّل ← قناة أخرى");

        // --- hesap / uyelik ---
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
        if ("code_title".equals(key)) return pick(c, "E-postana gelen kodu gir", "Enter the code sent to your e-mail", "Введите код из письма", "أدخل الرمز المُرسل إلى بريدك");
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

    /** "145 kanal" / "145 channels" gibi sayım metni. */
    public static String channels(Context c, int n) {
        String l = Prefs.uiLang(c);
        if ("en".equals(l)) return n + (n == 1 ? " channel" : " channels");
        if ("ru".equals(l)) return n + " каналов";
        if ("ar".equals(l)) return n + " قناة";
        return n + " kanal";
    }

    /** Alt baslik. 9.77: kanal sayisi artik alttaki veri seridinde; burada tekrarlanmiyor. */
    public static String subtitle(Context c, int count) {
        String l = Prefs.uiLang(c);
        if ("en".equals(l)) return "open by number, watch 3 at once, record";
        if ("ru".equals(l)) return "по номеру, 3 экрана, запись";
        if ("ar".equals(l)) return "بالرقم، ٣ شاشات، تسجيل";
        return "no ile aç, 3'lü izle, kayıt";
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
