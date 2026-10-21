package com.ashuni.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private int oldUiVisibility;
    private static final String HOME = "https://ashuni.lol/";

    private final Handler tickHandler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            Achievements.get(MainActivity.this).tick();
            tickHandler.postDelayed(this, 1000);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportMultipleWindows(false);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (Config.GAME_HOOKS) webView.addJavascriptInterface(new Bridge(), "AshuniSocial");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if (host != null && (host.equals("ashuni.lol") || host.endsWith(".ashuni.lol"))) return false;
                // Open external links in the browser instead of allowing unrelated pages in the app.
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                catch (Exception ignored) { Toast.makeText(MainActivity.this, "Lien impossible à ouvrir", Toast.LENGTH_SHORT).show(); }
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) { callback.onCustomViewHidden(); return; }
                customView = view; customViewCallback = callback;
                oldUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                FrameLayout decor = (FrameLayout) getWindow().getDecorView();
                decor.addView(customView, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(5894 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                webView.setVisibility(View.GONE);
            }
            @Override public void onHideCustomView() { hideCustomView(); }
        });

        // Conteneur : le jeu + un petit bouton 🏆 pour ouvrir le menu (succès / classement / compte)
        FrameLayout root = new FrameLayout(this);
        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        root.addView(menuButton());
        setContentView(root);

        if (state == null) webView.loadUrl(HOME); else webView.restoreState(state);

        // Succès
        final Achievements ach = Achievements.get(this);
        ach.listener = new Achievements.Listener() {
            @Override public void onUnlocked(Achievements.Def d) {
                Toast.makeText(MainActivity.this, d.icon + " Succès débloqué : " + d.name, Toast.LENGTH_LONG).show();
            }
        };
        ach.onLaunch();
    }

    private View menuButton() {
        TextView btn = new TextView(this);
        btn.setText("🏆");
        btn.setTextSize(18);
        btn.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(0x88000000);
        btn.setBackground(bg);
        int size = (int) (40 * getResources().getDisplayMetrics().density);
        int margin = (int) (8 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size, Gravity.TOP | Gravity.START);
        lp.setMargins(margin, margin, 0, 0);
        btn.setLayoutParams(lp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { SocialPanel.showAchievements(MainActivity.this); }
        });
        return btn;
    }

    /** Fonctions appelables depuis la page : window.AshuniSocial.coin(1) etc. (si Config.GAME_HOOKS) */
    private class Bridge {
        @JavascriptInterface public void startRun() { Achievements.get(MainActivity.this).hookStartRun(); }
        @JavascriptInterface public void coin(int n) { Achievements.get(MainActivity.this).hookCoin(n); }
        @JavascriptInterface public void coinLost(int n) { Achievements.get(MainActivity.this).hookCoinLost(n); }
        @JavascriptInterface public void endRun(int score) { Achievements.get(MainActivity.this).hookEndRun(score); }
    }

    private void hideCustomView() {
        if (customView == null) return;
        ((FrameLayout) getWindow().getDecorView()).removeView(customView);
        customView = null;
        webView.setVisibility(View.VISIBLE);
        getWindow().getDecorView().setSystemUiVisibility(oldUiVisibility);
        if (customViewCallback != null) customViewCallback.onCustomViewHidden();
        customViewCallback = null;
    }

    @Override public void onBackPressed() {
        if (customView != null) { hideCustomView(); return; }
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); if (webView != null) webView.saveState(out); }
    @Override protected void onPause() {
        tickHandler.removeCallbacks(ticker);
        if (webView != null) { webView.onPause(); Achievements.get(this).pause(); }
        super.onPause();
    }
    @Override protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            Achievements.get(this).beginSession();
            tickHandler.postDelayed(ticker, 1000);
        }
    }
    @Override protected void onDestroy() {
        Achievements.get(this).listener = null;
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
