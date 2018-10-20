package com.codingchili.realm.instance.model.dialog;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.entity.Creature;
import com.codingchili.realm.instance.scripting.Bindings;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.codingchili.common.Strings.ID_LOG;

/**
 * @author Robin Duda
 * <p>
 * Reduces memory usage by referencing a dialog instance and keeps a cursor for an active dialog.
 */
public class ActiveDialog {
    private GameContext game;
    private Bindings bindings;
    private Dialog dialog;
    private Option cursor;

    /**
     * @param dialog
     * @param source
     * @param target
     */
    public ActiveDialog(GameContext game, Dialog dialog, Creature source, Creature target) {
        this.bindings = new Bindings()
                .set(ID_LOG, (Consumer<String>) this::log)
                .setSource(source)
                .setTarget(target);

        this.game = game;
        this.dialog = dialog;
        this.cursor = dialog.get(dialog.getStart());
    }

    public String text() {
        return cursor.getText();
    }

    private void log(String line) {
        game.getLogger(getClass()).event("log").put("dialog", dialog.getId()).send(line);
    }

    /**
     * @param optionKey
     */
    public void say(String optionKey) {
        Option option = dialog.get(optionKey);

        if (option.isAvailable(bindings)) {
            option.use(bindings);
            cursor = option;
        } else {
            throw new FilteredDialogOptionException(optionKey);
        }
    }

    /**
     * @return
     */
    public Set<Line> lines() {
        return cursor.getNext().stream()
                .filter(next -> dialog.get(next.getOption()).isAvailable(bindings))
                .collect(Collectors.toSet());
    }

    /**
     * @return
     */
    public boolean isEnded() {
        return cursor.getNext().isEmpty();
    }
}
