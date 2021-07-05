/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api;

import io.github.nucleuspowered.nucleus.api.core.NucleusAPIMetaService;
import io.github.nucleuspowered.nucleus.api.core.NucleusPlayerMetadataService;
import io.github.nucleuspowered.nucleus.api.core.NucleusUserPreferenceService;
import io.github.nucleuspowered.nucleus.api.core.NucleusWarmupManagerService;
import io.github.nucleuspowered.nucleus.api.core.NucleusWorldUUIDChangeService;
import io.github.nucleuspowered.nucleus.api.module.afk.NucleusAFKService;
import io.github.nucleuspowered.nucleus.api.module.back.NucleusBackService;
import io.github.nucleuspowered.nucleus.api.module.freezeplayer.NucleusFreezePlayerService;
import io.github.nucleuspowered.nucleus.api.module.home.NucleusHomeService;
import io.github.nucleuspowered.nucleus.api.module.invulnerability.NucleusInvulnerabilityService;
import io.github.nucleuspowered.nucleus.api.module.jail.NucleusJailService;
import io.github.nucleuspowered.nucleus.api.module.kit.NucleusKitService;
import io.github.nucleuspowered.nucleus.api.module.mail.NucleusMailService;
import io.github.nucleuspowered.nucleus.api.module.message.NucleusPrivateMessagingService;
import io.github.nucleuspowered.nucleus.api.module.mute.NucleusMuteService;
import io.github.nucleuspowered.nucleus.api.module.nameban.NucleusNameBanService;
import io.github.nucleuspowered.nucleus.api.module.nickname.NucleusNicknameService;
import io.github.nucleuspowered.nucleus.api.module.note.NucleusNoteService;
import io.github.nucleuspowered.nucleus.api.module.playerinfo.NucleusSeenService;
import io.github.nucleuspowered.nucleus.api.module.rtp.NucleusRTPService;
import io.github.nucleuspowered.nucleus.api.module.staffchat.NucleusStaffChatService;
import io.github.nucleuspowered.nucleus.api.module.teleport.NucleusPlayerTeleporterService;
import io.github.nucleuspowered.nucleus.api.module.warning.NucleusWarningService;
import io.github.nucleuspowered.nucleus.api.module.warp.NucleusWarpService;
import io.github.nucleuspowered.nucleus.api.placeholder.NucleusPlaceholderService;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplateFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;

import java.util.Optional;

/**
 * Contains static methods as an alternative way from using the Sponge Service manager.
 */
public class NucleusAPI {

    private NucleusAPI() {}

    /**
     * Gets the API Meta service, which contains the version information.
     * @return The {@link NucleusAPIMetaService}
     * @throws IllegalStateException if Nucleus hasn't completed pre init yet.
     */
    public static NucleusAPIMetaService getMetaService() {
        return getService(NucleusAPIMetaService.class).orElseThrow(() -> new IllegalStateException("Nucleus API has not started registering yet"));
    }

    /**
     * Gets the API Player Metadata service, which contains the player metadata information.
     * @return The {@link NucleusPlayerMetadataService}
     * @throws IllegalStateException if Nucleus hasn't completed post init yet.
     */
    public static NucleusPlayerMetadataService getPlayerMetadataService() {
        return getService(NucleusPlayerMetadataService.class).orElseThrow(() -> new IllegalStateException("Nucleus API has not started registering yet"));
    }

    /**
     * Gets the {@link NucleusWarmupManagerService} service, which allows plugins to use Nucleus to manage their warmups.
     * @return The {@link NucleusWarmupManagerService}
     * @throws IllegalStateException if Nucleus hasn't completed pre init yet.
     */
    public static NucleusWarmupManagerService getWarmupManagerService() {
        return getService(NucleusWarmupManagerService.class).orElseThrow(() -> new IllegalStateException("Nucleus API has not started registering yet"));
    }

    /**
     * Gets the {@link NucleusPlaceholderService} service, which allows plugins to register message placeholders.
     * @return The {@link NucleusPlaceholderService}
     * @throws IllegalStateException if Nucleus hasn't completed pre init yet.
     */
    public static NucleusPlaceholderService getPlaceholderService() {
        return getService(NucleusPlaceholderService.class)
                .orElseThrow(() -> new IllegalStateException("Placeholders are not being registered yet"));
    }

