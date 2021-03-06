/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.config;

import io.github.nucleuspowered.nucleus.core.services.interfaces.annotation.configuratehelper.LocalisedComment;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class AFKConfig {

    @Setting(value = "afk-time")
    @LocalisedComment("config.afk.time")
    private long afkTime = 300;

    @Setting(value = "afk-time-to-kick")
    @LocalisedComment("config.afk.timetokick")
    private long afkTimeToKick = 0;

    @Setting(value = "broadcast-afk-when-vanished")
    @LocalisedComment("config.afk.whenvanished")
    private boolean broadcastAfkOnVanish = false;

    @Setting(value = "alert-on-command-send")
    @LocalisedComment("config.afk.oncommandsend")
    private boolean alertSenderOnAfk = true;

    @Setting(value = "broadcast-to-all-on-kick")
    @LocalisedComment("config.afk.broadcastonkick")
    private boolean broadcastOnKick = true;

    @Setting(value = "messages")
    @LocalisedComment("config.afk.messages.base")
    private MessagesConfig messages = new MessagesConfig();

    @Setting(value = "triggers")
    @LocalisedComment("config.afk.triggers.summary")
    private Triggers triggers = new Triggers();

    @Setting(value = "disable-in-spectator-mode")
    @LocalisedComment("config.afk.spectatormode")
    private boolean disableInSpectatorMode = false;

    public Triggers getTriggers() {
        return this.triggers;
    }

    public long getAfkTime() {
        return this.afkTime;
    }

    public long getAfkTimeToKick() {
        return this.afkTimeToKick;
    }

    public boolean isBroadcastAfkOnVanish() {
        return this.broadcastAfkOnVanish;
    }

    public boolean isAlertSenderOnAfk() {
        return this.alertSenderOnAfk;
    }

    public boolean isBroadcastOnKick() {
        return this.broadcastOnKick;
    }

    public MessagesConfig getMessages() {
        return this.messages;
    }

    public boolean isDisableInSpectatorMode() {
        return this.disableInSpectatorMode;
    }

    @ConfigSerializable
    public static class Triggers {

        @Setting(value = "on-chat")
        @LocalisedComment("config.afk.triggers.onchat")
        private boolean onChat = true;

        @Setting(value = "on-command")
        @LocalisedComment("config.afk.triggers.oncommand")
        private boolean onCommand = true;

        @Setting(value = "on-movement")
        @LocalisedComment("config.afk.triggers.onmove")
        private boolean onMovement = true;

        @Setting(value = "on-rotation")
        @LocalisedComment("config.afk.triggers.onrotation")
        private boolean onRotation = true;

        @Setting(value = "on-interact")
        @LocalisedComment("config.afk.triggers.oninteract")
        private boolean onInteract = true;

        public boolean isOnChat() {
            return this.onChat;
        }

        public boolean isOnCommand() {
            return this.onCommand;
        }

        public boolean isOnMovement() {
            return this.onMovement;
        }

        public boolean isOnRotation() {
            return this.onRotation;
        }

        public boolean isOnInteract() {
            return this.onInteract;
        }
    }
}
