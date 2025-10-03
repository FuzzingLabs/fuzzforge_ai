package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class FirebaseDatabaseActivity extends AppCompatActivity {
    public Button mBtn;
    EditText m_flg;
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_firebase_database);
        this.mBtn = (Button) findViewById(C0572R.id.button);
        this.m_flg = (EditText) findViewById(C0572R.id.flag);
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.preferences = getSharedPreferences("preferences", 0);
        this.mBtn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.ctf.FirebaseDatabaseActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                EditText flg = (EditText) FirebaseDatabaseActivity.this.findViewById(C0572R.id.flag);
                String result = flg.getText().toString();
                String pref_result = FirebaseDatabaseActivity.this.preferences.getString("11_firebase", "");
                byte[] data = Base64.decode(pref_result, 0);
                String text = new String(data, StandardCharsets.UTF_8);
                if (result.equals(text)) {
                    SharedPreferences.Editor editor = FirebaseDatabaseActivity.this.sharedPreferences.edit();
                    editor.putFloat("ctf_score_firebase", 6.25f);
                    editor.apply();
                    Intent ctf_captured = new Intent(FirebaseDatabaseActivity.this, (Class<?>) FlagCaptured.class);
                    String intent_firebase_str = Float.toString(6.25f);
                    ctf_captured.putExtra("intent_str", intent_firebase_str);
                    FirebaseDatabaseActivity.this.startActivity(ctf_captured);
                    return;
                }
                FirebaseDatabaseActivity.this.m_flg.setError("Wrong answer");
            }
        });
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
    }
}
