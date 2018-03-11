package com.epages.restdocs.raml

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.junit.Test


class RamlFragmentTest: FragmentFixtures {

    @Test
    fun `should recognize private resource`() {
        RamlFragment("/some", parsedPrivateResourceFragmentMap()).privateResource.`should be true`()
    }

    @Test
    fun `should recognize public resource`() {
        RamlFragment("/some", parsedResourceFragmentMap()).privateResource.`should be false`()
    }

    @Test
    fun `should extract first path part`() {
        RamlFragment.fromYamlMap(parsedResourceFragmentMap()).firstPathPart `should be equal to` "/payment-integrations"
    }

    @Test
    fun `should extract first path part for path with one element`() {
        RamlFragment("/some", parsedPrivateResourceFragmentMap()).firstPathPart `should be equal to` "/some"
    }

    @Test
    fun `should extract first path part for empty path`() {
        RamlFragment("/", parsedPrivateResourceFragmentMap()).firstPathPart `should be equal to` "/"
    }
}
