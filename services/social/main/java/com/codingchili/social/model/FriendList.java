package com.codingchili.social.model;

import java.util.ArrayList;
import java.util.List;

import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * A friend list storage object.
 */
public class FriendList implements Storable {
    private String account;
    private List<String> friends = new ArrayList<>();
    private List<String> requests = new ArrayList<>();

    public FriendList() {
    }

    public FriendList(String account) {
        this.account = account;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getRequests() {
        return requests;
    }

    public void setRequests(List<String> requests) {
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
        requests.add(from);
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
