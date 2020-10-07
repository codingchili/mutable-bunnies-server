package com.codingchili.instance;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.SystemContext;
import com.codingchili.instance.context.GameContext;
import com.codingchili.instance.context.InstanceContext;
import com.codingchili.instance.context.InstanceSettings;
import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.configuration.RealmSettings;

/**
 * Provides test contexts.
 */
public class TestContext {

    /**
     * @return test context with a running instance.
     */
    public static GameContext game() {
        final CoreContext core = new SystemContext();
        final RealmContext realm = new RealmContext(core, RealmSettings::new);
        final InstanceSettings settings = new InstanceSettings();
        final InstanceContext instance = new InstanceContext(realm, settings);
        return new GameContext(instance);
    }
}
