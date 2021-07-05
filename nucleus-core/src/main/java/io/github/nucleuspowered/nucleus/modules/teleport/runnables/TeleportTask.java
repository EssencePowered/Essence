/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.runnables;

import io.github.nucleuspowered.nucleus.scaffold.task.TaskBase;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.interfaces.IPlayerTeleporterService;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

public class TeleportTask implements TaskBase {

    private final IPlayerTeleporterService teleporterService;

    @Inject
    public TeleportTask(INucleusServiceCollection serviceCollection) {
        this.teleporterService = serviceCollection.teleporterService();
    }

    @Override
    public void accept(Task task) {
        teleporterService.removeExpired();
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(2, ChronoUnit.SECONDS);
    }
}
