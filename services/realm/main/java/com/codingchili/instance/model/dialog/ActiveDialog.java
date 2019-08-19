package com.codingchili.instance.model.dialog;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.model.entity.Creature;
import com.codingchili.instance.model.entity.Entity;
import com.codingchili.instance.scripting.Bindings;

import java.util.HashMap;
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
    private static final String ID_QUESTS = "quests";
    private GameContext game;
    private Bindings bindings;
    private Dialog dialog;
    private Option cursor;

    /**
     * @param game   the game context.
     * @param dialog the dialog to be used.
     * @param source the dialog initiator.
     * @param target the dialog holder.
     */
    public ActiveDialog(GameContext game, Dialog dialog, Entity source, Entity target) {
        this.bindings = new Bindings()
                .setContext(game)
                .set(ID_QUESTS, game.quests())
                .setState(new HashMap<>())
                .set(ID_LOG, (Consumer<String>) this::log)
                .setSource(source)
                .setTarget(target);

        this.game = game;
        this.dialog = dialog;
        this.cursor = dialog.get(dialog.getStart());
    }

    public Creature source() {
        return bindings.getSource();
    }

    public Creature target() {
        return bindings.getTarget();
    }

    public String text() {
        return cursor.getText();
    }

    private void log(String line) {
        game.getLogger(getClass()).event("log").put("dialog", dialog.getId()).send(line);
    }

    /**
     * @param optionKey the key name for the response.
     */
    public void say(String optionKey) {
        Option option = dialog.get(optionKey);

        if (option.isAvailable(bindings)) {
            option.use(bindings);

            if (option.getRedirect() != null) {
                cursor = dialog.get(option.getRedirect());
            } else {
                cursor = option;
            }
        } else {
            throw new FilteredDialogOptionException(optionKey);
        }
    }

    /**
     * @return the available steps.
     */
    public Set<Line> lines() {
        return cursor.getNext().stream()
                .filter(next -> {
                    Option option = dialog.get(next.getId());

                    if (option != null) {
                        return option.isAvailable(bindings);
                    } else {
                        log(String.format("missing handler for option '%s'.", next.getId()));
                        return false;
                    }
                })
                .collect(Collectors.toSet());
    }

    /**
     * @return true if the dialog has ended.
     */
    public boolean isEnded() {
        return cursor.getNext().isEmpty() || cursor.getText() == null;
    }
}
