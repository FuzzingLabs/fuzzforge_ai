package com.google.android.gms.common;

import java.util.Arrays;

/* loaded from: classes.dex */
final class zzf extends zze {
    private final byte[] zzu;

    zzf(byte[] bArr) {
        super(Arrays.copyOfRange(bArr, 0, 25));
        this.zzu = bArr;
    }

    @Override // com.google.android.gms.common.zze
    final byte[] getBytes() {
        return this.zzu;
    }
}
