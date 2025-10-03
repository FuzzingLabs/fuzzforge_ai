package com.google.firebase.heartbeatinfo;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class HeartBeatInfoStorage {
    private static final String GLOBAL = "fire-global";
    private static final int HEART_BEAT_COUNT_LIMIT = 200;
    private static final String HEART_BEAT_COUNT_TAG = "fire-count";
    private static final String PREFERENCES_NAME = "FirebaseAppHeartBeat";
    private static final String STORAGE_PREFERENCES_NAME = "FirebaseAppHeartBeatStorage";
    private final SharedPreferences heartBeatSharedPreferences;
    private final SharedPreferences sharedPreferences;
    private static HeartBeatInfoStorage instance = null;
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd/MM/yyyy z");

    private HeartBeatInfoStorage(Context applicationContext) {
        this.sharedPreferences = applicationContext.getSharedPreferences(PREFERENCES_NAME, 0);
        this.heartBeatSharedPreferences = applicationContext.getSharedPreferences(STORAGE_PREFERENCES_NAME, 0);
    }

    HeartBeatInfoStorage(SharedPreferences preferences, SharedPreferences heartBeatSharedPreferences) {
        this.sharedPreferences = preferences;
        this.heartBeatSharedPreferences = heartBeatSharedPreferences;
    }

    int getHeartBeatCount() {
        return (int) this.sharedPreferences.getLong(HEART_BEAT_COUNT_TAG, 0L);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static synchronized HeartBeatInfoStorage getInstance(Context applicationContext) {
        HeartBeatInfoStorage heartBeatInfoStorage;
        synchronized (HeartBeatInfoStorage.class) {
            if (instance == null) {
                instance = new HeartBeatInfoStorage(applicationContext);
            }
            heartBeatInfoStorage = instance;
        }
        return heartBeatInfoStorage;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void storeHeartBeatInformation(String heartBeatTag, long millis) {
        long heartBeatCount = this.sharedPreferences.getLong(HEART_BEAT_COUNT_TAG, 0L);
        this.heartBeatSharedPreferences.edit().putString(String.valueOf(millis), heartBeatTag).apply();
        this.sharedPreferences.edit().putLong(HEART_BEAT_COUNT_TAG, heartBeatCount + 1).apply();
        if (heartBeatCount + 1 > 200) {
            cleanUpStoredHeartBeats();
        }
    }

    private synchronized void cleanUpStoredHeartBeats() {
        long heartBeatCount = this.sharedPreferences.getLong(HEART_BEAT_COUNT_TAG, 0L);
        ArrayList<Long> timestampList = new ArrayList<>();
        for (Map.Entry<String, ?> entry : this.heartBeatSharedPreferences.getAll().entrySet()) {
            timestampList.add(Long.valueOf(Long.parseLong(entry.getKey())));
        }
        Collections.sort(timestampList);
        Iterator<Long> it = timestampList.iterator();
        while (it.hasNext()) {
            Long millis = it.next();
            this.heartBeatSharedPreferences.edit().remove(String.valueOf(millis)).apply();
            this.sharedPreferences.edit().putLong(HEART_BEAT_COUNT_TAG, heartBeatCount - 1).apply();
            heartBeatCount--;
            if (heartBeatCount <= 100) {
                return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized long getLastGlobalHeartBeat() {
        return this.sharedPreferences.getLong(GLOBAL, -1L);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void updateGlobalHeartBeat(long millis) {
        this.sharedPreferences.edit().putLong(GLOBAL, millis).apply();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized List<SdkHeartBeatResult> getStoredHeartBeats(boolean shouldClear) {
        ArrayList<SdkHeartBeatResult> sdkHeartBeatResults;
        sdkHeartBeatResults = new ArrayList<>();
        for (Map.Entry<String, ?> entry : this.heartBeatSharedPreferences.getAll().entrySet()) {
            long millis = Long.parseLong(entry.getKey());
            String sdkName = (String) entry.getValue();
            sdkHeartBeatResults.add(SdkHeartBeatResult.create(sdkName, millis));
        }
        Collections.sort(sdkHeartBeatResults);
        if (shouldClear) {
            clearStoredHeartBeats();
        }
        return sdkHeartBeatResults;
    }

    synchronized void clearStoredHeartBeats() {
        this.heartBeatSharedPreferences.edit().clear().apply();
        this.sharedPreferences.edit().remove(HEART_BEAT_COUNT_TAG).apply();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static boolean isSameDateUtc(long base, long target) {
        Date baseDate = new Date(base);
        Date targetDate = new Date(target);
        SimpleDateFormat simpleDateFormat = FORMATTER;
        return !simpleDateFormat.format(baseDate).equals(simpleDateFormat.format(targetDate));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized boolean shouldSendSdkHeartBeat(String heartBeatTag, long millis) {
        if (this.sharedPreferences.contains(heartBeatTag)) {
            if (isSameDateUtc(this.sharedPreferences.getLong(heartBeatTag, -1L), millis)) {
                this.sharedPreferences.edit().putLong(heartBeatTag, millis).apply();
                return true;
            }
            return false;
        }
        this.sharedPreferences.edit().putLong(heartBeatTag, millis).apply();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized boolean shouldSendGlobalHeartBeat(long millis) {
        return shouldSendSdkHeartBeat(GLOBAL, millis);
    }
}
