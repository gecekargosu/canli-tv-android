# -*- coding: utf-8 -*-
"""i18n kapsam denetimi.

1) assets/i18n.tsv: sütun sayısı, tekrar, boş çeviri.
2) src/**/*.java: ekranda görünen Türkçe metin sabitleri I18n.t(...) içinde mi?
   (toast/setText/setTitle/setHint/setContentDescription argümanları taranır)

Kullanım:  PYTHONIOENCODING=utf-8 python tools/i18n-kapsam.py
Çıkış kodu: bulgu varsa 1.
"""
import io
import os
import re
import sys

TSV = 'assets/i18n.tsv'
SRC = 'src/com/cengiz/canlitv'
TR_CHARS = u'çğıöşüÇĞİÖŞÜ'
UI_CALL = re.compile(r'(Toast\.makeText|\.setText|\.setTitle|\.setMessage|\.setHint|'
                     r'setContentDescription|setPositiveButton|setNegativeButton)\s*\(')


def load_table():
    rows = {}
    order = []
    bad = []
    dup = []
    for i, line in enumerate(io.open(TSV, encoding='utf-8').read().split('\n'), 1):
        if not line.strip() or line.startswith('#'):
            continue
        p = line.split(u'\u00a7')
        if len(p) != 8:
            bad.append((i, len(p)))
            continue
        key = p[0]
        if key in rows:
            dup.append(key)
        rows[key] = p
        order.append(key)
    return rows, bad, dup


def java_literals(text):
    for m in re.finditer(r'"((?:[^"\\\n]|\\.){2,160})"', text):
        yield m.start(), m.group(1)


def main():
    rows, bad, dup = load_table()
    print(u'== TABLO ==  anahtar=%d  bozuk_satir=%d  tekrar=%d'
          % (len(rows), len(bad), len(dup)))
    for i, n in bad[:10]:
        print(u'   bozuk satir %d (%d sutun)' % (i, n))
    for k in dup[:10]:
        print(u'   tekrar anahtar: %s' % k)

    empty = []
    for k, p in rows.items():
        if p[0].strip() == '':
            empty.append((k, 'tr'))
        for idx, name in enumerate(['tr', 'en', 'de', 'fr', 'es', 'ru', 'ar', 'pt']):
            if not p[idx].strip():
                empty.append((k, name))
    print(u'== BOS CEVIRI ==  %d' % len(empty))
    for k, lang in empty[:15]:
        print(u'   %-40s %s' % (k[:40], lang))

    missing = {}
    for f in sorted(os.listdir(SRC)):
        if not f.endswith('.java'):
            continue
        path = os.path.join(SRC, f)
        s = io.open(path, encoding='utf-8').read()
        wrapped = set()
        # I18n.t(...) / I18n.s(...) içindeki sabitler çevrilmiş sayılır
        for m in re.finditer(r'I18n\.(?:t|s)\([^;]{0,400}?\)', s, re.S):
            for _, lit in java_literals(m.group(0)):
                wrapped.add(lit)
        for pos, lit in java_literals(s):
            if lit not in wrapped:
                missing.setdefault(f, set()).add(lit)

    # ekranda görünen çağrıların satırlarını ayrıca işaretle
    hard = {}
    for f in sorted(os.listdir(SRC)):
        if not f.endswith('.java'):
            continue
        path = os.path.join(SRC, f)
        for ln, line in enumerate(io.open(path, encoding='utf-8').read().split('\n'), 1):
            if not UI_CALL.search(line):
                continue
            if 'I18n.' in line or 'Lang.' in line or 'Countries.' in line:
                continue
            for _, lit in java_literals(line):
                if any(ch in lit for ch in TR_CHARS) or (' ' in lit and len(lit) > 6):
                    hard.setdefault(f, set()).add((ln, lit))

    print(u'== SARMALANMAMIŞ GÖRÜNÜR METİN (toast/setText...) ==')
    total = 0
    for f, items in sorted(hard.items()):
        if not items:
            continue
        print(u' -- %s (%d)' % (f, len(items)))
        for ln, lit in sorted(items)[:12]:
            print(u'    %4d  %s' % (ln, lit[:100]))
        total += len(items)
    print(u'   TOPLAM: %d' % total)

    kapsam = 0
    for f, lits in missing.items():
        for lit in lits:
            if lit in rows:
                kapsam += 1
    print(u'== TABLODA OLAN AMA KODDA SARILMAMIŞ ANAHTAR: %d ==' % kapsam)
    return 0 if (not bad and not dup and not empty and total == 0) else 1


if __name__ == '__main__':
    sys.exit(main())
