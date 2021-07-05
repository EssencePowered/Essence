/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.events.RequestEvent;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.interfaces.IPlayerTeleporterService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.stream.Collectors;

@NonnullByDefault
@EssentialsEquivalent({"tpaall"})
@Command(aliases = {"tpaall", "tpaskall"}, basePermission = TeleportPermissions.BASE_TPAALL, commandDescriptionKey = "tpaall")
public class TeleportAskAllHereCommand implements ICommandExecutor<Player> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                GenericArguments.flags().flag("f").buildWith(GenericArguments.none())
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        List<Player> cancelled = Lists.newArrayList();
        IPlayerTeleporterService playerTeleporterService = context
                .getServiceCollection()
                .teleporterService();
        for (Player x : Sponge.getServer().getOnlinePlayers()) {
            if (context.is(x)) {
                continue;
            }

            // Before we do all this, check the event.
            RequestEvent.PlayerToCause event = new RequestEvent.PlayerToCause(Sponge.getCauseStackManager().getCurrentCause(), x);
            if (Sponge.getEventManager().post(event)) {
                cancelled.add(x);
                continue;
            }

            playerTeleporterService.requestTeleport(
                    context.getIfPlayer(),
                    x,
                    0,
                    0,
                    x,
                    context.getIfPlayer(),
                    !context.getOne("f", Boolean.class).orElse(false),
                    false,
                    true,
                    p -> {},
                    "command.tpahere.question"
            );
        }

        context.sendMessage("command.tpaall.success");
        if (!cancelled.isEmpty()) {
            context.sendMessage("command.tpall.cancelled",
                    cancelled.stream().map(User::getName).collect(Collectors.joining(", ")));
        }

        return context.successResult();
    }
}
