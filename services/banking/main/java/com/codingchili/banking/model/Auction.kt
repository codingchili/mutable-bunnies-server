package com.codingchili.banking.model

import com.codingchili.core.storage.Storable
import java.time.Instant
import java.util.*
import kotlin.math.max

public class Auction(var initial: Int, var item: Item, var seller: String) : Storable {
    var bids = listOf<Bid>()
    var end: Long = Date().time + 2 * 60 * 1000
    var _id: String? = UUID.randomUUID().toString()

    override fun getId(): String? {
        return _id
    }

    fun setId(id: String) {
        _id = id
    }

    fun high(): Int {
        return max(initial, bids.firstOrNull()?.value ?: 0)
    }

    fun finished(): Boolean {
        return Instant.now().toEpochMilli() > end
    }
}