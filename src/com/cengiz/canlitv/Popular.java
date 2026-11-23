package com.cengiz.canlitv;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Ülke listelerini "önce bilinen/en çok izlenen kanallar" sırasına dizer.
 *
 * Neden: iptv-org listeleri rastgele sırada; kullanıcı ilk sıralarda hiç duymadığı
 * kanalları görüyordu. Burada her ülke için tanınmış kanal adları sırayla aranır;
 * eşleşenler başa alınır, kalanlar listenin kendi sırasını korur (alfabetik değil,
 * böylece yayıncı sırası bozulmaz).
 */
public final class Popular {

    /** Ülke -> sırayla aranacak ad parçaları (küçük harf). */
    private static final String[][] TABLE = {
            {"tr",
                    "trt 1", "trt1", "atv", "star tv", "show tv", "now tv", "tv8", "kanal d",
                    "trt haber", "haber global", "cnn türk", "ntv", "trt spor", "a spor",
                    "bein sports", "trt 2", "trt belgesel", "trt müzik", "trt çocuk",
                    "cartoon network", "power türk", "kral tv", "teve2", "tv100", "halk tv",
                    "360", "flash haber", "tvnet", "ülev", "beyaz tv", "tv 24", "ulusal"},
            {"us",
                    "abc", "cbs", "nbc", "fox", "cnn", "msnbc", "espn", "hbo", "pbs",
                    "bloomberg", "cnbc", "discovery", "national geographic", "cartoon network",
                    "nickelodeon", "mtv", "tnt", "tbs", "usa network", "comedy central",
                    "weather channel", "npr", "pbs kids", "history", "amc"},
            {"gb",
                    "bbc one", "bbc two", "itv", "channel 4", "channel 5", "sky news",
                    "bbc news", "cbbc", "dave", "e4", "film4", "itv2", "itv3", "quest",
                    "sky sports", "bbc scotland"},
            {"de",
                    "ard", "zdf", "rtl", "sat.1", "pro sieben", "vox", "kabel eins",
                    "n-tv", "ntv", "phoenix", "3sat", "arte", "zdfneo", "zdf neo", "rtl2",
                    "sixx", "dmax", "sport1", "dw", "mtv germany"},
            {"ru",
                    "первый", "россия 1", "нтв", "тнт", "стс", "рен тв", "россия 24",
                    "звезда", "тв центр", "пятый", "рбк", "культура", "карусель", "мир"},
            {"in",
                    "star plus", "zee tv", "sony", "colors", "ndtv", "aaj tak",
                    "star sports", "sab tv", "&tv", "dd national", "india today", "times now"},
            {"br",
                    "globo", "sbt", "record", "band", "globonews", "sportv", "rede tv",
                    "band news", "cultura", "jovem pan"},
            {"mx",
                    "televisa", "azteca", "canal 5", "las estrellas", "milenio", "adn40",
                    "imagen", "once", "tv unam"},
            {"sa",
                    "العربية", "mbc", "السعودية", "الجزيرة", "روتانا", "الحدث", "الاخبارية",
                    "دبي", "أبوظبي", "الكويت"},
            {"fr",
                    "tf1", "france 2", "france 3", "france 5", "m6", "bfm", "cnews", "arte",
                    "canal+", "france 24", "rmc"},
            {"es",
                    "la 1", "la 2", "antena 3", "telecinco", "cuatro", "la sexta", "24h",
                    "tve", "movistar"},
            {"it",
                    "rai 1", "rai 2", "rai 3", "canale 5", "italia 1", "rete 4", "la7",
                    "sky tg24", "tgcom"},
            {"nl",
                    "nos", "npo 1", "npo 2", "npo 3", "rtl 4", "sbs6", "veronica", "een"},
            {"se",
                    "svt1", "svt2", "tv4", "tv3", "kanal 5", "svt24", "kunskapskanalen"},
            {"az",
                    "aztv", "itv", "atv", "xezer", "cbc", "real tv", "apa"},
    };

    /** Ülke -> sırayla aranacak radyo adları (TR listesi için). */
    private static final String[][] RADIO = {
            {"tr", "genç kral", "genc kral", "kral fm", "süper fm", "super fm", "metro fm",
                    "joy türk", "joy fm", "radio 7", "ntv radyo", "trt fm", "radyo 1",
                    "power fm", "virgin radio", "fenomen", "best fm", "number one",
                    "radyo d", "alem fm", "show radyo", "pal fm", "retro türk"},
    };

