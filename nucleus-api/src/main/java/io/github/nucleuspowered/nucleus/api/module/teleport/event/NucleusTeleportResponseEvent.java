/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.module.teleport.event;

import io.github.nucleuspowered.nucleus.api.util.CancelMessageEvent;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.user.TargetUserEvent;

import java.util.UUID;

import javax.annotation.Nullable;

public interface NucleusTeleportResponseEvent extends CancelMessageEvent, TargetUserEvent {

    /**
     * @return Target {@link User}
     */
    @Nullable @Override User getTargetUser();

    /**
     * @return The {@link User} will be teleported
     */
    @Nullable
    User getOriginUser();

    /**
     * @return Target {@link User}'s {@link UUID}
     */
    UUID getTargetUUID();

    /**
     * @return The {@link UUID} {@link User} will be teleported
     */
    UUID getOriginUUID();

    interface Accept extends NucleusTeleportResponseEvent {}

    interface Deny extends NucleusTeleportResponseEvent {}
}
