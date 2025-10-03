package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class VulnerableActivityIntent extends AppCompatActivity {
    public static String ctf_score_intent_redirect = "ctf_score_intent_redirect";
    public static String flag_scores = "flag_scores";
    Button btn_ctf;
    Button btn_flag;
    EditText edt_flag;
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_vulnerable_intent);
        this.btn_ctf = (Button) findViewById(C0572R.id.button);
        this.btn_flag = (Button) findViewById(C0572R.id.buttonFlag);
        this.sharedPreferences = getSharedPreferences(flag_scores, 0);
        this.preferences = getSharedPreferences("preferences", 0);
        this.btn_ctf.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.ctf.VulnerableActivityIntent.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Toast.makeText(VulnerableActivityIntent.this, "Look for exported activity to re-direct intent", 1).show();
            }
        });
    }

    public void captureFlag(View view) {
        EditText m_flag = (EditText) findViewById(C0572R.id.flag);
        String pref_result = this.preferences.getString("6_activity", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (m_flag.getText().toString().equals(text)) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putFloat(ctf_score_intent_redirect, 6.25f);
            editor.apply();
            Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
            String intent_str = Float.toString(6.25f);
            ctf_captured.putExtra("intent_str", intent_str);
            startActivity(ctf_captured);
            return;
        }
        m_flag.setError("Wrong answer");
    }
}
