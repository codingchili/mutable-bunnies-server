package com.codingchili.realm.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

import static com.codingchili.common.Strings.EXT_JSON;
import static com.codingchili.common.Strings.PATH_REALM;

/**
 * @author Robin Duda
 */
public class EnabledRealm {
    private String realm;

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    @JsonIgnore
    public String getPath() {
        return PATH_REALM + realm + EXT_JSON;
    }
}
