/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands.gamemode;

import io.github.nucleuspowered.nucleus.modules.admin.AdminPermissions;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.core.scaffold.command.modifier.CommandModifiers;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
@Command(
        aliases = {"gms", "survival"},
        basePermission = {AdminPermissions.BASE_GAMEMODE, AdminPermissions.GAMEMODE_MODES_SURVIVAL},
        commandDescriptionKey = "gms",
        modifiers =
                {
                        @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission = AdminPermissions.EXEMPT_WARMUP_GAMEMODE,
                                useFrom = GamemodeCommand.class),
                        @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = AdminPermissions.EXEMPT_COOLDOWN_GAMEMODE,
                                useFrom = GamemodeCommand.class),
                        @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = AdminPermissions.EXEMPT_COST_GAMEMODE,
                                useFrom = GamemodeCommand.class)
                }
)
@EssentialsEquivalent({"gms", "survival"})
public class SurvivalGamemodeCommand extends GamemodeBase {

    @Override
    public ICommandResult execute(final ICommandContext src) throws CommandException {
        return this.baseCommand(src, src.getIfPlayer(), GameModes.SURVIVAL);
    }
}
