/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.core.services.interfaces.data;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.service.permission.PermissionDescription;

public enum SuggestedLevel {
    USER(null, "nucleus.user", PermissionDescription.ROLE_USER),
    MOD(USER, "nucleus.mod", PermissionDescription.ROLE_STAFF),
    ADMIN(MOD, "nucleus.admin", PermissionDescription.ROLE_ADMIN),
    OWNER(ADMIN, "nucleus.owner", PermissionDescription.ROLE_ADMIN),
    NONE(OWNER, null, null);

    @Nullable private final SuggestedLevel level;
    @Nullable private final String permission;
    @Nullable private final String role;

    SuggestedLevel(@Nullable final SuggestedLevel level, @Nullable final String permission, @Nullable final String role) {
        this.level = level;
        this.permission = permission;
        this.role = role;
    }

    @Nullable
    public SuggestedLevel getLowerLevel() {
        return this.level;
    }

    @Nullable
    public String getPermission() {
        return this.permission;
    }

    @Nullable
    public String getRole() {
        return this.role;
    }
}
