/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.NucleusPlugin;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class InternalServiceManager {

    private final NucleusPlugin plugin;
    private final Map<Class<?>, Object> serviceMap = Maps.newConcurrentMap();

    public InternalServiceManager(NucleusPlugin plugin) {
        this.plugin = plugin;
    }

    public <I, C extends I> void registerService(Class<I> key, C service) {
        if (serviceMap.containsKey(key)) {
            return;
        }

        serviceMap.put(key, service);
    }

    @SuppressWarnings("unchecked")
    public <I> Optional<I> getService(Class<I> key) {
        if (serviceMap.containsKey(key)) {
            return Optional.of((I)serviceMap.get(key));
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <I> I getServiceUnchecked(Class<I> key) {
        if (serviceMap.containsKey(key)) {
            return (I)serviceMap.get(key);
        }

        throw new NoSuchElementException(key.getName());
    }
}
