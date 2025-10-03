package com.google.firebase;

import android.content.Context;
import android.text.TextUtils;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.common.internal.Preconditions;
import com.google.android.gms.common.internal.StringResourceValueReader;
import com.google.android.gms.common.util.Strings;

/* loaded from: classes.dex */
public final class FirebaseOptions {
    private static final String API_KEY_RESOURCE_NAME = "google_api_key";
    private static final String APP_ID_RESOURCE_NAME = "google_app_id";
    private static final String DATABASE_URL_RESOURCE_NAME = "firebase_database_url";
    private static final String GA_TRACKING_ID_RESOURCE_NAME = "ga_trackingId";
    private static final String GCM_SENDER_ID_RESOURCE_NAME = "gcm_defaultSenderId";
    private static final String PROJECT_ID_RESOURCE_NAME = "project_id";
    private static final String STORAGE_BUCKET_RESOURCE_NAME = "google_storage_bucket";
    private final String apiKey;
    private final String applicationId;
    private final String databaseUrl;
    private final String gaTrackingId;
    private final String gcmSenderId;
    private final String projectId;
    private final String storageBucket;

    /* loaded from: classes.dex */
    public static final class Builder {
        private String apiKey;
        private String applicationId;
        private String databaseUrl;
        private String gaTrackingId;
        private String gcmSenderId;
        private String projectId;
        private String storageBucket;

        public Builder() {
        }

        public Builder(FirebaseOptions options) {
            this.applicationId = options.applicationId;
            this.apiKey = options.apiKey;
            this.databaseUrl = options.databaseUrl;
            this.gaTrackingId = options.gaTrackingId;
            this.gcmSenderId = options.gcmSenderId;
            this.storageBucket = options.storageBucket;
            this.projectId = options.projectId;
        }

        public Builder setApiKey(String apiKey) {
            this.apiKey = Preconditions.checkNotEmpty(apiKey, "ApiKey must be set.");
            return this;
        }

        public Builder setApplicationId(String applicationId) {
            this.applicationId = Preconditions.checkNotEmpty(applicationId, "ApplicationId must be set.");
            return this;
        }

        public Builder setDatabaseUrl(String databaseUrl) {
            this.databaseUrl = databaseUrl;
            return this;
        }

        public Builder setGaTrackingId(String gaTrackingId) {
            this.gaTrackingId = gaTrackingId;
            return this;
        }

        public Builder setGcmSenderId(String gcmSenderId) {
            this.gcmSenderId = gcmSenderId;
            return this;
        }

        public Builder setStorageBucket(String storageBucket) {
            this.storageBucket = storageBucket;
            return this;
        }

        public Builder setProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public FirebaseOptions build() {
            return new FirebaseOptions(this.applicationId, this.apiKey, this.databaseUrl, this.gaTrackingId, this.gcmSenderId, this.storageBucket, this.projectId);
        }
    }

    private FirebaseOptions(String applicationId, String apiKey, String databaseUrl, String gaTrackingId, String gcmSenderId, String storageBucket, String projectId) {
        Preconditions.checkState(!Strings.isEmptyOrWhitespace(applicationId), "ApplicationId must be set.");
        this.applicationId = applicationId;
        this.apiKey = apiKey;
        this.databaseUrl = databaseUrl;
        this.gaTrackingId = gaTrackingId;
        this.gcmSenderId = gcmSenderId;
        this.storageBucket = storageBucket;
        this.projectId = projectId;
    }

    public static FirebaseOptions fromResource(Context context) {
        StringResourceValueReader reader = new StringResourceValueReader(context);
        String applicationId = reader.getString(APP_ID_RESOURCE_NAME);
        if (TextUtils.isEmpty(applicationId)) {
            return null;
        }
        return new FirebaseOptions(applicationId, reader.getString(API_KEY_RESOURCE_NAME), reader.getString(DATABASE_URL_RESOURCE_NAME), reader.getString(GA_TRACKING_ID_RESOURCE_NAME), reader.getString(GCM_SENDER_ID_RESOURCE_NAME), reader.getString(STORAGE_BUCKET_RESOURCE_NAME), reader.getString(PROJECT_ID_RESOURCE_NAME));
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public String getDatabaseUrl() {
        return this.databaseUrl;
    }

    public String getGaTrackingId() {
        return this.gaTrackingId;
    }

    public String getGcmSenderId() {
        return this.gcmSenderId;
    }

    public String getStorageBucket() {
        return this.storageBucket;
    }

    public String getProjectId() {
        return this.projectId;
    }

    public boolean equals(Object o) {
        if (!(o instanceof FirebaseOptions)) {
            return false;
        }
        FirebaseOptions other = (FirebaseOptions) o;
        return Objects.equal(this.applicationId, other.applicationId) && Objects.equal(this.apiKey, other.apiKey) && Objects.equal(this.databaseUrl, other.databaseUrl) && Objects.equal(this.gaTrackingId, other.gaTrackingId) && Objects.equal(this.gcmSenderId, other.gcmSenderId) && Objects.equal(this.storageBucket, other.storageBucket) && Objects.equal(this.projectId, other.projectId);
    }

    public int hashCode() {
        return Objects.hashCode(this.applicationId, this.apiKey, this.databaseUrl, this.gaTrackingId, this.gcmSenderId, this.storageBucket, this.projectId);
    }

    public String toString() {
        return Objects.toStringHelper(this).add("applicationId", this.applicationId).add("apiKey", this.apiKey).add("databaseUrl", this.databaseUrl).add("gcmSenderId", this.gcmSenderId).add("storageBucket", this.storageBucket).add("projectId", this.projectId).toString();
    }
}
