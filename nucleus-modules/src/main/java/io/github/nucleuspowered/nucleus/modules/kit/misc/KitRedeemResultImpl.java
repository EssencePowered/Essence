/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.misc;

import io.github.nucleuspowered.nucleus.api.module.kit.KitRedeemResult;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public class KitRedeemResultImpl implements KitRedeemResult {

    private final Collection<ItemStackSnapshot> rejected;
    private final Status status;
    @Nullable private final Instant nextCooldown;
    @Nullable private final Component message;

    public KitRedeemResultImpl(
            final Status status,
            final Collection<ItemStackSnapshot> rejected,
            @Nullable final Instant nextCooldown,
            @Nullable final Component message) {
        this.rejected = rejected;
        this.status = status;
        this.nextCooldown = nextCooldown;
        this.message = message;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public Optional<Instant> getCooldownExpiry() {
        return Optional.ofNullable(this.nextCooldown);
    }

    @Override
    public Optional<Component> getMessage() {
        return Optional.ofNullable(this.message);
    }

    @Override
    public Collection<ItemStackSnapshot> rejectedItems() {
        return this.rejected;
    }

}
