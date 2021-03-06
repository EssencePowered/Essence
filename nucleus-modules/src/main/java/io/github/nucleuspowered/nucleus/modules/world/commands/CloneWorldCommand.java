/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.modules.world.WorldPermissions;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.core.scaffold.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.core.services.INucleusServiceCollection;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.UUID;
import java.util.function.Supplier;

@Command(
        aliases = {"clone", "copy"},
        basePermission = WorldPermissions.BASE_WORLD_CLONE,
        commandDescriptionKey = "world.clone",
        parentCommand = WorldCommand.class
)
public class CloneWorldCommand implements ICommandExecutor {

    private final Parameter.Value<String> newNameParameter = Parameter.string()
            .key("new name")
            .build();

    @Override public Parameter[] parameters(final INucleusServiceCollection serviceCollection) {
        return new Parameter[] {
                NucleusParameters.ONLINE_WORLD,
                this.newNameParameter
        };
    }

    @Override public ICommandResult execute(final ICommandContext context) throws CommandException {
        final ServerWorld worldToCopy = context.requireOne(NucleusParameters.ONLINE_WORLD);
        final ResourceKey oldName = worldToCopy.key();
        final ResourceKey newName = ResourceKey.of(context.getServiceCollection().pluginContainer(), context.requireOne(this.newNameParameter));
        if (Sponge.server().worldManager().world(newName).isPresent()) {
            return context.errorResult("command.world.clone.alreadyexists", newName.asString());
        }

        context.sendMessage("command.world.clone.starting", oldName.asString(), newName.asString());
        if (!context.is(SystemSubject.class)) {
            context.sendMessageTo(Sponge.systemSubject(), "command.world.clone.starting", oldName, newName);
        }

        // Well, you never know, the player might die or disconnect - we have to be vigilant.
        final Supplier<Audience> mr;
        if (context.audience() instanceof ServerPlayer) {
            final UUID uuid = ((ServerPlayer) context.audience()).uniqueId();
            mr = () -> Sponge.server().player(uuid).map(x -> (Audience) x).orElseGet(Audience::empty);
        } else {
            mr = context::audience;
        }

        Sponge.server().worldManager().copyWorld(oldName, newName).handle((result, ex) -> {

            final Audience m = mr.get();
            if (ex == null && result != null) {
                context.sendMessage("command.world.clone.success", oldName, newName);
                if (!(m instanceof SystemSubject)) {
                    context.sendMessageTo(Sponge.systemSubject(), "command.world.clone.success", oldName, newName);
                }
            } else {
                context.sendMessage("command.world.clone.failed", oldName, newName);
                if (!(m instanceof SystemSubject)) {
                    context.sendMessageTo(Sponge.systemSubject(), "command.world.clone.failed", oldName, newName);
                }
            }

            return result;
        });

        return context.successResult();
    }
}
