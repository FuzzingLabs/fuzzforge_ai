package okio.internal;

import androidx.constraintlayout.core.motion.utils.TypedValues;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.Intrinsics;
import okio.Buffer;
import okio.ByteString;
import okio.Segment;
import okio.SegmentedByteString;
import okio.Util;

/* compiled from: SegmentedByteString.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000R\n\u0000\n\u0002\u0010\b\n\u0002\u0010\u0015\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0005\n\u0002\b\u0003\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a$\u0010\u0000\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0001H\u0000\u001a\u0017\u0010\u0006\u001a\u00020\u0007*\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\nH\u0080\b\u001a\r\u0010\u000b\u001a\u00020\u0001*\u00020\bH\u0080\b\u001a\r\u0010\f\u001a\u00020\u0001*\u00020\bH\u0080\b\u001a\u0015\u0010\r\u001a\u00020\u000e*\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0001H\u0080\b\u001a-\u0010\u0010\u001a\u00020\u0007*\u00020\b2\u0006\u0010\u0011\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u0001H\u0080\b\u001a-\u0010\u0010\u001a\u00020\u0007*\u00020\b2\u0006\u0010\u0011\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\u00152\u0006\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u0001H\u0080\b\u001a\u001d\u0010\u0016\u001a\u00020\u0015*\u00020\b2\u0006\u0010\u0017\u001a\u00020\u00012\u0006\u0010\u0018\u001a\u00020\u0001H\u0080\b\u001a\r\u0010\u0019\u001a\u00020\u0012*\u00020\bH\u0080\b\u001a%\u0010\u001a\u001a\u00020\u001b*\u00020\b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0014\u001a\u00020\u0001H\u0080\b\u001a]\u0010\u001e\u001a\u00020\u001b*\u00020\b2K\u0010\u001f\u001aG\u0012\u0013\u0012\u00110\u0012¢\u0006\f\b!\u0012\b\b\"\u0012\u0004\b\b(#\u0012\u0013\u0012\u00110\u0001¢\u0006\f\b!\u0012\b\b\"\u0012\u0004\b\b(\u0011\u0012\u0013\u0012\u00110\u0001¢\u0006\f\b!\u0012\b\b\"\u0012\u0004\b\b(\u0014\u0012\u0004\u0012\u00020\u001b0 H\u0080\bø\u0001\u0000\u001aj\u0010\u001e\u001a\u00020\u001b*\u00020\b2\u0006\u0010\u0017\u001a\u00020\u00012\u0006\u0010\u0018\u001a\u00020\u00012K\u0010\u001f\u001aG\u0012\u0013\u0012\u00110\u0012¢\u0006\f\b!\u0012\b\b\"\u0012\u0004\b\b(#\u0012\u0013\u0012\u00110\u0001¢\u0006\f\b!\u0012\b\b\"\u0012\u0004\b\b(\u0011\u0012\u0013\u0012\u00110\u0001¢\u0006\f\b!\u0012\b\b\"\u0012\u0004\b\b(\u0014\u0012\u0004\u0012\u00020\u001b0 H\u0082\b\u001a\u0014\u0010$\u001a\u00020\u0001*\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0001H\u0000\u0082\u0002\u0007\n\u0005\b\u009920\u0001¨\u0006%"}, m238d2 = {"binarySearch", "", "", "value", "fromIndex", "toIndex", "commonEquals", "", "Lokio/SegmentedByteString;", "other", "", "commonGetSize", "commonHashCode", "commonInternalGet", "", "pos", "commonRangeEquals", TypedValues.Cycle.S_WAVE_OFFSET, "", "otherOffset", "byteCount", "Lokio/ByteString;", "commonSubstring", "beginIndex", "endIndex", "commonToByteArray", "commonWrite", "", "buffer", "Lokio/Buffer;", "forEachSegment", "action", "Lkotlin/Function3;", "Lkotlin/ParameterName;", "name", "data", "segment", "okio"}, m239k = 2, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class SegmentedByteStringKt {
    public static final int binarySearch(int[] binarySearch, int value, int fromIndex, int toIndex) {
        Intrinsics.checkNotNullParameter(binarySearch, "$this$binarySearch");
        int left = fromIndex;
        int right = toIndex - 1;
        while (left <= right) {
            int mid = (left + right) >>> 1;
            int midVal = binarySearch[mid];
            if (midVal < value) {
                left = mid + 1;
            } else {
                if (midVal <= value) {
                    return mid;
                }
                right = mid - 1;
            }
        }
        return (-left) - 1;
    }

    public static final int segment(SegmentedByteString segment, int pos) {
        Intrinsics.checkNotNullParameter(segment, "$this$segment");
        int i = binarySearch(segment.getDirectory(), pos + 1, 0, segment.getSegments().length);
        return i >= 0 ? i : ~i;
    }

    public static final void forEachSegment(SegmentedByteString forEachSegment, Function3<? super byte[], ? super Integer, ? super Integer, Unit> action) {
        Intrinsics.checkNotNullParameter(forEachSegment, "$this$forEachSegment");
        Intrinsics.checkNotNullParameter(action, "action");
        int segmentCount = forEachSegment.getSegments().length;
        int pos = 0;
        for (int s = 0; s < segmentCount; s++) {
            int segmentPos = forEachSegment.getDirectory()[segmentCount + s];
            int nextSegmentOffset = forEachSegment.getDirectory()[s];
            action.invoke(forEachSegment.getSegments()[s], Integer.valueOf(segmentPos), Integer.valueOf(nextSegmentOffset - pos));
            pos = nextSegmentOffset;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void forEachSegment(SegmentedByteString $this$forEachSegment, int beginIndex, int endIndex, Function3<? super byte[], ? super Integer, ? super Integer, Unit> function3) {
        int s = segment($this$forEachSegment, beginIndex);
        int pos = beginIndex;
        while (pos < endIndex) {
            int segmentOffset = s == 0 ? 0 : $this$forEachSegment.getDirectory()[s - 1];
            int segmentSize = $this$forEachSegment.getDirectory()[s] - segmentOffset;
            int segmentPos = $this$forEachSegment.getDirectory()[$this$forEachSegment.getSegments().length + s];
            int byteCount = Math.min(endIndex, segmentOffset + segmentSize) - pos;
            int offset = (pos - segmentOffset) + segmentPos;
            function3.invoke($this$forEachSegment.getSegments()[s], Integer.valueOf(offset), Integer.valueOf(byteCount));
            pos += byteCount;
            s++;
        }
    }

    public static final ByteString commonSubstring(SegmentedByteString commonSubstring, int beginIndex, int endIndex) {
        Intrinsics.checkNotNullParameter(commonSubstring, "$this$commonSubstring");
        if (!(beginIndex >= 0)) {
            throw new IllegalArgumentException(("beginIndex=" + beginIndex + " < 0").toString());
        }
        if (!(endIndex <= commonSubstring.size())) {
            throw new IllegalArgumentException(("endIndex=" + endIndex + " > length(" + commonSubstring.size() + ')').toString());
        }
        int subLen = endIndex - beginIndex;
        if (!(subLen >= 0)) {
            throw new IllegalArgumentException(("endIndex=" + endIndex + " < beginIndex=" + beginIndex).toString());
        }
        if (beginIndex == 0 && endIndex == commonSubstring.size()) {
            return commonSubstring;
        }
        if (beginIndex == endIndex) {
            return ByteString.EMPTY;
        }
        int beginSegment = segment(commonSubstring, beginIndex);
        int endSegment = segment(commonSubstring, endIndex - 1);
        byte[][] newSegments = (byte[][]) ArraysKt.copyOfRange(commonSubstring.getSegments(), beginSegment, endSegment + 1);
        int[] newDirectory = new int[newSegments.length * 2];
        int index = 0;
        if (beginSegment <= endSegment) {
            int s = beginSegment;
            while (true) {
                newDirectory[index] = Math.min(commonSubstring.getDirectory()[s] - beginIndex, subLen);
                int index2 = index + 1;
                newDirectory[index + newSegments.length] = commonSubstring.getDirectory()[commonSubstring.getSegments().length + s];
                if (s == endSegment) {
                    break;
                }
                s++;
                index = index2;
            }
        }
        int segmentOffset = beginSegment != 0 ? commonSubstring.getDirectory()[beginSegment - 1] : 0;
        int length = newSegments.length;
        newDirectory[length] = newDirectory[length] + (beginIndex - segmentOffset);
        return new SegmentedByteString(newSegments, newDirectory);
    }

    public static final byte commonInternalGet(SegmentedByteString commonInternalGet, int pos) {
        Intrinsics.checkNotNullParameter(commonInternalGet, "$this$commonInternalGet");
        Util.checkOffsetAndCount(commonInternalGet.getDirectory()[commonInternalGet.getSegments().length - 1], pos, 1L);
        int segment = segment(commonInternalGet, pos);
        int segmentOffset = segment == 0 ? 0 : commonInternalGet.getDirectory()[segment - 1];
        int segmentPos = commonInternalGet.getDirectory()[commonInternalGet.getSegments().length + segment];
        return commonInternalGet.getSegments()[segment][(pos - segmentOffset) + segmentPos];
    }

    public static final int commonGetSize(SegmentedByteString commonGetSize) {
        Intrinsics.checkNotNullParameter(commonGetSize, "$this$commonGetSize");
        return commonGetSize.getDirectory()[commonGetSize.getSegments().length - 1];
    }

    public static final byte[] commonToByteArray(SegmentedByteString commonToByteArray) {
        Intrinsics.checkNotNullParameter(commonToByteArray, "$this$commonToByteArray");
        byte[] result = new byte[commonToByteArray.size()];
        int resultPos = 0;
        int segmentCount$iv = commonToByteArray.getSegments().length;
        int pos$iv = 0;
        for (int s$iv = 0; s$iv < segmentCount$iv; s$iv++) {
            int segmentPos$iv = commonToByteArray.getDirectory()[segmentCount$iv + s$iv];
            int nextSegmentOffset$iv = commonToByteArray.getDirectory()[s$iv];
            byte[] data = commonToByteArray.getSegments()[s$iv];
            int byteCount = nextSegmentOffset$iv - pos$iv;
            ArraysKt.copyInto(data, result, resultPos, segmentPos$iv, segmentPos$iv + byteCount);
            resultPos += byteCount;
            pos$iv = nextSegmentOffset$iv;
        }
        return result;
    }

    public static final void commonWrite(SegmentedByteString commonWrite, Buffer buffer, int offset, int byteCount) {
        Intrinsics.checkNotNullParameter(commonWrite, "$this$commonWrite");
        Intrinsics.checkNotNullParameter(buffer, "buffer");
        int endIndex$iv = offset + byteCount;
        int s$iv = segment(commonWrite, offset);
        int pos$iv = offset;
        while (pos$iv < endIndex$iv) {
            int segmentOffset$iv = s$iv == 0 ? 0 : commonWrite.getDirectory()[s$iv - 1];
            int segmentSize$iv = commonWrite.getDirectory()[s$iv] - segmentOffset$iv;
            int segmentPos$iv = commonWrite.getDirectory()[commonWrite.getSegments().length + s$iv];
            int byteCount$iv = Math.min(endIndex$iv, segmentOffset$iv + segmentSize$iv) - pos$iv;
            int offset$iv = (pos$iv - segmentOffset$iv) + segmentPos$iv;
            byte[] data = commonWrite.getSegments()[s$iv];
            Segment segment = new Segment(data, offset$iv, offset$iv + byteCount$iv, true, false);
            if (buffer.head == null) {
                segment.prev = segment;
                segment.next = segment.prev;
                buffer.head = segment.next;
            } else {
                Segment segment2 = buffer.head;
                Intrinsics.checkNotNull(segment2);
                Segment segment3 = segment2.prev;
                Intrinsics.checkNotNull(segment3);
                segment3.push(segment);
            }
            pos$iv += byteCount$iv;
            s$iv++;
        }
        buffer.setSize$okio(buffer.size() + commonWrite.size());
    }

    public static final boolean commonRangeEquals(SegmentedByteString commonRangeEquals, int offset, ByteString other, int otherOffset, int byteCount) {
        int $i$f$commonRangeEquals = 0;
        Intrinsics.checkNotNullParameter(commonRangeEquals, "$this$commonRangeEquals");
        Intrinsics.checkNotNullParameter(other, "other");
        if (offset >= 0 && offset <= commonRangeEquals.size() - byteCount) {
            int otherOffset2 = otherOffset;
            int endIndex$iv = offset + byteCount;
            int s$iv = segment(commonRangeEquals, offset);
            int pos$iv = offset;
            while (pos$iv < endIndex$iv) {
                int segmentOffset$iv = s$iv == 0 ? 0 : commonRangeEquals.getDirectory()[s$iv - 1];
                int segmentSize$iv = commonRangeEquals.getDirectory()[s$iv] - segmentOffset$iv;
                int segmentPos$iv = commonRangeEquals.getDirectory()[commonRangeEquals.getSegments().length + s$iv];
                int byteCount$iv = Math.min(endIndex$iv, segmentOffset$iv + segmentSize$iv) - pos$iv;
                int offset$iv = (pos$iv - segmentOffset$iv) + segmentPos$iv;
                byte[] data = commonRangeEquals.getSegments()[s$iv];
                int offset2 = $i$f$commonRangeEquals;
                if (!other.rangeEquals(otherOffset2, data, offset$iv, byteCount$iv)) {
                    return false;
                }
                otherOffset2 += byteCount$iv;
                pos$iv += byteCount$iv;
                s$iv++;
                $i$f$commonRangeEquals = offset2;
            }
            return true;
        }
        return false;
    }

    public static final boolean commonRangeEquals(SegmentedByteString commonRangeEquals, int offset, byte[] other, int otherOffset, int byteCount) {
        Intrinsics.checkNotNullParameter(commonRangeEquals, "$this$commonRangeEquals");
        Intrinsics.checkNotNullParameter(other, "other");
        if (offset < 0 || offset > commonRangeEquals.size() - byteCount || otherOffset < 0 || otherOffset > other.length - byteCount) {
            return false;
        }
        int otherOffset2 = otherOffset;
        int endIndex$iv = offset + byteCount;
        int s$iv = segment(commonRangeEquals, offset);
        int pos$iv = offset;
        while (pos$iv < endIndex$iv) {
            int segmentOffset$iv = s$iv == 0 ? 0 : commonRangeEquals.getDirectory()[s$iv - 1];
            int segmentSize$iv = commonRangeEquals.getDirectory()[s$iv] - segmentOffset$iv;
            int segmentPos$iv = commonRangeEquals.getDirectory()[commonRangeEquals.getSegments().length + s$iv];
            int byteCount$iv = Math.min(endIndex$iv, segmentOffset$iv + segmentSize$iv) - pos$iv;
            int offset$iv = segmentPos$iv + (pos$iv - segmentOffset$iv);
            byte[] data = commonRangeEquals.getSegments()[s$iv];
            if (!Util.arrayRangeEquals(data, offset$iv, other, otherOffset2, byteCount$iv)) {
                return false;
            }
            otherOffset2 += byteCount$iv;
            pos$iv += byteCount$iv;
            s$iv++;
        }
        return true;
    }

    public static final boolean commonEquals(SegmentedByteString commonEquals, Object other) {
        Intrinsics.checkNotNullParameter(commonEquals, "$this$commonEquals");
        if (other == commonEquals) {
            return true;
        }
        if (other instanceof ByteString) {
            return ((ByteString) other).size() == commonEquals.size() && commonEquals.rangeEquals(0, (ByteString) other, 0, commonEquals.size());
        }
        return false;
    }

    public static final int commonHashCode(SegmentedByteString commonHashCode) {
        Intrinsics.checkNotNullParameter(commonHashCode, "$this$commonHashCode");
        int result = commonHashCode.getHashCode();
        if (result != 0) {
            return result;
        }
        int result2 = 1;
        int segmentCount$iv = commonHashCode.getSegments().length;
        int pos$iv = 0;
        for (int s$iv = 0; s$iv < segmentCount$iv; s$iv++) {
            int segmentPos$iv = commonHashCode.getDirectory()[segmentCount$iv + s$iv];
            int nextSegmentOffset$iv = commonHashCode.getDirectory()[s$iv];
            byte[] data = commonHashCode.getSegments()[s$iv];
            int byteCount = nextSegmentOffset$iv - pos$iv;
            int limit = segmentPos$iv + byteCount;
            for (int i = segmentPos$iv; i < limit; i++) {
                result2 = (result2 * 31) + data[i];
            }
            pos$iv = nextSegmentOffset$iv;
        }
        commonHashCode.setHashCode$okio(result2);
        return result2;
    }
}
