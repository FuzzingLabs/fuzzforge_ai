package androidx.constraintlayout.core.motion;

import androidx.constraintlayout.core.motion.utils.Utils;
import androidx.core.view.ViewCompat;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/* loaded from: classes.dex */
public class CustomAttribute {
    private static final String TAG = "TransitionLayout";
    boolean mBooleanValue;
    private int mColorValue;
    private float mFloatValue;
    private int mIntegerValue;
    private boolean mMethod;
    String mName;
    private String mStringValue;
    private AttributeType mType;

    /* loaded from: classes.dex */
    public enum AttributeType {
        INT_TYPE,
        FLOAT_TYPE,
        COLOR_TYPE,
        COLOR_DRAWABLE_TYPE,
        STRING_TYPE,
        BOOLEAN_TYPE,
        DIMENSION_TYPE,
        REFERENCE_TYPE
    }

    public AttributeType getType() {
        return this.mType;
    }

    public boolean isContinuous() {
        switch (C01701.f16xbc2a0812[this.mType.ordinal()]) {
            case 1:
            case 2:
            case 3:
                return false;
            default:
                return true;
        }
    }

    public void setFloatValue(float value) {
        this.mFloatValue = value;
    }

    public void setColorValue(int value) {
        this.mColorValue = value;
    }

    public void setIntValue(int value) {
        this.mIntegerValue = value;
    }

    public void setStringValue(String value) {
        this.mStringValue = value;
    }

    public int numberOfInterpolatedValues() {
        switch (this.mType) {
            case COLOR_TYPE:
            case COLOR_DRAWABLE_TYPE:
                return 4;
            default:
                return 1;
        }
    }

    public float getValueToInterpolate() {
        switch (this.mType) {
            case BOOLEAN_TYPE:
                return this.mBooleanValue ? 1.0f : 0.0f;
            case STRING_TYPE:
                throw new RuntimeException("Cannot interpolate String");
            case COLOR_TYPE:
            case COLOR_DRAWABLE_TYPE:
                throw new RuntimeException("Color does not have a single color to interpolate");
            case INT_TYPE:
                return this.mIntegerValue;
            case FLOAT_TYPE:
                return this.mFloatValue;
            case DIMENSION_TYPE:
                return this.mFloatValue;
            default:
                return Float.NaN;
        }
    }

    public void getValuesToInterpolate(float[] ret) {
        switch (this.mType) {
            case BOOLEAN_TYPE:
                ret[0] = this.mBooleanValue ? 1.0f : 0.0f;
                return;
            case STRING_TYPE:
                throw new RuntimeException("Color does not have a single color to interpolate");
            case COLOR_TYPE:
            case COLOR_DRAWABLE_TYPE:
                int i = this.mColorValue;
                int a = (i >> 24) & 255;
                int r = (i >> 16) & 255;
                int g = (i >> 8) & 255;
                int b = i & 255;
                float f_r = (float) Math.pow(r / 255.0f, 2.2d);
                float f_g = (float) Math.pow(g / 255.0f, 2.2d);
                float f_b = (float) Math.pow(b / 255.0f, 2.2d);
                ret[0] = f_r;
                ret[1] = f_g;
                ret[2] = f_b;
                ret[3] = a / 255.0f;
                return;
            case INT_TYPE:
                ret[0] = this.mIntegerValue;
                return;
            case FLOAT_TYPE:
                ret[0] = this.mFloatValue;
                return;
            case DIMENSION_TYPE:
                ret[0] = this.mFloatValue;
                return;
            default:
                return;
        }
    }

    public void setValue(float[] value) {
        switch (C01701.f16xbc2a0812[this.mType.ordinal()]) {
            case 1:
            case 6:
                this.mIntegerValue = (int) value[0];
                return;
            case 2:
                this.mBooleanValue = ((double) value[0]) > 0.5d;
                return;
            case 3:
                throw new RuntimeException("Color does not have a single color to interpolate");
            case 4:
            case 5:
                int hsvToRgb = hsvToRgb(value[0], value[1], value[2]);
                this.mColorValue = hsvToRgb;
                this.mColorValue = (hsvToRgb & ViewCompat.MEASURED_SIZE_MASK) | (clamp((int) (value[3] * 255.0f)) << 24);
                return;
            case 7:
                this.mFloatValue = value[0];
                return;
            case 8:
                this.mFloatValue = value[0];
                return;
            default:
                return;
        }
    }

    public static int hsvToRgb(float hue, float saturation, float value) {
        int h = (int) (hue * 6.0f);
        float f = (6.0f * hue) - h;
        int p = (int) ((value * 255.0f * (1.0f - saturation)) + 0.5f);
        int q = (int) ((value * 255.0f * (1.0f - (f * saturation))) + 0.5f);
        int t = (int) ((value * 255.0f * (1.0f - ((1.0f - f) * saturation))) + 0.5f);
        int v = (int) ((255.0f * value) + 0.5f);
        switch (h) {
            case 0:
                return (-16777216) | ((v << 16) + (t << 8) + p);
            case 1:
                return (-16777216) | ((q << 16) + (v << 8) + p);
            case 2:
                return (-16777216) | ((p << 16) + (v << 8) + t);
            case 3:
                return (-16777216) | ((p << 16) + (q << 8) + v);
            case 4:
                return (-16777216) | ((t << 16) + (p << 8) + v);
            case 5:
                return (-16777216) | ((v << 16) + (p << 8) + q);
            default:
                return 0;
        }
    }

