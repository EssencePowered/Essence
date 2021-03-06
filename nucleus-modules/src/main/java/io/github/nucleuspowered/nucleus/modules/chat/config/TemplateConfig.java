/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import io.github.nucleuspowered.nucleus.core.services.interfaces.annotation.configuratehelper.LocalisedComment;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class TemplateConfig {

    @Setting(value = "use-group-templates")
    @LocalisedComment("config.chat.useGroupTemplates")
    private boolean useGroupTemplates = true;

    @Setting(value = "default")
    @LocalisedComment("config.chat.default-template")
    private ChatTemplateConfig defaultTemplate = new ChatTemplateConfig();

    // NoMergeIfPresent
    @Setting(value = "group-templates")
    @LocalisedComment("config.chat.group-templates")
    private Map<String, ChatTemplateConfig> groupTemplates = new HashMap<String, ChatTemplateConfig>() {{
        // We don't want this affecting the default group, but we need an example.
        this.put("DefaultTemplate", new ChatTemplateConfig());
    }};

    public ChatTemplateConfig getDefaultTemplate() {
        return this.defaultTemplate;
    }

    public Map<String, ChatTemplateConfig> getGroupTemplates() {
        return this.groupTemplates;
    }

    public boolean isUseGroupTemplates() {
        return this.useGroupTemplates;
    }

}
