package com.codingchili.social.model;

import com.codingchili.social.configuration.SocialContext;

import java.util.*;

import com.codingchili.core.context.CoreContext;

/**
 * @author Robin Duda
 *
 * Tracks accounts that are online for friendlists and chat.
 */
public class OnlineDB {
    private CoreContext context;
    private Map<String, Set<String>> connected = new HashMap<>();

    /**
     *
     * @param context
     */
    public OnlineDB(CoreContext context) {
        this.context = context;
    }

    /**
     *
     * @param account
     * @param realm
     */
    public void add(String account, String realm) {
        connected.computeIfAbsent(account, key -> new HashSet<>());
        connected.get(account).add(realm);
    }

    /**
     *
     * @param account
     * @param realm
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
     *
     * @param account
     * @return
     */
    public boolean is(String account) {
        return connected.containsKey(account);
    }

    /**
     *
     * @param account
     * @return
     */
    public Set<String> realms(String account) {
        return connected.getOrDefault(account, new HashSet<>());
    }
}
