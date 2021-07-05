/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.teleport.event;

import io.github.nucleuspowered.nucleus.api.util.CancelMessageEvent;
import io.github.nucleuspowered.nucleus.api.util.MightOccurAsync;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;
import org.spongepowered.api.event.user.TargetUserEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import java.util.UUID;

import javax.annotation.Nullable;

/**
 * These events are fired <em>before</em> teleportation actually happens.
 */
@NonnullByDefault
public interface NucleusTeleportEvent extends CancelMessageEvent {

    /**
     * Indicates that a teleport request (such as through <code>/tpa</code>) is being sent.
     */
    @MightOccurAsync
    interface Request extends NucleusTeleportEvent, TargetUserEvent {

        /**
         * The recipient of the request.
         *
         * @return The {@link User} in question.
         */
        @Override User getTargetUser();

        /**
         * Called when the root cause wants to teleport to the target player (like /tpa).
         */
        interface CauseToPlayer extends Request {

        }

        /**
         * Called when the root cause wants the target player to teleport to them (like /tpahere).
         */
        interface PlayerToCause extends Request {

        }
    }

    @MightOccurAsync
    interface Command extends NucleusTeleportEvent, TargetUserEvent {

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

        /**
         * Called when the root cause wants to teleport to the target user (like /tp).
         */
        interface CauseToUser extends Command {

        }

        /**
         * Called when the root cause wants the target user to teleport to them (like /tphere).
         */
        interface UserToCause extends Command {

        }
    }

    /**
     * Called when a player is about to be teleported through the Nucleus system.
     */
    interface AboutToTeleport extends NucleusTeleportEvent, TargetPlayerEvent {

        /**
         * Gets the proposed location of the entity that will be teleported.
         *
         * @return The {@link Transform} that the player would teleport to. Note that for {@link Request}, this might change when the
         * teleportation gets underway - any changes should be made during the {@link MoveEntityEvent.Teleport} event.
         */
        Transform<World> getToTransform();

        /**
         * The {@link Player} to be teleported.
         *
         * @return The {@link Player} in question.
         */
        @Override Player getTargetEntity();
    }

}