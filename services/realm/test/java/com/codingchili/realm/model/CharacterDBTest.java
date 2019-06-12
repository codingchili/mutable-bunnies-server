package com.codingchili.realm.model;

import com.codingchili.instance.model.entity.PlayerCreature;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.storage.IndexedMapPersisted;
import com.codingchili.core.storage.StorageLoader;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
@Ignore("not ready yet.")
public class CharacterDBTest {
    private static SystemContext context;
    private static CharacterDB db;


    @BeforeClass
    public static void setUp(TestContext test) {
        Async async = test.async();
        context = new SystemContext();
        new StorageLoader<PlayerCreature>(context)
                .withValue(PlayerCreature.class)
                .withPlugin(IndexedMapPersisted.class)
                .withDB("test").build(done -> {
            if (done.succeeded()) {
                db = new CharacterDB(done.result());
                async.complete();
            } else {
                test.fail(done.cause());
            }
        });
    }

    @AfterClass
    public static void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void createCharacter(TestContext test) {
        Async async = test.async();

        PlayerCreature creature = new PlayerCreature();
        creature.setId("fozza");
        creature.setAccount("admin");

        db.create(done -> {
            if (done.succeeded()) {
                async.complete();
            } else {
                test.fail(done.cause());
            }
        }, creature);
    }

    @Test
    public void deleteCharacter(TestContext test) {
        Async async = test.async();

        db.remove(done -> {
            if (done.succeeded()) {
                async.complete();
            } else {
                test.fail(done.cause());
            }
        }, "admin", "fozza");
    }


    @Test
    @Ignore("tbd")
    public void findCharacterByAccount(TestContext test) {
        Async async = test.async();

    }
}
