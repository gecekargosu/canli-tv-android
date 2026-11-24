/**
 * UMAY Multi TV - site + uyelik API'si
 *
 *  GET  /                      gizlilik politikasi sayfalari (statik)
 *  GET  /giris                 "Google ile giris" sayfasi
 *  GET  /api/health            { ok, mail, google }
 *  POST /api/register          { email, name, hash }  -> kod maili
 *  POST /api/verify            { email, code }        -> dogrulandi + hos geldin maili
 *  POST /api/resend            { email }              -> yeni kod
 *  POST /api/login             { email, hash }
 *  GET  /api/google/start      Google OAuth (PKCE) baslatir
 *  GET  /api/google/callback   kodu degisir, 6 haneli giris kodu gosterir
 *
 * Gizli degerler (Sites -> Secrets): RESEND_API_KEY, MAIL_FROM,
 * GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET. Yoksa ilgili ozellik kapali kalir.
 */

const CODE_TTL_MIN = 15;
const RESEND_SEC = 45;
const MAX_TRIES = 6;

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;

    // P0-4 (v9.21): uyelik/hesap uclari kaldirildi. Yalnizca statik site + good/*.json.
    if (path.startsWith("/api/")) {
      return json({ ok: false, error: "account system removed" }, 410);
    }
    if (!path.startsWith("/api/")) {
      if (path === "/giris" || path === "/giris/") return googleLoginPage(env);
      if (path === "/indir" || path === "/indir/") return downloadPage();
      return env.ASSETS.fetch(request);   // "/" -> index.html, /gizlilik-politikasi -> html
    }

    if (request.method === "OPTIONS") return new Response(null, { headers: cors() });
    try {
      if (path === "/api/health") return json({ ok: true, mail: mailReady(env), google: googleReady(env) });
      const body = request.method === "POST" ? await readJson(request) : {};
      if (path === "/api/register") return await register(env, body);
      if (path === "/api/verify") return await verify(env, body);
      if (path === "/api/resend") return await resend(env, body);
      if (path === "/api/login") return await login(env, body);
      if (path === "/api/google/start") return await googleStart(env, url);
      if (path === "/api/google/callback") return await googleCallback(env, url);
      return json({ ok: false, error: "bilinmeyen istek" }, 404);
    } catch (e) {
      return json({ ok: false, error: "sunucu hatasi" }, 500);
    }
  },
};

// ---------------------------------------------------------------- indirme sayfasi
const APK_URL =
  "https://github.com/gecekargosu/canli-tv-android/releases/download/v9.64/CanliTV-9.64.apk";

function downloadPage() {
  return html(`<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<body style="font-family:system-ui;background:#11151c;color:#f5f7fa;padding:26px;max-width:520px;margin:0 auto">
<h1 style="color:#ff7a18;margin:0 0 4px">UMAY Multi TV</h1>
<p style="color:#9aa4b2;margin:0 0 20px">3.400+ canli TV kanali (55 ulke) + radyo</p>
<a href="${APK_URL}" style="display:block;text-align:center;background:#ff7a18;color:#111;padding:18px;border-radius:16px;text-decoration:none;font-weight:bold;font-size:18px">⬇ APK indir (Android 8+)</a>
<h2 style="font-size:16px;margin:26px 0 8px">Nasil kurulur?</h2>
<ol style="color:#c7d0dc;line-height:1.6;padding-left:20px;font-size:14px">
<li>Yukaridaki dugmeye bas, dosya insin (yaklasik 5 MB).</li>
<li>Inen dosyaya dokun. \"Bilinmeyen kaynaklara izin ver\" derse, ayarlardan bu tarayici icin izin ver.</li>
<li>Kur → Ac. Baska izin gerekmez.</li>
</ol>
<p style="color:#9aa4b2;font-size:12px;line-height:1.6">Play Store surumu gelistirici dogrulamasi
onayindan sonra yayinlanacak; o zamana kadar buradan indirilebilir. Guncelleme geldiginde ayni
adresten yeni dosyayi indirip uzerine kurabilirsin (ayarlarin korunur).</p>
<p><a href="/" style="color:#ff7a18">Siteye don</a></p></body>`);
}

