package app.beetlebug.ctf;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import java.util.List;

/* loaded from: classes6.dex */
public class DeeplinkAccountActivity extends AppCompatActivity {
    Button copy;
    TextView flag;
    TextView msg;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_deeplink_account);
        this.flag = (TextView) findViewById(C0572R.id.textViewFlag);
        this.copy = (Button) findViewById(C0572R.id.copy);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        this.msg = (TextView) findViewById(C0572R.id.textViewUrl);
        Uri uri = getIntent().getData();
        if (uri != null) {
            List<String> parameters = uri.getPathSegments();
            String param = parameters.get(parameters.size() - 1);
            this.msg.setText(param);
            Toast.makeText(this, "Fingerprint Auth Successful", 1).show();
            this.copy.setOnClickListener(new View.OnClickListener() { // from class: app.beetlebug.ctf.DeeplinkAccountActivity.1
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    ClipboardManager clipboardManager = (ClipboardManager) DeeplinkAccountActivity.this.getSystemService("clipboard");
                    ClipData clipData = ClipData.newPlainText("TextView", DeeplinkAccountActivity.this.flag.getText().toString());
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(DeeplinkAccountActivity.this, "Copied to clipboard", 0).show();
                }
            });
        }
    }
}
