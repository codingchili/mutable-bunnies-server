package com.codingchili.social.model;

import java.util.*;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * A friend list storage object.
 */
public class FriendList implements Storable {
    private String account;
    private transient Map<String, Set<String>> online = new HashMap<>();
    private Set<String> friends = new HashSet<>();
    private Set<String> requests = new HashSet<>();

    public FriendList() {
    }

    public FriendList(String account) {
        this.account = account;
    }

    public Set<String> getFriends() {
        return friends;
    }

    public void setFriends(Set<String> friends) {
        this.friends = friends;
    }

    public Set<String> getRequests() {
        return requests;
    }

    public void setRequests(Set<String> requests) {
        this.requests = requests;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String getId() {
        return account;
    }

    public void request(String from) {
        if (!friends.contains(from) && !account.equals(from)) {
            requests.add(from);
        }
    }

    public void reject(String from) {
        requests.remove(from);
    }

    public boolean accept(String friend) {
        if (requests.remove(friend)) {
            friends.add(friend);
            return true;
        } else {
            return false;
        }
    }

    public Map<String, Set<String>> getOnline() {
        return online;
    }

    public void setOnline(Map<String, Set<String>> online) {
        this.online = online;
    }

    public void online(String account, Set<String> realm) {
        this.online.put(account, realm);
    }

    public boolean isFriend(String other) {
        return friends.contains(other);
    }

    public void remove(String friend) {
        friends.remove(friend);
    }

    public void accepted(String account) {
        friends.add(account);
    }

    @Override
    public String toString() {
        return account +
                "\r\nrequested: " + String.join(",", requests) +
                "\nfriends: " + String.join(",", friends);
    }

    @Override
    public int hashCode() {
        return account.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FriendList) {
            return ((FriendList) other).account.equals(account);
        } else {
            return false;
        }
    }
}
