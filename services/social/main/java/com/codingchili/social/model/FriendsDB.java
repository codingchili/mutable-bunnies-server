package com.codingchili.social.model;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.codingchili.core.storage.*;

/**
 * @author Robin Duda
 * <p>
 * Implementation of a friends store.
 */
public class FriendsDB implements AsyncFriendStore {
    private static final String ID_REQUESTED = "requests[]";
    private AsyncStorage<FriendList> friends;
    private OnlineDB online;

    public FriendsDB(AsyncStorage<FriendList> storage, OnlineDB online) {
        this.friends = storage;
        this.online = online;
        friends.addIndex(ID_REQUESTED);
    }

    @Override
    public Future<Void> request(String from, String to) {
        Promise<Void> promise = Promise.promise();

        getOrCreate(to, list -> {
            if (!list.getFriends().contains(to)) {
                list.request(from);
                friends.put(list, (ignored) -> promise.complete());
            } else {
                promise.complete();
            }
        });

        return promise.future();
    }

    @Override
    public Future<FriendList> accept(String account, String friend) {
        Promise<FriendList> promise = Promise.promise();

        // add add as friend to first
        getOrCreate(account, list -> {
            if (list.accept(friend)) {
                friends.put(list, done -> {

                    // add as friend to second.
                    getOrCreate(friend, other -> {
                        other.accepted(account);
                        friends.put(other, (ignored) -> {
                            promise.complete(list);
                        });
                    });
                });
            } else {
                promise.complete(list);
            }
        });

        return promise.future();
    }

    @Override
    public Future<FriendList> reject(String account, String requestor) {
        Promise<FriendList> promise = Promise.promise();

        getOrCreate(account, list -> {
            list.reject(requestor);
            friends.put(list, (ignored) -> promise.complete(setOnlineStatus(list)));
        });
        return promise.future();
    }

    @Override
    public Future<FriendList> list(String account) {
        Promise<FriendList> promise = Promise.promise();
        getOrCreate(account, list -> promise.complete(setOnlineStatus(list)));
        return promise.future();
    }

    @Override
    public Future<PendingList> pending(String account) {
        Promise<PendingList> promise = Promise.promise();

        friends.query(ID_REQUESTED).equalTo(account)
                .execute(query -> {
                    if (query.succeeded()) {
                        promise.complete(new PendingList(
                                query.result().stream()
                                        .map(FriendList::getAccount)
                                        .collect(Collectors.toList()))
                        );
                    } else {
                        promise.fail(query.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public Future<FriendList> remove(String account, String friend) {
        Promise<FriendList> promise = Promise.promise();
        getOrCreate(account, first -> {
            first.remove(friend);
            friends.put(first, done -> {
                getOrCreate(friend, second -> {
                    second.remove(account);
                    friends.put(second, (saved) -> {
                        promise.complete(setOnlineStatus(second));
                    });
                });

            });

        });
        return promise.future();
    }

    private FriendList setOnlineStatus(FriendList list) {
        list.getFriends().forEach(friend -> {
            if (online.is(friend)) {
                list.online(friend, online.realms(friend));
            }
        });
        return list;
    }

    @Override
    public Future<SuggestionList> suggestions(String query) {
        Promise<SuggestionList> promise = Promise.promise();
        friends.query(Storable.idField)
                .startsWith(query)
                .pageSize(6)
                .order(SortOrder.ASCENDING)
                .execute(q -> {
                    if (q.succeeded()) {
                        promise.complete(new SuggestionList(
                                q.result().stream()
                                        .map(FriendList::getAccount)
                                        .collect(Collectors.toList()))
                        );
                    } else {
                        promise.fail(q.cause());
                    }
                });
        return promise.future();
    }

    @Override
    public Future<Void> clear() {
        Promise<Void> promise = Promise.promise();
        friends.clear(promise);
        return promise.future();
    }

    private void getOrCreate(String id, Consumer<FriendList> handler) {
        friends.get(id, done -> {
            if (done.succeeded()) {
                handler.accept(done.result());
            } else {
                handler.accept(new FriendList(id));
            }
        });
    }
}
