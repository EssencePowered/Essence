/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.services;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Singleton;
import io.github.nucleuspowered.nucleus.api.module.teleport.data.TeleportRequest;
import io.github.nucleuspowered.nucleus.api.teleport.data.TeleportResult;
import io.github.nucleuspowered.nucleus.api.teleport.data.TeleportScanners;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfig;
import io.github.nucleuspowered.nucleus.modules.teleport.events.TeleportResponseEvent;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.NucleusKeysProvider;
import io.github.nucleuspowered.nucleus.services.interfaces.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.interfaces.INucleusTeleportService;
import io.github.nucleuspowered.nucleus.services.interfaces.IPermissionService;
import io.github.nucleuspowered.nucleus.services.interfaces.IPlayerTeleporterService;
import io.github.nucleuspowered.nucleus.services.interfaces.IReloadableService;
import io.github.nucleuspowered.nucleus.services.interfaces.IUserPreferenceService;
import io.github.nucleuspowered.nucleus.services.interfaces.IWarmupService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.World;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.inject.Inject;

@Singleton
public class PlayerTeleporterService implements IPlayerTeleporterService, IReloadableService.Reloadable {

    private boolean showAcceptDeny = true;

    private final INucleusServiceCollection serviceCollection;
    private final INucleusTeleportService safeTeleportService;
    private final IMessageProviderService messageProviderService;
    private final IUserPreferenceService userPreferenceService;
    private final IPermissionService permissionService;
    private final IWarmupService warmupService;

    private boolean refundOnDeny = false;
    private boolean useRequestLocation = true;
    private boolean useCommandsOnClickAcceptDeny = false;
    private boolean isOnlySameDimension = false;

    @Inject
    public PlayerTeleporterService(
        PluginContainer pluginContainer,
        IReloadableService reloadableService,
        INucleusServiceCollection serviceCollection
    ) {
        this.safeTeleportService = serviceCollection.teleportService();
        this.messageProviderService = serviceCollection.messageProvider();
        this.userPreferenceService = serviceCollection.userPreferenceService();
        this.permissionService = serviceCollection.permissionService();
        this.warmupService = serviceCollection.warmupService();
        this.serviceCollection = serviceCollection;

        Sponge.getServiceManager().setProvider(pluginContainer, IPlayerTeleporterService.class, this);
        reloadableService.registerReloadable(this);
    }

    @Override
    public boolean canTeleportTo(User source, User to) {
        if (!canBypassTpToggle(source)) {
            if (!this.userPreferenceService.get(to.getUniqueId(), NucleusKeysProvider.TELEPORT_TARGETABLE).orElse(true)) {
                if (source instanceof MessageReceiver) {
                    this.messageProviderService.sendMessageTo((MessageReceiver) source, "teleport.fail.targettoggle", to.getName());
                }
                return false;
            }
        }

        if (this.isOnlySameDimension && source.getWorldUniqueId().isPresent()) {
            if (!to.getWorldUniqueId().map(x -> source.getWorldUniqueId().get().equals(x)).orElse(false)) {
                if (!this.permissionService.hasPermission(source, "nucleus.teleport.exempt.samedimension")) {
                    if (source instanceof MessageReceiver) {
                        this.messageProviderService.sendMessageTo((MessageReceiver) source, "teleport.fail.samedimension", to.getName());
                    }
                    return false;
                }
            }
        }

        return true;
    }

    private boolean canBypassTpToggle(Subject from) {
        return this.permissionService.hasPermission(from, TeleportPermissions.TPTOGGLE_EXEMPT);
    }

    private final Map<UUID, TeleportRequest> activeTeleportRequestsCommand = new HashMap<>();
    private final Multimap<UUID, TeleportRequest> activeTeleportRequests = HashMultimap.create();

    public TeleportResult teleportWithMessage(
        CommandSource source,
        Player playerToTeleport,
        Player target,
        boolean safe,
        boolean quietSource,
        boolean quietTarget) {

        TeleportResult result =
            this.safeTeleportService.teleportPlayerSmart(
                playerToTeleport,
                target.getTransform(),
                false,
                safe,
                TeleportScanners.NO_SCAN.get()
            );
        if (result.isSuccessful()) {
            if (!source.equals(target) && !quietSource) {
                this.messageProviderService.sendMessageTo(source, "teleport.success.source",
                    playerToTeleport.getName(),
                    target.getName());
            }

            this.messageProviderService.sendMessageTo(playerToTeleport, "teleport.to.success", target.getName());
            if (!quietTarget) {
                this.messageProviderService.sendMessageTo(target, "teleport.from.success", playerToTeleport.getName());
            }
        } else if (!quietSource) {
            this.messageProviderService.sendMessageTo(source, result == TeleportResult.FAIL_NO_LOCATION ? "teleport.nosafe" : "teleport.cancelled");
        }

        return result;
    }

