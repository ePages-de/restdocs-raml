package com.epages.restdocs.raml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.jayway.jsonpath.JsonPath
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be empty`
import org.junit.Test


class ToRamlMapTest: FragmentFixtures {

    val objectMapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    @Test
    fun `should convert minimal resource to raml map`() {
        val fragments = listOf(
                RamlFragment("cart-line-item-update", "/carts/{id}",
                        Method(method = "put", description = "description")
                ),
                RamlFragment("cart-get", "/carts/{id}",
                        Method(method = "get", description = "description")
                )
        )

        val ramlMap = RamlResource.fromFragments(fragments, NoOpJsonSchemaMerger).toRamlMap(RamlVersion.V_1_0)

        with (JsonPath.parse(objectMapper.writeValueAsString(ramlMap))) {
            read<String>("/carts/{id}.get.description").`should not be empty`()
            read<String>("/carts/{id}.put.description").`should not be empty`()
        }
    }

    @Test
    fun `should convert full resource to raml map`() {
        val fragments = listOf(RamlFragment.fromYamlMap("some", parsedFragmentMap { rawFullFragment() }))

        val ramlMap = RamlResource.fromFragments(fragments, NoOpJsonSchemaMerger).toRamlMap(RamlVersion.V_1_0)

        with (JsonPath.parse(objectMapper.writeValueAsString(ramlMap))) {
            read<String>("/tags/{id}.uriParameters.id.type").`should not be empty`()
            read<String>("/tags/{id}.uriParameters.id.description").`should not be empty`()
            read<String>("/tags/{id}.put.description").`should not be empty`()
            read<List<String>>("/tags/{id}.put.securedBy").size `should be equal to` 2
            read<List<String>>("/tags/{id}.put.is").size `should be equal to` 1
            read<String>("/tags/{id}.put.queryParameters.some.description").`should not be empty`()
            read<String>("/tags/{id}.put.queryParameters.some.type") `should be equal to` "integer"
            read<String>("/tags/{id}.put.queryParameters.other.description").`should not be empty`()
            read<String>("/tags/{id}.put.queryParameters.other.type") `should be equal to` "string"
            read<String>("/tags/{id}.put.body.application/hal+json.type.location").`should not be empty`()
            read<String>("/tags/{id}.put.body.application/hal+json.examples.tags-create.location").`should not be empty`()
            read<String>("/tags/{id}.put.responses.200.application/hal+json.type.location").`should not be empty`()
            read<String>("/tags/{id}.put.responses.200.application/hal+json.examples.tags-list.location").`should not be empty`()
        }
    }
}
