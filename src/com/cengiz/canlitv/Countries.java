package com.cengiz.canlitv;

/** Desteklenen ulkeler: kod, Turkce ad, Ingilizce ad. Kanal listesi iptv-org'dan ulke koduna gore gelir. */
public class Countries {

    public static final String[][] LIST = {
            {"tr", "Türkiye", "Turkey"},
            {"az", "Azerbaycan", "Azerbaijan"},
            {"de", "Almanya", "Germany"},
            {"at", "Avusturya", "Austria"},
            {"bd", "Bangladeş", "Bangladesh"},
            {"ae", "Birleşik Arap Emirlikleri", "United Arab Emirates"},
            {"us", "Amerika Birleşik Devletleri", "United States"},
            {"ar", "Arjantin", "Argentina"},
            {"al", "Arnavutluk", "Albania"},
            {"au", "Avustralya", "Australia"},
            {"ma", "Fas", "Morocco"},
            {"br", "Brezilya", "Brazil"},
            {"gb", "Birleşik Krallık", "United Kingdom"},
            {"bg", "Bulgaristan", "Bulgaria"},
            {"dz", "Cezayir", "Algeria"},
            {"cn", "Çin", "China"},
            {"dk", "Danimarka", "Denmark"},
            {"id", "Endonezya", "Indonesia"},
            {"ee", "Estonya", "Estonia"},
            {"fi", "Finlandiya", "Finland"},
            {"ph", "Filipinler", "Philippines"},
            {"fr", "Fransa", "France"},
            {"kr", "Güney Kore", "South Korea"},
            {"ge", "Gürcistan", "Georgia"},
            {"in", "Hindistan", "India"},
            {"nl", "Hollanda", "Netherlands"},
            {"hr", "Hırvatistan", "Croatia"},
            {"iq", "Irak", "Iraq"},
            {"ir", "İran", "Iran"},
            {"ie", "İrlanda", "Ireland"},
            {"es", "İspanya", "Spain"},
            {"il", "İsrail", "Israel"},
            {"se", "İsveç", "Sweden"},
            {"ch", "İsviçre", "Switzerland"},
            {"it", "İtalya", "Italy"},
            {"jp", "Japonya", "Japan"},
            {"ca", "Kanada", "Canada"},
            {"qa", "Katar", "Qatar"},
            {"kz", "Kazakistan", "Kazakhstan"},
            {"eg", "Mısır", "Egypt"},
            {"mx", "Meksika", "Mexico"},
            {"ng", "Nijerya", "Nigeria"},
            {"pk", "Pakistan", "Pakistan"},
            {"ro", "Romanya", "Romania"},
            {"ru", "Rusya", "Russia"},
            {"rs", "Sırbistan", "Serbia"},
            {"sa", "Suudi Arabistan", "Saudi Arabia"},
            {"sy", "Suriye", "Syria"},
            {"gr", "Yunanistan", "Greece"},
            {"jo", "Ürdün", "Jordan"},
            {"vn", "Vietnam", "Vietnam"},
            {"ua", "Ukrayna", "Ukraine"},
            {"om", "Umman", "Oman"},
            {"lb", "Lübnan", "Lebanon"},
            {"by", "Belarus", "Belarus"},
    };

    /** Bu ulke listede var mi? (IP'den gelen kod icin) */
    public static boolean supported(String code) {
        for (String[] c : LIST) if (c[0].equals(code)) return true;
        return false;
    }

    public static String name(String code, boolean english) {
        for (String[] c : LIST) if (c[0].equals(code)) return english ? c[2] : c[1];
        return code.toUpperCase();
    }

    /** Ulke kodundan bayrak emojisi uretir (tr -> TR). */
    public static String flag(String code) {
        if (code == null || code.length() != 2) return "🌐";
        int a = Character.toUpperCase(code.charAt(0)) - 'A' + 0x1F1E6;
        int b = Character.toUpperCase(code.charAt(1)) - 'A' + 0x1F1E6;
        if (a < 0x1F1E6 || b < 0x1F1E6) return "🌐";
        return new String(Character.toChars(a)) + new String(Character.toChars(b));
    }
}
