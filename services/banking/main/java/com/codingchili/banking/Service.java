package com.codingchili.banking;


import com.codingchili.banking.configuration.BankingContext;
import com.codingchili.banking.controller.AuctionHandler;
import com.codingchili.banking.controller.BankingHandler;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.listener.CoreService;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

public class Service implements CoreService {
    private CoreContext core;

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public void start(Future<Void> start) {
        var auctions = Future.future();
        var banking = Future.future();

        BankingContext.create(core).setHandler(context -> {
            core.handler(() -> new AuctionHandler(context.result()))
                    .mapEmpty()
                    .setHandler(auctions);

            core.handler(() -> new BankingHandler(context.result()))
                    .mapEmpty()
                    .setHandler(banking);
        });
        
        CompositeFuture.all(auctions, banking)
                .<Void>mapEmpty()
                .setHandler(start);
    }
}
