package com.codingchili.banking.model

import java.time.Instant

class Bid(
        val owner: String,
        val value: Int,
        val date: Long = Instant.now().toEpochMilli()
) {}