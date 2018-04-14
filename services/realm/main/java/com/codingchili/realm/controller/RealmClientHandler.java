package com.codingchili.realm.controller;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.instance.model.entity.PlayableClass;
import com.codingchili.realm.instance.model.entity.PlayerCreature;
import com.codingchili.realm.instance.model.events.JoinMessage;
import com.codingchili.realm.instance.model.events.LeaveMessage;
import com.codingchili.realm.model.*;
import io.vertx.core.json.JsonObject;

import java.util.Collection;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.*;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.configuration.CoreStrings.ANY;
import static com.codingchili.core.protocol.RoleMap.PUBLIC;

/**
 * @author Robin Duda
 * <p>
 * Handles messages between the realm handler and clients.
 */
@Address(Address.WEBSOCKET)
public class RealmClientHandler implements CoreHandler {
    private final Protocol<Request> protocol = new Protocol<>(this);
    private AsyncCharacterStore characters;
    private RealmContext context;
    private Logger logger;

    public RealmClientHandler(RealmContext context) {
        this.context = context;
        this.characters = context.characters();
        this.logger = context.logger(getClass());
    }

    @Api(PUBLIC)
    public void ping(Request request) {
        request.accept();
    }

    @Api(route = ANY)
    public void instanceMessage(RealmRequest request) {
        message(request, request.data());
    }

    private void message(Request request, Object msg) {
        if (!(msg instanceof JsonObject)) {
            msg = Serializer.json(msg);
        }
        context.bus().send(request.connection().getProperty(ID_INSTANCE).orElseThrow(
                () -> new CoreRuntimeException("Not connected to an instance.")
        ), msg, (reply) -> {
            request.result(reply);
            if (reply.failed()) {
                logger.onError(reply.cause());
            }
        });
    }

    @Override
    public void handle(Request request) {
        protocol.get(request.route(), authenticator(request)).submit(new RealmRequest(request));
    }

    @Api(route = CLIENT_INSTANCE_JOIN)
    public void join(RealmRequest request) {
        if (context.isConnected(request.account())) {
            throw new CoreRuntimeException("Failure: Already connected to " +
                    request.connection().getProperty(ID_INSTANCE).orElse("?"));
        } else {
            characters.findOne(find -> {
                if (find.succeeded()) {
                    PlayerCreature creature = find.result();

                    JoinMessage join = new JoinMessage()
                            .setPlayer(find.result())
                            .setRealmName(context.realm().getName());

                    // store the player characters name on the connection.
                    request.connection().setProperty(ID_NAME, creature.getName());

                    // notify the remote instance when the client disconnects.
                    request.connection().onClose(() -> {
                        leave(request);
                    });

                    // save the instance the player is connected to on the request object.
                    context.connect(creature, request.connection());
                    message(request, join);
                } else {
                    request.result(find);
                }
            }, request.account(), request.character());
        }
    }

    @Api(route = CLIENT_INSTANCE_LEAVE)
    public void leave(RealmRequest request) {
        context.remove(request);
        message(request, new LeaveMessage(request));
    }

    @Api(route = CLIENT_CHARACTER_REMOVE)
    public void characterRemove(RealmRequest request) {
        characters.remove(remove -> {
            if (remove.succeeded()) {
                request.accept();
            } else {
                request.error(remove.cause());
            }
        }, request.account(), request.character());
    }

    @Api(route = CLIENT_CHARACTER_LIST)
    public void characterList(RealmRequest request) {
        characters.findByUsername(characters -> {
            if (characters.succeeded()) {
                Collection<PlayerCreature> result = characters.result();

                if (result != null) {
                    request.write(new CharacterList(context.realm(), result));
                } else {
                    request.error(new CharacterMissingException(request.account()));
                }
            } else {
                request.error(characters.cause());
            }
        }, request.account());
    }

    @Api(route = CLIENT_CHARACTER_CREATE)
    public void characterCreate(RealmRequest request) {
        PlayerCreature creature = new PlayerCreature(request.character());
        creature.setAccount(request.account());
        creature.setClassName(request.className());

        characters.create(creation -> {
            if (creation.succeeded()) {
                request.accept();
            } else {
                request.error(new CharacterExistsException(request.character()));
            }

            // todo: create a PlayerCreature from the class template.
            //
        }, creature);
    }

    private PlayableClass readTemplate(String characterName, String className) throws PlayerClassDisabledException {

        for (PlayableClass pc : context.getClasses()) {
            if (pc.getName().equals(className))
                return pc;
        }
        throw new PlayerClassDisabledException();
    }


    private Role authenticator(Request request) {
        if (context.verifyToken(request.token())) {
            return Role.USER;
        } else {
            return Role.PUBLIC;
        }
    }
}
