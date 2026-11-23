# -*- coding: utf-8 -*-
"""Ulke kanal listelerini (iptv-org) ve radyo listelerini (radio-browser) saglik taramasindan gecirir.
Kullanim: python _build/saglik.py us de tr gb in br mx ru se
Sonuc: docs/yayin-saglik-raporu.md
"""
import sys, json, ssl, urllib.request, urllib.error
from concurrent.futures import ThreadPoolExecutor

UA = "Mozilla/5.0 (Linux; Android 14) UMAYMultiTV/9.5"
CTX = ssl.create_default_context()
CTX.check_hostname = False
CTX.verify_mode = ssl.CERT_NONE


def get(url, timeout=8, rng=4096):
    req = urllib.request.Request(url, headers={
        "User-Agent": UA, "Range": "bytes=0-%d" % rng, "Accept": "*/*"})
    r = urllib.request.urlopen(req, timeout=timeout, context=CTX)
    data = r.read(2048)
    return r.getcode(), data


def probe(url):
    try:
        code, data = get(url)
        if code in (200, 206) and len(data) > 0:
            head = data[:200].lstrip()
            if head.startswith(b"#EXTM3U"):
                return "ok(m3u)"
            return "ok"
        return "bos(%s)" % code
    except urllib.error.HTTPError as e:
        return "http%s" % e.code
    except Exception:
        return "olu"


def parse_m3u(text):
    out, name, group = [], None, ""
    for line in text.splitlines():
        line = line.strip()
        if line.startswith("#EXTINF"):
            comma = line.rfind(",")
            name = line[comma + 1:].strip() if comma >= 0 else "?"
            g = ""
            if 'group-title="' in line:
                g = line.split('group-title="', 1)[1].split('"', 1)[0]
            group = g
        elif line and not line.startswith("#") and name:
            out.append((name, group, line))
            name = None
    return out


def check_list(cc, sample=45):
    url = "https://iptv-org.github.io/iptv/countries/%s.m3u" % cc
    try:
        req = urllib.request.Request(url, headers={"User-Agent": UA})
        text = urllib.request.urlopen(req, timeout=30, context=CTX).read().decode("utf-8", "ignore")
    except Exception:
        # Range desteklenmiyorsa tam indir
        try:
            req = urllib.request.Request(url, headers={"User-Agent": UA})
            text = urllib.request.urlopen(req, timeout=30, context=CTX).read().decode("utf-8", "ignore")
        except Exception as e:
            return cc, 0, 0, [], ["liste indirilemedi: %s" % e]
    items = parse_m3u(text)
    total = len(items)
    pick = items[:sample]
    with ThreadPoolExecutor(max_workers=24) as ex:
        results = list(ex.map(lambda it: probe(it[2]), pick))
    alive = sum(1 for r in results if r.startswith("ok"))
    dead = [(pick[i][0], pick[i][2], results[i]) for i in range(len(pick)) if not results[i].startswith("ok")]
    return cc, total, alive, dead, pick


def radio(cc, sample=25):
    url = ("https://de1.api.radio-browser.info/json/stations/search"
           "?countrycode=%s&order=clickcount&reverse=true&limit=%d&hidebroken=true" % (cc, sample))
    try:
        req = urllib.request.Request(url, headers={"User-Agent": UA})
        data = json.loads(urllib.request.urlopen(req, timeout=25, context=CTX).read().decode("utf-8"))
    except Exception as e:
        return 0, 0, ["radyo listesi alinamadi: %s" % e]
    urls = [(s.get("name", "?"), s.get("url_resolved") or s.get("url")) for s in data]
    urls = [(n, u) for n, u in urls if u]
    with ThreadPoolExecutor(max_workers=20) as ex:
        res = list(ex.map(lambda nu: probe(nu[1]), urls))
    alive = sum(1 for r in res if r.startswith("ok"))
    dead = [(urls[i][0], urls[i][1], res[i]) for i in range(len(urls)) if not res[i].startswith("ok")]
    return len(urls), alive, dead


def main():
    codes = sys.argv[1:] or ["us", "de", "tr", "gb", "in", "br", "mx", "ru", "se", "sa"]
    lines = ["# Yayın sağlık raporu", "",
             "Örnekleme: her ülkenin listesinden ilk 45 kanal (iptv-org sırası).", ""]
    rows = []
    for cc in codes:
        c, total, alive, dead, _ = check_list(cc)
        rt, ra, rdead = radio(cc)
        rows.append((cc, total, alive, len(dead), rt, ra))
        lines.append("## %s" % cc.upper())
        lines.append("- kanal listesi: **%d** kayıt, örneklenen 45 içinde **%d açıldı**" % (total, alive))
        if dead:
            lines.append("- açılmayan örnekler:")
            for n, u, r in dead[:8]:
                lines.append("  - `%s` → %s" % (n[:40], r))
        lines.append("- radyo: **%d** istasyon denendi, **%d açıldı**" % (rt, ra))
        if rdead:
            lines.append("- açılmayan radyo örnekleri:")
            for n, u, r in rdead[:5]:
                lines.append("  - `%s` → %s" % (n[:40], r))
        lines.append("")
    lines.append("## Özet")
    lines.append("| Ülke | Kanal | Örnekte açılan | Radyo | Radyo açılan |")
    lines.append("|---|---|---|---|---|")
    for cc, total, alive, deadn, rt, ra in rows:
        lines.append("| %s | %d | %d/45 | %d | %d |" % (cc.upper(), total, alive, rt, ra))
    out = "docs/yayin-saglik-raporu.md"
    open(out, "w", encoding="utf-8").write("\n".join(lines) + "\n")
    print("\n".join(lines[-len(rows) - 3:]))
    print("yazildi:", out)


if __name__ == "__main__":
    main()
