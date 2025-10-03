package app.beetlebug.p001db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import kotlin.text.Typography;

/* loaded from: classes3.dex */
public class ContentHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "beetleDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_DATA = "data";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_AUTHOR = "author";
    private static final String[] COLUMNS = {KEY_ID, KEY_TITLE, KEY_AUTHOR};

    public ContentHelper(Context context) {
        super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase db) {
        initDatabase(db);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS data");
        onCreate(db);
    }

    public void initDatabase(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE data ( id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, author TEXT )");
        } catch (Exception e) {
        }
    }

    public void initDatabaseOuter() {
        SQLiteDatabase db = getReadableDatabase();
        initDatabase(db);
    }

    public void addRecord(DatabaseRecord record) {
        try {
            Log.d("beetlebug", record.toString());
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_TITLE, record.getTitle());
            values.put(KEY_AUTHOR, record.getAuthor());
            db.insert(TABLE_DATA, null, values);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DatabaseRecord getRecord(int id) {
        DatabaseRecord record = new DatabaseRecord();
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from data where id='" + String.valueOf(id) + Typography.quote, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            record.setId(Integer.parseInt(cursor.getString(0)));
            record.setTitle(cursor.getString(1));
            record.setAuthor(cursor.getString(2));
            Log.d("beetlebug", "getRecord(" + id + "): " + record.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }

    public ArrayList<DatabaseRecord> getAllRecords() {
        ArrayList<DatabaseRecord> records = new ArrayList<>();
        try {
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM data", null);
            if (cursor.moveToFirst()) {
                do {
                    DatabaseRecord record = new DatabaseRecord();
                    record.setId(Integer.parseInt(cursor.getString(0)));
                    record.setTitle(cursor.getString(1));
                    record.setAuthor(cursor.getString(2));
                    records.add(record);
                } while (cursor.moveToNext());
            }
            Log.d("beetlebug", "getAllRecords(): " + records.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }

    public int updateRecord(DatabaseRecord record) {
        int i = -1;
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_TITLE, record.getTitle());
            values.put(KEY_AUTHOR, record.getAuthor());
            i = db.update(TABLE_DATA, values, "id = ?", new String[]{String.valueOf(record.getId())});
            db.close();
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return i;
        }
    }

    public void deleteRecord(DatabaseRecord record) {
        try {
            Log.d("beetlebug", "deleteRecord: " + record.toString());
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_DATA, "id = ?", new String[]{String.valueOf(record.getId())});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String rawSQLQuery(String query) {
        StringBuilder sb = new StringBuilder();
        SQLiteDatabase db = getWritableDatabase();
        try {
            Log.d("beetlebug", "rawSQLQuery: " + query);
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            while (!cursor.isAfterLast()) {
                String e = DatabaseUtils.dumpCurrentRowToString(cursor);
                Log.d("beetlebug", e);
                sb.append(e);
                sb.append("\n");
                cursor.moveToNext();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        db.close();
        return sb.toString();
    }

    public Cursor rawSQLQueryCursor(String query) {
        new StringBuilder();
        SQLiteDatabase db = getWritableDatabase();
        try {
            Log.d("beetlebug", "rawSQLQueryCursor: " + query);
            Cursor cursor = db.rawQuery(query, null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
