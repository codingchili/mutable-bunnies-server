package com.codingchili.banking;


import com.codingchili.banking.configuration.BankingContext;
import com.codingchili.banking.controller.AuctionHandler;
import com.codingchili.banking.controller.BankingHandler;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import com.codingchili.core.protocol.Serializer;

import com.fasterxml.jackson.module.kotlin.KotlinModule;
import io.vertx.core.*;

public class Service implements CoreService {
    private CoreContext core;

    @Override
    public void init(CoreContext core) {
        this.core = core;
        Serializer.json.registerModule(new KotlinModule());
    }

    @Override
    public void start(Promise<Void> start) {
        var auctions = Promise.promise();
        var banking = Promise.promise();

        BankingContext.create(core).onComplete(context -> {
            core.handler(() -> new AuctionHandler(context.result()))
                    .mapEmpty()
                    .onComplete(auctions);

            core.handler(() -> new BankingHandler(context.result()))
                    .mapEmpty()
                    .onComplete(banking);
        });
        
        CompositeFuture.all(auctions.future(), banking.future())
                .<Void>mapEmpty()
                .onComplete(start);
    }
}
