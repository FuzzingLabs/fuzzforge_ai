package kotlin.sequences;

import java.util.Iterator;
import kotlin.Metadata;
import kotlin.UByte;
import kotlin.UInt;
import kotlin.ULong;
import kotlin.UShort;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: _USequences.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\"\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u001c\u0010\u0000\u001a\u00020\u0001*\b\u0012\u0004\u0012\u00020\u00030\u0002H\u0007ø\u0001\u0000¢\u0006\u0004\b\u0004\u0010\u0005\u001a\u001c\u0010\u0000\u001a\u00020\u0001*\b\u0012\u0004\u0012\u00020\u00010\u0002H\u0007ø\u0001\u0000¢\u0006\u0004\b\u0006\u0010\u0005\u001a\u001c\u0010\u0000\u001a\u00020\u0007*\b\u0012\u0004\u0012\u00020\u00070\u0002H\u0007ø\u0001\u0000¢\u0006\u0004\b\b\u0010\t\u001a\u001c\u0010\u0000\u001a\u00020\u0001*\b\u0012\u0004\u0012\u00020\n0\u0002H\u0007ø\u0001\u0000¢\u0006\u0004\b\u000b\u0010\u0005\u0082\u0002\u0004\n\u0002\b\u0019¨\u0006\f"}, m238d2 = {"sum", "Lkotlin/UInt;", "Lkotlin/sequences/Sequence;", "Lkotlin/UByte;", "sumOfUByte", "(Lkotlin/sequences/Sequence;)I", "sumOfUInt", "Lkotlin/ULong;", "sumOfULong", "(Lkotlin/sequences/Sequence;)J", "Lkotlin/UShort;", "sumOfUShort", "kotlin-stdlib"}, m239k = 5, m240mv = {1, 4, 0}, m242xi = 1, m243xs = "kotlin/sequences/USequencesKt")
/* loaded from: classes11.dex */
class USequencesKt___USequencesKt {
    public static final int sumOfUInt(Sequence<UInt> sum) {
        Intrinsics.checkNotNullParameter(sum, "$this$sum");
        int sum2 = 0;
        Iterator<UInt> it = sum.iterator();
        while (it.hasNext()) {
            int element = it.next().getData();
            sum2 = UInt.m380constructorimpl(sum2 + element);
        }
        return sum2;
    }

    public static final long sumOfULong(Sequence<ULong> sum) {
        Intrinsics.checkNotNullParameter(sum, "$this$sum");
        long sum2 = 0;
        Iterator<ULong> it = sum.iterator();
        while (it.hasNext()) {
            long element = it.next().getData();
            sum2 = ULong.m450constructorimpl(sum2 + element);
        }
        return sum2;
    }

    public static final int sumOfUByte(Sequence<UByte> sum) {
        Intrinsics.checkNotNullParameter(sum, "$this$sum");
        int sum2 = 0;
        Iterator<UByte> it = sum.iterator();
        while (it.hasNext()) {
            byte element = it.next().getData();
            sum2 = UInt.m380constructorimpl(UInt.m380constructorimpl(element & 255) + sum2);
        }
        return sum2;
    }

    public static final int sumOfUShort(Sequence<UShort> sum) {
        Intrinsics.checkNotNullParameter(sum, "$this$sum");
        int sum2 = 0;
        Iterator<UShort> it = sum.iterator();
        while (it.hasNext()) {
            short element = it.next().getData();
            sum2 = UInt.m380constructorimpl(UInt.m380constructorimpl(65535 & element) + sum2);
        }
        return sum2;
    }
}
