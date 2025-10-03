package app.beetlebug.p001db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/* loaded from: classes3.dex */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE = "CREATE TABLE users(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, password CHAR(50));";
    static final String DB_NAME = "user.db";
    static final int DB_VERSION = 1;
    public static final String NAME = "name";
    public static final String PASS = "password";
    public static final String TABLE_NAME = "users";
    public static final String _ID = "_id";
    private SQLiteDatabase database;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, 1);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public void open() throws SQLException {
        this.database = getWritableDatabase();
    }

    @Override // android.database.sqlite.SQLiteOpenHelper, java.lang.AutoCloseable
    public void close() {
        this.database.close();
    }

    public void add(String name, String address) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put(PASS, address);
        this.database.insert(TABLE_NAME, null, contentValues);
    }

    public Cursor getAllEmployees() {
        String[] projection = {_ID, "name", PASS};
        Cursor cursor = this.database.query(TABLE_NAME, projection, null, null, null, null, null);
        return cursor;
    }

    public int update(long _id, String name, String address) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put(PASS, address);
        int count = this.database.update(TABLE_NAME, contentValues, _ID + " = " + _id, null);
        return count;
    }

    public void delete(long _id) {
        this.database.delete(TABLE_NAME, "_id = " + _id, null);
    }
}
