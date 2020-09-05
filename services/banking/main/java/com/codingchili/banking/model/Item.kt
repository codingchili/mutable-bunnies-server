package com.codingchili.banking.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.vertx.core.json.JsonObject

class Item(
        val icon: String,
        val quantity: Int = 0,
        val rarity: ItemRarity,
        val name: String,
        val description: String,
        val stats: Stats = Stats(),
        val slot: String? = null,
        val type: String? = null
) {
    lateinit var id: String

    @JsonIgnore
    var source: JsonObject? = null

    @JsonIgnore
    fun toJson(): JsonObject {
        return source!!
    }
}