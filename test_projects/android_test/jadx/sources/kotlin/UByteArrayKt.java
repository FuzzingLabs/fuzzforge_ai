package kotlin;

import kotlin.jvm.functions.Function1;

/* compiled from: UByteArray.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u001a\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a0\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00060\u0005H\u0087\bø\u0001\u0000ø\u0001\u0001¢\u0006\u0002\u0010\u0007\u001a\u001f\u0010\b\u001a\u00020\u00012\n\u0010\t\u001a\u00020\u0001\"\u00020\u0006H\u0087\bø\u0001\u0000¢\u0006\u0004\b\n\u0010\u000b\u0082\u0002\u000b\n\u0002\b\u0019\n\u0005\b\u009920\u0001¨\u0006\f"}, m238d2 = {"UByteArray", "Lkotlin/UByteArray;", "size", "", "init", "Lkotlin/Function1;", "Lkotlin/UByte;", "(ILkotlin/jvm/functions/Function1;)[B", "ubyteArrayOf", "elements", "ubyteArrayOf-GBYM_sE", "([B)[B", "kotlin-stdlib"}, m239k = 2, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class UByteArrayKt {
    private static final byte[] UByteArray(int size, Function1<? super Integer, UByte> function1) {
        byte[] bArr = new byte[size];
        for (int i = 0; i < size; i++) {
            int index = i;
            bArr[i] = function1.invoke(Integer.valueOf(index)).getData();
        }
        return UByteArray.m356constructorimpl(bArr);
    }

    /* renamed from: ubyteArrayOf-GBYM_sE, reason: not valid java name */
    private static final byte[] m372ubyteArrayOfGBYM_sE(byte... elements) {
        return elements;
    }
}
