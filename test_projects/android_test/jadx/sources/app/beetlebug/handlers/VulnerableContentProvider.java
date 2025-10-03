package app.beetlebug.handlers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import java.util.HashMap;

/* loaded from: classes10.dex */
public class VulnerableContentProvider extends ContentProvider {
    static final String CREATE_DB_TABLE = " CREATE TABLE Users (id INTEGER PRIMARY KEY AUTOINCREMENT,  name TEXT NOT NULL);";
    static final String DATABASE_NAME = "UserDB";
    static final int DATABASE_VERSION = 1;
    static final String PROVIDER_NAME = "app.beetlebug.provider";
    static final String TABLE_NAME = "Users";

    /* renamed from: id */
    static final String f114id = "id";
    public static final String name = "name";
    static final int uriCode = 1;
    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;

    /* renamed from: db */
    private SQLiteDatabase f115db;
    static final String URL = "content://app.beetlebug.provider/users";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    static {
        UriMatcher uriMatcher2 = new UriMatcher(-1);
        uriMatcher = uriMatcher2;
        uriMatcher2.addURI(PROVIDER_NAME, app.beetlebug.p001db.DatabaseHelper.TABLE_NAME, 1);
        uriMatcher2.addURI(PROVIDER_NAME, "users/*", 1);
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case 1:
                return "vnd.android.cursor.dir/users";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        this.f115db = writableDatabase;
        if (writableDatabase != null) {
            return true;
        }
        return false;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case 1:
                qb.setProjectionMap(values);
                if (sortOrder == null || sortOrder == "") {
                    sortOrder = f114id;
                }
                Cursor c = qb.query(this.f115db, projection, selection, selectionArgs, null, null, sortOrder);
                c.setNotificationUri(getContext().getContentResolver(), uri);
                return c;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues values2) {
        long rowID = this.f115db.insert(TABLE_NAME, "", values2);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLiteException("Failed to add a record into " + uri);
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues values2, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case 1:
                int count = this.f115db.update(TABLE_NAME, values2, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case 1:
                int count = this.f115db.delete(TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, VulnerableContentProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(VulnerableContentProvider.CREATE_DB_TABLE);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS Users");
            onCreate(db);
        }
    }
}
