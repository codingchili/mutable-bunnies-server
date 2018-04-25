package com.codingchili.common;

import java.util.*;

import com.codingchili.core.security.Token;
import com.codingchili.core.storage.Storable;

/**
 * @author Robin Duda
 * <p>
 * Contains information about a realm, received from a realmserver..
 */
public class RegisteredRealm implements Storable {
    private Map<String, Object> attributes = new HashMap<>();
    private Set<String> availableClasses = new HashSet<>();
    private Token authentication;
    private String resources;
    private String host;
    private String version;
    private String node;
    private Boolean trusted;
    private Boolean secure;
    private long updated;
    private int players = 0;
    private int port;
    private int size;

    @Override
    public String getId() {
        return node;
    }

    /**
     * @return the resource server for the realm.
     */
    public String getResources() {
        return resources;
    }

    /**
     * @param resources set the resource server for the realm.
     * @return fluent.
     */
    public RegisteredRealm setResources(String resources) {
        this.resources = resources;
        return this;
    }

    /**
     * @return get the version of the realm.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version set the version of the realm.
     * @return fluent.
     */
    public RegisteredRealm setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * @return a list of classes available for play on the realm.
     */
    public Set<String> getAvailableClasses() {
        return availableClasses;
    }

    /**
     * @param availableClasses set the list of available classes on the realm.
     * @return fluent.
     */
    public RegisteredRealm setAvailableClasses(Set<String> availableClasses) {
        this.availableClasses = availableClasses;
        return this;
    }

    /**
     * @return the authentication token used to authenticate from the realm to
     * the realm registry. The key of this token is also used to generate clients realm
     * token.
     */
    public Token getAuthentication() {
        return authentication;
    }

    /**
     * @param authentication set the authentication token used to authenticate
     *                       from the realm to the realm registry. The key of othis
     *                       token is also used to generate clients realm token.
     * @return fluent.
     */
    public RegisteredRealm setAuthentication(Token authentication) {
        this.authentication = authentication;
        return this;
    }

    /**
     * @return maximum number of players that may connect to the server.
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size sets the maximum number of players that may connect to the server.
     * @return fluent
     */
    public RegisteredRealm setSize(int size) {
        this.size = size;
        return this;
    }


    /**
     * @return the public hostname of the realm server.
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host set the public hostname of the realm server.
     * @return fluent
     */
    public RegisteredRealm setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * @return the port of the realm server.
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port sets the port of the realm server.
     * @return fluent
     */
    public RegisteredRealm setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * @return get the number of connected players.
     */
    public int getPlayers() {
        return players;
    }

    /**
     * @param players set the number of connected players.
     * @return fluent
     */
    public RegisteredRealm setPlayers(int players) {
        this.players = players;
        return this;
    }

    /**
     * @return true if the realm is trusted.
     */
    public Boolean getTrusted() {
        return trusted;
    }

    /**
     * @param trusted indicates if the realm is trusted, non third-party server.
     * @return fluent
     */
    public RegisteredRealm setTrusted(Boolean trusted) {
        this.trusted = trusted;
        return this;
    }

    /**
     * @return true if the connection is secure.
     */
    public Boolean getSecure() {
        return secure;
    }

    /**
     * @param secure indicates if the connection to the server must be secured.
     * @return fluent
     */
    public RegisteredRealm setSecure(Boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * @return the epoch ms in which the realm was last updated.
     */
    public long getUpdated() {
        return updated;
    }

    /**
     * @param updated sets the time in epoch ms when the realm was last updated in the registry.
     * @return fluent
     */
    public RegisteredRealm setUpdated(long updated) {
        this.updated = updated;
        return this;
    }

    /**
     * @return the name of the realm.
     */
    public String getNode() {
        return node;
    }

    /**
     * @param node the new name of the realm.
     * @return fluent
     */
    public RegisteredRealm setNode(String node) {
        this.node = node;
        return this;
    }

    /**
     * @return a set of extra attributes for the realm.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes a set of extra attributes to set.
     * @return fluent
     */
    public RegisteredRealm setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "{name=" + node;
    }

    @Override
    public boolean equals(Object other) {
        return compareTo(other) == 0;
    }
}
