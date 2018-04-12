package com.epages.restdocs.raml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.File


open class JsonSchemaMerger(private val directory: File) {
    private val objectMapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    open fun mergeSchemas(schemas: List<Include>): Include {
        val targetInclude = schemas
                .sortedBy { it.location }
                .first()
                .let { Include(it.location.replace(".json", "-merged.json")) }

        return schemas.reduce { i1, i2 ->
            objectMapper.readValue(fileFromInclude(i1), Map::class.java)
                    .let { objectMapper.readerForUpdating(it) }
                    .let { it.readValue<Map<*,*>>(fileFromInclude(i2)) }
                    .let { objectMapper.writeValue(fileFromInclude(targetInclude), it) }
                    .let { targetInclude }
        }
    }

    private fun fileFromInclude(include: Include) = File(directory, include.location)
}
