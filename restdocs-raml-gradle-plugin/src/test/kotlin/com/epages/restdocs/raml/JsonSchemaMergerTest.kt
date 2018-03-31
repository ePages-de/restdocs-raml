package com.epages.restdocs.raml

import com.jayway.jsonpath.JsonPath
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should exist`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.shouldContainAll
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class JsonSchemaMergerTest {

    @Rule @JvmField val tempFolder = TemporaryFolder().also { it.create() }

    lateinit var includes: List<Include>


    val schema1 = """{
  "type" : "object",
  "properties" : {
    "name" : {
      "description" : "The name of the shipping method.",
      "type" : "string"
    },
    "weightBasedPrice" : {
      "description" : "The price depending on the package weight.",
      "type" : "object",
      "properties" : {
        "weightPriceThresholds" : {
          "description" : "A list of package prices that are valid up to the respective maximum weight.",
          "type" : "array"
        },
        "unlimitedWeightPrice" : {
          "description" : "The price for the package if its weight exceeds the highest weight threshold. If this value is not available, the shipping method is not applicable.",
          "type" : "object"
        }
      }
    }
  },
  "required": ["other"]

}""".trimIndent()

    val schema2 = """{
  "type" : "object",
  "properties" : {
    "name" : {
      "description" : "The name of the shipping method.",
      "type" : "string"
    },
    "fixedPrice" : {
      "description" : "The fixed price for the shipping method irrespective of weight, dimensions, etc.",
      "type" : "object"
    }
  },
  "required": ["name"]
}""".trimIndent()

    val schema3 = """{
  "type" : "object",
  "properties" : {
    "third" : {
      "description" : "The fixed price for the shipping method irrespective of weight, dimensions, etc.",
      "type" : "object"
    }
  },
  "required": ["third"]
}""".trimIndent()

    @Test
    fun `should merge three schemas`() {
        val jsonSchemaMerger = JsonSchemaMerger(tempFolder.root)
        givenIncludes(schema1, schema2, schema3)

        val result = jsonSchemaMerger.mergeSchemas(includes)

        result.`should not be null`()
        result.location `should be equal to` "schema0-merged.json"
        with(File(tempFolder.root, result.location)) {
            this.`should exist`()
            val mergedSchema = this.readText()
            JsonPath.read<Map<*,*>>(mergedSchema, "properties.weightBasedPrice").`should not be null`()
            JsonPath.read<Map<*,*>>(mergedSchema, "properties.fixedPrice").`should not be null`()
            JsonPath.read<Map<*,*>>(mergedSchema, "properties.third").`should not be null`()
            JsonPath.read<List<String>>(mergedSchema, "required") shouldContainAll listOf("other", "name", "third")
        }
    }

    @Test
    fun `should merge two schemas`() {
        val jsonSchemaMerger = JsonSchemaMerger(tempFolder.root)
        givenIncludes(schema1, schema2)

        val result = jsonSchemaMerger.mergeSchemas(includes)

        result.`should not be null`()
        with(File(tempFolder.root, result.location)) {
            this.`should exist`()
            val mergedSchema = this.readText()
            JsonPath.read<Map<*,*>>(mergedSchema, "properties.weightBasedPrice").`should not be null`()
            JsonPath.read<Map<*,*>>(mergedSchema, "properties.fixedPrice").`should not be null`()
            JsonPath.read<List<String>>(mergedSchema, "required") shouldContainAll listOf("other", "name")
        }
    }

    @Test
    fun `should return single input`() {
        val jsonSchemaMerger = JsonSchemaMerger(tempFolder.root)
        givenIncludes(schema1)

        val result = jsonSchemaMerger.mergeSchemas(includes)

        result `should be` includes.first()
    }

    private fun givenIncludes(vararg schemas: String) {
        includes = schemas.mapIndexed { index, s -> File(tempFolder.root, "schema$index.json").apply { writeText(s) }.let { file -> Include(file.name) } }
    }

}
