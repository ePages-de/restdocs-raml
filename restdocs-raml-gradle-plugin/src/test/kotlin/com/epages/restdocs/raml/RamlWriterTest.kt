package com.epages.restdocs.raml

import org.amshove.kluent.shouldContain
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder


class RamlWriterTest {

    @Rule @JvmField val tempFolder = TemporaryFolder()

    @Test
    fun `should_write_map`() {
        tempFolder.newFile().let { file ->
            RamlWriter.writeFile(file, mapOf("title" to "title", "baseUri" to "http://localhost"), "#%RAML 0.8")
            file.readLines().let {
                it.shouldContain("#%RAML 0.8")
                it.shouldContain("title: title")
                it.shouldContain("baseUri: http://localhost")
            }
        }

    }
}
