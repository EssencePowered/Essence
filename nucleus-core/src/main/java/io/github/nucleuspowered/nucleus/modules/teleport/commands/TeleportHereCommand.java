/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.api.teleport.data.TeleportResult;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfig;
import io.github.nucleuspowered.nucleus.modules.teleport.events.CommandEvent;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.scaffold.command.parameter.IfConditionElseArgument;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.interfaces.IPlayerTeleporterService;
import io.github.nucleuspowered.nucleus.services.interfaces.IReloadableService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * NOTE: TeleportHere is considered an admin command, as there is a potential for abuse for non-admin players trying to pull players. No cost or
 * warmups will be applied. /tpahere should be used instead in these circumstances.
 */
@EssentialsEquivalent(value = {"tphere", "s", "tpohere"}, isExact = false,
                      notes = "If you have permission, this will override '/tptoggle' automatically.")
@NonnullByDefault
@Command(
        aliases = {"tphere", "tph"},
        basePermission = TeleportPermissions.BASE_TPHERE,
        commandDescriptionKey = "tphere",
        associatedPermissions = {
                TeleportPermissions.TPHERE_OFFLINE,
                TeleportPermissions.TPTOGGLE_EXEMPT
        }
)
public class TeleportHereCommand implements ICommandExecutor<Player>, IReloadableService.Reloadable {

    private boolean isDefaultQuiet = false;

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        this.isDefaultQuiet =
                serviceCollection.moduleDataProvider()
                        .getModuleConfig(TeleportConfig.class)
                        .isDefaultQuiet();
    }

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[]{
                GenericArguments.flags().flag("q", "-quiet").buildWith(
                        IfConditionElseArgument.permission(
                                serviceCollection.permissionService(),
                                TeleportPermissions.TPHERE_OFFLINE,
                                NucleusParameters.ONE_USER_PLAYER_KEY.get(serviceCollection),
                                NucleusParameters.ONE_PLAYER.get(serviceCollection)))
        };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        boolean beQuiet = context.getOne("q", Boolean.class).orElse(this.isDefaultQuiet);
        User target = context.requireOne(NucleusParameters.Keys.PLAYER, User.class);
        IPlayerTeleporterService sts = context.getServiceCollection().teleporterService();

        CommandEvent.UserToCause event = new CommandEvent.UserToCause(context.getCause(), context.getIfPlayer().getUniqueId(), target.getUniqueId());
        if (Sponge.getEventManager().post(event)) {
            if (event.getCancelMessage().isPresent()) {
                return context.errorResultLiteral(event.getCancelMessage().get());
            } else {
                return context.errorResult("command.tp.eventfailed");
            }
        }

        if (target.getPlayer().isPresent()) {
            Player to = target.getPlayer().get();
            TeleportResult result = sts.teleportWithMessage(
                    context.getIfPlayer(),
                    to,
                    context.getIfPlayer(),
                    false,
                    false,
                    beQuiet
            );
            return result.isSuccessful() ? context.successResult() : context.failResult();
        } else {
            if (!context.testPermission(TeleportPermissions.TPHERE_OFFLINE)) {
                return context.errorResult("command.tphere.noofflineperms");
            }

            Player src = context.getIfPlayer();
            // Update the offline player's next location
            target.setLocation(src.getPosition(), src.getWorld().getUniqueId());
            context.sendMessage("command.tphere.offlinesuccess", target.getName());
        }

        return context.successResult();
    }
}
