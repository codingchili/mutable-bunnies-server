package com.codingchili.social.model;

import java.util.*;

/**
 * @author Robin Duda
 *
 * A list of suggestions of accounts to add as friend.
 */
public class SuggestionList {
    private Collection<String> suggestions;

    public SuggestionList(Collection<String> list) {
        this.suggestions = list;
    }

    public Collection<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(Collection<String> suggestions) {
        this.suggestions = suggestions;
    }
}
