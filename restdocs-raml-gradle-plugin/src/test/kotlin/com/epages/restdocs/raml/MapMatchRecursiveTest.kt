package com.epages.restdocs.raml

import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.junit.Test

@Suppress("UNCHECKED_CAST")
class MapMatchRecursiveTest: FragmentFixtures {

    @Test
    fun `should match recursive`() {
        parsedPrivateResourceFragmentMap().anyMatchRecursive {
                            key is String && key == "is" && value is List<*> && (value as List<String>).contains("private")
                        } .`should be true`()
    }

    @Test
    fun `should not match recursive`() {
        parsedPrivateResourceFragmentMap().anyMatchRecursive {
            key is String && key == "i dont exist" && value is List<*> && (value as List<String>).contains("private")
        } .`should be false`()
    }
}
