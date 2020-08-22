/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.teleport.event;

import io.github.nucleuspowered.nucleus.api.util.CancelMessageEvent;
import io.github.nucleuspowered.nucleus.api.util.MightOccurAsync;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.ServerLocation;

/**
 * These events are fired <em>before</em> teleportation actually happens.
 */
public interface NucleusTeleportEvent extends CancelMessageEvent {

    /**
     * Indicates that a teleport request (such as through <code>/tpa</code>) is being sent.
     */
    @MightOccurAsync
    interface Request extends NucleusTeleportEvent {

        /**
         * The recipient of the request.
         *
         * @return The {@link Player} in question.
         */
        Player getPlayer();

        /**
         * Called when the root cause wants to teleport to the target player (like /tpa).
         */
        interface CauseToPlayer extends Request {}

        /**
         * Called when the root cause wants the target player to teleport to them (like /tpahere).
         */
        interface PlayerToCause extends Request {}
    }

    /**
     * Called when a player is about to be teleported through the Nucleus system.
     */
    interface AboutToTeleport extends NucleusTeleportEvent {

        /**
         * Gets the proposed location of the entity that will be teleported.
         *
         * @return The {@link ServerLocation} that the player would teleport to.
         * Any changes should be made during the {@link MoveEntityEvent} or
         * {@link ChangeEntityWorldEvent.Reposition}.
         */
        ServerLocation getToLocation();

        /**
         * The {@link Player} to be teleported.
         *
         * @return The {@link Player} in question.
         */
        Player getPlayer();
    }

}