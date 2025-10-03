package androidx.media;

import android.media.AudioAttributes;
import androidx.versionedparcelable.VersionedParcel;

/* loaded from: classes.dex */
public final class AudioAttributesImplApi21Parcelizer {
    public static AudioAttributesImplApi21 read(VersionedParcel parcel) {
        AudioAttributesImplApi21 obj = new AudioAttributesImplApi21();
        obj.mAudioAttributes = (AudioAttributes) parcel.readParcelable(obj.mAudioAttributes, 1);
        obj.mLegacyStreamType = parcel.readInt(obj.mLegacyStreamType, 2);
        return obj;
    }

    public static void write(AudioAttributesImplApi21 obj, VersionedParcel parcel) {
        parcel.setSerializationFlags(false, false);
        parcel.writeParcelable(obj.mAudioAttributes, 1);
        parcel.writeInt(obj.mLegacyStreamType, 2);
    }
}
