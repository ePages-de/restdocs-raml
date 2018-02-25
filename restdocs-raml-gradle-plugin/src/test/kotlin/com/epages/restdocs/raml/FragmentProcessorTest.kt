package com.epages.restdocs.raml

import com.epages.restdocs.raml.RamlFragment.Companion.fromYamlMap
import org.amshove.kluent.`should equal`
import org.junit.Test


class FragmentProcessorTest {

    lateinit var fragments: List<RamlFragment>

    lateinit var groupedFragments: List<FragmentGroup>

    @Test
    fun `should group raml fragments`() {
        `given fragments`()

        `when fragments grouped`()

        groupedFragments.map { it.commonPathPrefix } `should equal` listOf("/carts", "/products")
        val cartsFragments = groupedFragments.map { it.commonPathPrefix to it.ramlFragments }.toMap().get("/carts")?: emptyList()
        cartsFragments.map { it.path } `should equal` listOf("", "/{id}")
        cartsFragments.single { it.path == "/{id}" } `should equal` RamlFragment("/{id}", mapOf("get" to "some", "post" to "some"))
        cartsFragments.single { it.path == "" } `should equal` RamlFragment("", mapOf("get" to "some"))
    }

    @Test
    fun `should create aggregate file map`() {
        `given grouped fragments`()

        val aggregateFileMap = FragmentProcessor.aggregateFileMap("title", "http://example.com", "0.8", "api", groupedFragments)

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

        val cartGroupFileMap = FragmentProcessor.groupFileMap(groupedFragments.single { it.commonPathPrefix == "/carts" })
        cartGroupFileMap `should equal` mapOf(
                "get" to "some",
                "/{id}" to mapOf("get" to "some", "post" to "some")
        )
    }

    fun `given grouped fragments`() {
        `given fragments`()
        `when fragments grouped`()
    }

    private fun `given fragments`() {
        fragments = listOf(
                fromYamlMap(mapOf("/carts" to mapOf("get" to "some"))),
                fromYamlMap(mapOf("/carts/{id}" to mapOf("get" to "some"))),
                fromYamlMap(mapOf("/carts/{id}" to mapOf("post" to "some"))),
                fromYamlMap(mapOf("/products/{id}" to mapOf("get" to "some")))
        )
    }

    private fun `when fragments grouped`() {
        groupedFragments = FragmentProcessor.groupFragments(fragments)
    }
}