    /**
     * Gets the {@link NucleusTextTemplateFactory} service, which allows plugins to create {@link NucleusTextTemplate}s.
     * @return The {@link NucleusTextTemplateFactory}
     * @throws IllegalStateException if Nucleus hasn't completed pre init yet.
     */
    public static NucleusTextTemplateFactory getTextTemplateFactory() {
        return getService(NucleusTextTemplateFactory.class)
                .orElseThrow(() -> new IllegalStateException("Nucleus Text Template Factory has not been registered"));
    }

    /**
     * Gets the {@link NucleusWorldUUIDChangeService} service, which allows plugins to check if the admin has created a world UUID mapping.
     *
     * <p>
     *     For proper verification, this will not contain any mappings until after the {@link GameStartingServerEvent} on default ordering
     * </p>
     *
     * @return The {@link NucleusWorldUUIDChangeService}
     * @throws IllegalStateException if Nucleus hasn't completed postinit.
     */
    public static NucleusWorldUUIDChangeService getWorldUUIDChangeService() {
        return getService(NucleusWorldUUIDChangeService.class).orElseThrow(() -> new IllegalStateException("World UUID mappings have not yet been "
                + "loaded"));
    }

    /**
     * Gets the {@link NucleusUserPreferenceService} service, which allows plugins to read and set user preferences.
     * @return The {@link NucleusUserPreferenceService}
     * @throws IllegalStateException if Nucleus hasn't completed pre init yet.
     */
    public static NucleusUserPreferenceService getUserPreferenceService() {
        return getService(NucleusUserPreferenceService.class)
                .orElseThrow(() -> new IllegalStateException("Nucleus API has not started registering yet"));
    }

    /**
     * Gets the {@link NucleusAFKService}, if it exists.
     *
     * <p>
     *     Requires the "afk" module.
     * </p>
     *
     * @return The {@link NucleusAFKService}
     */
    public static Optional<NucleusAFKService> getAFKService() {
        return getService(NucleusAFKService.class);
    }

    /**
     * Gets the {@link NucleusBackService}, if it exists.
     *
     * <p>
     *     Requires the "back" module.
     * </p>
     *
     * @return The {@link NucleusBackService}
     */
    public static Optional<NucleusBackService> getBackService() {
        return getService(NucleusBackService.class);
    }

    /**
     * Gets the {@link NucleusFreezePlayerService}, if it exists.
     *
     * <p>
     *     Requires the "freeze-player" module.
     * </p>
     *
     * @return The {@link NucleusFreezePlayerService}
     */
    public static Optional<NucleusFreezePlayerService> getFreezePlayerService() {
        return getService(NucleusFreezePlayerService.class);
    }

    /**
     * Gets the {@link NucleusHomeService}, if it exists.
     *
     * <p>
     *     Requires the "home" module.
     * </p>
     *
     * @return The {@link NucleusHomeService}
     */
    public static Optional<NucleusHomeService> getHomeService() {
        return getService(NucleusHomeService.class);
    }

    /**
     * Gets the {@link NucleusInvulnerabilityService}, if it exists.
     *
     * <p>
     *     Requires the "misc" module.
     * </p>
     *
     * @return The {@link NucleusInvulnerabilityService}
     */
    public static Optional<NucleusInvulnerabilityService> getInvulnerabilityService() {
        return getService(NucleusInvulnerabilityService.class);
    }

    /**
     * Gets the {@link NucleusJailService}, if it exists.
     *
     * <p>
     *     Requires the "jail" module.
     * </p>
     *
     * @return The {@link NucleusJailService}
     */
    public static Optional<NucleusJailService> getJailService() {
        return getService(NucleusJailService.class);
    }

    /**
     * Gets the {@link NucleusKitService}, if it exists.
     *
     * <p>
     *     Requires the "kit" module.
     * </p>
     *
     * @return The {@link NucleusKitService}
     */
    public static Optional<NucleusKitService> getKitService() {
        return getService(NucleusKitService.class);
    }

