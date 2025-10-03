package com.google.common.collect;

import com.google.common.collect.Maps;
import java.util.function.Function;

/* compiled from: D8$$SyntheticClass */
/* loaded from: classes.dex */
public final /* synthetic */ class Maps$$ExternalSyntheticLambda4 implements Function {
    public static final /* synthetic */ Maps$$ExternalSyntheticLambda4 INSTANCE = new Maps$$ExternalSyntheticLambda4();

    private /* synthetic */ Maps$$ExternalSyntheticLambda4() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((Maps.Accumulator) obj).toImmutableMap();
    }
}
