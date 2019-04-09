package com.codingchili.instance.model.spells;

import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.Ticker;
import com.codingchili.instance.model.entity.Model;
import com.codingchili.instance.model.entity.Vector;
import com.codingchili.instance.scripting.Bindings;
import com.codingchili.instance.scripting.Scripted;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A spell collider in the form of a projectile.
 */
public class Projectile {
    private static final String SPELLS = "spells";
    public static final int EXPLOSION_AREA = 512;
    public static final float DEFAULT_VELOCITY = 4.0f;
    public static final int DEFAULT_SIZE = 16;
    private AtomicBoolean done = new AtomicBoolean(false);
    private ActiveSpell spell;
    private GameContext game;
    private Model model = new Model();
    private long ttl = 10_000;
    private boolean explode = false;
    private String id = UUID.randomUUID().toString();
    private Vector vector;

    /**
     * Creates a new projectile.
     *
     * @param game  the game context the projectile will be added to.
     * @param spell a reference to the spell being casted.
     */
    public Projectile(GameContext game, ActiveSpell spell) {
        this.game = game;
        this.spell = spell;

        Vector source = spell.getSource().getVector();
        Vector target = spell.getTarget().getVector();

        float direction = (float) Math.atan2(target.getY() - source.getY(), target.getX() - source.getY());

        this.vector = spell.getSource().getVector()
                .copy()
                .setDirection(direction)
                .setVelocity(DEFAULT_VELOCITY)
                .setSize(DEFAULT_SIZE);
    }

    /**
     * @param ms time to live for the projectile in ms, if unset the default ttl is {@link #ttl}.
     *           The projectile will expire on collision if the model object is set to blocking.
     * @return fluent.
     */
    public Projectile setTtl(long ms) {
        this.ttl = ms;
        return this;
    }

    /**
     * @return time in milliseconds until the projectile expires.
     */
    public long getTtl() {
        return ttl;
    }

    /**
     * @return the unique ID of the projectile for client referencing.
     */
    public String getId() {
        return id;
    }

    /**
     * @param explode indicates if the projectile should explode when it expires.
     * @return fluent.
     */
    public Projectile setExplosion(boolean explode) {
        this.explode = explode;
        return this;
    }

    /**
     * @param direction the direction of the projectile.
     * @return fluent.
     */
    public Projectile setDirection(float direction) {
        vector.setDirection(direction);
        return this;
    }

    /**
     * @param velocity the constant speed at which the projectile moves.
     * @return fluent.
     */
    public Projectile setVelocity(float velocity) {
        vector.setVelocity(velocity);
        return this;
    }

    /**
     * @return the model object used by the projectile.
     */
    public Model getModel() {
        return model;
    }

    /**
     * @param model the model object used by the projectile.
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * @param size the size of the projectile.
     * @return fluent.
     */
    public Projectile setSize(int size) {
        vector.setSize(size);
        return this;
    }

    /**
     * @return the vector of the projectile.
     */
    public Vector getVector() {
        return vector;
    }

    /**
     * Updates the projectile and performs collision checking.
     *
     * @param ticker the ticker object that is updating the projectiles.
     * @return true if the projectile has expired.
     */
    public boolean tick(Ticker ticker) {
        AtomicBoolean hit = new AtomicBoolean(false);

        vector.forward(ticker);
        Scripted onHit = spell.getSpell().getOnSpellActive();
        Bindings bindings = getBindings();

        ttl -= ticker.deltaMS();

        if (ttl <= 0) {
            done.set(true);
            expire();
        } else {
            game.creatures().radius(vector).forEach(creature -> {
                hit.set(true);

                if (onHit != null) {
                    bindings.setTarget(creature);
                    onHit.apply(bindings);
                }
                if (model.isBlocking()) {
                    // if not blocking passes through creatures.
                    done.set(true);
                }
            });
            if (done.get()) {
                expire();
            }
        }
        return done.get();
    }


    // todo:
    // implement projectiles on the client :)
    // use animated sprite, or use particle system. or combine?

    private void expire() {
        if (explode) {
            Bindings bindings = getBindings();
            Scripted onHit = spell.getSpell().getOnSpellActive();
            // todo: send explosion events to all creatures within the networked partition.
            game.creatures().radius(vector.copy().setSize(EXPLOSION_AREA)).forEach(creature -> {
                if (onHit != null) {
                    bindings.setTarget(creature);
                    onHit.apply(bindings);
                }
            });
        } else {
            // todo: send expire event to client.
        }
    }

    /**
     * @return creates the script bindings used for the on-hit effect.
     */
    private Bindings getBindings() {
        Bindings bindings = new Bindings();
        bindings.setSource(spell.getSource());
        bindings.put(SPELLS, game.spells());
        return bindings;
    }
}
