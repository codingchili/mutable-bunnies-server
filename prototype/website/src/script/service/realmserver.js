/**
 * Realmserver api.
 */
class RealmServer {

    constructor(realm) {
        this.realm = realm;
        this.connection = new Connection(realm);
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

    create(callback, classId, characterName) {
        this.connection.send('character.create', {
            classId: classId,
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

    leave() {
        this.connection.send('leave', {}, {
            accepted: () => {
                // disconnected successfully.
                console.log('on player leave')
            },
            error: (e) => {
                // failed to disconnect gracefully - connection might have closed already.
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