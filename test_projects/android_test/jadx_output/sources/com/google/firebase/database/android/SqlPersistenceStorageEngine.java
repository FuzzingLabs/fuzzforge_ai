package com.google.firebase.database.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.core.CompoundWrite;
import com.google.firebase.database.core.Path;
import com.google.firebase.database.core.UserWriteRecord;
import com.google.firebase.database.core.persistence.PersistenceStorageEngine;
import com.google.firebase.database.core.persistence.PruneForest;
import com.google.firebase.database.core.persistence.TrackedQuery;
import com.google.firebase.database.core.utilities.ImmutableTree;
import com.google.firebase.database.core.utilities.NodeSizeEstimator;
import com.google.firebase.database.core.utilities.Pair;
import com.google.firebase.database.core.utilities.Utilities;
import com.google.firebase.database.core.view.QuerySpec;
import com.google.firebase.database.logging.LogWrapper;
import com.google.firebase.database.snapshot.ChildKey;
import com.google.firebase.database.snapshot.ChildrenNode;
import com.google.firebase.database.snapshot.EmptyNode;
import com.google.firebase.database.snapshot.NamedNode;
import com.google.firebase.database.snapshot.Node;
import com.google.firebase.database.snapshot.NodeUtilities;
import com.google.firebase.database.util.JsonMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public class SqlPersistenceStorageEngine implements PersistenceStorageEngine {
    private static final int CHILDREN_NODE_SPLIT_SIZE_THRESHOLD = 16384;
    private static final String CREATE_SERVER_CACHE = "CREATE TABLE serverCache (path TEXT PRIMARY KEY, value BLOB);";
    private static final String CREATE_TRACKED_KEYS = "CREATE TABLE trackedKeys (id INTEGER, key TEXT);";
    private static final String CREATE_TRACKED_QUERIES = "CREATE TABLE trackedQueries (id INTEGER PRIMARY KEY, path TEXT, queryParams TEXT, lastUse INTEGER, complete INTEGER, active INTEGER);";
    private static final String CREATE_WRITES = "CREATE TABLE writes (id INTEGER, path TEXT, type TEXT, part INTEGER, node BLOB, UNIQUE (id, part));";
    private static final String FIRST_PART_KEY = ".part-0000";
    private static final String LOGGER_COMPONENT = "Persistence";
    private static final String PART_KEY_FORMAT = ".part-%04d";
    private static final String PART_KEY_PREFIX = ".part-";
    private static final String PATH_COLUMN_NAME = "path";
    private static final String ROW_ID_COLUMN_NAME = "rowid";
    private static final int ROW_SPLIT_SIZE = 262144;
    private static final String SERVER_CACHE_TABLE = "serverCache";
    private static final String TRACKED_KEYS_ID_COLUMN_NAME = "id";
    private static final String TRACKED_KEYS_KEY_COLUMN_NAME = "key";
    private static final String TRACKED_KEYS_TABLE = "trackedKeys";
    private static final String TRACKED_QUERY_ACTIVE_COLUMN_NAME = "active";
    private static final String TRACKED_QUERY_COMPLETE_COLUMN_NAME = "complete";
    private static final String TRACKED_QUERY_ID_COLUMN_NAME = "id";
    private static final String TRACKED_QUERY_LAST_USE_COLUMN_NAME = "lastUse";
    private static final String TRACKED_QUERY_PARAMS_COLUMN_NAME = "queryParams";
    private static final String TRACKED_QUERY_PATH_COLUMN_NAME = "path";
    private static final String TRACKED_QUERY_TABLE = "trackedQueries";
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private static final String VALUE_COLUMN_NAME = "value";
    private static final String WRITES_TABLE = "writes";
    private static final String WRITE_ID_COLUMN_NAME = "id";
    private static final String WRITE_NODE_COLUMN_NAME = "node";
    private static final String WRITE_PART_COLUMN_NAME = "part";
    private static final String WRITE_TYPE_COLUMN_NAME = "type";
    private static final String WRITE_TYPE_MERGE = "m";
    private static final String WRITE_TYPE_OVERWRITE = "o";
    private final SQLiteDatabase database;
    private boolean insideTransaction;
    private final LogWrapper logger;
    private long transactionStart = 0;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class PersistentCacheOpenHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 2;

        public PersistentCacheOpenHelper(Context context, String cacheId) {
            super(context, cacheId, (SQLiteDatabase.CursorFactory) null, 2);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SqlPersistenceStorageEngine.CREATE_SERVER_CACHE);
            db.execSQL(SqlPersistenceStorageEngine.CREATE_WRITES);
            db.execSQL(SqlPersistenceStorageEngine.CREATE_TRACKED_QUERIES);
            db.execSQL(SqlPersistenceStorageEngine.CREATE_TRACKED_KEYS);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Utilities.hardAssert(newVersion == 2, "Why is onUpgrade() called with a different version?");
            if (oldVersion <= 1) {
                dropTable(db, SqlPersistenceStorageEngine.SERVER_CACHE_TABLE);
                db.execSQL(SqlPersistenceStorageEngine.CREATE_SERVER_CACHE);
                dropTable(db, SqlPersistenceStorageEngine.TRACKED_QUERY_COMPLETE_COLUMN_NAME);
                db.execSQL(SqlPersistenceStorageEngine.CREATE_TRACKED_KEYS);
                db.execSQL(SqlPersistenceStorageEngine.CREATE_TRACKED_QUERIES);
                return;
            }
            throw new AssertionError("We don't handle upgrading to " + newVersion);
        }

        private void dropTable(SQLiteDatabase db, String table) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
    }

    public SqlPersistenceStorageEngine(Context context, com.google.firebase.database.core.Context firebaseContext, String cacheId) {
        try {
            String sanitizedCacheId = URLEncoder.encode(cacheId, "utf-8");
            this.logger = firebaseContext.getLogger(LOGGER_COMPONENT);
            this.database = openDatabase(context, sanitizedCacheId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void saveUserOverwrite(Path path, Node node, long writeId) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        byte[] serializedNode = serializeObject(node.getValue(true));
        saveWrite(path, writeId, WRITE_TYPE_OVERWRITE, serializedNode);
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Persisted user overwrite in %dms", Long.valueOf(duration)), new Object[0]);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void saveUserMerge(Path path, CompoundWrite children, long writeId) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        byte[] serializedNode = serializeObject(children.getValue(true));
        saveWrite(path, writeId, WRITE_TYPE_MERGE, serializedNode);
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Persisted user merge in %dms", Long.valueOf(duration)), new Object[0]);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void removeUserWrite(long writeId) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        int count = this.database.delete(WRITES_TABLE, "id = ?", new String[]{String.valueOf(writeId)});
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Deleted %d write(s) with writeId %d in %dms", Integer.valueOf(count), Long.valueOf(writeId), Long.valueOf(duration)), new Object[0]);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public List<UserWriteRecord> loadUserWrites() {
        byte[] serialized;
        UserWriteRecord record;
        String[] columns = {"id", "path", WRITE_TYPE_COLUMN_NAME, WRITE_PART_COLUMN_NAME, WRITE_NODE_COLUMN_NAME};
        long start = System.currentTimeMillis();
        Cursor cursor = this.database.query(WRITES_TABLE, columns, null, null, null, null, "id, part");
        List<UserWriteRecord> writes = new ArrayList<>();
        while (cursor.moveToNext()) {
            try {
                try {
                    long writeId = cursor.getLong(0);
                    Path path = new Path(cursor.getString(1));
                    String type = cursor.getString(2);
                    if (cursor.isNull(3)) {
                        serialized = cursor.getBlob(4);
                    } else {
                        List<byte[]> parts = new ArrayList<>();
                        do {
                            parts.add(cursor.getBlob(4));
                            if (!cursor.moveToNext()) {
                                break;
                            }
                        } while (cursor.getLong(0) == writeId);
                        cursor.moveToPrevious();
                        serialized = joinBytes(parts);
                    }
                    String serializedString = new String(serialized, UTF8_CHARSET);
                    Object writeValue = JsonMapper.parseJsonValue(serializedString);
                    if (WRITE_TYPE_OVERWRITE.equals(type)) {
                        Node set = NodeUtilities.NodeFromJSON(writeValue);
                        record = new UserWriteRecord(writeId, path, set, true);
                    } else if (WRITE_TYPE_MERGE.equals(type)) {
                        CompoundWrite merge = CompoundWrite.fromValue((Map) writeValue);
                        record = new UserWriteRecord(writeId, path, merge);
                    } else {
                        throw new IllegalStateException("Got invalid write type: " + type);
                    }
                    writes.add(record);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load writes", e);
                }
            } finally {
                cursor.close();
            }
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Loaded %d writes in %dms", Integer.valueOf(writes.size()), Long.valueOf(duration)), new Object[0]);
        }
        return writes;
    }

    private void saveWrite(Path path, long writeId, String type, byte[] serializedWrite) {
        verifyInsideTransaction();
        this.database.delete(WRITES_TABLE, "id = ?", new String[]{String.valueOf(writeId)});
        if (serializedWrite.length >= 262144) {
            List<byte[]> parts = splitBytes(serializedWrite, 262144);
            for (int i = 0; i < parts.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("id", Long.valueOf(writeId));
                values.put("path", pathToKey(path));
                values.put(WRITE_TYPE_COLUMN_NAME, type);
                values.put(WRITE_PART_COLUMN_NAME, Integer.valueOf(i));
                values.put(WRITE_NODE_COLUMN_NAME, parts.get(i));
                this.database.insertWithOnConflict(WRITES_TABLE, null, values, 5);
            }
            return;
        }
        ContentValues values2 = new ContentValues();
        values2.put("id", Long.valueOf(writeId));
        values2.put("path", pathToKey(path));
        values2.put(WRITE_TYPE_COLUMN_NAME, type);
        values2.put(WRITE_PART_COLUMN_NAME, (Integer) null);
        values2.put(WRITE_NODE_COLUMN_NAME, serializedWrite);
        this.database.insertWithOnConflict(WRITES_TABLE, null, values2, 5);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public Node serverCache(Path path) {
        return loadNested(path);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void overwriteServerCache(Path path, Node node) {
        verifyInsideTransaction();
        updateServerCache(path, node, false);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void mergeIntoServerCache(Path path, Node node) {
        verifyInsideTransaction();
        updateServerCache(path, node, true);
    }

    private void updateServerCache(Path path, Node node, boolean merge) {
        int removedRows;
        int removedRows2;
        long start = System.currentTimeMillis();
        if (!merge) {
            removedRows = removeNested(SERVER_CACHE_TABLE, path);
            removedRows2 = saveNested(path, node);
        } else {
            int removedRows3 = 0;
            int savedRows = 0;
            for (NamedNode child : node) {
                removedRows3 += removeNested(SERVER_CACHE_TABLE, path.child(child.getName()));
                savedRows += saveNested(path.child(child.getName()), child.getNode());
            }
            removedRows = removedRows3;
            removedRows2 = savedRows;
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Persisted a total of %d rows and deleted %d rows for a set at %s in %dms", Integer.valueOf(removedRows2), Integer.valueOf(removedRows), path.toString(), Long.valueOf(duration)), new Object[0]);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void mergeIntoServerCache(Path path, CompoundWrite children) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        int savedRows = 0;
        int removedRows = 0;
        Iterator<Map.Entry<Path, Node>> it = children.iterator();
        while (it.hasNext()) {
            Map.Entry<Path, Node> entry = it.next();
            removedRows += removeNested(SERVER_CACHE_TABLE, path.child(entry.getKey()));
            savedRows += saveNested(path.child(entry.getKey()), entry.getValue());
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Persisted a total of %d rows and deleted %d rows for a merge at %s in %dms", Integer.valueOf(savedRows), Integer.valueOf(removedRows), path.toString(), Long.valueOf(duration)), new Object[0]);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public long serverCacheEstimatedSizeInBytes() {
        String query = String.format("SELECT sum(length(%s) + length(%s)) FROM %s", VALUE_COLUMN_NAME, "path", SERVER_CACHE_TABLE);
        Cursor cursor = this.database.rawQuery(query, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
            throw new IllegalStateException("Couldn't read database result!");
        } finally {
            cursor.close();
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void saveTrackedQuery(TrackedQuery trackedQuery) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put("id", Long.valueOf(trackedQuery.f247id));
        values.put("path", pathToKey(trackedQuery.querySpec.getPath()));
        values.put(TRACKED_QUERY_PARAMS_COLUMN_NAME, trackedQuery.querySpec.getParams().toJSON());
        values.put(TRACKED_QUERY_LAST_USE_COLUMN_NAME, Long.valueOf(trackedQuery.lastUse));
        values.put(TRACKED_QUERY_COMPLETE_COLUMN_NAME, Boolean.valueOf(trackedQuery.complete));
        values.put(TRACKED_QUERY_ACTIVE_COLUMN_NAME, Boolean.valueOf(trackedQuery.active));
        this.database.insertWithOnConflict(TRACKED_QUERY_TABLE, null, values, 5);
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Saved new tracked query in %dms", Long.valueOf(duration)), new Object[0]);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void deleteTrackedQuery(long trackedQueryId) {
        verifyInsideTransaction();
        String trackedQueryIdStr = String.valueOf(trackedQueryId);
        this.database.delete(TRACKED_QUERY_TABLE, "id = ?", new String[]{trackedQueryIdStr});
        this.database.delete(TRACKED_KEYS_TABLE, "id = ?", new String[]{trackedQueryIdStr});
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public List<TrackedQuery> loadTrackedQueries() {
        String[] columns = {"id", "path", TRACKED_QUERY_PARAMS_COLUMN_NAME, TRACKED_QUERY_LAST_USE_COLUMN_NAME, TRACKED_QUERY_COMPLETE_COLUMN_NAME, TRACKED_QUERY_ACTIVE_COLUMN_NAME};
        long start = System.currentTimeMillis();
        Cursor cursor = this.database.query(TRACKED_QUERY_TABLE, columns, null, null, null, null, "id");
        List<TrackedQuery> queries = new ArrayList<>();
        while (cursor.moveToNext()) {
            try {
                long id = cursor.getLong(0);
                Path path = new Path(cursor.getString(1));
                String paramsStr = cursor.getString(2);
                try {
                    Map<String, Object> paramsObject = JsonMapper.parseJson(paramsStr);
                    QuerySpec query = QuerySpec.fromPathAndQueryObject(path, paramsObject);
                    long lastUse = cursor.getLong(3);
                    boolean complete = cursor.getInt(4) != 0;
                    boolean active = cursor.getInt(5) != 0;
                    TrackedQuery trackedQuery = new TrackedQuery(id, query, lastUse, complete, active);
                    queries.add(trackedQuery);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                cursor.close();
            }
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Loaded %d tracked queries in %dms", Integer.valueOf(queries.size()), Long.valueOf(duration)), new Object[0]);
        }
        return queries;
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void resetPreviouslyActiveTrackedQueries(long lastUse) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(TRACKED_QUERY_ACTIVE_COLUMN_NAME, (Boolean) false);
        values.put(TRACKED_QUERY_LAST_USE_COLUMN_NAME, Long.valueOf(lastUse));
        this.database.updateWithOnConflict(TRACKED_QUERY_TABLE, values, "active = 1", new String[0], 5);
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Reset active tracked queries in %dms", Long.valueOf(duration)), new Object[0]);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void saveTrackedQueryKeys(long trackedQueryId, Set<ChildKey> keys) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        String trackedQueryIdStr = String.valueOf(trackedQueryId);
        this.database.delete(TRACKED_KEYS_TABLE, "id = ?", new String[]{trackedQueryIdStr});
        for (ChildKey addedKey : keys) {
            ContentValues values = new ContentValues();
            values.put("id", Long.valueOf(trackedQueryId));
            values.put(TRACKED_KEYS_KEY_COLUMN_NAME, addedKey.asString());
            this.database.insertWithOnConflict(TRACKED_KEYS_TABLE, null, values, 5);
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Set %d tracked query keys for tracked query %d in %dms", Integer.valueOf(keys.size()), Long.valueOf(trackedQueryId), Long.valueOf(duration)), new Object[0]);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void updateTrackedQueryKeys(long trackedQueryId, Set<ChildKey> added, Set<ChildKey> removed) {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        String trackedQueryIdStr = String.valueOf(trackedQueryId);
        for (ChildKey removedKey : removed) {
            this.database.delete(TRACKED_KEYS_TABLE, "id = ? AND key = ?", new String[]{trackedQueryIdStr, removedKey.asString()});
        }
        for (ChildKey addedKey : added) {
            ContentValues values = new ContentValues();
            values.put("id", Long.valueOf(trackedQueryId));
            values.put(TRACKED_KEYS_KEY_COLUMN_NAME, addedKey.asString());
            this.database.insertWithOnConflict(TRACKED_KEYS_TABLE, null, values, 5);
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Updated tracked query keys (%d added, %d removed) for tracked query id %d in %dms", Integer.valueOf(added.size()), Integer.valueOf(removed.size()), Long.valueOf(trackedQueryId), Long.valueOf(duration)), new Object[0]);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public Set<ChildKey> loadTrackedQueryKeys(long trackedQueryId) {
        return loadTrackedQueryKeys(Collections.singleton(Long.valueOf(trackedQueryId)));
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public Set<ChildKey> loadTrackedQueryKeys(Set<Long> trackedQueryIds) {
        String[] columns = {TRACKED_KEYS_KEY_COLUMN_NAME};
        long start = System.currentTimeMillis();
        String whereClause = "id IN (" + commaSeparatedList(trackedQueryIds) + ")";
        Cursor cursor = this.database.query(true, TRACKED_KEYS_TABLE, columns, whereClause, null, null, null, null, null);
        Set<ChildKey> keys = new HashSet<>();
        while (cursor.moveToNext()) {
            try {
                String key = cursor.getString(0);
                keys.add(ChildKey.fromString(key));
            } finally {
                cursor.close();
            }
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Loaded %d tracked queries keys for tracked queries %s in %dms", Integer.valueOf(keys.size()), trackedQueryIds.toString(), Long.valueOf(duration)), new Object[0]);
        }
        return keys;
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void pruneCache(Path root, PruneForest pruneForest) {
        char c;
        if (!pruneForest.prunesAnything()) {
            return;
        }
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        Cursor cursor = loadNestedQuery(root, new String[]{ROW_ID_COLUMN_NAME, "path"});
        ImmutableTree<Long> rowIdsToPrune = new ImmutableTree<>(null);
        ImmutableTree<Long> rowIdsToPrune2 = rowIdsToPrune;
        ImmutableTree<Long> rowIdsToKeep = new ImmutableTree<>(null);
        while (cursor.moveToNext()) {
            long rowId = cursor.getLong(0);
            Path rowPath = new Path(cursor.getString(1));
            if (!root.contains(rowPath)) {
                this.logger.warn("We are pruning at " + root + " but we have data stored higher up at " + rowPath + ". Ignoring.");
            } else {
                Path relativePath = Path.getRelative(root, rowPath);
                if (pruneForest.shouldPruneUnkeptDescendants(relativePath)) {
                    rowIdsToPrune2 = rowIdsToPrune2.set(relativePath, Long.valueOf(rowId));
                } else if (pruneForest.shouldKeep(relativePath)) {
                    rowIdsToKeep = rowIdsToKeep.set(relativePath, Long.valueOf(rowId));
                } else {
                    this.logger.warn("We are pruning at " + root + " and have data at " + rowPath + " that isn't marked for pruning or keeping. Ignoring.");
                }
            }
        }
        int prunedCount = 0;
        int resavedCount = 0;
        if (rowIdsToPrune2.isEmpty()) {
            c = 1;
        } else {
            List<Pair<Path, Node>> rowsToResave = new ArrayList<>();
            c = 1;
            pruneTreeRecursive(root, Path.getEmptyPath(), rowIdsToPrune2, rowIdsToKeep, pruneForest, rowsToResave);
            Collection<Long> rowIdsToDelete = rowIdsToPrune2.values();
            String whereClause = "rowid IN (" + commaSeparatedList(rowIdsToDelete) + ")";
            this.database.delete(SERVER_CACHE_TABLE, whereClause, null);
            for (Pair<Path, Node> node : rowsToResave) {
                saveNested(root.child(node.getFirst()), node.getSecond());
            }
            prunedCount = rowIdsToDelete.size();
            resavedCount = rowsToResave.size();
        }
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            LogWrapper logWrapper = this.logger;
            Locale locale = Locale.US;
            Object[] objArr = new Object[3];
            objArr[0] = Integer.valueOf(prunedCount);
            objArr[c] = Integer.valueOf(resavedCount);
            objArr[2] = Long.valueOf(duration);
            logWrapper.debug(String.format(locale, "Pruned %d rows with %d nodes resaved in %dms", objArr), new Object[0]);
        }
    }

    private void pruneTreeRecursive(Path pruneRoot, final Path relativePath, ImmutableTree<Long> rowIdsToPrune, final ImmutableTree<Long> rowIdsToKeep, PruneForest pruneForest, final List<Pair<Path, Node>> rowsToResaveAccumulator) {
        if (rowIdsToPrune.getValue() != null) {
            int nodesToResave = ((Integer) pruneForest.foldKeptNodes(0, new ImmutableTree.TreeVisitor<Void, Integer>() { // from class: com.google.firebase.database.android.SqlPersistenceStorageEngine.1
                @Override // com.google.firebase.database.core.utilities.ImmutableTree.TreeVisitor
                public Integer onNodeValue(Path keepPath, Void ignore, Integer nodesToResave2) {
                    return Integer.valueOf(rowIdsToKeep.get(keepPath) == null ? nodesToResave2.intValue() + 1 : nodesToResave2.intValue());
                }
            })).intValue();
            if (nodesToResave > 0) {
                Path absolutePath = pruneRoot.child(relativePath);
                if (this.logger.logsDebug()) {
                    this.logger.debug(String.format(Locale.US, "Need to rewrite %d nodes below path %s", Integer.valueOf(nodesToResave), absolutePath), new Object[0]);
                }
                final Node currentNode = loadNested(absolutePath);
                pruneForest.foldKeptNodes(null, new ImmutableTree.TreeVisitor<Void, Void>() { // from class: com.google.firebase.database.android.SqlPersistenceStorageEngine.2
                    @Override // com.google.firebase.database.core.utilities.ImmutableTree.TreeVisitor
                    public Void onNodeValue(Path keepPath, Void ignore, Void ignore2) {
                        if (rowIdsToKeep.get(keepPath) == null) {
                            rowsToResaveAccumulator.add(new Pair(relativePath.child(keepPath), currentNode.getChild(keepPath)));
                            return null;
                        }
                        return null;
                    }
                });
            }
            return;
        }
        Iterator<Map.Entry<ChildKey, ImmutableTree<Long>>> it = rowIdsToPrune.getChildren().iterator();
        while (it.hasNext()) {
            Map.Entry<ChildKey, ImmutableTree<Long>> entry = it.next();
            ChildKey childKey = entry.getKey();
            PruneForest childPruneForest = pruneForest.child(entry.getKey());
            pruneTreeRecursive(pruneRoot, relativePath.child(childKey), entry.getValue(), rowIdsToKeep.getChild(childKey), childPruneForest, rowsToResaveAccumulator);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void removeAllUserWrites() {
        verifyInsideTransaction();
        long start = System.currentTimeMillis();
        int count = this.database.delete(WRITES_TABLE, null, null);
        long duration = System.currentTimeMillis() - start;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Deleted %d (all) write(s) in %dms", Integer.valueOf(count), Long.valueOf(duration)), new Object[0]);
        }
    }

    public void purgeCache() {
        verifyInsideTransaction();
        this.database.delete(SERVER_CACHE_TABLE, null, null);
        this.database.delete(WRITES_TABLE, null, null);
        this.database.delete(TRACKED_QUERY_TABLE, null, null);
        this.database.delete(TRACKED_KEYS_TABLE, null, null);
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void beginTransaction() {
        Utilities.hardAssert(!this.insideTransaction, "runInTransaction called when an existing transaction is already in progress.");
        if (this.logger.logsDebug()) {
            this.logger.debug("Starting transaction.", new Object[0]);
        }
        this.database.beginTransaction();
        this.insideTransaction = true;
        this.transactionStart = System.currentTimeMillis();
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void endTransaction() {
        this.database.endTransaction();
        this.insideTransaction = false;
        long elapsed = System.currentTimeMillis() - this.transactionStart;
        if (this.logger.logsDebug()) {
            this.logger.debug(String.format(Locale.US, "Transaction completed. Elapsed: %dms", Long.valueOf(elapsed)), new Object[0]);
        }
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void setTransactionSuccessful() {
        this.database.setTransactionSuccessful();
    }

    @Override // com.google.firebase.database.core.persistence.PersistenceStorageEngine
    public void close() {
        this.database.close();
    }

    private SQLiteDatabase openDatabase(Context context, String cacheId) {
        PersistentCacheOpenHelper helper = new PersistentCacheOpenHelper(context, cacheId);
        try {
            SQLiteDatabase database = helper.getWritableDatabase();
            database.rawQuery("PRAGMA locking_mode = EXCLUSIVE", null).close();
            database.beginTransaction();
            database.endTransaction();
            return database;
        } catch (SQLiteException e) {
            if (e instanceof SQLiteDatabaseLockedException) {
                throw new DatabaseException("Failed to gain exclusive lock to Firebase Database's offline persistence. This generally means you are using Firebase Database from multiple processes in your app. Keep in mind that multi-process Android apps execute the code in your Application class in all processes, so you may need to avoid initializing FirebaseDatabase in your Application class. If you are intentionally using Firebase Database from multiple processes, you can only enable offline persistence (i.e. call setPersistenceEnabled(true)) in one of them.", e);
            }
            throw e;
        }
    }

    private void verifyInsideTransaction() {
        Utilities.hardAssert(this.insideTransaction, "Transaction expected to already be in progress.");
    }

    private int saveNested(Path path, Node node) {
        long estimatedSize = NodeSizeEstimator.estimateSerializedNodeSize(node);
        if ((node instanceof ChildrenNode) && estimatedSize > 16384) {
            if (this.logger.logsDebug()) {
                this.logger.debug(String.format(Locale.US, "Node estimated serialized size at path %s of %d bytes exceeds limit of %d bytes. Splitting up.", path, Long.valueOf(estimatedSize), 16384), new Object[0]);
            }
            int sum = 0;
            for (NamedNode child : node) {
                sum += saveNested(path.child(child.getName()), child.getNode());
            }
            if (!node.getPriority().isEmpty()) {
                saveNode(path.child(ChildKey.getPriorityKey()), node.getPriority());
                sum++;
            }
            saveNode(path, EmptyNode.Empty());
            return sum + 1;
        }
        saveNode(path, node);
        return 1;
    }

    private String partKey(Path path, int i) {
        return pathToKey(path) + String.format(Locale.US, PART_KEY_FORMAT, Integer.valueOf(i));
    }

    private void saveNode(Path path, Node node) {
        byte[] serialized = serializeObject(node.getValue(true));
        if (serialized.length >= 262144) {
            List<byte[]> parts = splitBytes(serialized, 262144);
            if (this.logger.logsDebug()) {
                this.logger.debug("Saving huge leaf node with " + parts.size() + " parts.", new Object[0]);
            }
            for (int i = 0; i < parts.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("path", partKey(path, i));
                values.put(VALUE_COLUMN_NAME, parts.get(i));
                this.database.insertWithOnConflict(SERVER_CACHE_TABLE, null, values, 5);
            }
            return;
        }
        ContentValues values2 = new ContentValues();
        values2.put("path", pathToKey(path));
        values2.put(VALUE_COLUMN_NAME, serialized);
        this.database.insertWithOnConflict(SERVER_CACHE_TABLE, null, values2, 5);
    }

    private Node loadNested(Path path) {
        Cursor cursor;
        Cursor cursor2;
        long loadingDuration;
        Node savedNode;
        Path savedPath;
        Map<Path, Node> priorities;
        Path savedPath2;
        List<String> pathStrings = new ArrayList<>();
        List<byte[]> payloads = new ArrayList<>();
        long queryStart = System.currentTimeMillis();
        Cursor cursor3 = loadNestedQuery(path, new String[]{"path", VALUE_COLUMN_NAME});
        long queryDuration = System.currentTimeMillis() - queryStart;
        long loadingStart = System.currentTimeMillis();
        while (cursor3.moveToNext()) {
            try {
                try {
                    pathStrings.add(cursor3.getString(0));
                    payloads.add(cursor3.getBlob(1));
                } catch (Throwable th) {
                    th = th;
                    cursor = cursor3;
                    cursor.close();
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                cursor = cursor3;
            }
        }
        cursor3.close();
        long loadingDuration2 = System.currentTimeMillis() - loadingStart;
        long serializingStart = System.currentTimeMillis();
        Node node = EmptyNode.Empty();
        boolean sawDescendant = false;
        Map<Path, Node> priorities2 = new HashMap<>();
        int i = 0;
        while (true) {
            long loadingStart2 = loadingStart;
            if (i < payloads.size()) {
                if (pathStrings.get(i).endsWith(FIRST_PART_KEY)) {
                    String pathString = pathStrings.get(i);
                    cursor2 = cursor3;
                    Path savedPath3 = new Path(pathString.substring(0, pathString.length() - FIRST_PART_KEY.length()));
                    int splitNodeRunLength = splitNodeRunLength(savedPath3, pathStrings, i);
                    if (!this.logger.logsDebug()) {
                        savedPath2 = savedPath3;
                        loadingDuration = loadingDuration2;
                    } else {
                        savedPath2 = savedPath3;
                        loadingDuration = loadingDuration2;
                        this.logger.debug("Loading split node with " + splitNodeRunLength + " parts.", new Object[0]);
                    }
                    Node savedNode2 = deserializeNode(joinBytes(payloads.subList(i, i + splitNodeRunLength)));
                    i = (i + splitNodeRunLength) - 1;
                    savedNode = savedNode2;
                    savedPath = savedPath2;
                } else {
                    cursor2 = cursor3;
                    loadingDuration = loadingDuration2;
                    Node savedNode3 = deserializeNode(payloads.get(i));
                    savedNode = savedNode3;
                    savedPath = new Path(pathStrings.get(i));
                }
                if (savedPath.getBack() == null || !savedPath.getBack().isPriorityChildName()) {
                    priorities = priorities2;
                    if (savedPath.contains(path)) {
                        Utilities.hardAssert(!sawDescendant, "Descendants of path must come after ancestors.");
                        node = savedNode.getChild(Path.getRelative(savedPath, path));
                    } else if (path.contains(savedPath)) {
                        Path childPath = Path.getRelative(path, savedPath);
                        node = node.updateChild(childPath, savedNode);
                        sawDescendant = true;
                    } else {
                        throw new IllegalStateException(String.format("Loading an unrelated row with path %s for %s", savedPath, path));
                    }
                } else {
                    priorities = priorities2;
                    priorities.put(savedPath, savedNode);
                }
                i++;
                priorities2 = priorities;
                loadingStart = loadingStart2;
                cursor3 = cursor2;
                loadingDuration2 = loadingDuration;
            } else {
                long loadingDuration3 = loadingDuration2;
                for (Map.Entry<Path, Node> entry : priorities2.entrySet()) {
                    Path priorityPath = entry.getKey();
                    node = node.updateChild(Path.getRelative(path, priorityPath), entry.getValue());
                }
                long serializeDuration = System.currentTimeMillis() - serializingStart;
                long duration = System.currentTimeMillis() - queryStart;
                if (this.logger.logsDebug()) {
                    this.logger.debug(String.format(Locale.US, "Loaded a total of %d rows for a total of %d nodes at %s in %dms (Query: %dms, Loading: %dms, Serializing: %dms)", Integer.valueOf(payloads.size()), Integer.valueOf(NodeSizeEstimator.nodeCount(node)), path, Long.valueOf(duration), Long.valueOf(queryDuration), Long.valueOf(loadingDuration3), Long.valueOf(serializeDuration)), new Object[0]);
                }
                return node;
            }
        }
    }

    private int splitNodeRunLength(Path path, List<String> pathStrings, int startPosition) {
        int endPosition = startPosition + 1;
        String pathPrefix = pathToKey(path);
        if (!pathStrings.get(startPosition).startsWith(pathPrefix)) {
            throw new IllegalStateException("Extracting split nodes needs to start with path prefix");
        }
        while (endPosition < pathStrings.size() && pathStrings.get(endPosition).equals(partKey(path, endPosition - startPosition))) {
            endPosition++;
        }
        if (endPosition < pathStrings.size()) {
            if (pathStrings.get(endPosition).startsWith(pathPrefix + PART_KEY_PREFIX)) {
                throw new IllegalStateException("Run did not finish with all parts");
            }
        }
        return endPosition - startPosition;
    }

    private Cursor loadNestedQuery(Path path, String[] columns) {
        String pathPrefixStart = pathToKey(path);
        String pathPrefixEnd = pathPrefixStartToPrefixEnd(pathPrefixStart);
        String[] arguments = new String[path.size() + 3];
        String whereClause = buildAncestorWhereClause(path, arguments);
        String whereClause2 = whereClause + " OR (path > ? AND path < ?)";
        arguments[path.size() + 1] = pathPrefixStart;
        arguments[path.size() + 2] = pathPrefixEnd;
        return this.database.query(SERVER_CACHE_TABLE, columns, whereClause2, arguments, null, null, "path");
    }

    private static String pathToKey(Path path) {
        if (path.isEmpty()) {
            return "/";
        }
        return path.toString() + "/";
    }

    private static String pathPrefixStartToPrefixEnd(String prefix) {
        Utilities.hardAssert(prefix.endsWith("/"), "Path keys must end with a '/'");
        return prefix.substring(0, prefix.length() - 1) + '0';
    }

    private static String buildAncestorWhereClause(Path path, String[] arguments) {
        Utilities.hardAssert(arguments.length >= path.size() + 1);
        int count = 0;
        StringBuilder whereClause = new StringBuilder("(");
        while (!path.isEmpty()) {
            whereClause.append("path");
            whereClause.append(" = ? OR ");
            arguments[count] = pathToKey(path);
            path = path.getParent();
            count++;
        }
        whereClause.append("path");
        whereClause.append(" = ?)");
        arguments[count] = pathToKey(Path.getEmptyPath());
        return whereClause.toString();
    }

    private int removeNested(String table, Path path) {
        String pathPrefixStart = pathToKey(path);
        String pathPrefixEnd = pathPrefixStartToPrefixEnd(pathPrefixStart);
        return this.database.delete(table, "path >= ? AND path < ?", new String[]{pathPrefixStart, pathPrefixEnd});
    }

    private static List<byte[]> splitBytes(byte[] bytes, int size) {
        int parts = ((bytes.length - 1) / size) + 1;
        List<byte[]> partList = new ArrayList<>(parts);
        for (int i = 0; i < parts; i++) {
            int length = Math.min(size, bytes.length - (i * size));
            byte[] part = new byte[length];
            System.arraycopy(bytes, i * size, part, 0, length);
            partList.add(part);
        }
        return partList;
    }

    private byte[] joinBytes(List<byte[]> payloads) {
        int totalSize = 0;
        Iterator<byte[]> it = payloads.iterator();
        while (it.hasNext()) {
            totalSize += it.next().length;
        }
        byte[] buffer = new byte[totalSize];
        int currentBytePosition = 0;
        for (byte[] payload : payloads) {
            System.arraycopy(payload, 0, buffer, currentBytePosition, payload.length);
            currentBytePosition += payload.length;
        }
        return buffer;
    }

    private byte[] serializeObject(Object object) {
        try {
            return JsonMapper.serializeJsonValue(object).getBytes(UTF8_CHARSET);
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize leaf node", e);
        }
    }

    private Node deserializeNode(byte[] value) {
        try {
            Object o = JsonMapper.parseJsonValue(new String(value, UTF8_CHARSET));
            return NodeUtilities.NodeFromJSON(o);
        } catch (IOException e) {
            String stringValue = new String(value, UTF8_CHARSET);
            throw new RuntimeException("Could not deserialize node: " + stringValue, e);
        }
    }

    private String commaSeparatedList(Collection<Long> items) {
        StringBuilder list = new StringBuilder();
        boolean first = true;
        Iterator<Long> it = items.iterator();
        while (it.hasNext()) {
            long item = it.next().longValue();
            if (!first) {
                list.append(",");
            }
            first = false;
            list.append(item);
        }
        return list.toString();
    }
}
