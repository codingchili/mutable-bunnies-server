package com.codingchili.social.configuration;

import com.codingchili.core.configuration.Configurable;
import com.codingchili.core.security.Token;

import static com.codingchili.core.configuration.CoreStrings.getService;

/**
 * @author Robin Duda
 *
 * Settings for the social service.
 */
public class SocialSettings implements Configurable {
    public static final String PATH = getService("social");
    private String storage;
    private Token logging;
    private byte[] clientSecret;

    public Token getLogging() {
        return logging;
    }

    public void setLogging(Token logging) {
        this.logging = logging;
    }

    public byte[] getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(byte[] clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    @Override
    public String getPath() {
        return PATH;
    }
}
