package com.codingchili.social.model;

import io.vertx.core.Future;

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
        Future<Void> future = Future.future();

        getOrCreate(to, list -> {
            if (!list.getFriends().contains(to)) {
                list.request(from);
                friends.put(list, (ignored) -> future.complete());
            } else {
                future.complete();
            }
        });

        return future;
    }

    @Override
    public Future<FriendList> accept(String account, String friend) {
        Future<FriendList> future = Future.future();

        // add add as friend to first
        getOrCreate(account, list -> {
            if (list.accept(friend)) {
                friends.put(list, done -> {

                    // add as friend to second.
                    getOrCreate(friend, other -> {
                        other.accepted(account);
                        friends.put(other, (ignored) -> {
                            future.complete(list);
                        });
                    });
                });
            } else {
                future.complete(list);
            }
        });

        return future;
    }

    @Override
    public Future<FriendList> reject(String account, String requestor) {
        Future<FriendList> future = Future.future();

        getOrCreate(account, list -> {
            list.reject(requestor);
            friends.put(list, (ignored) -> future.complete(setOnlineStatus(list)));
        });
        return future;
    }

    @Override
    public Future<FriendList> list(String account) {
        Future<FriendList> future = Future.future();
        getOrCreate(account, list -> future.complete(setOnlineStatus(list)));
        return future;
    }

    @Override
    public Future<PendingList> pending(String account) {
        Future<PendingList> future = Future.future();

        friends.query(ID_REQUESTED).equalTo(account)
                .execute(query -> {
                    if (query.succeeded()) {
                        future.complete(new PendingList(
                                query.result().stream()
                                        .map(FriendList::getAccount)
                                        .collect(Collectors.toList()))
                        );
                    } else {
                        future.fail(query.cause());
                    }
                });
        return future;
    }

    @Override
    public Future<FriendList> remove(String account, String friend) {
        Future<FriendList> future = Future.future();
        getOrCreate(account, first -> {
            first.remove(friend);
            friends.put(first, done -> {
                getOrCreate(friend, second -> {
                    second.remove(account);
                    friends.put(second, (saved) -> {
                        future.complete(setOnlineStatus(second));
                    });
                });

            });

        });
        return future;
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
        Future<SuggestionList> future = Future.future();
        friends.query(Storable.idField)
                .startsWith(query)
                .pageSize(6)
                .order(SortOrder.ASCENDING)
                .execute(q -> {
                    if (q.succeeded()) {
                        future.complete(new SuggestionList(
                                q.result().stream()
                                        .map(FriendList::getAccount)
                                        .collect(Collectors.toList()))
                        );
                    } else {
                        future.fail(q.cause());
                    }
                });
        return future;
    }

    @Override
    public Future<Void> clear() {
        Future<Void> future = Future.future();
        friends.clear(future);
        return future;
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
