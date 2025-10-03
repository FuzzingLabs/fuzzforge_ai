package androidx.media;

import androidx.versionedparcelable.VersionedParcel;

/* loaded from: classes.dex */
public final class AudioAttributesImplBaseParcelizer {
    public static AudioAttributesImplBase read(VersionedParcel parcel) {
        AudioAttributesImplBase obj = new AudioAttributesImplBase();
        obj.mUsage = parcel.readInt(obj.mUsage, 1);
        obj.mContentType = parcel.readInt(obj.mContentType, 2);
        obj.mFlags = parcel.readInt(obj.mFlags, 3);
        obj.mLegacyStream = parcel.readInt(obj.mLegacyStream, 4);
        return obj;
    }

    public static void write(AudioAttributesImplBase obj, VersionedParcel parcel) {
        parcel.setSerializationFlags(false, false);
        parcel.writeInt(obj.mUsage, 1);
        parcel.writeInt(obj.mContentType, 2);
        parcel.writeInt(obj.mFlags, 3);
        parcel.writeInt(obj.mLegacyStream, 4);
    }
}
