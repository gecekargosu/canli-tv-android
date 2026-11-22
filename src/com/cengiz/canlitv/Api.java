package com.cengiz.canlitv;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Uyelik sunucusu istemcisi (saf HttpURLConnection - ek kutuphane yok).
 * Sunucu: Freebuff Sites uzerindeki Worker + D1. Sunucu kapaliysa cagrilar "ok=false" doner,
 * uygulama cevrimdisi moda duser (hesap bu telefonda tutulur).
 */
public class Api {

    public static final String BASE = "https://umay-multi-tv-gq3zer.freebuff.site";

    public static class Res {
        public boolean ok;
        public String msg = "";
        public JSONObject json;
        public boolean offline;

        Res(boolean ok, String msg) {
            this.ok = ok;
            this.msg = msg;
        }
    }

    public static Res health() {
        return get("/api/health");
    }

    public static Res register(String email, String name, String hash) {
        JSONObject b = new JSONObject();
        try {
            b.put("email", email);
            b.put("name", name);
            b.put("hash", hash);
        } catch (Exception ignored) {
        }
        return post("/api/register", b.toString());
    }

    public static Res verify(String email, String code) {
        JSONObject b = new JSONObject();
        try {
            b.put("email", email);
            b.put("code", code);
        } catch (Exception ignored) {
        }
        return post("/api/verify", b.toString());
    }

    public static Res resend(String email) {
        JSONObject b = new JSONObject();
        try {
            b.put("email", email);
        } catch (Exception ignored) {
        }
        return post("/api/resend", b.toString());
    }

    public static Res login(String email, String hash) {
        JSONObject b = new JSONObject();
        try {
            b.put("email", email);
            b.put("hash", hash);
        } catch (Exception ignored) {
        }
        return post("/api/login", b.toString());
    }

    // --- tasima ---
    private static Res get(String path) {
        return call("GET", path, null);
    }

    private static Res post(String path, String body) {
        return call("POST", path, body);
    }

    private static Res call(String method, String path, String body) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(BASE + path).openConnection();
            c.setRequestMethod(method);
            c.setConnectTimeout(8000);
            c.setReadTimeout(8000);
            c.setRequestProperty("Accept", "application/json");
            c.setRequestProperty("User-Agent", "UMAY-Multi-TV-Android");
            if (body != null) {
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                OutputStream os = c.getOutputStream();
                os.write(body.getBytes(StandardCharsets.UTF_8));
                os.close();
            }
            int code = c.getResponseCode();
            String text = read(code >= 400 ? c.getErrorStream() : c.getInputStream());
            JSONObject j = null;
            try {
                j = new JSONObject(text);
            } catch (Exception ignored) {
            }
            Res r = new Res(code >= 200 && code < 300, j != null ? j.optString("msg", "") : "");
            r.json = j;
            if (j != null) {
                r.ok = j.optBoolean("ok", r.ok);
                if (r.msg.length() == 0) r.msg = j.optString("error", "");
            }
            return r;
        } catch (Exception e) {
            Res r = new Res(false, "offline");
            r.offline = true;
            return r;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private static String read(InputStream in) {
        if (in == null) return "";
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder b = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) b.append(line);
            r.close();
            return b.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
