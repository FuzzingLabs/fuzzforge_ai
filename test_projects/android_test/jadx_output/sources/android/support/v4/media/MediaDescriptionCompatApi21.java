package android.support.v4.media;

import android.graphics.Bitmap;
import android.media.MediaDescription;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;

/* loaded from: classes.dex */
class MediaDescriptionCompatApi21 {
    public static String getMediaId(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getMediaId();
    }

    public static CharSequence getTitle(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getTitle();
    }

    public static CharSequence getSubtitle(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getSubtitle();
    }

    public static CharSequence getDescription(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getDescription();
    }

    public static Bitmap getIconBitmap(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getIconBitmap();
    }

    public static Uri getIconUri(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getIconUri();
    }

    public static Bundle getExtras(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getExtras();
    }

    public static void writeToParcel(Object descriptionObj, Parcel dest, int flags) {
        ((MediaDescription) descriptionObj).writeToParcel(dest, flags);
    }

    public static Object fromParcel(Parcel in) {
        return MediaDescription.CREATOR.createFromParcel(in);
    }

    /* loaded from: classes.dex */
    static class Builder {
        public static Object newInstance() {
            return new MediaDescription.Builder();
        }

        public static void setMediaId(Object builderObj, String mediaId) {
            ((MediaDescription.Builder) builderObj).setMediaId(mediaId);
        }

        public static void setTitle(Object builderObj, CharSequence title) {
            ((MediaDescription.Builder) builderObj).setTitle(title);
        }

        public static void setSubtitle(Object builderObj, CharSequence subtitle) {
            ((MediaDescription.Builder) builderObj).setSubtitle(subtitle);
        }

        public static void setDescription(Object builderObj, CharSequence description) {
            ((MediaDescription.Builder) builderObj).setDescription(description);
        }

        public static void setIconBitmap(Object builderObj, Bitmap iconBitmap) {
            ((MediaDescription.Builder) builderObj).setIconBitmap(iconBitmap);
        }

        public static void setIconUri(Object builderObj, Uri iconUri) {
            ((MediaDescription.Builder) builderObj).setIconUri(iconUri);
        }

        public static void setExtras(Object builderObj, Bundle extras) {
            ((MediaDescription.Builder) builderObj).setExtras(extras);
        }

        public static Object build(Object builderObj) {
            return ((MediaDescription.Builder) builderObj).build();
        }

        private Builder() {
        }
    }

    private MediaDescriptionCompatApi21() {
    }
}
