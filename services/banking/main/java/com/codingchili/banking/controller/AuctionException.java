package com.codingchili.banking.controller;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.protocol.ResponseStatus;

/**
 *
 */
public class AuctionException extends CoreRuntimeException {
    public AuctionException(String description) {
        super(description);
    }

    @Override
    public ResponseStatus status() {
        return ResponseStatus.ERROR;
    }
}
