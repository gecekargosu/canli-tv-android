package com.cengiz.canlitv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.ads.AdView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends android.app.Activity {

    private static final int BG = Color.parseColor("#11151C");
    private static final int CARD = Color.parseColor("#1B2230");
    private static final int ACCENT = Color.parseColor("#FF7A18");
    private static final int TEXT = Color.parseColor("#F5F7FA");
    private static final int MUTED = Color.parseColor("#9AA4B2");
    private static final int SOFT = Color.parseColor("#2A3444");

    private final List<Channel> shown = new ArrayList<Channel>();
    private List<Channel> all;
    private ChannelAdapter adapter;
    private String group = "Tümü";
    private String query = "";
    private LinearLayout chipsRow;
    private TextView dataBar;
    private TextView emptyView;
    private ListView listView;
    private boolean onlyHidden = false;
    private String shownCountry = "";
    private String shownLang = "";

    // --- liste duzenleme ---
    private boolean editMode = false;
    private final java.util.LinkedHashSet<String> selected = new java.util.LinkedHashSet<String>();
    private LinearLayout editBar;
    private TextView editBtn, selCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashLog.install(this);   // cokme olursa cihaza log yaz (Ayarlar -> Hata bildir)
        all = ChannelRepo.all(this);
        shownCountry = Prefs.country(this);
        shownLang = Prefs.uiLang(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setFitsSystemWindows(true);

        // --- baslik ---
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.VERTICAL);
        head.setPadding(dp(16), dp(16), dp(16), dp(4));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView brand = new ImageView(this);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(42), dp(42));
        blp.setMargins(0, 0, dp(10), 0);
        brand.setLayoutParams(blp);
        brand.setScaleType(ImageView.ScaleType.FIT_CENTER);
        try { brand.setImageDrawable(getPackageManager().getApplicationIcon(getPackageName())); }
        catch (Exception ignored) { }
        topRow.addView(brand);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(this);
        title.setText("UMAY Multi TV");
        title.setTextColor(TEXT);
        title.setTextSize(17);
        title.setSingleLine(true);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        TextView sub = new TextView(this);
        sub.setText(Lang.subtitle(this, all.size()));
        sub.setTextColor(MUTED);
        sub.setTextSize(10);
        sub.setSingleLine(true);
        sub.setEllipsize(android.text.TextUtils.TruncateAt.END);
        titleCol.addView(title);
        titleCol.addView(sub);
        topRow.addView(titleCol, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView multi = pill("▦ 3'lü", ACCENT);
        multi.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MultiActivity.class));
            }
        });
        multi.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                savedSets();
                return true;
            }
        });
        topRow.addView(multi);

        TextView radio = pill("📻", SOFT);
        radio.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RadioActivity.class));
            }
        });
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.setMargins(dp(6), 0, 0, 0);
        radio.setLayoutParams(rlp);
        topRow.addView(radio);

        // --- ulke secimi: secilen ulkenin kanallari listeye gelir ---
        TextView countryBtn = pill(Countries.flag(shownCountry), SOFT);
        countryBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CountryActivity.class));
            }
        });
        LinearLayout.LayoutParams cblp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cblp.setMargins(dp(6), 0, 0, 0);
        countryBtn.setLayoutParams(cblp);
        topRow.addView(countryBtn);

        TextView settings = pill("⚙", SOFT);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.setMargins(dp(6), 0, 0, 0);
        settings.setLayoutParams(slp);
        topRow.addView(settings);

        head.addView(topRow);

        dataBar = new TextView(this);
        dataBar.setTextSize(11);
        dataBar.setTextColor(MUTED);
        dataBar.setPadding(0, dp(6), 0, 0);
        head.addView(dataBar);

        root.addView(head);

        // --- arama + kanal no ---
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams srlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        srlp.setMargins(dp(14), dp(8), dp(14), dp(4));
        searchRow.setLayoutParams(srlp);

        EditText search = new EditText(this);
        search.setHint(Lang.t(this, "search"));
        search.setHintTextColor(MUTED);
        search.setTextColor(TEXT);
        search.setTextSize(14);
        search.setSingleLine(true);
        GradientDrawable sbg = rounded(CARD, 14);
        search.setBackground(sbg);
        search.setPadding(dp(12), dp(8), dp(12), dp(8));
        searchRow.addView(search, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                query = s.toString().trim().toLowerCase();
                refresh();
            }
            public void afterTextChanged(Editable s) { }
        });

        final EditText noInput = new EditText(this);
        noInput.setHint("No");
        noInput.setHintTextColor(MUTED);
        noInput.setTextColor(TEXT);
        noInput.setTextSize(14);
        noInput.setSingleLine(true);
        noInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        noInput.setGravity(Gravity.CENTER);
        noInput.setBackground(rounded(CARD, 14));
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(dp(58),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.setMargins(dp(6), 0, 6, 0);
        noInput.setLayoutParams(nlp);
        searchRow.addView(noInput);

        TextView go = pill("Git", ACCENT);
        go.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    int no = Integer.parseInt(noInput.getText().toString().trim());
                    gotoNumber(no);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Kanal numarası yaz", Toast.LENGTH_SHORT).show();
                }
            }
        });
        searchRow.addView(go);
        root.addView(searchRow);

        // --- cipler ---
        HorizontalScrollView chipsScroll = new HorizontalScrollView(this);
        chipsScroll.setHorizontalScrollBarEnabled(false);
        chipsRow = new LinearLayout(this);
        chipsRow.setOrientation(LinearLayout.HORIZONTAL);
        chipsRow.setPadding(dp(12), dp(4), dp(12), dp(4));
        chipsScroll.addView(chipsRow);
        root.addView(chipsScroll);
        buildChips();

        // --- liste araclari: duzenle / sirala ---
        LinearLayout tools = new LinearLayout(this);
        tools.setOrientation(LinearLayout.HORIZONTAL);
        tools.setGravity(Gravity.CENTER_VERTICAL);
        tools.setPadding(dp(14), dp(2), dp(14), dp(2));

        editBtn = pill(Lang.t(this, "edit"), SOFT);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setEditMode(!editMode); }
        });
        tools.addView(editBtn);

        TextView sortBtn = pill(Lang.t(this, "sort"), SOFT);
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { sortDialog(); }
        });
        LinearLayout.LayoutParams sortLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sortLp.setMargins(dp(6), 0, 0, 0);
        sortBtn.setLayoutParams(sortLp);
        tools.addView(sortBtn);

        root.addView(tools);

        // --- duzenleme cubugu ---
        editBar = new LinearLayout(this);
        editBar.setOrientation(LinearLayout.HORIZONTAL);
        editBar.setGravity(Gravity.CENTER_VERTICAL);
        editBar.setPadding(dp(14), dp(2), dp(14), dp(6));
        editBar.setVisibility(View.GONE);

        TextView doneBtn = mini("✓ Bitti");
        doneBtn.setBackground(rounded(ACCENT, 14));
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setEditMode(false); }
        });
        editBar.addView(doneBtn);

        selCount = new TextView(this);
        selCount.setTextColor(MUTED);
        selCount.setTextSize(10);
        LinearLayout.LayoutParams sclp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        sclp.setMargins(dp(8), 0, dp(4), 0);
        editBar.addView(selCount, sclp);

        TextView upBtn = mini("▲");
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { moveSelected(-1); }
        });
        editBar.addView(upBtn);

        TextView downBtn = mini("▼");
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { moveSelected(1); }
        });
        editBar.addView(downBtn);

        TextView topBtn = mini("⤒");
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { moveSelectedToEdge(true); }
        });
        editBar.addView(topBtn);

        TextView bottomBtn = mini("⤓");
        bottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { moveSelectedToEdge(false); }
        });
        editBar.addView(bottomBtn);

        TextView delBtn = mini("🗑");
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { deleteSelected(); }
        });
        editBar.addView(delBtn);

        root.addView(editBar);

        // --- liste ---
        ListView list = new ListView(this);
        listView = list;
        list.setDivider(null);
        list.setDividerHeight(dp(6));
        list.setPadding(dp(12), dp(4), dp(12), dp(12));
        list.setClipToPadding(false);
        list.setSelector(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        adapter = new ChannelAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                Channel c = shown.get(pos);
                if (editMode) { toggleSelect(c); return; }
                if (onlyHidden) { channelMenu(c); return; }
                open(c);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> p, View v, int pos, long id) {
                channelMenu(shown.get(pos));
                return true;
            }
        });

        emptyView = new TextView(this);
        emptyView.setTextColor(MUTED);
        emptyView.setTextSize(13);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setPadding(dp(24), dp(40), dp(24), dp(24));
        emptyView.setVisibility(View.GONE);
        root.addView(emptyView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // reklam: sadece listenin altinda banner (oynatma ekraninda yok)
        android.view.View banner = AdManager.banner(this);
        if (banner != null) root.addView(banner);

        setContentView(root);
        AdManager.init(this);
        handleExtras(getIntent());
        refresh();
    }

    private void buildChips() {
        chipsRow.removeAllViews();
        List<String> cats = new ArrayList<String>();
        cats.add("Tümü");
        cats.add("★ Favoriler");
        if ("tr".equals(Prefs.country(this))) {
            cats.add("🇹🇷 Türkçe");
            cats.add("🌐 Yabancı");
        }
        cats.add("🕘 Son");
        cats.add("🗑 Gizlenenler");
        cats.addAll(ChannelRepo.groups(this));
        for (final String c : cats) {
            TextView chip = new TextView(this);
            chip.setText(Lang.tab(this, c));
            chip.setTextSize(12);
            chip.setPadding(dp(12), dp(7), dp(12), dp(7));
            chip.setTag(c);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    group = c;
                    onlyHidden = "🗑 Gizlenenler".equals(c);
                    selected.clear();
                    styleChips();
                    refresh();
                }
            });
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.setMargins(0, 0, dp(6), 0);
            chip.setLayoutParams(clp);
            chipsRow.addView(chip);
        }
        styleChips();
    }

    private void styleChips() {
        for (int i = 0; i < chipsRow.getChildCount(); i++) {
            TextView chip = (TextView) chipsRow.getChildAt(i);
            boolean sel = chip.getTag().toString().equals(group);
            chip.setBackground(rounded(sel ? ACCENT : CARD, 18));
            chip.setTextColor(sel ? Color.WHITE : MUTED);
        }
    }

    private void gotoNumber(int no) {
        final Channel c = ChannelRepo.byNumber(this, no);
        if (c == null) {
            Toast.makeText(this, "№" + no + " diye kanal yok (1-" + all.size() + ")",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Kanal " + no)
                .setMessage(c.flag() + "  " + c.name + "\n" + c.group + "\n\nAçılsın mı?")
                .setPositiveButton("Aç", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        open(c);
                    }
                })
                .setNegativeButton("Vazgeç", null)
                .show();
    }

    private void savedSets() {
        final List<String> sets = new ArrayList<String>(Prefs.multiSets(this));
        sets.add(0, "Son kullandığım set");
        new AlertDialog.Builder(this)
                .setTitle("3'lü izleme setleri")
                .setItems(sets.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        Intent i = new Intent(MainActivity.this, MultiActivity.class);
                        if (which > 0) i.putExtra("set", sets.get(which));
                        startActivity(i);
                    }
                })
                .setNeutralButton("Set sil", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { deleteSetDialog(); }
                })
                .show();
    }

    private void deleteSetDialog() {
        final List<String> sets = new ArrayList<String>(Prefs.multiSets(this));
        if (sets.isEmpty()) {
            Toast.makeText(this, "Kayıtlı set yok", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[sets.size()];
        for (int i = 0; i < sets.size(); i++) {
            String s = sets.get(i);
            labels[i] = s.startsWith("ad:") ? s.substring(3, Math.max(3, s.indexOf('|'))) : s;
        }
        new AlertDialog.Builder(this).setTitle("Hangi set silinsin?").setItems(labels,
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        Prefs.removeMultiSet(MainActivity.this, sets.get(which));
                        Toast.makeText(MainActivity.this, "Silindi", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    // ---------- duzenleme modu ----------
    private void setEditMode(boolean on) {
        editMode = on;
        if (!on) selected.clear();
        if (editBar != null) editBar.setVisibility(on ? View.VISIBLE : View.GONE);
        if (editBtn != null) {
            editBtn.setText(on ? "✏️ Düzenleniyor" : "✏️ Düzenle");
            editBtn.setBackground(rounded(on ? ACCENT : SOFT, 18));
        }
        updateSelCount();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void toggleSelect(Channel c) {
        if (selected.contains(c.url)) selected.remove(c.url);
        else selected.add(c.url);
        updateSelCount();
        adapter.notifyDataSetChanged();
    }

    private void updateSelCount() {
        if (selCount != null) {
            selCount.setText(selected.size() == 0
                    ? "kanala dokun → seç"
                    : selected.size() + " kanal seçili");
        }
        if (editBar != null) editBar.setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    private List<Channel> selectedChannels() {
        List<Channel> out = new ArrayList<Channel>();
        for (Channel c : shown) if (selected.contains(c.url)) out.add(c);
        return out;
    }

    private void moveSelected(int delta) {
        if (!canReorder()) return;
        List<Channel> sel = selectedChannels();
        if (sel.isEmpty()) {
            Toast.makeText(this, "Önce kanallara dokunup seç", Toast.LENGTH_SHORT).show();
            return;
        }
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
        adapter.notifyDataSetChanged();
    }

    private void moveSelectedToEdge(boolean top) {
        if (!canReorder()) return;
        List<Channel> sel = selectedChannels();
        if (sel.isEmpty()) {
            Toast.makeText(this, "Önce kanallara dokunup seç", Toast.LENGTH_SHORT).show();
            return;
        }
        shown.removeAll(sel);
        if (top) shown.addAll(0, sel);
        else shown.addAll(sel);
        saveOrder();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, top ? "En üste alındı" : "En alta alındı", Toast.LENGTH_SHORT).show();
    }

    private void deleteSelected() {
        if (!canReorder()) return;
        List<Channel> sel = selectedChannels();
        if (sel.isEmpty()) {
            Toast.makeText(this, "Silmek için kanal seç", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Channel c : sel) {
            if (onlyHidden) Prefs.unhideChannel(this, c.url);
            else Prefs.hideChannel(this, c.url);
        }
        shown.removeAll(sel);
        for (Channel c : sel) selected.remove(c.url);
        saveOrder();
        refresh();
        Toast.makeText(this, onlyHidden
                        ? sel.size() + " kanal listeye geri eklendi"
                        : sel.size() + " kanal listeden silindi · 🗑 Gizlenenler'den geri alabilirsin",
                Toast.LENGTH_LONG).show();
    }

    private boolean canReorder() {
        if (query.length() > 0) {
            Toast.makeText(this, "Sıralamak için arama kutusunu temizle", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveOrder() {
        if ("🕘 Son".equals(group)) return;
        List<String> urls = new ArrayList<String>();
        for (Channel c : shown) urls.add(c.url);
        Prefs.setOrder(this, group, urls);
    }

    /** Tek bir kanali bir adim yukari/asagi tasi (satirdaki oklar). */
    private void moveItem(int pos, int delta) {
        if (!canReorder()) return;
        int j = pos + delta;
        if (pos < 0 || pos >= shown.size() || j < 0 || j >= shown.size()) return;
        Channel c = shown.remove(pos);
        shown.add(j, c);
        saveOrder();
        adapter.notifyDataSetChanged();
    }

    private void askNumber(final Channel c) {
        final boolean filtered = query.length() > 0;
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(c.no));
        new AlertDialog.Builder(this)
                .setTitle("Kumanda numarası")
                .setMessage("Yeni numara hem kumanda numarasını hem listedeki sırasını değiştirir.\n\nDolu bir numara yazarsan o kanalla numaralar yer değiştirir.")
                .setView(et)
                .setPositiveButton("Kaydet", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        int no;
                        try { no = Integer.parseInt(et.getText().toString().trim()); }
                        catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Sadece sayı yaz", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (no < 1 || no > all.size()) {
                            Toast.makeText(MainActivity.this, "1 ile " + all.size() + " arasında bir numara seç",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Channel other = ChannelRepo.byNumber(MainActivity.this, no);
                        int oldNo = c.no;
                        if (other != null && !other.url.equals(c.url)) {
                            // Numara dolu: iki kanal numaralarini degistirir (kumanda sirasi)
                            Prefs.setNoOverride(MainActivity.this, other.url, oldNo);
                        }
                        Prefs.setNoOverride(MainActivity.this, c.url, no);
                        ChannelRepo.applyNumbers(MainActivity.this);
                        if (!filtered) {
                            // Numara = kumanda sirasi: sekmeyi numaraya gore diz
                            java.util.Collections.sort(shown, new java.util.Comparator<Channel>() {
                                @Override public int compare(Channel a, Channel b) {
                                    return a.no - b.no;
                                }
                            });
                            saveOrder();
                        }
                        refresh();
                        Toast.makeText(MainActivity.this,
                                c.name + " artık №" + no, Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Vazgeç", null).show();
    }

    private void sortDialog() {
        if (!canReorder()) return;
        final String[] items = {
                "🕘 Çok izlediklerim en üstte",
                "🆕 Hiç izlemediklerim en üstte",
                "🔤 Ada göre A → Z",
                "🔢 Kanal numarasına göre",
                "↩️ Varsayılan sıraya dön"
        };
        new AlertDialog.Builder(this)
                .setTitle("Bu sekme nasıl sıralansın?")
                .setMessage(group + " · " + shown.size() + " kanal")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == 4) {
                            Prefs.setOrder(MainActivity.this, group, new ArrayList<String>());
                            setEditMode(false);
                            refresh();
                            Toast.makeText(MainActivity.this, "Varsayılan sıra geri geldi",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<Channel> sorted = new ArrayList<Channel>(shown);
                        if (which == 0 || which == 1) {
                            final boolean mostFirst = which == 0;
                            java.util.Collections.sort(sorted, new java.util.Comparator<Channel>() {
                                @Override public int compare(Channel a, Channel b) {
                                    long sa = watchScore(a), sb = watchScore(b);
                                    if (sa == sb) return a.no - b.no;
                                    if (mostFirst) return sa > sb ? -1 : 1;
                                    return sa < sb ? -1 : 1;
                                }
                            });
                        } else if (which == 2) {
                            java.util.Collections.sort(sorted, new java.util.Comparator<Channel>() {
                                @Override public int compare(Channel a, Channel b) {
                                    return a.name.compareToIgnoreCase(b.name);
                                }
                            });
                        } else {
                            java.util.Collections.sort(sorted, new java.util.Comparator<Channel>() {
                                @Override public int compare(Channel a, Channel b) {
                                    return a.no - b.no;
                                }
                            });
                        }
                        shown.clear();
                        shown.addAll(sorted);
                        saveOrder();
                        setEditMode(false);
                        refresh();
                        Toast.makeText(MainActivity.this, "Sıra kaydedildi", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    /** Izlenme suresi agirlikli puan: cok izlenen kanali one almak icin. */
    private long watchScore(Channel c) {
        return Prefs.secs(this, c.url) * 1000L + Prefs.usage(this, c.url) / 1024L;
    }

    private void channelMenu(final Channel c) {
        final boolean hidden = Prefs.hiddenChannels(this).contains(c.url);
        final boolean fav = Prefs.favs(this).contains(c.url);
        final List<String> items = new ArrayList<String>();
        items.add("▶  Aç");
        items.add(fav ? "★  Favoriden çıkar" : "★  Favorilere ekle");
        items.add("🔢  Numarayı değiştir (№" + c.no + ")");
        items.add("☑  Seçim / sıralama modu");
        items.add(hidden ? "➕  Listeye geri ekle" : "🗑  Listeden sil");
        new AlertDialog.Builder(this)
                .setTitle("№" + c.no + "  " + c.flag() + "  " + c.name)
                .setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == 0) { open(c); return; }
                        if (which == 1) { Prefs.toggleFav(MainActivity.this, c.url); refresh(); return; }
                        if (which == 2) { askNumber(c); return; }
                        if (which == 3) {
                            setEditMode(true);
                            toggleSelect(c);
                            return;
                        }
                        if (hidden) {
                            Prefs.unhideChannel(MainActivity.this, c.url);
                            refresh();
                        } else {
                            Prefs.hideChannel(MainActivity.this, c.url);
                            refresh();
                            Toast.makeText(MainActivity.this,
                                    c.name + " listeden silindi.\nGeri eklemek: 🗑 Gizlenenler",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }).show();
    }

    private void open(final Channel c) {
        // 4. kanal acilisinda bir, en fazla 10 dakikada 1 gecis reklami
        AdManager.maybeInterstitial(this, new Runnable() {
            @Override public void run() {
                Intent i = new Intent(MainActivity.this, PlayerActivity.class);
                i.putExtra("index", ChannelRepo.indexOfUrl(c.url));
                startActivity(i);
            }
        });
    }

    private void handleExtras(Intent it) {
        if (it == null) return;
        String tab = it.getStringExtra("tab");
        if (tab != null && tab.length() > 0) {
            group = tab;
            onlyHidden = "🗑 Gizlenenler".equals(tab);
            styleChips();
            it.removeExtra("tab");
        }
        if (it.getBooleanExtra("edit", false)) {
            it.removeExtra("edit");
            setEditMode(true);
        }
        if (it.getBooleanExtra("sort", false)) {
            it.removeExtra("sort");
            refresh();
            sortDialog();
        }
    }

    private void refresh() {
        ChannelRepo.applyNumbers(this);
        shown.clear();
        Set<String> favs = Prefs.favs(this);
        List<String> recent = Prefs.recent(this);
        java.util.Set<String> hidden = Prefs.hiddenChannels(this);
        List<Channel> base = new ArrayList<Channel>();
        for (Channel c : all) {
            if (onlyHidden != hidden.contains(c.url)) continue;
            if (!onlyHidden && Prefs.hideDead(this) && Prefs.fails(this, c.url) >= 2) continue;
            if (!"Tümü".equals(group)) {
                if ("★ Favoriler".equals(group)) {
                    if (!favs.contains(c.url)) continue;
                } else if ("🇹🇷 Türkçe".equals(group)) {
                    if (!c.turkish()) continue;
                } else if ("🌐 Yabancı".equals(group)) {
                    if (c.turkish()) continue;
                } else if ("🕘 Son".equals(group)) {
                    if (!recent.contains(c.url)) continue;
                } else if (!c.group.equals(group)) {
                    continue;
                }
            }
            if (query.length() > 0
                    && !c.name.toLowerCase().contains(query)
                    && !c.group.toLowerCase().contains(query)
                    && !String.valueOf(c.no).equals(query)) continue;
            base.add(c);
        }

        // Bu sekme icin kaydedilmis kullanici sirasi (surukle/birak yerine kalici sira)
        if (!"🕘 Son".equals(group)) {
            final List<String> ord = Prefs.order(this, group);
            if (!ord.isEmpty()) {
                java.util.Collections.sort(base, new java.util.Comparator<Channel>() {
                    @Override public int compare(Channel a, Channel b) {
                        int ia = ord.indexOf(a.url);
                        int ib = ord.indexOf(b.url);
                        if (ia < 0) ia = Integer.MAX_VALUE;
                        if (ib < 0) ib = Integer.MAX_VALUE;
                        return ia < ib ? -1 : (ia == ib ? 0 : 1);
                    }
                });
            }
        }
        shown.addAll(base);

        if ("🕘 Son".equals(group)) {
            final List<Channel> ordered = new ArrayList<Channel>();
            for (String u : recent) for (Channel c : shown) if (c.url.equals(u)) ordered.add(c);
            for (Channel c : new ArrayList<Channel>(shown)) if (!ordered.contains(c)) ordered.add(c);
            shown.clear();
            shown.addAll(ordered);
        }

        // secimleri yalnizca gorunen kanallarla sinirla
        java.util.Set<String> visible = new java.util.HashSet<String>();
        for (Channel c : shown) visible.add(c.url);
        selected.retainAll(visible);

        updateSelCount();
        adapter.notifyDataSetChanged();
        if (shown.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(onlyHidden
                    ? "Gizlenen kanal yok.\n\nBir kanala uzun basıp \"Listeden sil\" dersen burada birikir."
                    : "Bu filtrede kanal yok.\n\nAramayı temizle veya başka bir sekme dene.");
            if (listView != null) listView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            if (listView != null) listView.setVisibility(View.VISIBLE);
        }
        updateDataBar();
    }

    private void updateDataBar() {
        long used = Prefs.todayBytes(this);
        int lim = Prefs.limitMb(this);
        String s = "📶 Bugün: " + Net.mb(used);
        if (lim > 0) {
            s += " / " + lim + " MB";
            if (used > lim * 1048576L) s += "  ⚠ LİMİT AŞILDI";
        } else {
            s += " · limit yok";
        }
        s += "  · 🎚 " + QualityDialog.label(this);
        s += Prefs.audioOnlyMobile(this) ? "  · 🎧 mobilde sadece ses" : "";
        dataBar.setText(s);
        dataBar.setTextColor(lim > 0 && used > lim * 1048576L ? Color.parseColor("#FF5252") : MUTED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ulke veya arayuz dili degistiyse ekrani bastan kur
        if (!shownCountry.equals(Prefs.country(this)) || !shownLang.equals(Prefs.uiLang(this))) {
            recreate();
            return;
        }
        ChannelRepo.applyNumbers(this);
        refresh();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleExtras(intent);
        refresh();
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView pill(String s, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(12);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(9), dp(12), dp(9));
        t.setBackground(rounded(color, 18));
        return t;
    }

    private TextView mini(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(11);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(6), dp(6), dp(6), dp(6));
        t.setBackground(rounded(SOFT, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(32));
        lp.setMargins(dp(4), 0, 0, 0);
        t.setLayoutParams(lp);
        return t;
    }

    private TextView arrow(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.WHITE);
        t.setTextSize(12);
        t.setGravity(Gravity.CENTER);
        t.setBackground(rounded(SOFT, 9));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(30), dp(30));
        lp.setMargins(dp(3), 0, 0, 0);
        t.setLayoutParams(lp);
        return t;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private class ChannelAdapter extends BaseAdapter {
        @Override public int getCount() { return shown.size(); }
        @Override public Object getItem(int i) { return shown.get(i); }
        @Override public long getItemId(int i) { return i; }

        @Override
        public View getView(int i, View convert, ViewGroup parent) {
            LinearLayout row = new LinearLayout(MainActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(8), dp(8), dp(10), dp(8));

            final Channel c = shown.get(i);
            final int pos = i;
            boolean sel = selected.contains(c.url);
            row.setBackground(rounded(sel ? Color.parseColor("#33405A") : CARD, 14));

            if (editMode) {
                TextView check = new TextView(MainActivity.this);
                check.setText(sel ? "☑" : "☐");
                check.setTextColor(sel ? ACCENT : MUTED);
                check.setTextSize(18);
                check.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(dp(28), dp(28));
                clp.setMargins(0, 0, dp(4), 0);
                check.setLayoutParams(clp);
                row.addView(check);
            }

            TextView no = new TextView(MainActivity.this);
            no.setText(String.valueOf(c.no));
            no.setTextColor(Color.WHITE);
            no.setTextSize(11);
            no.setGravity(Gravity.CENTER);
            no.setBackground(rounded(SOFT, 10));
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(dp(34), dp(28));
            nlp.setMargins(0, 0, dp(8), 0);
            no.setLayoutParams(nlp);
            row.addView(no);

            ImageView logo = new ImageView(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(38), dp(38));
            lp.setMargins(0, 0, dp(10), 0);
            logo.setLayoutParams(lp);
            logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LogoLoader.load(logo, c.logo);
            row.addView(logo);

            LinearLayout texts = new LinearLayout(MainActivity.this);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView name = new TextView(MainActivity.this);
            name.setText(c.flag() + "  " + c.name);
            name.setTextColor(TEXT);
            name.setTextSize(14);
            name.setTypeface(name.getTypeface(), android.graphics.Typeface.BOLD);
            texts.addView(name);

            TextView meta = new TextView(MainActivity.this);
            boolean fav = Prefs.favs(MainActivity.this).contains(c.url);
            String use = "";
            long ub = Prefs.usage(MainActivity.this, c.url);
            if (ub > 0) {
                int us = Prefs.secs(MainActivity.this, c.url);
                use = "  ·  " + Net.mb(ub) + (us > 0 ? " (" + us / 60 + " dk)" : "");
            }
            meta.setText(c.group + (fav ? "  ★" : "")
                    + (Prefs.fails(MainActivity.this, c.url) > 0 ? "  ⚠" : "") + use);
            meta.setTextColor(MUTED);
            meta.setTextSize(11);
            texts.addView(meta);
            row.addView(texts);

            if (editMode) {
                TextView up = arrow("▲");
                up.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { moveItem(pos, -1); }
                });
                row.addView(up);

                TextView down = arrow("▼");
                down.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { moveItem(pos, 1); }
                });
                row.addView(down);
            } else {
                TextView play = new TextView(MainActivity.this);
                play.setText("▶");
                play.setTextColor(Color.WHITE);
                play.setTextSize(13);
                play.setGravity(Gravity.CENTER);
                play.setBackground(rounded(ACCENT, 18));
                play.setLayoutParams(new LinearLayout.LayoutParams(dp(32), dp(32)));
                row.addView(play);
            }

            return row;
        }
    }
}
