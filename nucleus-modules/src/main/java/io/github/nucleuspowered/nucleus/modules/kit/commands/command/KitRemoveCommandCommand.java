/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.command;

import io.github.nucleuspowered.nucleus.api.module.kit.data.Kit;
import io.github.nucleuspowered.nucleus.modules.kit.KitPermissions;
import io.github.nucleuspowered.nucleus.modules.kit.parameters.KitParameter;
import io.github.nucleuspowered.nucleus.modules.kit.services.KitService;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.exception.CommandException;;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import java.util.List;

@Command(
        aliases = { "remove", "del", "-" },
        basePermission = KitPermissions.BASE_KIT_COMMAND_REMOVE,
        commandDescriptionKey = "kit.command.remove",
        async = true,
        parentCommand = KitCommandCommand.class
)
public class KitRemoveCommandCommand implements ICommandExecutor {

    private final String index = "index";

    @Override public CommandElement[] parameters(final INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.getServiceUnchecked(KitService.class).createKitElement(false),
                GenericArguments.firstParsing(new PositiveIntegerArgument(Text.of(this.index), serviceCollection), NucleusParameters.COMMAND)
        };
    }

    @Override public ICommandResult execute(final ICommandContext context) throws CommandException {
        final Kit kitInfo = context.requireOne(KitParameter.KIT_PARAMETER_KEY, Kit.class);
        final List<String> commands = kitInfo.getCommands();

        final String cmd;
        if (context.hasAny(this.index)) {
            final int idx = context.requireOne(this.index, Integer.class);
            if (idx == 0) {
                return context.errorResult("command.kit.command.remove.onebased");
            }

            if (idx > commands.size()) {
                return context.errorResult("command.kit.command.remove.overidx", commands.size(), kitInfo.getName());
            }

            cmd = commands.remove(idx - 1);
        } else {
            cmd = context.requireOne(NucleusParameters.Keys.COMMAND, String.class).replace(" {player} ", " {{player}} ");
            if (!commands.remove(cmd)) {
                return context.errorResult("command.kit.command.remove.noexist", cmd, kitInfo.getName());
            }
        }

        kitInfo.setCommands(commands);
        context.getServiceCollection().getServiceUnchecked(KitService.class).saveKit(kitInfo);
        context.sendMessage("command.kit.command.remove.success", cmd, kitInfo.getName());
        return context.successResult();
    }
}
