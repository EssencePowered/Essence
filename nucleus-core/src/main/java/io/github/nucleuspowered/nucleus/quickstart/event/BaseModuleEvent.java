/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.quickstart.event;

import io.github.nucleuspowered.nucleus.OldNucleusCore;
import io.github.nucleuspowered.nucleus.api.core.event.NucleusModuleEvent;
import io.github.nucleuspowered.nucleus.api.core.exception.ModulesLoadedException;
import io.github.nucleuspowered.nucleus.api.core.exception.NoModuleException;
import io.github.nucleuspowered.nucleus.api.core.exception.UnremovableModuleException;
import io.github.nucleuspowered.nucleus.quickstart.ModuleRegistrationProxyService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.plugin.PluginContainer;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseModuleEvent extends AbstractEvent implements NucleusModuleEvent {

    private final OldNucleusCore plugin;
    private final Cause cause;
    private final Map<String, ModuleEnableState> state;

    private BaseModuleEvent(final OldNucleusCore plugin) {
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
        this.plugin = plugin;
        this.state = this.getState();
    }

    @Override
    public Map<String, ModuleEnableState> getModuleList() {
        return this.state;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    Map<String, ModuleEnableState> getState() {
        return this.plugin.getModuleHolder().getModulesWithLoadingState()
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> this.getFromLoadingStatus(v.getValue())));
    }

    private ModuleEnableState getFromLoadingStatus(final LoadingStatus status) {
        if (status == LoadingStatus.DISABLED) {
            return ModuleEnableState.DISABLED;
        } else if (status == LoadingStatus.ENABLED) {
            return ModuleEnableState.ENABLED;
        } else {
            return ModuleEnableState.FORCE_ENABLED;
        }
    }

    public static class AboutToConstructEvent extends BaseModuleEvent implements NucleusModuleEvent.AboutToConstruct {
        public AboutToConstructEvent(final OldNucleusCore plugin) {
            super(plugin);
        }

        @Override
        public Map<String, ModuleEnableState> getModuleList() {
            return this.getState();
        }

        @Override
        public void disableModule(final String module, final PluginContainer plugin) throws UnremovableModuleException, NoModuleException {
            try {
                Sponge.getServiceManager().provideUnchecked(ModuleRegistrationProxyService.class).removeModule(module, plugin);
            } catch (final ModulesLoadedException e) {
                // This shouldn't happen, as this gets called before the registration
                // But, just in case...
                e.printStackTrace();
            }
        }
    }

    public static class AboutToEnable extends BaseModuleEvent implements NucleusModuleEvent.AboutToEnable {

        public AboutToEnable(final OldNucleusCore plugin) {
            super(plugin);
        }
    }

    public static class Complete extends BaseModuleEvent implements NucleusModuleEvent.Complete {

        public Complete(final OldNucleusCore plugin) {
            super(plugin);
        }
    }

    public static class PreEnable extends BaseModuleEvent implements NucleusModuleEvent.PreEnable {

        public PreEnable(final OldNucleusCore plugin) {
            super(plugin);
        }
    }

    public static class Enabled extends BaseModuleEvent implements NucleusModuleEvent.Enabled {

        public Enabled(final OldNucleusCore plugin) {
            super(plugin);
        }
    }
}
