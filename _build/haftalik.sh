#!/usr/bin/env bash
# UMAY Multi TV — haftalık yayın sağlık taraması (iş listesi #37 / #65)
#
# Ne yapar:
#   1) TR: gömülü assets/channels.m3u ilk 100 kanalı dener  -> assets/good_tr.json
#   2) 9 ülke: iptv-org listesini indirir, ilk 90 kanalı dener -> site/public/good/<cc>.json
#   3) TR dosyasını site/public/good/tr.json'a da kopyalar
#   4) Özet yazar; deploy ve zip'i SEN yaparsın (aşağıdaki komutlar)
#
# Kullanım:  bash _build/haftalik.sh
set -u
cd "$(dirname "$0")/.."

echo "== $(date '+%d.%m.%Y %H:%M') sağlık taraması başladı =="
python _build/good.py tr 100
python _build/good.py us de gb in br mx ru se sa fr es it nl 2>/dev/null || true
mkdir -p site/public/good
[ -f assets/good_tr.json ] && cp assets/good_tr.json site/public/good/tr.json

echo
echo "== özet =="
for f in site/public/good/*.json; do
  [ -f "$f" ] || continue
  python - "$f" <<'PY'
import json, sys, os
p = sys.argv[1]
d = json.load(open(p, encoding="utf-8"))
print("%-6s %3d/%-3d çalışıyor  (%s)  %5.1f KB" % (
    d.get("cc", "?").upper(), len(d.get("items", [])), d.get("checked", 0),
    d.get("updated", "")[:10], os.path.getsize(p) / 1024))
PY
done

echo
echo "Sıradaki adımlar (elle):"
echo "  1) uygulamaya gömülen TR listesi değiştiyse:  bash build.sh && bash make-aab.sh"
echo "  2) siteyi yayınla:                            (sites_deploy — manifest site/manifest.json)"
echo "  3) yeni sürüm + Claude zip'i üret"
