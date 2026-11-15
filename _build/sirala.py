# -*- coding: utf-8 -*-
"""channels.m3u sirasini duzenler: buyuk Turk kanallari en uste."""
import io, re, sys

SRC = r"C:\Users\isitm\Desktop\CANLI-TV-APP\assets\channels.m3u"

lines = io.open(SRC, encoding="utf-8").read().split("\n")
head, items, i = [], [], 0
while i < len(lines):
    l = lines[i].rstrip("\r")
    if l.startswith("#EXTM3U"):
        head.append(l); i += 1; continue
    if l.startswith("#EXTINF"):
        url = lines[i + 1].strip() if i + 1 < len(lines) else ""
        if url and not url.startswith("#"):
            items.append([l, url]); i += 2; continue
    i += 1

def nm(ext):
    return ext.rsplit(",", 1)[-1].strip()

def cn(ext):
    m = re.search(r'tvg-country="([^"]*)"', ext)
    return (m.group(1) if m else "").lower()

PRIO = [
    "trt 1", "trt 2", "tv8", "atv", "show tv", "star tv", "now tv", "kanal d",
    "trt haber", "haber global", "cnn türk", "ntv", "a haber", "halk tv",
    "sözcü tv", "tele1", "tv100", "kanal 7", "trt spor", "trt müzik",
    "trt çocuk", "trt belgesel", "trt avaz", "trt türk", "trt world",
    "24 tv", "a spor", "a2tv", "teve2", "beyaz tv", "kanal 24",
    "tvnet", "ulke tv", "kanal b", "flash tv", "tgrthaber", "habertürk",
    "ulusal kanal", "kanal v", "kent türk", "tv 24",
]

def rank(ext):
    """Once oncelik sirasi, sonra tam eslesme tercih edilir."""
    n = nm(ext).lower()
    n0 = re.sub(r"\s*\(.*?\)\s*", "", n).strip()
    best = (999, 9)
    for k, p in enumerate(PRIO):
        if n0 == p or n == p:
            best = min(best, (k, 0))
        elif n0.startswith(p) or n.startswith(p):
            best = min(best, (k, 1))
    return best

tr = [it for it in items if cn(it[0]) == "tr"]
other = [it for it in items if cn(it[0]) != "tr"]

tr.sort(key=lambda it: (rank(it[0]), nm(it[0]).lower()))
other.sort(key=lambda it: (cn(it[0]), nm(it[0]).lower()))
ordered = tr + other

out = list(head) if head else ["#EXTM3U"]
for ext, url in ordered:
    out.append(ext)
    out.append(url)
io.open(SRC, "w", encoding="utf-8", newline="\n").write("\n".join(out) + "\n")

print("toplam:", len(ordered), "| tr:", len(tr), "| diger:", len(other))
print("--- ilk 22 ---")
for n, (ext, _) in enumerate(ordered[:22], 1):
    print(n, nm(ext), "|", cn(ext))
