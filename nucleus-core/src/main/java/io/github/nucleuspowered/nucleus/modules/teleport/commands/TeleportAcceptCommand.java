/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.services.PlayerTeleporterService;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.EssentialsEquivalent;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@EssentialsEquivalent({"tpaccept", "tpyes"})
@Command(
        aliases = {"tpaccept", "teleportaccept", "tpyes"},
        basePermission = TeleportPermissions.BASE_TPACCEPT,
        commandDescriptionKey = "tpaccept"
)
public class TeleportAcceptCommand implements ICommandExecutor<Player> {

    @Override
    public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        return context.getResultFromBoolean(context.getServiceCollection()
                .teleporterService()
                .accept(context.getIfPlayer()));
    }

}