    public boolean diff(CustomAttribute CustomAttribute) {
        if (CustomAttribute == null || this.mType != CustomAttribute.mType) {
            return false;
        }
        switch (C01701.f16xbc2a0812[this.mType.ordinal()]) {
            case 1:
            case 6:
                return this.mIntegerValue == CustomAttribute.mIntegerValue;
            case 2:
                return this.mBooleanValue == CustomAttribute.mBooleanValue;
            case 3:
                return this.mIntegerValue == CustomAttribute.mIntegerValue;
            case 4:
            case 5:
                return this.mColorValue == CustomAttribute.mColorValue;
            case 7:
                return this.mFloatValue == CustomAttribute.mFloatValue;
            case 8:
                return this.mFloatValue == CustomAttribute.mFloatValue;
            default:
                return false;
        }
    }

    public CustomAttribute(String name, AttributeType attributeType) {
        this.mMethod = false;
        this.mName = name;
        this.mType = attributeType;
    }

    public CustomAttribute(String name, AttributeType attributeType, Object value, boolean method) {
        this.mMethod = false;
        this.mName = name;
        this.mType = attributeType;
        this.mMethod = method;
        setValue(value);
    }

    public CustomAttribute(CustomAttribute source, Object value) {
        this.mMethod = false;
        this.mName = source.mName;
        this.mType = source.mType;
        setValue(value);
    }

    public void setValue(Object value) {
        switch (C01701.f16xbc2a0812[this.mType.ordinal()]) {
            case 1:
            case 6:
                this.mIntegerValue = ((Integer) value).intValue();
                return;
            case 2:
                this.mBooleanValue = ((Boolean) value).booleanValue();
                return;
            case 3:
                this.mStringValue = (String) value;
                return;
            case 4:
            case 5:
                this.mColorValue = ((Integer) value).intValue();
                return;
            case 7:
                this.mFloatValue = ((Float) value).floatValue();
                return;
            case 8:
                this.mFloatValue = ((Float) value).floatValue();
                return;
            default:
                return;
        }
    }

    public static HashMap<String, CustomAttribute> extractAttributes(HashMap<String, CustomAttribute> base, Object view) {
        HashMap<String, CustomAttribute> ret = new HashMap<>();
        Class<?> cls = view.getClass();
        for (String name : base.keySet()) {
            CustomAttribute CustomAttribute = base.get(name);
            try {
                Method method = cls.getMethod("getMap" + name, new Class[0]);
                Object val = method.invoke(view, new Object[0]);
                ret.put(name, new CustomAttribute(CustomAttribute, val));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            }
        }
        return ret;
    }

    /* JADX WARN: Failed to find 'out' block for switch in B:9:0x0046. Please report as an issue. */
    public static void setAttributes(Object view, HashMap<String, CustomAttribute> map) {
        Class<?> cls = view.getClass();
        for (String name : map.keySet()) {
            CustomAttribute CustomAttribute = map.get(name);
            String methodName = name;
            if (!CustomAttribute.mMethod) {
                methodName = "set" + methodName;
            }
            try {
                switch (C01701.f16xbc2a0812[CustomAttribute.mType.ordinal()]) {
                    case 1:
                        Method method = cls.getMethod(methodName, Integer.TYPE);
                        method.invoke(view, Integer.valueOf(CustomAttribute.mIntegerValue));
                        break;
                    case 2:
                        Method method2 = cls.getMethod(methodName, Boolean.TYPE);
                        method2.invoke(view, Boolean.valueOf(CustomAttribute.mBooleanValue));
                        break;
                    case 3:
                        Method method3 = cls.getMethod(methodName, CharSequence.class);
                        method3.invoke(view, CustomAttribute.mStringValue);
                        break;
                    case 4:
                        Method method4 = cls.getMethod(methodName, Integer.TYPE);
                        method4.invoke(view, Integer.valueOf(CustomAttribute.mColorValue));
                        break;
                    case 6:
                        Method method5 = cls.getMethod(methodName, Integer.TYPE);
                        method5.invoke(view, Integer.valueOf(CustomAttribute.mIntegerValue));
                        break;
                    case 7:
                        Method method6 = cls.getMethod(methodName, Float.TYPE);
                        method6.invoke(view, Float.valueOf(CustomAttribute.mFloatValue));
                        break;
                    case 8:
                        Method method7 = cls.getMethod(methodName, Float.TYPE);
                        method7.invoke(view, Float.valueOf(CustomAttribute.mFloatValue));
                        break;
                }
            } catch (IllegalAccessException e) {
                Utils.loge(TAG, " Custom Attribute \"" + name + "\" not found on " + cls.getName());
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                Utils.loge(TAG, e2.getMessage());
                Utils.loge(TAG, " Custom Attribute \"" + name + "\" not found on " + cls.getName());
                StringBuilder sb = new StringBuilder();
                sb.append(cls.getName());
                sb.append(" must have a method ");
                sb.append(methodName);
                Utils.loge(TAG, sb.toString());
            } catch (InvocationTargetException e3) {
                Utils.loge(TAG, " Custom Attribute \"" + name + "\" not found on " + cls.getName());
                e3.printStackTrace();
            }
        }
    }

