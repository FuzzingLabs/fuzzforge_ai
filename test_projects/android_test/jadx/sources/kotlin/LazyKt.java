package kotlin;

@Metadata(m236bv = {1, 0, 3}, m237d1 = {"kotlin/LazyKt__LazyJVMKt", "kotlin/LazyKt__LazyKt"}, m239k = 4, m240mv = {1, 4, 0}, m242xi = 1)
/* loaded from: classes11.dex */
public final class LazyKt extends LazyKt__LazyKt {

    @Metadata(m236bv = {1, 0, 3}, m239k = 3, m240mv = {1, 4, 0})
    public final /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[LazyThreadSafetyMode.values().length];
            $EnumSwitchMapping$0 = iArr;
            iArr[LazyThreadSafetyMode.SYNCHRONIZED.ordinal()] = 1;
            iArr[LazyThreadSafetyMode.PUBLICATION.ordinal()] = 2;
            iArr[LazyThreadSafetyMode.NONE.ordinal()] = 3;
        }
    }

    private LazyKt() {
    }
}
