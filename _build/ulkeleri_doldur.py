# -*- coding: utf-8 -*-
"""M-85: öncelikli ülkelerin listelerini topluca üretir.

Sıra: yüksek nüfus + yüksek reklam getirisi olan ülkeler önce.
Kullanım: python _build/ulkeleri_doldur.py
"""
import sys
import json
import glob
import os
from concurrent.futures import ThreadPoolExecutor

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import good  # noqa: E402

# (ülke kodu, denenecek aday sayısı)
PLAN = [
    ("gb", 360), ("us", 460), ("de", 240), ("fr", 240), ("it", 220),
    ("es", 220), ("in", 260), ("br", 240), ("mx", 240), ("ru", 240),
    ("se", 240), ("sa", 240), ("id", 260), ("pk", 240), ("ng", 240),
    ("ph", 220), ("vn", 220), ("eg", 220), ("jp", 240), ("kr", 220),
    ("ca", 240), ("ma", 220), ("ua", 240), ("iq", 220), ("dz", 220),
    ("ar", 240), ("cn", 240), ("au", 240), ("nl", 220), ("ro", 220),
    ("kz", 220), ("ir", 220), ("jo", 200), ("bd", 220), ("ae", 220),
    ("ch", 200), ("at", 200), ("il", 200), ("gr", 200), ("ie", 200),
    ("dk", 200), ("fi", 200), ("bg", 200), ("rs", 180), ("ge", 180),
    ("hr", 180), ("qa", 180), ("om", 180), ("lb", 180), ("by", 180),
    ("al", 180), ("az", 180), ("ee", 180), ("sy", 160),
]


def one(job):
    cc, lim = job
    try:
        return cc, good.build(cc, lim)
    except Exception as e:
        print("%s: HATA %s" % (cc.upper(), e), flush=True)
        return cc, -1


def main():
    with ThreadPoolExecutor(max_workers=2) as ex:
        for cc, n in ex.map(one, PLAN):
            print("BITTI %s -> %d" % (cc.upper(), n), flush=True)

    codes = sorted(os.path.basename(p)[:-5]
                   for p in glob.glob(os.path.join(good.OUT, "*.json"))
                   if not p.endswith("index.json"))
    with open(os.path.join(good.OUT, "index.json"), "w", encoding="utf-8") as f:
        json.dump({"codes": codes}, f, ensure_ascii=False, separators=(",", ":"))
    print("INDEX %d ulke: %s" % (len(codes), ", ".join(codes)), flush=True)


if __name__ == "__main__":
    main()
