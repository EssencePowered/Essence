/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.events;

import io.github.nucleuspowered.nucleus.api.module.teleport.event.NucleusTeleportResponseEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public abstract class TeleportResponseEvent extends AbstractEvent implements NucleusTeleportResponseEvent {

    @Nullable private Text cancelMessage;
    private boolean isCancelled = false;

    private final Cause cause;
    private final UUID originUUID;
    private final UUID targetUUID;

    public TeleportResponseEvent(Cause cause, UUID originUUID, UUID targetUUID) {
        this.cause = cause;
        this.originUUID = originUUID;
        this.targetUUID = targetUUID;
    }


    @Override public Optional<Text> getCancelMessage() {
        return Optional.ofNullable(this.cancelMessage);
    }

    @Override public void setCancelMessage(@Nullable Text message) {
        this.cancelMessage = message;
    }

    @Override public User getTargetUser() {
        UserStorageService userStorageService = Sponge.getServiceManager().provide(UserStorageService.class).orElse(null);
        if (userStorageService != null) {
            return userStorageService.get(getTargetUUID()).orElse(null);
        }
        return null;
    }

    @Nullable @Override public User getOriginUser() {
        UserStorageService userStorageService = Sponge.getServiceManager().provide(UserStorageService.class).orElse(null);
        if (userStorageService != null) {
            return userStorageService.get(getOriginUUID()).orElse(null);
        }
        return null;
    }

    @Override public UUID getTargetUUID() {
        return this.targetUUID;
    }

    @Override public UUID getOriginUUID() {
        return this.originUUID;
    }

    @Override public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override public Cause getCause() {
        return this.cause;
    }

    public static class Accept extends TeleportResponseEvent implements NucleusTeleportResponseEvent.Accept {

        public Accept(Cause cause, UUID originUUID, UUID targetUUID) {
            super(cause, originUUID, targetUUID);
        }
    }

    public static class Deny extends TeleportResponseEvent implements NucleusTeleportResponseEvent.Deny {

        public Deny(Cause cause, UUID originUUID, UUID targetUUID) {
            super(cause, originUUID, targetUUID);
        }
    }
}
