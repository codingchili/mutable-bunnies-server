package com.codingchili.social.model;

import com.codingchili.social.configuration.SocialContext;

import java.util.*;

/**
 * @author Robin Duda
 * <p>
 * The engine that drives the party-time.
 */
public class PartyEngine {
    private static final int INVITE_TIMEOUT = 30_000;
    private Map<String, Set<String>> parties = new HashMap<>(); // party id to group.
    private Map<String, String> reverse = new HashMap<>();      // account to group.
    private Map<String, String> invites = new HashMap<>();      // invited account to group.
    private SocialContext context;

    public PartyEngine(SocialContext context) {
        this.context = context;
    }


    /**
     * Lists all the members in the party for the given account.
     *
     * @param account the id of a member in a party.
     * @return a list of id's of the party members if there are any, otherwise empty.
     */
    public Set<String> list(String account) {
        if (reverse.containsKey(account)) {
            String id = reverse.get(account);
            return parties.get(id);
        } else {
            return Collections.emptySet();
        }
    }


    /**
     * Sends a party invite to another account using cross-realm routing.
     *
     * @param account the account that is sending the party invite.
     * @param friend  the account that is receiving the party invite.
     */
    public String invite(String account, String friend) {
        if (reverse.containsKey(friend)) {
            throw new TargetAlreadyInPartyException(friend);
        } else {
            String id;
            if (reverse.containsKey(account)) {
                id = reverse.get(account);
                invites.put(friend, id);
                context.send(friend, new PartyInviteMessage(account, friend, id));
            } else {
                id = UUID.randomUUID().toString();
                invites.put(friend, id);
                reverse.put(account, id);
                parties.computeIfAbsent(id, partyId -> {
                    HashSet<String> members = new HashSet<>();
                    members.add(account);
                    return members;
                });
                context.send(friend, new PartyInviteMessage(account, friend, id));
            }
            context.timer(INVITE_TIMEOUT, (handler) -> {
                String partyId = invites.get(account);

                // if this is the last invitation after 30 seconds remove the invitation.
                // if it's not; a more recent timer has been scheduled.
                if (id.equals(partyId)) {
                    invites.remove(account);
                }
            });
            return id;
        }
    }


    /**
     * Leaves any current parties if any.
     *
     * @param account the account that is leaving.
     */
    public void leave(String account) {
        String id = reverse.remove(account);

        if (id != null) {
            parties.computeIfAbsent(id, partyId -> new HashSet<>());
            Set<String> members = parties.get(id);
            members.remove(account);

            if (members.isEmpty()) {
                parties.remove(id);
            } else {
                if (members.size() == 1 && !invites.containsKey(id)) {
                    // when the second last member leaves and there are no active invites, disband.
                    parties.remove(id);
                    reverse.remove(members.iterator().next());
                }
                PartyLeaveMessage msg = new PartyLeaveMessage(account, id);
                members.forEach(member -> context.send(member, msg.setTarget(member)));
            }
        }
    }


    /**
     * Accepts a party invitation by using the party id, requires that there is already
     * an invitation for the account and party id combination. Invitations cannot be cancelled
     * only accepted or declined by the recipient.
     *
     * @param account the account that is accepting an invitation.
     * @param party   the id of the party of which to accept.
     */
    public void accept(String account, String party) {
        Set<String> members = parties.get(party);

        if (invites.remove(account) != null) {
            reverse.put(account, party);

            PartyInviteResponseMessage response = new PartyInviteResponseMessage(account, party, true);
            members.forEach(member -> context.send(member, response.setTarget(member)));

            members.add(account);
        } else {
            throw new MissingPartyInviteException(party);
        }
    }


    /**
     * @param account
     * @param party
     */
    public void decline(String account, String party) {
        parties.computeIfAbsent(party, id -> new HashSet<>());
        Set<String> members = parties.get(party);

        if (invites.remove(account) != null) {
            PartyInviteResponseMessage response = new PartyInviteResponseMessage(account, party, false);
            members.forEach(member -> context.send(member, response.setTarget(member)));

            // when the last invitation is declined and only the creator is left, disband.
            if (!invites.containsKey(party) && members.size() == 1) {
                reverse.remove(members.iterator().next());
                parties.remove(party);
            }

        } else {
            throw new MissingPartyInviteException(party);
        }
    }

    public void message(String account, String message) {
        PartyChatMessage msg = new PartyChatMessage(account, message);
        list(account).forEach(member -> context.send(member, msg.setTarget(member)));
    }
}
