/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.handlers;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;
import io.github.nucleuspowered.nucleus.api.util.NoExceptionAutoClosable;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.ServiceChangeListener;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import io.github.nucleuspowered.nucleus.modules.afk.commands.AFKCommand;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.events.AFKEvents;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.Tuple;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.concurrent.GuardedBy;

public class AFKHandler implements NucleusAFKService, Reloadable {

    private final Map<UUID, AFKData> data = Maps.newConcurrentMap();
    private final AFKConfigAdapter afkConfigAdapter;
    private final CommandPermissionHandler afkPermissionHandler;
    private AFKConfig config;

    @GuardedBy("lock")
    private final Set<UUID> activity = Sets.newHashSet();

    @GuardedBy("lock2")
    private final Multimap<UUID, UUID> disabledTracking = HashMultimap.create();
    private final Object lock = new Object();
    private final Object lock2 = new Object();

    private final String exempttoggle = "exempt.toggle";
    private final String exemptkick = "exempt.kick";

    private final String afkOption = "nucleus.afk.toggletime";
    private final String afkKickOption = "nucleus.afk.kicktime";

    public AFKHandler() {
        this.afkPermissionHandler = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(AFKCommand.class);
        this.afkConfigAdapter = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKConfigAdapter.class);
    }

    public void stageUserActivityUpdate(Player player) {
        if (player.isOnline()) {
            stageUserActivityUpdate(player.getUniqueId());
        }
    }

    private void stageUserActivityUpdate(UUID uuid) {
        synchronized (lock) {
            synchronized (lock2) {
                if (this.disabledTracking.containsKey(uuid)) {
                    return;
                }
            }

            activity.add(uuid);
        }
    }

    public void onTick() {
        synchronized (lock) {
            activity.forEach(u -> data.compute(u, ((uuid, afkData) -> afkData == null ? new AFKData(uuid) : updateActivity(uuid, afkData))));
            activity.clear();
        }

        List<UUID> uuidList = Sponge.getServer().getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList());

        // Remove all offline players.
        Set<Map.Entry<UUID, AFKData>> entries = data.entrySet();
        entries.removeIf(refactor -> !uuidList.contains(refactor.getKey()));
        entries.stream().filter(x -> !x.getValue().cacheValid).forEach(x -> x.getValue().updateFromPermissions());

        long now = System.currentTimeMillis();

        // Check AFK status.
        entries.stream().filter(x -> x.getValue().isKnownAfk && !x.getValue().willKick && x.getValue().timeToKick > 0).forEach(e -> {
            if (now - e.getValue().lastActivityTime > e.getValue().timeToKick) {
                // Kick them
                e.getValue().willKick = true;
                NucleusTextTemplateImpl message = config.getMessages().getKickMessage();
                TextRepresentable t;
                if (message.isEmpty()) {
                    t = Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("afk.kickreason");
                } else {
                    t = message;
                }

                final NucleusTextTemplateImpl messageToServer = config.getMessages().getOnKick();

                Sponge.getServer().getPlayer(e.getKey()).ifPresent(player -> {
                    MessageChannel mc;
                    if (config.isBroadcastOnKick()) {
                        mc = MessageChannel.TO_ALL;
                    } else {
                        mc = MessageChannel.permission(this.afkPermissionHandler.getPermissionWithSuffix("notify"));
                    }

                    AFKEvents.Kick events = new AFKEvents.Kick(player, messageToServer.getForCommandSource(player), mc);
                    if (Sponge.getEventManager().post(events)) {
                        // Cancelled.
                        return;
                    }

                    Text toSend = t instanceof NucleusTextTemplateImpl ? ((NucleusTextTemplateImpl) t).getForCommandSource(player) : t.toText();
                    Sponge.getScheduler().createSyncExecutor(Nucleus.getNucleus()).execute(() -> player.kick(toSend));
                    events.getMessage().ifPresent(m -> events.getChannel().send(player, m, ChatTypes.SYSTEM));
                });
            }
        });

        // Check AFK status.
        entries.stream().filter(x -> !x.getValue().isKnownAfk && x.getValue().timeToAfk > 0).forEach(e -> {
            if (now - e.getValue().lastActivityTime  > e.getValue().timeToAfk) {
                Sponge.getServer().getPlayer(e.getKey()).ifPresent(this::setAfk);
            }
        });
    }

    public void invalidateAfkCache() {
        data.forEach((k, v) -> v.cacheValid = false);
    }

    public boolean isAfk(Player player) {
        return isAfk(player.getUniqueId());
    }

    public boolean isAfk(UUID uuid) {
        return data.containsKey(uuid) && data.get(uuid).isKnownAfk;
    }

    public boolean setAfk(Player player) {
        return setAfk(player, CauseStackHelper.createCause(player), false);
    }

    public boolean setAfk(Player player, Cause cause, boolean force) {
        if (!player.isOnline()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        AFKData a = data.compute(uuid, ((u, afkData) -> afkData == null ? new AFKData(u) : afkData));
        if (force) {
            a.isKnownAfk = false;
        } else if (a.isKnownAfk) {
            return false;
        }

        if (a.canGoAfk()) {
            // Don't accident undo setting AFK, remove any activity from the list.
            synchronized (lock) {
                activity.remove(uuid);
            }

            Tuple<Text, MessageChannel> ttmc = getAFKMessage(player, true);
            AFKEvents.To event = new AFKEvents.To(player, ttmc.getFirst(), ttmc.getSecond(), cause);
            Sponge.getEventManager().post(event);
            actionEvent(event, "command.afk.to.vanish");

            a.isKnownAfk = true;
            return true;
        }

        return false;
    }

    @Override
    public void onReload() {
        this.config = this.afkConfigAdapter.getNodeOrDefault();
    }

    private AFKData updateActivity(UUID uuid, AFKData data) {
        List<Object> lo = Lists.newArrayList();
        Sponge.getServer().getPlayer(uuid).ifPresent(lo::add);
        return updateActivity(uuid, data, CauseStackHelper.createCause(lo));
    }

    private AFKData updateActivity(UUID uuid, AFKData data, Cause cause) {
        data.lastActivityTime = System.currentTimeMillis();
        if (data.isKnownAfk) {
            data.isKnownAfk = false;
            data.willKick = false;
            Sponge.getServer().getPlayer(uuid).ifPresent(x -> {
                Tuple<Text, MessageChannel> ttmc = getAFKMessage(x, false);
                AFKEvents.From event = new AFKEvents.From(x, ttmc.getFirst(), ttmc.getSecond(), cause);
                Sponge.getEventManager().post(event);
                actionEvent(event, "command.afk.from.vanish");
            });

        }

        return data;
    }

    private void actionEvent(AFKEvents event, String key) {
        if (event.getMessage().isPresent()) {
            event.getChannel().send(event.getTargetEntity(), event.getMessage().get(), ChatTypes.SYSTEM);
        } else {
            event.getTargetEntity().sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat(key));
        }
    }

    private Tuple<Text, MessageChannel> getAFKMessage(Player player, boolean isAfk) {
        if (config.isAfkOnVanish() || !player.get(Keys.VANISH).orElse(false)) {
            NucleusTextTemplateImpl template = isAfk ? config.getMessages().getAfkMessage() : config.getMessages().getReturnAfkMessage();
            return Tuple.of(template.getForCommandSource(player), MessageChannel.TO_ALL);
        } else {
            // Tell the user in question about them going AFK
            // player.sendMessage(
            //        NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat(isAfk ? "command.afk.to.vanish" : "command.afk.from.vanish"));
            return Tuple.of(Text.EMPTY, MessageChannel.TO_NONE);
        }
    }

    @Override public boolean canGoAFK(User user) {
        return getData(user.getUniqueId()).canGoAfk();
    }

    @Override public boolean isAFK(Player player) {
        return this.data.computeIfAbsent(player.getUniqueId(), AFKData::new).isKnownAfk;
    }

    @Override public boolean setAFK(Cause cause, Player player, boolean isAfk) {
        Preconditions.checkArgument(cause.root() instanceof PluginContainer, "The root object MUST be a plugin container.");
        AFKData data = this.data.computeIfAbsent(player.getUniqueId(), AFKData::new);
        if (data.isKnownAfk == isAfk) {
            // Already AFK
            return false;
        }

        if (isAfk) {
            return setAfk(player, cause, false);
        } else {
            return !updateActivity(player.getUniqueId(), data, cause).isKnownAfk;
        }
    }

    @Override public boolean canBeKicked(User user) {
        return getData(user.getUniqueId()).canBeKicked();
    }

    @Override public Instant lastActivity(Player player) {
        return Instant.ofEpochMilli(this.data.computeIfAbsent(player.getUniqueId(), AFKData::new).lastActivityTime);
    }

    @Override public Optional<Duration> timeForInactivity(User user) {
        AFKData data = getData(user.getUniqueId());
        if (data.canGoAfk()) {
            return Optional.of(Duration.ofMillis(data.timeToAfk));
        }

        return Optional.empty();
    }

    @Override public Optional<Duration> timeForKick(User user) {
        AFKData data = getData(user.getUniqueId());
        if (data.canBeKicked()) {
            return Optional.of(Duration.ofMillis(data.timeToKick));
        }

        return Optional.empty();
    }

    @Override public void invalidateCachedPermissions() {
        invalidateAfkCache();
    }

    @Override public void updateActivityForUser(Player player) {
        stageUserActivityUpdate(player);
    }

    @Override public NoExceptionAutoClosable disableTrackingForPlayer(final Player player, int ticks) {
        // Disable tracking now with a new UUID.
        Task n = Task.builder().execute(t -> {
            synchronized (lock2) {
                disabledTracking.remove(player.getUniqueId(), t.getUniqueId());
            }
        }).delayTicks(ticks).submit(Nucleus.getNucleus());

        synchronized (lock2) {
            disabledTracking.put(player.getUniqueId(), n.getUniqueId());
        }

        return () -> {
            n.cancel();
            n.getConsumer().accept(n);
        };
    }

    private AFKData getData(UUID uuid) {
        AFKData data = this.data.get(uuid);
        if (data == null) {
            // Prevent more checks
            data = new AFKData(uuid, false);
        }

        return data;
    }

    class AFKData {

        private final UUID uuid;

        private long lastActivityTime = System.currentTimeMillis();
        private boolean isKnownAfk = false;
        private boolean willKick = false;

        private boolean cacheValid = false;
        private long timeToAfk = -1;
        private long timeToKick = -1;

        private AFKData(UUID uuid) {
            this(uuid, true);
        }

        private AFKData(UUID uuid, boolean permCheck) {
            this.uuid = uuid;
            if (permCheck) {
                updateFromPermissions();
            }
        }

        private boolean canGoAfk() {
            cacheValid = false;
            updateFromPermissions();
            return timeToAfk > 0;
        }

        private boolean canBeKicked() {
            cacheValid = false;
            updateFromPermissions();
            return timeToKick > 0;
        }

        void updateFromPermissions() {
            synchronized (this) {
                if (!cacheValid) {
                    // Get the subject.
                    Sponge.getServer().getPlayer(uuid).ifPresent(x -> {
                        if (!ServiceChangeListener.isOpOnly() && AFKHandler.this.afkPermissionHandler.testSuffix(x, exempttoggle)) {
                            timeToAfk = -1;
                        } else {
                            timeToAfk = Util.getPositiveLongOptionFromSubject(x, afkOption).orElseGet(() -> config.getAfkTime()) * 1000;
                        }

                        if (AFKHandler.this.afkPermissionHandler.testSuffix(x, exemptkick)) {
                            timeToKick = -1;
                        } else {
                            timeToKick = Util.getPositiveLongOptionFromSubject(x, afkKickOption).orElseGet(() -> config.getAfkTimeToKick()) * 1000;
                        }

                        cacheValid = true;
                    });
                }
            }
        }
    }
}
