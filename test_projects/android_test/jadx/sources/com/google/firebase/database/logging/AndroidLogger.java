package com.google.firebase.database.logging;

import android.util.Log;
import com.google.firebase.database.logging.Logger;
import java.util.List;

/* loaded from: classes.dex */
public class AndroidLogger extends DefaultLogger {
    public AndroidLogger(Logger.Level level, List<String> enabledComponents) {
        super(level, enabledComponents);
    }

    @Override // com.google.firebase.database.logging.DefaultLogger
    protected String buildLogMessage(Logger.Level level, String tag, String message, long msTimestamp) {
        return message;
    }

    @Override // com.google.firebase.database.logging.DefaultLogger
    protected void error(String tag, String toLog) {
        Log.e(tag, toLog);
    }

    @Override // com.google.firebase.database.logging.DefaultLogger
    protected void warn(String tag, String toLog) {
        Log.w(tag, toLog);
    }

    @Override // com.google.firebase.database.logging.DefaultLogger
    protected void info(String tag, String toLog) {
        Log.i(tag, toLog);
    }

    @Override // com.google.firebase.database.logging.DefaultLogger
    protected void debug(String tag, String toLog) {
        Log.d(tag, toLog);
    }
}
