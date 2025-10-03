package com.google.firebase.platforminfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/* loaded from: classes.dex */
public class GlobalLibraryVersionRegistrar {
    private static volatile GlobalLibraryVersionRegistrar INSTANCE;
    private final Set<LibraryVersion> infos = new HashSet();

    GlobalLibraryVersionRegistrar() {
    }

    public void registerVersion(String sdkName, String version) {
        synchronized (this.infos) {
            this.infos.add(LibraryVersion.create(sdkName, version));
        }
    }

    Set<LibraryVersion> getRegisteredVersions() {
        Set<LibraryVersion> unmodifiableSet;
        synchronized (this.infos) {
            unmodifiableSet = Collections.unmodifiableSet(this.infos);
        }
        return unmodifiableSet;
    }

    public static GlobalLibraryVersionRegistrar getInstance() {
        GlobalLibraryVersionRegistrar localRef = INSTANCE;
        if (localRef == null) {
            synchronized (GlobalLibraryVersionRegistrar.class) {
                localRef = INSTANCE;
                if (localRef == null) {
                    GlobalLibraryVersionRegistrar globalLibraryVersionRegistrar = new GlobalLibraryVersionRegistrar();
                    localRef = globalLibraryVersionRegistrar;
                    INSTANCE = globalLibraryVersionRegistrar;
                }
            }
        }
        return localRef;
    }
}
