package com.ashuni.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Achievements {

    public static class Def {
        public final String id, icon, name, desc, stat;
        public final long goal;
        public final boolean hook;
        Def(String id, String icon, String name, String desc, String stat, long goal, boolean hook) {
            this.id = id; this.icon = icon; this.name = name; this.desc = desc;
            this.stat = stat; this.goal = goal; this.hook = hook;
        }
    }
    public interface Listener { void onUnlocked(Def d); }

    // ---------- LISTE DES SUCCÈS (ajoute les tiens ici) ----------
    public static final List<Def> ALL = new ArrayList<Def>();
    private static void add(String id, String icon, String name, String desc, String stat, long goal) {
        ALL.add(new Def(id, icon, name, desc, stat, goal, false));
    }
    // succès liés au jeu (pièces, score) : visibles seulement si Config.GAME_HOOKS = true
    private static void hook(String id, String icon, String name, String desc, String stat, long goal) {
        ALL.add(new Def(id, icon, name, desc, stat, goal, true));
    }
    static {
        add("launch_1",  "🎮", "Bienvenue",       "Lancer l'appli pour la première fois", "launches", 1);
        add("launch_10", "🔁", "Habitué",         "Lancer l'appli 10 fois",               "launches", 10);
        add("launch_50", "🏠", "Pilier",          "Lancer l'appli 50 fois",               "launches", 50);

        add("play_10m",  "🔥", "Échauffement",    "Jouer 10 minutes au total",            "totalSeconds", 600);
        add("play_1h",   "⏳", "Déjà une heure ?", "Jouer 1 heure au total",               "totalSeconds", 3600);
        add("play_10h",  "🕹️", "Accro",           "Jouer 10 heures au total",             "totalSeconds", 36000);
        add("play_50h",  "♾️", "Sans limites",    "Jouer 50 heures au total",             "totalSeconds", 180000);

        add("sess_15",   "🎯", "Concentré",       "15 minutes de jeu d'affilée",          "bestSession", 900);
        add("sess_30",   "🏃", "Marathon",        "30 minutes de jeu d'affilée",          "bestSession", 1800);
        add("sess_60",   "💪", "Irréductible",    "1 heure de jeu d'affilée",             "bestSession", 3600);
        add("sess_120",  "🦾", "Increvable",      "2 heures de jeu d'affilée",            "bestSession", 7200);

        add("streak_3",  "📅", "Régulier",        "Jouer 3 jours d'affilée",              "daysStreak", 3);
        add("streak_7",  "🗓️", "Assidu",          "Jouer 7 jours d'affilée",              "daysStreak", 7);
        add("streak_30", "👑", "Légende",         "Jouer 30 jours d'affilée",             "daysStreak", 30);

        add("night",     "🌙", "Noctambule",      "Jouer entre minuit et 5 h",            "nightSessions", 1);

        hook("coins_100",    "🪙", "Petite monnaie",  "Ramasser 100 pièces au total",       "coinsTotal", 100);
        hook("coins_1000",   "💰", "Collectionneur",  "Ramasser 1 000 pièces au total",     "coinsTotal", 1000);
        hook("run_coins_50", "🧲", "Aimant à pièces", "50 pièces dans une seule partie",    "bestCoinsRun", 50);
        hook("noloss_30",    "🛡️", "Prudent",         "30 s sans perdre de pièces",         "bestNoLoss", 30);
        hook("noloss_60",    "💎", "Intouchable",     "1 minute sans perdre de pièces",     "bestNoLoss", 60);
        hook("noloss_120",   "🧊", "Sang-froid",      "2 minutes sans perdre de pièces",    "bestNoLoss", 120);
        hook("noloss_300",   "🏆", "Inatteignable",   "5 minutes sans perdre de pièces",    "bestNoLoss", 300);
        hook("score_1000",   "⭐", "Bon début",       "Faire 1 000 points",                 "bestScore", 1000);
        hook("score_10000",  "🌟", "Pro",             "Faire 10 000 points",                "bestScore", 10000);
    }

    public static boolean visible(Def d) { return !d.hook || Config.GAME_HOOKS; }
    public static int visibleCount() {
        int n = 0;
        for (Def d : ALL) if (visible(d)) n++;
        return n;
    }

    // ---------- INSTANCE ----------
    private static Achievements inst;
    public static synchronized Achievements get(Context c) {
        if (inst == null) inst = new Achievements(c.getApplicationContext());
        return inst;
    }

    private final Context app;
    private final SharedPreferences sp;
    private final Handler main = new Handler(Looper.getMainLooper());
    private JSONObject unlocked = new JSONObject();
    private JSONObject stats = new JSONObject();
    public volatile Listener listener;
    private long sessionSeconds;
    private boolean nightCounted;
    private long runStart, lastLoss, runCoins, runLost;
    private int ticks;

    private Achievements(Context c) {
        app = c;
        sp = c.getSharedPreferences("ashuni_social", Context.MODE_PRIVATE);
        load();
    }

    private void load() {
        try { unlocked = new JSONObject(sp.getString("ach", "{}")); } catch (Exception e) { unlocked = new JSONObject(); }
        try { stats = new JSONObject(sp.getString("stats", "{}")); } catch (Exception e) { stats = new JSONObject(); }
    }
    private void save() { sp.edit().putString("ach", unlocked.toString()).putString("stats", stats.toString()).apply(); }
    private static void put(JSONObject o, String k, long v) { try { o.put(k, v); } catch (Exception ignored) {} }
    private void setMax(String k, long v) { if (v > stats.optLong(k, 0)) put(stats, k, v); }
    private void inc(String k, long n) { put(stats, k, stats.optLong(k, 0) + n); }

    public synchronized boolean isUnlocked(String id) { return unlocked.has(id); }
    public synchronized int unlockedCount() {
        int n = 0;
        for (Def d : ALL) if (visible(d) && unlocked.has(d.id)) n++;
        return n;
    }

    private void check() {
        boolean changed = false;
        for (final Def d : ALL) {
            if (!visible(d) || unlocked.has(d.id)) continue;
            if (stats.optLong(d.stat, 0) >= d.goal) {
                put(unlocked, d.id, System.currentTimeMillis());
                changed = true;
                main.post(new Runnable() {
                    @Override public void run() { Listener l = listener; if (l != null) l.onUnlocked(d); }
                });
            }
        }
        if (changed) save();
    }

    // ---------- cycle de vie ----------
    public synchronized void onLaunch() {
        inc("launches", 1);
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String today = f.format(new Date());
        String last = sp.getString("lastDay", "");
        if (!today.equals(last)) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -1);
            long streak = last.equals(f.format(c.getTime())) ? sp.getLong("streak", 0) + 1 : 1;
            sp.edit().putString("lastDay", today).putLong("streak", streak).apply();
            setMax("daysStreak", streak);
        }
        save();
        check();
    }
    public synchronized void beginSession() { sessionSeconds = 0; nightCounted = false; }

    /** Appelée chaque seconde pendant que l'appli est au premier plan. */
    public synchronized void tick() {
        sessionSeconds++;
        inc("totalSeconds", 1);
        setMax("bestSession", sessionSeconds);
        if (!nightCounted && sessionSeconds >= 60 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 5) {
            nightCounted = true;
            inc("nightSessions", 1);
        }
        if (runStart > 0) setMax("bestNoLoss", (System.currentTimeMillis() - lastLoss) / 1000);
        check();
        ticks++;
        if (ticks % 10 == 0) save();
    }
    public synchronized void pause() { save(); }

    // ---------- hooks du jeu (utilisés si Config.GAME_HOOKS = true) ----------
    public synchronized void hookStartRun() {
        runStart = lastLoss = System.currentTimeMillis();
        runCoins = 0; runLost = 0;
    }
    public synchronized void hookCoin(int n) {
        if (runStart == 0) hookStartRun();
        runCoins += n;
        inc("coinsTotal", n);
        setMax("bestCoinsRun", runCoins);
        check();
    }
    public synchronized void hookCoinLost(int n) {
        if (runStart == 0) hookStartRun();
        runLost += n;
        lastLoss = System.currentTimeMillis();
    }
    public synchronized void hookEndRun(long score) {
        if (runStart > 0) setMax("bestNoLoss", (System.currentTimeMillis() - lastLoss) / 1000);
        runStart = 0;
        if (score < 0) score = 0;
        setMax("bestScore", score);
        inc("runs", 1);
        check();
        save();
    }
}
ator<String> it = s.keys(); it.hasNext();) {
                String k = it.next();
                setMax(k, s.optLong(k));
            }
        }
        save();
        check();
    }

    public synchronized void clearAll() {
        unlocked = new JSONObject();
        stats = new JSONObject();
        sessionSeconds = 0; runStart = 0; ticks = 0;
        sp.edit().clear().apply();
    }
}