    /**
     * Gets the {@link NucleusMailService}, if it exists.
     *
     * <p>
     *     Requires the "mail" module.
     * </p>
     *
     * @return The {@link NucleusMailService}
     */
    public static Optional<NucleusMailService> getMailService() {
        return getService(NucleusMailService.class);
    }

    /**
     * Gets the {@link NucleusMuteService}, if it exists.
     *
     * <p>
     *     Requires the "mute" module.
     * </p>
     *
     * @return The {@link NucleusMuteService}
     */
    public static Optional<NucleusMuteService> getMuteService() {
        return getService(NucleusMuteService.class);
    }

    /**
     * Gets the {@link NucleusNameBanService}, if it exists.
     *
     * <p>
     *     Requires the "nameban" module.
     * </p>
     *
     * @return The {@link NucleusNameBanService}
     */
    public static Optional<NucleusNameBanService> getNameBanService() {
        return getService(NucleusNameBanService.class);
    }


    /**
     * Gets the {@link NucleusNicknameService}, if it exists.
     *
     * <p>
     *     Requires the "nickname" module.
     * </p>
     *
     * @return The {@link NucleusNicknameService}
     */
    public static Optional<NucleusNicknameService> getNicknameService() {
        return getService(NucleusNicknameService.class);
    }


    /**
     * Gets the {@link NucleusNoteService}, if it exists.
     *
     * <p>
     *     Requires the "note" module.
     * </p>
     *
     * @return The {@link NucleusNoteService}
     */
    public static Optional<NucleusNoteService> getNoteService() {
        return getService(NucleusNoteService.class);
    }

    /**
     * Gets the {@link NucleusPrivateMessagingService}, if it exists.
     *
     * <p>
     *     Requires the "message" module.
     * </p>
     *
     * @return The {@link NucleusPrivateMessagingService}
     */
    public static Optional<NucleusPrivateMessagingService> getPrivateMessagingService() {
        return getService(NucleusPrivateMessagingService.class);
    }

    /**
     * Gets the {@link NucleusRTPService}, if it exists.
     *
     * <p>
     *     Requires the "rtp" module.
     * </p>
     *
     * @return The {@link NucleusRTPService}
     */
    public static Optional<NucleusRTPService> getRTPService() {
        return getService(NucleusRTPService.class);
    }

    /**
     * Gets the {@link NucleusSeenService}, if it exists.
     *
     * <p>
     *     Requires the "playerinfo" module.
     * </p>
     *
     * @return The {@link NucleusSeenService}
     */
    public static Optional<NucleusSeenService> getSeenService() {
        return getService(NucleusSeenService.class);
    }

    /**
     * Gets the {@link NucleusPlayerTeleporterService}, if it exists.
     *
     * <p>
     *     Requires the "teleport" module.
     * </p>
     *
     * @return The {@link NucleusPlayerTeleporterService}
     */
    public static Optional<NucleusPlayerTeleporterService> getTeleporterService() {
        return getService(NucleusPlayerTeleporterService.class);
    }

    /**
     * Gets the {@link NucleusStaffChatService}, if it exists.
     *
     * <p>
     *     Requires the "staff-chat" module.
     * </p>
     *
     * @return The {@link NucleusStaffChatService}
     */
    public static Optional<NucleusStaffChatService> getStaffChatService() {
        return getService(NucleusStaffChatService.class);
    }

    /**
     * Gets the {@link NucleusWarningService}, if it exists.
     *
     * <p>
     *     Requires the "warning" module.
     * </p>
     *
     * @return The {@link NucleusWarningService}
     */
    public static Optional<NucleusWarningService> getWarningService() {
        return getService(NucleusWarningService.class);
    }

    /**
     * Gets the {@link NucleusWarpService}, if it exists.
     *
     * <p>
     *     Requires the "warp" module.
     * </p>
     *
     * @return The {@link NucleusWarpService}
     */
    public static Optional<NucleusWarpService> getWarpService() {
        return getService(NucleusWarpService.class);
    }

    // A single point of failure means a single point to fix!
    private static <T> Optional<T> getService(Class<T> clazz) {
        return Sponge.getServiceManager().provide(clazz);
    }

}
