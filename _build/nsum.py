import subprocess,sys
out=subprocess.run(["C:/Users/isitm/AppData/Local/Android/Sdk/platform-tools/adb.exe","shell","dumpsys netstats detail"],capture_output=True,text=True,errors="ignore").stdout
tot=0; cur=False
for ln in out.splitlines():
    if "uid=10519" in ln: cur=True; continue
    if "uid=" in ln and "uid=10519" not in ln: cur=False; continue
    if cur and ln.strip().startswith("st="):
        for t in ln.split():
            if t.startswith("rb="): tot+=int(t[3:])
print(tot)
