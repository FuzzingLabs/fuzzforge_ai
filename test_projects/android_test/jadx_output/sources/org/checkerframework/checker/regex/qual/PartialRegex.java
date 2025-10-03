package org.checkerframework.checker.regex.qual;

import java.lang.annotation.Target;
import org.checkerframework.framework.qual.InvisibleQualifier;
import org.checkerframework.framework.qual.SubtypeOf;

@Target({})
@InvisibleQualifier
@SubtypeOf({UnknownRegex.class})
/* loaded from: classes11.dex */
public @interface PartialRegex {
    String value() default "";
}
