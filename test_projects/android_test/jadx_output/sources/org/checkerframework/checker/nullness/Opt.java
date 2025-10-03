package org.checkerframework.checker.nullness;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;

/* loaded from: classes11.dex */
public final class Opt {
    private Opt() {
        throw new AssertionError("shouldn't be instantiated");
    }

    public static <T> T get(T primary) {
        if (primary == null) {
            throw new NoSuchElementException("No value present");
        }
        return primary;
    }

    @EnsuresNonNullIf(expression = {"#1"}, result = true)
    public static boolean isPresent(Object primary) {
        return primary != null;
    }

    public static <T> void ifPresent(T primary, Consumer<? super T> consumer) {
        if (primary != null) {
            consumer.accept(primary);
        }
    }

    public static <T> T filter(T primary, Predicate<? super T> predicate) {
        if (primary != null && predicate.test(primary)) {
            return primary;
        }
        return null;
    }

    public static <T, U> U map(T primary, Function<? super T, ? extends U> mapper) {
        if (primary == null) {
            return null;
        }
        return mapper.apply(primary);
    }

    public static <T> T orElse(T primary, T other) {
        return primary != null ? primary : other;
    }

    public static <T> T orElseGet(T primary, Supplier<? extends T> other) {
        return primary != null ? primary : other.get();
    }

    public static <T, X extends Throwable> T orElseThrow(T primary, Supplier<? extends X> exceptionSupplier) throws Throwable {
        if (primary != null) {
            return primary;
        }
        throw exceptionSupplier.get();
    }
}
