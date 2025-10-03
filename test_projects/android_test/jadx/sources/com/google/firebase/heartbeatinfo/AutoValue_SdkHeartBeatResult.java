package com.google.firebase.heartbeatinfo;

/* loaded from: classes.dex */
final class AutoValue_SdkHeartBeatResult extends SdkHeartBeatResult {
    private final long millis;
    private final String sdkName;

    AutoValue_SdkHeartBeatResult(String sdkName, long millis) {
        if (sdkName == null) {
            throw new NullPointerException("Null sdkName");
        }
        this.sdkName = sdkName;
        this.millis = millis;
    }

    @Override // com.google.firebase.heartbeatinfo.SdkHeartBeatResult
    public String getSdkName() {
        return this.sdkName;
    }

    @Override // com.google.firebase.heartbeatinfo.SdkHeartBeatResult
    public long getMillis() {
        return this.millis;
    }

    public String toString() {
        return "SdkHeartBeatResult{sdkName=" + this.sdkName + ", millis=" + this.millis + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SdkHeartBeatResult)) {
            return false;
        }
        SdkHeartBeatResult that = (SdkHeartBeatResult) o;
        return this.sdkName.equals(that.getSdkName()) && this.millis == that.getMillis();
    }

    public int hashCode() {
        int h$ = 1 * 1000003;
        int h$2 = (h$ ^ this.sdkName.hashCode()) * 1000003;
        long j = this.millis;
        return h$2 ^ ((int) (j ^ (j >>> 32)));
    }
}
