package com.codingchili.realm.model;

import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.model.items.Inventory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.IndexedMapPersisted;
import com.codingchili.core.storage.StorageLoader;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class CharacterDBTest {
    private static SystemContext context;
    private static CharacterDB db;


    @BeforeClass
    public static void setUp(TestContext test) {
        Async async = test.async();
        context = new SystemContext();

        //new File("db/CharacterDBTest.sqlite").delete();

        new StorageLoader<PlayerCreature>(context)
                .withValue(PlayerCreature.class)
                .withPlugin(IndexedMapPersisted.class)
                .withDB("db", "CharacterDBTest").build(done -> {
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

        db.create(done -> {
            if (done.succeeded()) {
                async.complete();
            } else {
                test.fail(done.cause());
            }
        }, creature("createCharacter"));
    }

    private PlayerCreature creature(String id) {
        PlayerCreature creature = new PlayerCreature();
        creature.setName(id);
        creature.setId(id);
        creature.setAccount("admin");
        return creature;
    }

    @Test
    public void deleteCharacter(TestContext test) {
        Async async = test.async();
        PlayerCreature delete = creature("delete");

        db.create(done -> {
            db.remove(remove -> {
                if (remove.succeeded()) {
                    async.complete();
                } else {
                    test.fail(remove.cause());
                }
            }, "admin", delete.getId());

        }, delete);
    }


    @Test
    public void findCharacterByAccount(TestContext test) {
        Async async = test.async();
        PlayerCreature creature = creature("findByAccount");

        db.create(done -> {
            if (done.succeeded()) {

                db.findByUsername(found -> {
                    if (found.succeeded()) {
                        async.complete();
                    } else {
                        test.fail(found.cause());
                    }
                }, "admin");
            } else {
                test.fail(done.cause());
            }
        }, creature);
    }

    @Test
    public void testUpdateCharacter(TestContext test) {
        Async async = test.async();
        PlayerCreature creature = creature("updateCharacter");

        db.create(done -> {
            if (done.succeeded()) {
                creature.setVector(new Vector());
                creature.setInventory(new Inventory());
                db.update(creature).setHandler(update -> {
                    if (update.succeeded()) {
                        async.complete();
                    } else {
                        test.fail(update.cause());
                    }
                });
            } else {
                test.fail(done.cause());
            }
        }, creature);
    }


    // reproducer for super hard to find bug.

    @Test
    public void serializerTest(TestContext test) {
        Async async = test.async();
        PlayerCreature creature = Serializer.unpack("{\n" +
                "  \"interactions\" : [ ],\n" +
                "  \"model\" : {\n" +
                "    \"graphics\" : \"game/placeholder.png\",\n" +
                "    \"scale\" : 1.0,\n" +
                "    \"blocking\" : false,\n" +
                "    \"hitbox\" : {\n" +
                "      \"points\" : [ ],\n" +
                "      \"type\" : \"rectangular\"\n" +
                "    },\n" +
                "    \"layer\" : 5\n" +
                "  },\n" +
                "  \"id\" : \"XPALA\",\n" +
                "  \"attributes\" : { },\n" +
                "  \"name\" : \"XPALA\",\n" +
                "  \"vector\" : {\n" +
                "    \"acceleration\" : 0.4,\n" +
                "    \"velocity\" : 0.0,\n" +
                "    \"direction\" : 0.0,\n" +
                "    \"size\" : 24,\n" +
                "    \"x\" : -1.0,\n" +
                "    \"y\" : -1.0\n" +
                "  },\n" +
                "  \"afflictions\" : {\n" +
                "    \"list\" : [ ]\n" +
                "  },\n" +
                "  \"inventory\" : {\n" +
                "    \"equipped\" : { },\n" +
                "    \"items\" : [ {\n" +
                "      \"slot\" : \"none\",\n" +
                "      \"armorType\" : \"none\",\n" +
                "      \"weaponType\" : \"none\",\n" +
                "      \"id\" : \"Apple.LEGENDARY\",\n" +
                "      \"icon\" : \"apple_golden.png\",\n" +
                "      \"name\" : \"golden Apple\",\n" +
                "      \"description\" : \"Shiny and juicy, heals some health.\",\n" +
                "      \"stats\" : { },\n" +
                "      \"rarity\" : \"LEGENDARY\",\n" +
                "      \"quantity\" : 10\n" +
                "    } ],\n" +
                "    \"currency\" : 1\n" +
                "  },\n" +
                "  \"spells\" : {\n" +
                "    \"learned\" : [ ],\n" +
                "    \"charges\" : { },\n" +
                "    \"cooldowns\" : { }\n" +
                "  },\n" +
                "  \"stats\" : { },\n" +
                "  \"fromAnotherInstance\" : false,\n" +
                "  \"logins\" : 0,\n" +
                "  \"classId\" : \"paladin\",\n" +
                "  \"account\" : \"admin\",\n" +
                "  \"baseStats\" : { }\n" +
                "}", PlayerCreature.class);

        db.create(done -> {
            if (done.succeeded()) {

                PlayerCreature creatureUpdated = Serializer.unpack("{\n" +
                        "  \"interactions\" : [ ],\n" +
                        "  \"model\" : {\n" +
                        "    \"graphics\" : \"game/character/bunny/bunny\",\n" +
                        "    \"skin\" : \"blue\",\n" +
                        "    \"scale\" : 0.16,\n" +
                        "    \"blocking\" : false,\n" +
                        "    \"hitbox\" : {\n" +
                        "      \"points\" : [ ],\n" +
                        "      \"type\" : \"rectangular\"\n" +
                        "    },\n" +
                        "    \"layer\" : 5\n" +
                        "  },\n" +
                        "  \"id\" : \"XPALA\",\n" +
                        "  \"attributes\" : { },\n" +
                        "  \"name\" : \"XPALA\",\n" +
                        "  \"vector\" : {\n" +
                        "    \"acceleration\" : 0.4,\n" +
                        "    \"velocity\" : 0.0,\n" +
                        "    \"direction\" : 0.0,\n" +
                        "    \"size\" : 24,\n" +
                        "    \"x\" : 264.50613,\n" +
                        "    \"y\" : 506.0107\n" +
                        "  },\n" +
                        "  \"afflictions\" : {\n" +
                        "    \"list\" : [ ]\n" +
                        "  },\n" +
                        "  \"inventory\" : {\n" +
                        "    \"equipped\" : { },\n" +
                        "    \"items\" : [ {\n" +
                        "      \"slot\" : \"none\",\n" +
                        "      \"armorType\" : \"none\",\n" +
                        "      \"weaponType\" : \"none\",\n" +
                        "      \"id\" : \"Apple.LEGENDARY\",\n" +
                        "      \"icon\" : \"apple_golden.png\",\n" +
                        "      \"name\" : \"golden Apple\",\n" +
                        "      \"description\" : \"Shiny and juicy, heals some health.\",\n" +
                        "      \"stats\" : { },\n" +
                        "      \"rarity\" : \"LEGENDARY\",\n" +
                        "      \"quantity\" : 10\n" +
                        "    } ],\n" +
                        "    \"currency\" : 1\n" +
                        "  },\n" +
                        "  \"spells\" : {\n" +
                        "    \"learned\" : [ ],\n" +
                        "    \"charges\" : { },\n" +
                        "    \"cooldowns\" : { }\n" +
                        "  },\n" +
                        "  \"stats\" : {\n" +
                        "    \"maxhealth\" : 180.0,\n" +
                        "    \"magicResist\" : 12.0,\n" +
                        "    \"nextlevel\" : 180.0,\n" +
                        "    \"wisdom\" : 14.0,\n" +
                        "    \"constitution\" : 18.0,\n" +
                        "    \"level\" : 1.0,\n" +
                        "    \"armorClass\" : 24.0,\n" +
                        "    \"movement\" : 210.0,\n" +
                        "    \"intelligence\" : 16.0,\n" +
                        "    \"maxenergy\" : 380.0,\n" +
                        "    \"energy\" : 380.0,\n" +
                        "    \"experience\" : 15.0,\n" +
                        "    \"health\" : 180.0,\n" +
                        "    \"strength\" : 16.0,\n" +
                        "    \"dexterity\" : 14.0\n" +
                        "  },\n" +
                        "  \"fromAnotherInstance\" : false,\n" +
                        "  \"logins\" : 1,\n" +
                        "  \"instance\" : \"level 1\",\n" +
                        "  \"classId\" : \"paladin\",\n" +
                        "  \"account\" : \"admin\",\n" +
                        "  \"baseStats\" : {\n" +
                        "    \"level\" : 1.0,\n" +
                        "    \"maxhealth\" : 180.0,\n" +
                        "    \"maxenergy\" : 380.0,\n" +
                        "    \"energy\" : 380.0,\n" +
                        "    \"experience\" : 15.0,\n" +
                        "    \"health\" : 180.0,\n" +
                        "    \"nextlevel\" : 180.0\n" +
                        "  }\n" +
                        "}", PlayerCreature.class);

                db.update(creatureUpdated).setHandler(update -> {
                    if (update.succeeded()) {

                        async.complete();

                    } else {
                        test.fail(update.cause());
                    }
                });
            } else {
                test.fail(done.cause());
            }
        }, creature);
    }

    @Test
    public void removerTest(TestContext test) {
        Async async = test.async();

        db.remove(done -> {
            if (done.succeeded()) {
                async.complete();
            } else {
                test.fail(done.cause());
            }
        }, "admin", "XPALA");
    }
}
