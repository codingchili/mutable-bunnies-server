package com.codingchili.banking.configuration;

import com.codingchili.banking.model.*;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Authenticator;
import com.codingchili.core.protocol.Role;
import com.codingchili.core.protocol.RoleMap;
import com.codingchili.core.protocol.RoleType;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.core.storage.StorageLoader;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.function.Function;

/**
 *
 */
public class BankingContext extends SystemContext {
    private AsyncBankStore bank;
    private AsyncAuctionStore auctions;

    private BankingContext(CoreContext core) {
        super(core);
    }

    public BankingSettings settings() {
        return Configurations.get(BankingSettings.PATH, BankingSettings.class);
    }

    public Function<Request, Future<RoleType>> authenticator(TokenFactory tokens) {
        return (r) -> tokens.verify(r.token()).map(v -> Role.USER);
    }

    public static Future<BankingContext> create(CoreContext core) {
        var context = new BankingContext(core);
        var bank = Future.<AsyncStorage<Inventory>>future();
        var auctions = Future.<AsyncStorage<Auction>>future();

        new StorageLoader<Inventory>(context)
                .withPlugin(context.settings().getStorage())
                .withValue(Inventory.class)
                .build(bank);

        new StorageLoader<Auction>(context)
                .withPlugin(context.settings().getStorage())
                .withValue(Auction.class)
                .build(auctions);

        return CompositeFuture.all(bank, auctions).map(v -> {
            context.bank = new BankDB(bank.result());
            context.auctions = new AuctionDB(auctions.result());
            return context;
        });
    }

    public TokenFactory realmTokens() {
        return new TokenFactory(this, settings().getRealmSecret());
    }

    public TokenFactory clientTokens() {
        return new TokenFactory(this, settings().getClientSecret());
    }
}
