/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.storage.dataobjects.keyed;

import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.configurate.AbstractConfigurateBackedDataObject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.Optional;

import javax.annotation.Nullable;

public class AbstractKeyBasedDataObject<T extends IKeyedDataObject<T>> extends AbstractConfigurateBackedDataObject implements IKeyedDataObject<T> {

    @Override
    public boolean has(final DataKey<?, ? extends T> dataKey) {
        return !this.getNode(dataKey.getKey()).isVirtual();
    }

    public <V> Value<V> getAndSet(final DataKey<V, ? extends T> dataKey) {
        return new ValueImpl<>(this.getNullable(dataKey), dataKey);
    }

    @Nullable
    public <V> V getNullable(final DataKey<V, ? extends T> dataKey) {
        try {
            return this.getNode(dataKey.getKey()).getValue(dataKey.getType());
        } catch (final ObjectMappingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public <V> V getOrDefault(final DataKey<V, ? extends T> dataKey) {
        final V t = this.getNullable(dataKey);
        if (t == null) {
            return dataKey.getDefault();
        }

        return t;
    }

    public <V> Optional<V> get(final DataKey<V, ? extends T> dataKey) {
        return Optional.ofNullable(this.getNullable(dataKey));
    }

    public <V> boolean set(final DataKey<V, ? extends T> dataKey, final V data) {
        try {
            this.getNode(dataKey.getKey()).setValue(dataKey.getType(), data);
            return true;
        } catch (final ObjectMappingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void remove(final DataKey<?, ? extends T> dataKey) {
        this.getNode(dataKey.getKey()).setValue(null);
    }

    private ConfigurationNode getNode(final String[] key) {
        ConfigurationNode r = this.backingNode;
        for (final String k : key) {
            r = r.getNode(k);
        }

        return r;
    }

    public class ValueImpl<V, B extends T> implements IKeyedDataObject.Value<V> {

        @Nullable private V value;
        private final DataKey<V, B> dataKey;

        private ValueImpl(@Nullable final V value, final DataKey<V, B> dataKey) {
            this.value = value;
            this.dataKey = dataKey;
        }

        public Optional<V> getValue() {
            return Optional.ofNullable(this.value);
        }

        public void setValue(@Nullable final V value) {
            this.value = value;
        }

        @Override
        public void close() {
            if (this.value == null) {
                AbstractKeyBasedDataObject.this.remove(this.dataKey);
            } else {
                AbstractKeyBasedDataObject.this.set(this.dataKey, this.value);
            }
        }
    }
}
