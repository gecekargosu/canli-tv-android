package com.cengiz.canlitv;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * v10.10: Kanal listesi düzenleme modu (seçim + sıralama + silme) — MainActivity'den ayrıldı.
 *
 * Arayüz ayrıntılarını bilmez; gereken her şeyi {@link Host} üzerinden ister. Böylece
 * seçim/sıralama kuralları tek yerde durur ve MainActivity yalnız görünümü kurar.
 */
public final class ChannelEdit {

    /** MainActivity'nin sağladığı liste/arayüz erişimi. */
    public interface Host {
        /** Metin/kayıt işlemleri için bağlam. */
        android.content.Context context();
        /** Ekranda görünen (aktif) kanal listesi — doğrudan değiştirilebilir. */
        List<Channel> shownList();
        String group();
        String query();
        boolean hiddenTab();
        /** Düzenleme şeridi/görünümü: mod ve seçili sayısı değişti. */
        void editUi(boolean on, int count);
        /** Liste içeriği değişti (yeni liste kurulmadı). */
        void listChanged();
        /** Liste baştan kurulsun (refresh). */
        void listRebuilt();
        void say(String msg);
    }

    private final Host host;
    private final Set<String> selected = new LinkedHashSet<String>();
    private boolean mode = false;

    public ChannelEdit(Host host) {
        this.host = host;
    }

    public boolean mode() { return mode; }

    public boolean isSelected(String url) { return selected.contains(url); }

    public int count() { return selected.size(); }

    public void clear() { selected.clear(); }

    /** Modu açar/kapatır; kapanınca seçim temizlenir. */
    public void setMode(boolean on) {
        mode = on;
        if (!on) selected.clear();
        host.editUi(on, selected.size());
        host.listChanged();
    }

    /** Bir kanalın seçimini açar/kapatır (düzenleme modunda satıra dokunma). */
    public void toggle(Channel c) {
        if (c == null) return;
        if (selected.contains(c.url)) selected.remove(c.url);
        else selected.add(c.url);
        host.editUi(mode, selected.size());
        host.listChanged();
    }

    /** Seçimleri yalnız görünen kanallarda tut (filtre değişince hayalet seçim kalmasın). */
    public void retainVisible() {
        Set<String> visible = new java.util.HashSet<String>();
        for (Channel c : host.shownList()) visible.add(c.url);
        selected.retainAll(visible);
    }

    /** Seçim sayacını arayüze bildir (liste yeniden kurulduktan sonra). */
    public void publish() {
        host.editUi(mode, selected.size());
    }

    /** Satırdaki ▲▼ oku: tek kanalı bir adım taşır. */
    public void moveRow(int pos, int delta) {
        if (!canReorder()) return;
        List<Channel> shown = host.shownList();
        int j = pos + delta;
        if (pos < 0 || pos >= shown.size() || j < 0 || j >= shown.size()) return;
        Channel c = shown.remove(pos);
        shown.add(j, c);
        saveOrder();
        host.listChanged();
    }

    /** Seçili kanalları bir adım yukarı/aşağı taşır. */
    public void moveSelected(int delta) {
        if (!canReorder()) return;
        List<Channel> shown = host.shownList();
        List<Channel> sel = selectedChannels();
        if (sel.isEmpty()) { host.say(I18n.t(ctx(), "Önce kanallara dokunup seç")); return; }
        if (delta < 0) {
            for (Channel c : sel) {
                int i = shown.indexOf(c);
                if (i > 0) { shown.remove(i); shown.add(i - 1, c); }
            }
        } else {
            for (int k = sel.size() - 1; k >= 0; k--) {
                Channel c = sel.get(k);
                int i = shown.indexOf(c);
                if (i >= 0 && i < shown.size() - 1) { shown.remove(i); shown.add(i + 1, c); }
            }
        }
        saveOrder();
        host.listChanged();
    }

    /** Seçilileri en üste / en alta alır. */
    public void toEdge(boolean top) {
        if (!canReorder()) return;
        List<Channel> shown = host.shownList();
        List<Channel> sel = selectedChannels();
        if (sel.isEmpty()) { host.say(I18n.t(ctx(), "Önce kanallara dokunup seç")); return; }
        shown.removeAll(sel);
        if (top) shown.addAll(0, sel);
        else shown.addAll(sel);
        saveOrder();
        host.listChanged();
        host.say(I18n.t(ctx(), top ? "En üste alındı" : "En alta alındı"));
    }

    /** Seçili kanalları listeden çıkarır (Gizlenenler sekmesinde geri ekler). */
    public void deleteSelected() {
        if (!canReorder()) return;
        android.content.Context c = ctx();
        List<Channel> shown = host.shownList();
        List<Channel> sel = selectedChannels();
        if (sel.isEmpty()) { host.say(I18n.t(c, "Silmek için kanal seç")); return; }
        for (Channel ch : sel) {
            if (host.hiddenTab()) Prefs.unhideChannel(c, ch.url);
            else Prefs.hideChannel(c, ch.url);
        }
        shown.removeAll(sel);
        for (Channel ch : sel) selected.remove(ch.url);
        saveOrder();
        host.listRebuilt();
        host.say(host.hiddenTab()
                ? sel.size() + " " + I18n.t(c, "kanal listeye geri eklendi")
                : sel.size() + " " + I18n.t(c, "kanal listeden silindi · 🗑 Gizlenenler'den geri alabilirsin"));
    }

    /** Arama varken sıralama yapılmaz (görünen liste gerçek sıra değildir). */
    public boolean canReorder() {
        String q = host.query();
        if (q != null && q.length() > 0) {
            host.say(I18n.t(ctx(), "Sıralamak için arama kutusunu temizle"));
            return false;
        }
        return true;
    }

    /** Görünen sırayı sekmeye kaydeder ("🕘 Son" sekmesinde kaydedilmez). */
    public void saveOrder() {
        String g = host.group();
        if ("🕘 Son".equals(g)) return;
        List<String> urls = new ArrayList<String>();
        for (Channel ch : host.shownList()) urls.add(ch.url);
        Prefs.setOrder(ctx(), g, urls);
    }

    // ------------------------------------------------------------------ iç

    private List<Channel> selectedChannels() {
        List<Channel> out = new ArrayList<Channel>();
        for (Channel ch : host.shownList()) if (selected.contains(ch.url)) out.add(ch);
        return out;
    }

    private android.content.Context ctx() {
        return host.context();
    }
}
