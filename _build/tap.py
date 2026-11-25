"""UI dump'tan metne göre dokunma yardımcısı: python _build/tap.py "Ekle ›" """
import re
import subprocess
import sys

ADB = "C:/Users/isitm/AppData/Local/Android/Sdk/platform-tools/adb.exe"
DEV = "Z5R87DU8V4LBSOVG"


def sh(*args):
    return subprocess.run([ADB, "-s", DEV] + list(args),
                          capture_output=True, text=True, encoding="utf-8",
                          errors="replace").stdout


def nodes():
    sh("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    sh("pull", "/sdcard/ui.xml", "_build/ui.xml")
    xml = open("_build/ui.xml", encoding="utf-8").read()
    out = []
    for m in re.finditer(r'text="([^"]*)"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', xml):
        t = (m.group(1).replace("&#10;", " ")
             .replace("&amp;", "&").replace("&quot;", '"'))
        x = (int(m.group(2)) + int(m.group(4))) // 2
        y = (int(m.group(3)) + int(m.group(5))) // 2
        out.append((t, x, y))
    return out


def main():
    needle = sys.argv[1]
    do_tap = "--no-tap" not in sys.argv
    for text, x, y in nodes():
        if needle.lower() in text.lower():
            print("%s -> (%d,%d)" % (text[:48], x, y))
            if do_tap:
                sh("shell", "input", "tap", str(x), str(y))
            return 0
    print("BULUNAMADI: " + needle)
    return 1


if __name__ == "__main__":
    sys.exit(main())
