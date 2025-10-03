package com.airbnb.lottie;

import android.util.Log;
import androidx.collection.ArraySet;
import androidx.core.util.Pair;
import com.airbnb.lottie.utils.MeanCalculator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public class PerformanceTracker {
    private boolean enabled = false;
    private final Set<FrameListener> frameListeners = new ArraySet();
    private final Map<String, MeanCalculator> layerRenderTimes = new HashMap();
    private final Comparator<Pair<String, Float>> floatComparator = new Comparator<Pair<String, Float>>() { // from class: com.airbnb.lottie.PerformanceTracker.1
        @Override // java.util.Comparator
        public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
            float r1 = o1.second.floatValue();
            float r2 = o2.second.floatValue();
            if (r2 > r1) {
                return 1;
            }
            if (r1 > r2) {
                return -1;
            }
            return 0;
        }
    };

    public interface FrameListener {
        void onFrameRendered(float f);
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void recordRenderTime(String layerName, float millis) {
        if (!this.enabled) {
            return;
        }
        MeanCalculator meanCalculator = this.layerRenderTimes.get(layerName);
        if (meanCalculator == null) {
            meanCalculator = new MeanCalculator();
            this.layerRenderTimes.put(layerName, meanCalculator);
        }
        meanCalculator.add(millis);
        if (layerName.equals("__container")) {
            for (FrameListener listener : this.frameListeners) {
                listener.onFrameRendered(millis);
            }
        }
    }

    public void addFrameListener(FrameListener frameListener) {
        this.frameListeners.add(frameListener);
    }

    public void removeFrameListener(FrameListener frameListener) {
        this.frameListeners.remove(frameListener);
    }

    public void clearRenderTimes() {
        this.layerRenderTimes.clear();
    }

    public void logRenderTimes() {
        if (!this.enabled) {
            return;
        }
        List<Pair<String, Float>> sortedRenderTimes = getSortedRenderTimes();
        Log.d(C0633L.TAG, "Render times:");
        for (int i = 0; i < sortedRenderTimes.size(); i++) {
            Pair<String, Float> layer = sortedRenderTimes.get(i);
            Log.d(C0633L.TAG, String.format("\t\t%30s:%.2f", layer.first, layer.second));
        }
    }

    public List<Pair<String, Float>> getSortedRenderTimes() {
        if (!this.enabled) {
            return Collections.emptyList();
        }
        List<Pair<String, Float>> sortedRenderTimes = new ArrayList<>(this.layerRenderTimes.size());
        for (Map.Entry<String, MeanCalculator> e : this.layerRenderTimes.entrySet()) {
            sortedRenderTimes.add(new Pair<>(e.getKey(), Float.valueOf(e.getValue().getMean())));
        }
        Collections.sort(sortedRenderTimes, this.floatComparator);
        return sortedRenderTimes;
    }
}
