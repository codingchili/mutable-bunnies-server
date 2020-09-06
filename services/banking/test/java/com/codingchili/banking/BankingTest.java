package com.codingchili.banking;

import com.codingchili.banking.model.Item;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.Serializer;

@RunWith(VertxUnitRunner.class)
public class BankingTest {

    @BeforeClass
    public static void setup(TestContext test) {
        CoreContext core = new SystemContext();
        new Service().init(core);
        core.close(test.asyncAssertSuccess());
    }

    @Test
    public void ensureItemSerializable(TestContext test) {
        Item item = Serializer.unpack(testFile("item.json"), Item.class);
        Serializer.buffer(item);
        test.assertNotNull(item.getName());
    }

    private JsonObject testFile(String fileName) {
        try {
            return new JsonObject(
                    new String(Files.readAllBytes(Paths.get(getClass().getResource(fileName).toURI())))
            );
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
