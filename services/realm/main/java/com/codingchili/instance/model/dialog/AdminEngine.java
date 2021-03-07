package com.codingchili.instance.model.dialog;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.InstanceSettings;
import com.codingchili.instance.model.admin.AdminEvent;
import com.codingchili.instance.model.entity.*;
import com.codingchili.instance.model.events.*;
import com.codingchili.instance.model.npc.NoSuchNpcException;
import com.codingchili.realm.configuration.RealmSettings;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import com.codingchili.core.listener.Receiver;
import com.codingchili.core.protocol.*;

/**
 * @author Robin Duda
 * <p>
 * Implements admin commands.
 */
public class AdminEngine implements Receiver<AdminEvent> {
    private Protocol<AdminEvent> protocol = new Protocol<>(this);
    private GameContext game;

    public AdminEngine(GameContext game) {
        this.game = game;
    }

    @Api
    @Description("spawn the given entity id at the mouse position.")
    public void spawn(AdminEvent event) {
        game.spawner().spawn(new SpawnConfig()
                .setId(event.getId())
                .setPoint(event.getVector().toPoint())
        ).orElseThrow(() -> new NoSuchNpcException(event.getEntity()));
    }

    @Api
    @Description("promotes the given user account to admin")
    public void promote(AdminEvent event) {
        PlayerCreature player = game.getById(event.getEntity());
        realm(settings -> settings.getAdmins().add(player.getAccount()));
    }

    @Api
    @Description("remove admin privileges from target.")
    public void demote(AdminEvent event) {
        PlayerCreature player = game.getById(event.getEntity());
        realm(settings -> settings.getAdmins().remove(player.getAccount()));
    }

    private void realm(Consumer<RealmSettings> modifier) {
        RealmSettings settings = game.instance().realm();
        modifier.accept(settings);
        settings.save();
    }

    private void instance(Consumer<InstanceSettings> modifier) {
        InstanceSettings settings = game.instance().settings();
        modifier.accept(settings);
        settings.save();
    }

    @Api
    @Description("gives the target quantity exp")
    public void exp(AdminEvent event) {
        Creature target = game.getById(event.getEntity());
        game.spells().experience(target, event.getQuantity());
    }

    @Api
    @Description("kick the targeted user")
    public void kick(AdminEvent event) {
        game.remove(game.getById(event.getEntity()));
    }

    @Api
    @Description("teleport to the given instance id")
    public void tele(AdminEvent event) {
        game.movement().travel(game.getById(event.getEntity()), event.getId());
    }

    @Api
    @Description("create an item in the targeted creatures inventory.")
    public void item(AdminEvent event) {
        game.inventory().item(game.getById(event.getEntity()), event.getId(), event.getQuantity());
    }

    @Api
    @Description("smites the targeted creature millions of dmg.")
    public void smite(AdminEvent event) {
        game.spells().damage(game.getById(event.getTarget()), game.getById(event.getEntity()))
                .effect("slay")
                .critical(true)
                .magical(-32_000_000.0)
                .apply();
    }

    @Api
    @Description("display a notification banner for the instance.")
    public void banner(AdminEvent event) {
        game.publish(new NotificationEvent(event.getMessage()));
    }

    @Api
    @Description("get the id of the logged in account of the player")
    public void identify(AdminEvent event) {
        game.getById(event.getTarget()).handle(
                new Identification(game.getById(event.getEntity()))
        );
    }

    @Api
    public void help(AdminEvent event) {
        game.getById(event.getTarget()).handle(new CommandList());
    }

    @Override
    public void handle(AdminEvent event) {
        protocol.get(event.getCommand(), Role.ADMIN).submit(event);
    }

    private static class Identification implements Event {
        private String message;

        public Identification(Entity creature) {
            if (creature instanceof PlayerCreature) {
                PlayerCreature player = (PlayerCreature) creature;
                message = player.getName() + "@" + player.getAccount();
            } else {
                message = creature.getName() + "@" + creature.getId();
            }
        }

        public String getMessage() {
            return message;
        }

        @Override
        public EventType getRoute() {
            return EventType.admin;
        }
    }

    private static class CommandList implements Event {
        private static final StringBuilder message = new StringBuilder();

        static {
            message.append("Available admin commands\n");
            for (Method method : AdminEngine.class.getMethods()) {
                Api api = method.getAnnotation(Api.class);
                Description description = method.getAnnotation(Description.class);
                if (api != null) {
                    message.append(".");
                    message.append(method.getName());
                    message.append(" ");
                    if (description != null) {
                        message.append(description.value());
                    } else {
                        message.append("no description for command.");
                    }
                    message.append('\n');
                }
            }
        }

        public String getMessage() {
            return message.toString();
        }

        @Override
        public EventType getRoute() {
            return EventType.admin;
        }
    }
}
