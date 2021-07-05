/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.listeners;

import io.github.nucleuspowered.nucleus.api.EventContexts;
import io.github.nucleuspowered.nucleus.api.module.spawn.event.NucleusSendToSpawnEvent;
import io.github.nucleuspowered.nucleus.api.teleport.event.NucleusTeleportEvent;
import io.github.nucleuspowered.nucleus.api.util.data.NamedLocation;
import io.github.nucleuspowered.nucleus.modules.core.events.NucleusOnLoginEvent;
import io.github.nucleuspowered.nucleus.modules.fly.FlyKeys;
import io.github.nucleuspowered.nucleus.modules.jail.JailPermissions;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfig;
import io.github.nucleuspowered.nucleus.modules.jail.data.JailData;
import io.github.nucleuspowered.nucleus.modules.jail.services.JailHandler;
import io.github.nucleuspowered.nucleus.scaffold.listener.ListenerBase;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.nucleus.services.interfaces.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.interfaces.IPermissionService;
import io.github.nucleuspowered.nucleus.services.interfaces.IPlayerDisplayNameService;
import io.github.nucleuspowered.nucleus.services.interfaces.IReloadableService;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class JailListener implements IReloadableService.Reloadable, ListenerBase {

    private final IPermissionService permissionService;
    private final IMessageProviderService messageProviderService;
    private final IPlayerDisplayNameService playerDisplayNameService;
    private final JailHandler handler;
    private List<String> allowedCommands;
    private PluginContainer pluginContainer;

    @Inject
    public JailListener(INucleusServiceCollection serviceCollection) {
        this.permissionService = serviceCollection.permissionService();
        this.messageProviderService = serviceCollection.messageProvider();
        this.playerDisplayNameService = serviceCollection.playerDisplayNameService();
        this.handler = serviceCollection.getServiceUnchecked(JailHandler.class);
        this.pluginContainer = serviceCollection.pluginContainer();
    }

    // fires after spawn login event
    @Listener
    public void onPlayerLogin(final NucleusOnLoginEvent event, @Getter("getTargetUser") User user, @Getter("getUserService") IUserDataObject qs) {
        Optional<JailData> optionalJailData = this.handler.getPlayerJailDataInternal(user);
        if (!optionalJailData.isPresent()) {
            return;
        }

        JailData jd = optionalJailData.get();

        // Send them back to where they should be.
        Optional<NamedLocation> owl = this.handler.getWarpLocation(user);
        if (!owl.isPresent()) {
            new PermissionMessageChannel(this.permissionService, JailPermissions.JAIL_NOTIFY)
                    .send(Text.of(TextColors.RED, "WARNING: No jail is defined. Jailed players are going free!"));
            this.handler.unjailPlayer(user);
            return;
        }

        // always send the player back to the jail location
        event.setTo(owl.get().getTransform().get());

        // Jailing the subject if we need to.
        if (this.handler.shouldJailOnNextLogin(user)) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContexts.IS_JAILING_ACTION, true);
                // only set previous location if the player hasn't been moved to the jail before.
                if (event.getFrom().equals(owl.get().getTransform().get())) {
                    jd.setPreviousLocation(event.getFrom().getLocation());
                }

                this.handler.updateJailData(user, jd);
                qs.set(FlyKeys.FLY_TOGGLE, false);
            }
        }
    }

    /**
     * At the time the subject joins, check to see if the subject is jailed.
     *
     * @param event The event.
     */
    @Listener(order = Order.LATE)
    public void onPlayerJoin(final ClientConnectionEvent.Join event) {
        final Player user = event.getTargetEntity();

        // Jailing the subject if we need to.
        Optional<JailData> data = this.handler.getPlayerJailDataInternal(user);
        if (this.handler.shouldJailOnNextLogin(user) && data.isPresent()) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContexts.IS_JAILING_ACTION, true);
                // It exists.
                NamedLocation owl = this.handler.getWarpLocation(user).get();
                JailData jd = data.get();
                Optional<Duration> timeLeft = jd.getRemainingTime();
                Text message = timeLeft.map(duration ->
                        this.messageProviderService.getMessageFor(
                                user.getLocale(),
                                "command.jail.jailedfor",
                                owl.getName(),
                                this.playerDisplayNameService.getDisplayName(jd.getJailerInternal()),
                                this.messageProviderService.getTimeString(user.getLocale(), duration.getSeconds()))
                )
                        .orElseGet(() -> this.messageProviderService.getMessageFor(user, "command.jail.jailedperm", owl.getName(),
                                this.playerDisplayNameService.getDisplayName(jd.getJailerInternal()), "", ""));

                user.sendMessage(message);
                this.messageProviderService.sendMessageTo(user, "standard.reasoncoloured", jd.getReason());
            }
        }

        this.handler.setJailOnNextLogin(user, false);

        // Kick off a scheduled task to do jail time checks.
        Sponge.getScheduler().createTaskBuilder().async().delay(500, TimeUnit.MILLISECONDS).execute(() -> {
            Optional<JailData> omd = this.handler.getPlayerJailDataInternal(user);
            if (omd.isPresent()) {
                JailData md = omd.get();
                md.nextLoginToTimestamp();

                if (md.expired()) {
                    // free.
                    this.handler.unjailPlayer(user);
                } else {
                    // ensure jailing is current
                    this.handler.onJail(md, event.getTargetEntity());
                }
            }
        }).submit(this.pluginContainer);
    }

    @Listener
    public void onRequestSent(NucleusTeleportEvent.Request event, @Root Player cause, @Getter("getTargetUser") User user) {
        if (this.handler.isPlayerJailed(cause)) {
            event.setCancelled(true);
            event.setCancelMessage(this.messageProviderService.getMessageFor(cause.getLocale(), "jail.teleportcause.isjailed"));
        } else if (this.handler.isPlayerJailed(user)) {
            event.setCancelled(true);
            event.setCancelMessage(this.messageProviderService.getMessageFor(cause.getLocale(),"jail.teleporttarget.isjailed", user.getName()));
        }
    }

    @Listener
    public void onAboutToTeleport(NucleusTeleportEvent.AboutToTeleport event, @Root CommandSource cause, @Getter("getTargetEntity") Player player) {
        if (event.getCause().getContext().get(EventContexts.IS_JAILING_ACTION).orElse(false)) {
            if (this.handler.isPlayerJailed(player)) {
                if (!this.permissionService.hasPermission(cause, JailPermissions.JAIL_TELEPORTJAILED)) {
                    event.setCancelled(true);
                    event.setCancelMessage(
                            this.messageProviderService.getMessageFor(cause, "jail.abouttoteleporttarget.isjailed", player.getName()));
                } else if (!this.permissionService.hasPermission(cause, JailPermissions.JAIL_TELEPORTTOJAILED)) {
                    event.setCancelled(true);
                    event.setCancelMessage(
                            this.messageProviderService.getMessageFor(cause,"jail.abouttoteleportcause.targetisjailed", player.getName()));
                }
            }
        }
    }

    @Listener
    public void onCommand(SendCommandEvent event, @Root Player player) {
        // Only if the command is not in the control list.
        if (this.handler.checkJail(player, false) && this.allowedCommands.stream().noneMatch(x -> event.getCommand().equalsIgnoreCase(x))) {
            event.setCancelled(true);

            // This is the easiest way to send the messages.
            this.handler.checkJail(player, true);
        }
    }

    @Listener
    public void onBlockChange(ChangeBlockEvent event, @Root Player player) {
        event.setCancelled(this.handler.checkJail(player, true));
    }

    @Listener
    public void onInteract(InteractEvent event, @Root Player player) {
        event.setCancelled(this.handler.checkJail(player, true));
    }

    @Listener
    public void onSpawn(RespawnPlayerEvent event) {
        if (this.handler.checkJail(event.getTargetEntity(), false)) {
            event.setToTransform(event.getToTransform().setLocation(this.handler.getWarpLocation(event.getTargetEntity()).get().getLocation().get()));
        }
    }

    @Listener
    public void onSendToSpawn(NucleusSendToSpawnEvent event, @Getter("getTargetUser") User user) {
        if (this.handler.checkJail(user, false)) {
            event.setCancelled(true);
            event.setCancelReason(this.messageProviderService.getMessageString(event.getCause().first(CommandSource.class)
                    .orElseGet(Sponge.getServer()::getConsole), "jail.isjailed"));
        }
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        this.allowedCommands = serviceCollection.moduleDataProvider().getModuleConfig(JailConfig.class).getAllowedCommands();
    }
}
