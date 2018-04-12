package com.epages.restdocs.raml

import java.io.File

object NoOpJsonSchemaMerger: JsonSchemaMerger(File("none.json")) {
        override fun mergeSchemas(schemas: List<Include>): Include = schemas.first()
    }
