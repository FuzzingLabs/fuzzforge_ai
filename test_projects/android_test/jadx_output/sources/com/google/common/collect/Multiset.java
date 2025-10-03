package com.google.common.collect;

import com.android.tools.r8.annotations.SynthesizedClass;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multiset;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

/* loaded from: classes.dex */
public interface Multiset<E> extends Collection<E> {

    /* loaded from: classes.dex */
    public interface Entry<E> {
        boolean equals(Object obj);

        int getCount();

        E getElement();

        int hashCode();

        String toString();
    }

    int add(E e, int i);

    boolean add(E e);

    boolean contains(Object obj);

    @Override // java.util.Collection
    boolean containsAll(Collection<?> collection);

    int count(Object obj);

    Set<E> elementSet();

    Set<Entry<E>> entrySet();

    boolean equals(Object obj);

    void forEach(Consumer<? super E> consumer);

    void forEachEntry(ObjIntConsumer<? super E> objIntConsumer);

    int hashCode();

    Iterator<E> iterator();

    int remove(Object obj, int i);

    boolean remove(Object obj);

    boolean removeAll(Collection<?> collection);

    boolean retainAll(Collection<?> collection);

    int setCount(E e, int i);

    boolean setCount(E e, int i, int i2);

    int size();

    Spliterator<E> spliterator();

    String toString();

    @SynthesizedClass(kind = "$-CC")
    /* renamed from: com.google.common.collect.Multiset$-CC, reason: invalid class name */
    /* loaded from: classes.dex */
    public final /* synthetic */ class CC<E> {
        public static void $default$forEachEntry(Multiset _this, final ObjIntConsumer objIntConsumer) {
            Preconditions.checkNotNull(objIntConsumer);
            _this.entrySet().forEach(new Consumer() { // from class: com.google.common.collect.Multiset$$ExternalSyntheticLambda1
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    objIntConsumer.accept(r2.getElement(), ((Multiset.Entry) obj).getCount());
                }
            });
        }

        public static void $default$forEach(Multiset _this, final Consumer consumer) {
            Preconditions.checkNotNull(consumer);
            _this.entrySet().forEach(new Consumer() { // from class: com.google.common.collect.Multiset$$ExternalSyntheticLambda0
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Multiset.CC.lambda$forEach$1(consumer, (Multiset.Entry) obj);
                }
            });
        }

        public static /* synthetic */ void lambda$forEach$1(Consumer action, Entry entry) {
            Object element = entry.getElement();
            int count = entry.getCount();
            for (int i = 0; i < count; i++) {
                action.accept(element);
            }
        }
    }
}
