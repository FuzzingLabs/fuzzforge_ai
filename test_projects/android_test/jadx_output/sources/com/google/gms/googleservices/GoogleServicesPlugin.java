package com.google.gms.googleservices;

import com.google.android.gms.dependencies.DependencyAnalyzer;
import com.google.android.gms.dependencies.DependencyInspector;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.Reference;
import groovy.transform.Generated;
import groovy.transform.Internal;
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Iterator;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.GStringImpl;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.CallSiteArray;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.ShortTypeHandling;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/* compiled from: GoogleServicesPlugin.groovy */
/* loaded from: classes.dex */
public class GoogleServicesPlugin implements Plugin<Project>, GroovyObject {
    private static /* synthetic */ SoftReference $callSiteArray = null;
    private static /* synthetic */ ClassInfo $staticClassInfo = null;
    private static /* synthetic */ ClassInfo $staticClassInfo$ = null;
    public static final String MINIMUM_VERSION = "9.0.0";
    public static final String MODULE_CORE = "firebase-core";
    public static final String MODULE_GROUP = "com.google.android.gms";
    public static final String MODULE_GROUP_FIREBASE = "com.google.firebase";
    public static final String MODULE_VERSION = "11.4.2";
    public static transient /* synthetic */ boolean __$stMC;
    private transient /* synthetic */ MetaClass metaClass;

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public static class GoogleServicesPluginConfig implements GroovyObject {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        private static /* synthetic */ ClassInfo $staticClassInfo$;
        public static transient /* synthetic */ boolean __$stMC;
        private boolean disableVersionCheck;
        private transient /* synthetic */ MetaClass metaClass;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            return new CallSiteArray(GoogleServicesPluginConfig.class, new String[0]);
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /*  JADX ERROR: IndexOutOfBoundsException in pass: MarkMethodsForInline
            java.lang.IndexOutOfBoundsException: Index: 0
            	at java.base/java.util.Collections$EmptyList.get(Collections.java:4808)
            	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:103)
            	at jadx.core.dex.visitors.MarkMethodsForInline.isSyntheticAccessPattern(MarkMethodsForInline.java:117)
            	at jadx.core.dex.visitors.MarkMethodsForInline.inlineMth(MarkMethodsForInline.java:86)
            	at jadx.core.dex.visitors.MarkMethodsForInline.process(MarkMethodsForInline.java:53)
            	at jadx.core.dex.visitors.MarkMethodsForInline.visit(MarkMethodsForInline.java:37)
            */
        public static /* synthetic */ java.lang.Object $static_methodMissing(java.lang.String r7, java.lang.Object r8) {
            /*
                $getCallSiteArray()
                java.lang.Class<com.google.gms.googleservices.GoogleServicesPlugin$GoogleServicesPluginConfig> r0 = com.google.gms.googleservices.GoogleServicesPlugin.GoogleServicesPluginConfig.class
                java.lang.Class<com.google.gms.googleservices.GoogleServicesPlugin> r1 = com.google.gms.googleservices.GoogleServicesPlugin.class
                org.codehaus.groovy.runtime.GStringImpl r2 = new org.codehaus.groovy.runtime.GStringImpl
                r3 = 1
                java.lang.Object[] r4 = new java.lang.Object[r3]
                r5 = 0
                r4[r5] = r7
                java.lang.String r6 = ""
                java.lang.String[] r6 = new java.lang.String[]{r6, r6}
                r2.<init>(r4, r6)
                java.lang.String r2 = org.codehaus.groovy.runtime.typehandling.ShortTypeHandling.castToString(r2)
                r4 = r2
                java.lang.String r4 = (java.lang.String) r4
                java.lang.Object[] r4 = new java.lang.Object[r5]
                java.lang.Object[] r6 = new java.lang.Object[r3]
                r6[r5] = r8
                int[] r3 = new int[r3]
                r3[r5] = r5
                java.lang.Object[] r3 = org.codehaus.groovy.runtime.ScriptBytecodeAdapter.despreadList(r4, r6, r3)
                java.lang.Object r0 = org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodN(r0, r1, r2, r3)
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.gms.googleservices.GoogleServicesPlugin.GoogleServicesPluginConfig.$static_methodMissing(java.lang.String, java.lang.Object):java.lang.Object");
        }

        /*  JADX ERROR: IndexOutOfBoundsException in pass: MarkMethodsForInline
            java.lang.IndexOutOfBoundsException: Index: 0
            	at java.base/java.util.Collections$EmptyList.get(Collections.java:4808)
            	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:103)
            	at jadx.core.dex.visitors.MarkMethodsForInline.isSyntheticAccessPattern(MarkMethodsForInline.java:117)
            	at jadx.core.dex.visitors.MarkMethodsForInline.inlineMth(MarkMethodsForInline.java:86)
            	at jadx.core.dex.visitors.MarkMethodsForInline.process(MarkMethodsForInline.java:53)
            	at jadx.core.dex.visitors.MarkMethodsForInline.visit(MarkMethodsForInline.java:37)
            */
        public static /* synthetic */ java.lang.Object $static_propertyMissing(java.lang.String r5) {
            /*
                $getCallSiteArray()
                java.lang.Class<com.google.gms.googleservices.GoogleServicesPlugin$GoogleServicesPluginConfig> r0 = com.google.gms.googleservices.GoogleServicesPlugin.GoogleServicesPluginConfig.class
                java.lang.Class<com.google.gms.googleservices.GoogleServicesPlugin> r1 = com.google.gms.googleservices.GoogleServicesPlugin.class
                org.codehaus.groovy.runtime.GStringImpl r2 = new org.codehaus.groovy.runtime.GStringImpl
                r3 = 1
                java.lang.Object[] r3 = new java.lang.Object[r3]
                r4 = 0
                r3[r4] = r5
                java.lang.String r4 = ""
                java.lang.String[] r4 = new java.lang.String[]{r4, r4}
                r2.<init>(r3, r4)
                java.lang.String r2 = org.codehaus.groovy.runtime.typehandling.ShortTypeHandling.castToString(r2)
                r3 = r2
                java.lang.String r3 = (java.lang.String) r3
                java.lang.Object r0 = org.codehaus.groovy.runtime.ScriptBytecodeAdapter.getProperty(r0, r1, r2)
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.gms.googleservices.GoogleServicesPlugin.GoogleServicesPluginConfig.$static_propertyMissing(java.lang.String):java.lang.Object");
        }

        public static /* synthetic */ void $static_propertyMissing(String str, Object obj) {
            $getCallSiteArray();
            ScriptBytecodeAdapter.setProperty(obj, (Class) null, GoogleServicesPlugin.class, ShortTypeHandling.castToString(new GStringImpl(new Object[]{str}, new String[]{"", ""})));
        }

        @Generated
        public GoogleServicesPluginConfig() {
            $getCallSiteArray();
            this.disableVersionCheck = false;
            this.metaClass = $getStaticMetaClass();
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != GoogleServicesPluginConfig.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public boolean getDisableVersionCheck() {
            return this.disableVersionCheck;
        }

        @Generated
        @Internal
        public /* synthetic */ MetaClass getMetaClass() {
            MetaClass metaClass = this.metaClass;
            if (metaClass != null) {
                return metaClass;
            }
            MetaClass $getStaticMetaClass = $getStaticMetaClass();
            this.metaClass = $getStaticMetaClass;
            return $getStaticMetaClass;
        }

        @Generated
        @Internal
        public /* synthetic */ Object getProperty(String str) {
            return getMetaClass().getProperty(this, str);
        }

        @Generated
        @Internal
        public /* synthetic */ Object invokeMethod(String str, Object obj) {
            return getMetaClass().invokeMethod(this, str, obj);
        }

        @Generated
        public boolean isDisableVersionCheck() {
            return this.disableVersionCheck;
        }

        public /* synthetic */ Object methodMissing(String str, Object obj) {
            $getCallSiteArray();
            return ScriptBytecodeAdapter.invokeMethodN(GoogleServicesPluginConfig.class, GoogleServicesPlugin.class, ShortTypeHandling.castToString(new GStringImpl(new Object[]{str}, new String[]{"", ""})), ScriptBytecodeAdapter.despreadList(new Object[0], new Object[]{obj}, new int[]{0}));
        }

        public /* synthetic */ Object propertyMissing(String str) {
            $getCallSiteArray();
            return ScriptBytecodeAdapter.getProperty(GoogleServicesPluginConfig.class, GoogleServicesPlugin.class, ShortTypeHandling.castToString(new GStringImpl(new Object[]{str}, new String[]{"", ""})));
        }

        public /* synthetic */ void propertyMissing(String str, Object obj) {
            $getCallSiteArray();
            ScriptBytecodeAdapter.setProperty(obj, (Class) null, GoogleServicesPlugin.class, ShortTypeHandling.castToString(new GStringImpl(new Object[]{str}, new String[]{"", ""})));
        }

        @Generated
        public void setDisableVersionCheck(boolean z) {
            this.disableVersionCheck = z;
        }

        @Generated
        @Internal
        public /* synthetic */ void setMetaClass(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        @Generated
        @Internal
        public /* synthetic */ void setProperty(String str, Object obj) {
            getMetaClass().setProperty(this, str, obj);
        }
    }

    private static /* synthetic */ CallSiteArray $createCallSiteArray() {
        String[] strArr = new String[53];
        $createCallSiteArray_1(strArr);
        return new CallSiteArray(GoogleServicesPlugin.class, strArr);
    }

    private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
        strArr[0] = "create";
        strArr[1] = "extensions";
        strArr[2] = "afterEvaluate";
        strArr[3] = "iterator";
        strArr[4] = "values";
        strArr[5] = "iterator";
        strArr[6] = "plugins";
        strArr[7] = "hasPlugin";
        strArr[8] = "plugins";
        strArr[9] = "setupPlugin";
        strArr[10] = "showWarningForPluginLocation";
        strArr[11] = "withId";
        strArr[12] = "plugins";
        strArr[13] = "withId";
        strArr[14] = "plugins";
        strArr[15] = "withId";
        strArr[16] = "plugins";
        strArr[17] = "warn";
        strArr[18] = "getLogger";
        strArr[19] = "APPLICATION";
        strArr[20] = "all";
        strArr[21] = "applicationVariants";
        strArr[22] = "android";
        strArr[23] = "LIBRARY";
        strArr[24] = "all";
        strArr[25] = "libraryVariants";
        strArr[26] = "android";
        strArr[27] = "FEATURE";
        strArr[28] = "all";
        strArr[29] = "featureVariants";
        strArr[30] = "android";
        strArr[31] = "MODEL_APPLICATION";
        strArr[32] = "all";
        strArr[33] = "applicationVariants";
        strArr[34] = "android";
        strArr[35] = "model";
        strArr[36] = "MODEL_LIBRARY";
        strArr[37] = "all";
        strArr[38] = "libraryVariants";
        strArr[39] = "android";
        strArr[40] = "model";
        strArr[41] = "file";
        strArr[42] = "buildDir";
        strArr[43] = "dirName";
        strArr[44] = "register";
        strArr[45] = "tasks";
        strArr[46] = "capitalize";
        strArr[47] = "name";
        strArr[48] = "respondsTo";
        strArr[49] = "configure";
        strArr[50] = "mergeResourcesProvider";
        strArr[51] = "dependsOn";
        strArr[52] = "mergeResources";
    }

    private static /* synthetic */ CallSite[] $getCallSiteArray() {
        CallSiteArray $createCallSiteArray;
        SoftReference softReference = $callSiteArray;
        if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
            $createCallSiteArray = $createCallSiteArray();
            $callSiteArray = new SoftReference($createCallSiteArray);
        }
        return $createCallSiteArray.array;
    }

    @Generated
    public GoogleServicesPlugin() {
        $getCallSiteArray();
        this.metaClass = $getStaticMetaClass();
    }

    protected /* synthetic */ MetaClass $getStaticMetaClass() {
        if (getClass() != GoogleServicesPlugin.class) {
            return ScriptBytecodeAdapter.initMetaClass(this);
        }
        ClassInfo classInfo = $staticClassInfo;
        if (classInfo == null) {
            classInfo = ClassInfo.getClassInfo(getClass());
            $staticClassInfo = classInfo;
        }
        return classInfo.getMetaClass();
    }

    @Generated
    @Internal
    public /* synthetic */ MetaClass getMetaClass() {
        MetaClass metaClass = this.metaClass;
        if (metaClass != null) {
            return metaClass;
        }
        MetaClass $getStaticMetaClass = $getStaticMetaClass();
        this.metaClass = $getStaticMetaClass;
        return $getStaticMetaClass;
    }

    @Generated
    @Internal
    public /* synthetic */ Object getProperty(String str) {
        return getMetaClass().getProperty(this, str);
    }

    @Generated
    @Internal
    public /* synthetic */ Object invokeMethod(String str, Object obj) {
        return getMetaClass().invokeMethod(this, str, obj);
    }

    @Generated
    @Internal
    public /* synthetic */ void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Generated
    @Internal
    public /* synthetic */ void setProperty(String str, Object obj) {
        getMetaClass().setProperty(this, str, obj);
    }

    public /* synthetic */ Object this$dist$get$1(String str) {
        $getCallSiteArray();
        return ScriptBytecodeAdapter.getGroovyObjectProperty(GoogleServicesPlugin.class, this, ShortTypeHandling.castToString(new GStringImpl(new Object[]{str}, new String[]{"", ""})));
    }

    public /* synthetic */ Object this$dist$invoke$1(String str, Object obj) {
        $getCallSiteArray();
        return ScriptBytecodeAdapter.invokeMethodOnCurrentN(GoogleServicesPlugin.class, this, ShortTypeHandling.castToString(new GStringImpl(new Object[]{str}, new String[]{"", ""})), ScriptBytecodeAdapter.despreadList(new Object[0], new Object[]{obj}, new int[]{0}));
    }

    public /* synthetic */ void this$dist$set$1(String str, Object obj) {
        $getCallSiteArray();
        ScriptBytecodeAdapter.setGroovyObjectProperty(obj, GoogleServicesPlugin.class, this, ShortTypeHandling.castToString(new GStringImpl(new Object[]{str}, new String[]{"", ""})));
    }

    public void apply(Project project) {
        Reference project2 = new Reference(project);
        CallSite[] $getCallSiteArray = $getCallSiteArray();
        Reference config = new Reference((GoogleServicesPluginConfig) ScriptBytecodeAdapter.castToType($getCallSiteArray[0].call($getCallSiteArray[1].callGetProperty((Project) project2.get()), "googleServices", GoogleServicesPluginConfig.class), GoogleServicesPluginConfig.class));
        $getCallSiteArray[2].call((Project) project2.get(), new _apply_closure1(this, this, config, project2));
        Iterator it = (Iterator) ScriptBytecodeAdapter.castToType($getCallSiteArray[3].call($getCallSiteArray[4].call(PluginType.class)), Iterator.class);
        while (it.hasNext()) {
            PluginType pluginType = (PluginType) ShortTypeHandling.castToEnum(it.next(), PluginType.class);
            Iterator it2 = (Iterator) ScriptBytecodeAdapter.castToType($getCallSiteArray[5].call($getCallSiteArray[6].call(pluginType)), Iterator.class);
            while (it2.hasNext()) {
                String plugin = ShortTypeHandling.castToString(it2.next());
                if (DefaultTypeTransformation.booleanUnbox($getCallSiteArray[7].call($getCallSiteArray[8].callGetProperty((Project) project2.get()), plugin))) {
                    $getCallSiteArray[9].callCurrent(this, (Project) project2.get(), pluginType);
                    return;
                }
            }
        }
        $getCallSiteArray[10].callCurrent(this, (Project) project2.get());
        $getCallSiteArray[11].call($getCallSiteArray[12].callGetProperty((Project) project2.get()), "android", new _apply_closure2(this, this, project2));
        $getCallSiteArray[13].call($getCallSiteArray[14].callGetProperty((Project) project2.get()), "android-library", new _apply_closure3(this, this, project2));
        $getCallSiteArray[15].call($getCallSiteArray[16].callGetProperty((Project) project2.get()), "android-feature", new _apply_closure4(this, this, project2));
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _apply_closure1 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference config;
        private /* synthetic */ Reference project;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[8];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_apply_closure1.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "disableVersionCheck";
            strArr[1] = "<$constructor$>";
            strArr[2] = "<$constructor$>";
            strArr[3] = "getName";
            strArr[4] = "plus";
            strArr[5] = "plus";
            strArr[6] = "all";
            strArr[7] = "getConfigurations";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _apply_closure1(Object obj, Object obj2, Reference reference, Reference reference2) {
            super(obj, obj2);
            $getCallSiteArray();
            this.config = reference;
            this.project = reference2;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _apply_closure1.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public Object doCall() {
            $getCallSiteArray();
            return doCall(null);
        }

        @Generated
        public GoogleServicesPluginConfig getConfig() {
            $getCallSiteArray();
            return (GoogleServicesPluginConfig) ScriptBytecodeAdapter.castToType(this.config.get(), GoogleServicesPluginConfig.class);
        }

        @Generated
        public Project getProject() {
            $getCallSiteArray();
            return (Project) ScriptBytecodeAdapter.castToType(this.project.get(), Project.class);
        }

        public Object doCall(Object it) {
            CallSite[] $getCallSiteArray = $getCallSiteArray();
            if (DefaultTypeTransformation.booleanUnbox($getCallSiteArray[0].callGroovyObjectGetProperty(this.config.get()))) {
                return null;
            }
            DependencyAnalyzer globalDependencies = (DependencyAnalyzer) ScriptBytecodeAdapter.castToType($getCallSiteArray[1].callConstructor(DependencyAnalyzer.class), DependencyAnalyzer.class);
            Reference strictVersionDepInspector = new Reference((DependencyInspector) ScriptBytecodeAdapter.castToType($getCallSiteArray[2].callConstructor(DependencyInspector.class, globalDependencies, $getCallSiteArray[3].call(this.project.get()), $getCallSiteArray[4].call($getCallSiteArray[5].call("This error message came from the google-services Gradle plugin, report", " issues at https://github.com/google/play-services-plugins and disable by "), "adding \"googleServices { disableVersionCheck = true }\" to your build.gradle file.")), DependencyInspector.class));
            return $getCallSiteArray[6].call($getCallSiteArray[7].call(this.project.get()), new _closure12(this, getThisObject(), strictVersionDepInspector));
        }

        /* compiled from: GoogleServicesPlugin.groovy */
        /* loaded from: classes.dex */
        public final class _closure12 extends Closure implements GeneratedClosure {
            private static /* synthetic */ SoftReference $callSiteArray;
            private static /* synthetic */ ClassInfo $staticClassInfo;
            public static transient /* synthetic */ boolean __$stMC;
            private /* synthetic */ Reference strictVersionDepInspector;

            private static /* synthetic */ CallSiteArray $createCallSiteArray() {
                String[] strArr = new String[4];
                $createCallSiteArray_1(strArr);
                return new CallSiteArray(_closure12.class, strArr);
            }

            private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
                strArr[0] = "contains";
                strArr[1] = "getName";
                strArr[2] = "afterResolve";
                strArr[3] = "getIncoming";
            }

            private static /* synthetic */ CallSite[] $getCallSiteArray() {
                CallSiteArray $createCallSiteArray;
                SoftReference softReference = $callSiteArray;
                if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                    $createCallSiteArray = $createCallSiteArray();
                    $callSiteArray = new SoftReference($createCallSiteArray);
                }
                return $createCallSiteArray.array;
            }

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            public _closure12(Object obj, Object obj2, Reference reference) {
                super(obj, obj2);
                $getCallSiteArray();
                this.strictVersionDepInspector = reference;
            }

            protected /* synthetic */ MetaClass $getStaticMetaClass() {
                if (getClass() != _closure12.class) {
                    return ScriptBytecodeAdapter.initMetaClass(this);
                }
                ClassInfo classInfo = $staticClassInfo;
                if (classInfo == null) {
                    classInfo = ClassInfo.getClassInfo(getClass());
                    $staticClassInfo = classInfo;
                }
                return classInfo.getMetaClass();
            }

            @Generated
            public DependencyInspector getStrictVersionDepInspector() {
                $getCallSiteArray();
                return (DependencyInspector) ScriptBytecodeAdapter.castToType(this.strictVersionDepInspector.get(), DependencyInspector.class);
            }

            public Object doCall(Object projectConfig) {
                CallSite[] $getCallSiteArray = $getCallSiteArray();
                if (DefaultTypeTransformation.booleanUnbox($getCallSiteArray[0].call($getCallSiteArray[1].call(projectConfig), "ompile"))) {
                    return $getCallSiteArray[2].call($getCallSiteArray[3].call(projectConfig), ScriptBytecodeAdapter.getMethodPointer(this.strictVersionDepInspector.get(), "afterResolve"));
                }
                return null;
            }
        }
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _apply_closure2 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference project;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[2];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_apply_closure2.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "setupPlugin";
            strArr[1] = "APPLICATION";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _apply_closure2(Object obj, Object obj2, Reference reference) {
            super(obj, obj2);
            $getCallSiteArray();
            this.project = reference;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _apply_closure2.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public Object doCall() {
            $getCallSiteArray();
            return doCall(null);
        }

        @Generated
        public Project getProject() {
            $getCallSiteArray();
            return (Project) ScriptBytecodeAdapter.castToType(this.project.get(), Project.class);
        }

        public Object doCall(Object it) {
            CallSite[] $getCallSiteArray = $getCallSiteArray();
            return $getCallSiteArray[0].callCurrent(this, this.project.get(), $getCallSiteArray[1].callGetProperty(PluginType.class));
        }
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _apply_closure3 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference project;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[2];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_apply_closure3.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "setupPlugin";
            strArr[1] = "LIBRARY";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _apply_closure3(Object obj, Object obj2, Reference reference) {
            super(obj, obj2);
            $getCallSiteArray();
            this.project = reference;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _apply_closure3.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public Object doCall() {
            $getCallSiteArray();
            return doCall(null);
        }

        @Generated
        public Project getProject() {
            $getCallSiteArray();
            return (Project) ScriptBytecodeAdapter.castToType(this.project.get(), Project.class);
        }

        public Object doCall(Object it) {
            CallSite[] $getCallSiteArray = $getCallSiteArray();
            return $getCallSiteArray[0].callCurrent(this, this.project.get(), $getCallSiteArray[1].callGetProperty(PluginType.class));
        }
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _apply_closure4 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference project;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[2];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_apply_closure4.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "setupPlugin";
            strArr[1] = "FEATURE";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _apply_closure4(Object obj, Object obj2, Reference reference) {
            super(obj, obj2);
            $getCallSiteArray();
            this.project = reference;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _apply_closure4.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public Object doCall() {
            $getCallSiteArray();
            return doCall(null);
        }

        @Generated
        public Project getProject() {
            $getCallSiteArray();
            return (Project) ScriptBytecodeAdapter.castToType(this.project.get(), Project.class);
        }

        public Object doCall(Object it) {
            CallSite[] $getCallSiteArray = $getCallSiteArray();
            return $getCallSiteArray[0].callCurrent(this, this.project.get(), $getCallSiteArray[1].callGetProperty(PluginType.class));
        }
    }

    private void showWarningForPluginLocation(Project project) {
        CallSite[] $getCallSiteArray = $getCallSiteArray();
        $getCallSiteArray[17].call($getCallSiteArray[18].call(project), "Warning: Please apply google-services plugin at the bottom of the build file.");
    }

    private void setupPlugin(Project project, PluginType pluginType) {
        Reference project2 = new Reference(project);
        CallSite[] $getCallSiteArray = $getCallSiteArray();
        if (ScriptBytecodeAdapter.isCase(pluginType, $getCallSiteArray[19].callGetProperty(PluginType.class))) {
            $getCallSiteArray[20].call($getCallSiteArray[21].callGetProperty($getCallSiteArray[22].callGetProperty((Project) project2.get())), new _setupPlugin_closure5(this, this, project2));
            return;
        }
        if (ScriptBytecodeAdapter.isCase(pluginType, $getCallSiteArray[23].callGetProperty(PluginType.class))) {
            $getCallSiteArray[24].call($getCallSiteArray[25].callGetProperty($getCallSiteArray[26].callGetProperty((Project) project2.get())), new _setupPlugin_closure6(this, this, project2));
            return;
        }
        if (ScriptBytecodeAdapter.isCase(pluginType, $getCallSiteArray[27].callGetProperty(PluginType.class))) {
            $getCallSiteArray[28].call($getCallSiteArray[29].callGetProperty($getCallSiteArray[30].callGetProperty((Project) project2.get())), new _setupPlugin_closure7(this, this, project2));
        } else if (ScriptBytecodeAdapter.isCase(pluginType, $getCallSiteArray[31].callGetProperty(PluginType.class))) {
            $getCallSiteArray[32].call($getCallSiteArray[33].callGetProperty($getCallSiteArray[34].callGetProperty($getCallSiteArray[35].callGetProperty((Project) project2.get()))), new _setupPlugin_closure8(this, this, project2));
        } else if (ScriptBytecodeAdapter.isCase(pluginType, $getCallSiteArray[36].callGetProperty(PluginType.class))) {
            $getCallSiteArray[37].call($getCallSiteArray[38].callGetProperty($getCallSiteArray[39].callGetProperty($getCallSiteArray[40].callGetProperty((Project) project2.get()))), new _setupPlugin_closure9(this, this, project2));
        }
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _setupPlugin_closure5 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference project;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[1];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_setupPlugin_closure5.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "handleVariant";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _setupPlugin_closure5(Object obj, Object obj2, Reference reference) {
            super(obj, obj2);
            $getCallSiteArray();
            this.project = reference;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _setupPlugin_closure5.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public Project getProject() {
            $getCallSiteArray();
            return (Project) ScriptBytecodeAdapter.castToType(this.project.get(), Project.class);
        }

        public Object doCall(Object variant) {
            return $getCallSiteArray()[0].callStatic(GoogleServicesPlugin.class, this.project.get(), variant);
        }
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _setupPlugin_closure6 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference project;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[1];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_setupPlugin_closure6.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "handleVariant";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _setupPlugin_closure6(Object obj, Object obj2, Reference reference) {
            super(obj, obj2);
            $getCallSiteArray();
            this.project = reference;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _setupPlugin_closure6.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public Project getProject() {
            $getCallSiteArray();
            return (Project) ScriptBytecodeAdapter.castToType(this.project.get(), Project.class);
        }

        public Object doCall(Object variant) {
            return $getCallSiteArray()[0].callStatic(GoogleServicesPlugin.class, this.project.get(), variant);
        }
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _setupPlugin_closure7 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference project;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[1];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_setupPlugin_closure7.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "handleVariant";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _setupPlugin_closure7(Object obj, Object obj2, Reference reference) {
            super(obj, obj2);
            $getCallSiteArray();
            this.project = reference;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _setupPlugin_closure7.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public Project getProject() {
            $getCallSiteArray();
            return (Project) ScriptBytecodeAdapter.castToType(this.project.get(), Project.class);
        }

        public Object doCall(Object variant) {
            return $getCallSiteArray()[0].callStatic(GoogleServicesPlugin.class, this.project.get(), variant);
        }
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _setupPlugin_closure8 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference project;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[1];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_setupPlugin_closure8.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "handleVariant";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _setupPlugin_closure8(Object obj, Object obj2, Reference reference) {
            super(obj, obj2);
            $getCallSiteArray();
            this.project = reference;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _setupPlugin_closure8.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public Project getProject() {
            $getCallSiteArray();
            return (Project) ScriptBytecodeAdapter.castToType(this.project.get(), Project.class);
        }

        public Object doCall(Object variant) {
            return $getCallSiteArray()[0].callStatic(GoogleServicesPlugin.class, this.project.get(), variant);
        }
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _setupPlugin_closure9 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference project;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[1];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_setupPlugin_closure9.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "handleVariant";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _setupPlugin_closure9(Object obj, Object obj2, Reference reference) {
            super(obj, obj2);
            $getCallSiteArray();
            this.project = reference;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _setupPlugin_closure9.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public Project getProject() {
            $getCallSiteArray();
            return (Project) ScriptBytecodeAdapter.castToType(this.project.get(), Project.class);
        }

        public Object doCall(Object variant) {
            return $getCallSiteArray()[0].callStatic(GoogleServicesPlugin.class, this.project.get(), variant);
        }
    }

    private static void handleVariant(Project project, Object variant) {
        Reference project2 = new Reference(project);
        Reference variant2 = new Reference(variant);
        CallSite[] $getCallSiteArray = $getCallSiteArray();
        Reference outputDir = new Reference((File) ScriptBytecodeAdapter.castToType($getCallSiteArray[41].call((Project) project2.get(), new GStringImpl(new Object[]{$getCallSiteArray[42].callGetProperty((Project) project2.get()), $getCallSiteArray[43].callGetProperty(variant2.get())}, new String[]{"", "/generated/res/google-services/", ""})), File.class));
        Reference processTask = new Reference($getCallSiteArray[44].call($getCallSiteArray[45].callGetProperty((Project) project2.get()), new GStringImpl(new Object[]{$getCallSiteArray[46].call($getCallSiteArray[47].callGetProperty(variant2.get()))}, new String[]{"process", "GoogleServices"}), GoogleServicesTask.class, new _handleVariant_closure10(GoogleServicesPlugin.class, GoogleServicesPlugin.class, outputDir, variant2, project2)));
        if (DefaultTypeTransformation.booleanUnbox($getCallSiteArray[48].call(variant2.get(), "getMergeResourcesProvider"))) {
            $getCallSiteArray[49].call($getCallSiteArray[50].callGetProperty(variant2.get()), new _handleVariant_closure11(GoogleServicesPlugin.class, GoogleServicesPlugin.class, processTask));
        } else {
            $getCallSiteArray[51].call($getCallSiteArray[52].callGetProperty(variant2.get()), processTask.get());
        }
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _handleVariant_closure10 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference outputDir;
        private /* synthetic */ Reference project;
        private /* synthetic */ Reference variant;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[17];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_handleVariant_closure10.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "setIntermediateDir";
            strArr[1] = "set";
            strArr[2] = "applicationId";
            strArr[3] = "applicationId";
            strArr[4] = "setBuildType";
            strArr[5] = "name";
            strArr[6] = "buildType";
            strArr[7] = "setProductFlavors";
            strArr[8] = "collect";
            strArr[9] = "productFlavors";
            strArr[10] = "respondsTo";
            strArr[11] = "builtBy";
            strArr[12] = "files";
            strArr[13] = "ext";
            strArr[14] = "registerGeneratedResFolders";
            strArr[15] = "generatedResFolders";
            strArr[16] = "registerResGeneratingTask";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _handleVariant_closure10(Object obj, Object obj2, Reference reference, Reference reference2, Reference reference3) {
            super(obj, obj2);
            $getCallSiteArray();
            this.outputDir = reference;
            this.variant = reference2;
            this.project = reference3;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _handleVariant_closure10.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public File getOutputDir() {
            $getCallSiteArray();
            return (File) ScriptBytecodeAdapter.castToType(this.outputDir.get(), File.class);
        }

        @Generated
        public Project getProject() {
            $getCallSiteArray();
            return (Project) ScriptBytecodeAdapter.castToType(this.project.get(), Project.class);
        }

        @Generated
        public Object getVariant() {
            $getCallSiteArray();
            return this.variant.get();
        }

        public Object doCall(Object task) {
            CallSite[] $getCallSiteArray = $getCallSiteArray();
            $getCallSiteArray[0].call(task, this.outputDir.get());
            $getCallSiteArray[1].call($getCallSiteArray[2].callGetProperty(task), $getCallSiteArray[3].callGetProperty(this.variant.get()));
            $getCallSiteArray[4].call(task, $getCallSiteArray[5].callGetProperty($getCallSiteArray[6].callGetProperty(this.variant.get())));
            $getCallSiteArray[7].call(task, $getCallSiteArray[8].call($getCallSiteArray[9].callGetProperty(this.variant.get()), new _closure13(this, getThisObject())));
            if (DefaultTypeTransformation.booleanUnbox($getCallSiteArray[10].call(this.variant.get(), "registerGeneratedResFolders"))) {
                ScriptBytecodeAdapter.setProperty($getCallSiteArray[11].call($getCallSiteArray[12].call(this.project.get(), this.outputDir.get()), task), (Class) null, $getCallSiteArray[13].callGetProperty(task), "generatedResFolders");
                return $getCallSiteArray[14].call(this.variant.get(), $getCallSiteArray[15].callGetProperty(task));
            }
            return $getCallSiteArray[16].call(this.variant.get(), task, this.outputDir.get());
        }

        /* compiled from: GoogleServicesPlugin.groovy */
        /* loaded from: classes.dex */
        public final class _closure13 extends Closure implements GeneratedClosure {
            private static /* synthetic */ SoftReference $callSiteArray;
            private static /* synthetic */ ClassInfo $staticClassInfo;
            public static transient /* synthetic */ boolean __$stMC;

            private static /* synthetic */ CallSiteArray $createCallSiteArray() {
                String[] strArr = new String[1];
                $createCallSiteArray_1(strArr);
                return new CallSiteArray(_closure13.class, strArr);
            }

            private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
                strArr[0] = "name";
            }

            private static /* synthetic */ CallSite[] $getCallSiteArray() {
                CallSiteArray $createCallSiteArray;
                SoftReference softReference = $callSiteArray;
                if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                    $createCallSiteArray = $createCallSiteArray();
                    $callSiteArray = new SoftReference($createCallSiteArray);
                }
                return $createCallSiteArray.array;
            }

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            public _closure13(Object obj, Object obj2) {
                super(obj, obj2);
                $getCallSiteArray();
            }

            protected /* synthetic */ MetaClass $getStaticMetaClass() {
                if (getClass() != _closure13.class) {
                    return ScriptBytecodeAdapter.initMetaClass(this);
                }
                ClassInfo classInfo = $staticClassInfo;
                if (classInfo == null) {
                    classInfo = ClassInfo.getClassInfo(getClass());
                    $staticClassInfo = classInfo;
                }
                return classInfo.getMetaClass();
            }

            @Generated
            public Object doCall() {
                $getCallSiteArray();
                return doCall(null);
            }

            public Object doCall(Object it) {
                return $getCallSiteArray()[0].callGetProperty(it);
            }
        }
    }

    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public final class _handleVariant_closure11 extends Closure implements GeneratedClosure {
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        public static transient /* synthetic */ boolean __$stMC;
        private /* synthetic */ Reference processTask;

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[1];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(_handleVariant_closure11.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "dependsOn";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        public _handleVariant_closure11(Object obj, Object obj2, Reference reference) {
            super(obj, obj2);
            $getCallSiteArray();
            this.processTask = reference;
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != _handleVariant_closure11.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        public Object doCall() {
            $getCallSiteArray();
            return doCall(null);
        }

        @Generated
        public Object getProcessTask() {
            $getCallSiteArray();
            return this.processTask.get();
        }

        public Object doCall(Object it) {
            return $getCallSiteArray()[0].callCurrent(this, this.processTask.get());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Failed to restore enum class, 'enum' modifier and super class removed */
    /* JADX WARN: Unknown enum class pattern. Please report as an issue! */
    /* compiled from: GoogleServicesPlugin.groovy */
    /* loaded from: classes.dex */
    public static final class PluginType implements GroovyObject {
        private static final /* synthetic */ PluginType[] $VALUES;
        private static /* synthetic */ SoftReference $callSiteArray;
        private static /* synthetic */ ClassInfo $staticClassInfo;
        private static /* synthetic */ ClassInfo $staticClassInfo$;
        public static final PluginType APPLICATION;
        public static final PluginType FEATURE;
        public static final PluginType LIBRARY;
        public static final PluginType MAX_VALUE;
        public static final PluginType MIN_VALUE;
        public static final PluginType MODEL_APPLICATION;
        public static final PluginType MODEL_LIBRARY;
        public static transient /* synthetic */ boolean __$stMC;
        private transient /* synthetic */ MetaClass metaClass;
        private final Collection plugins;

        public static final /* synthetic */ PluginType $INIT(Object... objArr) {
            $getCallSiteArray();
            Object[] despreadList = ScriptBytecodeAdapter.despreadList(new Object[0], new Object[]{objArr}, new int[]{0});
            switch (ScriptBytecodeAdapter.selectConstructorAndTransformArguments(despreadList, -1, PluginType.class)) {
                case -321238123:
                    return new PluginType(ShortTypeHandling.castToString(despreadList[0]), DefaultTypeTransformation.intUnbox(despreadList[1]), (Collection) ScriptBytecodeAdapter.castToType(despreadList[2], Collection.class));
                default:
                    throw new IllegalArgumentException("This class has been compiled with a super class which is binary incompatible with the current super class found on classpath. You should recompile this class with the new version.");
            }
        }

        private static /* synthetic */ CallSiteArray $createCallSiteArray() {
            String[] strArr = new String[15];
            $createCallSiteArray_1(strArr);
            return new CallSiteArray(PluginType.class, strArr);
        }

        private static /* synthetic */ void $createCallSiteArray_1(String[] strArr) {
            strArr[0] = "next";
            strArr[1] = "ordinal";
            strArr[2] = "size";
            strArr[3] = "getAt";
            strArr[4] = "previous";
            strArr[5] = "ordinal";
            strArr[6] = "minus";
            strArr[7] = "size";
            strArr[8] = "getAt";
            strArr[9] = "valueOf";
            strArr[10] = "$INIT";
            strArr[11] = "$INIT";
            strArr[12] = "$INIT";
            strArr[13] = "$INIT";
            strArr[14] = "$INIT";
        }

        private static /* synthetic */ CallSite[] $getCallSiteArray() {
            CallSiteArray $createCallSiteArray;
            SoftReference softReference = $callSiteArray;
            if (softReference == null || ($createCallSiteArray = (CallSiteArray) softReference.get()) == null) {
                $createCallSiteArray = $createCallSiteArray();
                $callSiteArray = new SoftReference($createCallSiteArray);
            }
            return $createCallSiteArray.array;
        }

        public static PluginType valueOf(String str) {
            return (PluginType) ShortTypeHandling.castToEnum($getCallSiteArray()[9].callStatic(PluginType.class, PluginType.class, str), PluginType.class);
        }

        public static final PluginType[] values() {
            $getCallSiteArray();
            return (PluginType[]) ScriptBytecodeAdapter.castToType($VALUES.clone(), PluginType[].class);
        }

        protected /* synthetic */ MetaClass $getStaticMetaClass() {
            if (getClass() != PluginType.class) {
                return ScriptBytecodeAdapter.initMetaClass(this);
            }
            ClassInfo classInfo = $staticClassInfo;
            if (classInfo == null) {
                classInfo = ClassInfo.getClassInfo(getClass());
                $staticClassInfo = classInfo;
            }
            return classInfo.getMetaClass();
        }

        @Generated
        @Internal
        public /* synthetic */ MetaClass getMetaClass() {
            MetaClass metaClass = this.metaClass;
            if (metaClass != null) {
                return metaClass;
            }
            MetaClass $getStaticMetaClass = $getStaticMetaClass();
            this.metaClass = $getStaticMetaClass;
            return $getStaticMetaClass;
        }

        @Generated
        @Internal
        public /* synthetic */ Object getProperty(String str) {
            return getMetaClass().getProperty(this, str);
        }

        @Generated
        @Internal
        public /* synthetic */ Object invokeMethod(String str, Object obj) {
            return getMetaClass().invokeMethod(this, str, obj);
        }

        public /* synthetic */ PluginType next() {
            CallSite[] $getCallSiteArray = $getCallSiteArray();
            Object call = $getCallSiteArray[0].call($getCallSiteArray[1].callCurrent(this));
            CallSite callSite = $getCallSiteArray[2];
            PluginType[] pluginTypeArr = $VALUES;
            if (ScriptBytecodeAdapter.compareGreaterThanEqual(call, callSite.call(pluginTypeArr))) {
                call = 0;
            }
            return (PluginType) ShortTypeHandling.castToEnum($getCallSiteArray[3].call(pluginTypeArr, call), PluginType.class);
        }

        public /* synthetic */ PluginType previous() {
            CallSite[] $getCallSiteArray = $getCallSiteArray();
            Object call = $getCallSiteArray[4].call($getCallSiteArray[5].callCurrent(this));
            if (ScriptBytecodeAdapter.compareLessThan(call, 0)) {
                call = $getCallSiteArray[6].call($getCallSiteArray[7].call($VALUES), 1);
            }
            return (PluginType) ShortTypeHandling.castToEnum($getCallSiteArray[8].call($VALUES, call), PluginType.class);
        }

        @Generated
        @Internal
        public /* synthetic */ void setMetaClass(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        @Generated
        @Internal
        public /* synthetic */ void setProperty(String str, Object obj) {
            getMetaClass().setProperty(this, str, obj);
        }

        static {
            PluginType pluginType = (PluginType) ShortTypeHandling.castToEnum($getCallSiteArray()[10].callStatic(PluginType.class, "APPLICATION", 0, ScriptBytecodeAdapter.createList(new Object[]{"android", "com.android.application"})), PluginType.class);
            APPLICATION = pluginType;
            PluginType pluginType2 = (PluginType) ShortTypeHandling.castToEnum($getCallSiteArray()[11].callStatic(PluginType.class, "LIBRARY", 1, ScriptBytecodeAdapter.createList(new Object[]{"android-library", "com.android.library"})), PluginType.class);
            LIBRARY = pluginType2;
            PluginType pluginType3 = (PluginType) ShortTypeHandling.castToEnum($getCallSiteArray()[12].callStatic(PluginType.class, "FEATURE", 2, ScriptBytecodeAdapter.createList(new Object[]{"android-feature", "com.android.feature"})), PluginType.class);
            FEATURE = pluginType3;
            PluginType pluginType4 = (PluginType) ShortTypeHandling.castToEnum($getCallSiteArray()[13].callStatic(PluginType.class, "MODEL_APPLICATION", 3, ScriptBytecodeAdapter.createList(new Object[]{"com.android.model.application"})), PluginType.class);
            MODEL_APPLICATION = pluginType4;
            PluginType pluginType5 = (PluginType) ShortTypeHandling.castToEnum($getCallSiteArray()[14].callStatic(PluginType.class, "MODEL_LIBRARY", 4, ScriptBytecodeAdapter.createList(new Object[]{"com.android.model.library"})), PluginType.class);
            MODEL_LIBRARY = pluginType5;
            MIN_VALUE = pluginType;
            MAX_VALUE = pluginType5;
            $VALUES = new PluginType[]{pluginType, pluginType2, pluginType3, pluginType4, pluginType5};
        }

        public PluginType(String __str, int __int, Collection plugins) {
            $getCallSiteArray();
            this.metaClass = $getStaticMetaClass();
            this.plugins = (Collection) ScriptBytecodeAdapter.castToType(plugins, Collection.class);
        }

        public Collection plugins() {
            $getCallSiteArray();
            return this.plugins;
        }
    }
}
