package com.codingchili.banking.model

class Stats : HashMap<String, Int>() {

    override fun toString(): String {
        return map { "${if (it.value > 0) "+${it.value}" else it.value} ${it.key}" }
            .joinToString("\n")
    }
}