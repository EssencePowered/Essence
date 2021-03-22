/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.core.services.impl.texttemplatefactory;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.core.services.INucleusServiceCollection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NucleusTextTemplateImpl implements NucleusTextTemplate {

    private static final class Holder {

        private static final NucleusTextTemplateImpl EMPTY = new NucleusTextTemplateImpl(null, Collections.emptyList(), null, null);
    }

    private final List<BiFunction<Object, Map<String, Function<Object, Optional<ComponentLike>>>, Component>> texts;
    @Nullable private final Component prefix;
    @Nullable private final Component suffix;
    @Nullable private final INucleusServiceCollection serviceCollection;

    public NucleusTextTemplateImpl(
            @Nullable final INucleusServiceCollection serviceCollection,
            final List<BiFunction<Object, Map<String, Function<Object, Optional<ComponentLike>>>, Component>> texts,
            @Nullable final Component prefix,
            @Nullable final Component suffix) {
        this.texts = texts;
        this.prefix = prefix;
        this.suffix = suffix;
        this.serviceCollection = serviceCollection;
    }

    @NonNull
    public static NucleusTextTemplateImpl empty() {
        return NucleusTextTemplateImpl.Holder.EMPTY;
    }

    @Override
    public boolean isEmpty() {
        return this.serviceCollection == null || this.texts.isEmpty();
    }

    @Override
    public Optional<Component> getPrefix() {
        if (this.serviceCollection == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.prefix);
    }

    @Override
    public Optional<Component> getSuffix() {
        if (this.serviceCollection == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.suffix);
    }

    @Override
    public boolean containsTokens() {
        if (this.serviceCollection == null) {
            return false;
        }
        return this.texts.stream().anyMatch(x -> x instanceof TemplateParser.PlaceholderElement);
    }

    @Override
    public Component getForObject(final Object source) {
        return this.getForObjectWithTokens(source, null);
    }

    @Override
    public Component getForObjectWithSenderToken(final Object source, final Object sender) {
        if (this.serviceCollection == null) {
            return Component.empty();
        }
        final Optional<ComponentLike> s =
                Optional.of(this.serviceCollection.placeholderService().parse(sender, "displayname"));
        return this.getForObjectWithSenderToken(source,
                ImmutableMap.<String, Function<Object, Optional<? extends ComponentLike>>>builder()
                        .put("sender", se -> s)
                        .build());
    }

    @Override
    public Component getForObjectWithTokens(final Object source, @Nullable final Map<String, Function<Object, Optional<ComponentLike>>> tokensArray) {
        if (this.serviceCollection == null) {
            return Component.empty();
        }

        final TextComponent.Builder builder = Component.text();
        if (this.prefix != null) {
            builder.append(this.prefix);
        }

        for (final BiFunction<Object, Map<String, Function<Object, Optional<ComponentLike>>>, Component> textComponent : this.texts) {
            builder.append(textComponent.apply(source, tokensArray));
        }

        if (this.suffix != null) {
            builder.append(this.suffix);
        }

        return builder.build();
    }

    @Override
    public @NonNull Component asComponent() {
        return this.getForObject(Sponge.systemSubject());
    }
}
