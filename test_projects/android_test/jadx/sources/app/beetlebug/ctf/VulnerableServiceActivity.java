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
import app.beetlebug.handlers.VulnerableService;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class VulnerableServiceActivity extends AppCompatActivity implements View.OnClickListener {
    public static String ctf_score_service = "ctf_score_service";
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;
    private Button start;
    private Button stop;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_vulnerable_service);
        this.start = (Button) findViewById(C0572R.id.buttonStart);
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.preferences = getSharedPreferences("preferences", 0);
        this.stop = (Button) findViewById(C0572R.id.buttonStop);
        this.start.setOnClickListener(this);
        this.stop.setOnClickListener(this);
        this.start.setVisibility(8);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.start) {
            startService(new Intent(this, (Class<?>) VulnerableService.class));
        } else if (view == this.stop) {
            stopService(new Intent(this, (Class<?>) VulnerableService.class));
        }
    }

    public void captureFlag(View view) {
        EditText m_flag = (EditText) findViewById(C0572R.id.flag);
        String result = m_flag.getText().toString();
        String pref_result = this.preferences.getString("8_service", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (result.equals(text)) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putFloat(ctf_score_service, 6.25f);
            editor.apply();
            Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
            String intent_xss_str = Float.toString(6.25f);
            ctf_captured.putExtra("intent_str", intent_xss_str);
            startActivity(ctf_captured);
            return;
        }
        m_flag.setError("Wrong answer");
    }
}
