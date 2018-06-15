/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Sets kit items.
 *
 * Command Usage: /kit add Permission: plugin.kit.add.base
 */
@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"add", "createFromInventory"}, subcommandOf = KitCommand.class)
@NoModifiers
@NonnullByDefault
public class KitAddCommand extends AbstractCommand<Player> {

    private final String name = "name";
    private final KitHandler handler = getServiceUnchecked(KitHandler.class);

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(GenericArguments.string(Text.of(name)))};
    }

    @Override
    public CommandResult executeCommand(final Player player, CommandContext args) throws Exception {
        String kitName = args.<String>getOne(name).get();

        if (this.handler.getKitNames().stream().noneMatch(kitName::equalsIgnoreCase)) {
            this.handler.saveKit(this.handler.createKit(kitName).updateKitInventory(player));
            player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.add.success", kitName));
            return CommandResult.success();
        } else {
            throw ReturnMessageException.fromKey("command.kit.add.alreadyexists", kitName);
        }
    }
}
