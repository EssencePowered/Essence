/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.events;

import io.github.nucleuspowered.nucleus.api.events.NucleusRedirectableSpawnEvent;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
public class RedirectableSpawnEvent extends AbstractEvent implements NucleusRedirectableSpawnEvent {

    private Transform<World> transform;
    private final User targetUser;
    private final Cause cause;

    @Nullable
    private String cancelReason = null;
    private boolean isCancelled = false;
    private boolean isRedirected = false;

    public RedirectableSpawnEvent(Transform<World> transform, User targetUser, Cause cause) {
        this.transform = transform;
        this.targetUser = targetUser;
        this.cause = cause;
    }

    @Override public Transform<World> getTransformTo() {
        // Copy!
        return new Transform<>(this.transform.getExtent(), this.transform.getPosition(), this.transform.getRotation());
    }

    @Override public void setTransformTo(Transform<World> transform) {
        // Copy!
        this.transform = new Transform<>(transform.getExtent(), transform.getPosition(), transform.getRotation());
        isRedirected = true;
    }

    public boolean isRedirected() { return this.isRedirected; }

    @Override public void setCancelReason(String reason) {
        this.cancelReason = reason;
    }

    @Override public boolean isCancelled() {
        return this.isCancelled;
    }

    public Optional<String> getCancelReason() {
        return Optional.ofNullable(this.cancelReason);
    }

    @Override public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override public User getTargetUser() {
        return this.targetUser;
    }

    @Override public Cause getCause() {
        return this.cause;
    }
}
