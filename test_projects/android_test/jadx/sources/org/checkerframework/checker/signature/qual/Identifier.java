package org.checkerframework.checker.signature.qual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import org.checkerframework.framework.qual.ImplicitFor;
import org.checkerframework.framework.qual.SubtypeOf;

@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@ImplicitFor(stringPatterns = {"^([A-Za-z_][A-Za-z_0-9]*)$"})
@SubtypeOf({SourceNameForNonArrayNonInner.class, BinaryNameForNonArrayInUnnamedPackage.class, IdentifierOrArray.class})
/* loaded from: classes11.dex */
public @interface Identifier {
}
