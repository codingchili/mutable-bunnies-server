package com.codingchili.instance.controller.dialog;

import com.codingchili.instance.context.*;
import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmSettings;
import com.codingchili.instance.controller.DialogHandler;
import com.codingchili.instance.model.dialog.DialogRequest;
import com.codingchili.instance.model.npc.DialogPerson;
import com.codingchili.instance.model.npc.TalkingPerson;
import com.codingchili.instance.transport.InstanceRequest;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import com.codingchili.core.context.SystemContext;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.testing.MessageMock;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class DialogHandlerTest {
    private GameContext game;

    @Before
    public void setUp() {
        RealmSettings settings = new RealmSettings().setNode("Angel Oak");

        RealmContext realm = new RealmContext(new SystemContext(), () -> settings);
        InstanceContext instance = new InstanceContext(realm, new InstanceSettings());

        game = new GameContext(instance);
    }

    @Test
    @Ignore("reading scripts from the classpath is not supported - to be fixed.")
    public void testSomeDialog() {
        DialogPerson npc = new DialogPerson();
        TalkingPerson player = new TalkingPerson();

        game.add(npc);
        game.add(player);

        DialogHandler dialog = new DialogHandler(game);
        DialogRequest request = new DialogRequest();

        request.setTargetId(npc.getId());
        InstanceRequest instance = request(request, player.getId());

        dialog.talk(instance);

        request.setNext("test2");
        instance = request(request, player.getId());
        dialog.say(instance);

        request.setNext("test3");
        instance = request(request, player.getId());
        dialog.say(instance);
    }

    private InstanceRequest request(Object object, String senderId) {
        return new InstanceRequest(new MessageMock("", (response, status) -> {
            System.out.println(response.encodePrettily());
        }, Serializer.json(object).put("target", senderId)));
    }
}