    private static final int UNKNOWN = 1000;

    private Popular() { }

    /** 0 = en popüler, 1000 = listede tanınmayan. */
    public static int rank(String cc, String name) {
        if (name == null) return UNKNOWN;
        String n = name.toLowerCase(new Locale("tr", "TR")).trim();
        for (int i = 0; i < TABLE.length; i++) {
            if (!TABLE[i][0].equals(cc)) continue;
            for (int j = 1; j < TABLE[i].length; j++) {
                String kw = TABLE[i][j];
                if (n.equals(kw)) return j - 1;                     // tam ad: "ATV"
                if (n.startsWith(kw + " ") || n.startsWith(kw + "(")
                        || n.startsWith(kw + " -")) return j - 1;   // "ATV Haber"
                if (n.contains(kw)) return j;                        // "ATV Alanya" biraz geride
            }
        }
        return UNKNOWN;
    }

    /** Radyo adının eşleştiği marka anahtarı (yoksa null). */
    public static String radioBrand(String cc, String name) {
        if (name == null) return null;
        String n = name.toLowerCase(new Locale("tr", "TR")).trim();
        for (int i = 0; i < RADIO.length; i++) {
            if (!RADIO[i][0].equals(cc)) continue;
            for (int j = 1; j < RADIO[i].length; j++) {
                String kw = RADIO[i][j];
                if (n.equals(kw) || n.startsWith(kw + " ") || n.startsWith(kw + " -") || n.contains(kw))
                    return kw;
            }
        }
        return null;
    }

    /** Radyo adı için sıra: 0 = en popüler. */
    public static int radioRank(String cc, String name) {
        if (name == null) return UNKNOWN;
        String n = name.toLowerCase(new Locale("tr", "TR")).trim();
        for (int i = 0; i < RADIO.length; i++) {
            if (!RADIO[i][0].equals(cc)) continue;
            for (int j = 1; j < RADIO[i].length; j++) {
                String kw = RADIO[i][j];
                if (n.equals(kw)) return j - 1;
                if (n.startsWith(kw + " ") || n.startsWith(kw + " -")) return j - 1;
                if (n.contains(kw)) return j;
            }
        }
        return UNKNOWN;
    }

    /**
     * Kanalları popülerliğe göre dizer: her tanınmış markadan **bir** kanal öne alınır
     * (ABC, CBS, NBC…), aynı markanın yerel yayınları arkada kalır. Sonra listenin kendi sırası gelir.
     */
    public static void arrange(String cc, List<Channel> list) {
        if (list == null || list.size() < 2) return;
        java.util.ArrayList<Channel> pool = new java.util.ArrayList<Channel>(list);
        java.util.ArrayList<Channel> head = new java.util.ArrayList<Channel>();
        String[] kws = keywords(cc);
        for (int j = 0; j < kws.length; j++) {
            Channel best = null;
            int bestScore = 3;
            for (Channel c : pool) {
                int s = score(kws[j], c.name);
                if (s > 2) continue;
                if (s < bestScore || (s == bestScore && best != null
                        && c.name.length() < best.name.length())) {
                    best = c;
                    bestScore = s;
                }
            }
            if (best != null) {
                head.add(best);
                pool.remove(best);
            }
        }
        list.clear();
        list.addAll(head);
        list.addAll(pool);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).baseNo = i + 1;
            list.get(i).no = i + 1;
        }
    }

    private static String[] keywords(String cc) {
        for (int i = 0; i < TABLE.length; i++) {
            if (TABLE[i][0].equals(cc)) {
                String[] out = new String[TABLE[i].length - 1];
                System.arraycopy(TABLE[i], 1, out, 0, out.length);
                return out;
            }
        }
        return new String[0];
    }

    /** 0 = tam ad, 1 = ad bu ifadeyle başlıyor, 2 = içeriyor, 3 = alakasız. */
    private static int score(String kw, String name) {
        if (name == null) return 3;
        String n = name.toLowerCase(new Locale("tr", "TR")).trim();
        if (n.equals(kw)) return 0;
        // "ATV (Turkiye)" gibi parantezli asil kanal, "ATV Alanya" gibi yerel yayindan onde olmali
        if (n.startsWith(kw + " (") || n.startsWith(kw + "(")) return 0;
        if (n.startsWith(kw + " ") || n.startsWith(kw + " -")) return 1;
        if (n.contains(kw)) return 2;
        return 3;
    }
}
