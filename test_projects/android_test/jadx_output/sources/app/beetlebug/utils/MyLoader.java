package app.beetlebug.utils;

import android.content.Context;
import android.database.Cursor;
import androidx.loader.content.CursorLoader;
import app.beetlebug.p001db.DatabaseHelper;

/* loaded from: classes7.dex */
public class MyLoader extends CursorLoader {
    DatabaseHelper myDatabaseHelper;

    public MyLoader(Context context, DatabaseHelper db) {
        super(context);
        this.myDatabaseHelper = db;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // androidx.loader.content.CursorLoader, androidx.loader.content.AsyncTaskLoader
    public Cursor loadInBackground() {
        return this.myDatabaseHelper.getAllEmployees();
    }
}
