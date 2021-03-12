package com.codingchili.logging.configuration;

import com.codingchili.common.Strings;
import com.codingchili.core.configuration.ServiceConfigurable;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.storage.JsonMap;
import io.vertx.core.json.JsonObject;

/**
 * @author Robin Duda
 * Contains settings for the logserver.
 */
public class LogServerSettings extends ServiceConfigurable {
    static final String PATH_LOGSERVER = Strings.getService("logging");
    private byte[] loggingSecret;
    private byte[] clientSecret;
    private Boolean console = true;
    private Boolean storage = false;
    private String db = "logging";
    private String collection = "events";
    private String plugin = JsonMap.class.getCanonicalName();
    private JsonObject elastic;

    public LogServerSettings() {
        this.path = PATH_LOGSERVER;
    }

    public static LogServerSettings get() {
        return Configurations.get(PATH_LOGSERVER, LogServerSettings.class);
    }

    /**
     * @return true if received messages are to be printed to console.
     */
    public Boolean getConsole() {
        return console;
    }

    /**
     * @param console indicates whether received messages are printed to console.
     */
    public void setConsole(Boolean console) {
        this.console = console;
    }

    /**
     * @return the secret key for logging tokens.
     */
    public byte[] getLoggingSecret() {
        return loggingSecret;
    }

    /**
     * @param secret sets the secret key for logging tokens.
     */
    public void setLoggingSecret(byte[] secret) {
        this.loggingSecret = secret;
    }

    /**
     * @return the secret used to verify client tokens.
     */
    public byte[] getClientSecret() {
        return clientSecret;
    }

    /**
     * @param clientSecret the secret used to verify client tokens.
     */
    public void setClientSecret(byte[] clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * @return the database that logs are stored in.
     */
    public String getDb() {
        return db;
    }

    /**
     * @param db sets the database that logs are stored in.
     */
    public void setDb(String db) {
        this.db = db;
    }

    /**
     * @return the handler of the collection that messages are stored in.
     */
    public String getCollection() {
        return collection;
    }

    /**
     * @param collection sets the handler of the collection where messages are stored.
     */
    public void setCollection(String collection) {
        this.collection = collection;
    }

    /**
     * @return the plugin to use for storing received log messages.
     */
    public String getPlugin() {
        return plugin;
    }

    /**
     * @param plugin sets the plugin to use for storing received log messages.
     */
    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    /**
     * @return elasticsearch mapping and index settings.
     */
    public JsonObject getElastic() {
        return elastic;
    }

    /**
     * @param elastic contains elasticsearch 'mapping' and/or 'index' settings.
     */
    public void setElastic(Object elastic) {
        this.elastic = Serializer.json(elastic);
    }

    /**
     * @return true if plugin-based storage should be used.
     */
    public Boolean getStorage() {
        return storage;
    }

    /**
     * @param storage true if plugin-based storage should be used.
     */
    public void setStorage(Boolean storage) {
        this.storage = storage;
    }
}