#!/usr/bin/env python3
"""Play Console kapali test kanalina AAB yukler.

Kullanim:
  python tools/play-upload.py --check
  python tools/play-upload.py --upload CanliTV.aab
"""
import argparse
import os
import sys

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

PKG = "com.cengiz.canlitv"
SA = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                  "site", ".secrets", "play-service-account.json")
SCOPE = "https://www.googleapis.com/auth/androidpublisher"


def service():
    creds = service_account.Credentials.from_service_account_file(SA, scopes=[SCOPE])
    return build("androidpublisher", "v3", credentials=creds, cache_discovery=False)


def show_tracks(svc, edit_id):
    res = svc.edits().tracks().list(packageName=PKG, editId=edit_id).execute()
    for t in res.get("tracks", []):
        rels = []
        for r in t.get("releases", []):
            rels.append("%s:%s:%s" % (r.get("status"), ",".join(r.get("versionCodes", [])),
                                      r.get("name", "")))
        print("%-10s %s" % (t.get("track"), " | ".join(rels) or "(bos)"))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    ap.add_argument("--upload")
    ap.add_argument("--track", default=None)
    ap.add_argument("--status", default="completed")
    ap.add_argument("--name", default="UMAY Multi TV 10.05")
    a = ap.parse_args()
    if not os.path.exists(SA):
        sys.exit("Servis hesabi yok: %s" % SA)

    svc = service()
    edit = svc.edits().insert(packageName=PKG, body={}).execute()
    eid = edit["id"]
    try:
        if a.check or not a.upload:
            print("Mevcut kanallar:")
            show_tracks(svc, eid)
            return
        print("Yukleniyor:", a.upload, os.path.getsize(a.upload), "bayt")
        media = MediaFileUpload(a.upload, mimetype="application/octet-stream", resumable=True)
        bundle = svc.edits().bundles().upload(
            packageName=PKG, editId=eid, media_body=media).execute()
        code = str(bundle["versionCode"])
        print("Yuklendi, versionCode =", code)

        res = svc.edits().tracks().list(packageName=PKG, editId=eid).execute()
        tracks = res.get("tracks", [])
        # Varsayilan daima alpha (kapali test). Onceden track listesinin ilk
        # elemani secildigi icin beta'ya yukleniyordu.
        track = a.track or "alpha"
        print("Kanal:", track)
        svc.edits().tracks().update(
            packageName=PKG, editId=eid, track=track,
            body={"track": track,
                  "releases": [{"versionCodes": [code], "status": a.status,
                                "name": a.name}]}).execute()
        commit = svc.edits().commit(packageName=PKG, editId=eid).execute()
        print("Commit:", commit.get("id"))
        print("Yukleme tamam. Play Console'da 'Incelemeye gonder' adimi gerekir.")
    finally:
        try:
            svc.edits().delete(packageName=PKG, editId=eid).execute()
        except Exception:
            pass


if __name__ == "__main__":
    main()
