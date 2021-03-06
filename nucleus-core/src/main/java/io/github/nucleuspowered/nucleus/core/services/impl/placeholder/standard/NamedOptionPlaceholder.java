/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.core.services.impl.placeholder.standard;

import io.github.nucleuspowered.nucleus.core.services.interfaces.IPermissionService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.service.permission.Subject;

import java.util.Objects;
import java.util.Optional;

public class NamedOptionPlaceholder implements PlaceholderParser {

    private final IPermissionService permissionService;
    private final String namedOption;

    public NamedOptionPlaceholder(final IPermissionService permissionService, final String namedOption) {
        this.permissionService = permissionService;
        this.namedOption = namedOption;
    }

    @Override
    public Component parse(final PlaceholderContext placeholderContext) {
        final Optional<Subject> subjectOptional = placeholderContext.associatedObject().filter(x -> x instanceof Subject).map(x -> (Subject) x);
        return subjectOptional.flatMap(subject -> this.permissionService
                .getOptionFromSubject(subject, this.namedOption)
                .map(LegacyComponentSerializer.legacyAmpersand()::deserialize))
                .orElse(Component.empty());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final NamedOptionPlaceholder that = (NamedOptionPlaceholder) o;
        return Objects.equals(this.namedOption, that.namedOption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.namedOption);
    }
}
