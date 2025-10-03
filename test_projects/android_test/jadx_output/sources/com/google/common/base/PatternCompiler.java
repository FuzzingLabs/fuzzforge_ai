package com.google.common.base;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public interface PatternCompiler {
    CommonPattern compile(String str);

    boolean isPcreLike();
}
