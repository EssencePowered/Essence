/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.services;

import io.github.nucleuspowered.nucleus.api.module.teleport.data.TeleportRequest;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

// Managing teleport requests.
public class TeleportRequestData extends TeleportTask implements TeleportRequest {

    private final Instant expiry;
    private boolean forcedExpired;
    private boolean expired;

    public TeleportRequestData(
            INucleusServiceCollection serviceCollection,
            UUID toTeleport,
            UUID target,
            Instant expiry,
            double cost,
            int warmup,
            @Nullable UUID requester,
            boolean safe,
            boolean silentTarget,
            boolean silentSource,
            @Nullable Transform<World> requestLocation,
            @Nullable Consumer<Player> successCallback) {
        super(serviceCollection, toTeleport, target, cost, warmup, safe, silentSource, silentTarget, requestLocation, requester, successCallback);
        this.expiry = expiry;
    }

    public Optional<Player> getToBeTeleported() {
        return Sponge.getServer().getPlayer(this.toTeleport);
    }

    public Optional<Player> getTarget() {
        return Sponge.getServer().getPlayer(this.target);
    }

    public void forceExpire(boolean callback) {
        if (!callback || isActive()) {
            this.forcedExpired = true;
            if (callback) {
                onCancel();
            }
        }
    }

    public boolean isActive() {
        if (!this.expired) {
            this.expired = (this.forcedExpired && Instant.now().isAfter(this.expiry));
            if (this.expired) {
                onCancel();
            }
        }

        return !this.forcedExpired;
    }

    public Instant getExpiryTime() {
        return this.expiry;
    }

}
