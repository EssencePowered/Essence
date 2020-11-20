/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat.commands;

import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatPermissions;
import io.github.nucleuspowered.nucleus.modules.staffchat.services.StaffChatService;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.NucleusKeysProvider;
import io.github.nucleuspowered.nucleus.services.interfaces.IUserPreferenceService;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
@Command(
        aliases = {"toggleviewstaffchat", "vsc", "togglevsc"},
        basePermission = StaffChatPermissions.BASE_STAFFCHAT,
        commandDescriptionKey = "toggleviewstaffchat"
)
public class ToggleStaffChatCommand implements ICommandExecutor {

    @Override
    public Parameter[] parameters(final INucleusServiceCollection serviceCollection) {
        return new Parameter[] {
                NucleusParameters.OPTIONAL_ONE_TRUE_FALSE
        };
    }

    @Override public ICommandResult execute(final ICommandContext context) throws CommandException {
        final IUserPreferenceService ups = context.getServiceCollection().userPreferenceService();
        final Player src = context.getIfPlayer();
        final boolean result =
                context.getOne(NucleusParameters.Keys.BOOL, Boolean.class).orElseGet(() ->
                    ups.getPreferenceFor(src.getUniqueId(), NucleusKeysProvider.VIEW_STAFF_CHAT).orElse(true));
        ups.setPreferenceFor(src, NucleusKeysProvider.VIEW_STAFF_CHAT, !result);
        final StaffChatService service = context.getServiceCollection().getServiceUnchecked(StaffChatService.class);

        if (!result && service.isToggledChat(src)) {
            service.toggle(src, false);
        }

        context.sendMessage("command.staffchat.view." + (result ? "on" : "off"));
        return context.successResult();
    }

}