package kotlin.sequences;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;

/* JADX INFO: Add missing generic type declarations: [R] */
/* compiled from: Sequences.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002\"\u0004\b\u0001\u0010\u0003\"\u0004\b\u0002\u0010\u0004*\b\u0012\u0004\u0012\u0002H\u00040\u0005H\u008a@¢\u0006\u0004\b\u0006\u0010\u0007"}, m238d2 = {"<anonymous>", "", "T", "C", "R", "Lkotlin/sequences/SequenceScope;", "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"}, m239k = 3, m240mv = {1, 4, 0})
@DebugMetadata(m246c = "kotlin.sequences.SequencesKt__SequencesKt$flatMapIndexed$1", m247f = "Sequences.kt", m248i = {0, 0, 0, 0}, m249l = {332}, m250m = "invokeSuspend", m251n = {"$this$sequence", "index", "element", "result"}, m252s = {"L$0", "I$0", "L$1", "L$3"})
/* loaded from: classes11.dex */
final class SequencesKt__SequencesKt$flatMapIndexed$1<R> extends RestrictedSuspendLambda implements Function2<SequenceScope<? super R>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Function1 $iterator;
    final /* synthetic */ Sequence $source;
    final /* synthetic */ Function2 $transform;
    int I$0;
    Object L$0;
    Object L$1;
    Object L$2;
    Object L$3;
    int label;

    /* renamed from: p$ */
    private SequenceScope f266p$;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    SequencesKt__SequencesKt$flatMapIndexed$1(Sequence sequence, Function2 function2, Function1 function1, Continuation continuation) {
        super(2, continuation);
        this.$source = sequence;
        this.$transform = function2;
        this.$iterator = function1;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    public final Continuation<Unit> create(Object obj, Continuation<?> completion) {
        Intrinsics.checkNotNullParameter(completion, "completion");
        SequencesKt__SequencesKt$flatMapIndexed$1 sequencesKt__SequencesKt$flatMapIndexed$1 = new SequencesKt__SequencesKt$flatMapIndexed$1(this.$source, this.$transform, this.$iterator, completion);
        sequencesKt__SequencesKt$flatMapIndexed$1.f266p$ = (SequenceScope) obj;
        return sequencesKt__SequencesKt$flatMapIndexed$1;
    }

    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(Object obj, Continuation<? super Unit> continuation) {
        return ((SequencesKt__SequencesKt$flatMapIndexed$1) create(obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARN: Removed duplicated region for block: B:10:0x003e  */
    /* JADX WARN: Removed duplicated region for block: B:17:0x007a  */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:16:0x006f -> B:7:0x0075). Please report as a decompilation issue!!! */
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
            switch(r1) {
                case 0: goto L2a;
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
            java.lang.Object r3 = r9.L$3
            java.lang.Object r5 = r9.L$2
            java.util.Iterator r5 = (java.util.Iterator) r5
            java.lang.Object r1 = r9.L$1
            int r4 = r9.I$0
            java.lang.Object r6 = r9.L$0
            r2 = r6
            kotlin.sequences.SequenceScope r2 = (kotlin.sequences.SequenceScope) r2
            kotlin.ResultKt.throwOnFailure(r10)
            r6 = r5
            r5 = r9
            goto L75
        L2a:
            kotlin.ResultKt.throwOnFailure(r10)
            kotlin.sequences.SequenceScope r1 = r9.f266p$
            r2 = 0
            kotlin.sequences.Sequence r3 = r9.$source
            java.util.Iterator r3 = r3.iterator()
            r5 = r3
            r3 = r9
        L38:
            boolean r4 = r5.hasNext()
            if (r4 == 0) goto L7a
            java.lang.Object r4 = r5.next()
            kotlin.jvm.functions.Function2 r6 = r3.$transform
            int r7 = r2 + 1
            if (r2 >= 0) goto L4b
            kotlin.collections.CollectionsKt.throwIndexOverflow()
        L4b:
            java.lang.Integer r2 = kotlin.coroutines.jvm.internal.Boxing.boxInt(r2)
            java.lang.Object r2 = r6.invoke(r2, r4)
            kotlin.jvm.functions.Function1 r6 = r3.$iterator
            java.lang.Object r6 = r6.invoke(r2)
            java.util.Iterator r6 = (java.util.Iterator) r6
            r3.L$0 = r1
            r3.I$0 = r7
            r3.L$1 = r4
            r3.L$2 = r5
            r3.L$3 = r2
            r8 = 1
            r3.label = r8
            java.lang.Object r6 = r1.yieldAll(r6, r3)
            if (r6 != r0) goto L6f
            return r0
        L6f:
            r6 = r5
            r5 = r3
            r3 = r2
            r2 = r1
            r1 = r4
            r4 = r7
        L75:
            r1 = r2
            r2 = r4
            r3 = r5
            r5 = r6
            goto L38
        L7a:
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlin.sequences.SequencesKt__SequencesKt$flatMapIndexed$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