    @Override
    public boolean requestTeleport(
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
        String messageKey) {
        removeExpired();

        if (canTeleportTo(playerToTeleport, target)) {
            CommandSource src = requester == null ? Sponge.getServer().getConsole() : requester;

            Transform<World> transform = null;
            if (target.getWorldUniqueId().isPresent()) {
                Optional<World> world = Sponge.getServer().getWorld(target.getWorldUniqueId().get());
                if (world.isPresent()) {
                    transform = new Transform<>(world.get(), target.getPosition());
                }
            }

            TeleportRequestData request = new TeleportRequestData(
                this.serviceCollection,
                playerToTeleport.getUniqueId(),
                target.getUniqueId(),
                Instant.now().plus(30, ChronoUnit.SECONDS),
                this.refundOnDeny ? cost : 0,
                warmup,
                requester == null ? null : requester.getUniqueId(),
                safe,
                silentTarget,
                silentSource,
                this.useRequestLocation ? transform : null,
                successCallback
            );
            /*TeleportRequest request = new TeleportRequest(
                    playerToTeleport.getUniqueId(),
                    target.getUniqueId(),
                    cost,
                    Instant.now().plus(30, ChronoUnit.SECONDS),
                    this.refundOnDeny ? cost : 0,
                    warmup,
                    requester == null ? null : requester.getUniqueId(),
                    safe,
                    silentTarget,
                    silentSource,
                    this.useRequestLocation ? target.getTransform() : null,
                    successCallback);*/
            this.activeTeleportRequestsCommand.put(toRequest.getUniqueId(), request);
            this.activeTeleportRequests.put(toRequest.getUniqueId(), request);
            if (toRequest.getPlayer().isPresent()) {
                Player player = toRequest.getPlayer().get();
                this.messageProviderService.sendMessageTo(player, messageKey, src.getName());
                getAcceptDenyMessage(player, request).ifPresent(player::sendMessage);
            }

            if (!silentSource) {
                this.messageProviderService.sendMessageTo(src, "command.tpask.sent", toRequest.getName());
            }
            return true;
        }

        return false;
    }

    /**
     * Gets the request associated with the tp accept.
     *
     * @return The request, if any.
     */
    public Optional<TeleportRequest> getCurrentRequest(Player player) {
        return Optional.ofNullable(this.activeTeleportRequestsCommand.get(player.getUniqueId()));
    }

    /**
     * Removes any outstanding requests for the specified player.
     *
     * @param player The player
     */
    public void removeAllRequests(Player player) {
        this.activeTeleportRequests.removeAll(player.getUniqueId()).forEach(x -> x.forceExpire(true));
        this.activeTeleportRequestsCommand.remove(player.getUniqueId());
    }

    public void removeExpired() {
        this.activeTeleportRequests.values().removeIf(x -> !x.isActive());
        this.activeTeleportRequestsCommand.values().removeIf(x -> !x.isActive());
    }

    private Optional<Text> getAcceptDenyMessage(Player forPlayer, TeleportRequestData target) {
        if (this.showAcceptDeny) {
            Text acceptText = this.messageProviderService.getMessageFor(forPlayer.getLocale(), "standard.accept")
                .toBuilder()
                .style(TextStyles.UNDERLINE)
                .onHover(TextActions.showText(
                    this.messageProviderService.getMessageFor(forPlayer.getLocale(), "teleport.accept.hover")))
                .onClick(TextActions.executeCallback(src -> {
                    if (!target.isActive() || !this.permissionService.hasPermission(src, TeleportPermissions.BASE_TPACCEPT)
                        || !(src instanceof Player)) {
                        this.messageProviderService.sendMessageTo(src, "command.tpaccept.nothing");
                        return;
                    }
                    if (this.useCommandsOnClickAcceptDeny) {
                        Sponge.getCommandManager().process(src, "nucleus:tpaccept");
                    } else {
                        accept((Player) src, target);
                    }
                })).build();
            Text denyText = this.messageProviderService.getMessageFor(forPlayer.getLocale(), "standard.deny")
                .toBuilder()
                .style(TextStyles.UNDERLINE)
                .onHover(TextActions.showText(this.messageProviderService.getMessageFor(forPlayer.getLocale(), "teleport.deny.hover")))
                .onClick(TextActions.executeCallback(src -> {
                    if (!target.isActive() || !this.permissionService.hasPermission(src, TeleportPermissions.BASE_TPDENY)
                        || !(src instanceof Player)) {
                        this.messageProviderService.sendMessageTo(src, "command.tpdeny.fail");
                        return;
                    }
                    if (this.useCommandsOnClickAcceptDeny) {
                        Sponge.getCommandManager().process(src, "nucleus:tpdeny");
                    } else {
                        deny((Player) src, target);
                    }
                })).build();

            return Optional.of(Text.builder()
                .append(acceptText)
                .append(Text.of(" - "))
                .append(denyText).build());
        }

        return Optional.empty();
    }

