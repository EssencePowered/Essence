/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.core;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.math.vector.Vector3d;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Retrieves Nucleus and general metadata about a {@link User}
 */
public interface NucleusPlayerMetadataService {

    /**
     * Gets the user metadata for a player with the specified UUID.
     * @param uuid The UUID of the user.
     * @return The {@link Result} if the user exists.
     */
    Optional<Result> getUserData(UUID uuid);

    /**
     * Gets the user metadata for a player with the specified {@link User}.
     * @param user The {@link User}.
     * @return The {@link Result} if the user exists.
     */
    default Optional<Result> getUserData(final User user) {
        return this.getUserData(user.uniqueId());
    }

    /**
     * Represents the metadata for a player.
     */
    interface Result {

        /**
         * Gets whether Nucleus will perform first join actions for this player on the next login.
         * This may be affected by server configuration.
         *
         * @return if first join actions may be run on this player's next login.
         */
        boolean hasFirstJoinBeenProcessed();

        /**
         * The {@link Instant} that the player last logged in. This does not get updated on logout,
         * see {@link #getLastLogout()}.
         *
         * @return The {@link Instant} the player last logged in, if any.
         */
        Optional<Instant> getLastLogin();

        /**
         * The {@link Instant} that the player last logged out. This does not get updated on login,
         * see {@link #getLastLogin()} ()}.
         *
         * @return The {@link Instant} the player last logged out, if any.
         */
        Optional<Instant> getLastLogout();

        /**
         * Gets the last known IP address of the user. This string is formed using
         * {@link InetAddress#toString()}
         *
         * @return The IP, if the player has logged in before.
         */
        Optional<String> getLastIP();

        /**
         * Gets the last known location of the user.
         *
         * <p>
         *     Note that this method exists to cater for unloaded worlds.
         * </p>
         *
         * @return The {@link Tuple} of the target {@link WorldProperties} and
         * {@link Vector3d} as the location.
         */
        Optional<Tuple<WorldProperties, Vector3d>> getLastLocation();
    }
}
