package com.epages.restdocs.raml

import com.epages.restdocs.raml.RamlFragment.Companion.fromYamlMap
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test


class FragmentProcessorTest {

    lateinit var fragments: List<RamlFragment>

    lateinit var groupedFragments: List<FragmentGroup>

    @Test
    fun `should group raml fragments`() {
        `given fragments`()

        `when fragments grouped with aggregated examples`()

        groupedFragments.map { it.commonPathPrefix } `should equal` listOf("/carts", "/products")
        val cartsFragments = groupedFragments.map { it.commonPathPrefix to it.ramlFragments }.toMap().get("/carts")?: emptyList()
        cartsFragments.map { it.path } `should equal` listOf("", "/{id}")
        cartsFragments.single { it.path == "/{id}" } `should equal` RamlFragment("cart-get", "/{id}", mapOf("get" to "some", "post" to "some"))
        cartsFragments.single { it.path == "" } `should equal` RamlFragment("carts-list", "", mapOf("get" to "some"))
    }

    @Test
    fun `should group raml fragments with same request method`() {
        `given fragments with examples`()

        `when fragments grouped with aggregated examples`()

        groupedFragments.size `should be equal to` 1
        with(groupedFragments[0].ramlFragments) {
            size `should equal` 1
            with(this[0]) {
                path `should equal` "/{shippingZoneId}/shipping-methods"
                requestMethod `should equal` "post"
                requestBody.shouldNotBeNull()
                requestBody!!.findValueByKeyRecursive("examples") `should equal` mapOf(
                        "shipping-zones-shipping-methods-create"
                                to Include("shipping-zones-shipping-methods-create-request.json"),
                        "shipping-zones-shipping-methods-create-with-weight-based-price"
                                to Include("shipping-zones-shipping-methods-create-with-weight-based-price-request.json"))
                responses.shouldNotBeNull()
                responses!!.findValueByKeyRecursive("examples") `should equal` mapOf(
                        "shipping-zones-shipping-methods-create"
                                to Include("shipping-zones-shipping-methods-create-response.json"),
                        "shipping-zones-shipping-methods-create-with-weight-based-price"
                                to Include("shipping-zones-shipping-methods-create-with-weight-based-price-response.json"))
            }
        }
    }

    @Test
    fun `should not aggregate multiple examples`() {
        `given fragments with examples`()

        `when fragments grouped without aggregated examples`()

        groupedFragments.size `should be equal to` 1
        with(groupedFragments[0].ramlFragments) {
            size `should equal` 1
            with(this[0]) {
                path `should equal` "/{shippingZoneId}/shipping-methods"
                requestMethod `should equal` "post"
                requestBody.shouldNotBeNull()
                requestBody!!.findValueByKeyRecursive("examples").shouldBeNull()
                requestBody!!.findValueByKeyRecursive("example").shouldNotBeNull()
            }
        }
    }

    private fun `given fragments with examples`() {
        fragments = listOf(
                fromYamlMap("shipping-zones-shipping-methods-create",
                        RamlParser.parseFragment("""/shipping-zones/{shippingZoneId}/shipping-methods:
                          post:
                            description:
                            body:
                              application/json:
                                schema: !include shipping-zones-shipping-methods-create-schema-request.json
                                example: !include shipping-zones-shipping-methods-create-request.json
                            responses:
                              201:
                                body:
                                  application/hal+json:
                                    example: !include shipping-zones-shipping-methods-create-response.json
                                """.trimIndent())),
                fromYamlMap("shipping-zones-shipping-methods-create-with-weight-based-price",
                        RamlParser.parseFragment("""/shipping-zones/{shippingZoneId}/shipping-methods:
                              post:
                                description:
                                body:
                                  application/json:
                                    schema: !include shipping-zones-shipping-methods-create-with-weight-based-price-schema-request.json
                                    example: !include shipping-zones-shipping-methods-create-with-weight-based-price-request.json
                                responses:
                                  201:
                                    body:
                                      application/hal+json:
                                        example: !include shipping-zones-shipping-methods-create-with-weight-based-price-response.json
                                """.trimIndent()))
        )
    }


    @Test
    fun `should create aggregate file map`() {
        `given grouped fragments`()

        val aggregateFileMap = FragmentProcessor(false).aggregateFileMap("title", "http://example.com", "api", groupedFragments)

        aggregateFileMap `should equal` mapOf(
                "title" to "title",
                "baseUri" to "http://example.com",
                "/carts" to Include("carts.raml"),
                "/products" to Include("products.raml")
        )
    }

    @Test
    fun `should create group file map`() {
        `given grouped fragments`()

        val cartGroupFileMap = FragmentProcessor(false).groupFileMap(groupedFragments.single { it.commonPathPrefix == "/carts" })
        cartGroupFileMap `should equal` mapOf(
                "get" to "some",
                "/{id}" to mapOf("get" to "some", "post" to "some")
        )
    }

    fun `given grouped fragments`() {
        `given fragments`()
        `when fragments grouped with aggregated examples`()
    }

    private fun `given fragments`() {
        fragments = listOf(
                fromYamlMap("carts-list", mapOf("/carts" to mapOf("get" to "some"))),
                fromYamlMap("cart-get", mapOf("/carts/{id}" to mapOf("get" to "some"))),
                fromYamlMap("cart-post", mapOf("/carts/{id}" to mapOf("post" to "some"))),
                fromYamlMap("product-get", mapOf("/products/{id}" to mapOf("get" to "some")))
        )
    }

    private fun `when fragments grouped with aggregated examples`() {
        groupedFragments = FragmentProcessor(true).groupFragments(fragments)
    }

    private fun `when fragments grouped without aggregated examples`() {
        groupedFragments = FragmentProcessor(false).groupFragments(fragments)
    }
}
