package com.ashuni.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONObject;

/** Écran de lancement : créer un compte ou se connecter. */
public class AuthActivity extends Activity {
    private boolean register = false;
    private EditText user, pass;
    private TextView err;
    private Button go, tabLogin, tabReg;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (!Api.token(this).isEmpty()) { openGame(); return; }

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0xFF0D1020);
        sv.setFillViewport(true);
        sv.setFitsSystemWindows(true);

        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setGravity(Gravity.CENTER);
        sv.addView(outer, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(20), dp(20), dp(20));
        box.setBackground(pill(0xFF171B33, 18));
        outer.addView(box, new LinearLayout.LayoutParams(dp(340), -2));

        TextView title = new TextView(this);
        title.setText("🎮 Bienvenue");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        box.addView(title);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabLogin = tabButton("Connexion");
        tabReg = tabButton("Créer un compte");
        tabs.addView(tabLogin, new LinearLayout.LayoutParams(0, dp(44), 1));
        tabs.addView(tabReg, new LinearLayout.LayoutParams(0, dp(44), 1));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1, -2);
        tlp.setMargins(0, dp(14), 0, dp(10));
        box.addView(tabs, tlp);

        user = field("Pseudo", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        pass = field("Mot de passe", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        box.addView(user);
        box.addView(pass);

        err = new TextView(this);
        err.setTextColor(0xFFFF7675);
        err.setTextSize(14);
        err.setGravity(Gravity.CENTER);
        err.setPadding(0, dp(4), 0, dp(4));
        box.addView(err);

        go = new Button(this);
        go.setTextColor(Color.WHITE);
        go.setAllCaps(false);
        go.setTextSize(16);
        go.setBackground(pill(0xFF6C5CE7, 12));
        go.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { submit(); } });
        box.addView(go, new LinearLayout.LayoutParams(-1, dp(48)));

        tabLogin.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { setMode(false); } });
        tabReg.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { setMode(true); } });
        setMode(false);
        setContentView(sv);
    }

    private void setMode(boolean reg) {
        register = reg;
        tabLogin.setBackground(pill(reg ? 0xFF232850 : 0xFF6C5CE7, 10));
        tabReg.setBackground(pill(reg ? 0xFF6C5CE7 : 0xFF232850, 10));
        go.setText(reg ? "Créer mon compte" : "Se connecter");
        err.setText("");
    }

    private void submit() {
        final String u = user.getText().toString().trim();
        final String p = pass.getText().toString();
        if (u.isEmpty() || p.isEmpty()) { err.setText("Remplis le pseudo et le mot de passe."); return; }
        if (Config.API_URL.contains("TON-SERVEUR")) { err.setText("Configure API_URL dans Config.java."); return; }
        err.setText("");
        go.setEnabled(false);
        try {
            JSONObject body = new JSONObject();
            body.put("username", u);
            body.put("password", p);
            Api.call(this, "POST", register ? "/api/register" : "/api/login", body, new Api.Callback() {
                @Override public void done(JSONObject json, String error, int status) {
                    go.setEnabled(true);
                    if (error != null || json == null) { err.setText(error != null ? error : "Erreur inconnue."); return; }
                    JSONObject userObj = json.optJSONObject("user");
                    Api.prefs(AuthActivity.this).edit()
                            .putString("token", json.optString("token"))
                            .putString("username", userObj != null ? userObj.optString("username", u) : u)
                            .apply();
                    Achievements.get(AuthActivity.this).merge(userObj);
                    openGame();
                }
            });
        } catch (Exception e) {
            go.setEnabled(true);
            err.setText("Erreur inattendue.");
        }
    }

    private void openGame() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private Button tabButton(String text) {
        Button t = new Button(this);
        t.setText(text);
        t.setAllCaps(false);
        t.setTextColor(Color.WHITE);
        t.setTextSize(14);
        return t;
    }

    private EditText field(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(0xFF8890B5);
        e.setTextColor(Color.WHITE);
        e.setSingleLine(true);
        e.setInputType(type);
        e.setPadding(dp(14), dp(12), dp(14), dp(12));
        e.setBackground(pill(0xFF0D1020, 10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(10));
        e.setLayoutParams(lp);
        return e;
    }

    private GradientDrawable pill(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density); }
}
