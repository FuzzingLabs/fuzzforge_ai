package com.google.common.collect;

import java.util.Map;
import java.util.function.Function;

/* compiled from: D8$$SyntheticClass */
/* loaded from: classes.dex */
public final /* synthetic */ class ImmutableMapValues$$ExternalSyntheticLambda1 implements Function {
    public static final /* synthetic */ ImmutableMapValues$$ExternalSyntheticLambda1 INSTANCE = new ImmutableMapValues$$ExternalSyntheticLambda1();

    private /* synthetic */ ImmutableMapValues$$ExternalSyntheticLambda1() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((Map.Entry) obj).getValue();
    }
}
