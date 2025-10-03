package okio;

import java.util.List;
import java.util.RandomAccess;
import kotlin.Metadata;
import kotlin.collections.AbstractList;
import kotlin.jvm.JvmStatic;
import kotlin.jvm.internal.DefaultConstructorMarker;

/* compiled from: Options.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\b\u0018\u0000 \u00152\b\u0012\u0004\u0012\u00020\u00020\u00012\u00060\u0003j\u0002`\u0004:\u0001\u0015B\u001f\b\u0002\u0012\u000e\u0010\u0005\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b¢\u0006\u0002\u0010\tJ\u0011\u0010\u0013\u001a\u00020\u00022\u0006\u0010\u0014\u001a\u00020\u000eH\u0096\u0002R\u001e\u0010\u0005\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00020\u0006X\u0080\u0004¢\u0006\n\n\u0002\u0010\f\u001a\u0004\b\n\u0010\u000bR\u0014\u0010\r\u001a\u00020\u000e8VX\u0096\u0004¢\u0006\u0006\u001a\u0004\b\u000f\u0010\u0010R\u0014\u0010\u0007\u001a\u00020\bX\u0080\u0004¢\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012¨\u0006\u0016"}, m238d2 = {"Lokio/Options;", "Lkotlin/collections/AbstractList;", "Lokio/ByteString;", "Ljava/util/RandomAccess;", "Lkotlin/collections/RandomAccess;", "byteStrings", "", "trie", "", "([Lokio/ByteString;[I)V", "getByteStrings$okio", "()[Lokio/ByteString;", "[Lokio/ByteString;", "size", "", "getSize", "()I", "getTrie$okio", "()[I", "get", "index", "Companion", "okio"}, m239k = 1, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class Options extends AbstractList<ByteString> implements RandomAccess {

    /* renamed from: Companion, reason: from kotlin metadata */
    public static final Companion INSTANCE = new Companion(null);
    private final ByteString[] byteStrings;
    private final int[] trie;

    @JvmStatic
    /* renamed from: of */
    public static final Options m270of(ByteString... byteStringArr) {
        return INSTANCE.m271of(byteStringArr);
    }

    private Options(ByteString[] byteStrings, int[] trie) {
        this.byteStrings = byteStrings;
        this.trie = trie;
    }

    public /* synthetic */ Options(ByteString[] byteStrings, int[] trie, DefaultConstructorMarker $constructor_marker) {
        this(byteStrings, trie);
    }

    @Override // kotlin.collections.AbstractCollection, java.util.Collection
    public final /* bridge */ boolean contains(Object obj) {
        if (obj instanceof ByteString) {
            return contains((ByteString) obj);
        }
        return false;
    }

    public /* bridge */ boolean contains(ByteString byteString) {
        return super.contains((Object) byteString);
    }

    @Override // kotlin.collections.AbstractList, java.util.List
    public final /* bridge */ int indexOf(Object obj) {
        if (obj instanceof ByteString) {
            return indexOf((ByteString) obj);
        }
        return -1;
    }

    public /* bridge */ int indexOf(ByteString byteString) {
        return super.indexOf((Object) byteString);
    }

    @Override // kotlin.collections.AbstractList, java.util.List
    public final /* bridge */ int lastIndexOf(Object obj) {
        if (obj instanceof ByteString) {
            return lastIndexOf((ByteString) obj);
        }
        return -1;
    }

    public /* bridge */ int lastIndexOf(ByteString byteString) {
        return super.lastIndexOf((Object) byteString);
    }

    /* renamed from: getByteStrings$okio, reason: from getter */
    public final ByteString[] getByteStrings() {
        return this.byteStrings;
    }

    /* renamed from: getTrie$okio, reason: from getter */
    public final int[] getTrie() {
        return this.trie;
    }

    @Override // kotlin.collections.AbstractList, kotlin.collections.AbstractCollection
    /* renamed from: getSize */
    public int get_size() {
        return this.byteStrings.length;
    }

    @Override // kotlin.collections.AbstractList, java.util.List
    public ByteString get(int index) {
        return this.byteStrings[index];
    }

    /* compiled from: Options.kt */
    @Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010\u0011\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002JT\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\u00052\b\b\u0002\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f2\b\b\u0002\u0010\u0011\u001a\u00020\r2\b\b\u0002\u0010\u0012\u001a\u00020\r2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\r0\u000fH\u0002J!\u0010\u0014\u001a\u00020\u00152\u0012\u0010\u000e\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00100\u0016\"\u00020\u0010H\u0007¢\u0006\u0002\u0010\u0017R\u0018\u0010\u0003\u001a\u00020\u0004*\u00020\u00058BX\u0082\u0004¢\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007¨\u0006\u0018"}, m238d2 = {"Lokio/Options$Companion;", "", "()V", "intCount", "", "Lokio/Buffer;", "getIntCount", "(Lokio/Buffer;)J", "buildTrieRecursive", "", "nodeOffset", "node", "byteStringOffset", "", "byteStrings", "", "Lokio/ByteString;", "fromIndex", "toIndex", "indexes", "of", "Lokio/Options;", "", "([Lokio/ByteString;)Lokio/Options;", "okio"}, m239k = 1, m240mv = {1, 4, 0})
    /* loaded from: classes11.dex */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }

        /* JADX WARN: Code restructure failed: missing block: B:48:0x0114, code lost:
        
            continue;
         */
        @kotlin.jvm.JvmStatic
        /* renamed from: of */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public final okio.Options m271of(okio.ByteString... r22) {
            /*
                Method dump skipped, instructions count: 388
                To view this dump add '--comments-level debug' option
            */
            throw new UnsupportedOperationException("Method not decompiled: okio.Options.Companion.m271of(okio.ByteString[]):okio.Options");
        }

        static /* synthetic */ void buildTrieRecursive$default(Companion companion, long j, Buffer buffer, int i, List list, int i2, int i3, List list2, int i4, Object obj) {
            int i5;
            int i6;
            int i7;
            long j2 = (i4 & 1) != 0 ? 0L : j;
            if ((i4 & 4) == 0) {
                i5 = i;
            } else {
                i5 = 0;
            }
            if ((i4 & 16) == 0) {
                i6 = i2;
            } else {
                i6 = 0;
            }
            if ((i4 & 32) == 0) {
                i7 = i3;
            } else {
                i7 = list.size();
            }
            companion.buildTrieRecursive(j2, buffer, i5, list, i6, i7, list2);
        }

        private final void buildTrieRecursive(long nodeOffset, Buffer node, int byteStringOffset, List<? extends ByteString> byteStrings, int fromIndex, int toIndex, List<Integer> indexes) {
            int fromIndex2;
            ByteString from;
            int prefixIndex;
            ByteString from2;
            int rangeEnd;
            Buffer childNodes;
            int selectChoiceCount;
            int prefixIndex2;
            ByteString from3;
            int fromIndex3;
            List<Integer> list = indexes;
            if (!(fromIndex < toIndex)) {
                throw new IllegalArgumentException("Failed requirement.".toString());
            }
            for (int i = fromIndex; i < toIndex; i++) {
                if (!(byteStrings.get(i).size() >= byteStringOffset)) {
                    throw new IllegalArgumentException("Failed requirement.".toString());
                }
            }
            ByteString from4 = byteStrings.get(fromIndex);
            ByteString to = byteStrings.get(toIndex - 1);
            if (byteStringOffset != from4.size()) {
                fromIndex2 = fromIndex;
                from = from4;
                prefixIndex = -1;
            } else {
                int prefixIndex3 = list.get(fromIndex).intValue();
                int fromIndex4 = fromIndex + 1;
                fromIndex2 = fromIndex4;
                from = byteStrings.get(fromIndex4);
                prefixIndex = prefixIndex3;
            }
            if (from.getByte(byteStringOffset) != to.getByte(byteStringOffset)) {
                int i2 = fromIndex2 + 1;
                int selectChoiceCount2 = 1;
                while (i2 < toIndex) {
                    int i3 = i2;
                    if (byteStrings.get(i3 - 1).getByte(byteStringOffset) != byteStrings.get(i3).getByte(byteStringOffset)) {
                        selectChoiceCount2++;
                    }
                    i2 = i3 + 1;
                }
                long childNodesOffset = nodeOffset + getIntCount(node) + 2 + (selectChoiceCount2 * 2);
                node.writeInt(selectChoiceCount2);
                node.writeInt(prefixIndex);
                for (int i4 = fromIndex2; i4 < toIndex; i4++) {
                    byte rangeByte = byteStrings.get(i4).getByte(byteStringOffset);
                    if (i4 == fromIndex2 || rangeByte != byteStrings.get(i4 - 1).getByte(byteStringOffset)) {
                        int other$iv = 255 & rangeByte;
                        node.writeInt(other$iv);
                    }
                }
                Buffer childNodes2 = new Buffer();
                int rangeStart = fromIndex2;
                while (rangeStart < toIndex) {
                    byte rangeByte2 = byteStrings.get(rangeStart).getByte(byteStringOffset);
                    int rangeEnd2 = toIndex;
                    int i5 = rangeStart + 1;
                    while (true) {
                        if (i5 >= toIndex) {
                            i5 = rangeEnd2;
                            break;
                        }
                        int rangeEnd3 = rangeEnd2;
                        if (rangeByte2 != byteStrings.get(i5).getByte(byteStringOffset)) {
                            break;
                        }
                        i5++;
                        rangeEnd2 = rangeEnd3;
                    }
                    if (rangeStart + 1 == i5 && byteStringOffset + 1 == byteStrings.get(rangeStart).size()) {
                        node.writeInt(list.get(rangeStart).intValue());
                        rangeEnd = i5;
                        childNodes = childNodes2;
                        selectChoiceCount = selectChoiceCount2;
                        prefixIndex2 = prefixIndex;
                        from3 = from;
                        fromIndex3 = fromIndex2;
                        rangeStart = rangeEnd;
                        fromIndex2 = fromIndex3;
                        prefixIndex = prefixIndex2;
                        selectChoiceCount2 = selectChoiceCount;
                        childNodes2 = childNodes;
                        from = from3;
                        list = indexes;
                    }
                    node.writeInt(((int) (childNodesOffset + getIntCount(childNodes2))) * (-1));
                    int selectChoiceCount3 = byteStringOffset + 1;
                    rangeEnd = i5;
                    childNodes = childNodes2;
                    selectChoiceCount = selectChoiceCount2;
                    prefixIndex2 = prefixIndex;
                    from3 = from;
                    fromIndex3 = fromIndex2;
                    buildTrieRecursive(childNodesOffset, childNodes2, selectChoiceCount3, byteStrings, rangeStart, rangeEnd, indexes);
                    rangeStart = rangeEnd;
                    fromIndex2 = fromIndex3;
                    prefixIndex = prefixIndex2;
                    selectChoiceCount2 = selectChoiceCount;
                    childNodes2 = childNodes;
                    from = from3;
                    list = indexes;
                }
                node.writeAll(childNodes2);
                return;
            }
            int prefixIndex4 = prefixIndex;
            ByteString from5 = from;
            int fromIndex5 = fromIndex2;
            int min = Math.min(from5.size(), to.size());
            int scanByteCount = 0;
            int scanByteCount2 = byteStringOffset;
            while (true) {
                if (scanByteCount2 >= min) {
                    from2 = from5;
                    break;
                }
                from2 = from5;
                if (from2.getByte(scanByteCount2) != to.getByte(scanByteCount2)) {
                    break;
                }
                scanByteCount++;
                scanByteCount2++;
                from5 = from2;
            }
            long childNodesOffset2 = nodeOffset + getIntCount(node) + 2 + scanByteCount + 1;
            node.writeInt(-scanByteCount);
            node.writeInt(prefixIndex4);
            int i6 = byteStringOffset + scanByteCount;
            for (int i7 = byteStringOffset; i7 < i6; i7++) {
                byte $this$and$iv = from2.getByte(i7);
                node.writeInt($this$and$iv & 255);
            }
            if (fromIndex5 + 1 != toIndex) {
                Buffer childNodes3 = new Buffer();
                node.writeInt(((int) (childNodesOffset2 + getIntCount(childNodes3))) * (-1));
                buildTrieRecursive(childNodesOffset2, childNodes3, byteStringOffset + scanByteCount, byteStrings, fromIndex5, toIndex, indexes);
                node.writeAll(childNodes3);
                return;
            }
            if (byteStringOffset + scanByteCount == byteStrings.get(fromIndex5).size()) {
                node.writeInt(indexes.get(fromIndex5).intValue());
                return;
            }
            throw new IllegalStateException("Check failed.".toString());
        }

        private final long getIntCount(Buffer $this$intCount) {
            return $this$intCount.size() / 4;
        }
    }
}
