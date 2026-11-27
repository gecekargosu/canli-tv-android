# -*- coding: utf-8 -*-
"""site/public/good/*.json içindeki kanal adlarından son ekleri temizler:
   "Show TV (Turkiye)" -> "Show TV", "TRT 1 (1080p)" -> "TRT 1"."""
import glob, io, json, re, unicodedata


def fold(s):
    s = unicodedata.normalize('NFKD', s.lower())
    return ''.join(ch for ch in s if not unicodedata.combining(ch)).replace('ı', 'i')


src = io.open('src/com/cengiz/canlitv/Countries.java', encoding='utf-8').read()
names = set()
for m in re.finditer(r'\{"[a-z]{2}",\s*"([^"]+)",\s*"([^"]+)"\}', src):
    names.add(fold(m.group(1)))
    names.add(fold(m.group(2)))
# sık geçen ek yazımlar
names.update(['turkiye', 'turkey', 'kabe'])

RES = re.compile(r'\s*\(\d{3,4}[pi]\)\s*$')
SUF = re.compile(r'\s*\(([^()]{2,40})\)\s*$')

total = changed = 0
for f in sorted(glob.glob('site/public/good/*.json')):
    raw = io.open(f, encoding='utf-8').read()
    d = json.loads(raw)
    items = d.get('items', [])
    dirty = False
    for it in items:
        total += 1
        nm = it.get('name', '')
        new = nm
        for _ in range(3):
            if RES.search(new):
                new = RES.sub('', new).strip()
                continue
            m = SUF.search(new)
            if m and fold(m.group(1)) in names:
                cut = new[:m.start()].strip()
                if cut:
                    new = cut
                    continue
            break
        if new != nm:
            it['name'] = new
            dirty = True
            changed += 1
    if dirty:
        io.open(f, 'w', encoding='utf-8', newline='\n').write(
            json.dumps(d, ensure_ascii=False, separators=(',', ':')) + '\n')

print('toplam', total, 'degisen', changed)
