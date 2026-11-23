import re, subprocess, sys, os, time
import xml.etree.ElementTree as ET

ADB = r"C:/Users/isitm/AppData/Local/Android/Sdk/platform-tools/adb.exe"
ENV = {**os.environ, "MSYS_NO_PATHCONV": "1"}
args = [a for a in sys.argv[1:] if not a.startswith("--")]
long_press = "--long" in sys.argv
target = args[0] if args else ""

subprocess.run([ADB, "shell", "uiautomator", "dump", "/sdcard/ui.xml"], capture_output=True, env=ENV)
subprocess.run([ADB, "pull", "/sdcard/ui.xml", "_build/ui.xml"], capture_output=True, env=ENV)

best = None
for node in ET.parse("_build/ui.xml").iter("node"):
    text = (node.get("text") or "").strip()
    desc = (node.get("content-desc") or "").strip()
    if target.lower() in text.lower() or target.lower() in desc.lower():
        b = re.findall(r"\d+", node.get("bounds") or "")
        if len(b) == 4:
            best = ((int(b[0]) + int(b[2])) // 2, (int(b[1]) + int(b[3])) // 2, text)
            break

if not best:
    print("BULUNAMADI:", target)
    sys.exit(1)

print("TIKLA:", ascii(best))
if long_press:
    subprocess.run([ADB, "shell", "input", "swipe", str(best[0]), str(best[1]),
                    str(best[0]), str(best[1]), "900"])
else:
    subprocess.run([ADB, "shell", "input", "tap", str(best[0]), str(best[1])])
