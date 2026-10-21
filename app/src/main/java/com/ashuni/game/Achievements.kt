package com.ashuni.game

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.sqrt

data class Achievement(
    val id: String,
    val emoji: String,
    val title: String,
    val desc: String,
    val secret: Boolean = false
)

object AchievementList {
    private fun a(id: String, emoji: String, title: String, desc: String, secret: Boolean = false) =
        Achievement(id, emoji, title, desc, secret)

    val all: List<Achievement> = listOf(
        // --- Lancements ---
        a("first_launch", "🚀", "Premier pas", "Lance l'appli pour la première fois"),
        a("launch_5", "🔁", "Habitué", "Ouvre l'appli 5 fois"),
        a("launch_25", "🏠", "Fidèle", "Ouvre l'appli 25 fois"),
        a("launch_100", "💯", "Accro", "Ouvre l'appli 100 fois"),
        a("launch_365", "📆", "Rituel quotidien", "Ouvre l'appli 365 fois"),
        // --- Temps de jeu ---
        a("play_10m", "⏱️", "Échauffement", "10 minutes de jeu au total"),
        a("play_1h", "🕐", "Déjà une heure ?", "1 heure de jeu au total"),
        a("play_5h", "🎯", "Sérieux", "5 heures de jeu au total"),
        a("play_24h", "🌍", "Un jour entier", "24 heures de jeu au total"),
        a("play_100h", "🧠", "Pro", "100 heures de jeu au total"),
        a("session_30", "🔥", "Dans la zone", "30 minutes d'affilée"),
        a("session_2h", "🏃", "Marathon", "2 heures d'affilée"),
        a("session_5h", "🌱", "Va toucher de l'herbe", "5 heures d'affilée", true),
        // --- Séries de jours ---
        a("streak_3", "📈", "Trois jours de suite", "Joue 3 jours d'affilée"),
        a("streak_7", "🗓️", "Semaine parfaite", "Joue 7 jours d'affilée"),
        a("streak_30", "🏅", "Mois de feu", "Joue 30 jours d'affilée"),
        // --- Heures & dates ---
        a("night_owl", "🦉", "Oiseau de nuit", "Joue entre minuit et 4 h", true),
        a("early_bird", "🐦", "Lève-tôt", "Joue entre 5 h et 7 h", true),
        a("lunch", "🥪", "Pause déj", "Joue entre 12 h et 13 h"),
        a("h333", "👻", "L'heure du mystère", "Joue à 03:33", true),
        a("h1111", "🔮", "Fais un vœu", "Joue à 11:11", true),
        a("midnight", "🌑", "Minuit pile", "Joue à 00:00", true),
        a("h2359", "😅", "Juste à temps", "Joue à 23:59", true),
        a("friday13", "🔪", "Vendredi 13", "Joue un vendredi 13", true),
        a("new_year", "🎆", "Bonne année !", "Joue le 1er janvier", true),
        a("halloween", "🎃", "Joyeux Halloween", "Joue le 31 octobre", true),
        a("christmas", "🎄", "Joyeux Noël", "Joue le 25 décembre", true),
        a("valentine", "💘", "Saint-Valentin", "Joue le 14 février", true),
        a("april_fools", "🐟", "Poisson d'avril", "Joue le 1er avril", true),
        a("leap_day", "🐸", "Jour bissextile", "Joue un 29 février", true),
        a("weekend", "🛋️", "Joueur du week-end", "Joue un samedi ou un dimanche"),
        // --- Taps ---
        a("tap_100", "👆", "Premiers clics", "100 taps au total"),
        a("tap_1000", "✌️", "Doigts agiles", "1 000 taps au total"),
        a("tap_10000", "💪", "Pouces d'acier", "10 000 taps au total"),
        a("tap_50000", "🦾", "Tendinite", "50 000 taps au total"),
        a("tap_42", "🌌", "La réponse", "Fais exactement ton 42e tap", true),
        a("tap_777", "🎰", "Jackpot", "Fais exactement ton 777e tap", true),
        a("tap_1337", "😎", "Leet", "Fais exactement ton 1337e tap", true),
        a("rapid_20", "⚡", "Doigts de feu", "20 taps en 3 secondes"),
        a("rapid_40", "🔫", "Mitraillette", "40 taps en 3 secondes", true),
        // --- Gestes ---
        a("zen", "🧘", "Zen", "Maintiens un doigt 3 s sans bouger", true),
        a("three_fingers", "🖖", "Trinité", "Pose 3 doigts en même temps"),
        a("five_fingers", "✋", "Tope là !", "Pose 5 doigts en même temps", true),
        a("lightning", "🌩️", "Éclair", "Fais un swipe long et ultra rapide"),
        a("swiper_100", "👉", "Glisseur", "100 swipes"),
        a("swiper_1000", "🧈", "Ça glisse tout seul", "1 000 swipes"),
        a("corner_tour", "🧭", "Tour du propriétaire", "Tape les 4 coins dans l'ordre", true),
        a("dev_mode", "🛠️", "Mode développeur", "Tape 7 fois en haut au centre", true),
        a("bullseye", "🎯", "En plein dans le mille", "Tape 10 fois pile au centre", true),
        a("konami", "🕹️", "Code Konami", "Swipe ↑ ↑ ↓ ↓ ← → ← →", true),
        // --- Capteurs ---
        a("shake", "👋", "Secousse", "Secoue le téléphone"),
        a("shake_10", "🌋", "Tremblement de terre", "Secoue 10 fois"),
        a("shake_100", "🍸", "Cocktail shaker", "Secoue 100 fois"),
        a("face_down", "🙈", "Je ne regarde pas", "Pose le téléphone face vers le bas", true),
        a("upside_down", "🙃", "Tête en bas", "Retourne le téléphone", true),
        a("rotate_5", "🔄", "Ça tourne", "Change 5 fois l'orientation"),
        a("rotate_50", "🌀", "Toupie", "Change 50 fois l'orientation"),
        // --- Touches ---
        a("vol_up_10", "🔊", "Plus fort !", "Appuie 10 fois sur Volume +"),
        a("vol_down_10", "🔉", "Plus doux", "Appuie 10 fois sur Volume −"),
        a("dj", "🎧", "DJ Ashuni", "Volume + + − + −", true),
        a("back_20", "↩️", "Retour, retour", "Appuie 20 fois sur Retour"),
        a("back_spam", "🏳️", "Panique", "10 retours en 5 secondes", true),
        // --- Web & plein écran ---
        a("first_page", "🌐", "Connecté", "Charge une page pour la première fois"),
        a("pages_100", "🧭", "Explorateur", "Charge 100 pages"),
        a("lost_void", "🕳️", "Perdu dans le vide", "Rencontre une erreur de chargement", true),
        a("fullscreen", "🖥️", "Plein écran", "Passe en plein écran"),
        a("fullscreen_10", "🎬", "Cinéma", "Passe 10 fois en plein écran"),
        // --- Menu trophées ---
        a("open_trophies", "🏆", "Salle des trophées", "Ouvre la liste des succès"),
        a("trophy_fan", "🛡️", "Collectionneur", "Ouvre les succès 10 fois"),
        a("drag_button", "🧲", "Déménageur", "Déplace le bouton 🏆"),
        a("drag_20", "🚚", "Camion de déménagement", "Déplace le bouton 20 fois"),
        a("long_trophy", "🗝️", "Menu caché", "Appui long sur le bouton 🏆", true),
        // --- Méta ---
        a("unlock_5", "🥉", "Chasseur", "Débloque 5 succès"),
        a("unlock_15", "🥈", "Chasseur confirmé", "Débloque 15 succès"),
        a("unlock_30", "🥇", "Maître chasseur", "Débloque 30 succès"),
        a("unlock_50", "💎", "Diamant", "Débloque 50 succès"),
        a("platinum", "👑", "Platine", "Débloque tous les autres succès")
    )

