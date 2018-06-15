/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Optional;

@ConfigSerializable
public class WorldConfig {

    @Setting(value = "default-world-border-diameter", comment = "config.world.defaultborder")
    private long worldBorderDefault = 0;

    @Setting(value = "pre-generation")
    private WorldGen worldGen = new WorldGen();

    @Setting(value = "separate-permissions", comment = "config.worlds.separate")
    private boolean separatePermissions = false;

    public boolean isDisplayWarningGeneration() {
        return worldGen.displayWarningGeneration;
    }

    public boolean isDisplayAfterEachGen() {
        return worldGen.displayEach;
    }

    public long getNotificationInterval() {
        return Math.max(1, worldGen.timeToNotify);
    }

    public Optional<Long> getWorldBorderDefault() {
        if (worldBorderDefault < 1) {
            return Optional.empty();
        }

        return Optional.of(worldBorderDefault);
    }

    public boolean isSeparatePermissions() {
        return separatePermissions;
    }

    @ConfigSerializable
    public static class WorldGen {

        @Setting(value = "display-generation-warning", comment = "config.world.gen.warning")
        boolean displayWarningGeneration = true;

        @Setting(value = "display-after-each-gen", comment = "config.world.gen.each")
        boolean displayEach = false;

        @Setting(value = "notification-interval", comment = "config.world.gen.notify")
        long timeToNotify = 20;
    }
}
