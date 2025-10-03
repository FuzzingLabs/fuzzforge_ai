package com.google.firebase.database.core;

import com.google.firebase.database.connection.ConnectionContext;
import com.google.firebase.database.connection.HostInfo;
import com.google.firebase.database.connection.PersistentConnection;
import com.google.firebase.database.core.persistence.PersistenceManager;
import com.google.firebase.database.logging.Logger;
import java.io.File;
import java.util.List;

/* loaded from: classes.dex */
public interface Platform {
    PersistenceManager createPersistenceManager(Context context, String str);

    String getPlatformVersion();

    File getSSLCacheDirectory();

    String getUserAgent(Context context);

    EventTarget newEventTarget(Context context);

    Logger newLogger(Context context, Logger.Level level, List<String> list);

    PersistentConnection newPersistentConnection(Context context, ConnectionContext connectionContext, HostInfo hostInfo, PersistentConnection.Delegate delegate);

    RunLoop newRunLoop(Context context);
}
