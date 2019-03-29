package com.codingchili.instance.context;

import com.codingchili.instance.model.afflictions.AfflictionDB;
import com.codingchili.instance.model.dialog.DialogDB;
import com.codingchili.instance.model.entity.EntityDB;
import com.codingchili.instance.model.npc.NpcDB;
import com.codingchili.instance.model.spells.SpellDB;
import com.codingchili.instance.scripting.ReferencedScript;
import com.codingchili.realm.model.ClassDB;
import io.vertx.core.Future;

import java.util.concurrent.*;

import com.codingchili.core.context.CoreContext;

/**
 * Runs initialization tasks required for the deployment of instances such as loading configuration
 * required by all instances and compiling scripts.
 */
public class InstancesBootstrap {

    /**
     * Attempts to initialize configurable subsystems.
     *
     * @param core the corecontext to initialize on.
     */
    public static Future<Void> bootstrap(CoreContext core) {
        Future<Void> future = Future.future();

        ReferencedScript.initialize(core).setHandler(done -> {
            if (done.succeeded()) {
                core.blocking(blocking -> {
                    ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
                    service.submit(() -> new NpcDB(core));
                    service.submit(() -> new EntityDB(core));
                    service.submit(() -> new SpellDB(core));
                    service.submit(() -> new ClassDB(core));
                    service.submit(() -> new AfflictionDB(core));
                    service.submit(() -> new DialogDB(core));
                    try {
                        service.shutdown();
                        service.awaitTermination(10, TimeUnit.SECONDS);
                        future.complete();
                    } catch (Exception e) {
                        future.fail(e);
                    }
                }, future);
            } else {
                future.fail(done.cause());
            }
        });
        return future;
    }

}

