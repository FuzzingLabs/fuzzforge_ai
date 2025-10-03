package com.google.firebase.database.connection;

import com.google.firebase.database.logging.Logger;
import java.util.concurrent.ScheduledExecutorService;

/* loaded from: classes.dex */
public class ConnectionContext {
    private final ConnectionTokenProvider appCheckTokenProvider;
    private final String applicationId;
    private final ConnectionTokenProvider authTokenProvider;
    private final String clientSdkVersion;
    private final ScheduledExecutorService executorService;
    private final Logger logger;
    private final boolean persistenceEnabled;
    private final String sslCacheDirectory;
    private final String userAgent;

    public ConnectionContext(Logger logger, ConnectionTokenProvider authTokenProvider, ConnectionTokenProvider appCheckTokenProvider, ScheduledExecutorService executorService, boolean persistenceEnabled, String clientSdkVersion, String userAgent, String applicationId, String sslCacheDirectory) {
        this.logger = logger;
        this.authTokenProvider = authTokenProvider;
        this.appCheckTokenProvider = appCheckTokenProvider;
        this.executorService = executorService;
        this.persistenceEnabled = persistenceEnabled;
        this.clientSdkVersion = clientSdkVersion;
        this.userAgent = userAgent;
        this.applicationId = applicationId;
        this.sslCacheDirectory = sslCacheDirectory;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public ConnectionTokenProvider getAuthTokenProvider() {
        return this.authTokenProvider;
    }

    public ConnectionTokenProvider getAppCheckTokenProvider() {
        return this.appCheckTokenProvider;
    }

    public ScheduledExecutorService getExecutorService() {
        return this.executorService;
    }

    public boolean isPersistenceEnabled() {
        return this.persistenceEnabled;
    }

    public String getClientSdkVersion() {
        return this.clientSdkVersion;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public String getSslCacheDirectory() {
        return this.sslCacheDirectory;
    }

    public String getApplicationId() {
        return this.applicationId;
    }
}
