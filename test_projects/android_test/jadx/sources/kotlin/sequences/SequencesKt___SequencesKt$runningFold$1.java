package kotlin.sequences;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: Add missing generic type declarations: [R] */
/* compiled from: _Sequences.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003*\b\u0012\u0004\u0012\u0002H\u00030\u0004H\u008a@¢\u0006\u0004\b\u0005\u0010\u0006"}, m238d2 = {"<anonymous>", "", "T", "R", "Lkotlin/sequences/SequenceScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, m239k = 3, m240mv = {1, 4, 0})
@DebugMetadata(m246c = "kotlin.sequences.SequencesKt___SequencesKt$runningFold$1", m247f = "_Sequences.kt", m248i = {0, 1, 1, 1}, m249l = {2071, 2075}, m250m = "invokeSuspend", m251n = {"$this$sequence", "$this$sequence", "accumulator", "element"}, m252s = {"L$0", "L$0", "L$1", "L$2"})
/* loaded from: classes11.dex */
final class SequencesKt___SequencesKt$runningFold$1<R> extends RestrictedSuspendLambda implements Function2<SequenceScope<? super R>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Object $initial;
    final /* synthetic */ Function2 $operation;
    final /* synthetic */ Sequence $this_runningFold;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    int label;

    /* renamed from: p$ */
    private SequenceScope f269p$;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    SequencesKt___SequencesKt$runningFold$1(Sequence sequence, Object obj, Function2 function2, Continuation continuation) {
        super(2, continuation);
        this.$this_runningFold = sequence;
        this.$initial = obj;
        this.$operation = function2;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        SequencesKt___SequencesKt$runningFold$1 sequencesKt___SequencesKt$runningFold$1 = new SequencesKt___SequencesKt$runningFold$1(this.$this_runningFold, this.$initial, this.$operation, completion);
        sequencesKt___SequencesKt$runningFold$1.f269p$ = (SequenceScope) obj;
        return sequencesKt___SequencesKt$runningFold$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(Object obj, Continuation<? super Unit> continuation) {
        return ((SequencesKt___SequencesKt$runningFold$1) create(obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x0054  */
    /* JADX WARN: Removed duplicated region for block: B:14:0x0077  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:13:0x0070 -> B:7:0x0074). Please report as a decompilation issue!!! */
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
            r2 = 0
            switch(r1) {
                case 0: goto L31;
                case 1: goto L27;
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
            java.lang.Object r4 = r8.L$3
            java.util.Iterator r4 = (java.util.Iterator) r4
            java.lang.Object r3 = r8.L$2
            java.lang.Object r2 = r8.L$1
            java.lang.Object r5 = r8.L$0
            r1 = r5
            kotlin.sequences.SequenceScope r1 = (kotlin.sequences.SequenceScope) r1
            kotlin.ResultKt.throwOnFailure(r9)
            r5 = r4
            r4 = r8
            goto L74
        L27:
            r1 = r2
            java.lang.Object r2 = r8.L$0
            r1 = r2
            kotlin.sequences.SequenceScope r1 = (kotlin.sequences.SequenceScope) r1
            kotlin.ResultKt.throwOnFailure(r9)
            goto L44
        L31:
            kotlin.ResultKt.throwOnFailure(r9)
            kotlin.sequences.SequenceScope r1 = r8.f269p$
            java.lang.Object r2 = r8.$initial
            r8.L$0 = r1
            r3 = 1
            r8.label = r3
            java.lang.Object r2 = r1.yield(r2, r8)
            if (r2 != r0) goto L44
            return r0
        L44:
            java.lang.Object r2 = r8.$initial
            kotlin.sequences.Sequence r3 = r8.$this_runningFold
            java.util.Iterator r3 = r3.iterator()
            r4 = r3
            r3 = r8
        L4e:
            boolean r5 = r4.hasNext()
            if (r5 == 0) goto L77
            java.lang.Object r5 = r4.next()
            kotlin.jvm.functions.Function2 r6 = r3.$operation
            java.lang.Object r2 = r6.invoke(r2, r5)
            r3.L$0 = r1
            r3.L$1 = r2
            r3.L$2 = r5
            r3.L$3 = r4
            r6 = 2
            r3.label = r6
            java.lang.Object r6 = r1.yield(r2, r3)
            if (r6 != r0) goto L70
            return r0
        L70:
            r7 = r4
            r4 = r3
            r3 = r5
            r5 = r7
        L74:
            r3 = r4
            r4 = r5
            goto L4e
        L77:
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlin.sequences.SequencesKt___SequencesKt$runningFold$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
