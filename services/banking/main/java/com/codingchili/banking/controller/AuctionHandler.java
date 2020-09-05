package com.codingchili.banking.controller;

import com.codingchili.banking.configuration.BankingContext;
import com.codingchili.banking.model.AuctionRequest;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Address;
import com.codingchili.core.protocol.Api;
import com.codingchili.core.protocol.Protocol;

import static com.codingchili.common.Strings.AUCTION_NODE;

@Address(AUCTION_NODE)
public class AuctionHandler implements CoreHandler {
    private Protocol<Request> protocol = new Protocol<>(this);
    private BankingContext context;

    public AuctionHandler(BankingContext context) {
        this.context = context;
        protocol.authenticator(context.authenticator(context.clientTokens()));
    }

    @Api
    public void auction(AuctionRequest request) {
        request.accept();
    }

    @Api
    public void search(AuctionRequest request) {

    }

    @Override
    public void handle(Request request) {
        protocol.process(new AuctionRequest(request));
    }
}
