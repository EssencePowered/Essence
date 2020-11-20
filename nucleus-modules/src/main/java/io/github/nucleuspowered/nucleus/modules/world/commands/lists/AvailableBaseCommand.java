/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.lists;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AvailableBaseCommand implements ICommandExecutor {

    private final Class<? extends CatalogType> catalogType;
    private final String titleKey;

    AvailableBaseCommand(final Class<? extends CatalogType> catalogType, final String titleKey) {
        this.catalogType = catalogType;
        this.titleKey = titleKey;
    }

    @Override public ICommandResult execute(final ICommandContext context) throws CommandException {

        final List<Text> types = Sponge.getRegistry().getAllOf(this.catalogType).stream()
                .map(x -> context.getMessage("command.world.presets.item", x.getId(), x.getName()))
                .collect(Collectors.toList());

        Util.getPaginationBuilder(context.getCommandSourceRoot())
                .title(context.getMessage(this.titleKey))
                .contents(types)
                .sendTo(context.getCommandSourceRoot());

        return context.successResult();
    }
}