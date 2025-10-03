package org.checkerframework.dataflow.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Documented
@Retention(RetentionPolicy.RUNTIME)
/* loaded from: classes11.dex */
public @interface Pure {

    /* loaded from: classes11.dex */
    public enum Kind {
        SIDE_EFFECT_FREE,
        DETERMINISTIC
    }
}
