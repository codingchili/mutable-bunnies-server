package com.codingchili.instance.model.spells;

import com.codingchili.instance.scripting.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class GroovyScriptTest {

    @BeforeClass
    public static void setUp(TestContext test) {
        Async async = test.async();
        CoreContext core = new SystemContext();
        ReferencedScript.initialize(core).setHandler(done -> {
            if (done.succeeded()) {
                async.complete();
            } else {
                test.fail(done.cause());
            }
        });
    }

    @Test
    public void potentVenomCompile() {
        new GroovyScript("println \"in potent_venom\"").apply(Bindings.NONE);
    }

}
