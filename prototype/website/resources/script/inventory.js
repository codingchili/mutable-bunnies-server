window.Inventory = class Inventory {

    constructor() {
        this.onInventoryUpdated = () => {
        };

        server.connection.setHandler('inventory_update', (event) => {
            this.onInventoryUpdated(event.inventory);
        });
    }

    requestLootList(entity) {
        server.connection.send('loot_list', {targetId: entity.id});
    }

    unsubscribeLootList(entity) {
        server.connection.send('loot_unsubscribe', {entityId: entity.id});
    }

    takeLoot(entity, item) {
        server.connection.send('loot_item', {
            targetId: entity.id,
            itemId: item.id
        });
    }

    equipItem(item) {
        server.connection.send('equip_item', {itemId: item.id});
    }

    useItem(item) {
        server.connection.send('use_item', {itemId: item.id});
    }
};