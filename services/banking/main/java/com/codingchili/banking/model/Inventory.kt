package com.codingchili.banking.model

import com.codingchili.core.storage.Storable

class Inventory(var items: List<Item> = listOf(),
                var funds: Int = 0,
                var liquidity: Int = 0) : Storable {

    var _id: String? = null

    override fun getId(): String? {
        return _id
    }

    fun setId(id: String) {
        _id = id
    }
}