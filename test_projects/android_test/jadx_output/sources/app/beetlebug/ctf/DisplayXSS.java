package app.beetlebug.ctf;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;

/* loaded from: classes6.dex */
public class DisplayXSS extends AppCompatActivity {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_display_xss);
        Intent i = getIntent();
        String result = i.getStringExtra(WebViewXSSActivity.PACKAGE_STRING);
        WebView webView = (WebView) findViewById(C0572R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebChromeClient client = new WebChromeClient();
        webView.setWebChromeClient(client);
        webView.loadData(result, "text/html", "UTF-8");
    }

    public void goBack(View view) {
        startActivity(new Intent(this, (Class<?>) WebViewXSSActivity.class));
    }

    public void copyResult(View view) {
        TextView tv = (TextView) findViewById(C0572R.id.textViewFlag);
        String result = (String) tv.getText();
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService("clipboard");
        ClipData clipData = ClipData.newPlainText("TextView", result);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, "Copied to clipboard: " + result, 0).show();
    }
}