// ---------------------------------------------------------------- yardimcilar
function cors() {
  return {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "content-type",
    "Access-Control-Allow-Methods": "GET,POST,OPTIONS",
  };
}

function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "content-type": "application/json; charset=utf-8", "cache-control": "no-store", ...cors() },
  });
}

function html(body, status = 200) {
  return new Response(body, {
    status,
    headers: { "content-type": "text/html; charset=utf-8", "cache-control": "no-store" },
  });
}

async function readJson(request) {
  const text = await request.text();
  if (!text) return {};
  try {
    return JSON.parse(text);
  } catch (e) {
    return {};
  }
}

function mailReady(env) {
  return !!(env.RESEND_API_KEY && env.MAIL_FROM);
}

function googleReady(env) {
  return !!(env.GOOGLE_CLIENT_ID && env.GOOGLE_CLIENT_SECRET);
}

function okEmail(s) {
  return typeof s === "string" && s.length <= 120 && /^[^@\s]+@[^@\s]+\.[a-zA-Z]{2,}$/.test(s);
}

function six() {
  const n = crypto.getRandomValues(new Uint32Array(1))[0] % 1000000;
  return String(n).padStart(6, "0");
}

async function sha256(s) {
  const buf = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(s));
  return [...new Uint8Array(buf)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

function b64url(bytes) {
  let s = btoa(String.fromCharCode(...new Uint8Array(bytes)));
  return s.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

async function db(env) {
  // tablolar ilk istekte kurulur (D1 uzerinde CREATE IF NOT EXISTS guvenli)
  await env.DB.batch([
    env.DB.prepare(
      "CREATE TABLE IF NOT EXISTS users (email TEXT PRIMARY KEY, name TEXT, hash TEXT, verified INTEGER DEFAULT 0, code TEXT, code_exp INTEGER DEFAULT 0, tries INTEGER DEFAULT 0, last_send INTEGER DEFAULT 0, created INTEGER, provider TEXT)"
    ),
    env.DB.prepare(
      "CREATE TABLE IF NOT EXISTS oauth (state TEXT PRIMARY KEY, verifier TEXT, exp INTEGER)"
    ),
  ]);
  return env.DB;
}

async function sendMail(env, to, subject, text, link) {
  if (!mailReady(env)) return { ok: false, reason: "mail kapali" };
  const res = await fetch("https://api.resend.com/emails", {
    method: "POST",
    headers: {
      authorization: "Bearer " + env.RESEND_API_KEY,
      "content-type": "application/json",
    },
    body: JSON.stringify({
      from: env.MAIL_FROM,
      to: [to],
      subject,
      html:
        `<div style="font-family:system-ui,Segoe UI,Arial,sans-serif;background:#11151c;padding:24px;color:#f5f7fa">` +
        `<h2 style="color:#ff7a18;margin:0 0 12px">UMAY Multi TV</h2>` +
        `<p style="font-size:15px;line-height:1.5">${text}</p>` +
        (link ? `<p><a href="${link}" style="color:#ff7a18">${link}</a></p>` : "") +
        `<p style="color:#9aa4b2;font-size:12px;margin-top:18px">Bu posta UMAY Multi TV uygulamasindaki uyelik islemi icin gonderildi.</p></div>`,
    }),
  });
  return { ok: res.ok, reason: res.ok ? "" : "posta gonderilemedi" };
}

// ---------------------------------------------------------------- uyelik
async function register(env, b) {
  const email = String(b.email || "").trim().toLowerCase();
  const name = String(b.name || "").trim().slice(0, 60);
  const hash = String(b.hash || "").slice(0, 200);
  if (!okEmail(email)) return json({ ok: false, error: "gecersiz e-posta" }, 400);
  if (hash.length < 32) return json({ ok: false, error: "gecersiz sifre ozeti" }, 400);

  const d = await db(env);
  const row = await d.prepare("SELECT email, hash, verified FROM users WHERE email = ?1").bind(email).first();
  if (row && row.hash && row.hash !== hash) {
    return json({ ok: false, error: "bu e-posta zaten kayitli, giris yap" }, 409);
  }

  const code = six();
  const now = Date.now();
  const exp = now + CODE_TTL_MIN * 60000;
  if (row) {
    await d.prepare("UPDATE users SET hash = ?2, name = ?3, code = ?4, code_exp = ?5, tries = 0, last_send = ?6 WHERE email = ?1")
      .bind(email, hash, name, code, exp, now).run();
  } else {
    await d.prepare("INSERT INTO users (email, name, hash, verified, code, code_exp, tries, last_send, created, provider) VALUES (?1,?2,?3,0,?4,?5,0,?6,?7,'mail')")
      .bind(email, name, hash, code, exp, now, now).run();
  }

  const mail = await sendMail(
    env,
    email,
    "UMAY Multi TV · dogrulama kodun: " + code,
    `Merhaba${name ? " " + name : ""}! Kaydin alindi. Dogrulama kodun: <b style="font-size:22px;letter-spacing:3px">${code}</b><br>Kod ${CODE_TTL_MIN} dakika gecerli.`,
    "https://umay-multi-tv-gq3zer.freebuff.site"
  );

  return json({
    ok: true,
    code_sent: mail.ok,
    msg: mail.ok
      ? "Dogrulama kodu e-postana gonderildi."
      : "Hesap olusturuldu ancak posta gonderimi kapali (yonetici ayari).",
  });
}

async function verify(env, b) {
  const email = String(b.email || "").trim().toLowerCase();
  const code = String(b.code || "").trim();
  if (!okEmail(email)) return json({ ok: false, error: "gecersiz e-posta" }, 400);

  const d = await db(env);
  const row = await d.prepare("SELECT email, name, code, code_exp, tries, verified FROM users WHERE email = ?1").bind(email).first();
  if (!row) return json({ ok: false, error: "bu e-posta kayitli degil" }, 404);
  if (row.verified) return json({ ok: true, msg: "E-posta zaten dogrulanmis." });
  if (!row.code) return json({ ok: false, error: "kod istenmemis, tekrar gonder" }, 400);
  if (Date.now() > row.code_exp) return json({ ok: false, error: "kodun suresi gecti, tekrar gonder" }, 400);
  if ((row.tries || 0) >= MAX_TRIES) return json({ ok: false, error: "cok fazla deneme, yeni kod iste" }, 429);
  if (row.code !== code) {
    await d.prepare("UPDATE users SET tries = tries + 1 WHERE email = ?1").bind(email).run();
    return json({ ok: false, error: "kod hatali" }, 400);
  }

  await d.prepare("UPDATE users SET verified = 1, code = NULL, tries = 0 WHERE email = ?1").bind(email).run();
  await sendMail(
    env,
    email,
    "UMAY Multi TV · aramiza hos geldin",
    `Merhaba${row.name ? " " + row.name : ""}! E-postan dogrulandi, uyeligın tamam. Favorilerin ve ayarlarin hesabina bagli. Iyi seyirler!`
  );
  return json({ ok: true, msg: "E-posta dogrulandi, tesekkurler!" });
}

async function resend(env, b) {
  const email = String(b.email || "").trim().toLowerCase();
  if (!okEmail(email)) return json({ ok: false, error: "gecersiz e-posta" }, 400);

  const d = await db(env);
  const row = await d.prepare("SELECT email, name, verified, last_send FROM users WHERE email = ?1").bind(email).first();
  if (!row) return json({ ok: false, error: "bu e-posta kayitli degil" }, 404);
  if (row.verified) return json({ ok: true, msg: "E-posta zaten dogrulanmis." });
  if (row.last_send && Date.now() - row.last_send < RESEND_SEC * 1000) {
    return json({ ok: false, error: "biraz bekle, az once gonderildi" }, 429);
  }

  const code = six();
  const now = Date.now();
  await d.prepare("UPDATE users SET code = ?2, code_exp = ?3, tries = 0, last_send = ?4 WHERE email = ?1")
    .bind(email, code, now + CODE_TTL_MIN * 60000, now).run();
  const mail = await sendMail(
    env,
    email,
    "UMAY Multi TV · yeni dogrulama kodun: " + code,
    `Yeni dogrulama kodun: <b style="font-size:22px;letter-spacing:3px">${code}</b><br>Kod ${CODE_TTL_MIN} dakika gecerli.`
  );
  return json({ ok: true, code_sent: mail.ok, msg: mail.ok ? "Yeni kod gonderildi." : "Posta gonderimi kapali." });
}

async function login(env, b) {
  const email = String(b.email || "").trim().toLowerCase();
  const hash = String(b.hash || "").slice(0, 200);
  if (!okEmail(email) || hash.length < 32) return json({ ok: false, error: "eksik bilgi" }, 400);

  const d = await db(env);
  const row = await d.prepare("SELECT email, name, hash, verified FROM users WHERE email = ?1").bind(email).first();
  if (!row) return json({ ok: false, error: "bu e-posta kayitli degil" }, 404);
  if (row.hash !== hash) return json({ ok: false, error: "e-posta veya sifre hatali" }, 401);
  return json({ ok: true, email: row.email, name: row.name || "", verified: !!row.verified });
}

// ---------------------------------------------------------------- google
function googleLoginPage(env) {
  if (googleReady(env)) {
    return html(`<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<body style="font-family:system-ui;background:#11151c;color:#f5f7fa;padding:28px;text-align:center">
<h2 style="color:#ff7a18">UMAY Multi TV</h2>
<p>Google hesabınla devam et.</p>
<p><a href="/api/google/start" style="display:inline-block;background:#ff7a18;color:#111;padding:14px 22px;border-radius:14px;text-decoration:none;font-weight:bold">Google ile giriş yap</a></p>
<p style="color:#9aa4b2;font-size:12px">Giriş sonrası ekranda 6 haneli bir kod çıkar; uygulamada "Google'dan aldığım kodla gireceğim" satırına yaz.</p></body>`);
  }
  return html(`<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<body style="font-family:system-ui;background:#11151c;color:#f5f7fa;padding:28px">
<h2 style="color:#ff7a18">Google girişi henüz açılmadı</h2>
<p style="color:#9aa4b2;line-height:1.6">Yönetici tarafında Google OAuth bilgileri eklenince bu sayfa çalışır.
Bu arada uygulamada <b>e-posta ile üye ol</b> ya da <b>misafir olarak devam et</b> seçeneklerini kullanabilirsin.</p>
<p><a href="/" style="color:#ff7a18">← Siteye dön</a></p></body>`, 200);
}

async function googleStart(env, url) {
  if (!googleReady(env)) return Response.redirect(url.origin + "/giris", 302);

  const state = b64url(crypto.getRandomValues(new Uint8Array(16)));
  const verifier = b64url(crypto.getRandomValues(new Uint8Array(32)));
  const challenge = b64url(await crypto.subtle.digest("SHA-256", new TextEncoder().encode(verifier)));

  const d = await db(env);
  await d.prepare("INSERT OR REPLACE INTO oauth (state, verifier, exp) VALUES (?1,?2,?3)")
    .bind(state, verifier, Date.now() + 10 * 60000).run();

  const auth = new URL("https://accounts.google.com/o/oauth2/v2/auth");
  auth.searchParams.set("client_id", env.GOOGLE_CLIENT_ID);
  auth.searchParams.set("redirect_uri", url.origin + "/api/google/callback");
  auth.searchParams.set("response_type", "code");
  auth.searchParams.set("scope", "openid email profile");
  auth.searchParams.set("state", state);
  auth.searchParams.set("code_challenge", challenge);
  auth.searchParams.set("code_challenge_method", "S256");
  auth.searchParams.set("prompt", "select_account");
  return Response.redirect(auth.toString(), 302);
}

async function googleCallback(env, url) {
  const code = url.searchParams.get("code");
  const state = url.searchParams.get("state");
  if (!code || !state) return html("<meta charset='utf-8'><p style='font-family:system-ui;background:#11151c;color:#f5f7fa;padding:24px'>Eksik parametre.</p>", 400);

  const d = await db(env);
  const st = await d.prepare("SELECT verifier, exp FROM oauth WHERE state = ?1").bind(state).first();
  if (!st || Date.now() > st.exp) return html("<meta charset='utf-8'><p style='font-family:system-ui;background:#11151c;color:#f5f7fa;padding:24px'>Oturum suresi gecti, tekrar dene.</p>", 400);

  const tok = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      client_id: env.GOOGLE_CLIENT_ID,
      client_secret: env.GOOGLE_CLIENT_SECRET,
      code,
      code_verifier: st.verifier,
      grant_type: "authorization_code",
      redirect_uri: url.origin + "/api/google/callback",
    }).toString(),
  });
  if (!tok.ok) return html("<meta charset='utf-8'><p style='font-family:system-ui;background:#11151c;color:#f5f7fa;padding:24px'>Google dogrulamasi tamamlanamadi.</p>", 400);

  const t = await tok.json();
  const payload = decodeJwt(t.id_token || "");
  const email = String(payload.email || "").toLowerCase();
  const name = String(payload.name || "");
  if (!okEmail(email)) return html("<meta charset='utf-8'><p style='font-family:system-ui;background:#11151c;color:#f5f7fa;padding:24px'>Google hesabindan e-posta alinamadi.</p>", 400);

  const row = await d.prepare("SELECT email, name FROM users WHERE email = ?1").bind(email).first();
  if (!row) {
    await d.prepare("INSERT INTO users (email, name, hash, verified, created, provider) VALUES (?1,?2,NULL,1,?3,'google')")
      .bind(email, name, Date.now()).run();
  } else {
    await d.prepare("UPDATE users SET verified = 1, name = COALESCE(NULLIF(name,''), ?2), provider = 'google' WHERE email = ?1")
      .bind(email, name).run();
  }

  const loginCode = six();
  await d.prepare("UPDATE users SET code = ?2, code_exp = ?3, tries = 0 WHERE email = ?1")
    .bind(email, loginCode, Date.now() + CODE_TTL_MIN * 60000).run();

  return html(`<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<body style="font-family:system-ui;background:#11151c;color:#f5f7fa;padding:28px;text-align:center">
<h2 style="color:#ff7a18">Giriş tamam 🎉</h2>
<p style="color:#9aa4b2">${escapeHtml(email)}</p>
<p style="font-size:13px">Uygulamada <b>Ayarlar → Hesap → "Google'dan aldığım kodla gireceğim"</b> satırına şu kodu yaz:</p>
<div style="font-size:34px;letter-spacing:10px;font-weight:bold;background:#1b2230;padding:16px;border-radius:16px;display:inline-block">${loginCode}</div>
<p style="color:#9aa4b2;font-size:12px;margin-top:16px">Kod ${CODE_TTL_MIN} dakika geçerli. Bu sayfayı kapatabilirsin.</p></body>`);
}

function decodeJwt(token) {
  try {
    const p = token.split(".")[1];
    const s = atob(p.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(s);
  } catch (e) {
    return {};
  }
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, (ch) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[ch]));
}
