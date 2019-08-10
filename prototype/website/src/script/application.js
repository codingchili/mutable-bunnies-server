/**
 * @author Robin Duda
 *
 * Used to pass application-level events between components.
 */

class Application {

    constructor() {
        this.development = {
            autologin: true,
            selectFirstRealm: true,
            selectFirstCharacter: false,
            clearCache: true,
            rightClick: true,
            logEvents: false,
            hardResetXY: true,
            metrics: true
        };

        this.handlers = {};

        if (this.development.clearCache) {
            localStorage.clear();
        }
    }

    realmLoaded(realm) {
        application.realm = realm;
        application.publish('onRealmLoaded', realm);
    }

    characterLoaded(character) {
        application.character = character;
        application.publish('onCharacterLoaded', character);
    }

    characterUpdate(character) {
        application.publish('onCharacterUpdate', character);
    }

    authenticated(authentication) {
        application.token = authentication.token;
        application.account = authentication.account;
        application.view('realm-list');
        application.publish('onAuthentication', application);
    }

    error(error, disconnect) {
        let callback = (disconnect) ? application.showOffline : application.showLogin;

        application.publish('onLogout', {});
        application.view('error-dialog');
        application.publish('onError', {text: error, callback: callback});

        if (typeof game !== 'undefined') {
            try {
                game.shutdown(disconnect);
            } catch (e) {
                console.log(e);
            }
        }
    }

    selectRealm(realm) {
        application.realm = realm;
        application.publish('onRealmSelect', application.realm);
        application.showCharacters();
    }

    logout() {
        application.publish('onLogout', {});
        application.showLogin();
    }

    selectCharacter(event) {
        this.character = event.character;
        this.server = event.server;
        application.publish('onCharacterSelect', event);
        application.showPatcher();
    }

    updateComplete(event) {
        application.publish('onCompleteUpdate', event);
    }

    loadedVersion(event) {
        application.publish('onVersion', event);
        this.version = event;
    }

    onCompleteUpdate(callback) {
        application.subscribe('onCompleteUpdate', callback);
    }

    onAuthentication(callback) {
        application.subscribe('onAuthentication', callback);
    }

    onRealmSelect(callback) {
        application.subscribe('onRealmSelect', callback);
    }

    onError(callback) {
        application.subscribe('onError', callback);
    }

    dialogEvent(dialog) {
        application.publish('dialog', dialog);
    }

    onDialogEvent(callback) {
        application.subscribe('dialog', callback);
    }

    onLogout(callback) {
        application.subscribe('onLogout', callback);
    }

    onRealmLoaded(callback) {
        if (application.realm) {
            callback(application.realm);
        }
        application.subscribe('onRealmLoaded', callback);
    }

    onCharacterSelect(callback) {
        application.subscribe('onCharacterSelect', callback);
    }

    onCharacterLoaded(callback) {
        if (application.character) {
            callback(application.character);
        }

        application.subscribe('onCharacterLoaded', callback);
    }

    onCharacterUpdate(callback) {
        application.subscribe('onCharacterUpdate', callback);
    }

    onScriptsLoaded(callback) {
        if (this.scripts) {
            callback();
        }
        application.subscribe('onScriptsLoaded', callback);
    }

    onScriptShutdown(callback) {
        application.subscribe('onScriptShutdown', callback);
    }

    scriptsLoaded() {
        this.scripts = true;
        application.publish('onScriptsLoaded', {});
    }

    onGameLoaded(callback) {
        if (this.game) {
            callback(this.game);
        }
        this.subscribe('onGameLoaded', callback);
    }

    gameLoaded(game) {
        this.game = game;
        this.publish('onGameLoaded', game);
    }

    scriptShutdown() {
        this.game = false;
        this.scripts = false;
        application.publish('onScriptShutdown', {});
    }

    onVersion(callback) {
        application.subscribe('onVersion', callback);

        if (this.version) {
            callback(this.version)
        }
    }

    showLogin() {
        application.view('game-login');
    }

    showRealms() {
        application.view('realm-list');
    }

    onRealmSelect(callback) {
        application.subscribe('onRealmSelect', callback);
    }

    showCharacters() {
        application.view('character-list');
    }

    showPatcher() {
        application.view('patch-download');
    }

    showGame() {
        application.view('game-view');
    }

    showStart() {
        application.view('page');
        application.publish('onViewStart', {});
    }

    showOffline() {
        application.view('offline-view');
    }

    view(view) {
        this.publish('view', view);
    }

    subscribe(event, callback) {
        if (this.handlers[event] == null)
            this.handlers[event] = [];

        this.handlers[event].push(callback);
    }

    publish(event, data) {
        if (application.development.logEvents) {
            console.log(`publishing event ${event}`);
        }

        if (this.handlers[event])
            for (let subscriber = 0; subscriber < this.handlers[event].length; subscriber++)
                this.handlers[event][subscriber](data);
    }
}

var application = new Application();