@file:Suppress("UNCHECKED_CAST")

package com.epages.restdocs.raml


fun Map<*, *>.anyMatchRecursive(predicate: Map.Entry<*, *>.() -> Boolean): Boolean {
    return this.map {
        if (it.predicate())
            return@map true
        else {
            when (it.value) {
                is Map<*, *> -> (it.value as Map<String, Any>).anyMatchRecursive(predicate)
                else -> false
            }
        }
    }.any { it }
}

fun Map<*, *>.replaceLKeyRecursive(oldKey: String, newKey: String): Map<*, *> {
    return this.map {
        if (it.key == oldKey)
            Pair(newKey, it.value)
        else {
            when (it.value) {
                is Map<*, *> -> Pair(it.key, (it.value as Map<*, *>).replaceLKeyRecursive(oldKey, newKey))
                else -> it.toPair()
            }
        }
    }.toMap()
}
