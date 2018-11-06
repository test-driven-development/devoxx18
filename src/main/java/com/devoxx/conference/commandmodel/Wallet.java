package com.devoxx.conference.commandmodel;

import com.devoxx.conference.coreapi.CreateWalletCommand;
import com.devoxx.conference.coreapi.DepositCommand;
import com.devoxx.conference.coreapi.DepositedEvent;
import com.devoxx.conference.coreapi.NotEnoughFundsException;
import com.devoxx.conference.coreapi.WalletCreatedEvent;
import com.devoxx.conference.coreapi.WithdrawCommand;
import com.devoxx.conference.coreapi.WithdrawnEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class Wallet {

    @AggregateIdentifier
    private String walletId;
    private int balance;

    public Wallet() {
    }

    @CommandHandler
    public Wallet(CreateWalletCommand command) {
        AggregateLifecycle.apply(new WalletCreatedEvent(command.getWalletId(), command.getBalance()));
    }

    @CommandHandler
    public void handle(DepositCommand command) {
        AggregateLifecycle.apply(new DepositedEvent(walletId, command.getAmount()));
    }

    @CommandHandler
    public void handle(WithdrawCommand command) throws NotEnoughFundsException {
        int amount = command.getAmount();
        if (balance - amount < 0) {
            throw new NotEnoughFundsException();
        }
        AggregateLifecycle.apply(new WithdrawnEvent(walletId, amount));
    }

    @EventSourcingHandler
    public void on(WalletCreatedEvent event) {
        walletId = event.getWalletId();
        balance = event.getBalance();
    }

    @EventSourcingHandler
    public void on(DepositedEvent event) {
        balance = balance + event.getAmount();
    }

    @EventSourcingHandler
    public void on(WithdrawnEvent event) {
        balance = balance - event.getAmount();
    }
}
