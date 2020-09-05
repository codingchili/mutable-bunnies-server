package com.codingchili.banking.model;

import com.codingchili.core.storage.AsyncStorage;

public class BankDB implements AsyncBankStore {
    private AsyncStorage<Inventory> bank;

    public BankDB(AsyncStorage<Inventory> bank) {
        this.bank = bank;
    }
}
