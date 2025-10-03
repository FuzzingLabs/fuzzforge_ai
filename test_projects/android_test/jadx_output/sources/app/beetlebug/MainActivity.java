package app.beetlebug;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import app.beetlebug.ctf.BinaryPatchActivity;
import app.beetlebug.home.AndroidComponentsHome;
import app.beetlebug.home.BiometricFragmentHome;
import app.beetlebug.home.DatabaseFragmentHome;
import app.beetlebug.home.InsecureStorageFragmentHome;
import app.beetlebug.home.SecretsFragmentHome;
import app.beetlebug.home.SensitiveDataFragmentHome;
import app.beetlebug.home.WebViewFragmentHome;
import app.beetlebug.user.PlayerStats;
import app.beetlebug.user.UserSignUp;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/* loaded from: classes4.dex */
public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    public static SharedPreferences preferences;
    BottomNavigationView bottomNavigationView;
    Context context;
    CardView mCardView1;
    ScrollView mScrollview;
    RelativeLayout mToolbar;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_main);
        this.mScrollview = (ScrollView) findViewById(C0572R.id.scroll_view);
        this.mToolbar = (RelativeLayout) findViewById(C0572R.id.toolbar);
        this.mCardView1 = (CardView) findViewById(C0572R.id.secretCard);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(C0572R.id.bottom_navigation);
        this.bottomNavigationView = bottomNavigationView;
        bottomNavigationView.setSelectedItemId(C0572R.id.nav_home);
        this.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() { // from class: app.beetlebug.MainActivity.1
            @Override // com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case C0572R.id.nav_flag /* 2131362198 */:
                        Intent intent2 = new Intent(MainActivity.this, (Class<?>) FlagsOverview.class);
                        MainActivity.this.startActivity(intent2);
                        return true;
                    case C0572R.id.nav_home /* 2131362199 */:
                    default:
                        return true;
                    case C0572R.id.nav_user /* 2131362200 */:
                        Intent intent3 = new Intent(MainActivity.this, (Class<?>) PlayerStats.class);
                        MainActivity.this.startActivity(intent3);
                        return true;
                }
            }
        });
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        encryptPreferences();
    }

    public void secretCtf(View view) {
        this.mScrollview.setVisibility(8);
        this.mToolbar.setVisibility(8);
        this.bottomNavigationView.setVisibility(8);
        Fragment fragment = new SecretsFragmentHome();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void dataStorageFragment(View view) {
        this.mScrollview.setVisibility(8);
        this.mToolbar.setVisibility(8);
        this.bottomNavigationView.setVisibility(8);
        Fragment fragment = new InsecureStorageFragmentHome();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void webViewFragment(View view) {
        this.mScrollview.setVisibility(8);
        this.mToolbar.setVisibility(8);
        this.bottomNavigationView.setVisibility(8);
        Fragment fragment = new WebViewFragmentHome();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void databaseFragment(View view) {
        this.mScrollview.setVisibility(8);
        this.mToolbar.setVisibility(8);
        this.bottomNavigationView.setVisibility(8);
        Fragment fragment = new DatabaseFragmentHome();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void biometricFragment(View view) {
        this.mScrollview.setVisibility(8);
        this.mToolbar.setVisibility(8);
        this.bottomNavigationView.setVisibility(8);
        Fragment fragment = new BiometricFragmentHome();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void AndroidComponentsFragment(View view) {
        this.mScrollview.setVisibility(8);
        this.mToolbar.setVisibility(8);
        this.bottomNavigationView.setVisibility(8);
        Fragment fragment = new AndroidComponentsHome();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void infoDisclosureCTF(View view) {
        this.mScrollview.setVisibility(8);
        this.mToolbar.setVisibility(8);
        this.bottomNavigationView.setVisibility(8);
        Fragment fragment = new SensitiveDataFragmentHome();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    public void patchFragment(View view) {
        Intent i = new Intent(this, (Class<?>) BinaryPatchActivity.class);
        startActivity(i);
    }

    @Override // android.widget.PopupMenu.OnMenuItemClickListener
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case C0572R.id.about /* 2131361806 */:
                showDialog();
                return true;
            case C0572R.id.clear /* 2131361943 */:
                SharedPreferences sharedPreferences_flg = getSharedPreferences("flag_scores", 0);
                SharedPreferences.Editor editor_flg = sharedPreferences_flg.edit();
                editor_flg.clear();
                editor_flg.commit();
                Toast.makeText(this, "All flags cleared!", 1).show();
                return true;
            case C0572R.id.logout /* 2131362137 */:
                SharedPreferences sharedPreferences = getSharedPreferences("user_info", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.commit();
                Intent logout = new Intent(this, (Class<?>) UserSignUp.class);
                startActivity(logout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(C0572R.layout.alert_about, (ViewGroup) null);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();
        alertDialog.show();
    }

    public void openDeveloperPage(View view) {
        Intent i = new Intent("android.intent.action.VIEW");
        i.setData(Uri.parse("http://www.hafiz.ng"));
        startActivity(i);
    }

    public void showPopup(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(C0572R.menu.pop_up_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    private void encryptPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", 0);
        preferences = sharedPreferences;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("3_pref", "MHgxNDQyYzA0");
        editor.putString("4_ext_store", "MHgzOTgyYyU0");
        editor.putString("5_sqlite", "MHgxMTcyYzA0");
        editor.putString("6_activity", "MHgzMzRmMjIx");
        editor.putString("7_content", "MHg3MzM0MjFN");
        editor.putString("8_service", "MHgyMjIxMDNB");
        editor.putString("9_log", "MHg1NTU0MWQz");
        editor.putString("10_sqli", "MHg5MTMzNFox");
        editor.putString("11_firebase", "MHgzMzY1QTEw");
        editor.putString("12_url", "MHgzM2YzMzQx");
        editor.putString("13_xss", "MHg2NnI5MjE0");
        editor.putString("14_clip", "MHgxMTMyYzQh");
        editor.putString("15_fingerprint", "MHg0M0oxMjMm");
        editor.putString("16_patch", "MHgzM2U5JGU=");
        editor.apply();
    }
}
