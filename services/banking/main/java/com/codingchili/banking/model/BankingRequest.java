package com.codingchili.banking.model;

import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.RequestWrapper;

public class BankingRequest implements RequestWrapper {
    private Request request;

    public BankingRequest(Request request) {
        this.request = request;
    }

    @Override
    public Request request() {
        return request;
    }
}
