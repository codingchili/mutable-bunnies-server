package com.codingchili.banking.controller;

import com.codingchili.banking.configuration.BankingContext;
import com.codingchili.banking.model.BankingRequest;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.*;
import io.vertx.core.Future;

import static com.codingchili.common.Strings.BANKING_NODE;

/**
 * Handles item desposits into the account-wide bank.
 * This handler must not be callable directly by clients - only by
 * realms to prevent player from placing arbitrary items in inventory.
 */
@Address(BANKING_NODE)
public class BankingHandler implements CoreHandler {
    private Protocol<Request> protocol = new Protocol<>(this);
    private BankingContext context;

    public BankingHandler(BankingContext context) {
        this.context = context;
        protocol.authenticator(context.authenticator(context.realmTokens()));
    }

    @Api
    public void deposit(BankingRequest request) {
        request.accept();
    }

    @Api
    public void withdraw(BankingRequest request) {
        request.accept();
    }

    @Api
    public void inventory(BankingRequest request) {
        request.accept();
    }

    @Override
    public void handle(Request request) {
        protocol.process(new BankingRequest(request));
    }
}
