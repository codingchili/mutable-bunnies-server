package com.codingchili.banking.model;

import com.codingchili.core.storage.AsyncStorage;

// text search
// seller search
// navleaf searches ..
   // favorites, active, sold, not sold, won, leading, overbid, lost

public class AuctionDB implements AsyncAuctionStore {
    private AsyncStorage<Auction> auctions;

    public AuctionDB(AsyncStorage<Auction> auctions) {
        this.auctions = auctions;
    }

    public void foo() {

    }
}
