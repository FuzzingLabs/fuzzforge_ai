package app.beetlebug.p001db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
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

    /* JADX WARN: Code restructure failed: missing block: B:4:0x0015, code lost:
    
        if (r2.moveToFirst() != false) goto L5;
     */
    /* JADX WARN: Code restructure failed: missing block: B:5:0x0017, code lost:
    
        r4 = new app.beetlebug.p001db.DatabaseRecord();
        r4.setId(java.lang.Integer.parseInt(r2.getString(0)));
        r4.setTitle(r2.getString(1));
        r4.setAuthor(r2.getString(2));
        r0.add(r4);
     */
    /* JADX WARN: Code restructure failed: missing block: B:6:0x0040, code lost:
    
        if (r2.moveToNext() != false) goto L15;
     */
    /* JADX WARN: Code restructure failed: missing block: B:9:0x0042, code lost:
    
        android.util.Log.d("beetlebug", "getAllRecords(): " + r0.toString());
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public java.util.ArrayList<app.beetlebug.p001db.DatabaseRecord> getAllRecords() {
        /*
            r7 = this;
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            android.database.sqlite.SQLiteDatabase r1 = r7.getWritableDatabase()     // Catch: java.lang.Exception -> L5e
            java.lang.String r2 = "SELECT * FROM data"
            r3 = 0
            android.database.Cursor r2 = r1.rawQuery(r2, r3)     // Catch: java.lang.Exception -> L5e
            r3 = 0
            boolean r4 = r2.moveToFirst()     // Catch: java.lang.Exception -> L5e
            if (r4 == 0) goto L42
        L17:
            app.beetlebug.db.DatabaseRecord r4 = new app.beetlebug.db.DatabaseRecord     // Catch: java.lang.Exception -> L5e
            r4.<init>()     // Catch: java.lang.Exception -> L5e
            r3 = r4
            r4 = 0
            java.lang.String r4 = r2.getString(r4)     // Catch: java.lang.Exception -> L5e
            int r4 = java.lang.Integer.parseInt(r4)     // Catch: java.lang.Exception -> L5e
            r3.setId(r4)     // Catch: java.lang.Exception -> L5e
            r4 = 1
            java.lang.String r4 = r2.getString(r4)     // Catch: java.lang.Exception -> L5e
            r3.setTitle(r4)     // Catch: java.lang.Exception -> L5e
            r4 = 2
            java.lang.String r4 = r2.getString(r4)     // Catch: java.lang.Exception -> L5e
            r3.setAuthor(r4)     // Catch: java.lang.Exception -> L5e
            r0.add(r3)     // Catch: java.lang.Exception -> L5e
            boolean r4 = r2.moveToNext()     // Catch: java.lang.Exception -> L5e
            if (r4 != 0) goto L17
        L42:
            java.lang.String r4 = "beetlebug"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L5e
            r5.<init>()     // Catch: java.lang.Exception -> L5e
            java.lang.String r6 = "getAllRecords(): "
            r5.append(r6)     // Catch: java.lang.Exception -> L5e
            java.lang.String r6 = r0.toString()     // Catch: java.lang.Exception -> L5e
            r5.append(r6)     // Catch: java.lang.Exception -> L5e
            java.lang.String r5 = r5.toString()     // Catch: java.lang.Exception -> L5e
            android.util.Log.d(r4, r5)     // Catch: java.lang.Exception -> L5e
            goto L62
        L5e:
            r1 = move-exception
            r1.printStackTrace()
        L62:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: app.beetlebug.p001db.ContentHelper.getAllRecords():java.util.ArrayList");
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
