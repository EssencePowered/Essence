/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.experience.listener;

import io.github.nucleuspowered.nucleus.modules.experience.ExperiencePermissions;
import io.github.nucleuspowered.nucleus.core.scaffold.listener.ListenerBase;
import io.github.nucleuspowered.nucleus.core.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.core.services.interfaces.IPermissionService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.Tristate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.inject.Inject;
import org.spongepowered.plugin.PluginContainer;

public class ExperienceListener implements ListenerBase {

    private final IPermissionService permissionService;
    private final Map<UUID, Integer> deadExpPlayers = new HashMap<>();
    private final PluginContainer pluginContainer;

    @Inject
    public ExperienceListener(final INucleusServiceCollection serviceCollection) {
        this.permissionService = serviceCollection.permissionService();
        this.pluginContainer = serviceCollection.pluginContainer();
    }

    // We check the tristate as we have three potential behaviours:
    // * TRUE: keep EXP
    // * FALSE (Explicitly set): remove EXP
    // * UNDEFINED: do whatever the system wants us to do.
    @Listener(order = Order.POST)
    public void onPlayerDeathMonitor(final DestructEntityEvent.Death deathEvent, @Getter("entity") final ServerPlayer player) {
        final Tristate tristate = this.permissionService.hasPermissionTristate(player, ExperiencePermissions.KEEP_EXP_PERMISSION);
        if (tristate == Tristate.TRUE) {
            final int exp = player.get(Keys.EXPERIENCE).orElse(0);
            this.deadExpPlayers.put(player.uniqueId(), exp);
        } else if (tristate == Tristate.FALSE) {
            this.deadExpPlayers.put(player.uniqueId(), 0);
        }
    }

    @Listener
    public void preventExperienceDroppingOrb(final SpawnEntityEvent event, @Root final Player player) {
        if (this.deadExpPlayers.getOrDefault(player.uniqueId(), 0) > 0) {
            // don't drop orbs for people who die, unless we're setting to zero.
            event.filterEntities(entity -> !(entity instanceof ExperienceOrb));
        }
    }

    @Listener
    public void onPlayerRespawn(final RespawnPlayerEvent.Post event, @Getter("entity") final ServerPlayer player) {
        this.applyExperience(player);
    }

    @Listener
    public void onPlayerJoin(final ServerSideConnectionEvent.Join event, @Getter("player") final ServerPlayer player) {
        this.applyExperience(player);
    }

    private void applyExperience(final ServerPlayer player) {
        if (this.deadExpPlayers.containsKey(player.uniqueId())) {
            final int exp = this.deadExpPlayers.get(player.uniqueId());
            final Task task =
                    Task.builder().delay(Ticks.of(1)).execute(() -> player.offer(Keys.EXPERIENCE, exp)).plugin(this.pluginContainer).build();
            Sponge.server().scheduler().submit(task);
            this.deadExpPlayers.remove(player.uniqueId());
        }
    }

}
