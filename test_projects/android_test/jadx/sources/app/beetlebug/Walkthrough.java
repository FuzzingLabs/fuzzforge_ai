package app.beetlebug;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import app.beetlebug.adapter.SlideViewPagerAdapter;
import app.beetlebug.user.UserSignUp;

/* loaded from: classes4.dex */
public class Walkthrough extends AppCompatActivity {
    public static ViewPager viewPager;
    SlideViewPagerAdapter adapter;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_walkthrough);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.yellow_700));
        }
        viewPager = (ViewPager) findViewById(C0572R.id.viewPager);
        SlideViewPagerAdapter slideViewPagerAdapter = new SlideViewPagerAdapter(this);
        this.adapter = slideViewPagerAdapter;
        viewPager.setAdapter(slideViewPagerAdapter);
        if (isOpenAlready()) {
            Intent intent = new Intent(this, (Class<?>) UserSignUp.class);
            intent.setFlags(268468224);
            startActivity(intent);
        } else {
            SharedPreferences.Editor editor = getSharedPreferences("walkthrough", 0).edit();
            editor.putBoolean("walkthrough", true);
            editor.commit();
        }
    }

    private boolean isOpenAlready() {
        SharedPreferences sharedPreferences = getSharedPreferences("walkthrough", 0);
        boolean result = sharedPreferences.getBoolean("walkthrough", false);
        return result;
    }
}
