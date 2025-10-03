package androidx.constraintlayout.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;

/* loaded from: classes.dex */
public class StateSet {
    private static final boolean DEBUG = false;
    public static final String TAG = "ConstraintLayoutStates";
    ConstraintSet mDefaultConstraintSet;
    int mDefaultState = -1;
    int mCurrentStateId = -1;
    int mCurrentConstraintNumber = -1;
    private SparseArray<State> mStateList = new SparseArray<>();
    private SparseArray<ConstraintSet> mConstraintSetMap = new SparseArray<>();
    private ConstraintsChangedListener mConstraintsChangedListener = null;

    public StateSet(Context context, XmlPullParser parser) {
        load(context, parser);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Failed to find switch 'out' block (already processed)
        	at jadx.core.dex.visitors.regions.RegionMaker.calcSwitchOut(RegionMaker.java:923)
        	at jadx.core.dex.visitors.regions.RegionMaker.processSwitch(RegionMaker.java:797)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:157)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:91)
        	at jadx.core.dex.visitors.regions.RegionMaker.processFallThroughCases(RegionMaker.java:841)
        	at jadx.core.dex.visitors.regions.RegionMaker.processSwitch(RegionMaker.java:800)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:157)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:91)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:735)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:152)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:91)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeEndlessLoop(RegionMaker.java:411)
        	at jadx.core.dex.visitors.regions.RegionMaker.processLoop(RegionMaker.java:201)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:135)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:91)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
        */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Failed to find 'out' block for switch in B:16:0x0033. Please report as an issue. */
    private void load(android.content.Context r12, org.xmlpull.v1.XmlPullParser r13) {
        /*
            r11 = this;
            android.util.AttributeSet r0 = android.util.Xml.asAttributeSet(r13)
            int[] r1 = androidx.constraintlayout.widget.C0208R.styleable.StateSet
            android.content.res.TypedArray r1 = r12.obtainStyledAttributes(r0, r1)
            int r2 = r1.getIndexCount()
            r3 = 0
        Lf:
            if (r3 >= r2) goto L24
            int r4 = r1.getIndex(r3)
            int r5 = androidx.constraintlayout.widget.C0208R.styleable.StateSet_defaultState
            if (r4 != r5) goto L21
            int r5 = r11.mDefaultState
            int r5 = r1.getResourceId(r4, r5)
            r11.mDefaultState = r5
        L21:
            int r3 = r3 + 1
            goto Lf
        L24:
            r1.recycle()
            r3 = 0
            r4 = 0
            r5 = 0
            int r6 = r13.getEventType()     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
        L2e:
            r7 = 1
            if (r6 == r7) goto La5
            java.lang.String r8 = "StateSet"
            switch(r6) {
                case 0: goto L99;
                case 1: goto L36;
                case 2: goto L45;
                case 3: goto L38;
                default: goto L36;
            }
        L36:
            goto L9f
        L38:
            java.lang.String r7 = r13.getName()     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            boolean r7 = r8.equals(r7)     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            if (r7 == 0) goto L43
            return
        L43:
            r3 = 0
            goto L9f
        L45:
            java.lang.String r9 = r13.getName()     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            r3 = r9
            r9 = -1
            int r10 = r3.hashCode()     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            switch(r10) {
                case 80204913: goto L6e;
                case 1301459538: goto L64;
                case 1382829617: goto L5d;
                case 1901439077: goto L53;
                default: goto L52;
            }     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
        L52:
            goto L78
        L53:
            java.lang.String r7 = "Variant"
            boolean r7 = r3.equals(r7)     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            if (r7 == 0) goto L52
            r7 = 3
            goto L79
        L5d:
            boolean r8 = r3.equals(r8)     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            if (r8 == 0) goto L52
            goto L79
        L64:
            java.lang.String r7 = "LayoutDescription"
            boolean r7 = r3.equals(r7)     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            if (r7 == 0) goto L52
            r7 = 0
            goto L79
        L6e:
            java.lang.String r7 = "State"
            boolean r7 = r3.equals(r7)     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            if (r7 == 0) goto L52
            r7 = 2
            goto L79
        L78:
            r7 = r9
        L79:
            switch(r7) {
                case 0: goto L97;
                case 1: goto L96;
                case 2: goto L88;
                case 3: goto L7d;
                default: goto L7c;
            }     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
        L7c:
            goto L98
        L7d:
            androidx.constraintlayout.widget.StateSet$Variant r7 = new androidx.constraintlayout.widget.StateSet$Variant     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            r7.<init>(r12, r13)     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            if (r5 == 0) goto L98
            r5.add(r7)     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            goto L98
        L88:
            androidx.constraintlayout.widget.StateSet$State r7 = new androidx.constraintlayout.widget.StateSet$State     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            r7.<init>(r12, r13)     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            r5 = r7
            android.util.SparseArray<androidx.constraintlayout.widget.StateSet$State> r7 = r11.mStateList     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            int r8 = r5.mId     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            r7.put(r8, r5)     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            goto L98
        L96:
            goto L98
        L97:
        L98:
            goto L9f
        L99:
            java.lang.String r7 = r13.getName()     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            r4 = r7
        L9f:
            int r7 = r13.next()     // Catch: java.io.IOException -> La6 org.xmlpull.v1.XmlPullParserException -> Lab
            r6 = r7
            goto L2e
        La5:
            goto Laf
        La6:
            r4 = move-exception
            r4.printStackTrace()
            goto Lb0
        Lab:
            r4 = move-exception
            r4.printStackTrace()
        Laf:
        Lb0:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.constraintlayout.widget.StateSet.load(android.content.Context, org.xmlpull.v1.XmlPullParser):void");
    }

    public boolean needsToChange(int id, float width, float height) {
        int i = this.mCurrentStateId;
        if (i != id) {
            return true;
        }
        State state = id == -1 ? this.mStateList.valueAt(0) : this.mStateList.get(i);
        return (this.mCurrentConstraintNumber == -1 || !state.mVariants.get(this.mCurrentConstraintNumber).match(width, height)) && this.mCurrentConstraintNumber != state.findMatch(width, height);
    }

    public void setOnConstraintsChanged(ConstraintsChangedListener constraintsChangedListener) {
        this.mConstraintsChangedListener = constraintsChangedListener;
    }

    public int stateGetConstraintID(int id, int width, int height) {
        return updateConstraints(-1, id, width, height);
    }

    public int convertToConstraintSet(int currentConstrainSettId, int stateId, float width, float height) {
        State state = this.mStateList.get(stateId);
        if (state == null) {
            return stateId;
        }
        if (width == -1.0f || height == -1.0f) {
            if (state.mConstraintID == currentConstrainSettId) {
                return currentConstrainSettId;
            }
            Iterator<Variant> it = state.mVariants.iterator();
            while (it.hasNext()) {
                if (currentConstrainSettId == it.next().mConstraintID) {
                    return currentConstrainSettId;
                }
            }
            return state.mConstraintID;
        }
        Variant match = null;
        Iterator<Variant> it2 = state.mVariants.iterator();
        while (it2.hasNext()) {
            Variant mVariant = it2.next();
            if (mVariant.match(width, height)) {
                if (currentConstrainSettId == mVariant.mConstraintID) {
                    return currentConstrainSettId;
                }
                match = mVariant;
            }
        }
        if (match != null) {
            return match.mConstraintID;
        }
        return state.mConstraintID;
    }

    public int updateConstraints(int currentId, int id, float width, float height) {
        State state;
        if (currentId == id) {
            if (id == -1) {
                state = this.mStateList.valueAt(0);
            } else {
                state = this.mStateList.get(this.mCurrentStateId);
            }
            if (state == null) {
                return -1;
            }
            if (this.mCurrentConstraintNumber != -1 && state.mVariants.get(currentId).match(width, height)) {
                return currentId;
            }
            int match = state.findMatch(width, height);
            if (currentId == match) {
                return currentId;
            }
            return match == -1 ? state.mConstraintID : state.mVariants.get(match).mConstraintID;
        }
        State state2 = this.mStateList.get(id);
        if (state2 == null) {
            return -1;
        }
        int match2 = state2.findMatch(width, height);
        return match2 == -1 ? state2.mConstraintID : state2.mVariants.get(match2).mConstraintID;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class State {
        int mConstraintID;
        int mId;
        boolean mIsLayout;
        ArrayList<Variant> mVariants = new ArrayList<>();

        public State(Context context, XmlPullParser parser) {
            this.mConstraintID = -1;
            this.mIsLayout = false;
            AttributeSet attrs = Xml.asAttributeSet(parser);
            TypedArray a = context.obtainStyledAttributes(attrs, C0208R.styleable.State);
            int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                if (attr == C0208R.styleable.State_android_id) {
                    this.mId = a.getResourceId(attr, this.mId);
                } else if (attr == C0208R.styleable.State_constraints) {
                    this.mConstraintID = a.getResourceId(attr, this.mConstraintID);
                    String type = context.getResources().getResourceTypeName(this.mConstraintID);
                    context.getResources().getResourceName(this.mConstraintID);
                    if ("layout".equals(type)) {
                        this.mIsLayout = true;
                    }
                }
            }
            a.recycle();
        }

        void add(Variant size) {
            this.mVariants.add(size);
        }

        public int findMatch(float width, float height) {
            for (int i = 0; i < this.mVariants.size(); i++) {
                if (this.mVariants.get(i).match(width, height)) {
                    return i;
                }
            }
            return -1;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class Variant {
        int mConstraintID;
        int mId;
        boolean mIsLayout;
        float mMaxHeight;
        float mMaxWidth;
        float mMinHeight;
        float mMinWidth;

        public Variant(Context context, XmlPullParser parser) {
            this.mMinWidth = Float.NaN;
            this.mMinHeight = Float.NaN;
            this.mMaxWidth = Float.NaN;
            this.mMaxHeight = Float.NaN;
            this.mConstraintID = -1;
            this.mIsLayout = false;
            AttributeSet attrs = Xml.asAttributeSet(parser);
            TypedArray a = context.obtainStyledAttributes(attrs, C0208R.styleable.Variant);
            int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                if (attr == C0208R.styleable.Variant_constraints) {
                    this.mConstraintID = a.getResourceId(attr, this.mConstraintID);
                    String type = context.getResources().getResourceTypeName(this.mConstraintID);
                    context.getResources().getResourceName(this.mConstraintID);
                    if ("layout".equals(type)) {
                        this.mIsLayout = true;
                    }
                } else if (attr == C0208R.styleable.Variant_region_heightLessThan) {
                    this.mMaxHeight = a.getDimension(attr, this.mMaxHeight);
                } else if (attr == C0208R.styleable.Variant_region_heightMoreThan) {
                    this.mMinHeight = a.getDimension(attr, this.mMinHeight);
                } else if (attr == C0208R.styleable.Variant_region_widthLessThan) {
                    this.mMaxWidth = a.getDimension(attr, this.mMaxWidth);
                } else if (attr == C0208R.styleable.Variant_region_widthMoreThan) {
                    this.mMinWidth = a.getDimension(attr, this.mMinWidth);
                } else {
                    Log.v("ConstraintLayoutStates", "Unknown tag");
                }
            }
            a.recycle();
        }

        boolean match(float widthDp, float heightDp) {
            if (!Float.isNaN(this.mMinWidth) && widthDp < this.mMinWidth) {
                return false;
            }
            if (!Float.isNaN(this.mMinHeight) && heightDp < this.mMinHeight) {
                return false;
            }
            if (Float.isNaN(this.mMaxWidth) || widthDp <= this.mMaxWidth) {
                return Float.isNaN(this.mMaxHeight) || heightDp <= this.mMaxHeight;
            }
            return false;
        }
    }
}
