package app.beetlebug.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.MainActivity;
import app.beetlebug.utils.CustomProgressBar;

/* loaded from: classes4.dex */
public class PlayerStats extends AppCompatActivity {
    TextView flags_captured;
    ImageView m_btn_back;
    SharedPreferences sharedPreferences;
    CustomProgressBar userProgress;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_user_profile);
        this.userProgress = (CustomProgressBar) findViewById(C0572R.id.user_progress_bar);
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.m_btn_back = (ImageView) findViewById(C0572R.id.back);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        this.m_btn_back.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.user.PlayerStats.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent i = new Intent(PlayerStats.this, (Class<?>) MainActivity.class);
                PlayerStats.this.startActivity(i);
            }
        });
        setupProgressBar();
        setUpFlagsCaptured();
    }

    public void setupProgressBar() {
        SharedPreferences sharedPreferences = getSharedPreferences("flag_scores", 0);
        float secret_source_score = sharedPreferences.getFloat("ctf_score_secret_source", 0.0f);
        float secret_string_score = sharedPreferences.getFloat("ctf_score_secret_string", 0.0f);
        float shared_pref_score = sharedPreferences.getFloat("ctf_score_shared_pref", 0.0f);
        float sqlite_score = sharedPreferences.getFloat("ctf_score_sqlite", 0.0f);
        float external_str_score = sharedPreferences.getFloat("ctf_score_external", 0.0f);
        float xss_score = sharedPreferences.getFloat("ctf_score_xss", 0.0f);
        float webview_score = sharedPreferences.getFloat("ctf_score_webview", 0.0f);
        float intent_redirect_score = sharedPreferences.getFloat("ctf_score_intent_redirect", 0.0f);
        float service_score = sharedPreferences.getFloat("ctf_score_service", 0.0f);
        float content_score = sharedPreferences.getFloat("ctf_score_content_provider", 0.0f);
        float auth_score = sharedPreferences.getFloat("ctf_score_auth", 0.0f);
        float clip_score = sharedPreferences.getFloat("ctf_score_clip", 0.0f);
        float log_score = sharedPreferences.getFloat("ctf_score_log", 0.0f);
        float firebase_score = sharedPreferences.getFloat("ctf_score_firebase", 0.0f);
        float sqli_score = sharedPreferences.getFloat("ctf_score_sqli", 0.0f);
        float patch_score = sharedPreferences.getFloat("ctf_score_patch", 0.0f);
        float total_score = sqlite_score + shared_pref_score + secret_source_score + secret_string_score + external_str_score + firebase_score + sqli_score + intent_redirect_score + service_score + log_score + xss_score + content_score + patch_score + clip_score + auth_score + webview_score;
        this.userProgress.setProgressWithAnimation(total_score, 2000);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public void setUpFlagsCaptured() {
        char c;
        SharedPreferences sharedPreferences = getSharedPreferences("flag_scores", 0);
        float secret_source_score = sharedPreferences.getFloat("ctf_score_secret_source", 0.0f);
        float secret_string_score = sharedPreferences.getFloat("ctf_score_secret_string", 0.0f);
        float shared_pref_score = sharedPreferences.getFloat("ctf_score_shared_pref", 0.0f);
        float sqlite_score = sharedPreferences.getFloat("ctf_score_sqlite", 0.0f);
        float external_str_score = sharedPreferences.getFloat("ctf_score_external", 0.0f);
        float xss_score = sharedPreferences.getFloat("ctf_score_xss", 0.0f);
        float webview_score = sharedPreferences.getFloat("ctf_score_webview", 0.0f);
        float intent_redirect_score = sharedPreferences.getFloat("ctf_score_intent_redirect", 0.0f);
        float service_score = sharedPreferences.getFloat("ctf_score_service", 0.0f);
        float content_score = sharedPreferences.getFloat("ctf_score_content_provider", 0.0f);
        float auth_score = sharedPreferences.getFloat("ctf_score_auth", 0.0f);
        float clip_score = sharedPreferences.getFloat("ctf_score_clip", 0.0f);
        float log_score = sharedPreferences.getFloat("ctf_score_log", 0.0f);
        float firebase_score = sharedPreferences.getFloat("ctf_score_firebase", 0.0f);
        float sqli_score = sharedPreferences.getFloat("ctf_score_sqli", 0.0f);
        float patch_score = sharedPreferences.getFloat("ctf_score_patch", 0.0f);
        float total_score = sqlite_score + shared_pref_score + secret_source_score + secret_string_score + external_str_score + firebase_score + sqli_score + intent_redirect_score + service_score + log_score + xss_score + content_score + patch_score + clip_score + auth_score + webview_score;
        String stringF = Float.toString(total_score);
        this.flags_captured = (TextView) findViewById(C0572R.id.flag_score);
        switch (stringF.hashCode()) {
            case 1509288:
                if (stringF.equals("12.5")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1541957:
                if (stringF.equals("25.0")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1573675:
                if (stringF.equals("37.5")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 1626525:
                if (stringF.equals("50.0")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1654523:
                if (stringF.equals("6.25")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1658243:
                if (stringF.equals("62.5")) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case 1690912:
                if (stringF.equals("75.0")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1722630:
                if (stringF.equals("87.5")) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 46730099:
                if (stringF.equals("100.0")) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 46966789:
                if (stringF.equals("18.75")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 48605139:
                if (stringF.equals("31.25")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 49588397:
                if (stringF.equals("43.75")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 50601136:
                if (stringF.equals("56.25")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 51584394:
                if (stringF.equals("68.75")) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 53222744:
                if (stringF.equals("81.25")) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 54206002:
                if (stringF.equals("93.75")) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                this.flags_captured.setText("1");
                return;
            case 1:
                this.flags_captured.setText("2");
                return;
            case 2:
                this.flags_captured.setText("3");
                return;
            case 3:
                this.flags_captured.setText("4");
                return;
            case 4:
                this.flags_captured.setText("5");
                return;
            case 5:
                this.flags_captured.setText("6");
                return;
            case 6:
                this.flags_captured.setText("7");
                return;
            case 7:
                this.flags_captured.setText("8");
                return;
            case '\b':
                this.flags_captured.setText("9");
                return;
            case '\t':
                this.flags_captured.setText("10");
                return;
            case '\n':
                this.flags_captured.setText("11");
                return;
            case 11:
                this.flags_captured.setText("12");
                return;
            case '\f':
                this.flags_captured.setText("13");
                return;
            case '\r':
                this.flags_captured.setText("14");
                return;
            case 14:
                this.flags_captured.setText("15");
                return;
            case 15:
                this.flags_captured.setText("16");
                return;
            default:
                this.flags_captured.setText("0");
                return;
        }
    }
}
