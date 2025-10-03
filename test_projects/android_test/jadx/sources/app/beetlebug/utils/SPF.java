package app.beetlebug.utils;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

/* loaded from: classes7.dex */
public class SPF extends AppCompatActivity {
    public static SharedPreferences sharedPreferences;

    public void base64encodeData() {
        SharedPreferences sharedPreferences2 = getSharedPreferences("flag_scores", 0);
        sharedPreferences = sharedPreferences2;
        SharedPreferences.Editor editor = sharedPreferences2.edit();
        editor.putString("result-one", "NzQzMjU4MA==");
    }
}
