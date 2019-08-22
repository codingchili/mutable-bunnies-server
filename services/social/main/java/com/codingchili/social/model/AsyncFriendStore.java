package com.codingchili.social.model;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 * <p>
 * Interface for a friend list store.
 */
public interface AsyncFriendStore {

    /**
     * Sends a friend request.
     *
     * @param from the id of the account sending the friend request.
     * @param to   the id of the account receiving the friend request.
     * @return future completed with the new state of the friendlist.
     */
    Future<Void> request(String from, String to);

    /**
     * @param account the id of the account accepting the friend request.
     * @param friend  the id of the account requesting to be friends.
     * @return future completed with the new state of the friendlist.
     */
    Future<FriendList> accept(String account, String friend);

    /**
     * @param account   the id of the account rejecting the friend request.
     * @param requestor the id of the account that requested friendship.
     * @return future completed with the new state of the friendlist.
     */
    Future<FriendList> reject(String account, String requestor);

    /**
     * @param account the id of the account to retrieve the current state of the friendlist for.
     * @return future completed with the new state of the friendlist.
     */
    Future<FriendList> list(String account);

    /**
     * Returns a list of friends the given account has requested friendship with.
     *
     * @param account the id of the account to find friend requests for.
     * @return a list of account id's that the given account have sent requests to.
     */
    Future<PendingList> pending(String account);

    /**
     * Removes the friendship, may be called by either friend.
     *
     * @param account the account removing the friendship.
     * @param friend  the friend being removed, both ways.
     * @return future completed with the new state of the friendlist.
     */
    Future<FriendList> remove(String account, String friend);

    Future<SuggestionList> suggestions(String query);

    /**
     * Removes all data in the database.
     *
     * @return future
     */
    Future<Void> clear();
}
