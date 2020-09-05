package com.codingchili.banking.configuration;

import com.codingchili.core.configuration.Configurable;

import static com.codingchili.core.configuration.CoreStrings.getService;

public class BankingSettings implements Configurable {
    public static final String PATH = getService("banking");
    private String storage;
    private byte[] realmSecret;
    private byte[] clientSecret;

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public byte[] getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(byte[] clientSecret) {
        this.clientSecret = clientSecret;
    }

    public byte[] getRealmSecret() {
        return realmSecret;
    }

    public void setRealmSecret(byte[] realmSecret) {
        this.realmSecret = realmSecret;
    }

    @Override
    public String getPath() {
        return PATH;
    }
}
