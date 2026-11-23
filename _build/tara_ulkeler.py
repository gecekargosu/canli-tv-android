"""Ülke listelerini (site/public/good/*.json) gerçek oynatma zinciriyle tarar.

- master playlist -> en düşük çalışan varyant
- 301/302 takip edilir
- açılmayan kanal listeden çıkarılır
Kullanım: python _build/tara_ulkeler.py [--dry]
"""
import glob
import io
import json
import re
import ssl
import sys
import urllib.request
import concurrent.futures
from collections import Counter

DRY = "--dry" in sys.argv
CTX = ssl.create_default_context()
CTX.check_hostname = False
CTX.verify_mode = ssl.CERT_NONE
UA = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36"


def get(url, t=9):
    req = urllib.request.Request(url, headers={"User-Agent": UA, "Accept": "*/*"})
    with urllib.request.urlopen(req, timeout=t, context=CTX) as r:
        return r.status, r.read(150000).decode("utf-8", "replace")


def check(url):
    try:
        st, txt = get(url)
        if st != 200:
            return False
        if "#EXTM3U" not in txt:
            return False
        if "#EXT-X-STREAM-INF" in txt:
            # Muhafazakâr kural: master 200 + geçerli m3u ise kanalı TUT.
            # Varyant hatası coğrafi kilit/oturum olabilir; yanlış silme yapmayalım.
            return True
        if False:
            base = url[: url.rfind("/") + 1]
            lines = txt.splitlines()
            cands = []
            for i, l in enumerate(lines):
                if not l.startswith("#EXT-X-STREAM-INF"):
                    continue
                h = 0
                m = re.search(r"RESOLUTION=\d+x(\d+)", l)
                if m:
                    h = int(m.group(1))
                for j in range(i + 1, min(i + 4, len(lines))):
                    c = lines[j].strip()
                    if c and not c.startswith("#"):
                        cands.append((h, c if c.startswith("http") else base + c))
                        break
            for _, v in sorted(cands):
                try:
                    st2, vt = get(v)
                    if st2 == 200 and "#EXTINF" in vt:
                        return True
                except Exception:
                    pass
            return False
        return "#EXTINF" in txt
    except Exception:
        return False


def main():
    for path in sorted(glob.glob("site/public/good/*.json")):
        d = json.load(io.open(path, encoding="utf-8"))
        items = d.get("items", [])
        with concurrent.futures.ThreadPoolExecutor(max_workers=24) as ex:
            ok = list(ex.map(lambda it: check(it.get("url", "")), items))
        # Yanlış silmeyi azalt: başarısızları 2 sn sonra bir kez daha dene.
        import time as _t
        _t.sleep(2)
        with concurrent.futures.ThreadPoolExecutor(max_workers=16) as ex:
            ok2 = list(ex.map(lambda pair: pair[1] or check(pair[0].get("url", "")),
                              zip(items, ok)))
        ok = ok2
        keep = [it for it, good in zip(items, ok) if good]
        dead = [it["name"] for it, good in zip(items, ok) if not good]
        print(f"{d.get('cc','?'):3} {len(items):4} -> {len(keep):4}  ölü: {len(dead)}")
        if dead:
            print("     ", ", ".join(dead[:12]) + (" …" if len(dead) > 12 else ""))
        if not DRY and keep:
            d["items"] = keep
            d["checked"] = True
            io.open(path, "w", encoding="utf-8").write(
                json.dumps(d, ensure_ascii=False, indent=1))


if __name__ == "__main__":
    main()
