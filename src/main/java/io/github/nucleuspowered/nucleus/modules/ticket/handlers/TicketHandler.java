/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ticket.handlers;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Ticket;
import io.github.nucleuspowered.nucleus.api.service.NucleusTicketService;
import io.github.nucleuspowered.nucleus.modules.ticket.data.TicketData;
import io.github.nucleuspowered.nucleus.modules.ticket.data.TicketDataManager;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.api.entity.living.player.User;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TicketHandler implements NucleusTicketService {
    private final NucleusPlugin nucleus;
    @Inject private TicketDataManager ticketDataManager;

    public TicketHandler(NucleusPlugin nucleus) {
        this.nucleus = nucleus;
    }

    @Override
    public CompletableFuture<Optional<Ticket>> getTicketWithID(int id) {
        try {
            return ticketDataManager.getCache().lookupTicketByID(id);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return CompletableFuture.completedFuture(Optional.empty()); //Failed
    }

    @Override
    public CompletableFuture<List<Ticket>> getTicketsForOwner(User user) {
        try {
            return ticketDataManager.getCache().lookupTicketsByOwner(user.getUniqueId());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return CompletableFuture.completedFuture(Lists.newArrayList()); //Failed
    }

    @Override
    public CompletableFuture<List<Ticket>> getTicketsForAssignee(User user) {
        try {
            return ticketDataManager.getCache().lookupTicketsByOwner(user.getUniqueId());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return CompletableFuture.completedFuture(Lists.newArrayList()); //Failed
    }

    @Override
    public CompletableFuture<List<Ticket>> getTicketsByStatus(boolean closed) {
        try {
            return ticketDataManager.getCache().lookupTicketsByStatus(closed);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return CompletableFuture.completedFuture(Lists.newArrayList()); //Failed
    }

    @Override
    public CompletableFuture<List<Ticket>> getTicketsByArguments(List<Triple<String, String, String>> arguments) {
        try {
            return ticketDataManager.getCache().lookupTicket(arguments);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return CompletableFuture.completedFuture(Lists.newArrayList()); //Failed
    }

    @Override
    public boolean createTicket(User creator, String initialMessage) {
        TicketData ticketData = new TicketData(creator.getUniqueId(), initialMessage);

        try {
            ticketDataManager.createTicket(ticketData);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addMessageByTicketID(int id, String message) {
        final Instant creationDate = Instant.now();
        try {
            CompletableFuture<Boolean> future = ticketDataManager.getCache().lookupTicketByID(id)
                    .thenApply(ticket -> {
                        try {
                            if (!ticket.isPresent()) return false;
                            ticketDataManager.createTicketMessage(ticket.get(), creationDate, message);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            return false;
                        }

                        return true;
                    });
            return future.get();
        } catch (SQLException | ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}