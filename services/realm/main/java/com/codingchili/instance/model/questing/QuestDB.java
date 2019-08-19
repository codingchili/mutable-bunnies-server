package com.codingchili.instance.model.questing;

import com.codingchili.instance.model.MetadataStore;
import com.codingchili.instance.model.npc.DB;
import io.vertx.core.buffer.Buffer;

import java.util.Map;
import java.util.Optional;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.protocol.Serializer;

/**
 * @author Robin Duda
 * <p>
 * Quest database.
 */
public class QuestDB implements MetadataStore<Quest> {
    private static final String CONF_PATH = "conf/game/quests";
    private DB<Quest> quests;
    private CoreContext core;

    public QuestDB(CoreContext core) {
        this.core = core;
        this.quests = DB.create(core, Quest.class, CONF_PATH);
        this.quests.setOnInvalidate(this::evict);
    }

    @Override
    public Optional<Quest> getById(String id) {
        return quests.getById(id);
    }

    @Override
    public Map<String, Quest> asMap() {
        return quests.asMap();
    }

    @Override
    public void evict() {
        quests.asMap().values().forEach(quest -> {
            // perform sanity checks for quests.
            if (quest.getStage().size() == 0) {
                core.logger(getClass())
                        .onError(new InvalidQuestException("Quest " + quest.getId() + " is missing stages."));
            }
        });
    }

    @Override
    public Buffer toBuffer() {
        return Serializer.buffer(asMap());
    }
}
