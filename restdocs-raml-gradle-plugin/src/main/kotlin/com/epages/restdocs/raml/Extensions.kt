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

fun Map<*, *>.replaceMapEntryRecursive(oldKey: String, newKey: String = oldKey, valueProvider: (Any) -> Any = { value -> value } , doNotTraverse: List<String> = emptyList()): Map<*, *> {
    return this.map {
        if (it.key == oldKey)
            Pair(newKey, valueProvider(it.value!!))
        else  {
            when (it.value) {
                is Map<*, *> ->
                    if (!doNotTraverse.contains(it.key))
                        Pair(it.key, (it.value as Map<*, *>).replaceMapEntryRecursive(oldKey, newKey, valueProvider, doNotTraverse))
                    else it.toPair()
                else -> it.toPair()
            }
        }
    }.toMap()
}

fun Map<*, *>.findValueByKeyRecursive(keyToFind: String, doNotTraverse: List<String> = emptyList()): Any? {
    this.forEach { (key, value) ->
        if (keyToFind == key) return value
        else if (value is Map<*, *> && !doNotTraverse.contains(key)) {
            val result = value.findValueByKeyRecursive(keyToFind, doNotTraverse)
            if (result != null) return result
        }
    }
    return null
}
