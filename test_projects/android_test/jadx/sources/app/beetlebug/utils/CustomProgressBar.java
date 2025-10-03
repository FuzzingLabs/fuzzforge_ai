package app.beetlebug.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import app.beetlebug.C0572R;

/* loaded from: classes7.dex */
public class CustomProgressBar extends View {
    private int DEFAULT_ANIMATION_DURATION;
    private int[] backgroundBarGradientColors;
    private Paint backgroundCircle;
    private int backgroundProgressColor;
    private int backgroundProgressThickness;
    private int centerPoint;
    private int drawOuterRadius;
    private int drawRadius;
    private int[] foregroundBarGradientColors;
    private Paint foregroundCircle;
    private int foregroundProgressColor;
    private int foregroundProgressThickness;
    private int height;
    private final int mBarColorStandard;
    private int max;
    private int min;
    private float progress;
    private RectF rectF;
    private boolean roundedCorner;
    private int startAngle;
    private int subtractingValue;
    private int width;

    public CustomProgressBar(Context context) {
        super(context);
        this.foregroundProgressThickness = 10;
        this.backgroundProgressThickness = 5;
        this.foregroundCircle = new Paint();
        this.backgroundCircle = new Paint();
        this.progress = 0.0f;
        this.mBarColorStandard = -16738680;
        this.foregroundProgressColor = -16711936;
        this.backgroundProgressColor = -7829368;
        this.foregroundBarGradientColors = new int[]{-16738680};
        this.backgroundBarGradientColors = new int[]{-16738680};
        this.min = 0;
        this.max = 100;
        this.startAngle = -90;
        this.rectF = new RectF();
        this.DEFAULT_ANIMATION_DURATION = 2100;
        init();
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.foregroundProgressThickness = 10;
        this.backgroundProgressThickness = 5;
        this.foregroundCircle = new Paint();
        this.backgroundCircle = new Paint();
        this.progress = 0.0f;
        this.mBarColorStandard = -16738680;
        this.foregroundProgressColor = -16711936;
        this.backgroundProgressColor = -7829368;
        this.foregroundBarGradientColors = new int[]{-16738680};
        this.backgroundBarGradientColors = new int[]{-16738680};
        this.min = 0;
        this.max = 100;
        this.startAngle = -90;
        this.rectF = new RectF();
        this.DEFAULT_ANIMATION_DURATION = 2100;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, C0572R.styleable.CustomProgressBar, 0, 0);
        TypedArray colorsArray = context.getResources().obtainTypedArray(C0572R.array.gradient_colors);
        this.backgroundProgressThickness = typedArray.getInteger(1, this.backgroundProgressThickness);
        this.foregroundProgressThickness = typedArray.getInteger(4, this.foregroundProgressThickness);
        this.progress = typedArray.getFloat(7, this.progress);
        this.foregroundProgressColor = typedArray.getInt(2, this.foregroundProgressColor);
        this.backgroundProgressColor = typedArray.getColor(0, this.backgroundProgressColor);
        this.roundedCorner = typedArray.getBoolean(8, this.roundedCorner);
        this.min = typedArray.getInt(6, this.min);
        this.max = typedArray.getInt(5, this.max);
        this.foregroundBarGradientColors = new int[colorsArray.length()];
        for (int i = 0; i < colorsArray.length(); i++) {
            this.foregroundBarGradientColors[i] = colorsArray.getColor(i, 0);
        }
        typedArray.recycle();
        init();
    }

    private void init() {
        this.rectF = new RectF();
        this.foregroundCircle.setStrokeWidth(this.foregroundProgressThickness);
        this.foregroundCircle.setAntiAlias(true);
        this.foregroundCircle.setStyle(Paint.Style.STROKE);
        this.backgroundCircle.setAntiAlias(true);
        this.backgroundCircle.setStyle(Paint.Style.STROKE);
        this.backgroundCircle.setStrokeWidth(this.backgroundProgressThickness);
        setRoundedCorner(this.roundedCorner);
        setupBarPaint();
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int defaultSize = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        this.height = defaultSize;
        this.centerPoint = Math.min(this.width, defaultSize);
        int min = Math.min(this.width, this.height);
        this.min = min;
        setMeasuredDimension(min, min);
        setRadiusRect();
    }

    private void setRadiusRect() {
        this.centerPoint = Math.min(this.width, this.height) / 2;
        int i = this.backgroundProgressThickness;
        int i2 = this.foregroundProgressThickness;
        if (i <= i2) {
            i = i2;
        }
        this.subtractingValue = i;
        int newSeekWidth = i / 2;
        this.drawRadius = Math.min((this.width - i) / 2, (this.height - i) / 2);
        int min = Math.min(this.width - newSeekWidth, this.height - newSeekWidth);
        this.drawOuterRadius = min;
        RectF rectF = this.rectF;
        int i3 = this.subtractingValue;
        rectF.set(i3 / 2, i3 / 2, min, min);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = this.centerPoint;
        canvas.drawCircle(i, i, this.drawRadius, this.backgroundCircle);
        float sweepAngle = (this.progress * 360.0f) / this.max;
        canvas.drawArc(this.rectF, this.startAngle, sweepAngle, false, this.foregroundCircle);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void setForegroundBarGradientColors(int... foregroundBarGradientColors) {
        this.foregroundBarGradientColors = foregroundBarGradientColors;
        setupBarPaint();
    }

    public void setBackgroundBarGradientColors(int... backgroundBarGradientColors) {
        this.backgroundBarGradientColors = backgroundBarGradientColors;
        setupBarPaint();
    }

    public void setBackgroundProgressThickness(int thickness) {
        this.backgroundProgressThickness = thickness;
        this.backgroundCircle.setStrokeWidth(thickness);
        requestLayout();
        invalidate();
    }

    public int getBackgroundProgressThickness() {
        return this.backgroundProgressThickness;
    }

    public int getForegroundProgressThickness() {
        return this.foregroundProgressThickness;
    }

    public void setForegroundProgressThickness(int foregroundProgressThickness) {
        this.foregroundProgressThickness = foregroundProgressThickness;
        this.foregroundCircle.setStrokeWidth(foregroundProgressThickness);
        requestLayout();
        invalidate();
    }

    private void setupBarPaint() {
        int[] iArr = this.foregroundBarGradientColors;
        if (iArr.length > 1) {
            Matrix matrix = new Matrix();
            Paint paint = this.foregroundCircle;
            int i = this.centerPoint;
            paint.setShader(new SweepGradient(i, i, this.foregroundBarGradientColors, (float[]) null));
            this.foregroundCircle.getShader().getLocalMatrix(matrix);
            int i2 = this.centerPoint;
            matrix.postTranslate(-i2, -i2);
            matrix.postRotate(this.startAngle);
            int i3 = this.centerPoint;
            matrix.postTranslate(i3, i3);
            this.foregroundCircle.getShader().setLocalMatrix(matrix);
            this.foregroundCircle.setColor(this.foregroundBarGradientColors[0]);
        } else if (iArr.length == 1) {
            this.foregroundCircle.setColor(this.foregroundProgressColor);
            this.foregroundCircle.setShader(null);
        } else {
            this.foregroundCircle.setColor(this.foregroundProgressColor);
            this.foregroundCircle.setShader(null);
        }
        int[] iArr2 = this.backgroundBarGradientColors;
        if (iArr2.length <= 1) {
            if (iArr2.length == 1) {
                this.backgroundCircle.setColor(this.backgroundProgressColor);
                this.backgroundCircle.setShader(null);
                return;
            } else {
                this.backgroundCircle.setColor(this.backgroundProgressColor);
                this.backgroundCircle.setShader(null);
                return;
            }
        }
        Matrix matrix2 = new Matrix();
        Paint paint2 = this.backgroundCircle;
        int i4 = this.centerPoint;
        paint2.setShader(new SweepGradient(i4, i4, this.backgroundBarGradientColors, (float[]) null));
        this.backgroundCircle.getShader().getLocalMatrix(matrix2);
        int i5 = this.centerPoint;
        matrix2.postTranslate(-i5, -i5);
        matrix2.postRotate(this.startAngle);
        int i6 = this.centerPoint;
        matrix2.postTranslate(i6, i6);
        this.backgroundCircle.getShader().setLocalMatrix(matrix2);
        this.backgroundCircle.setColor(this.backgroundBarGradientColors[0]);
    }

    public void setRoundedCorner(boolean roundedCorner) {
        if (roundedCorner) {
            this.foregroundCircle.setStrokeCap(Paint.Cap.ROUND);
            this.backgroundCircle.setStrokeCap(Paint.Cap.ROUND);
        } else {
            this.foregroundCircle.setStrokeCap(Paint.Cap.SQUARE);
            this.backgroundCircle.setStrokeCap(Paint.Cap.SQUARE);
        }
    }

    public void setProgressWithAnimation(float progress) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
        objectAnimator.setDuration(this.DEFAULT_ANIMATION_DURATION);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
    }

    public void setProgressWithAnimation(float progress, int duration) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getForegroundProgressColor() {
        return this.foregroundProgressColor;
    }

    public void setForegroundProgressColor(int foregroundProgressColor) {
        this.foregroundProgressColor = foregroundProgressColor;
        this.foregroundCircle.setColor(foregroundProgressColor);
        invalidate();
    }

    public int getBackgroundProgressColor() {
        return this.backgroundProgressColor;
    }

    public void setBackgroundProgressColor(int backgroundProgressColor) {
        this.backgroundProgressColor = backgroundProgressColor;
        this.backgroundCircle.setColor(backgroundProgressColor);
        invalidate();
    }
}
