package com.airbnb.lottie.model.content;

import android.graphics.PointF;
import com.airbnb.lottie.model.CubicCurveData;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.MiscUtils;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class ShapeData {
    private boolean closed;
    private final List<CubicCurveData> curves;
    private PointF initialPoint;

    public ShapeData(PointF initialPoint, boolean closed, List<CubicCurveData> curves) {
        this.initialPoint = initialPoint;
        this.closed = closed;
        this.curves = new ArrayList(curves);
    }

    public ShapeData() {
        this.curves = new ArrayList();
    }

    private void setInitialPoint(float x, float y) {
        if (this.initialPoint == null) {
            this.initialPoint = new PointF();
        }
        this.initialPoint.set(x, y);
    }

    public PointF getInitialPoint() {
        return this.initialPoint;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public List<CubicCurveData> getCurves() {
        return this.curves;
    }

    public void interpolateBetween(ShapeData shapeData1, ShapeData shapeData2, float percentage) {
        if (this.initialPoint == null) {
            this.initialPoint = new PointF();
        }
        this.closed = shapeData1.isClosed() || shapeData2.isClosed();
        if (shapeData1.getCurves().size() != shapeData2.getCurves().size()) {
            Logger.warning("Curves must have the same number of control points. Shape 1: " + shapeData1.getCurves().size() + "\tShape 2: " + shapeData2.getCurves().size());
        }
        int points = Math.min(shapeData1.getCurves().size(), shapeData2.getCurves().size());
        if (this.curves.size() < points) {
            for (int i = this.curves.size(); i < points; i++) {
                this.curves.add(new CubicCurveData());
            }
        } else if (this.curves.size() > points) {
            for (int i2 = this.curves.size() - 1; i2 >= points; i2--) {
                List<CubicCurveData> list = this.curves;
                list.remove(list.size() - 1);
            }
        }
        PointF initialPoint1 = shapeData1.getInitialPoint();
        PointF initialPoint2 = shapeData2.getInitialPoint();
        setInitialPoint(MiscUtils.lerp(initialPoint1.x, initialPoint2.x, percentage), MiscUtils.lerp(initialPoint1.y, initialPoint2.y, percentage));
        int i3 = this.curves.size() - 1;
        while (i3 >= 0) {
            CubicCurveData curve1 = shapeData1.getCurves().get(i3);
            CubicCurveData curve2 = shapeData2.getCurves().get(i3);
            PointF cp11 = curve1.getControlPoint1();
            PointF cp21 = curve1.getControlPoint2();
            PointF vertex1 = curve1.getVertex();
            PointF cp12 = curve2.getControlPoint1();
            PointF cp22 = curve2.getControlPoint2();
            PointF vertex2 = curve2.getVertex();
            this.curves.get(i3).setControlPoint1(MiscUtils.lerp(cp11.x, cp12.x, percentage), MiscUtils.lerp(cp11.y, cp12.y, percentage));
            this.curves.get(i3).setControlPoint2(MiscUtils.lerp(cp21.x, cp22.x, percentage), MiscUtils.lerp(cp21.y, cp22.y, percentage));
            this.curves.get(i3).setVertex(MiscUtils.lerp(vertex1.x, vertex2.x, percentage), MiscUtils.lerp(vertex1.y, vertex2.y, percentage));
            i3--;
            points = points;
        }
    }

    public String toString() {
        return "ShapeData{numCurves=" + this.curves.size() + "closed=" + this.closed + '}';
    }
}
