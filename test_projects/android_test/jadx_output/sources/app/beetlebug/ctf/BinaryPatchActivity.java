package app.beetlebug.ctf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.FlagCaptured;
import java.nio.charset.StandardCharsets;

/* loaded from: classes6.dex */
public class BinaryPatchActivity extends AppCompatActivity {
    LinearLayout lin;
    SharedPreferences preferences;
    SharedPreferences sharedPreferences;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_binary_patch);
        Button m_btn = (Button) findViewById(C0572R.id.button);
        LinearLayout linearLayout = (LinearLayout) findViewById(C0572R.id.flagLinearLayout);
        this.lin = linearLayout;
        linearLayout.setVisibility(8);
        this.sharedPreferences = getSharedPreferences("flag_scores", 0);
        this.preferences = getSharedPreferences("preferences", 0);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        m_btn.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.ctf.BinaryPatchActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                EditText flag = (EditText) BinaryPatchActivity.this.findViewById(C0572R.id.flag);
                String result = flag.getText().toString();
                String pref_result = BinaryPatchActivity.this.preferences.getString("16_patch", "");
                byte[] data = Base64.decode(pref_result, 0);
                String text = new String(data, StandardCharsets.UTF_8);
                if (result.equals(text)) {
                    SharedPreferences.Editor editor = BinaryPatchActivity.this.sharedPreferences.edit();
                    editor.putFloat("ctf_score_patch", 6.25f);
                    editor.apply();
                    Intent i = new Intent(BinaryPatchActivity.this, (Class<?>) FlagCaptured.class);
                    String intent_patch_str = Float.toString(6.25f);
                    i.putExtra("intent_str", intent_patch_str);
                    BinaryPatchActivity.this.startActivity(i);
                    return;
                }
                flag.setError("Try again");
            }
        });
    }

    public void grantAccess(View view) {
        this.lin.setVisibility(0);
        Toast.makeText(this, "Flag Found!", 1).show();
    }
}