    public void applyCustom(Object view) {
        Class<?> cls = view.getClass();
        String name = this.mName;
        String methodName = name;
        if (!this.mMethod) {
            methodName = "set" + methodName;
        }
        try {
            switch (C01701.f16xbc2a0812[this.mType.ordinal()]) {
                case 1:
                case 6:
                    Method method = cls.getMethod(methodName, Integer.TYPE);
                    method.invoke(view, Integer.valueOf(this.mIntegerValue));
                    return;
                case 2:
                    Method method2 = cls.getMethod(methodName, Boolean.TYPE);
                    method2.invoke(view, Boolean.valueOf(this.mBooleanValue));
                    return;
                case 3:
                    Method method3 = cls.getMethod(methodName, CharSequence.class);
                    method3.invoke(view, this.mStringValue);
                    return;
                case 4:
                    Method method4 = cls.getMethod(methodName, Integer.TYPE);
                    method4.invoke(view, Integer.valueOf(this.mColorValue));
                    return;
                case 5:
                default:
                    return;
                case 7:
                    Method method5 = cls.getMethod(methodName, Float.TYPE);
                    method5.invoke(view, Float.valueOf(this.mFloatValue));
                    return;
                case 8:
                    Method method6 = cls.getMethod(methodName, Float.TYPE);
                    method6.invoke(view, Float.valueOf(this.mFloatValue));
                    return;
            }
        } catch (IllegalAccessException e) {
            Utils.loge(TAG, " Custom Attribute \"" + name + "\" not found on " + cls.getName());
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            Utils.loge(TAG, e2.getMessage());
            Utils.loge(TAG, " Custom Attribute \"" + name + "\" not found on " + cls.getName());
            StringBuilder sb = new StringBuilder();
            sb.append(cls.getName());
            sb.append(" must have a method ");
            sb.append(methodName);
            Utils.loge(TAG, sb.toString());
        } catch (InvocationTargetException e3) {
            Utils.loge(TAG, " Custom Attribute \"" + name + "\" not found on " + cls.getName());
            e3.printStackTrace();
        }
    }

    private static int clamp(int c) {
        int c2 = (c & (~(c >> 31))) - 255;
        return (c2 & (c2 >> 31)) + 255;
    }

    public void setInterpolatedValue(Object view, float[] value) {
        Class<?> cls = view.getClass();
        String methodName = "set" + this.mName;
        try {
            switch (this.mType) {
                case BOOLEAN_TYPE:
                    Method method = cls.getMethod(methodName, Boolean.TYPE);
                    boolean z = true;
                    Object[] objArr = new Object[1];
                    if (value[0] <= 0.5f) {
                        z = false;
                    }
                    objArr[0] = Boolean.valueOf(z);
                    method.invoke(view, objArr);
                    return;
                case STRING_TYPE:
                    throw new RuntimeException("unable to interpolate strings " + this.mName);
                case COLOR_TYPE:
                    Method method2 = cls.getMethod(methodName, Integer.TYPE);
                    int r = clamp((int) (((float) Math.pow(value[0], 0.45454545454545453d)) * 255.0f));
                    int g = clamp((int) (((float) Math.pow(value[1], 0.45454545454545453d)) * 255.0f));
                    int b = clamp((int) (((float) Math.pow(value[2], 0.45454545454545453d)) * 255.0f));
                    int a = clamp((int) (value[3] * 255.0f));
                    int color = (a << 24) | (r << 16) | (g << 8) | b;
                    method2.invoke(view, Integer.valueOf(color));
                    return;
                case COLOR_DRAWABLE_TYPE:
                default:
                    return;
                case INT_TYPE:
                    Method method3 = cls.getMethod(methodName, Integer.TYPE);
                    method3.invoke(view, Integer.valueOf((int) value[0]));
                    return;
                case FLOAT_TYPE:
                    Method method4 = cls.getMethod(methodName, Float.TYPE);
                    method4.invoke(view, Float.valueOf(value[0]));
                    return;
                case DIMENSION_TYPE:
                    Method method5 = cls.getMethod(methodName, Float.TYPE);
                    method5.invoke(view, Float.valueOf(value[0]));
                    return;
            }
        } catch (IllegalAccessException e) {
            Utils.loge(TAG, "cannot access method " + methodName + " on View \"" + view.getClass().getName() + "\"");
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            Utils.loge(TAG, "no method " + methodName + " on View \"" + view.getClass().getName() + "\"");
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
    }
}
