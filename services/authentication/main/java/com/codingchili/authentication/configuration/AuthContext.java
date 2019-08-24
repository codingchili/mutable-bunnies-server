package com.codingchili.authentication.configuration;

import com.codingchili.authentication.model.*;
import io.vertx.core.Future;

import com.codingchili.core.context.*;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.security.*;
import com.codingchili.core.storage.StorageLoader;

import static com.codingchili.authentication.configuration.AuthenticationSettings.PATH_AUTHSERVER;
import static com.codingchili.common.Strings.*;
import static com.codingchili.core.logging.Level.WARNING;

/**
 * @author Robin Duda
 * <p>
 * Authentication service context.
 */
public class AuthContext extends SystemContext implements ServiceContext {
    private static final String LOG_ACCOUNT = "account";
    private AsyncAccountStore accounts;
    private Logger logger;

    public AuthContext(CoreContext core) {
        super(core);
        this.logger = core.logger(getClass());
    }

    public static void create(Future<AuthContext> future, CoreContext core) {
        AuthContext context = new AuthContext(core);
        new StorageLoader<AccountMapping>(context)
                .withPlugin(context.service().getStorage())
                .withCollection(COLLECTION_ACCOUNTS)
                .withValue(AccountMapping.class)
                .build(prepare -> {
                    context.accounts = new AccountDB(prepare.result(), context);
                    future.complete(context);
                });
    }

    public AsyncAccountStore getAccountStore() {
        return accounts;
    }

    public Future<Void> verifyClientToken(Token token) {
        return new TokenFactory(this, service().getClientSecret()).verify(token);
    }

    public Future<Token> signClientToken(String domain) {
        Token token = new Token(domain);
        return new TokenFactory(this, service().getClientSecret()).hmac(token).map(token);
    }

    public AuthenticationSettings service() {
        return Configurations.get(PATH_AUTHSERVER, AuthenticationSettings.class);
    }

    public void onAuthenticationFailure(Account account, String host) {
        logger.event(LOG_ACCOUNT_UNAUTHORIZED, WARNING)
                .put(LOG_ACCOUNT, account.getUsername())
                .put(LOG_REMOTE, host).send();
    }

    public void onAuthenticated(String username, String host) {
        logger.event(LOG_ACCOUNT_AUTHENTICATED)
                .put(LOG_ACCOUNT, username)
                .put(LOG_REMOTE, host).send();
    }

    public void onRegistered(String username, String host) {
        logger.event(LOG_ACCOUNT_REGISTERED)
                .put(LOG_ACCOUNT, username)
                .put(LOG_REMOTE, host).send();
    }
}