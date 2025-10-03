package kotlin.time;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: formatToDecimals.kt */
@Metadata(m236bv = {1, 0, 3}, m237d1 = {"\u0000.\n\u0000\n\u0002\u0010\u0011\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0003\u001a\u0010\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH\u0002\u001a\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0000\u001a\u0018\u0010\u0010\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\n\u001a\u00020\u000bH\u0000\u001a\u0018\u0010\u0011\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\n\u001a\u00020\u000bH\u0000\"\u001c\u0010\u0000\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00030\u00020\u0001X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0004\"\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004¢\u0006\u0002\n\u0000\"\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u0004¢\u0006\u0002\n\u0000\"\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00030\u0002X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\u0012"}, m238d2 = {"precisionFormats", "", "Ljava/lang/ThreadLocal;", "Ljava/text/DecimalFormat;", "[Ljava/lang/ThreadLocal;", "rootNegativeExpFormatSymbols", "Ljava/text/DecimalFormatSymbols;", "rootPositiveExpFormatSymbols", "scientificFormat", "createFormatForDecimals", "decimals", "", "formatScientific", "", "value", "", "formatToExactDecimals", "formatUpToDecimals", "kotlin-stdlib"}, m239k = 2, m240mv = {1, 4, 0})
/* loaded from: classes11.dex */
public final class FormatToDecimalsKt {
    private static final ThreadLocal<DecimalFormat>[] precisionFormats;
    private static final DecimalFormatSymbols rootNegativeExpFormatSymbols;
    private static final DecimalFormatSymbols rootPositiveExpFormatSymbols;
    private static final ThreadLocal<DecimalFormat> scientificFormat;

    static {
        DecimalFormatSymbols $this$apply = new DecimalFormatSymbols(Locale.ROOT);
        $this$apply.setExponentSeparator("e");
        rootNegativeExpFormatSymbols = $this$apply;
        DecimalFormatSymbols $this$apply2 = new DecimalFormatSymbols(Locale.ROOT);
        $this$apply2.setExponentSeparator("e+");
        rootPositiveExpFormatSymbols = $this$apply2;
        ThreadLocal<DecimalFormat>[] threadLocalArr = new ThreadLocal[4];
        for (int i = 0; i < 4; i++) {
            threadLocalArr[i] = new ThreadLocal<>();
        }
        precisionFormats = threadLocalArr;
        scientificFormat = new ThreadLocal<>();
    }

    private static final DecimalFormat createFormatForDecimals(int decimals) {
        DecimalFormat $this$apply = new DecimalFormat("0", rootNegativeExpFormatSymbols);
        if (decimals > 0) {
            $this$apply.setMinimumFractionDigits(decimals);
        }
        $this$apply.setRoundingMode(RoundingMode.HALF_UP);
        return $this$apply;
    }

    public static final String formatToExactDecimals(double value, int decimals) {
        DecimalFormat createFormatForDecimals;
        ThreadLocal<DecimalFormat>[] threadLocalArr = precisionFormats;
        if (decimals < threadLocalArr.length) {
            ThreadLocal<DecimalFormat> threadLocal = threadLocalArr[decimals];
            DecimalFormat decimalFormat = threadLocal.get();
            if (decimalFormat == null) {
                decimalFormat = createFormatForDecimals(decimals);
                threadLocal.set(decimalFormat);
            }
            createFormatForDecimals = decimalFormat;
        } else {
            createFormatForDecimals = createFormatForDecimals(decimals);
        }
        DecimalFormat format = createFormatForDecimals;
        String format2 = format.format(value);
        Intrinsics.checkNotNullExpressionValue(format2, "format.format(value)");
        return format2;
    }

    public static final String formatUpToDecimals(double value, int decimals) {
        DecimalFormat $this$apply = createFormatForDecimals(0);
        $this$apply.setMaximumFractionDigits(decimals);
        String format = $this$apply.format(value);
        Intrinsics.checkNotNullExpressionValue(format, "createFormatForDecimals(… }\n        .format(value)");
        return format;
    }

    public static final String formatScientific(double value) {
        ThreadLocal<DecimalFormat> threadLocal = scientificFormat;
        DecimalFormat $this$apply = threadLocal.get();
        if ($this$apply == null) {
            DecimalFormat $this$apply2 = new DecimalFormat("0E0", rootNegativeExpFormatSymbols);
            $this$apply2.setMinimumFractionDigits(2);
            threadLocal.set($this$apply2);
            $this$apply = $this$apply2;
        }
        $this$apply.setDecimalFormatSymbols((value >= ((double) 1) || value <= ((double) (-1))) ? rootPositiveExpFormatSymbols : rootNegativeExpFormatSymbols);
        String format = $this$apply.format(value);
        Intrinsics.checkNotNullExpressionValue(format, "scientificFormat.getOrSe… }\n        .format(value)");
        return format;
    }
}
