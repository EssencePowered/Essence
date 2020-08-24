/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.parameters;

import io.github.nucleuspowered.nucleus.modules.home.HomePermissions;
import io.github.nucleuspowered.nucleus.modules.home.services.HomeService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.interfaces.IPermissionService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public class HomeOtherArgument extends HomeArgument {

    private final DisplayNameArgument nickArg;
    private final IPermissionService permissionService;

    public HomeOtherArgument(@Nullable final TextComponent key,
            final HomeService homeService,
            final INucleusServiceCollection serviceCollection) {
        super(key, homeService, serviceCollection.messageProvider());
        this.nickArg = new DisplayNameArgument(Text.of("user"), DisplayNameArgument.Target.USER, serviceCollection);
        this.permissionService = serviceCollection.permissionService();
    }


    @Nullable
    @Override
    protected Object parseValue(final CommandSource source, final CommandArgs args) throws ArgumentParseException {
        final String player = args.next();
        final Optional<String> ohome = args.nextIfPresent();

        if (!ohome.isPresent()) {
            throw args.createError(this.messageProviderService.getMessageFor(source, "args.homeother.notenough"));
        }

        // We know it's an instance of a user.
        final Set<?> users = this.nickArg.parseValue(source, player.toLowerCase(),
                (key, entry) -> args.createError(this.messageProviderService.getMessageFor(source.getLocale(), key, entry)));
        if (users.size() != 1) {
            throw args.createError(this.messageProviderService.getMessageFor(source.getLocale(), "args.homeother.ambiguous"));
        }

        final User user = (User) users.iterator().next();
        if (this.permissionService.hasPermission(source, HomePermissions.HOME_OTHER_EXEMPT_TARGET)) {
            throw args.createError(this.messageProviderService.getMessageFor(source.getLocale(), "args.homeother.exempt"));
        }

        return this.getHome(user, source, ohome.get(), args);
    }

    @Override
    public List<String> complete(final CommandSource src, final CommandArgs args, final CommandContext context) {
        CommandArgs.Snapshot saveState = null;
        try {
            saveState = args.getSnapshot();

            // Do we have two args?
            final String arg1 = args.next();
            final Optional<String> arg2 = args.nextIfPresent();
            if (arg2.isPresent()) {
                // Get the user
                final Set<?> u = this.nickArg.parseValue(src, arg1.toLowerCase(),
                        (key, entry) -> args.createError(this.messageProviderService.getMessageFor(src.getLocale(), key, entry)));
                if (u.size() != 1) {
                    throw args.createError(this.messageProviderService.getMessageFor(src.getLocale(), "args.homeother.ambiguous"));
                }

                final User user = (User) (u.iterator().next());
                return this.complete(user, arg2.get());
            } else {
                args.applySnapshot(saveState);
                return this.nickArg.complete(src, args, context);
            }

        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (saveState != null) {
                args.applySnapshot(saveState);
            }
        }

        return Collections.emptyList();
    }

    @Override
    public TextComponent getUsage(final CommandSource src) {
        return Text.of("<user> <home>");
    }
}
