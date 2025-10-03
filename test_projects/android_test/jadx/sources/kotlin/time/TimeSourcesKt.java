package kotlin.time;

import kotlin.Deprecated;
import kotlin.DeprecationLevel;
import kotlin.Metadata;
import kotlin.ReplaceWith;

/* compiled from: TimeSources.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000B\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004*\\\b\u0007\u0010\u0000\"\u00020\u00012\u00020\u0001B\f\b\u0002\u0012\b\b\u0003\u0012\u0004\b\b(\u0004B\u0002\b\u0005B<\b\u0006\u0012\b\b\u0007\u0012\u0004\b\b(\b\u0012\"\b\t\u0012\u001e\b\u000bB\u001a\b\n\u0012\f\b\u000b\u0012\b\b\fJ\u0004\b\b(\f\u0012\b\b\r\u0012\u0004\b\b(\u000e\u0012\n\b\u000f\u0012\u0006\b\n0\u00108\u0011*\\\b\u0007\u0010\u0012\"\u00020\u00132\u00020\u0013B\f\b\u0002\u0012\b\b\u0003\u0012\u0004\b\b(\u0004B\u0002\b\u0005B<\b\u0006\u0012\b\b\u0007\u0012\u0004\b\b(\u0014\u0012\"\b\t\u0012\u001e\b\u000bB\u001a\b\n\u0012\f\b\u000b\u0012\b\b\fJ\u0004\b\b(\u0015\u0012\b\b\r\u0012\u0004\b\b(\u0016\u0012\n\b\u000f\u0012\u0006\b\n0\u00108\u0011*\\\b\u0007\u0010\u0017\"\u00020\u00182\u00020\u0018B\f\b\u0002\u0012\b\b\u0003\u0012\u0004\b\b(\u0004B\u0002\b\u0005B<\b\u0006\u0012\b\b\u0007\u0012\u0004\b\b(\u0019\u0012\"\b\t\u0012\u001e\b\u000bB\u001a\b\n\u0012\f\b\u000b\u0012\b\b\fJ\u0004\b\b(\u001a\u0012\b\b\r\u0012\u0004\b\b(\u001b\u0012\n\b\u000f\u0012\u0006\b\n0\u00108\u0011*\\\b\u0007\u0010\u001c\"\u00020\u001d2\u00020\u001dB\f\b\u0002\u0012\b\b\u0003\u0012\u0004\b\b(\u0004B\u0002\b\u0005B<\b\u0006\u0012\b\b\u0007\u0012\u0004\b\b(\u001e\u0012\"\b\t\u0012\u001e\b\u000bB\u001a\b\n\u0012\f\b\u000b\u0012\b\b\fJ\u0004\b\b(\u001f\u0012\b\b\r\u0012\u0004\b\b( \u0012\n\b\u000f\u0012\u0006\b\n0\u00108\u0011¨\u0006!"}, m238d2 = {"AbstractDoubleClock", "Lkotlin/time/AbstractDoubleTimeSource;", "Lkotlin/SinceKotlin;", "version", "1.3", "Lkotlin/time/ExperimentalTime;", "Lkotlin/Deprecated;", "message", "Use AbstractDoubleTimeSource instead.", "replaceWith", "Lkotlin/ReplaceWith;", "imports", "kotlin.time.AbstractDoubleTimeSource", "expression", "AbstractDoubleTimeSource", "level", "Lkotlin/DeprecationLevel;", "ERROR", "AbstractLongClock", "Lkotlin/time/AbstractLongTimeSource;", "Use AbstractLongTimeSource instead.", "kotlin.time.AbstractLongTimeSource", "AbstractLongTimeSource", "MonoClock", "Lkotlin/time/TimeSource$Monotonic;", "Use TimeSource.Monotonic instead.", "kotlin.time.TimeSource", "TimeSource.Monotonic", "TestClock", "Lkotlin/time/TestTimeSource;", "Use TestTimeSource instead.", "kotlin.time.TestTimeSource", "TestTimeSource", "kotlin-stdlib"}, m239k = 2, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class TimeSourcesKt {
    @Deprecated(level = DeprecationLevel.ERROR, message = "Use AbstractDoubleTimeSource instead.", replaceWith = @ReplaceWith(expression = "AbstractDoubleTimeSource", imports = {"kotlin.time.AbstractDoubleTimeSource"}))
    public static /* synthetic */ void AbstractDoubleClock$annotations() {
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Use AbstractLongTimeSource instead.", replaceWith = @ReplaceWith(expression = "AbstractLongTimeSource", imports = {"kotlin.time.AbstractLongTimeSource"}))
    public static /* synthetic */ void AbstractLongClock$annotations() {
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Use TimeSource.Monotonic instead.", replaceWith = @ReplaceWith(expression = "TimeSource.Monotonic", imports = {"kotlin.time.TimeSource"}))
    public static /* synthetic */ void MonoClock$annotations() {
    }

    @Deprecated(level = DeprecationLevel.ERROR, message = "Use TestTimeSource instead.", replaceWith = @ReplaceWith(expression = "TestTimeSource", imports = {"kotlin.time.TestTimeSource"}))
    public static /* synthetic */ void TestClock$annotations() {
    }
}
