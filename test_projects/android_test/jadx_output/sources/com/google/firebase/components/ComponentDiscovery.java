package com.google.firebase.components;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.inject.Provider;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* loaded from: classes.dex */
public final class ComponentDiscovery<T> {
    private static final String COMPONENT_KEY_PREFIX = "com.google.firebase.components:";
    private static final String COMPONENT_SENTINEL_VALUE = "com.google.firebase.components.ComponentRegistrar";
    static final String TAG = "ComponentDiscovery";
    private final T context;
    private final RegistrarNameRetriever<T> retriever;

    /* loaded from: classes.dex */
    interface RegistrarNameRetriever<T> {
        List<String> retrieve(T t);
    }

    public static ComponentDiscovery<Context> forContext(Context context, Class<? extends Service> discoveryService) {
        return new ComponentDiscovery<>(context, new MetadataRegistrarNameRetriever(discoveryService));
    }

    ComponentDiscovery(T context, RegistrarNameRetriever<T> retriever) {
        this.context = context;
        this.retriever = retriever;
    }

    @Deprecated
    public List<ComponentRegistrar> discover() {
        List<ComponentRegistrar> result = new ArrayList<>();
        for (String registrarName : this.retriever.retrieve(this.context)) {
            try {
                ComponentRegistrar registrar = instantiate(registrarName);
                if (registrar != null) {
                    result.add(registrar);
                }
            } catch (InvalidRegistrarException ex) {
                Log.w(TAG, "Invalid component registrar.", ex);
            }
        }
        return result;
    }

    public List<Provider<ComponentRegistrar>> discoverLazy() {
        List<Provider<ComponentRegistrar>> result = new ArrayList<>();
        for (final String registrarName : this.retriever.retrieve(this.context)) {
            result.add(new Provider() { // from class: com.google.firebase.components.ComponentDiscovery$$ExternalSyntheticLambda0
                @Override // com.google.firebase.inject.Provider
                public final Object get() {
                    ComponentRegistrar instantiate;
                    instantiate = ComponentDiscovery.instantiate(registrarName);
                    return instantiate;
                }
            });
        }
        return result;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static ComponentRegistrar instantiate(String registrarName) {
        try {
            Class<?> loadedClass = Class.forName(registrarName);
            if (!ComponentRegistrar.class.isAssignableFrom(loadedClass)) {
                throw new InvalidRegistrarException(String.format("Class %s is not an instance of %s", registrarName, COMPONENT_SENTINEL_VALUE));
            }
            return (ComponentRegistrar) loadedClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, String.format("Class %s is not an found.", registrarName));
            return null;
        } catch (IllegalAccessException e2) {
            throw new InvalidRegistrarException(String.format("Could not instantiate %s.", registrarName), e2);
        } catch (InstantiationException e3) {
            throw new InvalidRegistrarException(String.format("Could not instantiate %s.", registrarName), e3);
        } catch (NoSuchMethodException e4) {
            throw new InvalidRegistrarException(String.format("Could not instantiate %s", registrarName), e4);
        } catch (InvocationTargetException e5) {
            throw new InvalidRegistrarException(String.format("Could not instantiate %s", registrarName), e5);
        }
    }

    /* loaded from: classes.dex */
    private static class MetadataRegistrarNameRetriever implements RegistrarNameRetriever<Context> {
        private final Class<? extends Service> discoveryService;

        private MetadataRegistrarNameRetriever(Class<? extends Service> discoveryService) {
            this.discoveryService = discoveryService;
        }

        @Override // com.google.firebase.components.ComponentDiscovery.RegistrarNameRetriever
        public List<String> retrieve(Context ctx) {
            Bundle metadata = getMetadata(ctx);
            if (metadata == null) {
                Log.w(ComponentDiscovery.TAG, "Could not retrieve metadata, returning empty list of registrars.");
                return Collections.emptyList();
            }
            List<String> registrarNames = new ArrayList<>();
            for (String key : metadata.keySet()) {
                Object rawValue = metadata.get(key);
                if (ComponentDiscovery.COMPONENT_SENTINEL_VALUE.equals(rawValue) && key.startsWith(ComponentDiscovery.COMPONENT_KEY_PREFIX)) {
                    registrarNames.add(key.substring(ComponentDiscovery.COMPONENT_KEY_PREFIX.length()));
                }
            }
            return registrarNames;
        }

        private Bundle getMetadata(Context context) {
            try {
                PackageManager manager = context.getPackageManager();
                if (manager == null) {
                    Log.w(ComponentDiscovery.TAG, "Context has no PackageManager.");
                    return null;
                }
                ServiceInfo info = manager.getServiceInfo(new ComponentName(context, this.discoveryService), 128);
                if (info == null) {
                    Log.w(ComponentDiscovery.TAG, this.discoveryService + " has no service info.");
                    return null;
                }
                return info.metaData;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(ComponentDiscovery.TAG, "Application info not found.");
                return null;
            }
        }
    }
}
