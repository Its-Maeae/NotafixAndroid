package com.notafix.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.concurrent.Executor;

@CapacitorPlugin(name = "Notafix")
public class NotafixPlugin extends Plugin {

    private static final String PREFS = "notafix_security";
    private static final String KEY_LOCK_ENABLED = "lock_enabled";
    private static final String KEY_LOCK_TYPE = "lock_type"; // "biometric" or "password"

    // ── Reload WebView ────────────────────────────────────────────────────

    @PluginMethod
    public void reload(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            getBridge().getWebView().reload();
        });
        call.resolve();
    }

    // ── Security Settings ─────────────────────────────────────────────────

    @PluginMethod
    public void getSecuritySettings(PluginCall call) {
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        JSObject result = new JSObject();
        result.put("lockEnabled", prefs.getBoolean(KEY_LOCK_ENABLED, false));
        result.put("lockType", prefs.getString(KEY_LOCK_TYPE, "biometric"));
        call.resolve(result);
    }

    @PluginMethod
    public void setSecuritySettings(PluginCall call) {
        boolean lockEnabled = call.getBoolean("lockEnabled", false);
        String lockType = call.getString("lockType", "biometric");
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(KEY_LOCK_ENABLED, lockEnabled)
            .putString(KEY_LOCK_TYPE, lockType)
            .apply();
        call.resolve();
    }

    // ── Biometric Check ───────────────────────────────────────────────────

    @PluginMethod
    public void isBiometricAvailable(PluginCall call) {
        BiometricManager manager = BiometricManager.from(getContext());
        int canAuth = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK |
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );
        JSObject result = new JSObject();
        result.put("available", canAuth == BiometricManager.BIOMETRIC_SUCCESS);
        call.resolve(result);
    }

    // ── Biometric / Device-Credential Auth ────────────────────────────────

    @PluginMethod
    public void authenticate(PluginCall call) {
        String lockType = call.getString("lockType", "biometric");
        String title = call.getString("title", "Notafix entsperren");
        String subtitle = call.getString("subtitle", "");

        Executor executor = ContextCompat.getMainExecutor(getContext());

        BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                JSObject ret = new JSObject();
                ret.put("success", true);
                call.resolve(ret);
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED) {
                    call.reject("USER_CANCELLED", "Abgebrochen");
                } else {
                    call.reject("AUTH_ERROR", errString.toString());
                }
            }

            @Override
            public void onAuthenticationFailed() {
                // Don't reject here - Android shows retry automatically
            }
        };

        getActivity().runOnUiThread(() -> {
            BiometricPrompt prompt = new BiometricPrompt(
                (FragmentActivity) getActivity(), executor, callback
            );

            BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle.isEmpty() ? null : subtitle);

            if ("biometric".equals(lockType)) {
                // Fingerabdruck bevorzugt, Geräte-PIN als Fallback
                builder.setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK |
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                );
            } else {
                // Nur PIN/Passwort/Muster
                builder.setAllowedAuthenticators(
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                );
            }

            prompt.authenticate(builder.build());
        });
    }
}
