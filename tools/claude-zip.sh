#!/usr/bin/env bash
# M-02: Claude/başka ajanlara verilecek kaynak zip'i.
# Gizli dosyalar (anahtar, keystore, konuşma kayıtları) ASLA pakete girmez.
set -e
cd "$(dirname "$0")/.."
OUT="../UMAY-MULTI-TV-Claude-$(grep -m1 "versionName" app/build.gradle | sed "s/.*'\(.*\)'.*/\1/").zip"
rm -f "$OUT"
python - "$OUT" <<'PY'
import os,sys,zipfile
out=sys.argv[1]
skip={'.git','.gradle','build','build_aab','node_modules','.idea','_build','pazarlama','.secrets'}
bad_ext=('.keystore','.jks','.zip','.key','.p12','.pem')
bad_name={'keystore.properties','konusmalar.txt','mailfrom.txt','local.properties'}
n=0
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as z:
    for dp,dns,fns in os.walk('.'):
        rel=os.path.relpath(dp,'.')
        dns[:]=[d for d in dns if d not in skip]
        if rel.startswith('releases'+os.sep):
            parts=rel.split(os.sep)
            if len(parts)>1 and parts[1]!='v'+os.path.basename(out).split('-v')[-1].replace('.zip',''):
                dns[:]=[]; continue
        for f in fns:
            if f.lower().endswith(bad_ext) or f in bad_name: continue
            p=os.path.join(dp,f)
            z.write(p,'UMAY-MULTI-TV/'+os.path.relpath(p,'.')); n+=1
with zipfile.ZipFile(out) as z:
    leak=[x for x in z.namelist() if '.secrets' in x or 'keystore' in x.lower() or 'resend' in x.lower()]
print(out,n,'dosya · sizinti:',leak)
assert not leak, 'GIZLI DOSYA PAKETTE!'
PY
