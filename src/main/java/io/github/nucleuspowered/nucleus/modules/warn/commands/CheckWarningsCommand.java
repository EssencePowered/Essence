/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warn.commands;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.WarnData;
import io.github.nucleuspowered.nucleus.internal.annotations.*;
import io.github.nucleuspowered.nucleus.internal.command.CommandBase;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.warn.handlers.WarnHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Checks the warnings of a player.
 *
 * Command Usage: /checkwarnings user Permission: quickstart.checkwarnings.base
 */
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoWarmup
@NoCooldown
@NoCost
@RegisterCommand({"checkwarnings", "checkwarn", "warnings"})
public class CheckWarningsCommand extends CommandBase<CommandSource> {

    @Inject private WarnHandler handler;
    private final String playerKey = "player";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.flags().flag("-all", "a").flag("-expired", "e").buildWith(
                GenericArguments.onlyOne(GenericArguments.user(Text.of(playerKey))))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User user = args.<User>getOne(playerKey).get();

        List<WarnData> warnings;
        if (args.hasAny("all")) {
            warnings = handler.getWarnings(user);
        } else if (args.hasAny("expired")) {
            warnings = handler.getWarnings(user, false, true);
        } else {
            warnings = handler.getWarnings(user, true, false);
        }

        if (warnings.isEmpty()) {
            src.sendMessage(Util.getTextMessageWithFormat("command.checkwarnings.none", user.getName()));
            return CommandResult.success();
        }
        handler.updateWarnings(user);

        int index = 0;
        List<Text> messages = Lists.newArrayList();
        for (WarnData warning : warnings) {
            String name;
            if (warning.getWarner().equals(Util.consoleFakeUUID)) {
                name = Sponge.getServer().getConsole().getName();
            } else {
                Optional<User> ou = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(warning.getWarner());
                name = ou.isPresent() ? ou.get().getName() : Util.getMessageWithFormat("standard.unknown");
            }

            String time;
            if (warning.getEndTimestamp().isPresent()) {
                time = Util.getTimeStringFromSeconds(Instant.now().until(warning.getEndTimestamp().get(), ChronoUnit.SECONDS));
            } else if (warning.getTimeFromNextLogin().isPresent()) {
                time = Util.getTimeStringFromSeconds(warning.getTimeFromNextLogin().get().getSeconds());
            } else {
                time = Util.getMessageWithFormat("standard.restoftime");
            }

            if (warning.isExpired()) {
                messages.add(Util.getTextMessageWithFormat("command.checkwarnings.expired", String.valueOf(index + 1), user.getName(), name, warning.getReason()));
            } else {
                messages.add(Util.getTextMessageWithFormat("command.checkwarnings.warn", String.valueOf(index + 1), user.getName(), name, time, warning.getReason()));
            }
            index++;
        }

        PaginationService paginationService = Sponge.getGame().getServiceManager().provideUnchecked(PaginationService.class);
        paginationService.builder()
                .title(
                        Text.builder()
                        .color(TextColors.GOLD)
                        .append(Text.of(user.getName() + Util.getMessageWithFormat("command.checkwarnings.header")))
                        .build())
                .padding(
                        Text.builder()
                        .color(TextColors.YELLOW)
                        .append(Text.of("="))
                        .build())
                .contents(messages)
                .sendTo(src);

        return CommandResult.success();
    }
}
