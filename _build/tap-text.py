import re, subprocess, sys, xml.etree.ElementTree as ET

ADB = r"C:/Users/isitm/AppData/Local/Android/Sdk/platform-tools/adb.exe"
target = sys.argv[1]

subprocess.run([ADB, "shell", "uiautomator", "dump", "/sdcard/ui.xml"],
               capture_output=True, env={**__import__("os").environ, "MSYS_NO_PATHCONV": "1"})
subprocess.run([ADB, "pull", "/sdcard/ui.xml", r"_build/ui.xml"],
               capture_output=True, env={**__import__("os").environ, "MSYS_NO_PATHCONV": "1"})

tree = ET.parse("_build/ui.xml")
best = None
for node in tree.iter("node"):
    text = (node.get("text") or "").strip()
    desc = (node.get("content-desc") or "").strip()
    if target.lower() in text.lower() or target.lower() in desc.lower():
        b = re.findall(r"\d+", node.get("bounds") or "")
        if len(b) == 4:
            cx = (int(b[0]) + int(b[2])) // 2
            cy = (int(b[1]) + int(b[3])) // 2
            best = (cx, cy, text)
            break

if not best:
    print("BULUNAMADI:", target)
    sys.exit(1)
print("TIKLA:", ascii(best))
subprocess.run([ADB, "shell", "input", "tap", str(best[0]), str(best[1])])
