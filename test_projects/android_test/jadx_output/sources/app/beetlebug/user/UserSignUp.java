package app.beetlebug.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import app.beetlebug.C0572R;
import app.beetlebug.MainActivity;
import java.util.UUID;

/* loaded from: classes4.dex */
public class UserSignUp extends AppCompatActivity {
    public static final String MyPREFERENCES = "user";
    public static final String m_name = "name";
    EditText mName;
    Button mSignUpButton;
    EditText m_password;
    Button m_signup;
    EditText m_username;
    SharedPreferences sharedPreferences;
    boolean isLoggedIn = false;
    boolean isAllFieldsChecked = false;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_user_sign_up);
        this.m_username = (EditText) findViewById(C0572R.id.editTextUsername);
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", 0);
        this.sharedPreferences = sharedPreferences;
        boolean z = sharedPreferences.getBoolean("is_logged_in", false);
        this.isLoggedIn = z;
        if (z) {
            Intent i = new Intent(this, (Class<?>) MainActivity.class);
            startActivity(i);
            finish();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
    }

    public void buttonSignUp(View view) {
        String username = this.m_username.getText().toString();
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(MyPREFERENCES, username);
        editor.putBoolean("is_logged_in", true);
        editor.putString("flag", "0x32f6641");
        editor.putString("user_token", UUID.randomUUID().toString());
        editor.apply();
        boolean CheckAllFields = CheckAllFields();
        this.isAllFieldsChecked = CheckAllFields;
        if (CheckAllFields) {
            Intent i = new Intent(this, (Class<?>) MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    public boolean CheckAllFields() {
        if (this.m_username.length() == 0) {
            this.m_username.setError("This field is required");
            return false;
        }
        return true;
    }

    public static String encrypt(String input) {
        return Base64.encodeToString(input.getBytes(), 0);
    }
}
