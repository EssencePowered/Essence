/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.modules.world.WorldPermissions;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.CatalogTypes;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.storage.WorldProperties;

@Command(
        aliases = {"setdifficulty", "difficulty"},
        basePermission = WorldPermissions.BASE_WORLD_SETDIFFICULTY,
        commandDescriptionKey = "world.setdifficulty",
        parentCommand = WorldCommand.class
)
public class SetDifficultyWorldCommand implements ICommandExecutor {

    private final String difficulty = "difficulty";

    @Override
    public Parameter[] parameters(final INucleusServiceCollection serviceCollection) {
        return new Parameter[] {
                GenericArguments.onlyOne(new ImprovedCatalogTypeArgument(Text.of(this.difficulty), CatalogTypes.DIFFICULTY, serviceCollection)),
                NucleusParameters.OPTIONAL_WORLD_PROPERTIES_ALL.get(serviceCollection)
        };
    }

    @Override public ICommandResult execute(final ICommandContext context) throws CommandException {
        final Difficulty difficultyInput = context.requireOne(this.difficulty, Difficulty.class);
        final WorldProperties worldProperties = context.getWorldPropertiesOrFromSelfOptional(NucleusParameters.Keys.WORLD)
                        .orElseThrow(() -> context.createException("command.world.player"));

        worldProperties.setDifficulty(difficultyInput);
        context.sendMessage("command.world.setdifficulty.success",
                worldProperties.getWorldName(),
                Util.getTranslatableIfPresent(difficultyInput));

        return context.successResult();
    }
}