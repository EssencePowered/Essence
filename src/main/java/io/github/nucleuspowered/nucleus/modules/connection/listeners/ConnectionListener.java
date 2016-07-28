/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.connection.listeners;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.util.Tristate;

import java.util.Map;

public class ConnectionListener extends ListenerBase {

    private final String joinFullServer = PermissionRegistry.PERMISSIONS_PREFIX + "connection.joinfullserver";


    /**
     * At the time the player joins if the server is full, check if they are permitted to join a full server.
     *
     * @param event The event.
     */
    @Listener
    @IsCancelled(Tristate.UNDEFINED)
    public void onPlayerJoin(ClientConnectionEvent.Login event) {
        if (!(Sponge.getServer().getOnlinePlayers().size() >= Sponge.getServer().getMaxPlayers())) {
            return;
        }

        if (event.getTargetUser().hasPermission(joinFullServer)) {
            event.setCancelled(false);
        }
    }

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = Maps.newHashMap();
        mp.put(joinFullServer, new PermissionInformation(Util.getMessageWithFormat("permission.connection.joinfullserver"), SuggestedLevel.MOD));
        return mp;
    }
}
