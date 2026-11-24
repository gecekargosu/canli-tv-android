#!/usr/bin/env python3
"""
Liste sağlığı denetimi (M-14 / M-15).

Kullanım:
    python tools/check-lists.py            # rapor
    python tools/check-lists.py --fail     # sorun varsa çıkış kodu 1

Denetlenenler:
  * Çıplak IP adresli yayın adresleri (kaynağı belirsiz / kısa ömürlü)
  * raw.githubusercontent.com kaynakları (telif güvenliği yok)
  * Ücretli/şifreli marka adları (Cinemax, AXN, BBC First, Star Action, beIN…)
  * http:// yayınlar (HTTPS zorunluluğu; radyo istisnaları hariç)
  * Aynı ada sahip kopya kayıtlar
"""
import glob
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

BANNED_BRANDS = [
    "cinemax", "axn", "bbc first", "star action", "bein", "digiturk",
    "tivibu", "netflix", "disney+", "hbo", "showtime", "paramount+",
]
ALLOW_IP_HOSTS = {"46.20.3.207", "46.20.3.250"}  # TR radyo yayıncıları (bilinçli istisna)
HTTP_OK_HOSTS = ("live.streamtheworld.com", "radyotvonline.net", "arabeskinmerkezi.com",
                 "rtmp.org", "live.net.sa", "46.20.3.207", "46.20.3.250")


def is_bare_ip(host):
    if not host:
        return False
    return bool(re.fullmatch(r"\d{1,3}(\.\d{1,3}){3}", host))


def check_url(url):
    problems = []
    try:
        u = urlparse(url)
    except Exception:
        return ["geçersiz adres"]
    host = (u.hostname or "").lower()
    if is_bare_ip(host) and host not in ALLOW_IP_HOSTS:
        problems.append("çıplak IP")
    if "raw.githubusercontent.com" in host:
        problems.append("raw.githubusercontent")
    if u.scheme == "http" and not any(host == h or host.endswith("." + h) for h in HTTP_OK_HOSTS):
        problems.append("http (HTTPS değil)")
    return problems


def scan_m3u(path, label):
    issues = []
    names = {}
    cur = None
    with open(path, encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.strip()
            if line.startswith("#EXTINF"):
                cur = line.split(",", 1)[-1] if "," in line else ""
                low = cur.lower()
                for b in BANNED_BRANDS:
                    if b in low:
                        issues.append((label, cur, "ücretli marka: " + b))
                names[cur] = names.get(cur, 0) + 1
            elif line and not line.startswith("#") and cur:
                for p in check_url(line):
                    issues.append((label, cur, p))
                cur = None
    for n, c in names.items():
        if c > 1:
            issues.append((label, n, "kopya kayıt x%d" % c))
    return issues


def scan_good(path):
    issues = []
    try:
        data = json.load(open(path, encoding="utf-8"))
    except Exception as e:
        return [(os.path.basename(path), "-", "okunamadı: %s" % e)]
    items = data if isinstance(data, list) else data.get("channels", [])
    for it in items:
        if isinstance(it, str):
            url, name = it, it
        else:
            url = it.get("url", "")
            name = it.get("name", "")
        for p in check_url(url):
            issues.append((os.path.basename(path), name, p))
        for b in BANNED_BRANDS:
            if b in name.lower():
                issues.append((os.path.basename(path), name, "ücretli marka: " + b))
    return issues


def main():
    issues = []
    m3u = os.path.join(ROOT, "assets", "channels.m3u")
    if os.path.exists(m3u):
        issues += scan_m3u(m3u, "channels.m3u")
    radios = os.path.join(ROOT, "assets", "radios.m3u")
    if os.path.exists(radios):
        issues += scan_m3u(radios, "radios.m3u")
    for g in sorted(glob.glob(os.path.join(ROOT, "site", "public", "good", "*.json"))):
        issues += scan_good(g)

    if not issues:
        print("✅ Liste denetimi temiz: çıplak IP / raw.github / ücretli marka / http sorunu yok.")
        return 0
    print("⚠️  %d sorun bulundu:" % len(issues))
    for label, name, problem in issues[:80]:
        print(" - [%s] %s → %s" % (label, name[:48], problem))
    return 1 if "--fail" in sys.argv else 0


if __name__ == "__main__":
    sys.exit(main())
