package com.epages.restdocs.raml

import com.epages.restdocs.raml.RamlFragment.Companion.fromYamlMap
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder


class FragmentProcessorTest {

    @Rule @JvmField val tempFolder = TemporaryFolder().also { it.create() }

    lateinit var fragments: List<RamlFragment>

    lateinit var groupedFragments: List<FragmentGroup>

    lateinit var jsonSchemaMerger: JsonSchemaMerger

    @Test
    fun `should group raml fragments`() {
        `given schema merger with current directory`()
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
        `given schema merger with current directory`()
        `given fragments with request body and response examples`()

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
                requestBody!!.findValueByKeyRecursive("type") `should equal` Include("shipping-zones-shipping-methods-create-schema-request-merged.json")

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
        `given schema merger with current directory`()
        `given fragments with request body and response examples`()

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

    @Test
    fun `should group raml fragments with either request of response examples`() {
        `given schema merger with current directory`()
        `given fragments with either response body or request body examples`()

        `when fragments grouped with aggregated examples`()

        groupedFragments.size `should be equal to` 1
        with(groupedFragments[0].ramlFragments) {
            with(groupedFragments[0].ramlFragments) {
                size `should equal` 1
                with(this[0]) {
                    path `should equal` "/line-items/{lineItemId}"
                }
            }

        }
    }

    @Test
    fun `should create aggregate file map`() {
        `given schema merger with current directory`()
        `given grouped fragments`()

        val aggregateFileMap = FragmentProcessor(false, jsonSchemaMerger).aggregateFileMap("title", "http://example.com", "api", groupedFragments)

        aggregateFileMap `should equal` mapOf(
                "title" to "title",
                "baseUri" to "http://example.com",
                "/carts" to Include("carts.raml"),
                "/products" to Include("products.raml")
        )
    }

    @Test
    fun `should create group file map`() {
        `given schema merger with current directory`()
        `given grouped fragments`()

        val cartGroupFileMap = FragmentProcessor(false, jsonSchemaMerger).groupFileMap(groupedFragments.single { it.commonPathPrefix == "/carts" })
        cartGroupFileMap `should equal` mapOf(
                "get" to "some",
                "/{id}" to mapOf("get" to "some", "post" to "some")
        )
    }

    private fun `given schema merger with current directory`() {
        jsonSchemaMerger = JsonSchemaMerger(tempFolder.root)
    }

    private fun `given fragments with request body and response examples`() {
        tempFolder.newFile("shipping-zones-shipping-methods-create-schema-request.json").writeText("""{
  "type" : "object",
  "properties" : {
    "taxClass" : {
      "description" : "The tax class. Can be `REGULAR`, `REDUCED`, or `EXEMPT`.",
      "type" : "string"
    },
    "freeShippingValue" : {
      "description" : "Once a customer reaches this amount (excluding payment fee) this shipping method is free of charge.",
      "type" : "object"
    },
    "name" : {
      "description" : "The name of the shipping method.",
      "type" : "string"
    },
    "description" : {
      "description" : "description",
      "type" : "string"
    },
    "fixedPrice" : {
      "description" : "The fixed price for the shipping method irrespective of weight, dimensions, etc.",
      "type" : "object"
    }
  }
}""".trimIndent())

            tempFolder.newFile("shipping-zones-shipping-methods-create-with-weight-based-price-schema-request.json").writeText("""{
  "type" : "object",
  "properties" : {
    "taxClass" : {
      "description" : "The tax class. Can be `REGULAR`, `REDUCED`, or `EXEMPT`.",
      "type" : "string"
    },
    "freeShippingValue" : {
      "description" : "Once a customer reaches this amount (excluding payment fee) this shipping method is free of charge.",
      "type" : "object"
    },
    "name" : {
      "description" : "The name of the shipping method.",
      "type" : "string"
    },
    "description" : {
      "description" : "description",
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
  }
}""".trimIndent())

            fragments = listOf(
                    fromYamlMap("shipping-zones-shipping-methods-create",
                            RamlParser.parseFragment("""/shipping-zones/{shippingZoneId}/shipping-methods:
                          post:
                            description:
                            body:
                              application/json:
                                type: !include shipping-zones-shipping-methods-create-schema-request.json
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
                                    type: !include shipping-zones-shipping-methods-create-with-weight-based-price-schema-request.json
                                    example: !include shipping-zones-shipping-methods-create-with-weight-based-price-request.json
                                responses:
                                  201:
                                    body:
                                      application/hal+json:
                                        example: !include shipping-zones-shipping-methods-create-with-weight-based-price-response.json
                                """.trimIndent()))
            )
        }

        private fun `given fragments with either response body or request body examples`() {
            fragments = listOf(
                    fromYamlMap("cart-delete-line-item",
                            RamlParser.parseFragment("""/{cartId}/line-items/{lineItemId}:
                          delete:
                            description: null
                            responses:
                              200:
                                body:
                                  application/hal+json:
                                    example: !include 'cart-delete-line-item-response.json'
                                """.trimIndent())),
                    fromYamlMap("cart-line-item-update-response",
                            RamlParser.parseFragment("""/{cartId}/line-items/{lineItemId}:
                              put:
                                  description: null
                                  body:
                                    text/uri-list:
                                      examples:
                                        payment-method-sort: !include 'cart-line-item-update-response.json'
                                """.trimIndent()))
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
            groupedFragments = FragmentProcessor(true, jsonSchemaMerger).groupFragments(fragments)
        }

        private fun `when fragments grouped without aggregated examples`() {
            groupedFragments = FragmentProcessor(false, jsonSchemaMerger).groupFragments(fragments)
        }
}
