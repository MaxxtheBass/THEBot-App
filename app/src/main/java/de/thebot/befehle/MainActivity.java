package de.thebot.befehle;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#05080F"));
        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#05080F"));
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if ("file".equals(u.getScheme())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception e) { }
                return true;
            }
        });
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void copy(String text) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("Befehl", text));
            }
            // Normaler Weg: laden und das System fragt nach der Installation
            @JavascriptInterface
            public void install(final String url) {
                new Thread(new Runnable() { public void run() { ladeUndInstalliere(url, false); } }).start();
            }
            // Automatisch: wenn Android es erlaubt, ohne Nachfrage (sonst fragt es normal)
            @JavascriptInterface
            public void installAuto(final String url) {
                new Thread(new Runnable() { public void run() { ladeUndInstalliere(url, true); } }).start();
            }
        }, "Android");
        IntentFilter f = new IntentFilter(AKTION);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(ergebnis, f, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(ergebnis, f);
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }

    private static final String AKTION = "de.thebot.befehle.INSTALL_ERGEBNIS";

    private void js(final String code) {
        runOnUiThread(new Runnable() { public void run() { web.evaluateJavascript(code, null); } });
    }

    // Lädt die neue APK selbst herunter und übergibt sie dem System-Installer. Ab Android 12 geht das ohne Nachfrage, wenn die App den Vorgänger selbst installiert hat.
    private void ladeUndInstalliere(String adresse, boolean leise) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(adresse).openConnection();
            c.setConnectTimeout(15000);
            c.setReadTimeout(30000);
            c.setInstanceFollowRedirects(true);
            if (c.getResponseCode() != 200) throw new Exception("HTTP " + c.getResponseCode());
            long gesamt = c.getContentLengthLong();
            PackageInstaller pi = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams sp = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            // Ab Android 12: Update ohne Nachfrage, wenn das System es erlaubt (sonst kommt die normale Bestätigung)
            if (leise && Build.VERSION.SDK_INT >= 31) sp.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
            int id = pi.createSession(sp);
            PackageInstaller.Session se = pi.openSession(id);
            try {
                InputStream in = c.getInputStream();
                OutputStream out = se.openWrite("THE-Bot.apk", 0, gesamt);
                byte[] buf = new byte[65536];
                long got = 0; int n, letzte = -1;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                    got += n;
                    int p = gesamt > 0 ? (int) (got * 100 / gesamt) : 0;
                    if (p != letzte) { letzte = p; js("window.updStatus&&updStatus('lade'," + p + ")"); }
                }
                se.fsync(out);
                out.close(); in.close();
                js("window.updStatus&&updStatus('install',100)");
                Intent i = new Intent(AKTION).setPackage(getPackageName());
                int fl = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0);
                se.commit(PendingIntent.getBroadcast(this, id, i, fl).getIntentSender());
            } catch (Exception e) { se.abandon(); throw e; }
        } catch (Exception e) {
            js("window.updStatus&&updStatus('fehler',0)");
        }
    }

    private final BroadcastReceiver ergebnis = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent in) {
            int st = in.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
            if (st == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                Intent frage = (Intent) in.getParcelableExtra(Intent.EXTRA_INTENT);
                if (frage != null) { frage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); try { startActivity(frage); } catch (Exception e) { } }
            } else if (st != PackageInstaller.STATUS_SUCCESS) {
                js("window.updStatus&&updStatus('fehler',0)");
            }
        }
    };

    @Override
    protected void onDestroy() {
        try { unregisterReceiver(ergebnis); } catch (Exception e) { }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
