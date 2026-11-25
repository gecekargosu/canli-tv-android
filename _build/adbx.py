# -*- coding: utf-8 -*-
"""adb yardimcisi: metne gore dokunma + ekran goruntusu (uiautomator dump taze)."""
import subprocess, time, re, os, sys, xml.etree.ElementTree as ET

ADB = r"C:/Users/isitm/AppData/Local/Android/Sdk/platform-tools/adb.exe"
DEV = "Z5R87DU8V4LBSOVG"

def sh(*args, timeout=60):
    p = subprocess.run([ADB, "-s", DEV] + list(args), capture_output=True, text=True,
                       encoding="utf-8", errors="replace", timeout=timeout)
    return (p.stdout or "") + (p.stderr or "")

def dump():
    for _ in range(4):
        sh("shell", "uiautomator", "dump", "/sdcard/ui.xml")
        out = sh("shell", "cat", "/sdcard/ui.xml")
        if "<hierarchy" in out:
            try:
                return ET.fromstring(out[out.index("<hierarchy"):])
            except Exception:
                pass
        time.sleep(0.7)
    return None

def nodes(root, text=None, contains=None):
    res = []
    for n in root.iter("node"):
        t = n.get("text", "") or ""
        d = n.get("content-desc", "") or ""
        if text is not None and t.strip() != text:
            continue
        if contains is not None and contains.lower() not in (t + " " + d).lower():
            continue
        res.append((n, t))
    return res

def center(n):
    b = re.findall(r"\d+", n.get("bounds", ""))
    if len(b) != 4: return None
    x1, y1, x2, y2 = map(int, b)
    return ((x1 + x2) // 2, (y1 + y2) // 2)

def tap_text(text=None, contains=None, idx=0, wait=1.0):
    r = dump()
    if r is None: return False
    hits = nodes(r, text=text, contains=contains)
    if len(hits) <= idx: return False
    c = center(hits[idx][0])
    if not c: return False
    sh("shell", "input", "tap", str(c[0]), str(c[1]))
    time.sleep(wait)
    return True

def tap(x, y, wait=1.0):
    sh("shell", "input", "tap", str(x), str(y))
    time.sleep(wait)

def back(wait=1.0):
    sh("shell", "input", "keyevent", "4"); time.sleep(wait)

def shot(name):
    p = "/sdcard/%s.png" % name
    sh("shell", "screencap", "-p", p)
    dst = os.path.join("_build", name + ".png")
    sh("pull", p, dst)
    try:
        from PIL import Image
        im = Image.open(dst); im.thumbnail((460, 1000))
        im.convert("RGB").save(os.path.join("_build", name + "s.jpg"), quality=70)
    except Exception as e:
        print("kucultme hatasi:", e)
    return dst

def texts(root):
    return [n.get("text", "") for n in root.iter("node") if (n.get("text") or "").strip()]

if __name__ == "__main__":
    r = dump()
    print("dump:", "ok" if r is not None else "YOK")
    if r is not None:
        for t in texts(r)[:40]:
            print(" -", t)
