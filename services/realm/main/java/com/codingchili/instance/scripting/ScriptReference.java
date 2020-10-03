package com.codingchili.instance.scripting;

import io.vertx.core.Future;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.*;
import com.codingchili.core.files.*;
import com.codingchili.core.logging.Logger;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * A script that can be used to reference another script. The referenced script
 * must be loaded before this script is loaded.
 */
public class ScriptReference implements Scripted {
    public static final String TYPE = "reference";
    private static final String SCRIPT_LOAD = "script.load";
    private static final String SCRIPT_PATH = "conf/game/scripts";
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static final Map<String, Scripted> scripts = new ConcurrentHashMap<>();
    private Scripted reference;

    public static Future<Void> initialize(CoreContext core) {
        long start = System.currentTimeMillis();
        Logger logger = core.logger(ScriptReference.class);

        if (!initialized.getAndSet(true)) {
            Future<Void> future = Future.future();

            core.blocking((blocking) -> {
                try {
                    long loaded = ConfigurationFactory.enumerate(SCRIPT_PATH, true)
                            .parallel()
                            .map(File::new)
                            .peek(ScriptReference::loadScriptAt)
                            .filter((file) -> Boolean.TRUE)
                            .count();

                    logger.event(SCRIPT_LOAD)
                            .put(ID_COUNT, loaded)
                            .put(ID_TIME, (System.currentTimeMillis() - start) + "ms")
                            .send();

                    setupFileWatcher(core, logger);
                    blocking.complete();
                } catch (Exception e) {
                    blocking.fail(e);
                }
            }, future);
            return future;
        } else {
            return Future.succeededFuture();
        }
    }

    private static void setupFileWatcher(CoreContext core, Logger logger) {
        FileWatcher.builder(core)
                .onDirectory(SCRIPT_PATH)
                .rate(TimerSource.of(1500))
                .withListener(new FileStoreListener() {
                    @Override
                    public void onFileModify(Path path) {
                        loadScriptAt(path.toFile());
                        logger.event(SCRIPT_LOAD).send("script updated " + path.toString());
                    }
                }).build();


    }

    private static void loadScriptAt(File file) {
        try {
            String name = file.getName();
            String scriptType = name.substring(name.indexOf(".") + 1);
            Scripted script = ScriptEngines.script(
                    new String(Files.readAllBytes(file.toPath())), scriptType);
            add(name, script);
        } catch (Exception e) {
            throw new ScriptCompileException(file.getPath(), e);
        }
    }

    /**
     * Loads a script that has been referenced using {@link #add(String, Scripted)}.
     *
     * @param referenceName the name of the script to load from the bank.
     */
    public ScriptReference(String referenceName) {
        this.reference = get(referenceName).orElseThrow(() -> new CoreRuntimeException(
                String.format("Failed to find referenced script '%s/%s'.", SCRIPT_PATH, referenceName)));
    }

    @Override
    public <T> T apply(Bindings bindings) {
        return reference.apply(bindings);
    }

    @Override
    public String getEngine() {
        return reference.getEngine();
    }

    @Override
    public String getSource() {
        return reference.getSource();
    }

    /**
     * Adds a script to the bank that may be referenced later using the given reference.
     *
     * @param reference a reference used to reference the script.
     * @param script    the actual script.
     */
    public static void add(String reference, Scripted script) {
        scripts.put(reference, script);
    }

    /**
     * Retrieves a script by the given reference.
     *
     * @param reference the reference of an added script.
     * @return the referenced script, empty otherwise.
     */
    public static Optional<Scripted> get(String reference) {
        return Optional.ofNullable(scripts.get(reference));
    }
}
