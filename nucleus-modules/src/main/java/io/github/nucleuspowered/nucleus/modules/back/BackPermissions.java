/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back;

import io.github.nucleuspowered.nucleus.core.services.interfaces.annotation.PermissionMetadata;
import io.github.nucleuspowered.nucleus.core.services.interfaces.data.SuggestedLevel;

public final class BackPermissions {
    private BackPermissions() {
        throw new AssertionError("Nope");
    }

    @PermissionMetadata(descriptionKey = "permission.base", replacements = { "back" }, level = SuggestedLevel.USER)
    public static final String BASE_BACK = "nucleus.back.base";

    @PermissionMetadata(descriptionKey = "permission.base", replacements = { "clearback" }, level = SuggestedLevel.ADMIN)
    public static final String BASE_CLEARBACK = "nucleus.clearback.base";

    @PermissionMetadata(descriptionKey = "permission.others", replacements = { "clearback" }, level = SuggestedLevel.ADMIN)
    public static final String OTHERS_CLEARBACK = "nucleus.clearback.others";

    @PermissionMetadata(descriptionKey = "permission.tppos.border", level = SuggestedLevel.ADMIN)
    public static final String TPPOS_BORDER = "nucleus.back.exempt.bordercheck";

    @PermissionMetadata(descriptionKey = "permission.exempt.cooldown", replacements = { "back" }, level = SuggestedLevel.ADMIN)
    public static final String EXEMPT_COOLDOWN_BACK = "nucleus.back.exempt.cooldown";

    @PermissionMetadata(descriptionKey = "permission.exempt.cost", replacements = { "back" }, level = SuggestedLevel.ADMIN)
    public static final String EXEMPT_COST_BACK = "nucleus.back.exempt.cost";

    @PermissionMetadata(descriptionKey = "permission.back.exempt.samedimension", level = SuggestedLevel.MOD)
    public static final String BACK_EXEMPT_SAMEDIMENSION = "nucleus.back.exempt.samedimension";

    @PermissionMetadata(descriptionKey = "permission.exempt.warmup", replacements = { "back" }, level = SuggestedLevel.ADMIN)
    public static final String EXEMPT_WARMUP_BACK = "nucleus.back.exempt.warmup";

    @PermissionMetadata(descriptionKey = "permission.back.ondeath", level = SuggestedLevel.USER)
    public static final String BACK_ONDEATH = "nucleus.back.targets.death";

    @PermissionMetadata(descriptionKey = "permission.back.onportal", level = SuggestedLevel.USER)
    public static final String BACK_ONPORTAL = "nucleus.back.targets.portal";

    @PermissionMetadata(descriptionKey = "permission.back.onteleport", level = SuggestedLevel.USER)
    public static final String BACK_ONTELEPORT = "nucleus.back.targets.teleport";

}
