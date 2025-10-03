package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;

/* loaded from: classes6.dex */
public class EmbeddedSecretStrings extends AppCompatActivity {
    Button m_btn;
    EditText pin;
    SharedPreferences sharedPreferences;
    public static String flag_scores = "flag_scores";
    public static String ctf_score_sqlite = "ctf_score_sqlite";

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_embedded_secret_strings);
        this.m_btn = (Button) findViewById(C0572R.id.buttonUnlock);
        this.sharedPreferences = getSharedPreferences(flag_scores, 0);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        this.m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.ctf.EmbeddedSecretStrings.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                EmbeddedSecretStrings embeddedSecretStrings = EmbeddedSecretStrings.this;
                embeddedSecretStrings.pin = (EditText) embeddedSecretStrings.findViewById(C0572R.id.editTextSecretPin);
                String s1 = EmbeddedSecretStrings.this.pin.getText().toString();
                if (s1.equals(EmbeddedSecretStrings.this.getString(C0572R.string.V98bFQrpGkDJ))) {
                    SharedPreferences.Editor editor = EmbeddedSecretStrings.this.sharedPreferences.edit();
                    editor.putFloat("ctf_score_secret_string", 6.25f);
                    editor.apply();
                    Toast.makeText(EmbeddedSecretStrings.this, "Folder Unlocked!", 1).show();
                    Intent secret_intent = new Intent(EmbeddedSecretStrings.this, (Class<?>) FlagCaptured.class);
                    String intent_secret_str = Float.toString(6.25f);
                    secret_intent.putExtra("intent_str", intent_secret_str);
                    EmbeddedSecretStrings.this.startActivity(secret_intent);
                    return;
                }
                if (s1.isEmpty()) {
                    Toast.makeText(EmbeddedSecretStrings.this, "Try again.", 0).show();
                    EmbeddedSecretStrings.this.pin.setError("Input your PIN");
                }
            }
        });
    }
}
