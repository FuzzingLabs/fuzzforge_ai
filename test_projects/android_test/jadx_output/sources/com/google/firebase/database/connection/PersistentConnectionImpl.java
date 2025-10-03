package com.google.firebase.database.connection;

import androidx.core.app.NotificationCompat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.connection.Connection;
import com.google.firebase.database.connection.ConnectionTokenProvider;
import com.google.firebase.database.connection.PersistentConnection;
import com.google.firebase.database.connection.util.RetryHelper;
import com.google.firebase.database.logging.LogWrapper;
import com.google.firebase.database.util.GAuthToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import kotlin.text.Typography;

/* loaded from: classes.dex */
public class PersistentConnectionImpl implements Connection.Delegate, PersistentConnection {
    private static final long GET_CONNECT_TIMEOUT = 3000;
    private static final String IDLE_INTERRUPT_REASON = "connection_idle";
    private static final long IDLE_TIMEOUT = 60000;
    private static final String INVALID_APP_CHECK_TOKEN = "Invalid appcheck token";
    private static final long INVALID_TOKEN_THRESHOLD = 3;
    private static final String REQUEST_ACTION = "a";
    private static final String REQUEST_ACTION_APPCHECK = "appcheck";
    private static final String REQUEST_ACTION_AUTH = "auth";
    private static final String REQUEST_ACTION_GAUTH = "gauth";
    private static final String REQUEST_ACTION_GET = "g";
    private static final String REQUEST_ACTION_MERGE = "m";
    private static final String REQUEST_ACTION_ONDISCONNECT_CANCEL = "oc";
    private static final String REQUEST_ACTION_ONDISCONNECT_MERGE = "om";
    private static final String REQUEST_ACTION_ONDISCONNECT_PUT = "o";
    private static final String REQUEST_ACTION_PUT = "p";
    private static final String REQUEST_ACTION_QUERY = "q";
    private static final String REQUEST_ACTION_QUERY_UNLISTEN = "n";
    private static final String REQUEST_ACTION_STATS = "s";
    private static final String REQUEST_ACTION_UNAPPCHECK = "unappcheck";
    private static final String REQUEST_ACTION_UNAUTH = "unauth";
    private static final String REQUEST_APPCHECK_TOKEN = "token";
    private static final String REQUEST_AUTHVAR = "authvar";
    private static final String REQUEST_COMPOUND_HASH = "ch";
    private static final String REQUEST_COMPOUND_HASH_HASHES = "hs";
    private static final String REQUEST_COMPOUND_HASH_PATHS = "ps";
    private static final String REQUEST_COUNTERS = "c";
    private static final String REQUEST_CREDENTIAL = "cred";
    private static final String REQUEST_DATA_HASH = "h";
    private static final String REQUEST_DATA_PAYLOAD = "d";
    private static final String REQUEST_ERROR = "error";
    private static final String REQUEST_NUMBER = "r";
    private static final String REQUEST_PATH = "p";
    private static final String REQUEST_PAYLOAD = "b";
    private static final String REQUEST_QUERIES = "q";
    private static final String REQUEST_STATUS = "s";
    private static final String REQUEST_TAG = "t";
    private static final String RESPONSE_FOR_REQUEST = "b";
    private static final String SERVER_ASYNC_ACTION = "a";
    private static final String SERVER_ASYNC_APP_CHECK_REVOKED = "apc";
    private static final String SERVER_ASYNC_AUTH_REVOKED = "ac";
    private static final String SERVER_ASYNC_DATA_MERGE = "m";
    private static final String SERVER_ASYNC_DATA_RANGE_MERGE = "rm";
    private static final String SERVER_ASYNC_DATA_UPDATE = "d";
    private static final String SERVER_ASYNC_LISTEN_CANCELLED = "c";
    private static final String SERVER_ASYNC_PAYLOAD = "b";
    private static final String SERVER_ASYNC_SECURITY_DEBUG = "sd";
    private static final String SERVER_DATA_END_PATH = "e";
    private static final String SERVER_DATA_RANGE_MERGE = "m";
    private static final String SERVER_DATA_START_PATH = "s";
    private static final String SERVER_DATA_TAG = "t";
    private static final String SERVER_DATA_UPDATE_BODY = "d";
    private static final String SERVER_DATA_UPDATE_PATH = "p";
    private static final String SERVER_DATA_WARNINGS = "w";
    private static final String SERVER_KILL_INTERRUPT_REASON = "server_kill";
    private static final String SERVER_RESPONSE_DATA = "d";
    private static final long SUCCESSFUL_CONNECTION_ESTABLISHED_DELAY = 30000;
    private static final String TOKEN_REFRESH_INTERRUPT_REASON = "token_refresh";
    private static long connectionIds = 0;
    private String appCheckToken;
    private final ConnectionTokenProvider appCheckTokenProvider;
    private String authToken;
    private final ConnectionTokenProvider authTokenProvider;
    private String cachedHost;
    private final ConnectionContext context;
    private final PersistentConnection.Delegate delegate;
    private final ScheduledExecutorService executorService;
    private boolean forceAppCheckTokenRefresh;
    private boolean forceAuthTokenRefresh;
    private boolean hasOnDisconnects;
    private final HostInfo hostInfo;
    private long lastConnectionEstablishedTime;
    private String lastSessionId;
    private long lastWriteTimestamp;
    private Map<QuerySpec, OutstandingListen> listens;
    private final LogWrapper logger;
    private List<OutstandingDisconnect> onDisconnectRequestQueue;
    private Map<Long, OutstandingGet> outstandingGets;
    private Map<Long, OutstandingPut> outstandingPuts;
    private Connection realtime;
    private Map<Long, ConnectionRequestCallback> requestCBHash;
    private final RetryHelper retryHelper;
    private HashSet<String> interruptReasons = new HashSet<>();
    private boolean firstConnection = true;
    private ConnectionState connectionState = ConnectionState.Disconnected;
    private long writeCounter = 0;
    private long readCounter = 0;
    private long requestCounter = 0;
    private long currentGetTokenAttempt = 0;
    private int invalidAuthTokenCount = 0;
    private int invalidAppCheckTokenCount = 0;
    private ScheduledFuture<?> inactivityTimer = null;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public interface ConnectionRequestCallback {
        void onResponse(Map<String, Object> map);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public enum ConnectionState {
        Disconnected,
        GettingToken,
        Connecting,
        Authenticating,
        Connected
    }

    static /* synthetic */ int access$1008(PersistentConnectionImpl x0) {
        int i = x0.invalidAuthTokenCount;
        x0.invalidAuthTokenCount = i + 1;
        return i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class QuerySpec {
        private final List<String> path;
        private final Map<String, Object> queryParams;

        public QuerySpec(List<String> path, Map<String, Object> queryParams) {
            this.path = path;
            this.queryParams = queryParams;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof QuerySpec)) {
                return false;
            }
            QuerySpec that = (QuerySpec) o;
            if (this.path.equals(that.path)) {
                return this.queryParams.equals(that.queryParams);
            }
            return false;
        }

        public int hashCode() {
            int result = this.path.hashCode();
            return (result * 31) + this.queryParams.hashCode();
        }

        public String toString() {
            return ConnectionUtils.pathToString(this.path) + " (params: " + this.queryParams + ")";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class OutstandingListen {
        private final ListenHashProvider hashFunction;
        private final QuerySpec query;
        private final RequestResultCallback resultCallback;
        private final Long tag;

        private OutstandingListen(RequestResultCallback callback, QuerySpec query, Long tag, ListenHashProvider hashFunction) {
            this.resultCallback = callback;
            this.query = query;
            this.hashFunction = hashFunction;
            this.tag = tag;
        }

        public QuerySpec getQuery() {
            return this.query;
        }

        public Long getTag() {
            return this.tag;
        }

        public ListenHashProvider getHashFunction() {
            return this.hashFunction;
        }

        public String toString() {
            return this.query.toString() + " (Tag: " + this.tag + ")";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class OutstandingGet {
        private final ConnectionRequestCallback onComplete;
        private final Map<String, Object> request;
        private boolean sent;

        private OutstandingGet(String action, Map<String, Object> request, ConnectionRequestCallback onComplete) {
            this.request = request;
            this.onComplete = onComplete;
            this.sent = false;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public ConnectionRequestCallback getOnComplete() {
            return this.onComplete;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public Map<String, Object> getRequest() {
            return this.request;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean markSent() {
            if (this.sent) {
                return false;
            }
            this.sent = true;
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class OutstandingPut {
        private String action;
        private RequestResultCallback onComplete;
        private Map<String, Object> request;
        private boolean sent;

        private OutstandingPut(String action, Map<String, Object> request, RequestResultCallback onComplete) {
            this.action = action;
            this.request = request;
            this.onComplete = onComplete;
        }

        public String getAction() {
            return this.action;
        }

        public Map<String, Object> getRequest() {
            return this.request;
        }

        public RequestResultCallback getOnComplete() {
            return this.onComplete;
        }

        public void markSent() {
            this.sent = true;
        }

        public boolean wasSent() {
            return this.sent;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class OutstandingDisconnect {
        private final String action;
        private final Object data;
        private final RequestResultCallback onComplete;
        private final List<String> path;

        private OutstandingDisconnect(String action, List<String> path, Object data, RequestResultCallback onComplete) {
            this.action = action;
            this.path = path;
            this.data = data;
            this.onComplete = onComplete;
        }

        public String getAction() {
            return this.action;
        }

        public List<String> getPath() {
            return this.path;
        }

        public Object getData() {
            return this.data;
        }

        public RequestResultCallback getOnComplete() {
            return this.onComplete;
        }
    }

    public PersistentConnectionImpl(ConnectionContext context, HostInfo info, PersistentConnection.Delegate delegate) {
        this.delegate = delegate;
        this.context = context;
        ScheduledExecutorService executorService = context.getExecutorService();
        this.executorService = executorService;
        this.authTokenProvider = context.getAuthTokenProvider();
        this.appCheckTokenProvider = context.getAppCheckTokenProvider();
        this.hostInfo = info;
        this.listens = new HashMap();
        this.requestCBHash = new HashMap();
        this.outstandingPuts = new HashMap();
        this.outstandingGets = new ConcurrentHashMap();
        this.onDisconnectRequestQueue = new ArrayList();
        this.retryHelper = new RetryHelper.Builder(executorService, context.getLogger(), "ConnectionRetryHelper").withMinDelayAfterFailure(1000L).withRetryExponent(1.3d).withMaxDelay(SUCCESSFUL_CONNECTION_ESTABLISHED_DELAY).withJitterFactor(0.7d).build();
        long connId = connectionIds;
        connectionIds = 1 + connId;
        this.logger = new LogWrapper(context.getLogger(), "PersistentConnection", "pc_" + connId);
        this.lastSessionId = null;
        doIdleCheck();
    }

    @Override // com.google.firebase.database.connection.Connection.Delegate
    public void onReady(long timestamp, String sessionId) {
        if (this.logger.logsDebug()) {
            this.logger.debug("onReady", new Object[0]);
        }
        this.lastConnectionEstablishedTime = System.currentTimeMillis();
        handleTimestamp(timestamp);
        if (this.firstConnection) {
            sendConnectStats();
        }
        restoreTokens();
        this.firstConnection = false;
        this.lastSessionId = sessionId;
        this.delegate.onConnect();
    }

    @Override // com.google.firebase.database.connection.Connection.Delegate
    public void onCacheHost(String host) {
        this.cachedHost = host;
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void listen(List<String> path, Map<String, Object> queryParams, ListenHashProvider currentHashFn, Long tag, RequestResultCallback listener) {
        QuerySpec query = new QuerySpec(path, queryParams);
        if (this.logger.logsDebug()) {
            this.logger.debug("Listening on " + query, new Object[0]);
        }
        ConnectionUtils.hardAssert(!this.listens.containsKey(query), "listen() called twice for same QuerySpec.", new Object[0]);
        if (this.logger.logsDebug()) {
            this.logger.debug("Adding listen query: " + query, new Object[0]);
        }
        OutstandingListen outstandingListen = new OutstandingListen(listener, query, tag, currentHashFn);
        this.listens.put(query, outstandingListen);
        if (connected()) {
            sendListen(outstandingListen);
        }
        doIdleCheck();
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public Task<Object> get(List<String> path, Map<String, Object> queryParams) {
        final QuerySpec query = new QuerySpec(path, queryParams);
        final TaskCompletionSource<Object> source = new TaskCompletionSource<>();
        final long readId = this.readCounter;
        this.readCounter = 1 + readId;
        Map<String, Object> request = new HashMap<>();
        request.put("p", ConnectionUtils.pathToString(query.path));
        request.put("q", query.queryParams);
        final OutstandingGet outstandingGet = new OutstandingGet(REQUEST_ACTION_GET, request, new ConnectionRequestCallback() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl$$ExternalSyntheticLambda2
            @Override // com.google.firebase.database.connection.PersistentConnectionImpl.ConnectionRequestCallback
            public final void onResponse(Map map) {
                PersistentConnectionImpl.this.m227xb452292f(query, source, map);
            }
        });
        this.outstandingGets.put(Long.valueOf(readId), outstandingGet);
        if (!connected()) {
            this.executorService.schedule(new Runnable() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() {
                    PersistentConnectionImpl.this.m228x1e81b14e(outstandingGet, readId, source);
                }
            }, GET_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        if (canSendReads()) {
            sendGet(Long.valueOf(readId));
        }
        doIdleCheck();
        return source.getTask();
    }

    /* renamed from: lambda$get$0$com-google-firebase-database-connection-PersistentConnectionImpl */
    public /* synthetic */ void m227xb452292f(QuerySpec query, TaskCompletionSource source, Map response) {
        String status = (String) response.get("s");
        if (status.equals("ok")) {
            Object body = response.get("d");
            this.delegate.onDataUpdate(query.path, body, false, null);
            source.setResult(body);
            return;
        }
        source.setException(new Exception((String) response.get("d")));
    }

    /* renamed from: lambda$get$1$com-google-firebase-database-connection-PersistentConnectionImpl */
    public /* synthetic */ void m228x1e81b14e(OutstandingGet outstandingGet, long readId, TaskCompletionSource source) {
        if (!outstandingGet.markSent()) {
            return;
        }
        if (this.logger.logsDebug()) {
            this.logger.debug("get " + readId + " timed out waiting for connection", new Object[0]);
        }
        this.outstandingGets.remove(Long.valueOf(readId));
        source.setException(new Exception("Client is offline"));
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void initialize() {
        tryScheduleReconnect();
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void shutdown() {
        interrupt("shutdown");
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void put(List<String> path, Object data, RequestResultCallback onComplete) {
        putInternal("p", path, data, null, onComplete);
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void compareAndPut(List<String> path, Object data, String hash, RequestResultCallback onComplete) {
        putInternal("p", path, data, hash, onComplete);
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void merge(List<String> path, Map<String, Object> data, RequestResultCallback onComplete) {
        putInternal("m", path, data, null, onComplete);
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void purgeOutstandingWrites() {
        for (OutstandingPut put : this.outstandingPuts.values()) {
            if (put.onComplete != null) {
                put.onComplete.onRequestResult("write_canceled", null);
            }
        }
        for (OutstandingDisconnect onDisconnect : this.onDisconnectRequestQueue) {
            if (onDisconnect.onComplete != null) {
                onDisconnect.onComplete.onRequestResult("write_canceled", null);
            }
        }
        this.outstandingPuts.clear();
        this.onDisconnectRequestQueue.clear();
        if (!connected()) {
            this.hasOnDisconnects = false;
        }
        doIdleCheck();
    }

    @Override // com.google.firebase.database.connection.Connection.Delegate
    public void onDataMessage(Map<String, Object> message) {
        if (message.containsKey(REQUEST_NUMBER)) {
            long rn = ((Integer) message.get(REQUEST_NUMBER)).intValue();
            ConnectionRequestCallback responseListener = this.requestCBHash.remove(Long.valueOf(rn));
            if (responseListener != null) {
                Map<String, Object> response = (Map) message.get("b");
                responseListener.onResponse(response);
                return;
            }
            return;
        }
        if (!message.containsKey(REQUEST_ERROR)) {
            if (message.containsKey("a")) {
                String action = (String) message.get("a");
                Map<String, Object> body = (Map) message.get("b");
                onDataPush(action, body);
            } else if (this.logger.logsDebug()) {
                this.logger.debug("Ignoring unknown message: " + message, new Object[0]);
            }
        }
    }

    @Override // com.google.firebase.database.connection.Connection.Delegate
    public void onDisconnect(Connection.DisconnectReason reason) {
        boolean lastConnectionWasSuccessful;
        if (this.logger.logsDebug()) {
            this.logger.debug("Got on disconnect due to " + reason.name(), new Object[0]);
        }
        this.connectionState = ConnectionState.Disconnected;
        this.realtime = null;
        this.hasOnDisconnects = false;
        this.requestCBHash.clear();
        cancelSentTransactions();
        if (shouldReconnect()) {
            long currentTimeMillis = System.currentTimeMillis();
            long j = this.lastConnectionEstablishedTime;
            long timeSinceLastConnectSucceeded = currentTimeMillis - j;
            if (j > 0) {
                lastConnectionWasSuccessful = timeSinceLastConnectSucceeded > SUCCESSFUL_CONNECTION_ESTABLISHED_DELAY;
            } else {
                lastConnectionWasSuccessful = false;
            }
            if (reason == Connection.DisconnectReason.SERVER_RESET || lastConnectionWasSuccessful) {
                this.retryHelper.signalSuccess();
            }
            tryScheduleReconnect();
        }
        this.lastConnectionEstablishedTime = 0L;
        this.delegate.onDisconnect();
    }

    @Override // com.google.firebase.database.connection.Connection.Delegate
    public void onKill(String reason) {
        if (reason.equals(INVALID_APP_CHECK_TOKEN)) {
            int i = this.invalidAppCheckTokenCount;
            if (i < 3) {
                this.invalidAppCheckTokenCount = i + 1;
                this.logger.warn("Detected invalid AppCheck token. Reconnecting (" + (3 - this.invalidAppCheckTokenCount) + " attempts remaining)");
                return;
            }
        }
        this.logger.warn("Firebase Database connection was forcefully killed by the server. Will not attempt reconnect. Reason: " + reason);
        interrupt(SERVER_KILL_INTERRUPT_REASON);
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void unlisten(List<String> path, Map<String, Object> queryParams) {
        QuerySpec query = new QuerySpec(path, queryParams);
        if (this.logger.logsDebug()) {
            this.logger.debug("unlistening on " + query, new Object[0]);
        }
        OutstandingListen listen = removeListen(query);
        if (listen != null && connected()) {
            sendUnlisten(listen);
        }
        doIdleCheck();
    }

    private boolean connected() {
        return this.connectionState == ConnectionState.Authenticating || this.connectionState == ConnectionState.Connected;
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void onDisconnectPut(List<String> path, Object data, RequestResultCallback onComplete) {
        this.hasOnDisconnects = true;
        if (canSendWrites()) {
            sendOnDisconnect(REQUEST_ACTION_ONDISCONNECT_PUT, path, data, onComplete);
        } else {
            this.onDisconnectRequestQueue.add(new OutstandingDisconnect(REQUEST_ACTION_ONDISCONNECT_PUT, path, data, onComplete));
        }
        doIdleCheck();
    }

    private boolean canSendWrites() {
        return this.connectionState == ConnectionState.Connected;
    }

    private boolean canSendReads() {
        return this.connectionState == ConnectionState.Connected;
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void onDisconnectMerge(List<String> path, Map<String, Object> updates, RequestResultCallback onComplete) {
        this.hasOnDisconnects = true;
        if (canSendWrites()) {
            sendOnDisconnect(REQUEST_ACTION_ONDISCONNECT_MERGE, path, updates, onComplete);
        } else {
            this.onDisconnectRequestQueue.add(new OutstandingDisconnect(REQUEST_ACTION_ONDISCONNECT_MERGE, path, updates, onComplete));
        }
        doIdleCheck();
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void onDisconnectCancel(List<String> path, RequestResultCallback onComplete) {
        if (canSendWrites()) {
            sendOnDisconnect(REQUEST_ACTION_ONDISCONNECT_CANCEL, path, null, onComplete);
        } else {
            this.onDisconnectRequestQueue.add(new OutstandingDisconnect(REQUEST_ACTION_ONDISCONNECT_CANCEL, path, null, onComplete));
        }
        doIdleCheck();
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void interrupt(String reason) {
        if (this.logger.logsDebug()) {
            this.logger.debug("Connection interrupted for: " + reason, new Object[0]);
        }
        this.interruptReasons.add(reason);
        Connection connection = this.realtime;
        if (connection != null) {
            connection.close();
            this.realtime = null;
        } else {
            this.retryHelper.cancel();
            this.connectionState = ConnectionState.Disconnected;
        }
        this.retryHelper.signalSuccess();
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void resume(String reason) {
        if (this.logger.logsDebug()) {
            this.logger.debug("Connection no longer interrupted for: " + reason, new Object[0]);
        }
        this.interruptReasons.remove(reason);
        if (shouldReconnect() && this.connectionState == ConnectionState.Disconnected) {
            tryScheduleReconnect();
        }
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public boolean isInterrupted(String reason) {
        return this.interruptReasons.contains(reason);
    }

    boolean shouldReconnect() {
        return this.interruptReasons.size() == 0;
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void refreshAuthToken() {
        this.logger.debug("Auth token refresh requested", new Object[0]);
        interrupt(TOKEN_REFRESH_INTERRUPT_REASON);
        resume(TOKEN_REFRESH_INTERRUPT_REASON);
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void refreshAuthToken(String token) {
        this.logger.debug("Auth token refreshed.", new Object[0]);
        this.authToken = token;
        if (connected()) {
            if (token != null) {
                upgradeAuth();
            } else {
                sendUnauth();
            }
        }
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void refreshAppCheckToken() {
        this.logger.debug("App check token refresh requested", new Object[0]);
        interrupt(TOKEN_REFRESH_INTERRUPT_REASON);
        resume(TOKEN_REFRESH_INTERRUPT_REASON);
    }

    @Override // com.google.firebase.database.connection.PersistentConnection
    public void refreshAppCheckToken(String token) {
        this.logger.debug("App check token refreshed.", new Object[0]);
        this.appCheckToken = token;
        if (connected()) {
            if (token != null) {
                upgradeAppCheck();
            } else {
                sendUnAppCheck();
            }
        }
    }

    private void tryScheduleReconnect() {
        if (shouldReconnect()) {
            ConnectionUtils.hardAssert(this.connectionState == ConnectionState.Disconnected, "Not in disconnected state: %s", this.connectionState);
            final boolean forceAuthTokenRefresh = this.forceAuthTokenRefresh;
            final boolean forceAppCheckTokenRefresh = this.forceAppCheckTokenRefresh;
            this.logger.debug("Scheduling connection attempt", new Object[0]);
            this.forceAuthTokenRefresh = false;
            this.forceAppCheckTokenRefresh = false;
            this.retryHelper.retry(new Runnable() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    PersistentConnectionImpl.this.m232xcbe04342(forceAuthTokenRefresh, forceAppCheckTokenRefresh);
                }
            });
        }
    }

    /* renamed from: lambda$tryScheduleReconnect$4$com-google-firebase-database-connection-PersistentConnectionImpl */
    public /* synthetic */ void m232xcbe04342(boolean forceAuthTokenRefresh, boolean forceAppCheckTokenRefresh) {
        ConnectionUtils.hardAssert(this.connectionState == ConnectionState.Disconnected, "Not in disconnected state: %s", this.connectionState);
        this.connectionState = ConnectionState.GettingToken;
        this.currentGetTokenAttempt++;
        final long thisGetTokenAttempt = this.currentGetTokenAttempt;
        final Task<String> authToken = fetchAuthToken(forceAuthTokenRefresh);
        final Task<String> appCheckToken = fetchAppCheckToken(forceAppCheckTokenRefresh);
        Tasks.whenAll((Task<?>[]) new Task[]{authToken, appCheckToken}).addOnSuccessListener(this.executorService, new OnSuccessListener() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl$$ExternalSyntheticLambda1
            @Override // com.google.android.gms.tasks.OnSuccessListener
            public final void onSuccess(Object obj) {
                PersistentConnectionImpl.this.m230xf7813304(thisGetTokenAttempt, authToken, appCheckToken, (Void) obj);
            }
        }).addOnFailureListener(this.executorService, new OnFailureListener() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl$$ExternalSyntheticLambda0
            @Override // com.google.android.gms.tasks.OnFailureListener
            public final void onFailure(Exception exc) {
                PersistentConnectionImpl.this.m231x61b0bb23(thisGetTokenAttempt, exc);
            }
        });
    }

    /* renamed from: lambda$tryScheduleReconnect$2$com-google-firebase-database-connection-PersistentConnectionImpl */
    public /* synthetic */ void m230xf7813304(long thisGetTokenAttempt, Task authToken, Task appCheckToken, Void aVoid) {
        if (thisGetTokenAttempt == this.currentGetTokenAttempt) {
            if (this.connectionState == ConnectionState.GettingToken) {
                this.logger.debug("Successfully fetched token, opening connection", new Object[0]);
                openNetworkConnection((String) authToken.getResult(), (String) appCheckToken.getResult());
                return;
            } else {
                if (this.connectionState == ConnectionState.Disconnected) {
                    this.logger.debug("Not opening connection after token refresh, because connection was set to disconnected", new Object[0]);
                    return;
                }
                return;
            }
        }
        this.logger.debug("Ignoring getToken result, because this was not the latest attempt.", new Object[0]);
    }

    /* renamed from: lambda$tryScheduleReconnect$3$com-google-firebase-database-connection-PersistentConnectionImpl */
    public /* synthetic */ void m231x61b0bb23(long thisGetTokenAttempt, Exception error) {
        if (thisGetTokenAttempt == this.currentGetTokenAttempt) {
            this.connectionState = ConnectionState.Disconnected;
            this.logger.debug("Error fetching token: " + error, new Object[0]);
            tryScheduleReconnect();
            return;
        }
        this.logger.debug("Ignoring getToken error, because this was not the latest attempt.", new Object[0]);
    }

    private Task<String> fetchAuthToken(boolean forceAuthTokenRefresh) {
        final TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        this.logger.debug("Trying to fetch auth token", new Object[0]);
        this.authTokenProvider.getToken(forceAuthTokenRefresh, new ConnectionTokenProvider.GetTokenCallback() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl.1
            @Override // com.google.firebase.database.connection.ConnectionTokenProvider.GetTokenCallback
            public void onSuccess(String token) {
                taskCompletionSource.setResult(token);
            }

            @Override // com.google.firebase.database.connection.ConnectionTokenProvider.GetTokenCallback
            public void onError(String error) {
                taskCompletionSource.setException(new Exception(error));
            }
        });
        return taskCompletionSource.getTask();
    }

    private Task<String> fetchAppCheckToken(boolean forceAppCheckTokenRefresh) {
        final TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        this.logger.debug("Trying to fetch app check token", new Object[0]);
        this.appCheckTokenProvider.getToken(forceAppCheckTokenRefresh, new ConnectionTokenProvider.GetTokenCallback() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl.2
            @Override // com.google.firebase.database.connection.ConnectionTokenProvider.GetTokenCallback
            public void onSuccess(String token) {
                taskCompletionSource.setResult(token);
            }

            @Override // com.google.firebase.database.connection.ConnectionTokenProvider.GetTokenCallback
            public void onError(String error) {
                taskCompletionSource.setException(new Exception(error));
            }
        });
        return taskCompletionSource.getTask();
    }

    public void openNetworkConnection(String authToken, String appCheckToken) {
        ConnectionUtils.hardAssert(this.connectionState == ConnectionState.GettingToken, "Trying to open network connection while in the wrong state: %s", this.connectionState);
        if (authToken == null) {
            this.delegate.onConnectionStatus(false);
        }
        this.authToken = authToken;
        this.appCheckToken = appCheckToken;
        this.connectionState = ConnectionState.Connecting;
        Connection connection = new Connection(this.context, this.hostInfo, this.cachedHost, this, this.lastSessionId, appCheckToken);
        this.realtime = connection;
        connection.open();
    }

    private void sendOnDisconnect(String action, List<String> path, Object data, final RequestResultCallback onComplete) {
        Map<String, Object> request = new HashMap<>();
        request.put("p", ConnectionUtils.pathToString(path));
        request.put("d", data);
        sendAction(action, request, new ConnectionRequestCallback() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl.3
            @Override // com.google.firebase.database.connection.PersistentConnectionImpl.ConnectionRequestCallback
            public void onResponse(Map<String, Object> response) {
                String status = (String) response.get("s");
                String errorMessage = null;
                String errorCode = null;
                if (!status.equals("ok")) {
                    errorCode = status;
                    errorMessage = (String) response.get("d");
                }
                RequestResultCallback requestResultCallback = onComplete;
                if (requestResultCallback != null) {
                    requestResultCallback.onRequestResult(errorCode, errorMessage);
                }
            }
        });
    }

    private void cancelSentTransactions() {
        List<OutstandingPut> cancelledTransactionWrites = new ArrayList<>();
        Iterator<Map.Entry<Long, OutstandingPut>> iter = this.outstandingPuts.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Long, OutstandingPut> entry = iter.next();
            OutstandingPut put = entry.getValue();
            if (put.getRequest().containsKey(REQUEST_DATA_HASH) && put.wasSent()) {
                cancelledTransactionWrites.add(put);
                iter.remove();
            }
        }
        Iterator<OutstandingPut> it = cancelledTransactionWrites.iterator();
        while (it.hasNext()) {
            it.next().getOnComplete().onRequestResult("disconnected", null);
        }
    }

    private void sendUnlisten(OutstandingListen listen) {
        Map<String, Object> request = new HashMap<>();
        request.put("p", ConnectionUtils.pathToString(listen.query.path));
        Long tag = listen.getTag();
        if (tag != null) {
            request.put("q", listen.getQuery().queryParams);
            request.put("t", tag);
        }
        sendAction(REQUEST_ACTION_QUERY_UNLISTEN, request, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public OutstandingListen removeListen(QuerySpec query) {
        if (this.logger.logsDebug()) {
            this.logger.debug("removing query " + query, new Object[0]);
        }
        if (!this.listens.containsKey(query)) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Trying to remove listener for QuerySpec " + query + " but no listener exists.", new Object[0]);
                return null;
            }
            return null;
        }
        OutstandingListen oldListen = this.listens.get(query);
        this.listens.remove(query);
        doIdleCheck();
        return oldListen;
    }

    private Collection<OutstandingListen> removeListens(List<String> path) {
        if (this.logger.logsDebug()) {
            this.logger.debug("removing all listens at path " + path, new Object[0]);
        }
        List<OutstandingListen> removedListens = new ArrayList<>();
        for (Map.Entry<QuerySpec, OutstandingListen> entry : this.listens.entrySet()) {
            QuerySpec query = entry.getKey();
            OutstandingListen listen = entry.getValue();
            if (query.path.equals(path)) {
                removedListens.add(listen);
            }
        }
        for (OutstandingListen toRemove : removedListens) {
            this.listens.remove(toRemove.getQuery());
        }
        doIdleCheck();
        return removedListens;
    }

    private void onDataPush(String action, Map<String, Object> body) {
        if (this.logger.logsDebug()) {
            this.logger.debug("handleServerMessage: " + action + " " + body, new Object[0]);
        }
        if (action.equals("d") || action.equals("m")) {
            boolean isMerge = action.equals("m");
            String pathString = (String) body.get("p");
            Object payloadData = body.get("d");
            Long tagNumber = ConnectionUtils.longFromObject(body.get("t"));
            if (isMerge && (payloadData instanceof Map) && ((Map) payloadData).size() == 0) {
                if (this.logger.logsDebug()) {
                    this.logger.debug("ignoring empty merge for path " + pathString, new Object[0]);
                    return;
                }
                return;
            }
            List<String> path = ConnectionUtils.stringToPath(pathString);
            this.delegate.onDataUpdate(path, payloadData, isMerge, tagNumber);
            return;
        }
        if (!action.equals(SERVER_ASYNC_DATA_RANGE_MERGE)) {
            if (action.equals("c")) {
                List<String> path2 = ConnectionUtils.stringToPath((String) body.get("p"));
                onListenRevoked(path2);
                return;
            }
            if (action.equals(SERVER_ASYNC_AUTH_REVOKED)) {
                String status = (String) body.get("s");
                String reason = (String) body.get("d");
                onAuthRevoked(status, reason);
                return;
            }
            if (action.equals(SERVER_ASYNC_APP_CHECK_REVOKED)) {
                String status2 = (String) body.get("s");
                String reason2 = (String) body.get("d");
                onAppCheckRevoked(status2, reason2);
                return;
            } else {
                if (action.equals(SERVER_ASYNC_SECURITY_DEBUG)) {
                    onSecurityDebugPacket(body);
                    return;
                }
                if (this.logger.logsDebug()) {
                    this.logger.debug("Unrecognized action from server: " + action, new Object[0]);
                    return;
                }
                return;
            }
        }
        String pathString2 = (String) body.get("p");
        List<String> path3 = ConnectionUtils.stringToPath(pathString2);
        Object payloadData2 = body.get("d");
        Long tag = ConnectionUtils.longFromObject(body.get("t"));
        List<Map<String, Object>> ranges = (List) payloadData2;
        List<RangeMerge> rangeMerges = new ArrayList<>();
        Iterator<Map<String, Object>> it = ranges.iterator();
        while (it.hasNext()) {
            Map<String, Object> range = it.next();
            String startString = (String) range.get("s");
            String endString = (String) range.get(SERVER_DATA_END_PATH);
            List<String> list = null;
            List<String> start = startString != null ? ConnectionUtils.stringToPath(startString) : null;
            if (endString != null) {
                list = ConnectionUtils.stringToPath(endString);
            }
            List<String> end = list;
            Object update = range.get("m");
            rangeMerges.add(new RangeMerge(start, end, update));
            it = it;
            payloadData2 = payloadData2;
            ranges = ranges;
        }
        if (rangeMerges.isEmpty()) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Ignoring empty range merge for path " + pathString2, new Object[0]);
                return;
            }
            return;
        }
        this.delegate.onRangeMergeUpdate(path3, rangeMerges, tag);
    }

    private void onListenRevoked(List<String> path) {
        Collection<OutstandingListen> listens = removeListens(path);
        if (listens != null) {
            for (OutstandingListen listen : listens) {
                listen.resultCallback.onRequestResult("permission_denied", null);
            }
        }
    }

    private void onAuthRevoked(String errorCode, String errorMessage) {
        this.logger.debug("Auth token revoked: " + errorCode + " (" + errorMessage + ")", new Object[0]);
        this.authToken = null;
        this.forceAuthTokenRefresh = true;
        this.delegate.onConnectionStatus(false);
        this.realtime.close();
    }

    private void onAppCheckRevoked(String errorCode, String errorMessage) {
        this.logger.debug("App check token revoked: " + errorCode + " (" + errorMessage + ")", new Object[0]);
        this.appCheckToken = null;
        this.forceAppCheckTokenRefresh = true;
    }

    private void onSecurityDebugPacket(Map<String, Object> message) {
        this.logger.info((String) message.get(NotificationCompat.CATEGORY_MESSAGE));
    }

    private void upgradeAuth() {
        sendAuthHelper(false);
    }

    private void upgradeAppCheck() {
        sendAppCheckTokenHelper(false);
    }

    private void sendAuthAndRestoreState() {
        sendAuthHelper(true);
    }

    private void sendAuthHelper(final boolean restoreStateAfterComplete) {
        ConnectionUtils.hardAssert(connected(), "Must be connected to send auth, but was: %s", this.connectionState);
        if (this.logger.logsDebug()) {
            this.logger.debug("Sending auth.", new Object[0]);
        }
        ConnectionRequestCallback onComplete = new ConnectionRequestCallback() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl.4
            @Override // com.google.firebase.database.connection.PersistentConnectionImpl.ConnectionRequestCallback
            public void onResponse(Map<String, Object> response) {
                String status = (String) response.get("s");
                if (!status.equals("ok")) {
                    PersistentConnectionImpl.this.authToken = null;
                    PersistentConnectionImpl.this.forceAuthTokenRefresh = true;
                    PersistentConnectionImpl.this.delegate.onConnectionStatus(false);
                    String reason = (String) response.get("d");
                    PersistentConnectionImpl.this.logger.debug("Authentication failed: " + status + " (" + reason + ")", new Object[0]);
                    PersistentConnectionImpl.this.realtime.close();
                    if (status.equals("invalid_token")) {
                        PersistentConnectionImpl.access$1008(PersistentConnectionImpl.this);
                        if (PersistentConnectionImpl.this.invalidAuthTokenCount >= 3) {
                            PersistentConnectionImpl.this.retryHelper.setMaxDelay();
                            PersistentConnectionImpl.this.logger.warn("Provided authentication credentials are invalid. This usually indicates your FirebaseApp instance was not initialized correctly. Make sure your google-services.json file has the correct firebase_url and api_key. You can re-download google-services.json from https://console.firebase.google.com/.");
                            return;
                        }
                        return;
                    }
                    return;
                }
                PersistentConnectionImpl.this.connectionState = ConnectionState.Connected;
                PersistentConnectionImpl.this.invalidAuthTokenCount = 0;
                PersistentConnectionImpl.this.sendAppCheckTokenHelper(restoreStateAfterComplete);
            }
        };
        Map<String, Object> request = new HashMap<>();
        GAuthToken gAuthToken = GAuthToken.tryParseFromString(this.authToken);
        if (gAuthToken != null) {
            request.put(REQUEST_CREDENTIAL, gAuthToken.getToken());
            if (gAuthToken.getAuth() != null) {
                request.put(REQUEST_AUTHVAR, gAuthToken.getAuth());
            }
            sendSensitive(REQUEST_ACTION_GAUTH, true, request, onComplete);
            return;
        }
        request.put(REQUEST_CREDENTIAL, this.authToken);
        sendSensitive(REQUEST_ACTION_AUTH, true, request, onComplete);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendAppCheckTokenHelper(final boolean restoreStateAfterComplete) {
        if (this.appCheckToken == null) {
            restoreState();
            return;
        }
        ConnectionUtils.hardAssert(connected(), "Must be connected to send auth, but was: %s", this.connectionState);
        if (this.logger.logsDebug()) {
            this.logger.debug("Sending app check.", new Object[0]);
        }
        ConnectionRequestCallback onComplete = new ConnectionRequestCallback() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl$$ExternalSyntheticLambda3
            @Override // com.google.firebase.database.connection.PersistentConnectionImpl.ConnectionRequestCallback
            public final void onResponse(Map map) {
                PersistentConnectionImpl.this.m229x9b5ee8e8(restoreStateAfterComplete, map);
            }
        };
        Map<String, Object> request = new HashMap<>();
        ConnectionUtils.hardAssert(this.appCheckToken != null, "App check token must be set!", new Object[0]);
        request.put(REQUEST_APPCHECK_TOKEN, this.appCheckToken);
        sendSensitive(REQUEST_ACTION_APPCHECK, true, request, onComplete);
    }

    /* renamed from: lambda$sendAppCheckTokenHelper$5$com-google-firebase-database-connection-PersistentConnectionImpl */
    public /* synthetic */ void m229x9b5ee8e8(boolean restoreStateAfterComplete, Map response) {
        String status = (String) response.get("s");
        if (status.equals("ok")) {
            this.invalidAppCheckTokenCount = 0;
        } else {
            this.appCheckToken = null;
            this.forceAppCheckTokenRefresh = true;
            String reason = (String) response.get("d");
            this.logger.debug("App check failed: " + status + " (" + reason + ")", new Object[0]);
        }
        if (restoreStateAfterComplete) {
            restoreState();
        }
    }

    private void sendUnauth() {
        ConnectionUtils.hardAssert(connected(), "Must be connected to send unauth.", new Object[0]);
        ConnectionUtils.hardAssert(this.authToken == null, "Auth token must not be set.", new Object[0]);
        sendAction(REQUEST_ACTION_UNAUTH, Collections.emptyMap(), null);
    }

    private void sendUnAppCheck() {
        ConnectionUtils.hardAssert(connected(), "Must be connected to send unauth.", new Object[0]);
        ConnectionUtils.hardAssert(this.appCheckToken == null, "App check token must not be set.", new Object[0]);
        sendAction(REQUEST_ACTION_UNAPPCHECK, Collections.emptyMap(), null);
    }

    private void restoreTokens() {
        if (this.logger.logsDebug()) {
            this.logger.debug("calling restore tokens", new Object[0]);
        }
        ConnectionUtils.hardAssert(this.connectionState == ConnectionState.Connecting, "Wanted to restore tokens, but was in wrong state: %s", this.connectionState);
        if (this.authToken != null) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Restoring auth.", new Object[0]);
            }
            this.connectionState = ConnectionState.Authenticating;
            sendAuthAndRestoreState();
            return;
        }
        if (this.logger.logsDebug()) {
            this.logger.debug("Not restoring auth because auth token is null.", new Object[0]);
        }
        this.connectionState = ConnectionState.Connected;
        sendAppCheckTokenHelper(true);
    }

    private void restoreState() {
        ConnectionUtils.hardAssert(this.connectionState == ConnectionState.Connected, "Should be connected if we're restoring state, but we are: %s", this.connectionState);
        if (this.logger.logsDebug()) {
            this.logger.debug("Restoring outstanding listens", new Object[0]);
        }
        for (OutstandingListen listen : this.listens.values()) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Restoring listen " + listen.getQuery(), new Object[0]);
            }
            sendListen(listen);
        }
        if (this.logger.logsDebug()) {
            this.logger.debug("Restoring writes.", new Object[0]);
        }
        ArrayList<Long> outstanding = new ArrayList<>(this.outstandingPuts.keySet());
        Collections.sort(outstanding);
        Iterator<Long> it = outstanding.iterator();
        while (it.hasNext()) {
            Long put = it.next();
            sendPut(put.longValue());
        }
        for (OutstandingDisconnect disconnect : this.onDisconnectRequestQueue) {
            sendOnDisconnect(disconnect.getAction(), disconnect.getPath(), disconnect.getData(), disconnect.getOnComplete());
        }
        this.onDisconnectRequestQueue.clear();
        if (this.logger.logsDebug()) {
            this.logger.debug("Restoring reads.", new Object[0]);
        }
        ArrayList<Long> outstandingGetKeys = new ArrayList<>(this.outstandingGets.keySet());
        Collections.sort(outstandingGetKeys);
        Iterator<Long> it2 = outstandingGetKeys.iterator();
        while (it2.hasNext()) {
            Long getId = it2.next();
            sendGet(getId);
        }
    }

    private void handleTimestamp(long timestamp) {
        if (this.logger.logsDebug()) {
            this.logger.debug("handling timestamp", new Object[0]);
        }
        long timestampDelta = timestamp - System.currentTimeMillis();
        Map<String, Object> updates = new HashMap<>();
        updates.put(Constants.DOT_INFO_SERVERTIME_OFFSET, Long.valueOf(timestampDelta));
        this.delegate.onServerInfoUpdate(updates);
    }

    private Map<String, Object> getPutObject(List<String> path, Object data, String hash) {
        Map<String, Object> request = new HashMap<>();
        request.put("p", ConnectionUtils.pathToString(path));
        request.put("d", data);
        if (hash != null) {
            request.put(REQUEST_DATA_HASH, hash);
        }
        return request;
    }

    private void putInternal(String action, List<String> path, Object data, String hash, RequestResultCallback onComplete) {
        Map<String, Object> request = getPutObject(path, data, hash);
        long writeId = this.writeCounter;
        this.writeCounter = 1 + writeId;
        this.outstandingPuts.put(Long.valueOf(writeId), new OutstandingPut(action, request, onComplete));
        if (canSendWrites()) {
            sendPut(writeId);
        }
        this.lastWriteTimestamp = System.currentTimeMillis();
        doIdleCheck();
    }

    private void sendPut(final long putId) {
        ConnectionUtils.hardAssert(canSendWrites(), "sendPut called when we can't send writes (we're disconnected or writes are paused).", new Object[0]);
        final OutstandingPut put = this.outstandingPuts.get(Long.valueOf(putId));
        final RequestResultCallback onComplete = put.getOnComplete();
        final String action = put.getAction();
        put.markSent();
        sendAction(action, put.getRequest(), new ConnectionRequestCallback() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl.5
            @Override // com.google.firebase.database.connection.PersistentConnectionImpl.ConnectionRequestCallback
            public void onResponse(Map<String, Object> response) {
                if (PersistentConnectionImpl.this.logger.logsDebug()) {
                    PersistentConnectionImpl.this.logger.debug(action + " response: " + response, new Object[0]);
                }
                OutstandingPut currentPut = (OutstandingPut) PersistentConnectionImpl.this.outstandingPuts.get(Long.valueOf(putId));
                if (currentPut == put) {
                    PersistentConnectionImpl.this.outstandingPuts.remove(Long.valueOf(putId));
                    if (onComplete != null) {
                        String status = (String) response.get("s");
                        if (status.equals("ok")) {
                            onComplete.onRequestResult(null, null);
                        } else {
                            String errorMessage = (String) response.get("d");
                            onComplete.onRequestResult(status, errorMessage);
                        }
                    }
                } else if (PersistentConnectionImpl.this.logger.logsDebug()) {
                    PersistentConnectionImpl.this.logger.debug("Ignoring on complete for put " + putId + " because it was removed already.", new Object[0]);
                }
                PersistentConnectionImpl.this.doIdleCheck();
            }
        });
    }

    private void sendGet(final Long readId) {
        ConnectionUtils.hardAssert(canSendReads(), "sendGet called when we can't send gets", new Object[0]);
        final OutstandingGet get = this.outstandingGets.get(readId);
        if (!get.markSent() && this.logger.logsDebug()) {
            this.logger.debug("get" + readId + " cancelled, ignoring.", new Object[0]);
            return;
        }
        sendAction(REQUEST_ACTION_GET, get.getRequest(), new ConnectionRequestCallback() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl.6
            @Override // com.google.firebase.database.connection.PersistentConnectionImpl.ConnectionRequestCallback
            public void onResponse(Map<String, Object> response) {
                OutstandingGet currentGet = (OutstandingGet) PersistentConnectionImpl.this.outstandingGets.get(readId);
                if (currentGet == get) {
                    PersistentConnectionImpl.this.outstandingGets.remove(readId);
                    get.getOnComplete().onResponse(response);
                } else if (PersistentConnectionImpl.this.logger.logsDebug()) {
                    PersistentConnectionImpl.this.logger.debug("Ignoring on complete for get " + readId + " because it was removed already.", new Object[0]);
                }
            }
        });
    }

    private void sendListen(final OutstandingListen listen) {
        Map<String, Object> request = new HashMap<>();
        request.put("p", ConnectionUtils.pathToString(listen.getQuery().path));
        Object tag = listen.getTag();
        if (tag != null) {
            request.put("q", listen.query.queryParams);
            request.put("t", tag);
        }
        ListenHashProvider hashFunction = listen.getHashFunction();
        request.put(REQUEST_DATA_HASH, hashFunction.getSimpleHash());
        if (hashFunction.shouldIncludeCompoundHash()) {
            CompoundHash compoundHash = hashFunction.getCompoundHash();
            ArrayList arrayList = new ArrayList();
            for (List<String> path : compoundHash.getPosts()) {
                arrayList.add(ConnectionUtils.pathToString(path));
            }
            Map<String, Object> hash = new HashMap<>();
            hash.put(REQUEST_COMPOUND_HASH_HASHES, compoundHash.getHashes());
            hash.put(REQUEST_COMPOUND_HASH_PATHS, arrayList);
            request.put(REQUEST_COMPOUND_HASH, hash);
        }
        sendAction("q", request, new ConnectionRequestCallback() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl.7
            @Override // com.google.firebase.database.connection.PersistentConnectionImpl.ConnectionRequestCallback
            public void onResponse(Map<String, Object> response) {
                String status = (String) response.get("s");
                if (status.equals("ok")) {
                    Map<String, Object> serverBody = (Map) response.get("d");
                    if (serverBody.containsKey(PersistentConnectionImpl.SERVER_DATA_WARNINGS)) {
                        List<String> warnings = (List) serverBody.get(PersistentConnectionImpl.SERVER_DATA_WARNINGS);
                        PersistentConnectionImpl.this.warnOnListenerWarnings(warnings, listen.query);
                    }
                }
                OutstandingListen currentListen = (OutstandingListen) PersistentConnectionImpl.this.listens.get(listen.getQuery());
                if (currentListen == listen) {
                    if (!status.equals("ok")) {
                        PersistentConnectionImpl.this.removeListen(listen.getQuery());
                        String errorMessage = (String) response.get("d");
                        listen.resultCallback.onRequestResult(status, errorMessage);
                        return;
                    }
                    listen.resultCallback.onRequestResult(null, null);
                }
            }
        });
    }

    private void sendStats(Map<String, Integer> stats) {
        if (!stats.isEmpty()) {
            Map<String, Object> request = new HashMap<>();
            request.put("c", stats);
            sendAction("s", request, new ConnectionRequestCallback() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl.8
                @Override // com.google.firebase.database.connection.PersistentConnectionImpl.ConnectionRequestCallback
                public void onResponse(Map<String, Object> response) {
                    String status = (String) response.get("s");
                    if (!status.equals("ok")) {
                        String errorMessage = (String) response.get("d");
                        if (PersistentConnectionImpl.this.logger.logsDebug()) {
                            PersistentConnectionImpl.this.logger.debug("Failed to send stats: " + status + " (message: " + errorMessage + ")", new Object[0]);
                        }
                    }
                }
            });
        } else if (this.logger.logsDebug()) {
            this.logger.debug("Not sending stats because stats are empty", new Object[0]);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void warnOnListenerWarnings(List<String> warnings, QuerySpec query) {
        if (warnings.contains("no_index")) {
            String indexSpec = "\".indexOn\": \"" + query.queryParams.get("i") + Typography.quote;
            this.logger.warn("Using an unspecified index. Your data will be downloaded and filtered on the client. Consider adding '" + indexSpec + "' at " + ConnectionUtils.pathToString(query.path) + " to your security and Firebase Database rules for better performance");
        }
    }

    private void sendConnectStats() {
        Map<String, Integer> stats = new HashMap<>();
        if (this.context.isPersistenceEnabled()) {
            stats.put("persistence.android.enabled", 1);
        }
        stats.put("sdk.android." + this.context.getClientSdkVersion().replace('.', '-'), 1);
        if (this.logger.logsDebug()) {
            this.logger.debug("Sending first connection stats", new Object[0]);
        }
        sendStats(stats);
    }

    private void sendAction(String action, Map<String, Object> message, ConnectionRequestCallback onResponse) {
        sendSensitive(action, false, message, onResponse);
    }

    private void sendSensitive(String action, boolean isSensitive, Map<String, Object> message, ConnectionRequestCallback onResponse) {
        long rn = nextRequestNumber();
        Map<String, Object> request = new HashMap<>();
        request.put(REQUEST_NUMBER, Long.valueOf(rn));
        request.put("a", action);
        request.put("b", message);
        this.realtime.sendRequest(request, isSensitive);
        this.requestCBHash.put(Long.valueOf(rn), onResponse);
    }

    private long nextRequestNumber() {
        long j = this.requestCounter;
        this.requestCounter = 1 + j;
        return j;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doIdleCheck() {
        if (isIdle()) {
            ScheduledFuture<?> scheduledFuture = this.inactivityTimer;
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
            this.inactivityTimer = this.executorService.schedule(new Runnable() { // from class: com.google.firebase.database.connection.PersistentConnectionImpl.9
                @Override // java.lang.Runnable
                public void run() {
                    PersistentConnectionImpl.this.inactivityTimer = null;
                    if (!PersistentConnectionImpl.this.idleHasTimedOut()) {
                        PersistentConnectionImpl.this.doIdleCheck();
                    } else {
                        PersistentConnectionImpl.this.interrupt(PersistentConnectionImpl.IDLE_INTERRUPT_REASON);
                    }
                }
            }, IDLE_TIMEOUT, TimeUnit.MILLISECONDS);
            return;
        }
        if (isInterrupted(IDLE_INTERRUPT_REASON)) {
            ConnectionUtils.hardAssert(!isIdle());
            resume(IDLE_INTERRUPT_REASON);
        }
    }

    private boolean isIdle() {
        return this.listens.isEmpty() && this.outstandingGets.isEmpty() && this.requestCBHash.isEmpty() && !this.hasOnDisconnects && this.outstandingPuts.isEmpty();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean idleHasTimedOut() {
        long now = System.currentTimeMillis();
        return isIdle() && now > this.lastWriteTimestamp + IDLE_TIMEOUT;
    }

    public void injectConnectionFailure() {
        Connection connection = this.realtime;
        if (connection != null) {
            connection.injectConnectionFailure();
        }
    }
}
