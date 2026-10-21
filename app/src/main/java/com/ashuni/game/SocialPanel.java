package com.ashuni.game;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;

/** Fenêtre de la liste des succès. */
public class SocialPanel {

    private static int dp(Context c, int v) { 
        return (int) (v * c.getResources().getDisplayMetrics().density); 
    }

    public static void showAchievements(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("ashuni_achievements", Context.MODE_PRIVATE);
        
        // Récupération de la liste depuis l'objet Kotlin AchievementList
        List<Achievement> allAchievements = AchievementList.INSTANCE.getAll();
        
        int unlockedCount = 0;
        for (Achievement ach : allAchievements) {
            if (prefs.getBoolean("u_" + ach.getId(), false)) {
                unlockedCount++;
            }
        }

        ScrollView sv = new ScrollView(ctx);
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(ctx, 20), dp(ctx, 8), dp(ctx, 20), dp(ctx, 8));

        for (Achievement ach : allAchievements) {
            boolean unlocked = prefs.getBoolean("u_" + ach.getId(), false);
            
            TextView t = new TextView(ctx);
            t.setText(ach.getEmoji() + "  " + ach.getTitle() + "\n" + ach.getDesc());
            t.setTextColor(Color.WHITE);
            t.setTextSize(15);
            t.setPadding(0, dp(ctx, 8), 0, dp(ctx, 8));
            t.setAlpha(unlocked ? 1f : 0.4f);
            box.addView(t);
        }
        
        sv.addView(box);
        
        new AlertDialog.Builder(ctx, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle("🏆 Succès (" + unlockedCount + "/" + allAchievements.size() + ")")
                .setView(sv)
                .setPositiveButton("Fermer", null)
                .show();
    }
}
