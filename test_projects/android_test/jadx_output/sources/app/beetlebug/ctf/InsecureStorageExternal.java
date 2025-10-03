package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class InsecureStorageExternal extends AppCompatActivity {
    LinearLayout mFlagLayout;
    EditText m_email;
    EditText m_pass;
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;
    public static String flag_scores = "flag_scores";
    public static String ctf_score_external = "ctf_score_external";

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_insecure_storage_external);
        LinearLayout linearLayout = (LinearLayout) findViewById(C0572R.id.flagLayout);
        this.mFlagLayout = linearLayout;
        linearLayout.setVisibility(8);
        this.sharedPreferences = getSharedPreferences(flag_scores, 0);
        this.preferences = getSharedPreferences("preferences", 0);
        String state = Environment.getExternalStorageState();
        if (!"mounted".equals(state) && "mounted_ro".equals(state)) {
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
    }

    public void saveCreds(View view) {
        this.m_pass = (EditText) findViewById(C0572R.id.editTextPassword);
        EditText editText = (EditText) findViewById(C0572R.id.editTextEmail);
        this.m_email = editText;
        String email = editText.getText().toString();
        String pass = this.m_pass.getText().toString();
        this.mFlagLayout.setVisibility(0);
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter username", 0).show();
            this.m_email.setError("Email cannot be blank");
            return;
        }
        if (pass.isEmpty()) {
            this.m_pass.setError("Enter your password");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Pass: " + pass);
        sb.append("\n");
        sb.append("Email: " + email);
        sb.append("\n");
        sb.append("flag : 0x3982c%4");
        String data = sb.toString();
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(folder, "user.txt");
        writeTextData(file, data);
        Toast.makeText(this, "Data saved to" + file.getAbsolutePath(), 1).show();
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:26:0x0031 -> B:6:0x0041). Please report as a decompilation issue!!! */
    private void writeTextData(File file, String data) {
        FileOutputStream fileOutputStream = null;
        try {
            try {
                try {
                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(data.getBytes());
                    Toast.makeText(this, "Done" + file.getAbsolutePath(), 0).show();
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                }
            } catch (Throwable th) {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (IOException e3) {
            e3.printStackTrace();
        }
    }

    public void captureFlag(View view) {
        EditText m_flag = (EditText) findViewById(C0572R.id.flag);
        String pref_result = this.preferences.getString("4_ext_store", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (m_flag.getText().toString().equals(text)) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putFloat(ctf_score_external, 6.25f);
            editor.apply();
            Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
            String intent_pref_str = Float.toString(6.25f);
            ctf_captured.putExtra("intent_str", intent_pref_str);
            startActivity(ctf_captured);
            return;
        }
        m_flag.setError("Wrong answer");
    }
}
