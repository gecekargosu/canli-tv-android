# -*- coding: utf-8 -*-
"""ui.xml icindeki metinleri ve dokunma noktalarini yazar: python ui_dump.py ui.xml [maxY]"""
import re, sys, io

path = sys.argv[1] if len(sys.argv) > 1 else "docs/ui.xml"
maxy = int(sys.argv[2]) if len(sys.argv) > 2 else 3000
s = io.open(path, encoding="utf-8", errors="ignore").read()
pat = re.compile(r'text="([^"]{1,40})"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
for m in pat.finditer(s):
    t = m.group(1)
    x1, y1, x2, y2 = (int(m.group(i)) for i in range(2, 6))
    if y1 > maxy:
        continue
    cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
    print("%-42s  x=%4d y=%4d  (box %d,%d-%d,%d)" % (t.encode("ascii", "replace").decode(), cx, cy, x1, y1, x2, y2))
