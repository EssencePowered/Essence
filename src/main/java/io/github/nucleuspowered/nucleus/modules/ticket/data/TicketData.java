/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ticket.data;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Ticket;

import java.time.Instant;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

public class TicketData implements Ticket {
    private int id;
    private UUID owner;
    private UUID assignee;
    private long creationDate;
    private long lastUpdateDate;
    private TreeMap<Long, String> messages;
    private boolean closed;

    public TicketData() { }

    public TicketData(UUID owner, String initialMessage) {
        this(-1, owner, initialMessage);
    }

    public TicketData(int id, UUID owner, String initialMessage) {
        this(id, owner, null, Instant.now().toEpochMilli(), Instant.now().toEpochMilli(), Maps.newTreeMap(), false);
        messages.put(Instant.now().toEpochMilli(), initialMessage);
    }

    public TicketData(int id, UUID owner, UUID assignee, long creationDate, long lastUpdateDate, TreeMap<Long, String> messages, boolean closed) {
        this.id = id;
        this.owner = owner;
        this.assignee = assignee;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.messages = messages;
        this.closed = closed;
    }

    public int getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public Optional<UUID> getAssignee() {
        return Optional.ofNullable(assignee);
    }

    public Instant getCreationDate() {
        return Instant.ofEpochMilli(creationDate);
    }

    public Instant getLastUpdateDate() {
        return Instant.ofEpochMilli(lastUpdateDate);
    }

    public TreeMap<Long, String> getMessages() {
        return messages;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setAssignee(UUID assignee) {
        this.assignee = assignee;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate.toEpochMilli();
    }

    public void setLastUpdateDate(Instant lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate.toEpochMilli();
    }

    public void setMessages(TreeMap<Long, String> messages) {
        this.messages = messages;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}