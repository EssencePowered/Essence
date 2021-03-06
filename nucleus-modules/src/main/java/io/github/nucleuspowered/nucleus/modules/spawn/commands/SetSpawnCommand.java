/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import io.github.nucleuspowered.nucleus.modules.spawn.SpawnKeys;
import io.github.nucleuspowered.nucleus.modules.spawn.SpawnPermissions;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.EssentialsEquivalent;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.math.vector.Vector3d;

@EssentialsEquivalent("setspawn")
@Command(
        aliases = { "setspawn" },
        basePermission = SpawnPermissions.BASE_SETSPAWN,
        commandDescriptionKey = "setspawn"
)
public class SetSpawnCommand implements ICommandExecutor {

    @Override public ICommandResult execute(final ICommandContext context) throws CommandException {
        final ServerPlayer src = context.requirePlayer();
        // Minecraft does not set the rotation of the player at the spawn point, so we'll do it for them!
        final Vector3d rotation = src.rotation();
        context.getServiceCollection()
                .storageManager()
                .getWorldService()
                .getOrNew(src.world().key())
                .thenAccept(x -> x.set(SpawnKeys.WORLD_SPAWN_ROTATION, rotation));

        src.world().properties().setSpawnPosition(src.location().blockPosition());
        context.sendMessage("command.setspawn.success", src.world().key().asString());
        return context.successResult();
    }
}
