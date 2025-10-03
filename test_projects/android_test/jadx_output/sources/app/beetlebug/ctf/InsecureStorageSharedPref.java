package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import app.beetlebug.p001db.DatabaseHelper;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class InsecureStorageSharedPref extends AppCompatActivity {
    LinearLayout ctf_layout;
    EditText et_password;
    EditText et_username;
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreferences_pref;
    public static String flag_scores = "flag_scores";
    public static String m_username = "username";
    public static String m_password = DatabaseHelper.PASS;
    public static String ctf_score_shared_pref = "ctf_score_shared_pref";
    public static String shared_pref_flag = "shared_pref_flag";

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_insecure_storage_sharedpref);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        this.sharedPreferences_pref = getSharedPreferences(shared_pref_flag, 0);
        this.sharedPreferences = getSharedPreferences(flag_scores, 0);
        this.preferences = getSharedPreferences("preferences", 0);
        LinearLayout linearLayout = (LinearLayout) findViewById(C0572R.id.layoutCtf);
        this.ctf_layout = linearLayout;
        linearLayout.setVisibility(8);
    }

    public void saveUser(View view) {
        this.et_username = (EditText) findViewById(C0572R.id.editTextUsername);
        this.et_password = (EditText) findViewById(C0572R.id.editTextPassword);
        String username = this.et_username.getText().toString();
        String password = this.et_password.getText().toString();
        this.ctf_layout.setVisibility(0);
        if (username.isEmpty()) {
            Toast.makeText(this, "Input your username", 0).show();
            this.et_username.setError("Username cannot be blank");
        }
        if (password.isEmpty()) {
            this.et_password.setError("Password field is empty");
            return;
        }
        SharedPreferences.Editor editor = this.sharedPreferences_pref.edit();
        editor.putString(m_username, username);
        editor.putString(m_password, password);
        editor.putString("flag 3", "0x1442c04");
        editor.clear();
        editor.apply();
        Toast.makeText(this, "Login successful", 0).show();
    }

    public void captureFlag(View view) {
        EditText m_flag = (EditText) findViewById(C0572R.id.flag);
        String pref_result = this.preferences.getString("3_pref", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (m_flag.getText().toString().equals(text)) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putFloat(ctf_score_shared_pref, 6.25f);
            editor.commit();
            Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
            String intent_pref_str = Float.toString(6.25f);
            ctf_captured.putExtra("intent_str", intent_pref_str);
            startActivity(ctf_captured);
            return;
        }
        m_flag.setError("Try again");
    }
}
