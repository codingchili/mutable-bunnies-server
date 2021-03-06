package com.codingchili.instance.controller.dialog;

import com.codingchili.instance.context.*;
import com.codingchili.instance.transport.InstanceRequest;
import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmSettings;
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
        RealmSettings settings = new RealmSettings().setId("angel_oak");

        RealmContext realm = new RealmContext(new SystemContext(), () -> settings);
        InstanceContext instance = new InstanceContext(realm, new InstanceSettings());

        game = new GameContext(instance);
    }

    @Test
    @Ignore("reading scripts from the classpath is not supported - to be fixed.")
    public void testSomeDialog() {
        /*DialogPerson npc = new DialogPerson();
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
        dialog.say(instance);*/
    }

    private InstanceRequest request(Object object, String senderId) {
        return new InstanceRequest(new MessageMock(Serializer.json(object).put("target", senderId))
                .setListener((response, status) -> {
                    System.out.println(response.encodePrettily());
                }));
    }
}
