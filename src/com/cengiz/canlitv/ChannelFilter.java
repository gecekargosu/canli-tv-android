package com.cengiz.canlitv;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * v10.10: Kanal listesinin "hangi kanallar, hangi sırada" hesabı — MainActivity'den ayrıldı.
 *
 * Arayüzden bağımsızdır: verilen sekme (group), arama metni ve "gizlenenler" bayrağına
 * göre listeyi üretir. Sıralama kaydı (Prefs.order) ve son izlenenler de burada uygulanır.
 */
public final class ChannelFilter {

    private ChannelFilter() { }

    /**
     * Gösterilecek kanalları üretir. Dönen liste yenidir; çağıran kendi listesine kopyalar.
     *
     * @param all        tüm kanallar (ChannelRepo.all)
     * @param group      sekme adı: "Tümü", "★ Favoriler", "🇹🇷 Türkçe", "🕘 Son", kategori…
     * @param query      arama metni (Lang.fold ile katlanmış, boş olabilir)
     * @param onlyHidden "🗑 Gizlenenler" sekmesi mi
     */
    public static List<Channel> build(Context c, List<Channel> all, String group,
                                      String query, boolean onlyHidden) {
        List<Channel> base = new ArrayList<Channel>();
        if (all == null) return base;
        Set<String> favs = Prefs.favs(c);
        List<String> recent = Prefs.recent(c);
        java.util.Set<String> hidden = Prefs.hiddenChannels(c);
        boolean hideDead = Prefs.hideDead(c);

        for (Channel ch : all) {
            if (onlyHidden != hidden.contains(ch.url)) continue;
            // 9.75: TV8 gibi büyük kanallar iki geçici hatadan sonra listeden düşüyordu.
            // Sabit (öne alınan) kanallar otomatik gizlemeden muaf.
            if (!onlyHidden && hideDead && Prefs.fails(c, ch.url) >= 2
                    && !ChannelRepo.pinned(ch.name)) continue;
            // 9.96: "Gizlenenler" sekmesinde kategori filtresi uygulanmaz (liste boş kalıyordu).
            if (!"Tümü".equals(group) && !onlyHidden) {
                if ("★ Favoriler".equals(group)) {
                    if (!favs.contains(ch.url)) continue;
                } else if ("🇹🇷 Türkçe".equals(group)) {
                    if (!ch.turkish()) continue;
                } else if ("🌐 Yabancı".equals(group)) {
                    if (ch.turkish()) continue;
                } else if ("🕘 Son".equals(group)) {
                    if (!recent.contains(ch.url)) continue;
                } else if (!ch.group.equals(group)) {
                    continue;
                }
            }
            if (query != null && query.length() > 0
                    && !Lang.fold(ch.name).contains(query)
                    && !Lang.fold(ch.group).contains(query)
                    && !String.valueOf(ch.no).equals(query)) continue;
            base.add(ch);
        }

        return order(c, base, group, recent);
    }

    /** Bu sekme için kaydedilmiş kullanıcı sırası + "Son" sekmesinin izlenme sırası. */
    static List<Channel> order(Context c, List<Channel> base, String group, List<String> recent) {
        if (!"🕘 Son".equals(group)) {
            final List<String> ord = Prefs.order(c, group);
            if (!ord.isEmpty()) {
                Collections.sort(base, new Comparator<Channel>() {
                    @Override public int compare(Channel a, Channel b) {
                        int ia = ord.indexOf(a.url);
                        int ib = ord.indexOf(b.url);
                        if (ia < 0) ia = Integer.MAX_VALUE;
                        if (ib < 0) ib = Integer.MAX_VALUE;
                        return ia < ib ? -1 : (ia == ib ? 0 : 1);
                    }
                });
            }
            return base;
        }
        final List<Channel> ordered = new ArrayList<Channel>();
        for (String u : recent) for (Channel ch : base) if (ch.url.equals(u)) ordered.add(ch);
        for (Channel ch : new ArrayList<Channel>(base)) if (!ordered.contains(ch)) ordered.add(ch);
        return ordered;
    }
}
