package com.codingchili.banking.controller;

import com.codingchili.banking.configuration.BankingContext;
import com.codingchili.banking.model.AuctionRequest;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Address;
import com.codingchili.core.protocol.Api;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.Roles;

import java.util.ArrayList;

import static com.codingchili.banking.model.AuctionResponse.Companion;
import static com.codingchili.common.Strings.AUCTION_NODE;
import static com.codingchili.core.protocol.RoleMap.PUBLIC;

@Roles(PUBLIC)
@Address(AUCTION_NODE)
public class AuctionHandler implements CoreHandler {
    private final Protocol<Request> protocol = new Protocol<>(this);
    private final AuctionServiceHandler handler;
    private final BankingContext context;
    private final Logger logger;

    public AuctionHandler(BankingContext context) {
        this.context = context;
        this.handler = new AuctionServiceHandler(context.vertx());
        this.logger = context.logger(getClass());

        protocol.authenticator(context.authenticator(context.clientTokens()));
    }

    @Api
    public void bid(AuctionRequest request) {
        request.write(
                Companion.auction(handler.bid(request.owner(), request.value(), request.auctionId()))
        );
    }

    @Api
    public void findById(AuctionRequest request) {
        request.write(
                Companion.auction(handler.findById(request.auctionId()))
        );
    }

    @Api
    public void inventory(AuctionRequest request) {
        request.write(
                Companion.inventory(handler.inventory(request.owner()))
        );
    }

    @Api
    public void notifications(AuctionRequest request) {
        request.write(
                Companion.notifications(handler.notifications(request.owner()))
        );
    }

    @Api
    public void favorite(AuctionRequest request) {
        request.write(
                handler.favorite(request.owner(), request.auctionId(), request.isFavorite())
        );
    }

    @Api
    public void favorites(AuctionRequest request) {
        request.write(
                Companion.auctions(new ArrayList<>(handler.favorites(request.owner())))
        );
    }

    @Api
    public void auction(AuctionRequest request) {
        request.write(
                Companion.auction(handler.auction(request.owner(), request.itemId(), request.value()))
        );
    }

    @Api
    public void search(AuctionRequest request) {
        request.write(
                Companion.auctions(handler.search(request.owner(), request.query(), request.param()))
        );
    }

    @Override
    public void handle(Request request) {
        logger.log(request.data().encodePrettily());
        protocol.process(new AuctionRequest(request));
    }
}