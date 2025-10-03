package org.checkerframework.checker.index.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.SubtypeOf;

@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@ImplicitFor(literals = {LiteralKind.NULL})
@Retention(RetentionPolicy.RUNTIME)
@SubtypeOf({LessThan.class})
/* loaded from: classes11.dex */
public @interface LessThanBottom {
}
