package com.google.android.gms.common.internal.safeparcel;

import android.os.Parcelable;

/* loaded from: classes.dex */
public interface SafeParcelable extends Parcelable {
    public static final String NULL = "SAFE_PARCELABLE_NULL_STRING";

    /* loaded from: classes.dex */
    public @interface Class {
        String creator();

        boolean validate() default false;
    }

    /* loaded from: classes.dex */
    public @interface Constructor {
    }

    /* loaded from: classes.dex */
    public @interface Field {
        String defaultValue() default "SAFE_PARCELABLE_NULL_STRING";

        String defaultValueUnchecked() default "SAFE_PARCELABLE_NULL_STRING";

        String getter() default "SAFE_PARCELABLE_NULL_STRING";

        /* renamed from: id */
        int m28id();

        String type() default "SAFE_PARCELABLE_NULL_STRING";
    }

    /* loaded from: classes.dex */
    public @interface Indicator {
        String getter() default "SAFE_PARCELABLE_NULL_STRING";
    }

    /* loaded from: classes.dex */
    public @interface Param {
        /* renamed from: id */
        int m29id();
    }

    /* loaded from: classes.dex */
    public @interface Reserved {
        int[] value();
    }

    /* loaded from: classes.dex */
    public @interface VersionField {
        String getter() default "SAFE_PARCELABLE_NULL_STRING";

        /* renamed from: id */
        int m30id();

        String type() default "SAFE_PARCELABLE_NULL_STRING";
    }
}
