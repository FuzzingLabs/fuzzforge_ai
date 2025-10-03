package androidx.constraintlayout.core.widgets;

/* loaded from: classes.dex */
public class Rectangle {
    public int height;
    public int width;

    /* renamed from: x */
    public int f45x;

    /* renamed from: y */
    public int f46y;

    public void setBounds(int x, int y, int width, int height) {
        this.f45x = x;
        this.f46y = y;
        this.width = width;
        this.height = height;
    }

    void grow(int w, int h) {
        this.f45x -= w;
        this.f46y -= h;
        this.width += w * 2;
        this.height += h * 2;
    }

    boolean intersects(Rectangle bounds) {
        int i;
        int i2;
        int i3 = this.f45x;
        int i4 = bounds.f45x;
        return i3 >= i4 && i3 < i4 + bounds.width && (i = this.f46y) >= (i2 = bounds.f46y) && i < i2 + bounds.height;
    }

    public boolean contains(int x, int y) {
        int i;
        int i2 = this.f45x;
        return x >= i2 && x < i2 + this.width && y >= (i = this.f46y) && y < i + this.height;
    }

    public int getCenterX() {
        return (this.f45x + this.width) / 2;
    }

    public int getCenterY() {
        return (this.f46y + this.height) / 2;
    }
}
