package com.cengiz.canlitv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Hesap ekrani: e-posta ile uye ol / giris yap, e-posta kodu ile dogrula, Google ile devam et.
 * Uyelik istege baglidir - "misafir olarak devam et" her zaman acik.
 */
public class AccountActivity extends Activity {

    private static final int BG = Color.parseColor("#11151C");
    private static final int CARD = Color.parseColor("#1B2230");
    private static final int ACCENT = Color.parseColor("#FF7A18");
    private static final int SOFT = Color.parseColor("#2A3444");
    private static final int TEXT = Color.parseColor("#F5F7FA");
    private static final int MUTED = Color.parseColor("#9AA4B2");
    private static final int OK = Color.parseColor("#3DDC84");
    private static final int WARN = Color.parseColor("#FFC24B");

    private LinearLayout body;
    private TextView status;
    private EditText emailIn, passIn, nameIn, codeIn;
    private int mode = 0;          // 0 = giris, 1 = uye ol
    private boolean codeStep = false;
    private String codeEmail = "";
    private boolean busy = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(12), dp(12), dp(18));

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = pill("‹ " + Lang.t(AccountActivity.this, "back").replace("‹ ", ""));
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        head.addView(back);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(12), 0, 0, 0);
        TextView t = new TextView(this);
        t.setText(Lang.t(AccountActivity.this, "account"));
        t.setTextColor(TEXT);
        t.setTextSize(21);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        TextView sub = new TextView(this);
        sub.setText(Lang.t(AccountActivity.this, "account_sub"));
        sub.setTextColor(MUTED);
        sub.setTextSize(10);
        sub.setSingleLine(true);
        titles.addView(t);
        titles.addView(sub);
        head.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(head);

        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0, dp(12), 0, 0);
        root.addView(body);

        scroll.addView(root);
        AdManager.wrap(this, scroll);

        // durum cubugunun altindan basla (edge-to-edge)
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public android.view.WindowInsets onApplyWindowInsets(View v, android.view.WindowInsets insets) {
                v.setPadding(dp(12), dp(12) + insets.getSystemWindowInsetTop(), dp(12), dp(18));
                return insets;
            }
        });
        render();
    }

    // ---------------- ekran ----------------
    private void render() {
        body.removeAllViews();
        status = null;

        if (Account.signedIn(this)) {
            profileCard();
            if (!Account.verified(this)) verifyCard();
            signOutRow();
            return;
        }

        if (codeStep) {
            codeCard();
            return;
        }

        introCard();
        modeChips();
        formCard();
        primaryButton();
        googleRow();
        guestRow();
    }

    private void profileCard() {
        LinearLayout card = card();
        TextView t = new TextView(this);
        String nm = Account.name(this).length() > 0 ? Account.name(this) : Account.email(this);
        t.setText("👤 " + nm);
        t.setTextColor(TEXT);
        t.setTextSize(15);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        card.addView(t);

        TextView e = new TextView(this);
        e.setText(Account.email(this));
        e.setTextColor(MUTED);
        e.setTextSize(11);
        e.setPadding(0, dp(2), 0, dp(6));
        card.addView(e);

        TextView v = new TextView(this);
        boolean ok = Account.verified(this);
        v.setText(ok ? "✔ " + Lang.t(AccountActivity.this, "verified") : "⏳ " + Lang.t(AccountActivity.this, "unverified"));
        v.setTextColor(ok ? OK : WARN);
        v.setTextSize(11);
        card.addView(v);

        TextView note = new TextView(this);
        note.setText(Lang.t(AccountActivity.this, "verified_note"));
        note.setTextColor(MUTED);
        note.setTextSize(10);
        note.setPadding(0, dp(6), 0, 0);
        card.addView(note);
        body.addView(card, margin(10));
    }

    private void verifyCard() {
        LinearLayout card = card();
        TextView t = new TextView(this);
        t.setText(Lang.t(AccountActivity.this, "code_title"));
        t.setTextColor(TEXT);
        t.setTextSize(13);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        card.addView(t);

        TextView s = new TextView(this);
        s.setText(Lang.t(AccountActivity.this, "code_sub") + " " + Account.email(this));
        s.setTextColor(MUTED);
        s.setTextSize(10);
        s.setPadding(0, dp(4), 0, dp(8));
        card.addView(s);

        codeIn = input(Lang.t(AccountActivity.this, "code"), InputType.TYPE_CLASS_NUMBER);
        codeIn.setTextSize(20);
        card.addView(codeIn);

        status = new TextView(this);
        status.setTextColor(MUTED);
        status.setTextSize(11);
        status.setPadding(0, dp(8), 0, 0);
        card.addView(status);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView ok = pill(Lang.t(AccountActivity.this, "verify"));
        ok.setBackground(rounded(ACCENT, 16));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { doVerify(); }
        });
        row.addView(ok);

        TextView re = pill(Lang.t(AccountActivity.this, "resend"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(6), 0, 0, 0);
        re.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { doResend(); }
        });
        row.addView(re, lp);
        card.addView(row, margin(8));

        body.addView(card, margin(10));
    }

    private void signOutRow() {
        TextView out = pill(Lang.t(AccountActivity.this, "signout"));
        out.setTextColor(WARN);
        out.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new AlertDialog.Builder(AccountActivity.this)
                        .setMessage(Lang.t(AccountActivity.this, "signout_q"))
                        .setNegativeButton(Lang.t(AccountActivity.this, "cancel"), null)
                        .setPositiveButton(Lang.t(AccountActivity.this, "signout"), new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int w) {
                                Account.signOut(AccountActivity.this);
                                codeStep = false;
                                render();
                            }
                        }).show();
            }
        });
        LinearLayout wrap = new LinearLayout(this);
        wrap.setPadding(0, dp(4), 0, 0);
        wrap.addView(out);
        body.addView(wrap);
    }

    private void introCard() {
        LinearLayout card = card();
        TextView t = new TextView(this);
        t.setText(Lang.t(AccountActivity.this, "account_intro"));
        t.setTextColor(TEXT);
        t.setTextSize(12);
        t.setLineSpacing(dp(2), 1f);
        card.addView(t);
        TextView n = new TextView(this);
        n.setText(Lang.t(AccountActivity.this, "account_why"));
        n.setTextColor(MUTED);
        n.setTextSize(10);
        n.setPadding(0, dp(6), 0, 0);
        card.addView(n);
        body.addView(card, margin(10));
    }

    private void modeChips() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView a = pill(Lang.t(AccountActivity.this, "signin"));
        TextView b = pill(Lang.t(AccountActivity.this, "signup"));
        a.setBackground(rounded(mode == 0 ? ACCENT : SOFT, 16));
        b.setBackground(rounded(mode == 1 ? ACCENT : SOFT, 16));
        a.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { mode = 0; render(); }
        });
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { mode = 1; render(); }
        });
        row.addView(a);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(6), 0, 0, 0);
        row.addView(b, lp);
        body.addView(row, margin(6));
    }

    private void formCard() {
        LinearLayout card = card();

        if (mode == 1) {
            nameIn = input(Lang.t(AccountActivity.this, "fullname"), InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            card.addView(nameIn);
        }
        emailIn = input(Lang.t(AccountActivity.this, "email"), InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        card.addView(emailIn);
        passIn = input(Lang.t(AccountActivity.this, "password"), InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        card.addView(passIn);

        TextView hint = new TextView(this);
        hint.setText(mode == 1 ? Lang.t(AccountActivity.this, "pass_hint") : Lang.t(AccountActivity.this, "forgot_hint"));
        hint.setTextColor(MUTED);
        hint.setTextSize(10);
        hint.setPadding(0, dp(6), 0, 0);
        card.addView(hint);

        status = new TextView(this);
        status.setTextColor(MUTED);
        status.setTextSize(11);
        status.setPadding(0, dp(8), 0, 0);
        card.addView(status);

        body.addView(card, margin(8));
    }

    private void primaryButton() {
        TextView go = pill(mode == 1 ? Lang.t(AccountActivity.this, "signup") : Lang.t(AccountActivity.this, "signin"));
        go.setBackground(rounded(ACCENT, 18));
        go.setTextSize(14);
        go.setPadding(dp(18), dp(12), dp(18), dp(12));
        go.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (busy) return;
                if (mode == 1) doRegister(); else doLogin();
            }
        });
        LinearLayout wrap = new LinearLayout(this);
        wrap.setGravity(Gravity.CENTER_HORIZONTAL);
        wrap.setPadding(0, dp(4), 0, dp(4));
        wrap.addView(go);
        body.addView(wrap);
    }

    private void googleRow() {
        TextView g = pill("🔵 " + Lang.t(AccountActivity.this, "google"));
        g.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(Api.BASE + "/giris")));
                } catch (Exception e) {
                    say(Lang.t(AccountActivity.this, "no_browser"));
                }
            }
        });
        LinearLayout wrap = new LinearLayout(this);
        wrap.setGravity(Gravity.CENTER_HORIZONTAL);
        wrap.setPadding(0, dp(2), 0, dp(2));
        wrap.addView(g);
        body.addView(wrap);

        TextView has = new TextView(this);
        has.setText(Lang.t(AccountActivity.this, "have_code"));
        has.setTextColor(MUTED);
        has.setTextSize(11);
        has.setGravity(Gravity.CENTER);
        has.setPadding(0, dp(8), 0, 0);
        has.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                codeStep = true;
                codeEmail = emailIn != null ? emailIn.getText().toString().trim() : "";
                render();
            }
        });
        body.addView(has);
    }

    private void guestRow() {
        TextView g = new TextView(this);
        g.setText(Lang.t(AccountActivity.this, "guest_go"));
        g.setTextColor(MUTED);
        g.setTextSize(12);
        g.setGravity(Gravity.CENTER);
        g.setPadding(0, dp(14), 0, dp(4));
        g.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Account.setGuest(AccountActivity.this);
                finish();
            }
        });
        body.addView(g);
    }

    private void codeCard() {
        LinearLayout card = card();
        TextView t = new TextView(this);
        t.setText(Lang.t(AccountActivity.this, "code_title"));
        t.setTextColor(TEXT);
        t.setTextSize(14);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        card.addView(t);

        TextView s = new TextView(this);
        s.setText(Lang.t(AccountActivity.this, "code_sub") + " " + codeEmail);
        s.setTextColor(MUTED);
        s.setTextSize(10);
        s.setPadding(0, dp(4), 0, dp(8));
        card.addView(s);

        codeIn = input(Lang.t(AccountActivity.this, "code"), InputType.TYPE_CLASS_NUMBER);
        codeIn.setTextSize(20);
        card.addView(codeIn);

        status = new TextView(this);
        status.setTextColor(MUTED);
        status.setTextSize(11);
        status.setPadding(0, dp(8), 0, 0);
        card.addView(status);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView ok = pill(Lang.t(AccountActivity.this, "verify"));
        ok.setBackground(rounded(ACCENT, 16));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { doVerify(); }
        });
        row.addView(ok);

        TextView re = pill(Lang.t(AccountActivity.this, "resend"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(6), 0, 0, 0);
        re.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { doResend(); }
        });
        row.addView(re, lp);
        card.addView(row, margin(8));

        TextView back2 = new TextView(this);
        back2.setText(Lang.t(AccountActivity.this, "back_to_form"));
        back2.setTextColor(MUTED);
        back2.setTextSize(11);
        back2.setPadding(0, dp(10), 0, 0);
        back2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { codeStep = false; render(); }
        });
        card.addView(back2);

        body.addView(card, margin(8));
    }

    // ---------------- islemler ----------------
    private void doRegister() {
        final String email = emailIn.getText().toString().trim().toLowerCase();
        final String pass = passIn.getText().toString();
        final String name = nameIn == null ? "" : nameIn.getText().toString().trim();

        if (!Account.validEmail(email)) { say(Lang.t(AccountActivity.this, "err_email")); return; }
        if (pass.length() < 6) { say(Lang.t(AccountActivity.this, "err_pass")); return; }

        busy = true;
        say(Lang.t(AccountActivity.this, "working"));
        final String salt = Account.newSalt();
        final String h = Account.hash(pass, salt);

        new Thread(new Runnable() {
            @Override public void run() {
                final Api.Res r = Api.register(email, name, h);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        busy = false;
                        if (r.ok || r.offline) {
                            Account.savePassword(AccountActivity.this, pass);
                            Account.save(AccountActivity.this, email, name, false);
                            codeStep = true;
                            codeEmail = email;
                            render();
                            if (r.offline) {
                                say(Lang.t(AccountActivity.this, "offline_saved"));
                            } else {
                                say(r.msg.length() > 0 ? r.msg
                                        : Lang.t(AccountActivity.this, "code_sent"));
                            }
                        } else {
                            say(r.msg.length() > 0 ? r.msg : Lang.t(AccountActivity.this, "err_generic"));
                        }
                    }
                });
            }
        }).start();
    }

    private void doLogin() {
        final String email = emailIn.getText().toString().trim().toLowerCase();
        final String pass = passIn.getText().toString();
        if (!Account.validEmail(email)) { say(Lang.t(AccountActivity.this, "err_email")); return; }
        if (pass.length() < 6) { say(Lang.t(AccountActivity.this, "err_pass")); return; }

        busy = true;
        say(Lang.t(AccountActivity.this, "working"));
        final String salt = Account.newSalt();
        final String h = Account.hash(pass, salt);

        new Thread(new Runnable() {
            @Override public void run() {
                final Api.Res r = Api.login(email, h);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        busy = false;
                        if (r.ok) {
                            boolean v = r.json != null && r.json.optBoolean("verified", false);
                            String nm = r.json != null ? r.json.optString("name", "") : "";
                            Account.save(AccountActivity.this, email, nm, v);
                            Account.savePassword(AccountActivity.this, pass);
                            render();
                            Toast.makeText(AccountActivity.this,
                                    Lang.t(AccountActivity.this, "login_ok"),
                                    Toast.LENGTH_SHORT).show();
                            backToMain();
                        } else if (r.offline) {
                            // sunucu yok: bu telefonda kayitli sifre ile cevrimdisi giris
                            if (email.equals(Account.email(AccountActivity.this))
                                    && Account.hasPassword(AccountActivity.this)
                                    && Account.checkPassword(AccountActivity.this, pass)) {
                                Account.save(AccountActivity.this, email,
                                        Account.name(AccountActivity.this),
                                        Account.verified(AccountActivity.this));
                                render();
                                Toast.makeText(AccountActivity.this,
                                        Lang.t(AccountActivity.this, "offline_login"),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                say(Lang.t(AccountActivity.this, "offline_only"));
                            }
                        } else {
                            say(r.msg.length() > 0 ? r.msg : Lang.t(AccountActivity.this, "err_login"));
                        }
                    }
                });
            }
        }).start();
    }

    private void doVerify() {
        final String code = codeIn == null ? "" : codeIn.getText().toString().trim();
        final String email = codeEmail.length() > 0 ? codeEmail : Account.email(this);
        if (code.length() < 4) { say(Lang.t(AccountActivity.this, "err_code")); return; }
        busy = true;
        say(Lang.t(AccountActivity.this, "working"));
        new Thread(new Runnable() {
            @Override public void run() {
                final Api.Res r = Api.verify(email, code);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        busy = false;
                        if (r.ok) {
                            Account.save(AccountActivity.this, email,
                                    Account.name(AccountActivity.this), true);
                            codeStep = false;
                            render();
                            Toast.makeText(AccountActivity.this,
                                    Lang.t(AccountActivity.this, "verified_thanks"),
                                    Toast.LENGTH_LONG).show();
                            // dogrulama tamam: ana sayfaya don (misafir gorunumunde kalmasin)
                            backToMain();
                        } else if (r.offline) {
                            say(Lang.t(AccountActivity.this, "offline_login"));
                        } else {
                            say(r.msg.length() > 0 ? r.msg : Lang.t(AccountActivity.this, "err_code"));
                        }
                    }
                });
            }
        }).start();
    }

    private void doResend() {
        final String email = codeEmail.length() > 0 ? codeEmail : Account.email(this);
        busy = true;
        say(Lang.t(AccountActivity.this, "working"));
        new Thread(new Runnable() {
            @Override public void run() {
                final Api.Res r = Api.resend(email);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        busy = false;
                        if (r.offline) say(Lang.t(AccountActivity.this, "offline_login"));
                        else say(r.msg.length() > 0 ? r.msg
                                : Lang.t(AccountActivity.this, "code_sent"));
                    }
                });
            }
        }).start();
    }

    /** Uyelik islemi bitince ana sayfaya doner (geri tusuna basmak gerekmez). */
    private void backToMain() {
        android.content.Intent m = new android.content.Intent(this, MainActivity.class);
        m.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(m);
        finish();
    }

    private void say(String s) {
        if (status != null) status.setText(s);
    }

    // ---------------- kucuk yardimcilar ----------------
    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(12), dp(11), dp(12), dp(11));
        c.setBackground(rounded(CARD, 14));
        return c;
    }

    private EditText input(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(MUTED);
        e.setTextColor(TEXT);
        e.setTextSize(13);
        e.setInputType(type);
        e.setSingleLine(true);
        e.setBackground(rounded(SOFT, 12));
        e.setPadding(dp(10), dp(10), dp(10), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(4), 0, 0);
        e.setLayoutParams(lp);
        return e;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private TextView pill(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(TEXT);
        t.setTextSize(12);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12), dp(9), dp(12), dp(9));
        t.setBackground(rounded(SOFT, 16));
        return t;
    }

    private LinearLayout.LayoutParams margin(int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, dp(bottom));
        return lp;
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }
}
