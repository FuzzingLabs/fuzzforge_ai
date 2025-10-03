package app.beetlebug.ctf;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import app.beetlebug.handlers.VulnerableContentProvider;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class InsecureContentProvider extends AppCompatActivity {
    Uri CONTENT_URI = Uri.parse("content://app.beetlebug.provider/users");
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_insecure_content_provider);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.preferences = getSharedPreferences("preferences", 0);
    }

    public void loadData(View view) {
        Cursor cursor = getContentResolver().query(Uri.parse("content://app.beetlebug.provider/users"), null, null, null, null);
        if (cursor.moveToFirst()) {
            StringBuilder strBuild = new StringBuilder();
            while (!cursor.isAfterLast()) {
                strBuild.append("\n" + cursor.getString(cursor.getColumnIndex("id")) + "-" + cursor.getString(cursor.getColumnIndex("name")));
                cursor.moveToNext();
            }
        }
    }

    public void insertData(View view) {
        ContentValues values = new ContentValues();
        values.put("name", ((EditText) findViewById(C0572R.id.username)).getText().toString() + " - flg 0x733421M");
        getContentResolver().insert(VulnerableContentProvider.CONTENT_URI, values);
        Toast.makeText(getBaseContext(), "New Record Inserted", 1).show();
    }

    public void flg(View view) {
        EditText flg = (EditText) findViewById(C0572R.id.flag);
        String result = flg.getText().toString();
        String pref_result = this.preferences.getString("7_content", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (result.equals(text)) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putFloat("ctf_score_content_provider", 6.25f);
            editor.commit();
            Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
            String intent_content_str = Float.toString(6.25f);
            ctf_captured.putExtra("intent_str", intent_content_str);
            startActivity(ctf_captured);
            return;
        }
        if (result.isEmpty()) {
            flg.setError("Try again");
        }
    }
}
