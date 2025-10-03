package app.beetlebug;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.utils.CustomProgressBar;

/* loaded from: classes4.dex */
public class FlagCaptured extends AppCompatActivity {
    CustomProgressBar dailyProgressBar;
    TextView m_total_ctf_points;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_flag_captured);
        this.m_total_ctf_points = (TextView) findViewById(C0572R.id.totalCTFPoints);
        this.dailyProgressBar = (CustomProgressBar) findViewById(C0572R.id.user_progress_bar);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.default_light_grey_bg));
        }
        beetlePoints();
        setupProgressBar();
    }

    public void setupProgressBar() {
        SharedPreferences sharedPreferences = getSharedPreferences("flag_scores", 0);
        float sqlite_score = sharedPreferences.getFloat("ctf_score_sqlite", 0.0f);
        float shared_pref_score = sharedPreferences.getFloat("ctf_score_shared_pref", 0.0f);
        float secret_source_score = sharedPreferences.getFloat("ctf_score_secret_source", 0.0f);
        float secret_string_score = sharedPreferences.getFloat("ctf_score_secret_string", 0.0f);
        float external_str_score = sharedPreferences.getFloat("ctf_score_external", 0.0f);
        float firebase_score = sharedPreferences.getFloat("ctf_score_firebase", 0.0f);
        float sqli_score = sharedPreferences.getFloat("ctf_score_sqli", 0.0f);
        float intent_redirect_score = sharedPreferences.getFloat("ctf_score_intent_redirect", 0.0f);
        float service_score = sharedPreferences.getFloat("ctf_score_service", 0.0f);
        float log_score = sharedPreferences.getFloat("ctf_score_log", 0.0f);
        float xss_score = sharedPreferences.getFloat("ctf_score_xss", 0.0f);
        float content_score = sharedPreferences.getFloat("ctf_score_content_provider", 0.0f);
        float patch_score = sharedPreferences.getFloat("ctf_score_patch", 0.0f);
        float clip_score = sharedPreferences.getFloat("ctf_score_clip", 0.0f);
        float auth_score = sharedPreferences.getFloat("ctf_score_auth", 0.0f);
        float webview_score = sharedPreferences.getFloat("ctf_score_webview", 0.0f);
        float total_score = sqlite_score + shared_pref_score + secret_source_score + secret_string_score + external_str_score + firebase_score + sqli_score + intent_redirect_score + service_score + log_score + xss_score + content_score + patch_score + clip_score + auth_score + webview_score;
        this.m_total_ctf_points.setText(total_score + "XP");
        this.dailyProgressBar.setProgressWithAnimation(total_score, 2000);
    }

    public void continueCTF(View view) {
        Intent i = new Intent(this, (Class<?>) MainActivity.class);
        startActivity(i);
    }

    private void beetlePoints() {
        Intent i = getIntent();
        if (i.getStringExtra("intent_str").equals("6.25")) {
            TextView ctf_point = (TextView) findViewById(C0572R.id.ctfPoint);
            ctf_point.setText("6.25");
        }
    }
}
