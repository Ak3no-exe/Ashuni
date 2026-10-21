package com.ashuni.game;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Fenêtre de la liste des succès. */
public class SocialPanel {

    private static int dp(Context c, int v) { 
        return (int) (v * c.getResources().getDisplayMetrics().density); 
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
        
        new AlertDialog.Builder(ctx, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("🏆 Succès (" + a.unlockedCount() + "/" + Achievements.visibleCount() + ")")
                .setView(sv)
                .setPositiveButton("Fermer", null)
                .show();
    }
}
