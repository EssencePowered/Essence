/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.api.module.warp.data.Warp;
import io.github.nucleuspowered.nucleus.api.module.warp.data.WarpCategory;
import io.github.nucleuspowered.nucleus.modules.warp.WarpPermissions;
import io.github.nucleuspowered.nucleus.modules.warp.services.WarpService;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.Tuple;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Command(
        aliases = {"setcategory"},
        basePermission = WarpPermissions.BASE_WARP_SETCATEGORY,
        commandDescriptionKey = "warp.setcategory",
        parentCommand = WarpCommand.class
)
public class SetCategoryCommand implements ICommandExecutor {

    private static final TypeToken<Tuple<String, Boolean>> TUPLE_TYPE_TOKEN = new TypeToken<Tuple<String, Boolean>>() {};

    @Override public Parameter[] parameters(final INucleusServiceCollection serviceCollection) {
        return new Parameter[] {
            GenericArguments.flags().flag("r", "-remove", "-delete").flag("n", "-new").buildWith(
                GenericArguments.seq(
                        serviceCollection.getServiceUnchecked(WarpService.class).warpElement(false),
                        GenericArguments.optional(
                                new SetCategoryWarpCategoryArgument(
                                        serviceCollection.getServiceUnchecked(WarpService.class)
                                ))
                )
            )
        };
    }

    @Override public ICommandResult execute(final ICommandContext context) throws CommandException {
        final String warpName = context.requireOne(WarpService.WARP_KEY, Warp.class).getName();
        final WarpService handler = context.getServiceCollection().getServiceUnchecked(WarpService.class);
        if (context.hasAny("r")) {
            // Remove the category.
            if (handler.setWarpCategory(warpName, null)) {
                context.sendMessage("command.warp.category.removed", warpName);
                return context.successResult();
            }

            return context.errorResult("command.warp.category.noremove", warpName);
        }

        final Optional<Tuple<String, Boolean>> categoryOp = context.getOne(WarpService.WARP_CATEGORY_KEY, TUPLE_TYPE_TOKEN);
        if (!categoryOp.isPresent()) {
            return context.errorResult("command.warp.category.required");
        }

        final Tuple<String, Boolean> category = categoryOp.get();
        if (!context.hasAny("n") && !category.getSecond()) {
            context.sendMessageText(context.getMessage("command.warp.category.requirenew", category.getFirst())
                    .toBuilder().onClick(TextActions.runCommand("/warp setcategory -n " + warpName + " " + category.getFirst())).build()
            );

            return context.failResult();
        }

        // Add the category.
        if (handler.setWarpCategory(warpName, category.getFirst())) {
            context.sendMessage("command.warp.category.added", category.getFirst(), warpName);
            return context.successResult();
        }

        return context.errorResult("command.warp.category.couldnotadd", Text.of(category.getFirst()), Text.of(warpName));
    }

    private static class SetCategoryWarpCategoryArgument extends CommandElement {

        private final WarpService service;

        SetCategoryWarpCategoryArgument(final WarpService service) {
            super(Text.of(WarpService.WARP_CATEGORY_KEY));
            this.service = service;
        }

        @Nullable @Override protected Object parseValue(@Nonnull final CommandSource source, @Nonnull final CommandArgs args) throws ArgumentParseException {
            final String arg = args.next();
            return Tuple.of(arg, this.service
                    .getWarpsWithCategories().keySet().stream().filter(Objects::nonNull).anyMatch(x -> x.getId().equals(arg)));
        }

        @Nonnull @Override public List<String> complete(@Nonnull final CommandSource src, @Nonnull final CommandArgs args, @Nonnull
        final CommandContext context) {
            return this.service.getWarpsWithCategories()
                    .keySet()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(WarpCategory::getId)
                    .collect(Collectors.toList());
        }
    }
}