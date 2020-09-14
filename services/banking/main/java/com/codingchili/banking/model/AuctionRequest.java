package com.codingchili.banking.model;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;

public class AuctionRequest implements RequestWrapper {
    private Request request;

    public AuctionRequest(Request request) {
        this.request = request;
    }

    public String itemId() {
        return data().getString("itemId");
    }

    public String auctionId() {
        return data().getString("auctionId");
    }

    public String param() {
        return data().getString("params");
    }

    public Boolean isFavorite() {
        return data().getBoolean("add");
    }

    public Integer value() {
        return data().getInteger("value");
    }

    public QueryType query() {
        return QueryType.valueOf(data().getString("query"));
    }

    public String owner() {
        return token().getDomain();
    }

    @Override
    public Request request() {
        return request;
    }
}
