package org.checkerframework.framework.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
/* loaded from: classes11.dex */
public @interface ImplicitFor {
    LiteralKind[] literals() default {};

    String[] stringPatterns() default {};

    Class<?>[] typeNames() default {};

    TypeKind[] types() default {};
}
