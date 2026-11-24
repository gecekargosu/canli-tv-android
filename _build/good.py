# -*- coding: utf-8 -*-
"""Ülke listelerini (iptv-org) tarar, gerçekten açılanları site/public/good/<cc>.json
olarak yazar. Uygulama bu dosyayı indirip çalışan kanalları listenin başına alır.

M-85: sadece master playlist 200 dönmesi yetmez; master ise en düşük varyantın
gerçek segment listesini de kontrol eder (coğrafi kilit / ölü varyant ayıklanır).
Başarısız aday bir kez daha denenir; iki kez düşen listeye girmez.

Kullanım: python _build/good.py [--limit N] us de tr
"""
import sys, os, json, ssl, urllib.request, time
from concurrent.futures import ThreadPoolExecutor
from urllib.parse import urljoin

UA = "Mozilla/5.0 (Linux; Android 14) UMAYMultiTV/9.63"
CTX = ssl.create_default_context()
CTX.check_hostname = False
CTX.verify_mode = ssl.CERT_NONE
OUT = os.path.join("site", "public", "good")
RADIO_GROUPS = ("music", "radio")


def fetch(url, timeout=20):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    return urllib.request.urlopen(req, timeout=timeout, context=CTX).read().decode("utf-8", "ignore")


def _read(url, timeout=8):
    req = urllib.request.Request(url, headers={
        "User-Agent": UA, "Range": "bytes=0-65535", "Accept": "*/*"})
    r = urllib.request.urlopen(req, timeout=timeout, context=CTX)
    data = r.read(65536)
    return r.getcode(), data.decode("utf-8", "ignore")


def _variant_ok(url, text):
    """Master playlist: ilk (en düşük) varyantın segment listesini doğrula."""
    lines = text.splitlines()
    for i, l in enumerate(lines):
        if l.startswith("#EXT-X-STREAM-INF"):
            for j in range(i + 1, min(i + 4, len(lines))):
                cand = lines[j].strip()
                if cand and not cand.startswith("#"):
                    vurl = urljoin(url, cand)
                    # master sorgu parametreliyse (token) varyanta da taşı
                    if "?" not in cand and "?" in url:
                        vurl += "?" + url.split("?", 1)[1]
                    try:
                        _, vt = _read(vurl, timeout=8)
                    except Exception:
                        return False
                    return "#EXTINF" in vt or "#EXT-X-TARGETDURATION" in vt
            return False
    return False


def probe(url):
    try:
        code, data = _read(url)
        if code not in (200, 206) or not data:
            return False
        if "#EXTM3U" not in data:
            # TS/MP4 gibi doğrudan akış: bayt geldiyse çalışıyor say
            return True
        if "#EXT-X-STREAM-INF" in data:
            return _variant_ok(url, data)
        return "#EXTINF" in data or "#EXT-X-TARGETDURATION" in data
    except Exception:
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


def build(cc, limit=200):
    if cc == "tr":
        text = open(os.path.join("assets", "channels.m3u"), encoding="utf-8", errors="ignore").read()
    else:
        src = "uk" if cc == "gb" else cc   # iptv-org'da Birlesik Krallik "uk"
        text = fetch("https://iptv-org.github.io/iptv/countries/%s.m3u" % src, timeout=60)
    items = parse_m3u(text)

    # ayni URL'yi iki kez sorma
    seen, cand = set(), []
    for it in items:
        if it["url"] in seen:
            continue
        seen.add(it["url"])
        cand.append(it)
        if len(cand) >= limit:
            break

    with ThreadPoolExecutor(max_workers=24) as ex:
        ok = list(ex.map(lambda it: probe(it["url"]), cand))
    # ilk turda düşenleri bir kez daha dene (CDN anlık hatası kanalı silmesin)
    retry = [i for i, good in enumerate(ok) if not good]
    if retry:
        with ThreadPoolExecutor(max_workers=16) as ex:
            again = list(ex.map(lambda i: probe(cand[i]["url"]), retry))
        for i, good in zip(retry, again):
            ok[i] = good

    good = [cand[i] for i in range(len(cand)) if ok[i]]
    os.makedirs(OUT, exist_ok=True)
    payload = {"cc": cc, "updated": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
               "checked": len(cand), "items": good}
    path = os.path.join(OUT, cc + ".json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, separators=(",", ":"))
    print("%s: %d/%d calisiyor -> %s (%.1f KB)" % (
        cc.upper(), len(good), len(cand), path, os.path.getsize(path) / 1024), flush=True)
    return len(good)


if __name__ == "__main__":
    args = sys.argv[1:]
    lim = 200
    if "--limit" in args:
        i = args.index("--limit")
        lim = int(args[i + 1])
        del args[i:i + 2]
    codes = args or ["us", "de", "gb", "in", "br", "mx", "ru", "se", "sa"]
    for c in codes:
        try:
            build(c, lim)
        except Exception as e:
            print("%s: HATA %s" % (c.upper(), e), flush=True)
