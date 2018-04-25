package com.codingchili.realm.controller;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.instance.model.entity.PlayerCreature;
import com.codingchili.realm.instance.model.events.JoinMessage;
import com.codingchili.realm.instance.model.events.LeaveMessage;
import com.codingchili.realm.instance.transport.FasterRealmInstanceCodec;
import com.codingchili.realm.model.*;
import io.vertx.core.Future;
import io.vertx.core.eventbus.*;
import io.vertx.core.json.JsonObject;

import java.util.Collection;

import com.codingchili.core.context.CoreRuntimeException;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.listener.transport.Connection;
import com.codingchili.core.logging.Logger;
import com.codingchili.core.protocol.*;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.configuration.CoreStrings.ANY;
import static com.codingchili.core.protocol.RoleMap.PUBLIC;
import static io.vertx.core.eventbus.ReplyFailure.*;

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
    private DeliveryOptions delivery = new DeliveryOptions()
            .setCodecName(FasterRealmInstanceCodec.getName());

    public RealmClientHandler(RealmContext context) {
        this.context = context;
        this.logger = context.logger(getClass());
        this.characters = context.characters();
        delivery.setSendTimeout(context.realm().getListener().getTimeout());
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

        sendInstance(request.instance(), request.data()).setHandler(request::result);
    }

    private Future<Object> sendInstance(String instance, Object data) {
        Future<Object> future = Future.future();

        context.bus().send(instance, data, delivery, handler -> {
            // only process missing handlers and recipient failures - timeouts are expected.
            if (handler.failed() && handler.cause() instanceof ReplyException) {
                ReplyFailure type = ((ReplyException) handler.cause()).failureType();
                if (type == NO_HANDLERS || type == RECIPIENT_FAILURE) {
                    logger.onError(handler.cause());
                    future.fail(new CoreRuntimeException(throwableToString(handler.cause())));
                }
            }
            // ignore failures - the instance does not need to respond synchronously.
            // if they do respond - make sure the client receives it.
            if (handler.succeeded()) {
                future.complete(handler.result().body());
            }
        });
        return future;
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

                    context.onPlayerJoin(creature);

                    JoinMessage join = new JoinMessage()
                            .setPlayer(find.result())
                            .setRealmName(context.realm().getNode());

                    // store the player character and account names on the connection.
                    request.connection().setProperty(ID_NAME, creature.getName());

                    // notify the remote instance when the client disconnects - register handler only once.
                    request.connection().onCloseHandler("leaveInstances", () -> {
                       leave(request);
                    });

                    // save the instance the player is connected to on the request object.
                    context.connect(creature, request.connection());
                    sendInstance(request.instance(), join).setHandler(request::result);
                } else {
                    request.result(find);
                }
            }, request.account(), request.character());
        }
    }

    @Api(route = CLIENT_INSTANCE_LEAVE)
    public void leave(RealmRequest request) {
        context.remove(request);
        request.connection().getProperty(ID_NAME).ifPresent(character -> {
            sendInstance(request.instance(), new LeaveMessage()
                    .setPlayerName(character)
                    .setAccountName(request.account()));
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

    @Api
    public void classinfo(RealmRequest request) {
        request.write(context.classes().toBuffer());
    }

    @Api
    public void spellinfo(RealmRequest request) {
        request.write(context.spells().toBuffer());
    }

    @Api
    public void afflictioninfo(RealmRequest request) {
        request.write(context.afflictions().toBuffer());
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
        }, creature);
    }

    @Override
    public void handle(Request request) {
        protocol.get(request.route(), authenticator(request)).submit(new RealmRequest(request));
    }

    private Role authenticator(Request request) {
        // websockets are persistent: only need to verify users token once.
        if ((request.connection().getProperty(ID_ACCOUNT).isPresent())) {
            return Role.USER;
        } else {
            Token token = request.token();
            if (context.verifyToken(token)) {
                request.connection().setProperty(ID_ACCOUNT, token.getDomain());
                return Role.USER;
            } else {
                return Role.PUBLIC;
            }
        }
    }
}
