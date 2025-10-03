package app.beetlebug.ctf;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import app.beetlebug.C0572R;
import app.beetlebug.fragments.AddUserFragment;
import app.beetlebug.p001db.DatabaseHelper;
import app.beetlebug.utils.MyLoader;

/* loaded from: classes6.dex */
public class b33tleAdministrator extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 1976;
    private SimpleCursorAdapter adapter;
    LinearLayout flg;
    LinearLayout linearLayout;
    private ListView listView;
    private DatabaseHelper myHelper;
    RelativeLayout relativeLayout;
    RelativeLayout relativeLayout2;
    final String[] from = {DatabaseHelper._ID, "name", DatabaseHelper.PASS};

    /* renamed from: to */
    final int[] f111to = {C0572R.id.id, C0572R.id.name};

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0572R.layout.activity_b33tle_activtity);
        this.relativeLayout = (RelativeLayout) findViewById(C0572R.id.toolbar);
        this.relativeLayout2 = (RelativeLayout) findViewById(C0572R.id.relativeLayout);
        this.linearLayout = (LinearLayout) findViewById(C0572R.id.linear_layout_scroll);
        this.flg = (LinearLayout) findViewById(C0572R.id.ctfLayout);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(Integer.MIN_VALUE);
            window.clearFlags(67108864);
            window.setStatusBarColor(getResources().getColor(C0572R.color.white));
        }
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        this.myHelper = databaseHelper;
        databaseHelper.open();
        ListView listView = (ListView) findViewById(C0572R.id.list_view);
        this.listView = listView;
        listView.setEmptyView(findViewById(C0572R.id.textViewEmpty));
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, C0572R.layout.users, null, this.from, this.f111to, 0);
        this.adapter = simpleCursorAdapter;
        this.listView.setAdapter((ListAdapter) simpleCursorAdapter);
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    public void addUser(View view) {
        this.relativeLayout.setVisibility(8);
        this.relativeLayout2.setVisibility(8);
        this.flg.setVisibility(8);
        this.linearLayout.setVisibility(8);
        Fragment fragment = new AddUserFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(C0572R.id.container, fragment).commit();
    }

    @Override // androidx.loader.app.LoaderManager.LoaderCallbacks
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyLoader(this, this.myHelper);
    }

    @Override // androidx.loader.app.LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.adapter.swapCursor(data);
    }

    @Override // androidx.loader.app.LoaderManager.LoaderCallbacks
    public void onLoaderReset(Loader<Cursor> loader) {
        this.adapter.swapCursor(null);
    }
}
