package com.google.common.base;

/* loaded from: classes.dex */
interface PatternCompiler {
    CommonPattern compile(String str);

    boolean isPcreLike();
}
