package org.checkerframework.common.aliasing.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.DefaultFor;
import org.checkerframework.framework.qual.DefaultQualifierInHierarchy;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.LiteralKind;
import org.checkerframework.framework.qual.SubtypeOf;
import org.checkerframework.framework.qual.TypeUseLocation;

@Target({ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@ImplicitFor(literals = {LiteralKind.NULL}, typeNames = {Void.class})
@Retention(RetentionPolicy.RUNTIME)
@SubtypeOf({})
@DefaultFor({TypeUseLocation.UPPER_BOUND, TypeUseLocation.LOWER_BOUND})
@Documented
@DefaultQualifierInHierarchy
/* loaded from: classes11.dex */
public @interface MaybeAliased {
}
