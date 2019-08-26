package com.codingchili.social.model;

import java.util.*;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 * <p>
 * Tracks accounts that are online for friendlists and chat.
 */
public class OnlineDB {
    private CoreContext context;
    private Map<String, Set<String>> connected = new HashMap<>();

    /**
     * @param context creates the database for the given context.
     */
    public OnlineDB(CoreContext context) {
        this.context = context;
    }

    /**
     * Adds a new account to the online database. If the account already is online
     * then the realm will be added to their online presence.
     *
     * @param account the account to indicate as online.
     * @param realm   the realm for which the account is online.
     */
    public void add(String account, String realm) {
        connected.computeIfAbsent(account, key -> new HashSet<>());
        connected.get(account).add(realm);
    }

    /**
     * Removes an accounts online presence from the given realm.
     *
     * @param account the account to be removed.
     * @param realm   the realm that the account is to be removed from.
     */
    public void remove(String account, String realm) {
        Set<String> realms = connected.getOrDefault(account, new HashSet<>());
        realms.remove(realm);

        if (realms.isEmpty()) {
            connected.remove(account);
        } else {
            connected.put(account, realms);
        }
    }

    /**
     * @param account the account to check if online or not.
     * @return true if the account is online on at least one realm.
     */
    public boolean is(String account) {
        return connected.containsKey(account);
    }

    /**
     * Retrieves a set of all of the realms that the given account is considered online for.
     *
     * @param account the account to retrieve the online realms for.
     * @return a set of realm names.
     */
    public Set<String> realms(String account) {
        return connected.getOrDefault(account, new HashSet<>());
    }
}
