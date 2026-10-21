package com.ashuni.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Api {
    public interface Callback { void done(JSONObject json, String error, int status); }

    private static final Handler UI = new Handler(Looper.getMainLooper());

    public static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences("ashuni_social", Context.MODE_PRIVATE);
    }
    public static String token(Context c) { return prefs(c).getString("token", ""); }

    /** Appel HTTP asynchrone ; le callback (peut être null) est exécuté sur le thread principal. */
    public static void call(Context ctx, final String method, final String path, final JSONObject body, final Callback cb) {
        final String token = token(ctx);
        new Thread(new Runnable() {
            @Override public void run() {
                JSONObject json = null;
                String error = null;
                int status = 0;
                HttpURLConnection con = null;
                try {
                    con = (HttpURLConnection) new URL(Config.API_URL + path).openConnection();
                    con.setRequestMethod(method);
                    con.setConnectTimeout(10000);
                    con.setReadTimeout(10000);
                    con.setRequestProperty("Content-Type", "application/json");
                    if (!token.isEmpty()) con.setRequestProperty("Authorization", "Bearer " + token);
                    if (body != null) {
                        con.setDoOutput(true);
                        OutputStream os = con.getOutputStream();
                        os.write(body.toString().getBytes("UTF-8"));
                        os.close();
                    }
                    status = con.getResponseCode();
                    InputStream is = status >= 400 ? con.getErrorStream() : con.getInputStream();
                    String text = read(is);
                    try { json = new JSONObject(text); } catch (Exception ignored) {}
                    if (status >= 400) error = json != null ? json.optString("error", "Erreur " + status) : "Erreur " + status;
                } catch (Exception e) {
                    error = "Serveur injoignable. Vérifie ta connexion.";
                } finally {
                    if (con != null) con.disconnect();
                }
                final JSONObject fj = json; final String fe = error; final int fs = status;
                if (cb != null) UI.post(new Runnable() { @Override public void run() { cb.done(fj, fe, fs); } });
            }
        }).start();
    }

    private static String read(InputStream is) throws Exception {
        if (is == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
        is.close();
        return out.toString("UTF-8");
    }
}
