package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class SQLInjectionActivity extends AppCompatActivity {
    Button btn;
    EditText flg;
    private SQLiteDatabase mDB;
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_sqlinjection);
        this.flg = (EditText) findViewById(C0572R.id.flag);
        this.btn = (Button) findViewById(C0572R.id.button);
        this.flg.getText().toString();
        this.btn.getText().toString();
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.preferences = getSharedPreferences("preferences", 0);
        try {
            SQLiteDatabase openOrCreateDatabase = openOrCreateDatabase("sqli", 0, null);
            this.mDB = openOrCreateDatabase;
            openOrCreateDatabase.execSQL("DROP TABLE IF EXISTS sqliuser;");
            this.mDB.execSQL("CREATE TABLE IF NOT EXISTS sqliuser(user VARCHAR, password VARCHAR, credit_card VARCHAR);");
            this.mDB.execSQL("INSERT INTO sqliuser VALUES ('admin', 'passwd123', '1234567812345678');");
            this.mDB.execSQL("INSERT INTO sqliuser VALUES ('beetle-bug', 'flg', '0x91334Z1');");
        } catch (Exception e) {
            Log.d("beetle-sqli", "Error occurred while creating database for SQLI: " + e.getMessage());
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
    }

    public void search(View view) {
        EditText search_text = (EditText) findViewById(C0572R.id.username);
        try {
            Cursor cr = this.mDB.rawQuery("SELECT * FROM sqliuser WHERE user = '" + search_text.getText().toString() + "'", null);
            StringBuilder strb = new StringBuilder("");
            if (cr != null && cr.getCount() > 0) {
                cr.moveToFirst();
                do {
                    strb.append("User: (" + cr.getString(0) + ") pass: (" + cr.getString(1) + ") Credit card: (" + cr.getString(2) + ")\n");
                } while (cr.moveToNext());
            } else {
                strb.append("User: (" + search_text.getText().toString() + ") not found");
            }
            TextView text_sqli = (TextView) findViewById(C0572R.id.textViewSqliResult);
            text_sqli.setText(strb.toString());
        } catch (Exception e) {
            Log.d("Beetle-sqli", "Error occurred while searching in database: " + e.getMessage());
        }
    }

    public void captureFlag(View view) {
        EditText m_flag = (EditText) findViewById(C0572R.id.flag);
        String pref_result = this.preferences.getString("10_sqli", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (!m_flag.getText().toString().equals(text)) {
            Toast.makeText(this, "Wrong answer", 0).show();
            return;
        }
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putFloat("ctf_score_sqli", 6.25f);
        editor.apply();
        Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
        String intent_sqli_str = Float.toString(6.25f);
        ctf_captured.putExtra("intent_str", intent_sqli_str);
        startActivity(ctf_captured);
    }
}
