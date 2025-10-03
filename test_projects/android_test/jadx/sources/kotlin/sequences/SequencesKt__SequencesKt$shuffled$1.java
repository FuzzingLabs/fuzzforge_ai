package kotlin.sequences;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.random.Random;

/* JADX INFO: Add missing generic type declarations: [T] */
/* compiled from: Sequences.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u0010\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u0003H\u008a@¢\u0006\u0004\b\u0004\u0010\u0005"}, m238d2 = {"<anonymous>", "", "T", "Lkotlin/sequences/SequenceScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, m239k = 3, m240mv = {1, 4, 0})
@DebugMetadata(m246c = "kotlin.sequences.SequencesKt__SequencesKt$shuffled$1", m247f = "Sequences.kt", m248i = {0, 0, 0, 0, 0}, m249l = {145}, m250m = "invokeSuspend", m251n = {"$this$sequence", "buffer", "j", "last", "value"}, m252s = {"L$0", "L$1", "I$0", "L$2", "L$3"})
/* loaded from: classes11.dex */
final class SequencesKt__SequencesKt$shuffled$1<T> extends RestrictedSuspendLambda implements Function2<SequenceScope<? super T>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Random $random;
    final /* synthetic */ Sequence $this_shuffled;
    int I$0;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    int label;

    /* renamed from: p$ */
    private SequenceScope f268p$;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    SequencesKt__SequencesKt$shuffled$1(Sequence sequence, Random random, Continuation continuation) {
        super(2, continuation);
        this.$this_shuffled = sequence;
        this.$random = random;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        SequencesKt__SequencesKt$shuffled$1 sequencesKt__SequencesKt$shuffled$1 = new SequencesKt__SequencesKt$shuffled$1(this.$this_shuffled, this.$random, completion);
        sequencesKt__SequencesKt$shuffled$1.f268p$ = (SequenceScope) obj;
        return sequencesKt__SequencesKt$shuffled$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(Object obj, Continuation<? super Unit> continuation) {
        return ((SequencesKt__SequencesKt$shuffled$1) create(obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:10:0x0046  */
    /* JADX WARN: Removed duplicated region for block: B:18:0x007a  */
    /* JADX WARN: Type inference failed for: r2v2 */
    /* JADX WARN: Type inference failed for: r2v3, types: [java.lang.Object, kotlin.sequences.SequenceScope] */
    /* JADX WARN: Type inference failed for: r2v6 */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:16:0x0073 -> B:7:0x0077). Please report as a decompilation issue!!! */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r9) {
        /*
            r8 = this;
            java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r1 = r8.label
            switch(r1) {
                case 0: goto L2d;
                case 1: goto L11;
                default: goto L9;
            }
        L9:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "call to 'resume' before 'invoke' with coroutine"
            r0.<init>(r1)
            throw r0
        L11:
            r1 = 0
            r2 = r1
            r3 = r1
            r4 = 0
            r5 = r1
            java.lang.Object r3 = r8.L$3
            java.lang.Object r1 = r8.L$2
            int r4 = r8.I$0
            java.lang.Object r6 = r8.L$1
            r5 = r6
            java.util.List r5 = (java.util.List) r5
            java.lang.Object r6 = r8.L$0
            r2 = r6
            kotlin.sequences.SequenceScope r2 = (kotlin.sequences.SequenceScope) r2
            kotlin.ResultKt.throwOnFailure(r9)
            r7 = r3
            r3 = r2
            r2 = r8
            goto L77
        L2d:
            kotlin.ResultKt.throwOnFailure(r9)
            kotlin.sequences.SequenceScope r1 = r8.f268p$
            kotlin.sequences.Sequence r2 = r8.$this_shuffled
            java.util.List r2 = kotlin.sequences.SequencesKt.toMutableList(r2)
            r5 = r2
            r2 = r1
            r1 = r8
        L3b:
            r3 = r5
            java.util.Collection r3 = (java.util.Collection) r3
            boolean r3 = r3.isEmpty()
            r4 = 1
            r3 = r3 ^ r4
            if (r3 == 0) goto L7a
            kotlin.random.Random r3 = r1.$random
            int r6 = r5.size()
            int r3 = r3.nextInt(r6)
            java.lang.Object r6 = kotlin.collections.CollectionsKt.removeLast(r5)
            int r7 = r5.size()
            if (r3 >= r7) goto L5f
            java.lang.Object r7 = r5.set(r3, r6)
            goto L60
        L5f:
            r7 = r6
        L60:
            r1.L$0 = r2
            r1.L$1 = r5
            r1.I$0 = r3
            r1.L$2 = r6
            r1.L$3 = r7
            r1.label = r4
            java.lang.Object r4 = r2.yield(r7, r1)
            if (r4 != r0) goto L73
            return r0
        L73:
            r4 = r3
            r3 = r2
            r2 = r1
            r1 = r6
        L77:
            r1 = r2
            r2 = r3
            goto L3b
        L7a:
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlin.sequences.SequencesKt__SequencesKt$shuffled$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
