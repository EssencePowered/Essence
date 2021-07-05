/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.events.RequestEvent;
import io.github.nucleuspowered.nucleus.modules.teleport.services.PlayerTeleporterService;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.scaffold.command.NucleusParameters.Keys;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.NotifyIfAFK;
import io.github.nucleuspowered.nucleus.scaffold.command.modifier.CommandModifiers;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.interfaces.ICooldownService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

@NonnullByDefault
@EssentialsEquivalent("tpahere")
@NotifyIfAFK(NucleusParameters.Keys.PLAYER)
@Command(
        aliases = {"tpahere", "tpaskhere", "teleportaskhere"},
        basePermission = TeleportPermissions.BASE_TPAHERE,
        commandDescriptionKey = "tpahere",
        modifiers = {
                @CommandModifier(
                        value = CommandModifiers.HAS_WARMUP,
                        exemptPermission = TeleportPermissions.EXEMPT_WARMUP_TPAHERE,
                        onExecute = false,
                        onCompletion = false
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COOLDOWN,
                        exemptPermission = TeleportPermissions.EXEMPT_COOLDOWN_TPAHERE
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COST,
                        exemptPermission = TeleportPermissions.EXEMPT_COST_TPAHERE
                )
        },
        associatedPermissions = {
                TeleportPermissions.TPTOGGLE_EXEMPT,
                TeleportPermissions.TELEPORT_HERE_FORCE
        }
)
public class TeleportAskHereCommand implements ICommandExecutor<Player> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
            GenericArguments.flags()
                    .permissionFlag(TeleportPermissions.TELEPORT_HERE_FORCE, "f")
                    .buildWith(NucleusParameters.ONE_USER.get(serviceCollection))
        };
    }

    @Override public Optional<ICommandResult> preExecute(ICommandContext.Mutable<? extends Player> context) throws CommandException {
        boolean cont = context.getServiceCollection()
                .teleporterService()
                .canTeleportTo(context.getUserFromArgs(), context.requireOne(Keys.USER, User.class));
        return cont ? Optional.empty() : Optional.of(context.failResult());
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        User target = context.requireOne(Keys.USER, User.class);
        if (context.is(target)) {
            return context.errorResult("command.teleport.self");
        }

        Player src = context.getCommandSource();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(src);
            // Before we do all this, check the event.
            RequestEvent.PlayerToCause event = new RequestEvent.PlayerToCause(frame.getCurrentCause(), target);
            if (Sponge.getEventManager().post(event)) {
                return event.getCancelMessage()
                        .map(context::errorResultLiteral)
                        .orElseGet(() -> context.errorResult("command.tpa.eventfailed"));
            }

            ICooldownService cooldownService = context.getServiceCollection().cooldownService();
            Duration cooldownSeconds = Duration.ofSeconds(context.getCooldown());
            String key = context.getCommandKey();
            Consumer<Player> cooldownSetter = player -> cooldownService.setCooldown(
                    key,
                    player,
                    cooldownSeconds
            );
            context.getServiceCollection()
                    .teleporterService()
                    .requestTeleport(
                        src,
                        target,
                        context.getCost(),
                        context.getWarmup(),
                        target,
                        src,
                        !context.getOne("f", Boolean.class).orElse(false),
                        false,
                        false,
                        cooldownSetter,
                        "command.tpahere.question"
            );

            return context.successResult();
        }
    }
}
