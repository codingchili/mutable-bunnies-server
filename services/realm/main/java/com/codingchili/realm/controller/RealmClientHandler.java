package com.codingchili.realm.controller;

import com.codingchili.instance.model.entity.PlayerCreature;
import com.codingchili.instance.model.events.JoinMessage;
import com.codingchili.instance.model.events.LeaveMessage;
import com.codingchili.instance.model.items.*;
import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.model.*;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import java.util.Collection;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.transport.Connection;
import com.codingchili.core.protocol.*;
import com.codingchili.core.security.Token;

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
    private ItemDB items;

    public RealmClientHandler(RealmContext context) {
        this.context = context;
        this.characters = context.characters();
        this.items = new ItemDB(context);
    }

    @Api(PUBLIC)
    public void ping(Request request) {
        request.accept();
    }

    @Api(route = ANY)
    public void instanceMessage(RealmRequest request) {
        JsonObject json = request.data();
        Connection connection = request.connection();

        connection.getProperty(ID_ACCOUNT).ifPresent(account -> json.put(ID_ACCOUNT, account));
        connection.getProperty(ID_NAME).ifPresent(character -> json.put(PROTOCOL_TARGET, character));

        context.sendInstance(request.instance(), request.data()).onComplete(request::result);
    }

    @Api(route = CLIENT_INSTANCE_JOIN)
    public void join(RealmRequest request) {
        characters.findOne(find -> {
            if (find.succeeded()) {
                PlayerCreature creature = find.result();

                context.onPlayerJoin(creature);

                JoinMessage join = new JoinMessage()
                        .setPlayer(find.result())
                        .setRealmName(context.realm().getId());

                // store the player character and account names on the connection.
                request.connection().setProperty(ID_NAME, creature.getName());

                // notify the remote instance when the client disconnects - register handler only once.
                request.connection().onCloseHandler("leave-instances", () -> leaveInstance(request));

                // save the instance the player is connected to on the request object.
                context.setInstance(creature, request.connection());
                context.sendInstance(request.instance(), join).onComplete(request::result);
            } else {
                request.result(find);
            }
        }, request.account(), request.character());
    }

    @Api(route = CLIENT_INSTANCE_LEAVE)
    public void leave(RealmRequest request) {
        // already left the instance, unregister the close handler to prevent blocking next.
        request.connection().removeCloseHandler("leave-instances");
        leaveInstance(request);
    }

    private void leaveInstance(RealmRequest request) {
        request.connection().getProperty(ID_NAME).ifPresent(character -> {
            context.sendInstance(request.instance(), new LeaveMessage()
                    .setPlayerName(character)
                    .setAccountName(request.account()));

            context.clearInstance(request);
        });
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
                    request.write(new CharacterList(result));
                } else {
                    request.accept();
                }
            } else {
                request.error(characters.cause());
            }
        }, request.account());
    }

    @Api
    public void connect(RealmRequest request) {
        request.write(context.realm().toMetadata());
    }

    @Api(PUBLIC)
    public void classinfo(RealmRequest request) {
        request.write(context.classes().toBuffer());
    }

    @Api(PUBLIC)
    public void spellinfo(RealmRequest request) {
        request.write(context.spells().toBuffer());
    }

    @Api(PUBLIC)
    public void afflictioninfo(RealmRequest request) {
        request.write(context.afflictions().toBuffer());
    }

    @Api(route = CLIENT_CHARACTER_CREATE)
    public void characterCreate(RealmRequest request) {
        PlayerCreature creature = new PlayerCreature(request.character());
        creature.setAccount(request.account());
        creature.setClassId(request.classId());

        Inventory inventory = creature.getInventory();

        context.classes().getById(request.classId())
                .ifPresent(classData -> {
                    classData.getItems().forEach(item -> {
                        items.getById(item).ifPresent(inventory::add);
                    });
                });

        characters.create(creation -> {
            if (creation.succeeded()) {
                request.accept();
                context.onCharacterCreated(request);
            } else {
                context.logger(getClass()).onError(creation.cause());
                request.error(new CharacterExistsException(request.character()));
            }
        }, creature);
    }

    @Override
    public void handle(Request request) {
        protocol.process(new RealmRequest(request));
    }

    @Authenticator
    public Future<Role> authenticator(Request request) {
        Connection connection = request.connection();
        Promise<Role> promise = Promise.promise();

        // websockets are persistent: only need to verify users token once.
        if ((connection.getProperty(ID_ACCOUNT).isPresent())) {
            promise.complete(Role.USER);
        } else {
            Token token = request.token();

            context.verifyToken(token).onComplete(verify -> {
                if (verify.succeeded()) {
                    if (context.isConnected(token.getDomain())) {
                        promise.fail(new CoreRuntimeException("Account is already connected to this realm."));
                    } else {
                        context.connect(connection, token.getDomain());
                        connection.setProperty(ID_ACCOUNT, token.getDomain());
                        promise.complete(Role.USER);
                    }
                } else {
                    promise.complete(Role.PUBLIC);
                }
            });
        }
        return promise.future();
    }
}
