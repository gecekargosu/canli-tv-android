# -*- coding: utf-8 -*-
"""M-85 eki: planın ikinci yarısını paralel üretir (index.json yazmaz)."""
import sys
import os
from concurrent.futures import ThreadPoolExecutor

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import good  # noqa: E402

PLAN = [
    ("cn", 240), ("au", 240), ("nl", 220), ("ro", 220), ("kz", 220),
    ("ir", 220), ("jo", 200), ("bd", 220), ("ae", 220), ("ch", 200),
    ("at", 200), ("il", 200), ("gr", 200), ("ie", 200), ("dk", 200),
    ("fi", 200), ("bg", 200), ("rs", 180), ("ge", 180), ("hr", 180),
    ("qa", 180), ("om", 180), ("lb", 180), ("by", 180), ("al", 180),
    ("az", 180), ("ee", 180), ("sy", 160),
]


def one(job):
    cc, lim = job
    try:
        return cc, good.build(cc, lim)
    except Exception as e:
        print("%s: HATA %s" % (cc.upper(), e), flush=True)
        return cc, -1


with ThreadPoolExecutor(max_workers=2) as ex:
    for cc, n in ex.map(one, PLAN):
        print("BITTI2 %s -> %d" % (cc.upper(), n), flush=True)
