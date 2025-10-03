package androidx.viewpager.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

/* loaded from: classes.dex */
public class PagerTabStrip extends PagerTitleStrip {
    private static final int FULL_UNDERLINE_HEIGHT = 1;
    private static final int INDICATOR_HEIGHT = 3;
    private static final int MIN_PADDING_BOTTOM = 6;
    private static final int MIN_STRIP_HEIGHT = 32;
    private static final int MIN_TEXT_SPACING = 64;
    private static final int TAB_PADDING = 16;
    private static final int TAB_SPACING = 32;
    private static final String TAG = "PagerTabStrip";
    private boolean mDrawFullUnderline;
    private boolean mDrawFullUnderlineSet;
    private int mFullUnderlineHeight;
    private boolean mIgnoreTap;
    private int mIndicatorColor;
    private int mIndicatorHeight;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private int mMinPaddingBottom;
    private int mMinStripHeight;
    private int mMinTextSpacing;
    private int mTabAlpha;
    private int mTabPadding;
    private final Paint mTabPaint;
    private final Rect mTempRect;
    private int mTouchSlop;

    public PagerTabStrip(Context context) {
        this(context, null);
    }

    public PagerTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        Paint paint = new Paint();
        this.mTabPaint = paint;
        this.mTempRect = new Rect();
        this.mTabAlpha = 255;
        this.mDrawFullUnderline = false;
        this.mDrawFullUnderlineSet = false;
        int i = this.mTextColor;
        this.mIndicatorColor = i;
        paint.setColor(i);
        float density = context.getResources().getDisplayMetrics().density;
        this.mIndicatorHeight = (int) ((3.0f * density) + 0.5f);
        this.mMinPaddingBottom = (int) ((6.0f * density) + 0.5f);
        this.mMinTextSpacing = (int) (64.0f * density);
        this.mTabPadding = (int) ((16.0f * density) + 0.5f);
        this.mFullUnderlineHeight = (int) ((1.0f * density) + 0.5f);
        this.mMinStripHeight = (int) ((32.0f * density) + 0.5f);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        setTextSpacing(getTextSpacing());
        setWillNotDraw(false);
        this.mPrevText.setFocusable(true);
        this.mPrevText.setOnClickListener(new View.OnClickListener() { // from class: androidx.viewpager.widget.PagerTabStrip.1
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                PagerTabStrip.this.mPager.setCurrentItem(PagerTabStrip.this.mPager.getCurrentItem() - 1);
            }
        });
        this.mNextText.setFocusable(true);
        this.mNextText.setOnClickListener(new View.OnClickListener() { // from class: androidx.viewpager.widget.PagerTabStrip.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                PagerTabStrip.this.mPager.setCurrentItem(PagerTabStrip.this.mPager.getCurrentItem() + 1);
            }
        });
        if (getBackground() == null) {
            this.mDrawFullUnderline = true;
        }
    }

    public void setTabIndicatorColor(int color) {
        this.mIndicatorColor = color;
        this.mTabPaint.setColor(color);
        invalidate();
    }

    public void setTabIndicatorColorResource(int resId) {
        setTabIndicatorColor(ContextCompat.getColor(getContext(), resId));
    }

    public int getTabIndicatorColor() {
        return this.mIndicatorColor;
    }

    @Override // android.view.View
    public void setPadding(int left, int top, int right, int bottom) {
        if (bottom < this.mMinPaddingBottom) {
            bottom = this.mMinPaddingBottom;
        }
        super.setPadding(left, top, right, bottom);
    }

    @Override // androidx.viewpager.widget.PagerTitleStrip
    public void setTextSpacing(int textSpacing) {
        if (textSpacing < this.mMinTextSpacing) {
            textSpacing = this.mMinTextSpacing;
        }
        super.setTextSpacing(textSpacing);
    }

    @Override // android.view.View
    public void setBackgroundDrawable(Drawable d) {
        super.setBackgroundDrawable(d);
        if (!this.mDrawFullUnderlineSet) {
            this.mDrawFullUnderline = d == null;
        }
    }

    @Override // android.view.View
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        if (!this.mDrawFullUnderlineSet) {
            this.mDrawFullUnderline = ((-16777216) & color) == 0;
        }
    }

    @Override // android.view.View
    public void setBackgroundResource(int resId) {
        super.setBackgroundResource(resId);
        if (!this.mDrawFullUnderlineSet) {
            this.mDrawFullUnderline = resId == 0;
        }
    }

    public void setDrawFullUnderline(boolean drawFull) {
        this.mDrawFullUnderline = drawFull;
        this.mDrawFullUnderlineSet = true;
        invalidate();
    }

    public boolean getDrawFullUnderline() {
        return this.mDrawFullUnderline;
    }

    @Override // androidx.viewpager.widget.PagerTitleStrip
    int getMinHeight() {
        return Math.max(super.getMinHeight(), this.mMinStripHeight);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:21:0x0077, code lost:
    
        return true;
     */
    @Override // android.view.View
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean onTouchEvent(android.view.MotionEvent r7) {
        /*
            r6 = this;
            int r0 = r7.getAction()
            r1 = 0
            if (r0 == 0) goto Lc
            boolean r2 = r6.mIgnoreTap
            if (r2 == 0) goto Lc
            return r1
        Lc:
            float r2 = r7.getX()
            float r3 = r7.getY()
            r4 = 1
            switch(r0) {
                case 0: goto L70;
                case 1: goto L3a;
                case 2: goto L19;
                default: goto L18;
            }
        L18:
            goto L77
        L19:
            float r1 = r6.mInitialMotionX
            float r1 = r2 - r1
            float r1 = java.lang.Math.abs(r1)
            int r5 = r6.mTouchSlop
            float r5 = (float) r5
            int r1 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1))
            if (r1 > 0) goto L37
            float r1 = r6.mInitialMotionY
            float r1 = r3 - r1
            float r1 = java.lang.Math.abs(r1)
            int r5 = r6.mTouchSlop
            float r5 = (float) r5
            int r1 = (r1 > r5 ? 1 : (r1 == r5 ? 0 : -1))
            if (r1 <= 0) goto L77
        L37:
            r6.mIgnoreTap = r4
            goto L77
        L3a:
            android.widget.TextView r1 = r6.mCurrText
            int r1 = r1.getLeft()
            int r5 = r6.mTabPadding
            int r1 = r1 - r5
            float r1 = (float) r1
            int r1 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1))
            if (r1 >= 0) goto L55
            androidx.viewpager.widget.ViewPager r1 = r6.mPager
            androidx.viewpager.widget.ViewPager r5 = r6.mPager
            int r5 = r5.getCurrentItem()
            int r5 = r5 - r4
            r1.setCurrentItem(r5)
            goto L77
        L55:
            android.widget.TextView r1 = r6.mCurrText
            int r1 = r1.getRight()
            int r5 = r6.mTabPadding
            int r1 = r1 + r5
            float r1 = (float) r1
            int r1 = (r2 > r1 ? 1 : (r2 == r1 ? 0 : -1))
            if (r1 <= 0) goto L77
            androidx.viewpager.widget.ViewPager r1 = r6.mPager
            androidx.viewpager.widget.ViewPager r5 = r6.mPager
            int r5 = r5.getCurrentItem()
            int r5 = r5 + r4
            r1.setCurrentItem(r5)
            goto L77
        L70:
            r6.mInitialMotionX = r2
            r6.mInitialMotionY = r3
            r6.mIgnoreTap = r1
        L77:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.viewpager.widget.PagerTabStrip.onTouchEvent(android.view.MotionEvent):boolean");
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int left = this.mCurrText.getLeft() - this.mTabPadding;
        int right = this.mCurrText.getRight() + this.mTabPadding;
        int top = height - this.mIndicatorHeight;
        this.mTabPaint.setColor((this.mTabAlpha << 24) | (this.mIndicatorColor & ViewCompat.MEASURED_SIZE_MASK));
        canvas.drawRect(left, top, right, height, this.mTabPaint);
        if (this.mDrawFullUnderline) {
            this.mTabPaint.setColor((-16777216) | (this.mIndicatorColor & ViewCompat.MEASURED_SIZE_MASK));
            canvas.drawRect(getPaddingLeft(), height - this.mFullUnderlineHeight, getWidth() - getPaddingRight(), height, this.mTabPaint);
        }
    }

    @Override // androidx.viewpager.widget.PagerTitleStrip
    void updateTextPositions(int position, float positionOffset, boolean force) {
        Rect r = this.mTempRect;
        int bottom = getHeight();
        int left = this.mCurrText.getLeft() - this.mTabPadding;
        int right = this.mCurrText.getRight() + this.mTabPadding;
        int top = bottom - this.mIndicatorHeight;
        r.set(left, top, right, bottom);
        super.updateTextPositions(position, positionOffset, force);
        this.mTabAlpha = (int) (Math.abs(positionOffset - 0.5f) * 2.0f * 255.0f);
        int left2 = this.mCurrText.getLeft() - this.mTabPadding;
        int right2 = this.mCurrText.getRight() + this.mTabPadding;
        r.union(left2, top, right2, bottom);
        invalidate(r);
    }
}
