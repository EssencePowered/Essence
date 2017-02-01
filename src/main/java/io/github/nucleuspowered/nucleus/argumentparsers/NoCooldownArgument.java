/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;

import javax.annotation.Nullable;

public class NoCooldownArgument extends CommandElement {
    public static final String NO_COOLDOWN_ARGUMENT = "nocooldown";

    private final Text key;
    private final CommandElement element;

    public NoCooldownArgument(CommandElement element) {
        super(element.getKey());
        this.key = element.getKey();
        this.element = element;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        this.element.parse(source, args, context);

        // We'll get here if there are no exceptions thrown.
        if (key != null && context.getOne(key.toPlain()).isPresent()) {
            context.putArg(NO_COOLDOWN_ARGUMENT, true);
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return this.element.complete(src, args, context);
    }

    @Override
    public Text getUsage(CommandSource src) {
        return this.element.getUsage(src);
    }
}
