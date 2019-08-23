package com.codingchili.core.social;

import com.codingchili.social.model.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.IndexedMapPersisted;
import com.codingchili.core.storage.StorageLoader;

/**
 * @author Robin Duda
 * <p>
 * Tests for the friendslist implementation.
 */
@RunWith(VertxUnitRunner.class)
public class FriendListTest {
    private String ACCOUNT_A = "account_A";
    private String ACCOUNT_B = "account_B";
    private static AsyncFriendStore friends;
    private static CoreContext core;

    @BeforeClass
    public static void setUp(TestContext test) {
        Async async = test.async();
        core = new SystemContext();

        new StorageLoader<FriendList>(core)
                .withPlugin(IndexedMapPersisted.class)
                .withValue(FriendList.class)
                .build(storage -> {
                    if (storage.succeeded()) {
                        friends = new FriendsDB(storage.result(), new OnlineDB(core));
                        async.complete();
                    } else {
                        test.fail(storage.cause());
                    }
                });
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        core.close(test.asyncAssertSuccess());
    }

    @Before
    public void prepare(TestContext test) {
        Async async = test.async();
        friends.clear().setHandler(done -> async.complete());
    }

    @Test
    public void request(TestContext test) {
        Async async = test.async();

        friends.request(ACCOUNT_A, ACCOUNT_B).setHandler(done -> {
            if (done.succeeded()) {
                // verify the request is present on account B's state.
                friends.list(ACCOUNT_B).setHandler(list -> {
                    test.assertTrue(list.result().getRequests().contains(ACCOUNT_A));
                    async.complete();
                });
            } else {
                test.fail(done.cause());
            }
        });
    }

    @Test
    public void reject(TestContext test) {
        Async async = test.async();

        friends.request(ACCOUNT_A, ACCOUNT_B).setHandler(done -> {
            if (done.succeeded()) {
                friends.reject(ACCOUNT_B, ACCOUNT_A).setHandler(rejected -> {
                    if (rejected.succeeded()) {
                        async.complete();
                    } else {
                        test.fail(rejected.cause());
                    }
                });
            } else {
                test.fail(done.cause());
            }
        });
    }

    @Test
    public void accepted(TestContext test) {
        Async async = test.async();

        friends.request(ACCOUNT_A, ACCOUNT_B)
                .compose(m -> friends.accept(ACCOUNT_B, ACCOUNT_A))
                .setHandler(m -> friends.list(ACCOUNT_B).setHandler(list -> {

                    test.assertTrue(list.result().getFriends().contains(ACCOUNT_A));

                    friends.list(ACCOUNT_A).setHandler(second -> {
                        test.assertTrue(second.result().getFriends().contains(ACCOUNT_B));
                        async.complete();
                    });
                }));
    }


    @Test
    public void pending(TestContext test) {
        Async async = test.async();

        friends.request(ACCOUNT_A, ACCOUNT_B).setHandler(done -> {
            if (done.succeeded()) {
                friends.pending(ACCOUNT_A).setHandler(requested -> {
                    if (requested.succeeded()) {
                        test.assertTrue(requested.result()
                                .getPending().contains(ACCOUNT_B));
                    } else {
                        test.fail(requested.cause());
                    }
                    async.complete();
                });
            } else {
                test.fail(done.cause());
            }
        });
    }


    @Test
    public void unfriend(TestContext test) {
        Async async = test.async();

        friends.request(ACCOUNT_A, ACCOUNT_B)
                .compose(m -> friends.accept(ACCOUNT_B, ACCOUNT_A))
                .compose(m -> friends.remove(ACCOUNT_B, ACCOUNT_A))
                .setHandler(done -> {
                    friends.list(ACCOUNT_A).setHandler(a -> {
                        test.assertFalse(a.result().getFriends().contains(ACCOUNT_B));

                        friends.list(ACCOUNT_B).setHandler(b -> {
                            test.assertFalse(b.result().getFriends().contains(ACCOUNT_A));
                            async.complete();
                        });

                    });
                });
    }

    @Test
    public void suggestion(TestContext test) {
        Async async = test.async();

        friends.request(ACCOUNT_A, ACCOUNT_B)
                .compose(m -> friends.request("missing", "missing"))
                .compose(m -> friends.accept(ACCOUNT_B, ACCOUNT_A))
                .setHandler(done -> {
                    friends.suggestions(ACCOUNT_A.substring(0, 5))
                            .setHandler(q -> {
                                // verify missing not in hits.
                                test.assertEquals(q.result().getSuggestions().size(), 2);
                                async.complete();
                            });
                });
    }

    @Test
    public void serializeFriendList() {
        FriendList list = new FriendList();
        list.getFriends().add("friend_a");
        list.getRequests().add("requested");
        list.setAccount("admin");
        Serializer.json(list);
    }
}
