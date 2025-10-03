package com.google.common.collect;

import com.google.common.collect.Multiset;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/* loaded from: classes.dex */
final class Serialization {
    private Serialization() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int readCount(ObjectInputStream stream) throws IOException {
        return stream.readInt();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <K, V> void writeMap(Map<K, V> map, ObjectOutputStream stream) throws IOException {
        stream.writeInt(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            stream.writeObject(entry.getKey());
            stream.writeObject(entry.getValue());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <K, V> void populateMap(Map<K, V> map, ObjectInputStream stream) throws IOException, ClassNotFoundException {
        int size = stream.readInt();
        populateMap(map, stream, size);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Multi-variable type inference failed */
    public static <K, V> void populateMap(Map<K, V> map, ObjectInputStream stream, int size) throws IOException, ClassNotFoundException {
        for (int i = 0; i < size; i++) {
            map.put(stream.readObject(), stream.readObject());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <E> void writeMultiset(Multiset<E> multiset, ObjectOutputStream stream) throws IOException {
        int entryCount = multiset.entrySet().size();
        stream.writeInt(entryCount);
        for (Multiset.Entry<E> entry : multiset.entrySet()) {
            stream.writeObject(entry.getElement());
            stream.writeInt(entry.getCount());
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <E> void populateMultiset(Multiset<E> multiset, ObjectInputStream stream) throws IOException, ClassNotFoundException {
        int distinctElements = stream.readInt();
        populateMultiset(multiset, stream, distinctElements);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Multi-variable type inference failed */
    public static <E> void populateMultiset(Multiset<E> multiset, ObjectInputStream stream, int distinctElements) throws IOException, ClassNotFoundException {
        for (int i = 0; i < distinctElements; i++) {
            Object readObject = stream.readObject();
            int count = stream.readInt();
            multiset.add(readObject, count);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <K, V> void writeMultimap(Multimap<K, V> multimap, ObjectOutputStream stream) throws IOException {
        stream.writeInt(multimap.asMap().size());
        for (Map.Entry<K, Collection<V>> entry : multimap.asMap().entrySet()) {
            stream.writeObject(entry.getKey());
            stream.writeInt(entry.getValue().size());
            for (V value : entry.getValue()) {
                stream.writeObject(value);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <K, V> void populateMultimap(Multimap<K, V> multimap, ObjectInputStream stream) throws IOException, ClassNotFoundException {
        int distinctKeys = stream.readInt();
        populateMultimap(multimap, stream, distinctKeys);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Multi-variable type inference failed */
    public static <K, V> void populateMultimap(Multimap<K, V> multimap, ObjectInputStream stream, int distinctKeys) throws IOException, ClassNotFoundException {
        for (int i = 0; i < distinctKeys; i++) {
            Collection collection = multimap.get(stream.readObject());
            int valueCount = stream.readInt();
            for (int j = 0; j < valueCount; j++) {
                collection.add(stream.readObject());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T> FieldSetter<T> getFieldSetter(Class<T> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return new FieldSetter<>(field);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    /* loaded from: classes.dex */
    static final class FieldSetter<T> {
        private final Field field;

        private FieldSetter(Field field) {
            this.field = field;
            field.setAccessible(true);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void set(T instance, Object value) {
            try {
                this.field.set(instance, value);
            } catch (IllegalAccessException impossible) {
                throw new AssertionError(impossible);
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        public void set(T instance, int value) {
            try {
                this.field.set(instance, Integer.valueOf(value));
            } catch (IllegalAccessException impossible) {
                throw new AssertionError(impossible);
            }
        }
    }
}
