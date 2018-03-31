package com.epages.restdocs.raml

import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.junit.Test


class RamlResourceTest {

    @Test
    fun `should merge fragments with the same method`() {
        val fragments = listOf(
                RamlFragment("cart-get", "/carts/{id}",
                        Method(
                                method = "get",
                                description = "description",
                                requestBodies = listOf(
                                        Body("application/json", Include("cart-get-request.json"), Include("cart-get-request-schema.json"))),
                                responses = listOf(Response(200,
                                        listOf(Body("application/json", Include("cart-get-response.json"), Include("cart-get-response-schema.json")))))
                        )
                ),
                RamlFragment("cart-get-additional", "/carts/{id}",
                        Method(
                                method = "get",
                                description = "description",
                                requestBodies = listOf(
                                        Body("application/json", Include("cart-get-additional-request.json"), Include("cart-get-additional-schema.json"))),
                                responses = listOf(Response(200,
                                        listOf(Body("application/json", Include("cart-get-additional-response.json"), Include("cart-get-additional-response-schema.json")))))
                        )
                )
        )
        val resource = RamlResource.fromFragments(fragments, NoOpJsonSchemaMerger)

        with(resource) {
            path `should equal` "/carts/{id}"
            methods.size `should equal` 1
            methods.first().requestBodies.size `should equal` 1
            with(methods.first().requestBodies.first()) {
                contentType `should equal` "application/json"
                example.`should be null`()
                examples `should equal` listOf(Include("cart-get-request.json"), Include("cart-get-additional-request.json"))
                schema `should equal` Include("cart-get-request-schema.json")
            }

            methods.first().responses.size `should equal` 1
            with(methods.first().responses.first()) {
                bodies.size `should be` 1
                bodies.first().contentType `should equal` "application/json"
                bodies.first().example.`should be null`()
                bodies.first().examples `should equal` listOf(Include("cart-get-response.json"), Include("cart-get-additional-response.json"))
                bodies.first().schema `should equal` Include("cart-get-response-schema.json")
            }
        }
    }

    @Test
    fun `should add requests with different content types`() {
        val fragments = listOf(
                RamlFragment("cart-line-item-update", "/carts/{id}/line-items",
                        Method(
                                method = "put",
                                description = "description",
                                requestBodies = listOf(
                                        Body("application/json", Include("cart-line-item-update-request.json"), Include("cart-line-item-update-schema.json"))),
                                responses = listOf(Response(200,
                                        listOf(Body("application/json", Include("cart-line-item-update-response.json"), Include("cart-line-item-update-response-schema.json")))))
                        )
                ),
                RamlFragment("cart-line-item-assign", "/carts/{id}/line-items",
                        Method(
                                method = "put",
                                description = "description",
                                requestBodies = listOf(
                                        Body("text/uri-list", Include("cart-line-item-assign-request.json"), Include("cart-line-item-assign-schema.json"))),
                                responses = listOf(Response(200,
                                        listOf(Body("text/uri-list", Include("cart-line-item-assign-response.json"), Include("cart-line-item-assign-response-schema.json")))))
                        )
                )
        )

        val resource = RamlResource.fromFragments(fragments, NoOpJsonSchemaMerger)

        with(resource) {
            path `should equal` "/carts/{id}/line-items"
            methods.size `should equal` 1
            with(methods.first()) {
                requestBodies.size `should equal` 2
                requestBodies.map { it.contentType } `should equal` listOf("application/json", "text/uri-list")
                responses.size `should equal` 1
                responses.first().bodies.size `should equal` 2
                responses.first().bodies.map { it.contentType } `should equal` listOf("application/json", "text/uri-list")
            }
        }
    }

    @Test
    fun `should fail on fragments with different path`() {
        val fragments = listOf(
                RamlFragment("cart-line-item-update", "/carts/{id}/line-items",
                        Method(method = "put", description = "description")
                ),
                RamlFragment("cart-get", "/carts/{id}",
                        Method(method = "get", description = "description")
                )
        )

        val fromFragmentsFunctions = { RamlResource.fromFragments(fragments, NoOpJsonSchemaMerger) }
        fromFragmentsFunctions `should throw` IllegalArgumentException::class
    }
}
