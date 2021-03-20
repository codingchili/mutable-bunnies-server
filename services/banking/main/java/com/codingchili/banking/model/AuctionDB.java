package com.codingchili.banking.model;

import com.codingchili.core.storage.AsyncStorage;

public class AuctionDB implements AsyncAuctionStore {
    private AsyncStorage<Auction> auctions;

    public AuctionDB(AsyncStorage<Auction> auctions) {
        this.auctions = auctions;
    }
}
