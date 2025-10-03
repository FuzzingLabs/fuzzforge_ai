package okio;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Okio.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000\u0018\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\u001a\r\u0010\u0000\u001a\u00020\u0001H\u0007¢\u0006\u0002\b\u0002\u001a\n\u0010\u0003\u001a\u00020\u0004*\u00020\u0001\u001a\n\u0010\u0003\u001a\u00020\u0005*\u00020\u0006¨\u0006\u0007"}, m238d2 = {"blackholeSink", "Lokio/Sink;", "blackhole", "buffer", "Lokio/BufferedSink;", "Lokio/BufferedSource;", "Lokio/Source;", "okio"}, m239k = 5, m240mv = {1, 4, 0}, m243xs = "okio/Okio")
/* loaded from: classes11.dex */
final /* synthetic */ class Okio__OkioKt {
    public static final BufferedSource buffer(Source buffer) {
        Intrinsics.checkNotNullParameter(buffer, "$this$buffer");
        return new buffer(buffer);
    }

    public static final BufferedSink buffer(Sink buffer) {
        Intrinsics.checkNotNullParameter(buffer, "$this$buffer");
        return new buffer(buffer);
    }

    public static final Sink blackhole() {
        return new BlackholeSink();
    }
}
