package com.notafix.app;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private static final long LOCK_TIMEOUT_MS = 2 * 60 * 1000; // 2 Minuten
    private long pausedAt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(NotafixPlugin.class);
        super.onCreate(savedInstanceState);

        // Cookies persistent machen (überleben App-Neustart)
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(getBridge().getWebView(), true);
    }

    @Override
    public void onPause() {
        super.onPause();
        pausedAt = System.currentTimeMillis();
        // Cookies sofort auf Disk schreiben
        CookieManager.getInstance().flush();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pausedAt > 0) {
            long elapsed = System.currentTimeMillis() - pausedAt;
            if (elapsed > LOCK_TIMEOUT_MS) {
                // Timeout abgelaufen → JavaScript informieren
                getBridge().getWebView().post(() ->
                    getBridge().eval("window.__notafixLockTimeout && window.__notafixLockTimeout()", null)
                );
            }
        }
        pausedAt = 0;
    }

    @Override
    public void onStop() {
        super.onStop();
        CookieManager.getInstance().flush();
    }
}
