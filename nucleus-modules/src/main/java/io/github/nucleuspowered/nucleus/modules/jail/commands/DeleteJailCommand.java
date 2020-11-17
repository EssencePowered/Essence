/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.module.jail.data.Jail;
import io.github.nucleuspowered.nucleus.modules.jail.JailPermissions;
import io.github.nucleuspowered.nucleus.modules.jail.parameter.JailParameter;
import io.github.nucleuspowered.nucleus.modules.jail.services.JailService;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;

;

@EssentialsEquivalent({"deljail", "remjail", "rmjail"})
@Command(
        aliases = {"delete", "del", "remove", "#deljail", "#rmjail", "#deletejail" },
        basePermission = JailPermissions.BASE_JAILS_DELETE,
        async = true,
        commandDescriptionKey = "jails.delete",
        parentCommand = JailsCommand.class
)
public class DeleteJailCommand implements ICommandExecutor {

    private final Parameter.Value<Jail> parameter;

    @Inject
    public DeleteJailCommand(final INucleusServiceCollection serviceCollection) {
        this.parameter = Parameter.builder(Jail.class)
                .setKey("jail")
                .optional()
                .parser(new JailParameter(serviceCollection.getServiceUnchecked(JailService.class), serviceCollection.messageProvider()))
                .build();
    }

    @Override
    public Parameter[] parameters(final INucleusServiceCollection serviceCollection) {
        return new Parameter[] {
                this.parameter
        };
    }

    @Override
    public ICommandResult execute(final ICommandContext context) throws CommandException {
        final Jail wl = context.requireOne(this.parameter);
        if (context.getServiceCollection().getServiceUnchecked(JailService.class).removeJail(wl.getName())) {
            context.sendMessage("command.jails.del.success", wl.getName());
            return context.successResult();
        }

        return context.errorResult("command.jails.del.error", wl.getName());
    }
}
