import io

p = 'src/com/cengiz/canlitv/HlsRecorder.java'
raw = io.open(p, encoding='utf-8', newline='').read()
crlf = '\r\n' in raw
s = raw.replace('\r\n', '\n')


def rep(old, new):
    global s
    assert s.count(old) == 1, ('bulunamadi', s.count(old), old[:70])
    s = s.replace(old, new)


rep(
    '''                    String media = resolveMediaPlaylist(playlistUrl);
                    if (media == null) { fail("Yayin adresi okunamadi"); return; }
                    if (media.contains("METHOD=AES-128")) { fail("Sifreli yayin kaydedilemiyor"); return; }
''',
    '''                    String media = resolveMediaPlaylist(ctx, playlistUrl);
                    if (media == null) { fail("Yayin adresi okunamadi"); return; }
                    if (media.contains("METHOD=AES-128")) { fail("Sifreli yayin kaydedilemiyor"); return; }
                    // M-26: fMP4 (EXT-X-MAP) ve byte-range yayinlar .ts konteynerine
                    // yazilamaz; eskiden dosya "kaydedildi" gorunup acilmiyordu.
                    if (media.contains("#EXT-X-MAP")) {
                        fail("Bu kanal parcali (fMP4) yayin veriyor; kayit desteklenmiyor"); return;
                    }
                    if (media.contains("#EXT-X-BYTERANGE")) {
                        fail("Bu kanal byte-range yayin veriyor; kayit desteklenmiyor"); return;
                    }
                    // M-26: yer kontrolu - kayit ortasinda "yer yok" ile kesilmesin.
                    long free = freeBytes();
                    if (free >= 0 && free < 500L * 1048576L) {
                        fail("Yer yok: " + (free / 1048576L) + " MB bos, en az 500 MB gerekli"); return;
                    }
''')

rep(
    '''                        if (pl == null) { sleep(3000); continue; }
                        if (pl.contains("METHOD=AES-128")) { fail("Sifreli yayin kaydedilemiyor"); return; }
''',
    '''                        if (pl == null) { sleep(3000); continue; }
                        if (pl.contains("METHOD=AES-128")) { fail("Sifreli yayin kaydedilemiyor"); return; }
                        if (pl.contains("#EXT-X-MAP")) {
                            fail("Yayin fMP4'e gecti; kayit desteklenmiyor"); return;
                        }
                        if (pl.contains("#EXT-X-BYTERANGE")) {
                            fail("Yayin byte-range'e gecti; kayit desteklenmiyor"); return;
                        }
''')

rep(
    '''                    List<String> seen = new ArrayList<String>();
                    String base = media.substring(0, media.lastIndexOf('/') + 1);''',
    '''                    List<String> seen = new ArrayList<String>();
                    String base = media.substring(0, media.lastIndexOf('/') + 1);
                    String token = media.contains("?")
                            ? media.substring(media.indexOf('?')) : "";
                    int tokenLen = token.length();''')

rep(
    '''                            String segUrl = line.startsWith("http") ? line : base + line;
                            byte[] data = fetchBytes(segUrl);''',
    '''                            String segUrl = line.startsWith("http") ? line : base + line;
                            // M-26: master "?token=..." tasiyorsa segment de tasimali
                            // (jetonlu CDN'lerde segment 403 veriyordu).
                            if (!line.contains("?") && tokenLen > 0) segUrl += token;
                            byte[] data = fetchBytes(segUrl);''')

old5 = '''    private String resolveMediaPlaylist(String url) {
        String txt = fetch(url);
        if (txt == null) return null;
        if (!txt.contains("#EXT-X-STREAM-INF")) return url;
        String base = url.substring(0, url.lastIndexOf('/') + 1);
        String[] lines = txt.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("#EXT-X-STREAM-INF")) {
                for (int j = i + 1; j < lines.length; j++) {
                    String c = lines[j].trim();
                    if (c.length() == 0 || c.startsWith("#")) continue;
                    String v = c.startsWith("http") ? c : base + c;
                    if (!c.contains("?") && url.contains("?"))
                        v += "?" + url.substring(url.indexOf('?') + 1);
                    return v;
                }
            }
        }
        return url;
    }'''

new5 = '''    /**
     * Master playlist ise **kalite tercihine uygun** varyanti secer (M-26).
     * Eskiden her zaman ilk varyant (genelde 1080p) indiriliyordu; 360p tercih eden
     * kullanici kayitta saatte GB'larca veri harciyordu.
     */
    private String resolveMediaPlaylist(Context ctx, String url) {
        String txt = fetch(url);
        if (txt == null) return null;
        if (!txt.contains("#EXT-X-STREAM-INF")) return url;
        String base = url.substring(0, url.lastIndexOf('/') + 1);
        String token = url.contains("?") ? url.substring(url.indexOf('?')) : "";
        String[] lines = txt.split("\\n");
        List<Quality.Variant> variants = new ArrayList<Quality.Variant>();
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].startsWith("#EXT-X-STREAM-INF")) continue;
            int height = 0;
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("RESOLUTION=(\\\\d+)x(\\\\d+)").matcher(lines[i]);
            if (m.find()) height = Integer.parseInt(m.group(2));
            for (int j = i + 1; j < lines.length; j++) {
                String c = lines[j].trim();
                if (c.length() == 0 || c.startsWith("#")) continue;
                String v = c.startsWith("http") ? c : base + c;
                if (!c.contains("?") && token.length() > 0) v += token;
                variants.add(new Quality.Variant(height > 0 ? height + "p" : "varyant", v, height));
                break;
            }
        }
        if (variants.isEmpty()) return url;
        int pref = Prefs.economy(ctx) ? -1 : Prefs.maxHeight(ctx);
        String chosen = Quality.pick(variants, pref);
        return chosen == null ? url : chosen;
    }

    /** Kayit hedefinde kalan bos alan (bayt); bilinmiyorsa -1. */
    private long freeBytes() {
        try {
            return new android.os.StatFs(
                    Environment.getExternalStorageDirectory().getAbsolutePath())
                    .getAvailableBytes();
        } catch (Throwable t) { return -1; }
    }'''

rep(old5, new5)

io.open(p, 'w', encoding='utf-8', newline='').write(
    s.replace('\n', '\r\n') if crlf else s)
print('HlsRecorder yamalandi')
