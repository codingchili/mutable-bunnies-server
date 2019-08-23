package com.codingchili.core.social;

import com.codingchili.social.configuration.SocialContext;
import com.codingchili.social.model.*;
import io.vertx.core.CompositeFuture;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

/**
 * @author Robin Duda
 * <p>
 * Tests for party management.
 */
@RunWith(VertxUnitRunner.class)
public class PartyHandlerTest {
    private PartyEngine engine;
    private String A = "a";
    private String B = "b";

    @Before
    public void setUp() {
        engine = new PartyEngine(new SocialContext() {
            @Override
            public CompositeFuture send(String target, Object message) {
                return CompositeFuture.all(Collections.emptyList());
            }
        });
    }

    @Test
    public void invite() {
        engine.invite(A, B);
    }

    @Test
    public void decline(TestContext test) {
        String id = engine.invite(A, B);
        engine.decline(B, id);

        test.assertFalse(engine.list(B).contains(A));
        test.assertFalse(engine.list(A).contains(B));

        // sole party member. #sadface.
        test.assertTrue(engine.list(A).contains(A));
    }

    @Test
    public void accept(TestContext test) {
        String id = engine.invite(A, B);
        engine.accept(B, id);

        test.assertTrue(engine.list(B).contains(A));
        test.assertTrue(engine.list(A).contains(B));
    }

    @Test
    public void leave(TestContext test) {
        String id = engine.invite(A, B);
        engine.accept(B, id);
        engine.leave(B);

        // B is not referenced in any parties.
        test.assertFalse(engine.list(B).contains(A));
        test.assertFalse(engine.list(B).contains(B));

        // A is still left in party.
        test.assertTrue(engine.list(A).contains(A));
    }

    @Test
    public void declineMissingInvite() {
        try {
            engine.decline(B, "missing");
        } catch (MissingPartyInviteException e) {
            // expected.
        }
    }

    @Test
    public void acceptMissingInvite() {
        try {
            engine.accept(B, "missing");
        } catch (MissingPartyInviteException e) {
            // expected.
        }
    }

    @Test
    public void leaveWhenNotInParty() {
        engine.leave(A);
    }

    @Test
    public void inviteAlreadyPartiedPlayer() {
        String id = engine.invite(A, B);
        engine.accept(B, id);
        try {
            engine.invite(A, B);
        } catch (TargetAlreadyInPartyException e) {
            // expected.
        }
    }
}
