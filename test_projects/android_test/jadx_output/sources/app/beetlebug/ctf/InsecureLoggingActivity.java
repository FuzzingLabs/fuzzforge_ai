package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class InsecureLoggingActivity extends AppCompatActivity {
    EditText flg;
    boolean isAllFieldsChecked = false;
    EditText m_card_number;
    EditText m_cvv;
    EditText m_expires;
    Button m_flg;
    Button m_pay;
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;
    public static String flag_scores = "flag_scores";
    public static String ctf_score_log = "ctf_score_log";

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_insecure_logging);
        this.m_pay = (Button) findViewById(C0572R.id.buttonPay);
        this.m_card_number = (EditText) findViewById(C0572R.id.editTextCardNumber);
        this.m_expires = (EditText) findViewById(C0572R.id.editTextExpires);
        this.m_cvv = (EditText) findViewById(C0572R.id.editTextCvv);
        this.m_pay = (Button) findViewById(C0572R.id.buttonPay);
        this.m_flg = (Button) findViewById(C0572R.id.button);
        final LinearLayout lin = (LinearLayout) findViewById(C0572R.id.layoutCtf);
        lin.setVisibility(8);
        this.sharedPreferences = getSharedPreferences(flag_scores, 0);
        this.preferences = getSharedPreferences("preferences", 0);
        this.m_pay.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.ctf.InsecureLoggingActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                InsecureLoggingActivity insecureLoggingActivity = InsecureLoggingActivity.this;
                insecureLoggingActivity.isAllFieldsChecked = insecureLoggingActivity.CheckAllFields();
                lin.setVisibility(0);
                if (InsecureLoggingActivity.this.isAllFieldsChecked) {
                    String card = InsecureLoggingActivity.this.m_card_number.getText().toString();
                    InsecureLoggingActivity.this.m_card_number.setError("An Error Occurred");
                    Log.e("beetle-log", "Transaction Failed: " + card + "\nflg: " + InsecureLoggingActivity.this.getString(C0572R.string._0x532123));
                }
            }
        });
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
    }

    public void captureFlag(View view) {
        EditText editText = (EditText) findViewById(C0572R.id.flag);
        this.flg = editText;
        String result = editText.getText().toString();
        String pref_result = this.preferences.getString("9_log", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (result.equals(text)) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putFloat(ctf_score_log, 6.25f);
            editor.commit();
            Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
            String intent_pref_str = Float.toString(6.25f);
            ctf_captured.putExtra("intent_str", intent_pref_str);
            startActivity(ctf_captured);
            return;
        }
        if (result.isEmpty()) {
            this.flg.setError("Try again");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean CheckAllFields() {
        if (this.m_card_number.length() == 0) {
            this.m_card_number.setError("This field is required");
            return false;
        }
        if (this.m_expires.length() == 0) {
            this.m_expires.setError("This field is required");
            return false;
        }
        if (this.m_expires.length() == 0) {
            this.m_expires.setError("This field is required");
            return false;
        }
        if (this.m_cvv.length() == 0) {
            this.m_cvv.setError("This field is required");
            return false;
        }
        return true;
    }
}
