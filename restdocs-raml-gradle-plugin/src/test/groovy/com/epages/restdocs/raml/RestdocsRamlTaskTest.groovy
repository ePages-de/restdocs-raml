package com.epages.restdocs.raml

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class RestdocsRamlTaskTest extends Specification {

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    List<File> pluginClasspath
    BuildResult result

    String apiTitle
    String baseUri
    String ramlVersion
    boolean separatePublicApi = false

    def setup() {
        apiTitle = "Notes API"
        baseUri = "http://localhost:8080/"
        ramlVersion = 1.0

        buildFile = testProjectDir.newFile('build.gradle')

        testProjectDir.newFolder("build", "generated-snippets")

        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    }

    def "should aggregate raml fragments"() {
        given:
            separatePublicApi = true
            ramlVersion = "0.8"
            givenBuildFileWithRamldocClosure()
            givenSnippetFiles()
            givenRequestBodyJsonFile()
        when:
            whenPluginExecuted()
        then:
            result.task(":ramldoc").outcome == SUCCESS
            thenApiRamlFileGenerated()
            thenGroupFileGenerated()
            thenRequestBodyJsonFileFoundInOutputDirectory()
    }

    def "should aggregate raml fragments with empty config"() {
        given:
            apiTitle = null
            baseUri = null
            givenBuildFileWithoutConfig()
            givenSnippetFiles()
            givenRequestBodyJsonFile()
        when:
            whenPluginExecuted()
        then:
            result.task(":ramldoc").outcome == SUCCESS
            thenApiRamlFileGenerated()
            thenGroupFileGenerated()
            thenRequestBodyJsonFileFoundInOutputDirectory()
    }

    def "should aggregate raml fragments with version 1.0"() {
        given:
          givenBuildFileWithRamldocClosure()
          givenSnippetFiles()
          givenRequestBodyJsonFile()
        when:
          whenPluginExecuted()
        then:
            result.task(":ramldoc").outcome == SUCCESS
            thenApiRamlFileGenerated()
            thenGroupFileGenerated()
            thenRequestBodyJsonFileFoundInOutputDirectory()
    }

    private void whenPluginExecuted() {
        result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ramldoc')
                .withPluginClasspath(pluginClasspath)
                .build()
    }
    private void thenRequestBodyJsonFileFoundInOutputDirectory() {
        assert new File(testProjectDir.root,"build/ramldoc/carts-create-request.json").exists()
    }

    private void thenGroupFileGenerated() {
        def groupFile = new File(testProjectDir.root, "build/ramldoc/carts.raml")
        def groupFileLines = groupFile.readLines()
        assert groupFileLines.any {it.startsWith("  get:")}
        assert groupFileLines.any {it.startsWith("  post:")}
        assert groupFileLines.any {it.startsWith("  /{cartId}:")}
        assert groupFileLines.any {it.startsWith("    get:")}
        assert groupFileLines.any {it.startsWith("    delete:")}
        if (ramlVersion == "0.8")
            assert groupFileLines.any {it.contains("schema: !include carts-create-request.json")}
        else
            assert groupFileLines.any {it.contains("type: !include carts-create-request.json")}

        if (separatePublicApi)
            assert new File(testProjectDir.root, "build/ramldoc/carts-public.raml").exists()
    }

    private void thenApiRamlFileGenerated() {
        def apiFile = new File(testProjectDir.root, "build/ramldoc/api.raml")
        def lines = apiFile.readLines()
        assert apiFile.exists()
        assert lines.any { it == "#%RAML $ramlVersion" }
        assert lines.any { apiTitle ?: it.startsWith("title: API documentation") }
        if (baseUri != null) {
            assert lines.any { it.startsWith("baseUri:") }
        }
        assert lines.any() { it == "/carts: !include carts.raml" }
    }

    private def givenRequestBodyJsonFile() {
        testProjectDir.newFile("build/generated-snippets/carts-create/carts-create-request.json") << """{}"""
    }

    private def givenSnippetFiles() {
        new File(testProjectDir.newFolder("build", "generated-snippets", "carts-create"), "raml-resource.raml") << """/carts:
  post:
    description: "TODO - figure out how to set"
    securedBy: ["pymt:u"]
    body:
      application/hal+json:
        schema: !include carts-create-request.json
        example: !include carts-create-request.json
"""
        new File(testProjectDir.newFolder("build", "generated-snippets", "carts-get"), "raml-resource.raml") << """/carts/{cartId}:
  get:
    description: "TODO - figure out how to set"
    securedBy: ["pymt:u"]
"""
        new File(testProjectDir.newFolder("build", "generated-snippets", "carts-list"), "raml-resource.raml") << """/carts:
  get:
    description: "TODO - figure out how to set"
    securedBy: ["pymt:u"]
"""
        new File(testProjectDir.newFolder("build", "generated-snippets", "carts-delete"), "raml-resource.raml") << """/carts/{cartId}:
  delete:
    description: "TODO - figure out how to set"
    securedBy: ["pymt:u"]
"""
    }

    private def givenBuildFileWithoutConfig() {
        buildFile << baseBuildFile
    }

    private def givenBuildFileWithRamldocClosure() {
        buildFile << baseBuildFile + """
ramldoc {
    apiTitle = '$apiTitle'
    apiBaseUri = '$baseUri'
    ramlVersion = "$ramlVersion"
    separatePublicApi = $separatePublicApi
}
"""
    }

    def baseBuildFile = """
buildscript {
    repositories {
        mavenCentral()
        maven { url "https://plugins.gradle.org/m2/" }
    }
}

plugins {
    id 'java'
    id 'com.epages.restdocs-raml'
}
"""
}