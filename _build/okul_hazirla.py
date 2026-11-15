# -*- coding: utf-8 -*-
"""ck/1.DÖNEM ve 2.DÖNEM altindaki sinav/not dosyalarini toplayip GitHub deposu klasoru uretir."""
import os, shutil, re, io

ROOTS = ["C:/Users/isitm/Desktop/ck/1.DÖNEM", "C:/Users/isitm/Desktop/ck/2.DÖNEM"]
DEST = "C:/Users/isitm/Desktop/okul-sinav-notlari"
EXTS = {".pdf", ".docx", ".doc", ".pptx", ".xlsx", ".jpg", ".jpeg", ".png", ".odt", ".txt"}
KW = re.compile(r"s[iı]nav|vize|final|quiz|test|soru|çıkmış|cikmis|çalışma|calisma|not|ödev|odev|özet|ozet", re.I)
TR = {"İ": "I", "ı": "i", "Ş": "S", "ş": "s", "Ğ": "G", "ğ": "g",
      "Ü": "U", "ü": "u", "Ö": "O", "ö": "o", "Ç": "C", "ç": "c"}

if os.path.isdir(DEST):
    shutil.rmtree(DEST)
os.makedirs(DEST)

index, total, skipped = {}, 0, 0
for root in ROOTS:
    if not os.path.isdir(root):
        continue
    donem = os.path.basename(root)
    for dp, dn, fn in os.walk(root):
        for f in fn:
            p = os.path.join(dp, f)
            ext = os.path.splitext(f)[1].lower()
            try:
                size = os.path.getsize(p)
            except Exception:
                continue
            if ext not in EXTS or size > 12 * 1024 * 1024:
                skipped += 1
                continue
            if not KW.search(f) and ext != ".pdf":
                skipped += 1
                continue
            rel = os.path.relpath(p, root).replace(os.sep, "/")
            safe = rel
            for a, b in TR.items():
                safe = safe.replace(a, b)
            out = os.path.join(DEST, donem, safe)
            os.makedirs(os.path.dirname(out), exist_ok=True)
            try:
                shutil.copy2(p, out)
            except Exception:
                skipped += 1
                continue
            total += size
            ders = rel.split("/")[0] if "/" in rel else "(kok)"
            index.setdefault(donem + " / " + ders, []).append((safe, size))

lines = [
    "# Okul — Sınav Soruları ve Ders Notları",
    "",
    "Cengiz Kılıç · MDBF Yapay Zeka ve Makine Öğrenmesi",
    "",
    "Bu depo, bilgisayarımdaki ders klasörlerinden derlenen **sınav/çalışma soruları, ders notları ve ödev dokümanlarını** içerir.",
    "Dosyalar dönem ve ders klasörlerine göre düzenlendi; bazı sorular resim (`.png`, `.jpg`), bazıları PDF/DOCX/ODT.",
    "",
    "| Dönem / Ders | Dosya | Boyut |",
    "|---|---|---|",
]
for k in sorted(index):
    for name, size in sorted(index[k])[:60]:
        lines.append("| %s | `%s` | %.1f MB |" % (k, name, size / 1048576.0))

lines += [
    "",
    "## Notlar",
    "- 12 MB üzerindeki arşivler (zip) ve ses dosyaları (.m4a) depoya alınmadı; gerekirse ayrıca yüklenir.",
    "- Görüntü halindeki sorular metin aramasında çıkmaz; ders klasörüne göre ilerleyin.",
    "",
    "## Lisans",
    "Ders materyalleri ilgili öğretim üyelerine aittir; yalnızca kişisel arşiv amacıyla paylaşılmıştır.",
]
io.open(os.path.join(DEST, "README.md"), "w", encoding="utf-8", newline="\n").write("\n".join(lines) + "\n")
print("kopyalanan: %d dosya | %.1f MB | atlanan: %d" % (sum(len(v) for v in index.values()), total / 1048576.0, skipped))
for k in sorted(index):
    print("  ", k, "->", len(index[k]))