    val byId: Map<String, Achievement> = all.associateBy { it.id }
}

class AchievementManager(private val activity: Activity) : SensorEventListener {

    private val prefs = activity.getSharedPreferences("ashuni_achievements", Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())
    private val sm = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var sessionStart = System.currentTimeMillis()

    // ------------------------------------------------------------------
    // API à appeler depuis MainActivity
    // ------------------------------------------------------------------

    fun attach() {
        addTrophyButton()
        val launches = addCount("launch")
        unlock("first_launch")
        reach(launches, 5, "launch_5")
        reach(launches, 25, "launch_25")
        reach(launches, 100, "launch_100")
        reach(launches, 365, "launch_365")
        updateStreak()
        timeChecks()
    }

    fun onResume() {
        sessionStart = System.currentTimeMillis()
        sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        handler.post(ticker)
    }

    fun onPause() {
        handler.removeCallbacks(ticker)
        sm.unregisterListener(this)
        addCount("play_ms", System.currentTimeMillis() - sessionStart)
    }

    fun onTouch(ev: MotionEvent) {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x; downY = ev.y; downT = ev.eventTime; maxPtr = 1; moved = false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                maxPtr = maxOf(maxPtr, ev.pointerCount)
                if (maxPtr >= 3) unlock("three_fingers")
                if (maxPtr >= 5) unlock("five_fingers")
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(ev.x - downX) > 30 || abs(ev.y - downY) > 30) moved = true
            }
            MotionEvent.ACTION_UP -> {
                val dt = ev.eventTime - downT
                if (maxPtr == 1) {
                    if (!moved && dt < 300) onTap(ev.x, ev.y, ev.eventTime)
                    else if (!moved && dt >= 3000) unlock("zen")
                    else if (moved) onSwipe(ev.x - downX, ev.y - downY, dt)
                }
            }
        }
    }

    fun onKey(keyCode: Int, event: KeyEvent) {
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount != 0) return
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                reach(addCount("vol_up"), 10, "vol_up_10")
                pushVol('U')
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                reach(addCount("vol_down"), 10, "vol_down_10")
                pushVol('D')
            }
            KeyEvent.KEYCODE_BACK -> onBack()
        }
    }

    fun onBack() {
        reach(addCount("back"), 20, "back_20")
        val now = System.currentTimeMillis()
        backTimes.add(now)
        backTimes.removeAll { now - it > 5000 }
        if (backTimes.size >= 10) unlock("back_spam")
    }

    fun onPageLoaded() {
        unlock("first_page")
        reach(addCount("pages"), 100, "pages_100")
    }

    fun onWebError() = unlock("lost_void")

    fun onFullscreenChanged(entered: Boolean) {
        if (!entered) return
        unlock("fullscreen")
        reach(addCount("fs"), 10, "fullscreen_10")
    }

    fun onRotate() {
        val n = addCount("rotate")
        reach(n, 5, "rotate_5")
        reach(n, 50, "rotate_50")
    }

    // ------------------------------------------------------------------
    // Stockage / déblocage
    // ------------------------------------------------------------------

    private fun count(key: String) = prefs.getLong("c_$key", 0L)

    private fun addCount(key: String, by: Long = 1L): Long {
        val v = count(key) + by
        prefs.edit().putLong("c_$key", v).apply()
        return v
    }

    fun isUnlocked(id: String) = prefs.getBoolean("u_$id", false)

    private fun reach(value: Long, target: Long, id: String) {
        if (value >= target) unlock(id)
    }

    fun unlock(id: String) {
        if (isUnlocked(id)) return
        val a = AchievementList.byId[id] ?: return
        prefs.edit().putBoolean("u_$id", true).apply()
        enqueue(a)
        activity.window.decorView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        if (a.secret) emojiRain(a.emoji)
        checkMeta()
    }

    private fun checkMeta() {
        val n = AchievementList.all.count { isUnlocked(it.id) }
        if (n >= 5) unlock("unlock_5")
        if (n >= 15) unlock("unlock_15")
        if (n >= 30) unlock("unlock_30")
        if (n >= 50) unlock("unlock_50")
        if (AchievementList.all.filter { it.id != "platinum" }.all { isUnlocked(it.id) }) unlock("platinum")
    }

    // ------------------------------------------------------------------
    // Temps, dates, séries
    // ------------------------------------------------------------------

    private val ticker = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val session = (now - sessionStart) / 60_000
            if (session >= 30) unlock("session_30")
            if (session >= 120) unlock("session_2h")
            if (session >= 300) unlock("session_5h")
            val total = (count("play_ms") + now - sessionStart) / 60_000
            if (total >= 10) unlock("play_10m")
            if (total >= 60) unlock("play_1h")
            if (total >= 300) unlock("play_5h")
            if (total >= 1_440) unlock("play_24h")
            if (total >= 6_000) unlock("play_100h")
            timeChecks()
            handler.postDelayed(this, 20_000)
        }
    }

    private fun timeChecks() {
        val c = Calendar.getInstance()
        val h = c.get(Calendar.HOUR_OF_DAY)
        val m = c.get(Calendar.MINUTE)
        val d = c.get(Calendar.DAY_OF_MONTH)
        val mo = c.get(Calendar.MONTH)
        val dow = c.get(Calendar.DAY_OF_WEEK)
        if (h in 0..3) unlock("night_owl")
        if (h in 5..6) unlock("early_bird")
        if (h == 12) unlock("lunch")
        if (h == 3 && m == 33) unlock("h333")
        if (h == 11 && m == 11) unlock("h1111")
        if (h == 0 && m == 0) unlock("midnight")
        if (h == 23 && m == 59) unlock("h2359")
        if (dow == Calendar.FRIDAY && d == 13) unlock("friday13")
        if (mo == Calendar.JANUARY && d == 1) unlock("new_year")
        if (mo == Calendar.OCTOBER && d == 31) unlock("halloween")
        if (mo == Calendar.DECEMBER && d == 25) unlock("christmas")
        if (mo == Calendar.FEBRUARY && d == 14) unlock("valentine")
        if (mo == Calendar.APRIL && d == 1) unlock("april_fools")
        if (mo == Calendar.FEBRUARY && d == 29) unlock("leap_day")
        if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) unlock("weekend")
    }

    private fun updateStreak() {
        val now = System.currentTimeMillis()
        val today = (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
        val last = prefs.getLong("last_day", -1L)
        var streak = prefs.getLong("streak", 0L)
        if (last != today) {
            streak = if (last == today - 1) streak + 1 else 1
            prefs.edit().putLong("last_day", today).putLong("streak", streak).apply()
        }
        reach(streak, 3, "streak_3")
        reach(streak, 7, "streak_7")
        reach(streak, 30, "streak_30")
    }

    // ------------------------------------------------------------------
    // Gestes
    // ------------------------------------------------------------------

    private var downX = 0f
    private var downY = 0f
    private var downT = 0L
    private var maxPtr = 0
    private var moved = false
    private val tapTimes = ArrayList<Long>()
    private val devTaps = ArrayList<Long>()
    private val backTimes = ArrayList<Long>()
    private val cornerSeq = ArrayList<Int>()
    private var cornerT = 0L
    private val swipes = ArrayList<Char>()
    private val volSeq = StringBuilder()

    private fun onTap(x: Float, y: Float, t: Long) {
        val n = addCount("taps")
        reach(n, 100, "tap_100")
        reach(n, 1_000, "tap_1000")
        reach(n, 10_000, "tap_10000")
        reach(n, 50_000, "tap_50000")
        if (n == 42L) unlock("tap_42")
        if (n == 777L) unlock("tap_777")
        if (n == 1337L) unlock("tap_1337")

        tapTimes.add(t)
        tapTimes.removeAll { t - it > 3000 }
        if (tapTimes.size >= 20) unlock("rapid_20")
        if (tapTimes.size >= 40) unlock("rapid_40")

        val content = activity.findViewById<View>(android.R.id.content)
        val w = content.width.toFloat()
        val h = content.height.toFloat()
        if (w <= 0f || h <= 0f) return

        val c = cornerOf(x, y, w, h)
        if (c >= 0) {
            if (cornerSeq.isNotEmpty() && t - cornerT > 6000) cornerSeq.clear()
            if (cornerSeq.isEmpty()) cornerT = t
            if (c == cornerSeq.size) {
                cornerSeq.add(c)
                if (cornerSeq.size == 4) {
                    unlock("corner_tour")
                    cornerSeq.clear()
                }
            } else {
                cornerSeq.clear()
                if (c == 0) {
                    cornerSeq.add(0)
                    cornerT = t
                }
            }
        }

        if (y < h * 0.08f && abs(x - w / 2) < w * 0.15f) {
            devTaps.add(t)
            devTaps.removeAll { t - it > 4000 }
            if (devTaps.size >= 7) unlock("dev_mode")
        }

        if (abs(x - w / 2) < w * 0.03f && abs(y - h / 2) < h * 0.03f) {
            reach(addCount("center"), 10, "bullseye")
        }
    }

    private fun cornerOf(x: Float, y: Float, w: Float, h: Float): Int {
        val left = x < w * 0.18f
        val right = x > w * 0.82f
        val top = y < h * 0.18f
        val bottom = y > h * 0.82f
        return when {
            top && left -> 0
            top && right -> 1
            bottom && right -> 2
            bottom && left -> 3
            else -> -1
        }
    }

    private fun onSwipe(dx: Float, dy: Float, dt: Long) {
        val dist = sqrt(dx * dx + dy * dy)
        if (dist > 600 && dt < 150) unlock("lightning")
        if (dist < 150) return
        val n = addCount("swipes")
        reach(n, 100, "swiper_100")
        reach(n, 1_000, "swiper_1000")
        val dir = if (abs(dx) > abs(dy)) {
            if (dx > 0) 'R' else 'L'
        } else {
            if (dy > 0) 'D' else 'U'
        }
        swipes.add(dir)
        if (swipes.size > 8) swipes.removeAt(0)
        if (swipes.joinToString("") == "UUDDLRLR") unlock("konami")
    }

    private fun pushVol(c: Char) {
        volSeq.append(c)
        if (volSeq.length > 6) volSeq.deleteCharAt(0)
        if (volSeq.toString() == "UUDDUD") unlock("dj")
    }

    // ------------------------------------------------------------------
    // Capteurs
    // ------------------------------------------------------------------

    private var lastShake = 0L

    override fun onSensorChanged(e: SensorEvent) {
        val x = e.values[0]
        val y = e.values[1]
        val z = e.values[2]
        val g = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        val now = System.currentTimeMillis()
        if (g > 2.7f && now - lastShake > 600) {
            lastShake = now
            unlock("shake")
            val n = addCount("shakes")
            reach(n, 10, "shake_10")
            reach(n, 100, "shake_100")
        }
        if (z < -9.3f) unlock("face_down")
        if (y < -9.3f) unlock("upside_down")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ------------------------------------------------------------------
    // UI : toast, pluie d'emojis, bouton 🏆, listes
    // ------------------------------------------------------------------

    private val contentRoot: ViewGroup get() = activity.findViewById(android.R.id.content)

    private fun enqueue(a: Achievement) {
        // Logique de file d'attente des notifications de trophées
    }

    private fun emojiRain(emoji: String) {
        // Effet de pluie d'emojis pour les succès secrets
    }

    private fun addTrophyButton() {
        // Ajout du bouton 🏆 dans l'interface
    }
          }
          
