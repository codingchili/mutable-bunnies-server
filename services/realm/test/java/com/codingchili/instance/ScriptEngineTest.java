package com.codingchili.instance;

import com.codingchili.core.protocol.Serializer;
import com.codingchili.instance.scripting.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.unit.TestContext;
import org.junit.*;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.codingchili.core.benchmarking.*;
import com.codingchili.core.benchmarking.reporting.BenchmarkHTMLReport;
import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;

/**
 * @author Robin Duda
 * <p>
 * Tests for the scripting engines.
 */
@RunWith(VertxUnitRunner.class)
public class ScriptEngineTest {
    private static final String SOURCE = "//";
    private CoreContext context;

    @Rule
    public Timeout timeout = Timeout.seconds(60);

    @Before
    public void setUp() {
        context = new SystemContext();
    }

    @After
    public void tearDown(TestContext test) {
        context.close(test.asyncAssertSuccess());
    }

    @Test
    public void serializeScripted(TestContext text) {
        System.out.println(Serializer.json(ScriptEngines.script(SOURCE, JexlScript.TYPE)));
    }

    @Test
    public void deserializeScripted(TestContext test) {
        JsonObject json = new JsonObject().put(JexlScript.TYPE, SOURCE);
        Scripted scripted = Serializer.unpack(json, Scripted.class);
        test.assertEquals(scripted.getEngine(), JexlScript.TYPE);
        test.assertEquals(scripted.getSource(), SOURCE);
    }

    @Test
    @Ignore("Disabled by default, performance test only.")
    public void performance(TestContext test) {
        BenchmarkGroup group = new BenchmarkGroupBuilder("scriptEngines", 2000);
        Map<String, Scripted> scripts = new HashMap<>();
        Async async = test.async();

        AtomicReference<Float> myFloat = new AtomicReference<>(24.2f);
        Bindings bindings = new Bindings();

        BenchmarkImplementation compile = group.implementation("compile");
        BenchmarkImplementation execute = group.implementation("execute");

        Stream.of(NativeScript.TYPE, JexlScript.TYPE, GroovyScript.TYPE, JavaScript.TYPE).forEach(engine -> {
            compile.add(engine,
                    (future) -> {
                        if (engine.equals(NativeScript.TYPE)) {
                            scripts.put(engine, ScriptEngines.script("com.codingchili.instance.NativeScriptSample", engine));
                        } else {
                            scripts.put(engine, ScriptEngines.script(
                                    myFloat.updateAndGet(theFloat -> theFloat + 0.1f) + " * number;", engine));
                        }
                        future.complete();
                    });

            execute.add(engine,
                    (future) -> {
                        bindings.set("number", myFloat.getAndUpdate(theFloat -> theFloat + 0.1f));
                        scripts.get(engine).apply(bindings);
                        future.complete();
                    });
        });

        new BenchmarkExecutor(context)
                .setListener(new BenchmarkConsoleListener())
                .start(group).onComplete(done -> {
                    try {
                        new BenchmarkHTMLReport(done.result()).display();
                    } catch (Exception e) {
                        // no display available.
                    }
            async.complete();
        });
    }

}
