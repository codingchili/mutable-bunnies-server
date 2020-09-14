package com.codingchili.banking.model

/**
 * Queries defined on the server side.
 */
enum class QueryType {
    // auctions marked as favories
    favorites,

    // selling
    sold,
    active,
    not_sold,
    won,

    // buying
    leading,
    overbid,
    lost,
    ending_soon,

    seller,

    // free text search.
    text,

    // quick searches
    consumable,
    quest,
    slot,
    weapon_type,
    armor_type,
    rarity;

    fun readableName(): String {
        return name.replace("_", " ");
    }
}