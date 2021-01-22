package com.codingchili.realm.configuration;

import com.codingchili.common.RegisteredRealm;
import com.codingchili.instance.context.InstanceSettings;
import com.codingchili.instance.scripting.Scripted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import com.codingchili.core.configuration.AttributeConfigurable;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.*;
import static com.codingchili.core.files.Configurations.*;

/**
 * @author Robin Duda
 * <p>
 * Contains the settings for a realmName.
 */
@JsonIgnoreProperties({"instances"})
public class RealmSettings extends AttributeConfigurable {
    private final List<InstanceSettings> instances = new ArrayList<>();
    private Set<String> availableClasses = new HashSet<>();
    private Set<String> admins = new HashSet<>();
    private ListenerSettings listener = new ListenerSettings();
    private Scripted onPlayerJoin;
    private Scripted levelScaling;
    private Token authentication;
    private Token logging;
    private String resources;
    private String version;
    private String type;
    private String name;
    private String id;
    private String host;
    private Boolean trusted;
    private int players = 0;
    private long updated;
    private int size;

    /**
     * Checks if an overridden resource exist in PATH_GAME_OVERRIDE for the
     * specified realm.
     *
     * @param path  to the file to look if overridden.
     * @param realm the handler of the realm.
     * @return a path to the overridden resource or the path itself.
     */
    private static String override(String path, String realm) {
        String overridePath = path.replace(PATH_GAME, PATH_GAME_OVERRIDE + realm);
        File override = new File(overridePath);

        if (override.exists()) {
            return overridePath;
        } else {
            return path;
        }
    }

    /**
     * @return creates a copy of the realmsettings into a RegisteredRealm that contains
     * metadata for connecting clients. Do not include the realms token in this.
     */
    public RegisteredRealm toMetadata() {
        return new RegisteredRealm()
                .setBinaryWebsocket(listener.isBinaryWebsockets())
                .setName(name)
                .setId(id)
                .setSize(size)
                .setHost(host)
                .setAdmins(admins)
                .setPort(listener.getPort())
                .setSecure(listener.isSecure())
                .setResources(resources)
                .setVersion(version)
                .setPlayers(players)
                .setAvailableClasses(availableClasses)
                .setAttributes(attributes);
    }

    public void load() {
        available(PATH_INSTANCE).stream()
                .map(path -> override(path, id))
                .map(path -> get(path, InstanceSettings.class).setId(getInstanceIdFromPath(path)))
                .forEach(instances::add);
    }

    private String getInstanceIdFromPath(String path) {
        String fileName = Paths.get(path).getFileName().toString();
        return fileName.substring(0, Math.min(fileName.length(), fileName.lastIndexOf(".")));
    }

    /**
     * @return a list of available classes.
     */
    public Set<String> getAvailableClasses() {
        return availableClasses;
    }

    /**
     * @param availableClasses a list of available classes.
     * @return fluent
     */
    public RealmSettings setAvailableClasses(Set<String> availableClasses) {
        this.availableClasses = availableClasses;
        return this;
    }

    /**
     * @return the remote host of the realm.
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host set the remote host for this realm.
     * @return fluent
     */
    public RealmSettings setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * @return the port of this realm.
     */
    public int getPort() {
        return listener.getPort();
    }

    /**
     * @return returns true if the connection must be secure.
     */
    public Boolean getSecure() {
        return this.listener.isSecure();
    }

    /**
     * @param secure if true then all connections must be secure
     * @return fluent
     */
    private RealmSettings setSecure(Boolean secure) {
        this.listener.setSecure(secure);
        return this;
    }

    /**
     * @return the resource server where game resources are downloaded from..
     */
    public String getResources() {
        return resources;
    }

    /**
     * @param resources the resource server where resources are downloaded from.
     * @return fluent
     */
    public RealmSettings setResources(String resources) {
        this.resources = resources;
        return this;
    }

    /**
     * @return get the number of players connected.
     */
    public int getPlayers() {
        return players;
    }

    /**
     * @param players set the current number of players connected.
     * @return fluent
     */
    private RealmSettings setPlayers(int players) {
        this.players = players;
        return this;
    }

    /**
     * @return authentication token used to authenticate against the realm-registry.
     */
    public Token getAuthentication() {
        return authentication;
    }

    /**
     * @param authentication the authentication to use against the realm-registry.
     * @return fluent.
     */
    public RealmSettings setAuthentication(Token authentication) {
        this.authentication = authentication;
        return this;
    }

    public String getName() {
        return name;
    }

    public RealmSettings setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public RealmSettings setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return get the version of the realm.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set for the realm.
     * @return fluent
     */
    private RealmSettings setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return the script algorithm for determining the experience required for the next level.
     */
    public Scripted getLevelScaling() {
        return levelScaling;
    }

    /**
     * @param levelScaling the algorithm used to calculate required experience for the next level.
     */
    public void setLevelScaling(Scripted levelScaling) {
        this.levelScaling = levelScaling;
    }

    /**
     * @return the maximum number of players that may connect.
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the maximum number players that may connect
     * @return fluent
     */
    private RealmSettings setSize(int size) {
        this.size = size;
        return this;
    }

    /**
     * @return the type of the realm.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type of realm,  may be any string.
     * @return fluent
     */
    public RealmSettings setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * @return listener configuration for the realm.
     */
    public ListenerSettings getListener() {
        return listener;
    }

    /**
     * @param listener the listener configuration to use for the realm.
     * @return fluent
     */
    public RealmSettings setListener(ListenerSettings listener) {
        this.listener = listener;
        return this;
    }

    /**
     * @return true if this realm is not a third-party server.
     */
    public Boolean getTrusted() {
        return trusted;
    }

    /**
     * @param trusted indicates if this realm is a thirdparty server or not.
     * @return fluent
     */
    public RealmSettings setTrusted(Boolean trusted) {
        this.trusted = trusted;
        return this;
    }

    /**
     * @return a list of instances configured for the realm.
     */
    public List<InstanceSettings> getInstances() {
        return instances;
    }

    /**
     * @return a script that calculates the starting instance based on the given player.
     */
    public Scripted getOnPlayerJoin() {
        return onPlayerJoin;
    }

    /**
     * @param startingInstance a script that calculates the starting instance based on the given player.
     */
    public void setOnPlayerJoin(Scripted startingInstance) {
        this.onPlayerJoin = startingInstance;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RealmSettings && (((RealmSettings) other).getId().equals(id));
    }

    /**
     * @return a list of accounts that are admins on the realm.
     */
    public Set<String> getAdmins() {
        return admins;
    }

    /**
     * @param admins the list of accounts that are admins on the realm.
     */
    public void setAdmins(Set<String> admins) {
        this.admins = admins;
    }

    public Token getLogging() {
        return logging;
    }

    public void setLogging(Token logging) {
        this.logging = logging;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = Serializer.json(this);

        json.remove(ID_AFFLICTIONS);
        json.remove(ID_CLASSES);
        json.remove(ID_TEMPLATE);
        json.remove(ID_INSTANCES);

        return json;
    }

    @JsonIgnore
    public long getUpdated() {
        return updated;
    }

    /**
     * @param updated set the last time in MS when the realm was updated in the registry.
     * @return fluent
     */
    public RealmSettings setUpdated(long updated) {
        this.updated = updated;
        return this;
    }

    @JsonIgnore
    public byte[] getTokenBytes() {
        return getAuthentication().getKey().getBytes();
    }

    @JsonIgnore
    public boolean isAdmin(String target) {
        return admins.contains(target);
    }
}
