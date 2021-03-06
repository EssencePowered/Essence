/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info.config;

import io.github.nucleuspowered.nucleus.core.services.interfaces.annotation.configuratehelper.LocalisedComment;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class MotdConfig {

    @Setting(value = "show-motd-on-join")
    @LocalisedComment("config.motd.onjoin")
    private boolean showMotdOnJoin = true;

    @Setting(value = "motd-login-delay")
    @LocalisedComment("config.motd.delay")
    private float delay = 0.5f;

    @Setting(value = "motd-title")
    @LocalisedComment("config.motd.title")
    private String motdTitle = "MOTD";

    @Setting(value = "use-pagination")
    @LocalisedComment("config.motd.pagination")
    private boolean usePagination = true;

    public boolean isShowMotdOnJoin() {
        return this.showMotdOnJoin;
    }

    public String getMotdTitle() {
        return this.motdTitle;
    }

    public boolean isUsePagination() {
        return this.usePagination;
    }

    public float getDelay() {
        return this.delay;
    }
}
