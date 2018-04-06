package com.epages.restdocs.raml

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class RamlFragmentTest: FragmentFixtures {

    @Rule
    @JvmField val testProjectDir = TemporaryFolder()

    @Test
    fun `should recognize private resource`() {
        RamlFragment("some", "/some", parsedPrivateResourceFragmentMap()).privateResource.`should be true`()
    }

    @Test
    fun `should recognize public resource`() {
        RamlFragment("some", "/some", parsedResourceFragmentMap()).privateResource.`should be false`()
    }

    @Test
    fun `should extract first path part`() {
        RamlFragment.fromYamlMap("some", parsedResourceFragmentMap()).firstPathPart `should be equal to` "/payment-integrations"
    }

    @Test
    fun `should extract id from file path`() {
        val id = "some-get"
        val file = testProjectDir.newFolder("build", "generated-snippets", id)
                .let { File(it, "raml-resource.raml") }
                .also { it.writeText("""/carts/{cartId}:
  get:
    description: "TODO - figure out how to set"
    securedBy: ["pymt:u"]
""".trimIndent()) }
        with(RamlFragment.fromFile(file)) {
            this.id `should be equal to` id
            firstPathPart `should be equal to` "/carts"
        }
    }

    @Test
    fun `should extract first path part for path with one element`() {
        RamlFragment("some", "/some", parsedPrivateResourceFragmentMap()).firstPathPart `should be equal to` "/some"
    }

    @Test
    fun `should extract first path part for empty path`() {
        RamlFragment("some", "/", parsedPrivateResourceFragmentMap()).firstPathPart `should be equal to` "/"
    }
}
