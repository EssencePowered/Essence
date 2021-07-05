/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.module.teleport;

import io.github.nucleuspowered.nucleus.api.module.teleport.data.TeleportRequest;
import io.github.nucleuspowered.nucleus.api.teleport.data.TeleportResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * Functions of nucleus teleport
 */
public interface NucleusPlayerTeleporterService {

    boolean canTeleportTo(User source, User to);

    TeleportResult teleportWithMessage(
            CommandSource source,
            Player playerToTeleport,
            Player target,
            boolean safe,
            boolean quietSource,
            boolean quietTarget);

    boolean requestTeleport(
            @Nullable Player requester,
            User toRequest,
            double cost,
            int warmup,
            User playerToTeleport,
            User target,
            boolean safe,
            boolean silentTarget,
            boolean silentSource,
            @Nullable Consumer<Player> successCallback,
            String messageKey);

    Optional<TeleportRequest> getCurrentRequest(Player player);

    void removeAllRequests(Player player);

    void removeExpired();

    boolean accept(Player player);

    boolean deny(Player player);
}
