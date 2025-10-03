package com.google.android.gms.dynamite;

import dalvik.system.PathClassLoader;

/* loaded from: classes.dex */
final class zzh extends PathClassLoader {
    /* JADX INFO: Access modifiers changed from: package-private */
    public zzh(String str, ClassLoader classLoader) {
        super(str, classLoader);
    }

    @Override // java.lang.ClassLoader
    protected final Class<?> loadClass(String str, boolean z) throws ClassNotFoundException {
        if (!str.startsWith("java.") && !str.startsWith("android.")) {
            try {
                return findClass(str);
            } catch (ClassNotFoundException e) {
            }
        }
        return super.loadClass(str, z);
    }
}
