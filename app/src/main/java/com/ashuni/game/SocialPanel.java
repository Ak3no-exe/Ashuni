package com.ashuni.game;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

/** Fenêtres : menu, succès, classement, compte. */
public class SocialPanel {

    private static AlertDialog.Builder builder(Context c) {
        return new AlertDialog.Builder(c, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
    }
    private static int dp(Context c, int v) { return (int) (v * c.getResources().getDisplayMetrics().density); }

    public static void showMenu(final MainActivity act) {
        String me = Api.prefs(act).getString("username", "");
        String[] items = { "🏆  Succès", "📊  Classement", "👤  Compte (" + me + ")" };
        builder(act).setTitle("Menu")
                .setItems(items, new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int which) {
                        if (which == 0) showAchievements(act);
                        else if (which == 1) showLeaderboard(act);
                        else showAccount(act);
                    }
                })
                .setNegativeButton("Fermer", null)
                .show();
    }

    public static void showAchievements(Context ctx) {
        Achievements a = Achievements.get(ctx);
        ScrollView sv = new ScrollView(ctx);
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(ctx, 20), dp(ctx, 8), dp(ctx, 20), dp(ctx, 8));
        for (Achievements.Def d : Achievements.ALL) {
            if (!Achievements.visible(d)) continue;
            TextView t = new TextView(ctx);
            t.setText(d.icon + "  " + d.name + "\n" + d.desc);
            t.setTextColor(Color.WHITE);
            t.setTextSize(15);
            t.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
            t.setAlpha(a.isUnlocked(d.id) ? 1f : 0.4f);
            box.addView(t);
        }
        sv.addView(box);
        builder(ctx)
                .setTitle("🏆 Succès (" + a.unlockedCount() + "/" + Achievements.visibleCount() + ")")
                .setView(sv)
                .setPositiveButton("Fermer", null)
                .show();
    }

    public static void showLeaderboard(final Context ctx) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(ctx, 20), dp(ctx, 8), dp(ctx, 20), dp(ctx, 8));

        final TextView list = new TextView(ctx);
        list.setTextColor(Color.WHITE);
        list.setTextSize(15);
        list.setText("Chargement…");

        LinearLayout tabs = new LinearLayout(ctx);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        Button bTime = new Button(ctx);
        bTime.setText("Temps de jeu");
        Button bAch = new Button(ctx);
        bAch.setText("Succès");
        bTime.setAllCaps(false);
        bAch.setAllCaps(false);
        bTime.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { loadBoard(ctx, "time", list); } });
        bAch.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { loadBoard(ctx, "achievements", list); } });
        tabs.addView(bTime, new LinearLayout.LayoutParams(0, -2, 1));
        tabs.addView(bAch, new LinearLayout.LayoutParams(0, -2, 1));

        ScrollView sv = new ScrollView(ctx);
        sv.addView(list);
        box.addView(tabs);
        box.addView(sv);

        builder(ctx).setTitle("📊 Classement").setView(box).setPositiveButton("Fermer", null).show();
        loadBoard(ctx, "time", list);
    }

    private static void loadBoard(final Context ctx, final String mode, final TextView list) {
        list.setText("Chargement…");
        Api.call(ctx, "GET", "/api/leaderboard?mode=" + mode, null, new Api.Callback() {
            @Override public void done(JSONObject json, String error, int status) {
                if (error != null || json == null) { list.setText(error != null ? error : "Erreur."); return; }
                JSONArray rows = json.optJSONArray("rows");
                if (rows == null || rows.length() == 0) { list.setText("Aucun joueur pour l'instant."); return; }
                String me = Api.prefs(ctx).getString("username", "").toLowerCase();
                String[] medals = { "🥇", "🥈", "🥉" };
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject r = rows.optJSONObject(i);
                    if (r == null) continue;
                    String name = r.optString("username");
                    String val = mode.equals("time") ? fmt(r.optLong("time")) : r.optInt("achievements") + " 🏆";
                    if (name.toLowerCase().equals(me)) sb.append("➤ ");
                    sb.append(i < 3 ? medals[i] : (i + 1) + ".").append("  ").append(name).append(" — ").append(val).append("\n\n");
                }
                list.setText(sb.toString().trim());
            }
        });
    }

    private static String fmt(long s) {
        long h = s / 3600, m = (s % 3600) / 60;
        return h > 0 ? h + " h " + (m < 10 ? "0" : "") + m : m + " min";
    }

    public static void showAccount(final MainActivity act) {
        String me = Api.prefs(act).getString("username", "");
        builder(act).setTitle("👤 Compte")
                .setMessage("Connecté en tant que " + me)
                .setPositiveButton("Se déconnecter", new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int w) { act.logout(); }
                })
                .setNegativeButton("Fermer", null)
                .show();
    }
}
