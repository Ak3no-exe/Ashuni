package com.ashuni.game

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var achievementManager: AchievementManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialisation du gestionnaire de succès
        achievementManager = AchievementManager(this)
        
        // Attacher le gestionnaire (bouton 🏆, vérifications de démarrage, etc.)
        achievementManager.attach()
    }

    override fun onResume() {
        super.onResume()
        achievementManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        achievementManager.onPause()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        achievementManager.onTouch(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        achievementManager.onKey(keyCode, event)
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        achievementManager.onBack()
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        achievementManager.onRotate()
    }
}
