package kotlin;

import kotlin.jvm.functions.Function1;

/* compiled from: ULongArray.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u001a\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a0\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00060\u0005H\u0087\bø\u0001\u0000ø\u0001\u0001¢\u0006\u0002\u0010\u0007\u001a\u001f\u0010\b\u001a\u00020\u00012\n\u0010\t\u001a\u00020\u0001\"\u00020\u0006H\u0087\bø\u0001\u0000¢\u0006\u0004\b\n\u0010\u000b\u0082\u0002\u000b\n\u0002\b\u0019\n\u0005\b\u009920\u0001¨\u0006\f"}, m238d2 = {"ULongArray", "Lkotlin/ULongArray;", "size", "", "init", "Lkotlin/Function1;", "Lkotlin/ULong;", "(ILkotlin/jvm/functions/Function1;)[J", "ulongArrayOf", "elements", "ulongArrayOf-QwZRm1k", "([J)[J", "kotlin-stdlib"}, m239k = 2, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class ULongArrayKt {
    private static final long[] ULongArray(int size, Function1<? super Integer, ULong> function1) {
        long[] jArr = new long[size];
        for (int i = 0; i < size; i++) {
            int index = i;
            jArr[i] = function1.invoke(Integer.valueOf(index)).getData();
        }
        return ULongArray.m496constructorimpl(jArr);
    }

    /* renamed from: ulongArrayOf-QwZRm1k, reason: not valid java name */
    private static final long[] m512ulongArrayOfQwZRm1k(long... elements) {
        return elements;
    }
}
