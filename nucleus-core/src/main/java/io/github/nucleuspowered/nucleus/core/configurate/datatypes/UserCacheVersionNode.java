/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.core.configurate.datatypes;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class UserCacheVersionNode {

    @Setting
    private int version = 1;

    @Setting
    private Map<UUID, UserCacheDataNode> node = new HashMap<>();

    public int getVersion() {
        return this.version;
    }

    public Map<UUID, UserCacheDataNode> getNode() {
        return this.node;
    }
}
