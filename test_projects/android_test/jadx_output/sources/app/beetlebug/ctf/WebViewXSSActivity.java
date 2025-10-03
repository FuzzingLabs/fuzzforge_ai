package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class WebViewXSSActivity extends AppCompatActivity {
    public static final String PACKAGE_STRING = "app.beetlebug.ctf.WebViewXSSActivity";
    LinearLayout lin;
    EditText m_flag;
    public WebView myWebView;
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;
    TextView url2;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_web_view_xssactivity);
        this.m_flag = (EditText) findViewById(C0572R.id.flag);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.preferences = getSharedPreferences("preferences", 0);
    }

    public void submitResult(View view) {
        EditText editText = (EditText) findViewById(C0572R.id.editTextUrl);
        String post = editText.getText().toString();
        if (post.isEmpty()) {
            editText.setError("Field is empty");
            return;
        }
        Intent intent = new Intent(this, (Class<?>) DisplayXSS.class);
        intent.putExtra(PACKAGE_STRING, post);
        startActivity(intent);
    }

    public void captureFlag(View view) {
        EditText m_flag = (EditText) findViewById(C0572R.id.flag);
        String pref_result = this.preferences.getString("13_xss", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (!m_flag.getText().toString().equals(text)) {
            Toast.makeText(this, "Wrong answer", 0).show();
            return;
        }
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putFloat("ctf_score_xss", 6.25f);
        editor.apply();
        Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
        String intent_xss_str = Float.toString(6.25f);
        ctf_captured.putExtra("intent_str", intent_xss_str);
        startActivity(ctf_captured);
    }
}
