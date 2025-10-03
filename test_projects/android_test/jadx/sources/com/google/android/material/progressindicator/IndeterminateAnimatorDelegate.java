package com.google.android.material.progressindicator;

import android.animation.Animator;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

/* loaded from: classes.dex */
abstract class IndeterminateAnimatorDelegate<T extends Animator> {
    protected IndeterminateDrawable drawable;
    protected final int[] segmentColors;
    protected final float[] segmentPositions;

    abstract void cancelAnimatorImmediately();

    public abstract void invalidateSpecValues();

    public abstract void registerAnimatorsCompleteCallback(Animatable2Compat.AnimationCallback animationCallback);

    abstract void requestCancelAnimatorAfterCurrentCycle();

    abstract void startAnimator();

    public abstract void unregisterAnimatorsCompleteCallback();

    protected IndeterminateAnimatorDelegate(int segmentCount) {
        this.segmentPositions = new float[segmentCount * 2];
        this.segmentColors = new int[segmentCount];
    }

    protected void registerDrawable(IndeterminateDrawable drawable) {
        this.drawable = drawable;
    }

    protected float getFractionInRange(int playtime, int start, int duration) {
        return (playtime - start) / duration;
    }
}
