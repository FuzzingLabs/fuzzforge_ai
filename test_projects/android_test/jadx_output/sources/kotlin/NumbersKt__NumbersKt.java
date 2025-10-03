package kotlin;

/* compiled from: Numbers.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\b\n\u0002\u0010\u0005\n\u0002\u0010\n\n\u0002\b\b\u001a\r\u0010\u0000\u001a\u00020\u0001*\u00020\u0002H\u0087\b\u001a\r\u0010\u0000\u001a\u00020\u0001*\u00020\u0003H\u0087\b\u001a\r\u0010\u0004\u001a\u00020\u0001*\u00020\u0002H\u0087\b\u001a\r\u0010\u0004\u001a\u00020\u0001*\u00020\u0003H\u0087\b\u001a\r\u0010\u0005\u001a\u00020\u0001*\u00020\u0002H\u0087\b\u001a\r\u0010\u0005\u001a\u00020\u0001*\u00020\u0003H\u0087\b\u001a\u0014\u0010\u0006\u001a\u00020\u0002*\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0001H\u0007\u001a\u0014\u0010\u0006\u001a\u00020\u0003*\u00020\u00032\u0006\u0010\u0007\u001a\u00020\u0001H\u0007\u001a\u0014\u0010\b\u001a\u00020\u0002*\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0001H\u0007\u001a\u0014\u0010\b\u001a\u00020\u0003*\u00020\u00032\u0006\u0010\u0007\u001a\u00020\u0001H\u0007\u001a\r\u0010\t\u001a\u00020\u0002*\u00020\u0002H\u0087\b\u001a\r\u0010\t\u001a\u00020\u0003*\u00020\u0003H\u0087\b\u001a\r\u0010\n\u001a\u00020\u0002*\u00020\u0002H\u0087\b\u001a\r\u0010\n\u001a\u00020\u0003*\u00020\u0003H\u0087\b¨\u0006\u000b"}, m238d2 = {"countLeadingZeroBits", "", "", "", "countOneBits", "countTrailingZeroBits", "rotateLeft", "bitCount", "rotateRight", "takeHighestOneBit", "takeLowestOneBit", "kotlin-stdlib"}, m239k = 5, m240mv = {1, 4, 0}, m242xi = 1, m243xs = "kotlin/NumbersKt")
/* loaded from: classes11.dex */
class NumbersKt__NumbersKt extends NumbersKt__NumbersJVMKt {
    private static final int countOneBits(byte $this$countOneBits) {
        return Integer.bitCount($this$countOneBits & 255);
    }

    private static final int countLeadingZeroBits(byte $this$countLeadingZeroBits) {
        return Integer.numberOfLeadingZeros($this$countLeadingZeroBits & 255) - 24;
    }

    private static final int countTrailingZeroBits(byte $this$countTrailingZeroBits) {
        return Integer.numberOfTrailingZeros($this$countTrailingZeroBits | 256);
    }

    private static final byte takeHighestOneBit(byte $this$takeHighestOneBit) {
        return (byte) Integer.highestOneBit($this$takeHighestOneBit & 255);
    }

    private static final byte takeLowestOneBit(byte $this$takeLowestOneBit) {
        return (byte) Integer.lowestOneBit($this$takeLowestOneBit);
    }

    public static final byte rotateLeft(byte $this$rotateLeft, int bitCount) {
        return (byte) (($this$rotateLeft << (bitCount & 7)) | (($this$rotateLeft & 255) >>> (8 - (bitCount & 7))));
    }

    public static final byte rotateRight(byte $this$rotateRight, int bitCount) {
        return (byte) (($this$rotateRight << (8 - (bitCount & 7))) | (($this$rotateRight & 255) >>> (bitCount & 7)));
    }

    private static final int countOneBits(short $this$countOneBits) {
        return Integer.bitCount(65535 & $this$countOneBits);
    }

    private static final int countLeadingZeroBits(short $this$countLeadingZeroBits) {
        return Integer.numberOfLeadingZeros(65535 & $this$countLeadingZeroBits) - 16;
    }

    private static final int countTrailingZeroBits(short $this$countTrailingZeroBits) {
        return Integer.numberOfTrailingZeros(65536 | $this$countTrailingZeroBits);
    }

    private static final short takeHighestOneBit(short $this$takeHighestOneBit) {
        return (short) Integer.highestOneBit(65535 & $this$takeHighestOneBit);
    }

    private static final short takeLowestOneBit(short $this$takeLowestOneBit) {
        return (short) Integer.lowestOneBit($this$takeLowestOneBit);
    }

    public static final short rotateLeft(short $this$rotateLeft, int bitCount) {
        return (short) (($this$rotateLeft << (bitCount & 15)) | ((65535 & $this$rotateLeft) >>> (16 - (bitCount & 15))));
    }

    public static final short rotateRight(short $this$rotateRight, int bitCount) {
        return (short) (($this$rotateRight << (16 - (bitCount & 15))) | ((65535 & $this$rotateRight) >>> (bitCount & 15)));
    }
}
