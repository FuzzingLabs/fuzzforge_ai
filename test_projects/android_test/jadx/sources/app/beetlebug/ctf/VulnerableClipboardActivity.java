package app.beetlebug.ctf;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class VulnerableClipboardActivity extends AppCompatActivity {
    public Button m_pay;
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;
    public EditText u_card;
    public EditText u_cvv;
    public EditText u_exp;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_vulnerable_clipboard);
        this.u_card = (EditText) findViewById(C0572R.id.editTextCardNumber);
        this.u_exp = (EditText) findViewById(C0572R.id.editTextExpires);
        this.u_cvv = (EditText) findViewById(C0572R.id.editTextCvv);
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.preferences = getSharedPreferences("preferences", 0);
        Button button = (Button) findViewById(C0572R.id.pay);
        this.m_pay = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.ctf.VulnerableClipboardActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                String card = VulnerableClipboardActivity.this.u_card.getText().toString();
                String exp = VulnerableClipboardActivity.this.u_exp.getText().toString();
                String cvv = VulnerableClipboardActivity.this.u_cvv.getText().toString();
                if (card.isEmpty() && exp.isEmpty() && cvv.isEmpty()) {
                    VulnerableClipboardActivity.this.u_card.setError("Enter card all details");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("card: " + card + "\n  ");
                sb.append("expires: " + exp + "\n  ");
                sb.append("cvv: " + cvv + "\n  ");
                StringBuilder sb2 = new StringBuilder();
                sb2.append("flag: ");
                sb2.append(VulnerableClipboardActivity.this.getString(C0572R.string._1x33e91A));
                sb.append(sb2.toString());
                ClipboardManager clipboardManager = (ClipboardManager) VulnerableClipboardActivity.this.getSystemService("clipboard");
                ClipData clipData = ClipData.newPlainText("TextView", sb);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(VulnerableClipboardActivity.this, "Copied to clipboard" + ((Object) sb), 0).show();
            }
        });
    }

    public void flg(View view) {
        EditText flg = (EditText) findViewById(C0572R.id.flag);
        String result = flg.getText().toString();
        String pref_result = this.preferences.getString("14_clip", "");
        byte[] data = Base64.decode(pref_result, 0);
        String text = new String(data, StandardCharsets.UTF_8);
        if (result.equals(text)) {
            SharedPreferences.Editor editor = this.sharedPreferences.edit();
            editor.putFloat("ctf_score_clip", 6.25f);
            editor.apply();
            Intent ctf_captured = new Intent(this, (Class<?>) FlagCaptured.class);
            String intent_clip_str = Float.toString(6.25f);
            ctf_captured.putExtra("intent_str", intent_clip_str);
            startActivity(ctf_captured);
            return;
        }
        if (result.isEmpty()) {
            flg.setError("Try again");
        }
    }
}
