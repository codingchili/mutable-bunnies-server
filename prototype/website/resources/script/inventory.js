window.Inventory = class Inventory {

    constructor() {
    }

    requestLootList(entity) {
        server.connection.send('loot_list', {targetId: entity.id});
    }

    unsubscribeLootList(entity) {
        server.connection.send('loot_unsubscribe', {entityId: entity.id});
    }
};