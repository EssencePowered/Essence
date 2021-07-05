/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.interfaces;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.api.module.teleport.NucleusPlayerTeleporterService;
import io.github.nucleuspowered.nucleus.modules.teleport.services.PlayerTeleporterService;

@ImplementedBy(PlayerTeleporterService.class)
public interface IPlayerTeleporterService extends NucleusPlayerTeleporterService {

}
