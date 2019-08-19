window.QuestHandler = class QuestHandler {

    constructor() {
        server.connection.setHandler('quest_accepted', event => {
            this._accepted(event);
        });

        server.connection.setHandler('quest_updated', event => {
            this._updated(event);
        });

        server.connection.setHandler('quest_complete', event => {
            this._completed(event);
        });

        input.onKeysListener({
            down: () => {
                application.publish('show-quests');
            }
        }, 'k');
    }

    _accepted(event) {
        application.publish('notification', `Quest accepted ${event.name}.`);
    }

    _updated(event) {
        application.publish('notification', `Quest log updated for ${event.name}.`);
    }

    _completed(event) {
        application.publish('notification', `Completed quest ${event.name}.`);
    }

    list(callback) {
        server.connection.send('quest_list', {}, callback);
    }

    details(callback, questId) {
        server.connection.send('quest_details', {
            questId: questId
        }, callback);
    }
};