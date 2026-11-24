import re
import io
import sys

path = sys.argv[1] if len(sys.argv) > 1 else '_build/ui.xml'
s = io.open(path, encoding='utf-8').read()
for m in re.finditer(r'<node[^>]*>', s):
    tag = m.group(0)
    t = re.search(r'text="([^"]*)"', tag)
    d = re.search(r'content-desc="([^"]*)"', tag)
    c = re.search(r'class="([^"]*)"', tag)
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', tag)
    if not b:
        continue
    lab = (t.group(1) if t else '') or (d.group(1) if d else '')
    if not lab.strip():
        continue
    x1, y1, x2, y2 = map(int, b.groups())
    cls = (c.group(1) if c else '').split('.')[-1]
    print('%-34s %-12s (%d,%d)' % (lab[:32], cls, (x1 + x2) // 2, (y1 + y2) // 2))
