/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.services.target;

import io.github.nucleuspowered.nucleus.api.module.message.target.MessageTarget;
import io.github.nucleuspowered.nucleus.api.module.message.target.SystemMessageTarget;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;

import java.util.Optional;

public final class SystemSubjectMessageTarget extends AbstractMessageTarget implements SystemMessageTarget {

    private final Component serverDisplayName;

    public SystemSubjectMessageTarget(final Component serverDisplayName) {
        this.serverDisplayName = serverDisplayName;
    }

    @Override
    public Optional<Audience> getRepresentedAudience() {
        return Optional.of(Sponge.systemSubject());
    }

    @Override
    public Component getDisplayName() {
        return this.serverDisplayName;
    }

    @Override
    public void receiveMessageFrom(final MessageTarget messageTarget, final Component message) {
        this.setReplyTarget(messageTarget);
        Sponge.systemSubject().sendMessage(this.getIdentity(messageTarget), message);
    }

    @Override
    public boolean isAvailableForMessages() {
        return true;
    }

    @Override
    public void pushCauseToFrame(final CauseStackManager.StackFrame frame) {
        frame.pushCause(Sponge.systemSubject());
    }

    @Override
    public boolean canBypassMessageToggle() {
        return true;
    }
}
