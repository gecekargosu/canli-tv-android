# -*- coding: utf-8 -*-
"""Ülke listelerini (iptv-org) tarar, gerçekten açılanları site/public/good/<cc>.json
olarak yazar. Uygulama bu dosyayı indirip çalışan kanalları listenin başına alır.

Kullanım: python _build/good.py us de tr
"""
import sys, os, json, ssl, urllib.request, urllib.error, time
from concurrent.futures import ThreadPoolExecutor

UA = "Mozilla/5.0 (Linux; Android 14) UMAYMultiTV/9.11"
CTX = ssl.create_default_context()
CTX.check_hostname = False
CTX.verify_mode = ssl.CERT_NONE
OUT = os.path.join("site", "public", "good")


def fetch(url, timeout=15):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    return urllib.request.urlopen(req, timeout=timeout, context=CTX).read().decode("utf-8", "ignore")


def probe(url):
    try:
        req = urllib.request.Request(url, headers={
            "User-Agent": UA, "Range": "bytes=0-4095", "Accept": "*/*"})
        r = urllib.request.urlopen(req, timeout=7, context=CTX)
        data = r.read(2048)
        if r.getcode() in (200, 206) and len(data) > 0:
            return True
    except Exception:
        return False
    return False


def parse_m3u(text):
    out, name, group, logo = [], None, "", ""
    for line in text.splitlines():
        line = line.strip()
        if line.startswith("#EXTINF"):
            comma = line.rfind(",")
            name = line[comma + 1:].strip() if comma >= 0 else "?"
            group = ""
            if 'group-title="' in line:
                group = line.split('group-title="', 1)[1].split('"', 1)[0]
            logo = ""
            if 'tvg-logo="' in line:
                logo = line.split('tvg-logo="', 1)[1].split('"', 1)[0]
        elif line and not line.startswith("#") and name:
            out.append({"name": name, "group": group, "logo": logo, "url": line})
            name = None
    return out


def build(cc, limit=90):
    if cc == "tr":
        text = open(os.path.join("assets", "channels.m3u"), encoding="utf-8", errors="ignore").read()
    else:
        src = "uk" if cc == "gb" else cc   # iptv-org'da Birlesik Krallik "uk"
        text = fetch("https://iptv-org.github.io/iptv/countries/%s.m3u" % src, timeout=40)
    items = parse_m3u(text)
    cand = items[:limit]
    with ThreadPoolExecutor(max_workers=24) as ex:
        ok = list(ex.map(lambda it: probe(it["url"]), cand))
    good = [cand[i] for i in range(len(cand)) if ok[i]]
    os.makedirs(OUT, exist_ok=True)
    payload = {"cc": cc, "updated": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
               "checked": len(cand), "items": good}
    path = os.path.join(OUT, cc + ".json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, separators=(",", ":"))
    print("%s: %d/%d calisiyor -> %s (%.1f KB)" % (
        cc.upper(), len(good), len(cand), path, os.path.getsize(path) / 1024))
    return len(good)


if __name__ == "__main__":
    codes = sys.argv[1:] or ["us", "de", "gb", "in", "br", "mx", "ru", "se", "sa"]
    for c in codes:
        try:
            build(c)
        except Exception as e:
            print("%s: HATA %s" % (c.upper(), e))