    public boolean accept(Player player) {
        return accept(player, (TeleportRequestData) getCurrentRequest(player).orElse(null));
    }

    private boolean accept(Player player, @Nullable TeleportRequestData target) {
        if (target == null) {
            this.messageProviderService.sendMessageTo(player, "command.tpaccept.nothing");
            return false;
        }

        if (!target.isActive()) {
            this.messageProviderService.sendMessageTo(player, "command.tpaccept.expired");
            return false;
        }

        this.activeTeleportRequests.values().remove(target);
        this.activeTeleportRequestsCommand.values().remove(target);
        target.forceExpire(false);

        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            TeleportResponseEvent.Accept event = new TeleportResponseEvent.Accept(frame.getCurrentCause(), target.toTeleport, target.target);
            if (Sponge.getEventManager().post(event)) {
                if (event.getCancelMessage().isPresent()) {
                    this.messageProviderService.sendMessageTo(player, "command.tpaccept.expired");
                } else {
                    this.messageProviderService.sendMessageTo(player, "command.tpa.eventfailed");
                }
                return false;
            }
        }

        Optional<Player> playerToTeleport = target.getToBeTeleported();
        if (!playerToTeleport.isPresent()) {
            // error
            this.messageProviderService.sendMessageTo(player, "command.tpaccept.noplayer");
            return false;
        }
        if (target.warmup == 0) {
            target.run();
        } else {
            this.warmupService.executeAfter(
                playerToTeleport.get(),
                Duration.of(target.warmup, ChronoUnit.SECONDS),
                target::run,
                true);
        }
        this.messageProviderService.sendMessageTo(player, "command.tpaccept.success");
        return true;
    }

    public boolean deny(Player player) {
        return deny(player, (TeleportRequestData) getCurrentRequest(player).orElse(null));
    }

    private boolean deny(Player player, @Nullable TeleportRequestData target) {
        if (target == null) {
            this.messageProviderService.sendMessageTo(player, "command.tpaccept.nothing");
            return false;
        } else if (!target.isActive()) {
            this.messageProviderService.sendMessageTo(player, "command.tpaccept.expired");
            return false;
        }

        target.forceExpire(true);
        this.activeTeleportRequests.values().remove(target);
        this.activeTeleportRequestsCommand.values().remove(target);

        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            TeleportResponseEvent.Deny event = new TeleportResponseEvent.Deny(frame.getCurrentCause(), target.toTeleport, target.target);
            if (Sponge.getEventManager().post(event)) {
                if (event.getCancelMessage().isPresent()) {
                    this.messageProviderService.sendMessageTo(player, "command.tpaccept.expired");
                } else {
                    this.messageProviderService.sendMessageTo(player, "command.tpa.eventfailed");
                }
                return false;
            }
        }
        this.messageProviderService.sendMessageTo(player, "command.tpdeny.deny");
        return true;
    }

    static void onCancel(INucleusServiceCollection serviceCollection, UUID requester, UUID toTeleport, double cost) {
        final Text name = serviceCollection.playerDisplayNameService().getDisplayName(toTeleport);
        if (requester == null) {
            serviceCollection.messageProvider().sendMessageTo(Sponge.getServer().getConsole(), "command.tpdeny.denyrequester", name);
        } else {
            Optional<Player> op = Sponge.getServer().getPlayer(requester);
            op.ifPresent(x -> serviceCollection.messageProvider().sendMessageTo(x, "command.tpdeny.denyrequester", name));

            if (serviceCollection.economyServiceProvider().serviceExists() && cost > 0) {
                // refund the cost.
                op.ifPresent(x ->
                    serviceCollection.messageProvider().sendMessageTo(x,
                        "teleport.prep.cancel",
                        serviceCollection.economyServiceProvider().getCurrencySymbol(cost)));

                User user = op.map(x -> (User) x).orElseGet(() -> Sponge.getServiceManager()
                    .provideUnchecked(UserStorageService.class)
                    .get(requester).orElse(null));
                if (user != null) {
                    serviceCollection.economyServiceProvider().depositInPlayer(user, cost);
                }
            }
        }
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        TeleportConfig config = serviceCollection.moduleDataProvider().getModuleConfig(TeleportConfig.class);
        this.useCommandsOnClickAcceptDeny = config.isUseCommandsOnClickAcceptOrDeny();
        this.showAcceptDeny = config.isShowClickableAcceptDeny();
        this.refundOnDeny = config.isRefundOnDeny();
        this.useRequestLocation = config.isUseRequestLocation();
        this.isOnlySameDimension = config.isOnlySameDimension();
    }
}
