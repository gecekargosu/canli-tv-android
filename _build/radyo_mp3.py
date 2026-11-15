# -*- coding: utf-8 -*-
"""TR radyolarini MP3/codec bilgisiyle ceker, calisanlari test eder, radios.m3u yazar."""
import json, io, re, urllib.request, urllib.parse

BASE = "https://de1.api.radio-browser.info"
OUT = r"C:\Users\isitm\Desktop\CANLI-TV-APP\assets\radios.m3u"

def api(path):
    r = urllib.request.Request(BASE + path, headers={"User-Agent": "UmayMultiTV/2"})
    return json.load(urllib.request.urlopen(r, timeout=40))

stations = api("/json/stations/bycountrycodeexact/TR?limit=400&hidebroken=true&order=clickcount&reverse=true")
for q in ("karadeniz", "rize", "trabzon", "artvin", "kuran", "mekke"):
    try:
        stations += api("/json/stations/search?name=%s&limit=12&hidebroken=true" % urllib.parse.quote(q))
    except Exception:
        pass

WANT = ["trt fm", "trt radyo 1", "trt turku", "trt nagme", "kral fm", "kral pop", "kral turk",
        "power fm", "powerturk", "power turk", "super fm", "metro fm", "joy fm", "joy turk",
        "radyo 7", "radyo fenomen", "best fm", "show radyo", "ntv radyo", "radyo spor",
        "slow turk", "slowturk", "pal fm", "virgin", "alem fm", "turkuvaz", "kalp fm",
        "kafa radyo", "damar", "karadeniz", "rize", "trabzon", "artvin", "kuran", "mekke",
        "mekke", "risalet", "diyanet", "radyo 24", "number one"]

def norm(t):
    t = (t or "").lower()
    for a, b in (("ı", "i"), ("ş", "s"), ("ğ", "g"), ("ü", "u"), ("ö", "o"), ("ç", "c"), ("â", "a")):
        t = t.replace(a, b)
    return t

def wanted(s):
    n = norm(s.get("name"))
    return any(w in n for w in WANT)

def probe(url):
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "VLC/3.0", "Icy-MetaData": "0"})
        r = urllib.request.urlopen(req, timeout=6)
        data = r.read(200)
        ct = (r.headers.get("Content-Type") or "").lower()
        r.close()
        if len(data) < 40:
            return False
        if "mpegurl" in ct:
            return False  # HLS radyolar bu cihazda guvenilir degil
        return True
    except Exception:
        return False

rows, seen = [], set()
for s in stations:
    if not wanted(s):
        continue
    name = re.sub(r"\s+", " ", (s.get("name") or "").strip())
    key = norm(name)
    if key in seen:
        continue
    url = (s.get("url_resolved") or s.get("url") or "").strip()
    if not url.startswith("http"):
        continue
    codec = (s.get("codec") or "").upper()
    if codec and codec not in ("MP3", "AAC", "AAC+", "MP3,H.264"):
        continue
    if not probe(url):
        print("yok:", name[:32], url[:50])
        continue
    seen.add(key)
    kb = s.get("bitrate") or 0
    meta = "%s %s" % (codec if codec else "MP3", ("· %s kbps" % kb) if kb else "")
    extra = (s.get("state") or "").strip() or "Türkiye"
    rows.append((name, meta.strip(), extra[:28], url))

ORDER = ["trt", "kral", "power", "super", "metro", "joy", "radyo 7", "fenomen", "best",
         "show", "ntv", "spor", "slow", "pal", "virgin", "alem", "turkuvaz", "kalp",
         "kafa", "damar", "karadeniz", "rize", "trabzon", "artvin", "kuran", "mekke"]
def rank(n):
    nn = norm(n)
    for i, w in enumerate(ORDER):
        if w in nn:
            return i
    return 99
rows.sort(key=lambda r: (rank(r[0]), norm(r[0])))

out = ["#EXTM3U"]
for name, meta, extra, url in rows:
    out.append("#EXTINF:-1,%s | %s | %s" % (name, meta, extra))
    out.append(url)
io.open(OUT, "w", encoding="utf-8", newline="\n").write("\n".join(out) + "\n")
print("\nYAZILDI:", len(rows), "radyo")
for r in rows:
    print(" -", r[0].encode("ascii", "replace").decode(), "|", r[1], "|", r[3][:46])
