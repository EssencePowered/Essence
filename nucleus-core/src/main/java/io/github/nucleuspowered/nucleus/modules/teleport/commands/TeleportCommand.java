/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.api.teleport.data.TeleportResult;
import io.github.nucleuspowered.nucleus.api.teleport.data.TeleportScanners;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfig;
import io.github.nucleuspowered.nucleus.modules.teleport.events.CommandEvent;
import io.github.nucleuspowered.nucleus.modules.teleport.services.PlayerTeleporterService;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.scaffold.command.modifier.CommandModifiers;
import io.github.nucleuspowered.nucleus.scaffold.command.parameter.AlternativeUsageArgument;
import io.github.nucleuspowered.nucleus.scaffold.command.parameter.DisplayNameArgument;
import io.github.nucleuspowered.nucleus.scaffold.command.parameter.IfConditionElseArgument;
import io.github.nucleuspowered.nucleus.scaffold.command.parameter.SelectorArgument;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.interfaces.IPermissionService;
import io.github.nucleuspowered.nucleus.services.interfaces.IReloadableService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.function.Supplier;

@EssentialsEquivalent(value = {"tp", "tele", "tp2p", "teleport", "tpo"}, isExact = false,
                      notes = "If you have permission, this will override '/tptoggle' automatically.")
@NonnullByDefault
@Command(
        aliases = {"teleport", "tele", "$tp"},
        basePermission = TeleportPermissions.BASE_TELEPORT,
        commandDescriptionKey = "teleport",
        modifiers = {
                @CommandModifier(
                        value = CommandModifiers.HAS_WARMUP,
                        exemptPermission = TeleportPermissions.EXEMPT_WARMUP_TELEPORT
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COOLDOWN,
                        exemptPermission = TeleportPermissions.EXEMPT_COOLDOWN_TELEPORT
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COST,
                        exemptPermission = TeleportPermissions.EXEMPT_COST_TELEPORT
                )
        },
        associatedPermissions = {
                TeleportPermissions.TELEPORT_OFFLINE,
                TeleportPermissions.TELEPORT_QUIET,
                TeleportPermissions.OTHERS_TELEPORT,
                TeleportPermissions.TPTOGGLE_EXEMPT
        }
)
public class TeleportCommand implements ICommandExecutor<CommandSource>, IReloadableService.Reloadable {

    private final String playerToKey = "Player to warp to";
    private final String quietKey = "quiet";

