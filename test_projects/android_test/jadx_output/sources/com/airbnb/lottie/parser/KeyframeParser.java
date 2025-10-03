package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import androidx.collection.SparseArrayCompat;
import androidx.constraintlayout.core.motion.utils.TypedValues;
import androidx.core.view.animation.PathInterpolatorCompat;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.MiscUtils;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.Keyframe;
import java.io.IOException;
import java.lang.ref.WeakReference;

/* loaded from: classes.dex */
class KeyframeParser {
    private static final float MAX_CP_VALUE = 100.0f;
    private static SparseArrayCompat<WeakReference<Interpolator>> pathInterpolatorCache;
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    static JsonReader.Options NAMES = JsonReader.Options.m16of("t", "s", "e", "o", "i", "h", TypedValues.Transition.S_TO, "ti");

    KeyframeParser() {
    }

    private static SparseArrayCompat<WeakReference<Interpolator>> pathInterpolatorCache() {
        if (pathInterpolatorCache == null) {
            pathInterpolatorCache = new SparseArrayCompat<>();
        }
        return pathInterpolatorCache;
    }

    private static WeakReference<Interpolator> getInterpolator(int hash) {
        WeakReference<Interpolator> weakReference;
        synchronized (KeyframeParser.class) {
            weakReference = pathInterpolatorCache().get(hash);
        }
        return weakReference;
    }

    private static void putInterpolator(int hash, WeakReference<Interpolator> interpolator) {
        synchronized (KeyframeParser.class) {
            pathInterpolatorCache.put(hash, interpolator);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T> Keyframe<T> parse(JsonReader reader, LottieComposition composition, float scale, ValueParser<T> valueParser, boolean animated) throws IOException {
        if (animated) {
            return parseKeyframe(composition, reader, scale, valueParser);
        }
        return parseStaticValue(reader, scale, valueParser);
    }

    private static <T> Keyframe<T> parseKeyframe(LottieComposition composition, JsonReader reader, float scale, ValueParser<T> valueParser) throws IOException {
        Interpolator interpolator;
        Interpolator interpolator2;
        Interpolator interpolator3 = null;
        reader.beginObject();
        boolean hold = false;
        PointF pathCp1 = null;
        PointF pathCp2 = null;
        T endValue = null;
        T endValue2 = null;
        float startFrame = 0.0f;
        PointF cp2 = null;
        PointF cp22 = null;
        while (reader.hasNext()) {
            switch (reader.selectName(NAMES)) {
                case 0:
                    startFrame = (float) reader.nextDouble();
                    break;
                case 1:
                    endValue2 = valueParser.parse(reader, scale);
                    break;
                case 2:
                    endValue = valueParser.parse(reader, scale);
                    break;
                case 3:
                    cp22 = JsonUtils.jsonToPoint(reader, scale);
                    break;
                case 4:
                    cp2 = JsonUtils.jsonToPoint(reader, scale);
                    break;
                case 5:
                    hold = reader.nextInt() == 1;
                    break;
                case 6:
                    pathCp1 = JsonUtils.jsonToPoint(reader, scale);
                    break;
                case 7:
                    pathCp2 = JsonUtils.jsonToPoint(reader, scale);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        if (hold) {
            endValue = endValue2;
            interpolator = LINEAR_INTERPOLATOR;
        } else if (cp22 != null && cp2 != null) {
            cp22.x = MiscUtils.clamp(cp22.x, -scale, scale);
            cp22.y = MiscUtils.clamp(cp22.y, -100.0f, MAX_CP_VALUE);
            cp2.x = MiscUtils.clamp(cp2.x, -scale, scale);
            cp2.y = MiscUtils.clamp(cp2.y, -100.0f, MAX_CP_VALUE);
            int hash = Utils.hashFor(cp22.x, cp22.y, cp2.x, cp2.y);
            WeakReference<Interpolator> interpolatorRef = getInterpolator(hash);
            if (interpolatorRef != null) {
                Interpolator interpolator4 = interpolatorRef.get();
                interpolator3 = interpolator4;
            }
            if (interpolatorRef == null || interpolator3 == null) {
                cp22.x /= scale;
                cp22.y /= scale;
                cp2.x /= scale;
                cp2.y /= scale;
                try {
                    Interpolator interpolator5 = PathInterpolatorCompat.create(cp22.x, cp22.y, cp2.x, cp2.y);
                    interpolator2 = interpolator5;
                } catch (IllegalArgumentException e) {
                    interpolator2 = e.getMessage().equals("The Path cannot loop back on itself.") ? PathInterpolatorCompat.create(Math.min(cp22.x, 1.0f), cp22.y, Math.max(cp2.x, 0.0f), cp2.y) : new LinearInterpolator();
                }
                try {
                    putInterpolator(hash, new WeakReference(interpolator2));
                } catch (ArrayIndexOutOfBoundsException e2) {
                }
                interpolator = interpolator2;
            } else {
                interpolator = interpolator3;
            }
        } else {
            interpolator = LINEAR_INTERPOLATOR;
        }
        Keyframe<T> keyframe = new Keyframe<>(composition, endValue2, endValue, interpolator, startFrame, null);
        keyframe.pathCp1 = pathCp1;
        keyframe.pathCp2 = pathCp2;
        return keyframe;
    }

    private static <T> Keyframe<T> parseStaticValue(JsonReader reader, float scale, ValueParser<T> valueParser) throws IOException {
        T value = valueParser.parse(reader, scale);
        return new Keyframe<>(value);
    }
}
