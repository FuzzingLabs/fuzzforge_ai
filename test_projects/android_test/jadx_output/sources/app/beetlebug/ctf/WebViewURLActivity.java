package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class WebViewURLActivity extends AppCompatActivity {
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_web_view_urlactivity);
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.preferences = getSharedPreferences("preferences", 0);
    }

    public void captureFlag(View view) {
        EditText m_flag = (EditText) findViewById(C0572R.id.flag);
        String pref_result = this.preferences.getString("12_url", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (m_flag.getText().toString().equals(text)) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putFloat("ctf_score_webview", 6.25f);
            editor.apply();
            Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
            String intent_str_url = Float.toString(6.25f);
            ctf_captured.putExtra("intent_str", intent_str_url);
            startActivity(ctf_captured);
            return;
        }
        m_flag.setError("Wrong answer");
    }
}