    private boolean isDefaultQuiet = false;

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        this.isDefaultQuiet = serviceCollection.moduleDataProvider().getModuleConfig(TeleportConfig.class).isDefaultQuiet();
    }

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[]{
                GenericArguments.flags().flag("f")
                        .setAnchorFlags(true)
                        .valueFlag(
                                serviceCollection.commandElementSupplier()
                                        .createPermissionParameter(
                                                GenericArguments.bool(Text.of(this.quietKey)), TeleportPermissions.TELEPORT_QUIET, false), "q")
                        .buildWith(GenericArguments.none()),

                new AlternativeUsageArgument(
                        GenericArguments.seq(
                                IfConditionElseArgument.permission(
                                        serviceCollection.permissionService(),
                                        TeleportPermissions.TELEPORT_OFFLINE,
                                        NucleusParameters.ONE_USER_PLAYER_KEY.get(serviceCollection),
                                        NucleusParameters.ONE_PLAYER.get(serviceCollection)),

                                new IfConditionElseArgument(
                                        serviceCollection.permissionService(),
                                        GenericArguments.optionalWeak(
                                                new SelectorArgument(
                                                        new DisplayNameArgument(Text.of(this.playerToKey), DisplayNameArgument.Target.USER,
                                                                serviceCollection),
                                                        Player.class,
                                                        serviceCollection
                                                )
                                        ),
                                        GenericArguments.none(),
                                        this::testForSecondPlayer)),

                        src -> {
                            StringBuilder sb = new StringBuilder();
                            sb.append("<player to warp to>");
                            if (serviceCollection.permissionService().hasPermission(src, TeleportPermissions.OTHERS_TELEPORT)) {
                                sb.append("|<player to warp> <player to warp to>");
                            }

                            if (serviceCollection.permissionService().hasPermission(src, TeleportPermissions.TELEPORT_OFFLINE)) {
                                sb.append("|<offline player to warp to>");
                            }

                            return Text.of(sb.toString());
                        }
                )
        };
    }

    private boolean testForSecondPlayer(IPermissionService permissionService, CommandSource source, CommandContext context) {
        try {
            if (context.hasAny(NucleusParameters.Keys.PLAYER) && permissionService.hasPermission(source, TeleportPermissions.OTHERS_TELEPORT)) {
                return context.<User>getOne(NucleusParameters.Keys.PLAYER).map(y -> y.getPlayer().isPresent()).orElse(false);
            }
        } catch (Exception e) {
            // ignored
        }

        return false;
    }

    @Override
    public Optional<ICommandResult> preExecute(ICommandContext.Mutable<? extends CommandSource> context) throws CommandException {
        return context.getServiceCollection()
                .teleporterService()
                .canTeleportTo(context.getIfPlayer(), context.requireOne(NucleusParameters.Keys.PLAYER, Player.class)) ?
                Optional.empty() :
                Optional.of(context.failResult());
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        boolean beQuiet = context.getOne(this.quietKey, Boolean.class).orElse(this.isDefaultQuiet);
        Optional<Player> oTo = context.getOne(this.playerToKey, Player.class);
        User to;
        User fromUser;
        Player from;
        if (oTo.isPresent()) { // Two player argument.
            fromUser = context.requireOne(NucleusParameters.Keys.PLAYER, User.class);
            from = fromUser.getPlayer().orElse(null);
            to = oTo.get();
            if (context.is(to)) {
                return context.errorResult("command.teleport.player.noself");
            }
        } else if (context.is(Player.class)) {
            from = context.getIfPlayer();
            fromUser = from;
            to = context.requireOne(NucleusParameters.Keys.PLAYER, Player.class);
        } else {
            return context.errorResult("command.playeronly");
        }

        CommandEvent.CauseToUser event = new CommandEvent.CauseToUser(context.getCause(), fromUser.getUniqueId(), to.getUniqueId());
        if (Sponge.getEventManager().post(event)) {
            if (event.getCancelMessage().isPresent()) {
                return context.errorResultLiteral(event.getCancelMessage().get());
            } else {
                return context.errorResult("command.tp.eventfailed");
            }
        }

        if (from != null && to.getPlayer().isPresent()) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(context.getIfPlayer());
                TeleportResult result =
                        context.getServiceCollection()
                                .teleporterService()
                                .teleportWithMessage(
                                        context.getIfPlayer(),
                                        from,
                                        to.getPlayer().get(),
                                        !context.hasAny("f"),
                                        false,
                                        beQuiet
                                );
                return result.isSuccessful() ? context.successResult() : context.failResult();
            }
        }

        // We have an offline player.
        if (!context.testPermission(TeleportPermissions.TELEPORT_OFFLINE)) {
            return context.errorResult("command.teleport.noofflineperms");
        }

        // Can we get a location?
        Supplier<CommandException> r = () -> context.createException("command.teleport.nolastknown", to.getName());

        if (from == null) {
            if (fromUser.setLocation(to.getPosition(), to.getUniqueId())) {
                context.sendMessage("command.teleport.offline.other", fromUser.getName(), to.getName());
                return context.successResult();
            }
        } else {
            World w = to.getWorldUniqueId().flatMap(x -> Sponge.getServer().getWorld(x)).orElseThrow(r);
            Location<World> l = new Location<>(
                    w,
                    to.getPosition()
            );

            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(context.getIfPlayer());
                boolean result = context.getServiceCollection()
                        .teleportService()
                        .teleportPlayerSmart(
                                from,
                                l,
                                false,
                                !context.hasAny("f"),
                                TeleportScanners.NO_SCAN.get()
                        ).isSuccessful();
                if (result) {
                    if (!context.is(from)) {
                        context.sendMessage("command.teleport.offline.other", from.getName(), to.getName());
                    }

                    context.sendMessage("command.teleport.offline.self", to.getName());
                    return context.successResult();
                }
            }
        }

        return context.errorResult("command.teleport.error");
    }

}
