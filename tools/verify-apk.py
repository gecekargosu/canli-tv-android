#!/usr/bin/env python3
"""APK kapasite (bit/bayt) ve içerik denetimi — her güncellemede zorunlu.

Kullanım:
    python tools/verify-apk.py CanliTV.apk --prev releases/v9.55/CanliTV.apk --expect-version 9.56

Ne yapar:
  * dosya boyutu (bayt) ve SHA-256
  * versionName / versionCode (aapt2)
  * imza SHA-256 (apksigner) — yanlış anahtarla imzalanmış APK'yı yakalar
  * zip içindeki dosya sayısı ve açılmış (uncompressed) toplam bayt
  * classes*.dex sınıf sayısı (dexdump)
  * önceki sürümle karşılaştırma: sınıf/dosya sayısı DÜŞTÜYSE hata verir
    (güncelleme sırasında kod/dosya kaybı = "veri kaybı" şüphesi)
  * sonucu releases/apk-kapasite.json defterine ekler
"""
import json
import os
import re
import subprocess
import sys
import zipfile
import hashlib

try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

SDK = r"C:/Users/isitm/AppData/Local/Android/Sdk/build-tools/36.0.0"
AAPT = os.path.join(SDK, "aapt2.exe")
APKSIGNER = os.path.join(SDK, "apksigner.bat")
DEXDUMP = os.path.join(SDK, "dexdump.exe")
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def run(cmd):
    p = subprocess.run(cmd, capture_output=True, text=True, shell=False,
                       cwd=ROOT)
    return p.returncode, (p.stdout or "") + (p.stderr or "")


def apk_info(path):
    code, out = run([AAPT, "dump", "badging", path])
    name = re.search(r"versionName='([^']*)'", out)
    vcode = re.search(r"versionCode='([^']*)'", out)
    pkg = re.search(r"package: name='([^']*)'", out)
    return {
        "package": pkg.group(1) if pkg else "?",
        "versionName": name.group(1) if name else "?",
        "versionCode": vcode.group(1) if vcode else "?",
    }


def signature(path):
    code, out = run([APKSIGNER, "verify", "--print-certs", path])
    m = re.search(r"SHA-256 digest: ([0-9a-f]+)", out)
    return m.group(1) if m else "?"


def dex_classes(path):
    """DEX başlığındaki class_defs_size alanını okur (dexdump'a gerek yok)."""
    total = 0
    with zipfile.ZipFile(path) as z:
        dexes = [n for n in z.namelist() if re.match(r"classes\d*\.dex$", n)]
        for d in dexes:
            data = z.read(d)
            if len(data) > 100:
                total += int.from_bytes(data[96:100], "little")
    return total, len(dexes)


def zip_stats(path):
    with zipfile.ZipFile(path) as z:
        infos = z.infolist()
        return len(infos), sum(i.file_size for i in infos)


def digest(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def main():
    args = sys.argv[1:]
    if not args:
        print(__doc__)
        return 2
    apk = args[0]
    prev = None
    expect = None
    if "--prev" in args:
        prev = args[args.index("--prev") + 1]
    if "--expect-version" in args:
        expect = args[args.index("--expect-version") + 1]

    info = apk_info(apk)
    entries, unpacked = zip_stats(apk)
    classes, dexes = dex_classes(apk)
    rec = {
        "versionName": info["versionName"],
        "versionCode": info["versionCode"],
        "apkBytes": os.path.getsize(apk),
        "sha256": digest(apk),
        "signSha256": signature(apk),
        "zipEntries": entries,
        "unpackedBytes": unpacked,
        "dexFiles": dexes,
        "classes": classes,
    }

    print("APK        :", os.path.basename(apk))
    print("paket      :", info["package"], "| sürüm", info["versionName"],
          "(kod", info["versionCode"] + ")")
    print("boyut      :", rec["apkBytes"], "bayt")
    print("içerik     :", entries, "dosya |", unpacked, "bayt (açılmış)")
    print("dex        :", dexes, "dosya |", classes, "sınıf")
    print("imza       :", rec["signSha256"])

    ok = True
    if expect and info["versionName"] != expect:
        print("HATA: beklenen sürüm", expect, "ama APK", info["versionName"])
        ok = False
    if rec["signSha256"] == "?" or not rec["signSha256"].startswith("f3996c67"):
        print("HATA: beklenen imza (f3996c67…) değil")
        ok = False
    if classes < 100:
        print("HATA: sınıf sayısı çok düşük (", classes, ") — derleme eksik olabilir")
        ok = False

    if prev and os.path.exists(prev):
        p = apk_info(prev)
        pe, pu = zip_stats(prev)
        pc, _ = dex_classes(prev)
        print("\nönceki     :", p["versionName"], "|", os.path.getsize(prev),
              "bayt |", pe, "dosya |", pc, "sınıf")
        d_cls = classes - pc
        d_ent = entries - pe
        d_byt = rec["apkBytes"] - os.path.getsize(prev)
        print("fark       : sınıf %+d | dosya %+d | boyut %+d bayt" %
              (d_cls, d_ent, d_byt))
        if d_cls < 0:
            print("HATA: sınıf sayısı düştü — güncellemede kod kaybı var")
            ok = False
        if d_ent < -3:
            print("HATA: dosya sayısı belirgin düştü (", d_ent,
                  ") — kaynak/varlık kaybı olabilir")
            ok = False

    hist = os.path.join(ROOT, "releases", "apk-kapasite.json")
    data = []
    if os.path.exists(hist):
        try:
            data = json.load(open(hist, encoding="utf-8"))
        except Exception:
            data = []
    data = [d for d in data if d.get("versionName") != rec["versionName"]]
    data.append(rec)
    data.sort(key=lambda d: int(d.get("versionCode") or 0))
    json.dump(data, open(hist, "w", encoding="utf-8"), ensure_ascii=False, indent=1)

    print("\n" + ("✅ Kapasite denetimi TEMİZ" if ok else "❌ Kapasite denetimi BAŞARISIZ"))
    print("defter     : releases/apk-kapasite.json")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
