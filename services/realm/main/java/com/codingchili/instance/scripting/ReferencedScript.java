package com.codingchili.instance.scripting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codingchili.core.context.*;
import com.codingchili.core.files.FileStoreListener;
import com.codingchili.core.files.FileWatcher;
import com.codingchili.core.logging.Logger;

/**
 * @author Robin Duda
 * <p>
 * A script that can be used to reference another script. The referenced script
 * must be loaded before this script is loaded.
 */
public class ReferencedScript implements Scripted {
    public static final String TYPE = "reference";
    private static final String SCRIPT_LOAD = "script.load";
    private static final String SCRIPT_PATH = "conf/game/script";
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static final Map<String, Scripted> scripts = new ConcurrentHashMap<>();
    private Scripted reference;

    static {
        // loads all script in the game/scripts folder.
        StartupListener.subscribe(core -> {
            Logger logger = core.logger(ReferencedScript.class);

            if (!initialized.getAndSet(true)) {
                // perform the initial load of all files.
                File[] files = new File(SCRIPT_PATH).listFiles();
                if (files != null) {
                    for (File file : files) {
                        loadScriptAt(core, file);
                    }
                    logger.event(SCRIPT_LOAD).send("loaded " + files.length + " scripts from " + SCRIPT_PATH);
                }

                // set up a filewatcher to reload updated scripts.
                FileWatcher.builder(core)
                        .onDirectory(SCRIPT_PATH)
                        .rate(() -> 1500)
                        .withListener(new FileStoreListener() {
                            @Override
                            public void onFileModify(Path path) {
                                loadScriptAt(core, path.toFile());
                                logger.event(SCRIPT_LOAD).send("script updated " + path.toString());
                            }
                        }).build();
            }
        });
    }

    private static void loadScriptAt(CoreContext core, File file) {
        try {
            String name = file.getName();
            String scriptType = name.substring(name.indexOf(".") + 1);
            Scripted script = ScriptEngines.script(
                    new String(Files.readAllBytes(file.toPath())), scriptType);
            add(name, script);
        } catch (IOException e) {
            core.logger(ReferencedScript.class).onError(e);
        }
    }

    /**
     * Loads a script that has been referenced using {@link #add(String, Scripted)}.
     *
     * @param referenceName the name of the script to load from the bank.
     */
    public ReferencedScript(String referenceName) {
        this.reference = get(referenceName).orElseThrow(() -> new CoreRuntimeException(
                String.format("Failed to find referenced script '%s'.", referenceName)));
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
