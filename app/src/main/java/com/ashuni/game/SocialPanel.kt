package com.ashuni.game

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Fenêtre de la liste des succès. */
object SocialPanel {

    private fun dp(c: Context, v: Int): Int {
        return (v * c.resources.displayMetrics.density).toInt()
    }

    fun showAchievements(ctx: Context) {
        val prefs = ctx.getSharedPreferences("ashuni_achievements", Context.MODE_PRIVATE)
        val allAchievements = AchievementList.all
        
        var unlockedCount = 0
        for (ach in allAchievements) {
            if (prefs.getBoolean("u_${ach.id}", false)) {
                unlockedCount++
            }
        }

        val sv = ScrollView(ctx)
        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(ctx, 20), dp(ctx, 8), dp(ctx, 20), dp(ctx, 8))
        }

        for (ach in allAchievements) {
            val unlocked = prefs.getBoolean("u_${ach.id}", false)
            val t = TextView(ctx).apply {
                text = "${ach.emoji}  ${ach.title}\n${ach.desc}"
                setTextColor(Color.WHITE)
                textSize = 15f
                setPadding(0, dp(ctx, 8), 0, dp(ctx, 8))
                alpha = if (unlocked) 1f else 0.4f
            }
            box.addView(t)
        }
        
        sv.addView(box)

        AlertDialog.Builder(ctx, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
            .setTitle("🏆 Succès ($unlockedCount/${allAchievements.size})")
            .setView(sv)
            .setPositiveButton("Fermer", null)
            .show()
    }
}
