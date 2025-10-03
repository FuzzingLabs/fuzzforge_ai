package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

/* loaded from: classes6.dex */
public class BiometricActivityDeeplink extends AppCompatActivity {
    private BiometricPrompt biometricPrompt;
    private Executor executor;
    private ImageView mImageView;
    private SharedPreferences preferences;
    private BiometricPrompt.PromptInfo promptInfo;
    private SharedPreferences sharedPreferences;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_biometric);
        this.mImageView = (ImageView) findViewById(C0572R.id.fingerPrintImageView);
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.preferences = getSharedPreferences("preferences", 0);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        this.executor = ContextCompat.getMainExecutor(this);
        this.biometricPrompt = new BiometricPrompt(this, this.executor, new BiometricPrompt.AuthenticationCallback() { // from class: app.beetlebug.ctf.BiometricActivityDeeplink.1
            @Override // androidx.biometric.BiometricPrompt.AuthenticationCallback
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override // androidx.biometric.BiometricPrompt.AuthenticationCallback
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(BiometricActivityDeeplink.this, "Exploit deeplinks to bypass login", 1).show();
            }

            @Override // androidx.biometric.BiometricPrompt.AuthenticationCallback
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        this.promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Biometric Authentication").setSubtitle("Login using Fingerprint or Face").setNegativeButtonText("Cancel").build();
        this.mImageView.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.ctf.BiometricActivityDeeplink.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                BiometricActivityDeeplink.this.biometricPrompt.authenticate(BiometricActivityDeeplink.this.promptInfo);
            }
        });
    }

    public void signIn(View view) {
        EditText sign = (EditText) findViewById(C0572R.id.editTextPassword);
        sign.setError("Wrong password");
    }

    public void flg(View view) {
        EditText m_flg = (EditText) findViewById(C0572R.id.flag);
        String result = m_flg.getText().toString();
        String pref_result = this.preferences.getString("15_fingerprint", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (result.equals(text)) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putFloat("ctf_score_auth", 6.25f);
            editor.apply();
            Intent secret_intent = new Intent(this, (Class<?>) FlagCaptured.class);
            String intent_auth_str = Float.toString(6.25f);
            secret_intent.putExtra("intent_str", intent_auth_str);
            startActivity(secret_intent);
            return;
        }
        if (result.isEmpty()) {
            m_flg.setError("Enter flag");
        }
    }
}
