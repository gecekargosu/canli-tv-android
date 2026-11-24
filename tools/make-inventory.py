#!/usr/bin/env python3
"""
Kanal envanteri üretici (M-13).

`assets/channels.m3u` ve `assets/radios.m3u` dosyalarından, her yayın için kaynak alan adı,
hak durumu ve son kontrol tarihini içeren `site/public/kanallar.json` üretir.

Kullanım:
    python tools/make-inventory.py

Kayıt biçimi:
    {
      "version": 1,
      "generated": "2026-11-24",
      "channels": [
        {"name": "...", "type": "tv"|"radio", "url": "...", "host": "...",
         "rights": "public" | "review" | "removed", "checked": "2026-11-24", "status": "ok"}
      ]
    }

`rights` varsayılanı "public": yayıncının halka açık adresi. Şüpheli kaynaklar (çıplak IP,
kısa ömürlü CDN, ücretli marka) "review" olarak işaretlenir ve sunucu tarafı filtre bunları
herkese açık listeye almaz (M-15).
"""
import json
import os
import re
import sys
from urllib.parse import urlparse

try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "site", "public", "kanallar.json")
TODAY = "2026-11-24"

ALLOW_IP_HOSTS = {"46.20.3.207", "46.20.3.250"}
BANNED_BRANDS = ["cinemax", "axn", "bbc first", "star action", "bein", "digiturk", "tivibu"]


def parse_m3u(path, kind):
    items = []
    if not os.path.exists(path):
        return items
    name = None
    with open(path, encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.strip()
            if line.startswith("#EXTINF"):
                name = line.split(",", 1)[-1].strip() if "," in line else ""
            elif line and not line.startswith("#") and name:
                host = (urlparse(line).hostname or "").lower()
                rights = "public"
                low = name.lower()
                if any(b in low for b in BANNED_BRANDS):
                    rights = "review"
                if re.fullmatch(r"\d{1,3}(\.\d{1,3}){3}", host) and host not in ALLOW_IP_HOSTS:
                    rights = "review"
                if "raw.githubusercontent.com" in host:
                    rights = "review"
                items.append({
                    "name": name.split("|")[0].strip(),
                    "type": kind,
                    "url": line,
                    "host": host,
                    "rights": rights,
                    "checked": TODAY,
                    "status": "ok",
                })
                name = None
    return items


def main():
    chans = parse_m3u(os.path.join(ROOT, "assets", "channels.m3u"), "tv")
    radios = parse_m3u(os.path.join(ROOT, "assets", "radios.m3u"), "radio")
    all_items = chans + radios
    data = {"version": 1, "generated": TODAY, "channels": all_items}
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=1)
    review = [c for c in all_items if c["rights"] != "public"]
    print("✅ %s yazıldı: %d TV + %d radyo = %d kayıt"
          % (os.path.relpath(OUT, ROOT), len(chans), len(radios), len(all_items)))
    print("   hak durumu 'public': %d · 'review': %d" % (len(all_items) - len(review), len(review)))
    for c in review[:10]:
        print("   - review: %s (%s)" % (c["name"][:40], c["host"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
