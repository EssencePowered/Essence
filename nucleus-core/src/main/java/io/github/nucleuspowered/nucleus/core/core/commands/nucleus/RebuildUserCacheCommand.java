/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.core.core.commands.nucleus;

import io.github.nucleuspowered.nucleus.core.core.CorePermissions;
import io.github.nucleuspowered.nucleus.core.core.commands.NucleusCommand;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.Command;

@Command(
        aliases = "rebuildusercache",
        basePermission = CorePermissions.BASE_NUCLEUS_REBUILDUSERCACHE,
        commandDescriptionKey = "nucleus.rebuildusercache",
        parentCommand = NucleusCommand.class
)
public class RebuildUserCacheCommand implements ICommandExecutor {

    @Override
    public ICommandResult execute(final ICommandContext context) {
        context.sendMessage("command.nucleus.rebuild.start");
        if (context.getServiceCollection().userCacheService().fileWalk()) {
            context.sendMessage("command.nucleus.rebuild.end");
            return context.successResult();
        } else {
            return context.errorResult("command.nucleus.rebuild.fail");
        }
    }
}
