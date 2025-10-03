package com.google.firebase.heartbeatinfo;

import com.google.firebase.heartbeatinfo.HeartBeatInfo;

/* loaded from: classes.dex */
final class AutoValue_HeartBeatResult extends HeartBeatResult {
    private final HeartBeatInfo.HeartBeat heartBeat;
    private final long millis;
    private final String sdkName;

    AutoValue_HeartBeatResult(String sdkName, long millis, HeartBeatInfo.HeartBeat heartBeat) {
        if (sdkName == null) {
            throw new NullPointerException("Null sdkName");
        }
        this.sdkName = sdkName;
        this.millis = millis;
        if (heartBeat == null) {
            throw new NullPointerException("Null heartBeat");
        }
        this.heartBeat = heartBeat;
    }

    @Override // com.google.firebase.heartbeatinfo.HeartBeatResult
    public String getSdkName() {
        return this.sdkName;
    }

    @Override // com.google.firebase.heartbeatinfo.HeartBeatResult
    public long getMillis() {
        return this.millis;
    }

    @Override // com.google.firebase.heartbeatinfo.HeartBeatResult
    public HeartBeatInfo.HeartBeat getHeartBeat() {
        return this.heartBeat;
    }

    public String toString() {
        return "HeartBeatResult{sdkName=" + this.sdkName + ", millis=" + this.millis + ", heartBeat=" + this.heartBeat + "}";
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof HeartBeatResult)) {
            return false;
        }
        HeartBeatResult that = (HeartBeatResult) o;
        return this.sdkName.equals(that.getSdkName()) && this.millis == that.getMillis() && this.heartBeat.equals(that.getHeartBeat());
    }

    public int hashCode() {
        int h$ = 1 * 1000003;
        int h$2 = (h$ ^ this.sdkName.hashCode()) * 1000003;
        long j = this.millis;
        return ((h$2 ^ ((int) (j ^ (j >>> 32)))) * 1000003) ^ this.heartBeat.hashCode();
    }
}
