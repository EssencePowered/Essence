/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.module.teleport.data;

import org.spongepowered.api.entity.living.player.Player;

import java.time.Instant;
import java.util.Optional;

public interface TeleportRequest {

    Optional<Player> getToBeTeleported();

    Optional<Player> getTarget();

    void forceExpire(boolean callback);

    boolean isActive();

    Instant getExpiryTime();
}
