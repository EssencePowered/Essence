/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.events;

import io.github.nucleuspowered.nucleus.api.teleport.event.NucleusTeleportEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public abstract class CommandEvent extends AbstractEvent implements NucleusTeleportEvent.Command {

    @Nullable private Text cancelMessage;
    private boolean isCancelled = false;

    private final Cause cause;
    private final UUID originUser;
    private final UUID targetUser;

    public CommandEvent(Cause cause, UUID originUser, UUID targetUser) {
        this.cause = cause;
        this.originUser = originUser;
        this.targetUser = targetUser;
    }

    @Override public Optional<Text> getCancelMessage() {
        return Optional.ofNullable(this.cancelMessage);
    }

    @Override public void setCancelMessage(@Nullable Text message) {
        this.cancelMessage = message;
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

    @Nullable @Override public User getTargetUser() {
        UserStorageService userStorageService = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        return userStorageService.get(getTargetUUID()).orElse(null);
    }

    @Nullable @Override public User getOriginUser() {
        UserStorageService userStorageService = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
            return userStorageService.get(getOriginUUID()).orElse(null);
    }

    @Override public UUID getTargetUUID() {
        return targetUser;
    }

    @Override public UUID getOriginUUID() {
        return originUser;
    }

    public static class CauseToUser extends CommandEvent implements NucleusTeleportEvent.Command.CauseToUser {
        public CauseToUser(Cause cause, UUID originUser, UUID targetUser) {
            super(cause, originUser, targetUser);
        }
    }

    public static class UserToCause extends CommandEvent implements NucleusTeleportEvent.Command.UserToCause {
        public UserToCause(Cause cause, UUID originUser, UUID targetUser) {
            super(cause, originUser, targetUser);
        }
    }
}
