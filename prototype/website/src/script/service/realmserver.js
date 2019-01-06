/**
 * Realmserver api.
 */
class RealmServer {

    constructor(realm) {
        this.realm = realm;
        this.connection = new Connection(realm.host, realm.port, realm.secure);
    }

    connect(callback) {
        this.connection.send('connect', {
            token: this.realm.token
        }, callback);
    }

    characterlist(callback) {
        this.connection.send('character.list', {}, callback);
    }

    afflictioninfo(callback) {
        this.connection.send('afflictioninfo', {}, callback);
    }

    spellinfo(callback) {
        this.connection.send('spellinfo', {}, callback);
    }

    classinfo(callback) {
        this.connection.send('classinfo', {}, callback);
    }

    create(callback, className, characterName) {
        this.connection.send('character.create', {
            className: className,
            character: characterName
        }, callback);
    }

    remove(callback, characterName) {
        this.connection.send('character.remove', {
            character: characterName
        }, callback);
    }

    join(callback, characterName) {
        this.connection.send('join', {
            character: characterName
        }, callback);
    }

    cast(callback, spellName, spellTarget) {
        this.connection.send('cast', {
            spellName: spellName,
            spellTarget: spellTarget
        }, callback);
    }

    leave() {
        this.connection.send('leave', {}, {
            accepted: () => {
                // disconnected successfully.
                console.log('on player leave')
            },
            error: (e) => {
                // failed to disconnect gracefully.
                application.error(e.message);
            }
        });
    }

    close() {
        this.connection.close();
    }

    static ping(callback, realm) {
        new Network()
            .setPort(realm.port)
            .setHost(realm.host)
            .ping(callback);
    }
}