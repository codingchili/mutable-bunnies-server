package com.codingchili.banking.model;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;
import com.codingchili.core.protocol.Serializer;

public class AuctionRequest implements RequestWrapper {
    private Request request;

    public AuctionRequest(Request request) {
        this.request = request;
    }

    public String itemId() {
        return data().getString("item");
    }

    public Integer value() {
        return data().getInteger("value");
    }

    public QueryType query() {
        return Serializer.unpack(data().getString("query"), QueryType.class);
    }

    @Override
    public Request request() {
        return request;
    }
}
