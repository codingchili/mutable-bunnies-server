window.InventoryHandler = class InventoryHandler {

    constructor() {
        this.callbacks = [];
        this.inventory = application.character.inventory;

        server.connection.setHandler('inventory_update', (event) => {
            this._handleCurrencyUpdate(event);

            this.inventory = event.inventory;
            this.callbacks.forEach(callback => {
                callback(event.inventory);
            });
        });

        input.onKeysListener({
            down: () => {
                application.publish('show-inventory');
            }
        }, 'h');

        server.connection.setHandler('equip_item', (event) => {
            // use this to update rendered equipment.
        });
    }

    _handleCurrencyUpdate(event) {
        let current = this.inventory.currency;
        let updated = event.inventory.currency;

        if (current !== updated) {
            if (current < updated) {
                game.chat.add({text: `You gained ${updated - current}gp.`, system: true});
            } else {
                game.chat.add({text: `You lost ${Math.abs(current - updated)}gp.`, system: true});
            }
        }
    }

    onInventoryUpdated(callback) {
        this.callbacks.push(callback);
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

    unequipItem(item) {
        server.connection.send('unequip_item', {slot: item.slot});
    }

    useItem(item) {
        server.connection.send('use_item', {itemId: item.id});
    }
};