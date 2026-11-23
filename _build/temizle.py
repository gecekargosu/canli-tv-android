#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""UMAY Multi TV — gömülü kanal listesi telif riski temizliği (P0-1).

Kurallar (Claude raporu P0-1 + kendi kararımız):
  1) "Sinema" grubu tamamen çıkar (ücretli film kanallarının izinsiz yeniden yayını)
  2) Çıplak IP adresinden gelen yayınlar çıkar (yayıncının resmi dağıtımı değil)
  3) raw.githubusercontent.com kaynaklı yayınlar çıkar (ömrü belirsiz, resmi değil)
  4) Abonelik/ücretli marka adları çıkar (Cinemax, AXN, BBC First, beIN, Digiturk…)
  5) Yetişkin (xxx/adult/erotic) kayıtlar çıkar
  6) Kanal adlarındaki [Geo-blocked], [Not 24/7], (1080p) gibi ekler temizlenir
  7) http:// (şifresiz) kaynaklar KORUNUR ama raporda sayılır (bazı resmi TR yayınları http)

Kullanım:
  python _build/temizle.py            # kuru çalışma: sadece rapor
  python _build/temizle.py --yaz      # assets/channels.m3u dosyasını yeniden yaz
"""
import re
import sys
import collections

SRC = "assets/channels.m3u"

SUBSCRIPTION = re.compile(
    r"cinemax|axn\b|bbc first|star action|discovery|film\+|hbo\b|netflix|bein|s sport|"
    r"tivibu|d-smart|dsmart|digiturk|viju|tv1000|nickelodeon|cartoon network|nat geo|"
    r"fox crime|fox life|fx\b|amc\b|paramount|sony (channel|one|max)|mtv live|eurosport|dazn",
    re.I)
ADULT = re.compile(r"xxx|adult|erotic|porn|18\+", re.I)
BARE_IP = re.compile(r"^https?://\d{1,3}(\.\d{1,3}){3}(:\d+)?(/|$)")
TAGS = re.compile(r"\s*\[[^\]]*\]|\s*\(\d{3,4}p\)")


def parse(path):
    items, cur = [], None
    for line in open(path, encoding="utf-8", errors="ignore"):
        s = line.strip()
        if s.startswith("#EXTINF"):
            info = s
            name = info.rsplit(",", 1)[-1].strip()
            g = re.search(r'group-title="([^"]*)"', info)
            l = re.search(r'tvg-logo="([^"]*)"', info)
            cur = {"info": info, "name": name, "group": g.group(1) if g else "",
                   "logo": l.group(1) if l else ""}
        elif s and not s.startswith("#") and cur:
            cur["url"] = s
            items.append(cur)
            cur = None
    return items


def reason(it):
    url, name, group = it["url"], it["name"], it["group"]
    if group.lower() == "sinema":
        return "sinema-grubu"
    if BARE_IP.match(url):
        return "ciplak-ip"
    if "raw.githubusercontent" in url:
        return "github-ham"
    if SUBSCRIPTION.search(name):
        return "abonelik-markasi"
    if ADULT.search(name + " " + group):
        return "yetiskin"
    if not url.startswith("http"):
        return "gecersiz-url"
    return None


def main():
    write = "--yaz" in sys.argv
    items = parse(SRC)
    keep, drop = [], collections.Counter()
    for it in items:
        r = reason(it)
        if r:
            drop[r] += 1
        else:
            it["name"] = TAGS.sub("", it["name"]).strip()
            keep.append(it)

    print("once: %d kanal" % len(items))
    for k, v in drop.most_common():
        print("  cikarildi %-18s %3d" % (k, v))
    print("sonra: %d kanal" % len(keep))
    groups = collections.Counter(i["group"] for i in keep)
    print("gruplar:", groups.most_common())
    http = sum(1 for i in keep if i["url"].startswith("http://"))
    print("kalan http (sifresiz):", http)
    ip = sum(1 for i in keep if BARE_IP.match(i["url"]))
    print("kalan ciplak IP:", ip)

    if write:
        out = []
        for it in keep:
            info = it["info"]
            # adı etiketlerden arındırılmış haliyle yaz
            info = info[: info.rfind(",") + 1] + it["name"]
            out.append(info)
            out.append(it["url"])
        with open(SRC, "w", encoding="utf-8", newline="\n") as f:
            f.write("\n".join(out) + "\n")
        print("YAZILDI:", SRC)
    else:
        print("(kuru çalışma — yazmak için --yaz)")


if __name__ == "__main__":
    main()
