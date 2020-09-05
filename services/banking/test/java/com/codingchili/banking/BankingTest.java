package com.codingchili.banking;

import com.codingchili.banking.model.Item;
import com.codingchili.core.protocol.Serializer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(VertxUnitRunner.class)
public class BankingTest {
    private static final String UNMAPPED_PROPERTY = "unmappedProperty";

    @Test
    public void test(TestContext test) {
        Item item = Serializer.unpack(testFile("item.json"), Item.class);
        // drop properties for now.,
    }

    private JsonObject testFile(String fileName) {
        try {
            return new JsonObject(
                    new String(
                            Files.readAllBytes(Paths.get(getClass().getResource("item.json").toURI()))
                    )
            );
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
