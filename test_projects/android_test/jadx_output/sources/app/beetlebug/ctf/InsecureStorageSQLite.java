package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import app.beetlebug.p001db.DatabaseHelper;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* loaded from: classes6.dex */
public class InsecureStorageSQLite extends AppCompatActivity {
    public static String flag_scores = "flag_scores";
    public static String m_name = "name";
    public static String m_password = DatabaseHelper.PASS;
    Button btn;
    LinearLayout lin;
    private DatabaseHelper myHelper;
    EditText name;
    EditText pass;
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_insecure_storage_sql);
        this.lin = (LinearLayout) findViewById(C0572R.id.flagLayout);
        this.btn = (Button) findViewById(C0572R.id.button);
        this.lin.setVisibility(8);
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.preferences = getSharedPreferences("preferences", 0);
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        this.myHelper = databaseHelper;
        databaseHelper.open();
    }

    public void captureFlag(View view) {
        EditText flg = (EditText) findViewById(C0572R.id.flag);
        String result = flg.getText().toString();
        String pref_result = this.preferences.getString("5_sqlite", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (result.equals(text)) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putFloat("ctf_score_sqlite", 6.25f);
            editor.apply();
            Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
            String intent_sqlite_str = Float.toString(6.25f);
            ctf_captured.putExtra("intent_str", intent_sqlite_str);
            startActivity(ctf_captured);
            return;
        }
        flg.setError("Try again");
    }

    public void login(View view) {
        this.name = (EditText) findViewById(C0572R.id.editTextUsername);
        this.pass = (EditText) findViewById(C0572R.id.editTextPassword);
        this.lin.setVisibility(0);
        String flg = getString(C0572R.string.sqlite_string);
        String ps = this.pass.getText().toString();
        if (isValidPassword(ps)) {
            this.myHelper.add(ps, flg);
            Toast.makeText(this, "Password saved", 0).show();
        } else {
            Toast.makeText(this, "Use complex alphanumeric password for Master Key", 0).show();
        }
    }

    private boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
