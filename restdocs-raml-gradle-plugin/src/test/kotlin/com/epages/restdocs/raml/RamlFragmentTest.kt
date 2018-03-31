package com.epages.restdocs.raml

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldNotBeNullOrEmpty
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class RamlFragmentTest: FragmentFixtures {

    @Rule
    @JvmField val testProjectDir = TemporaryFolder()

    lateinit var file: File
    lateinit var fragment: RamlFragment
    var expectedId = "some-get"


    @Test
    fun `should parse fragment from file`() {
        givenFile()

        whenFragmentReadFromFile()

        with(fragment) {
            id `should be equal to` expectedId
            path `should be equal to` "/carts/{cartId}"
            method.method `should be equal to` "get"
            method.requestBodies.shouldBeEmpty()
            method.responses.shouldBeEmpty()
            method.securedBy `should equal` listOf("pymt:u")
            method.description.shouldNotBeNullOrEmpty()
        }
    }

    @Test
    fun `should parse minimal fragment`() {
        whenFragmentReadFromMap(::rawMinimalFragment)

        with(fragment) {
            id `should be equal to` expectedId
            path `should be equal to` "/payment-integrations/{paymentIntegrationId}"
            method.method `should be equal to` "get"
            method.description.shouldNotBeNullOrEmpty()
            method.requestBodies.shouldBeEmpty()
            method.responses.shouldBeEmpty()
        }
    }

    @Test
    fun `should parse fragment with example without schema`() {
        whenFragmentReadFromMap(this::rawFragmentWithoutSchema)

        with(fragment) {
            method.requestBodies.shouldBeEmpty()
            method.responses.size `should equal` 1
            with(method.responses.first()) {
                bodies.first().example.`should not be null`()
                bodies.first().schema.`should be null`()
            }
        }
    }

    @Test
    fun `should parse full fragment`() {
        whenFragmentReadFromMap(::rawFullFragment)

        with(fragment) {
            id `should equal` expectedId
            path `should equal` "/tags/{id}"
            uriParameters.size `should equal` 1
            uriParameters.first().name `should equal` "id"
            uriParameters.first().description `should equal` "The id"
            uriParameters.first().type `should equal` "string"
            with (method) {
                method `should equal` "put"
                description.shouldNotBeNullOrEmpty()

                queryParameters.size `should be` 2
                queryParameters.map { it.name } `should equal` listOf("some", "other")
                queryParameters.map { it.description } `should equal` listOf("some", "other")
                queryParameters.map { it.type } `should equal` listOf("integer", "string")

                traits.size `should equal` 1
                traits.first() `should equal` "private"

                securedBy.size `should equal` 2
                securedBy `should equal` listOf("scope-one", "scope-two")

                requestBodies.size `should equal` 1
                with(requestBodies.first()) {
                    contentType `should equal` "application/hal+json"
                    example.`should not be null`()
                    schema.`should not be null`()
                }

                responses.size `should be` 1
                with(responses.first()) {
                    status `should equal` 200
                    bodies.first().example.`should not be null`()
                    bodies.first().schema.`should not be null`()
                }
            }
        }
    }

    private fun whenFragmentReadFromMap(provider: () -> String) {
        fragment = RamlFragment.fromYamlMap(expectedId, parsedFragmentMap(provider) )
    }

    private fun whenFragmentReadFromFile() {
        fragment = RamlFragment.fromFile(file)
    }

    private fun givenFile() {
        file = testProjectDir.newFolder("build", "generated-snippets", expectedId)
                .let { File(it, "raml-resource.raml") }
                .also {
                    it.writeText("""/carts/{cartId}:
                        |  get:
                        |    description: "TODO - figure out how to set"
                        |    securedBy: ["pymt:u"]
    """.trimMargin())
                }
    }
}
