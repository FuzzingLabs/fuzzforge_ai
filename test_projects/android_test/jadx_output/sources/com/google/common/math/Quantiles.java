package com.google.common.math;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public final class Quantiles {
    public static ScaleAndIndex median() {
        return scale(2).index(1);
    }

    public static Scale quartiles() {
        return scale(4);
    }

    public static Scale percentiles() {
        return scale(100);
    }

    public static Scale scale(int scale) {
        return new Scale(scale);
    }

    /* loaded from: classes.dex */
    public static final class Scale {
        private final int scale;

        private Scale(int scale) {
            Preconditions.checkArgument(scale > 0, "Quantile scale must be positive");
            this.scale = scale;
        }

        public ScaleAndIndex index(int index) {
            return new ScaleAndIndex(this.scale, index);
        }

        public ScaleAndIndexes indexes(int... indexes) {
            return new ScaleAndIndexes(this.scale, (int[]) indexes.clone());
        }

        public ScaleAndIndexes indexes(Collection<Integer> indexes) {
            return new ScaleAndIndexes(this.scale, Ints.toArray(indexes));
        }
    }

    /* loaded from: classes.dex */
    public static final class ScaleAndIndex {
        private final int index;
        private final int scale;

        private ScaleAndIndex(int scale, int index) {
            Quantiles.checkIndex(index, scale);
            this.scale = scale;
            this.index = index;
        }

        public double compute(Collection<? extends Number> dataset) {
            return computeInPlace(Doubles.toArray(dataset));
        }

        public double compute(double... dataset) {
            return computeInPlace((double[]) dataset.clone());
        }

        public double compute(long... dataset) {
            return computeInPlace(Quantiles.longsToDoubles(dataset));
        }

        public double compute(int... dataset) {
            return computeInPlace(Quantiles.intsToDoubles(dataset));
        }

        public double computeInPlace(double... dataset) {
            Preconditions.checkArgument(dataset.length > 0, "Cannot calculate quantiles of an empty dataset");
            if (Quantiles.containsNaN(dataset)) {
                return Double.NaN;
            }
            long numerator = this.index * (dataset.length - 1);
            int quotient = (int) LongMath.divide(numerator, this.scale, RoundingMode.DOWN);
            int remainder = (int) (numerator - (quotient * this.scale));
            Quantiles.selectInPlace(quotient, dataset, 0, dataset.length - 1);
            if (remainder != 0) {
                Quantiles.selectInPlace(quotient + 1, dataset, quotient + 1, dataset.length - 1);
                return Quantiles.interpolate(dataset[quotient], dataset[quotient + 1], remainder, this.scale);
            }
            return dataset[quotient];
        }
    }

    /* loaded from: classes.dex */
    public static final class ScaleAndIndexes {
        private final int[] indexes;
        private final int scale;

        private ScaleAndIndexes(int scale, int[] indexes) {
            for (int index : indexes) {
                Quantiles.checkIndex(index, scale);
            }
            this.scale = scale;
            this.indexes = indexes;
        }

        public Map<Integer, Double> compute(Collection<? extends Number> dataset) {
            return computeInPlace(Doubles.toArray(dataset));
        }

        public Map<Integer, Double> compute(double... dataset) {
            return computeInPlace((double[]) dataset.clone());
        }

        public Map<Integer, Double> compute(long... dataset) {
            return computeInPlace(Quantiles.longsToDoubles(dataset));
        }

        public Map<Integer, Double> compute(int... dataset) {
            return computeInPlace(Quantiles.intsToDoubles(dataset));
        }

        public Map<Integer, Double> computeInPlace(double... dataset) {
            double[] dArr = dataset;
            Preconditions.checkArgument(dArr.length > 0, "Cannot calculate quantiles of an empty dataset");
            if (Quantiles.containsNaN(dataset)) {
                Map<Integer, Double> nanMap = new HashMap<>();
                for (int index : this.indexes) {
                    nanMap.put(Integer.valueOf(index), Double.valueOf(Double.NaN));
                }
                return Collections.unmodifiableMap(nanMap);
            }
            int[] iArr = this.indexes;
            int[] quotients = new int[iArr.length];
            int[] remainders = new int[iArr.length];
            int[] requiredSelections = new int[iArr.length * 2];
            int i = 0;
            int requiredSelectionsCount = 0;
            while (true) {
                if (i >= this.indexes.length) {
                    break;
                }
                long numerator = r1[i] * (dArr.length - 1);
                int quotient = (int) LongMath.divide(numerator, this.scale, RoundingMode.DOWN);
                int remainder = (int) (numerator - (quotient * this.scale));
                quotients[i] = quotient;
                remainders[i] = remainder;
                requiredSelections[requiredSelectionsCount] = quotient;
                requiredSelectionsCount++;
                if (remainder != 0) {
                    requiredSelections[requiredSelectionsCount] = quotient + 1;
                    requiredSelectionsCount++;
                }
                i++;
            }
            Arrays.sort(requiredSelections, 0, requiredSelectionsCount);
            Quantiles.selectAllInPlace(requiredSelections, 0, requiredSelectionsCount - 1, dataset, 0, dArr.length - 1);
            Map<Integer, Double> ret = new HashMap<>();
            int i2 = 0;
            while (true) {
                int[] iArr2 = this.indexes;
                if (i2 < iArr2.length) {
                    int quotient2 = quotients[i2];
                    int remainder2 = remainders[i2];
                    if (remainder2 == 0) {
                        ret.put(Integer.valueOf(iArr2[i2]), Double.valueOf(dArr[quotient2]));
                    } else {
                        ret.put(Integer.valueOf(iArr2[i2]), Double.valueOf(Quantiles.interpolate(dArr[quotient2], dArr[quotient2 + 1], remainder2, this.scale)));
                    }
                    i2++;
                    dArr = dataset;
                } else {
                    return Collections.unmodifiableMap(ret);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean containsNaN(double... dataset) {
        for (double value : dataset) {
            if (Double.isNaN(value)) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static double interpolate(double lower, double upper, double remainder, double scale) {
        if (lower == Double.NEGATIVE_INFINITY) {
            if (upper != Double.POSITIVE_INFINITY) {
                return Double.NEGATIVE_INFINITY;
            }
            return Double.NaN;
        }
        if (upper == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        }
        return (((upper - lower) * remainder) / scale) + lower;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void checkIndex(int index, int scale) {
        if (index < 0 || index > scale) {
            throw new IllegalArgumentException("Quantile indexes must be between 0 and the scale, which is " + scale);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static double[] longsToDoubles(long[] longs) {
        int len = longs.length;
        double[] doubles = new double[len];
        for (int i = 0; i < len; i++) {
            doubles[i] = longs[i];
        }
        return doubles;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static double[] intsToDoubles(int[] ints) {
        int len = ints.length;
        double[] doubles = new double[len];
        for (int i = 0; i < len; i++) {
            doubles[i] = ints[i];
        }
        return doubles;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void selectInPlace(int required, double[] array, int from, int to) {
        if (required == from) {
            int min = from;
            for (int index = from + 1; index <= to; index++) {
                if (array[min] > array[index]) {
                    min = index;
                }
            }
            if (min != from) {
                swap(array, min, from);
                return;
            }
            return;
        }
        while (to > from) {
            int partitionPoint = partition(array, from, to);
            if (partitionPoint >= required) {
                to = partitionPoint - 1;
            }
            if (partitionPoint <= required) {
                from = partitionPoint + 1;
            }
        }
    }

    private static int partition(double[] array, int from, int to) {
        movePivotToStartOfSlice(array, from, to);
        double pivot = array[from];
        int partitionPoint = to;
        for (int i = to; i > from; i--) {
            if (array[i] > pivot) {
                swap(array, partitionPoint, i);
                partitionPoint--;
            }
        }
        swap(array, from, partitionPoint);
        return partitionPoint;
    }

    private static void movePivotToStartOfSlice(double[] array, int from, int to) {
        int mid = (from + to) >>> 1;
        boolean toLessThanMid = array[to] < array[mid];
        boolean midLessThanFrom = array[mid] < array[from];
        boolean toLessThanFrom = array[to] < array[from];
        if (toLessThanMid == midLessThanFrom) {
            swap(array, mid, from);
        } else if (toLessThanMid != toLessThanFrom) {
            swap(array, from, to);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void selectAllInPlace(int[] allRequired, int requiredFrom, int requiredTo, double[] array, int from, int to) {
        int requiredChosen = chooseNextSelection(allRequired, requiredFrom, requiredTo, from, to);
        int required = allRequired[requiredChosen];
        selectInPlace(required, array, from, to);
        int requiredBelow = requiredChosen - 1;
        while (requiredBelow >= requiredFrom && allRequired[requiredBelow] == required) {
            requiredBelow--;
        }
        if (requiredBelow >= requiredFrom) {
            selectAllInPlace(allRequired, requiredFrom, requiredBelow, array, from, required - 1);
        }
        int requiredAbove = requiredChosen + 1;
        while (requiredAbove <= requiredTo && allRequired[requiredAbove] == required) {
            requiredAbove++;
        }
        if (requiredAbove <= requiredTo) {
            selectAllInPlace(allRequired, requiredAbove, requiredTo, array, required + 1, to);
        }
    }

    private static int chooseNextSelection(int[] allRequired, int requiredFrom, int requiredTo, int from, int to) {
        if (requiredFrom == requiredTo) {
            return requiredFrom;
        }
        int centerFloor = (from + to) >>> 1;
        int low = requiredFrom;
        int high = requiredTo;
        while (high > low + 1) {
            int mid = (low + high) >>> 1;
            if (allRequired[mid] > centerFloor) {
                high = mid;
            } else if (allRequired[mid] < centerFloor) {
                low = mid;
            } else {
                return mid;
            }
        }
        if (((from + to) - allRequired[low]) - allRequired[high] > 0) {
            return high;
        }
        return low;
    }

    private static void swap(double[] array, int i, int j) {
        double temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
