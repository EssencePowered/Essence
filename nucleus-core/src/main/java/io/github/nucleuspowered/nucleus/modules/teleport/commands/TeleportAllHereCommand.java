/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.api.teleport.data.TeleportScanners;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.events.CommandEvent;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

@NonnullByDefault
@EssentialsEquivalent("tpall")
@Command(aliases = {"tpall", "tpallhere"}, basePermission = TeleportPermissions.BASE_TPALL, commandDescriptionKey = "tpall")
public class TeleportAllHereCommand implements ICommandExecutor<Player> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[]{
                GenericArguments.flags().flag("f").buildWith(GenericArguments.none())
        };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        MessageChannel.TO_ALL.getMembers()
                .forEach(x -> context.sendMessageTo(x, "command.tpall.broadcast", context.getName()));
        Player player = context.getIfPlayer();
        Transform<World> toTransform = player.getTransform();
        for (Player x : Sponge.getServer().getOnlinePlayers()) {
            if (!context.is(x)) {
                CommandEvent.UserToCause event = new CommandEvent.UserToCause(context.getCause(), x.getUniqueId(), player.getUniqueId());
                if (Sponge.getEventManager().post(event)) {
                    if (event.getCancelMessage().isPresent()) {
                        return context.errorResultLiteral(event.getCancelMessage().get());
                    } else {
                        return context.errorResult("command.tp.eventfailed");
                    }
                }
                context.getServiceCollection()
                        .teleportService()
                        .teleportPlayerSmart(x,
                                toTransform,
                                false,
                                !context.getOne("f", Boolean.class).orElse(false),
                                TeleportScanners.NO_SCAN.get()
                        );
            }
        }
        return context.successResult();
    }
}
