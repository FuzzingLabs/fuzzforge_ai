package kotlin.sequences;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: Add missing generic type declarations: [S] */
/* compiled from: _Sequences.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\b\b\u0001\u0010\u0003*\u0002H\u0002*\b\u0012\u0004\u0012\u0002H\u00020\u0004H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006"}, m238d2 = {"<anonymous>", "", "S", "T", "Lkotlin/sequences/SequenceScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, m239k = 3, m240mv = {1, 4, 0})
@DebugMetadata(m246c = "kotlin.sequences.SequencesKt___SequencesKt$runningReduceIndexed$1", m247f = "_Sequences.kt", m248i = {0, 0, 0, 1, 1, 1, 1}, m249l = {2158, 2162}, m250m = "invokeSuspend", m251n = {"$this$sequence", "iterator", "accumulator", "$this$sequence", "iterator", "accumulator", "index"}, m252s = {"L$0", "L$1", "L$2", "L$0", "L$1", "L$2", "I$0"})
/* loaded from: classes11.dex */
final class SequencesKt___SequencesKt$runningReduceIndexed$1<S> extends RestrictedSuspendLambda implements Function2<SequenceScope<? super S>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Function3 $operation;
    final /* synthetic */ Sequence $this_runningReduceIndexed;
    int I$0;
    Object L$0;
    Object L$1;
    Object L$2;
    int label;

    /* renamed from: p$ */
    private SequenceScope f272p$;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    SequencesKt___SequencesKt$runningReduceIndexed$1(Sequence sequence, Function3 function3, Continuation continuation) {
        super(2, continuation);
        this.$this_runningReduceIndexed = sequence;
        this.$operation = function3;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        SequencesKt___SequencesKt$runningReduceIndexed$1 sequencesKt___SequencesKt$runningReduceIndexed$1 = new SequencesKt___SequencesKt$runningReduceIndexed$1(this.$this_runningReduceIndexed, this.$operation, completion);
        sequencesKt___SequencesKt$runningReduceIndexed$1.f272p$ = (SequenceScope) obj;
        return sequencesKt___SequencesKt$runningReduceIndexed$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(Object obj, Continuation<? super Unit> continuation) {
        return ((SequencesKt___SequencesKt$runningReduceIndexed$1) create(obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x0067  */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public final java.lang.Object invokeSuspend(java.lang.Object r10) {
        /*
            r9 = this;
            java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r1 = r9.label
            r2 = 0
            switch(r1) {
                case 0: goto L3a;
                case 1: goto L28;
                case 2: goto L12;
                default: goto La;
            }
        La:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "call to 'resume' before 'invoke' with coroutine"
            r0.<init>(r1)
            throw r0
        L12:
            r1 = r2
            r3 = r2
            r4 = 0
            int r4 = r9.I$0
            java.lang.Object r3 = r9.L$2
            java.lang.Object r5 = r9.L$1
            r2 = r5
            java.util.Iterator r2 = (java.util.Iterator) r2
            java.lang.Object r5 = r9.L$0
            r1 = r5
            kotlin.sequences.SequenceScope r1 = (kotlin.sequences.SequenceScope) r1
            kotlin.ResultKt.throwOnFailure(r10)
            r5 = r9
            goto L8f
        L28:
            r1 = r2
            r3 = r2
            java.lang.Object r3 = r9.L$2
            java.lang.Object r4 = r9.L$1
            r2 = r4
            java.util.Iterator r2 = (java.util.Iterator) r2
            java.lang.Object r4 = r9.L$0
            r1 = r4
            kotlin.sequences.SequenceScope r1 = (kotlin.sequences.SequenceScope) r1
            kotlin.ResultKt.throwOnFailure(r10)
            goto L5f
        L3a:
            kotlin.ResultKt.throwOnFailure(r10)
            kotlin.sequences.SequenceScope r1 = r9.f272p$
            kotlin.sequences.Sequence r2 = r9.$this_runningReduceIndexed
            java.util.Iterator r2 = r2.iterator()
            boolean r3 = r2.hasNext()
            if (r3 == 0) goto L90
            java.lang.Object r3 = r2.next()
            r9.L$0 = r1
            r9.L$1 = r2
            r9.L$2 = r3
            r4 = 1
            r9.label = r4
            java.lang.Object r4 = r1.yield(r3, r9)
            if (r4 != r0) goto L5f
            return r0
        L5f:
            r4 = 1
            r5 = r9
        L61:
            boolean r6 = r2.hasNext()
            if (r6 == 0) goto L91
            kotlin.jvm.functions.Function3 r6 = r5.$operation
            int r7 = r4 + 1
            if (r4 >= 0) goto L70
            kotlin.collections.CollectionsKt.throwIndexOverflow()
        L70:
            java.lang.Integer r4 = kotlin.coroutines.jvm.internal.Boxing.boxInt(r4)
            java.lang.Object r8 = r2.next()
            java.lang.Object r3 = r6.invoke(r4, r3, r8)
            r5.L$0 = r1
            r5.L$1 = r2
            r5.L$2 = r3
            r5.I$0 = r7
            r4 = 2
            r5.label = r4
            java.lang.Object r4 = r1.yield(r3, r5)
            if (r4 != r0) goto L8e
            return r0
        L8e:
            r4 = r7
        L8f:
            goto L61
        L90:
            r5 = r9
        L91:
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlin.sequences.SequencesKt___SequencesKt$runningReduceIndexed$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
