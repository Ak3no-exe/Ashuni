package com.ashuni.game

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var achievementManager: AchievementManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Création d'une vue de base pour éviter le crash au lancement
        val rootLayout = FrameLayout(this)
        setContentView(rootLayout)

        // Initialisation et attachement du gestionnaire de succès
        achievementManager = AchievementManager(this)
        achievementManager.attach()
    }

    override fun onResume() {
        super.onResume()
        if (::achievementManager.isInitialized) {
            achievementManager.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::achievementManager.isInitialized) {
            achievementManager.onPause()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (::achievementManager.isInitialized) {
            achievementManager.onTouch(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (::achievementManager.isInitialized) {
            achievementManager.onKey(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (::achievementManager.isInitialized) {
            achievementManager.onBack()
        }
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::achievementManager.isInitialized) {
            achievementManager.onRotate()
        }
    }
}
