// Copyright 2009 Indeed
package com.indeed.util.varexport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;

import java.util.Map;

/**
 * To be used instead of {@link com.indeed.util.varexport.Export} or the introspection methods
 * of {@link com.indeed.util.varexport.VarExporter} to export a manually updated variable.
 * <p>
 * Example usage:
 * <pre>
 *   ManagedVariable<Integer> var = ManagedVariable.<Integer>builder().setName("myvar").setValue(524).build();
 *   // ...
 *   var.set(52473);
 * </pre>
 *
 * @author jack@indeed.com (Jack Humphrey)
 */
public class LazilyManagedVariable<T> extends Variable<T> {

    public static <T> Builder<T> builder(final Class<T> c) {
        return new Builder<T>(c);
    }
    
    public static class Builder<T> {
        private final Class<T> c;
        private String name = null;
        private String doc = "";
        private boolean expand = false;
        private Supplier<T> valueSupplier = null;

        private Builder(final Class<T> c) {
            this.c = c;
        }

        public Builder<T> setName(final String name) {
            this.name = name;
            return this;
        }

        public Builder<T> setDoc(final String doc) {
            this.doc = doc;
            return this;
        }

        public Builder<T> setExpand(final boolean expand) {
            this.expand = expand;
            return this;
        }

        public Builder<T> setValue(final Supplier<T> valueSupplier) {
            this.valueSupplier = valueSupplier;
            return this;
        }

        public LazilyManagedVariable<T> build() {
            if (name == null) {
                throw new RuntimeException("name must not be null for ManagedVariable");
            }
            return new LazilyManagedVariable<T>(name, doc, expand, c, valueSupplier);
        }
    }

    @VisibleForTesting
    protected Supplier<Long> clock = new Supplier<Long>() {
        public Long get() {
            return System.currentTimeMillis();
        }
    };

    private final Class<T> c;
    private final Supplier<T> valueSupplier;
    private Long lastUpdated = clock.get();

    LazilyManagedVariable(final String name, final String doc, final boolean expand, final Class<T> c, final Supplier<T> valueSupplier) {
        super(name, doc, expand);
        this.c = c;
        this.valueSupplier = valueSupplier;
    }

    public void update() {
        lastUpdated = clock.get();
    }

    protected boolean canExpand() {
        return Map.class.isAssignableFrom(c);
    }

    @Override
    public Long getLastUpdated() {
        return lastUpdated;
    }

    public T getValue() {
        return valueSupplier.get();
    }
}