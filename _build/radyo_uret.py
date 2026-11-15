# -*- coding: utf-8 -*-
"""radio-browser.info'dan TR radyolarini cekip URL'leri test eder, assets/radios.m3u uretir."""
import json, io, urllib.request, urllib.parse, re

BASE = "https://de1.api.radio-browser.info"
OUT = r"C:\Users\isitm\Desktop\CANLI-TV-APP\assets\radios.m3u"

def api(path):
    req = urllib.request.Request(BASE + path, headers={"User-Agent": "UmayMultiTV/1.0"})
    return json.load(urllib.request.urlopen(req, timeout=35))

stations = []
for lim, tag in ((200, None),):
    stations += api("/json/stations/bycountrycodeexact/TR?limit=%d&hidebroken=true&order=clickcount&reverse=true" % lim)
for city in ("rize", "trabzon", "artvin", "karadeniz"):
    try:
        stations += api("/json/stations/search?name=%s&limit=15&hidebroken=true" % urllib.parse.quote(city))
    except Exception as e:
        print("arama hata", city, e)

# ayni url'i tekrarla
seen, uniq = set(), []
for s in stations:
    u = (s.get("url_resolved") or s.get("url") or "").strip()
    if not u.startswith("http") or u in seen:
        continue
    seen.add(u)
    uniq.append(s)

WANT = [
    "trt radyo 1", "trt fm", "trt radyo 3", "trt nagme", "trt turku", "trt world radyo",
    "kral fm", "kral pop", "kral turk", "power fm", "power turk", "powerturk",
    "super fm", "metro fm", "joy fm", "joy turk", "radyo 7", "radyo fenomen",
    "best fm", "show radyo", "ntv radyo", "radyo spor", "slow turk", "slowturk",
    "pal fm", "pal station", "virgin radio", "alem fm", "radyo turkuvaz",
    "kalp fm", "kafa radyo", "damar", "karadeniz", "rize", "trabzon", "artvin",
    "radyonom", "number one", "radyo mega", "safir", "baba radyo", "gul radyo",
]

def norm(t):
    t = (t or "").lower()
    for a, b in (("ı", "i"), ("ş", "s"), ("ğ", "g"), ("ü", "u"), ("ö", "o"), ("ç", "c"), ("â", "a")):
        t = t.replace(a, b)
    return t

def matches(s):
    n = norm(s.get("name"))
    return any(w in n for w in WANT)

def probe(url):
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "VLC/3.0.20", "Icy-MetaData": "0"})
        r = urllib.request.urlopen(req, timeout=7)
        ct = (r.headers.get("Content-Type") or "").lower()
        data = r.read(400)
        r.close()
        if len(data) < 20:
            return False
        if "audio" in ct or "mpegurl" in ct or "ogg" in ct:
            return True
        if data[:3] == b"ID3" or data[:1] == b"\xff" or data[:4] == b"#EXT" or data[:4] == b"OggS":
            return True
        return "text/html" not in ct and len(data) > 100
    except Exception:
        return False

rows, seen_name = [], set()
for s in uniq:
    if not matches(s):
        continue
    name = re.sub(r"\s+", " ", (s.get("name") or "").strip())
    key = norm(name)
    if key in seen_name:
        continue
    url = (s.get("url_resolved") or s.get("url")).strip()
    print("test:", name[:36], end=" ... ", flush=True)
    if not probe(url):
        print("YOK")
        continue
    print("OK")
    freq = s.get("frequency") or ""
    city = s.get("state") or s.get("country") or ""
    tags = (s.get("tags") or "").split(",")[:2]
    meta = (freq + " MHz" if freq else "internet").strip()
    extra = " · ".join([x for x in [city.strip(), ", ".join(t.strip() for t in tags if t.strip())] if x])
    rows.append((name, meta, extra[:40] or "Türkiye", url))
    seen_name.add(key)

WANT_ORDER = ["trt", "kral", "power", "super", "metro", "joy", "radyo 7", "fenomen",
              "best", "show", "ntv", "spor", "slow", "pal", "virgin", "alem",
              "turkuvaz", "kalp", "kafa", "damar", "karadeniz", "rize", "trabzon", "artvin"]
def rank(n):
    nn = norm(n)
    for i, w in enumerate(WANT_ORDER):
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
    print(" -", r[0], "|", r[1], "|", r[2])
