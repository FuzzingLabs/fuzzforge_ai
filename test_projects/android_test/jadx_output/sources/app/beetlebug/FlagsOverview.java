package app.beetlebug;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import app.beetlebug.ctf.BinaryPatchActivity;
import app.beetlebug.ctf.VulnerableActivityIntent;
import app.beetlebug.fragments.AndroidComponentsFragment;
import app.beetlebug.fragments.BiometricFragment;
import app.beetlebug.fragments.DatabasesFragment;
import app.beetlebug.fragments.InsecureStorageFragment;
import app.beetlebug.fragments.SecretsFragment;
import app.beetlebug.fragments.SensitiveDataFragment;
import app.beetlebug.fragments.WebViewFragment;
import app.beetlebug.utils.CustomProgressBar;

/* loaded from: classes4.dex */
public class FlagsOverview extends AppCompatActivity {
    CustomProgressBar androidComponentsProgressBar;
    CustomProgressBar bioProgressBar;
    TextView ctf_player;
    CustomProgressBar databasesProgressBar;
    TextView flags_captured;
    CustomProgressBar hardcodedSecretsProgressBar;
    CustomProgressBar insecureStoreProgressBar;
    ImageView mBackButton;
    ScrollView mScrollView;
    RelativeLayout mToolbar;
    Button mfinish;
    CustomProgressBar patchDetectionProgressBar;
    CustomProgressBar sensitiveInfoProgressBar;
    SharedPreferences sharedPreferences;
    SharedPreferences userPref;
    CustomProgressBar webViewsProgressBar;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_flags_overview);
        this.mBackButton = (ImageView) findViewById(C0572R.id.back);
        this.mScrollView = (ScrollView) findViewById(C0572R.id.scrollview_flags);
        this.mToolbar = (RelativeLayout) findViewById(C0572R.id.toolbar);
        this.hardcodedSecretsProgressBar = (CustomProgressBar) findViewById(C0572R.id.hardcoded_secrets_bar);
        this.webViewsProgressBar = (CustomProgressBar) findViewById(C0572R.id.progress_bar_webview);
        this.insecureStoreProgressBar = (CustomProgressBar) findViewById(C0572R.id.progress_bar_local_storage);
        this.androidComponentsProgressBar = (CustomProgressBar) findViewById(C0572R.id.progress_bar_components);
        this.sensitiveInfoProgressBar = (CustomProgressBar) findViewById(C0572R.id.progress_bar_sensitive_info);
        this.patchDetectionProgressBar = (CustomProgressBar) findViewById(C0572R.id.progress_bar_binary);
        this.databasesProgressBar = (CustomProgressBar) findViewById(C0572R.id.progress_bar_database);
        this.webViewsProgressBar = (CustomProgressBar) findViewById(C0572R.id.progress_bar_webview);
        this.bioProgressBar = (CustomProgressBar) findViewById(C0572R.id.progress_bar_bio);
        this.mfinish = (Button) findViewById(C0572R.id.finish_button);
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        this.mBackButton.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.FlagsOverview.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                Intent home_intent = new Intent(FlagsOverview.this, (Class<?>) MainActivity.class);
                FlagsOverview.this.startActivity(home_intent);
            }
        });
        setUpFlagsCaptured();
        setupProgressBarInsecureStorage();
        setupProgressBarHardCodedSecrets();
        setUpProgressBarAndroidComponents();
        setUpProgressBarInfoDiscl();
        setUpProgressBarDatabases();
        setUpProgressBarWebViews();
        setUpProgressBarPatch();
        setUpProgressBarAuth();
    }

    private void setUpProgressBarAuth() {
        float auth_score = this.sharedPreferences.getFloat("ctf_score_auth", 0.0f);
        String total_string = Float.toString(auth_score);
        if (total_string.equals("6.25")) {
            this.bioProgressBar.setProgressWithAnimation(100.0f, 2000);
        }
    }

    private void setUpProgressBarAndroidComponents() {
        float service_score = this.sharedPreferences.getFloat("ctf_score_service", 0.0f);
        float content_score = this.sharedPreferences.getFloat("ctf_score_content_provider", 0.0f);
        float intent_redirect_score = this.sharedPreferences.getFloat("ctf_score_intent_redirect", 0.0f);
        float total_score = service_score + content_score + intent_redirect_score;
        String total_string = Float.toString(total_score);
        if (total_string.equals("6.25")) {
            this.androidComponentsProgressBar.setProgressWithAnimation(33.0f, 2000);
        } else if (total_string.equals("12.5")) {
            this.androidComponentsProgressBar.setProgressWithAnimation(66.0f, 2000);
        } else if (total_string.equals("18.75")) {
            this.androidComponentsProgressBar.setProgressWithAnimation(100.0f, 2000);
        }
    }

    private void setUpProgressBarWebViews() {
        float xss_score = this.sharedPreferences.getFloat("ctf_score_xss", 0.0f);
        float webview_score = this.sharedPreferences.getFloat("ctf_score_webview", 0.0f);
        float total_score = xss_score + webview_score;
        String total_string = Float.toString(total_score);
        if (total_string.equals("6.25")) {
            this.webViewsProgressBar.setProgressWithAnimation(50.0f, 2000);
        } else if (total_string.equals("12.5")) {
            this.webViewsProgressBar.setProgressWithAnimation(100.0f, 2000);
        }
    }

    private void setUpProgressBarInfoDiscl() {
        float log_score = this.sharedPreferences.getFloat("ctf_score_log", 0.0f);
        float clip_score = this.sharedPreferences.getFloat("ctf_score_clip", 0.0f);
        float total_score = log_score + clip_score;
        String total_string = Float.toString(total_score);
        if (total_string.equals("6.25")) {
            this.sensitiveInfoProgressBar.setProgressWithAnimation(50.0f, 2000);
        } else if (total_string.equals("12.5")) {
            this.sensitiveInfoProgressBar.setProgressWithAnimation(100.0f, 2000);
        }
    }

    private void setUpProgressBarDatabases() {
        float firebase_score = this.sharedPreferences.getFloat("ctf_score_firebase", 0.0f);
        float sqli_score = this.sharedPreferences.getFloat("ctf_score_sqli", 0.0f);
        float total_score = firebase_score + sqli_score;
        String total_string = Float.toString(total_score);
        if (total_string.equals("6.25")) {
            this.databasesProgressBar.setProgressWithAnimation(50.0f, 2000);
        } else if (total_string.equals("12.5")) {
            this.databasesProgressBar.setProgressWithAnimation(100.0f, 2000);
        }
    }

    private void setUpProgressBarPatch() {
        float root_score = this.sharedPreferences.getFloat("ctf_score_patch", 0.0f);
        String score = Float.toString(root_score);
        if (score.equals("6.25")) {
            this.patchDetectionProgressBar.setProgressWithAnimation(100.0f, 2000);
        }
    }

    public void inSecureStorage(View v) {
        this.mScrollView.setVisibility(8);
        this.mToolbar.setVisibility(8);
        Fragment fragment = new InsecureStorageFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void biometricAuth(View v) {
        this.mScrollView.setVisibility(8);
        this.mToolbar.setVisibility(8);
        Fragment fragment = new BiometricFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void webViewFlag(View v) {
        this.mScrollView.setVisibility(8);
        this.mToolbar.setVisibility(8);
        Fragment fragment = new WebViewFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void inSecureActivity(View v) {
        Intent i = new Intent(this, (Class<?>) VulnerableActivityIntent.class);
        startActivity(i);
    }

    public void AndroidComponents(View view) {
        this.mScrollView.setVisibility(8);
        this.mToolbar.setVisibility(8);
        Fragment fragment = new AndroidComponentsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void databaseFlag(View view) {
        this.mScrollView.setVisibility(8);
        this.mToolbar.setVisibility(8);
        Fragment fragment = new DatabasesFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    private void setupProgressBarHardCodedSecrets() {
        float secret_string_score = this.sharedPreferences.getFloat("ctf_score_secret_string", 0.0f);
        float secret_source_score = this.sharedPreferences.getFloat("ctf_score_secret_source", 0.0f);
        float total_score = secret_source_score + secret_string_score;
        String total_string = Float.toString(total_score);
        if (total_string.equals("6.25")) {
            this.hardcodedSecretsProgressBar.setProgressWithAnimation(50.0f, 2000);
        } else if (total_string.equals("12.5")) {
            this.hardcodedSecretsProgressBar.setProgressWithAnimation(100.0f, 2000);
        }
    }

    private void setupProgressBarInsecureStorage() {
        float external_str_score = this.sharedPreferences.getFloat("ctf_score_external", 0.0f);
        float shared_pref_score = this.sharedPreferences.getFloat("ctf_score_shared_pref", 0.0f);
        float sqlite_score = this.sharedPreferences.getFloat("ctf_score_sqlite", 0.0f);
        float total_score = external_str_score + shared_pref_score + sqlite_score;
        String total_string = Float.toString(total_score);
        if (total_string.equals("6.25")) {
            this.insecureStoreProgressBar.setProgressWithAnimation(33.0f, 2000);
        } else if (total_string.equals("12.5")) {
            this.insecureStoreProgressBar.setProgressWithAnimation(66.0f, 2000);
        } else if (total_string.equals("18.75")) {
            this.insecureStoreProgressBar.setProgressWithAnimation(100.0f, 2000);
        }
    }

    public void inSecureLoggingFlag(View view) {
        this.mScrollView.setVisibility(8);
        this.mToolbar.setVisibility(8);
        Fragment fragment = new SensitiveDataFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void patchBinary(View view) {
        Intent i = new Intent(this, (Class<?>) BinaryPatchActivity.class);
        startActivity(i);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public void submitFlags(View view) {
        char c;
        Dialog finish_dialog = new Dialog(this);
        finish_dialog.requestWindowFeature(1);
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
        String num = Float.toString(total_score);
        switch (num.hashCode()) {
            case 1509288:
                if (num.equals("12.5")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 1541957:
                if (num.equals("25.0")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1573675:
                if (num.equals("37.5")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 1654523:
                if (num.equals("6.25")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 46730099:
                if (num.equals("100.0")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 48605139:
                if (num.equals("31.25")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 49588397:
                if (num.equals("43.75")) {
                    c = 5;
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
                finish_dialog.setContentView(C0572R.layout.try_again_sheet);
                finish_dialog.show();
                finish_dialog.getWindow().setLayout(-1, -2);
                finish_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                finish_dialog.getWindow().setGravity(80);
                return;
            case 1:
                finish_dialog.setContentView(C0572R.layout.try_again_sheet);
                finish_dialog.show();
                finish_dialog.getWindow().setLayout(-1, -2);
                finish_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                finish_dialog.getWindow().setGravity(80);
                return;
            case 2:
                finish_dialog.setContentView(C0572R.layout.try_again_sheet);
                finish_dialog.show();
                finish_dialog.getWindow().setLayout(-1, -2);
                finish_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                finish_dialog.getWindow().setGravity(80);
                return;
            case 3:
                finish_dialog.setContentView(C0572R.layout.bottom_sheet_continue);
                finish_dialog.show();
                finish_dialog.getWindow().setLayout(-1, -2);
                finish_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                finish_dialog.getWindow().setGravity(80);
                return;
            case 4:
                finish_dialog.setContentView(C0572R.layout.bottom_sheet_continue);
                finish_dialog.show();
                finish_dialog.getWindow().setLayout(-1, -2);
                finish_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                finish_dialog.getWindow().setGravity(80);
                return;
            case 5:
                finish_dialog.setContentView(C0572R.layout.bottom_sheet_continue);
                finish_dialog.show();
                finish_dialog.getWindow().setLayout(-1, -2);
                finish_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                finish_dialog.getWindow().setGravity(80);
                return;
            case 6:
                finish_dialog.setContentView(C0572R.layout.bottom_sheet);
                finish_dialog.show();
                finish_dialog.getWindow().setLayout(-1, -2);
                finish_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                finish_dialog.getWindow().setGravity(80);
                return;
            default:
                finish_dialog.setContentView(C0572R.layout.try_again_sheet);
                finish_dialog.show();
                finish_dialog.getWindow().setLayout(-1, -2);
                finish_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                finish_dialog.getWindow().setGravity(80);
                return;
        }
    }

    public void shareResult(View view) {
        ShareCompat.IntentBuilder.from(this).setType("text/plain").setText("Yayy! I have captured all the flags on @BeetlebugCTF https://github.com/hafiz-ng/beetlebug").startChooser();
    }

    public void embeddedSecrets(View view) {
        this.mScrollView.setVisibility(8);
        this.mToolbar.setVisibility(8);
        Fragment fragment = new SecretsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
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

    public void continueFlag(View view) {
        Intent continue_ctf = new Intent(this, (Class<?>) FlagsOverview.class);
        startActivity(continue_ctf);
    }

    public void continueUser(View view) {
        Intent continue_ctf = new Intent(this, (Class<?>) FlagsOverview.class);
        startActivity(continue_ctf);
    }
}
