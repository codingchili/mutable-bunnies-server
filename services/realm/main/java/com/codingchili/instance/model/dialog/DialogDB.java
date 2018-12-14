package com.codingchili.instance.model.dialog;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.files.*;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.CoreStrings.ID_COUNT;

/**
 * @author Robin Duda
 *
 * A database of all dialogs in the system.
 */
public class DialogDB {
    private static final String CONF_PATH = "conf/game/dialog";
    private static final String DIALOG_LOAD = "dialog.load";
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static Map<String, Dialog> dialogs;
    private Logger logger;

    public DialogDB(CoreContext core) {
        this.logger = core.logger(getClass());

        if (!initialized.getAndSet(true)) {
            dialogs = ConfigurationFactory.readDirectory(CONF_PATH).stream()
                    .map(config -> Serializer.unpack(config, Dialog.class))
                    .collect(Collectors.toMap(Dialog::getId, (v) -> v));

            logger.event(DIALOG_LOAD).put(ID_COUNT, dialogs.size()).send();

            FileWatcher.builder(core)
                    .onDirectory(CONF_PATH)
                    .rate(() -> 1500)
                    .withListener(new FileStoreListener() {
                        @Override
                        public void onFileModify(Path path) {
                            logger.event(DIALOG_LOAD).send("dialog updated " + path.toString());
                            Dialog dialog = Serializer.unpack(
                                    ConfigurationFactory.readObject(path.toString()), Dialog.class);
                            dialogs.put(dialog.getId(), dialog);
                        }
                    }).build();
        }
    }

    public Dialog getById(String id) {
        Dialog dialog = dialogs.get(id);

        if (dialog != null) {
            return dialog;
        } else {
            throw new NoSuchDialogException(id);
        }
    }
}
