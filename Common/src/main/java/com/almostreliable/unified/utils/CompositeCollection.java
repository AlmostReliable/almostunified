package com.almostreliable.unified.utils;

import com.google.common.collect.Iterables;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public class CompositeCollection<T> extends AbstractCollection<T> {

    private final Collection<? extends Collection<? extends T>> collections;

    public CompositeCollection(Collection<? extends Collection<? extends T>> collections) {
        this.collections = collections;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterables.concat(collections).iterator();
    }

    @Override
    public int size() {
        int size = 0;
        for (Collection<? extends T> collection : collections) {
            size += collection.size();
        }

        return size;
    }
}
