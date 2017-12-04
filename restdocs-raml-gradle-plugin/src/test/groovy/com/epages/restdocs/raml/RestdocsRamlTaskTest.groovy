package com.epages.restdocs.raml

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class RestdocsRamlTaskTest extends Specification {

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()

    RestdocsRamlTask restdocsRamlTask

    def "should aggregate raml fragments"() {
        given:
            givenTask("0.8", true)
            givenSnippetFiles()
            givenRequestBodyJsonFile()
        when:
            restdocsRamlTask.aggregateRamlFragments()
        then:
            thenApiRamlFileGenerated("#%RAML 0.8")
            thenGroupFileGenerated()
            thenRequestBodyJsonFileFoundInOutputDirectory()
    }

    def "should aggregate raml fragments with version 1.0"() {
        given:
          givenTask("1.0", false)
          givenSnippetFiles()
          givenRequestBodyJsonFile()
        when:
          restdocsRamlTask.aggregateRamlFragments()
        then:
          thenApiRamlFileGenerated("#%RAML 1.0")
          thenGroupFileGenerated()
          thenRequestBodyJsonFileFoundInOutputDirectory()
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
        if (restdocsRamlTask.ramlVersion.get() == "0.8")
            assert groupFileLines.any {it.contains("schema: !include carts-create-request.json")}
        else
            assert groupFileLines.any {it.contains("type: !include carts-create-request.json")}

        if (restdocsRamlTask.separatePublicApi.get())
            assert new File(testProjectDir.root, "build/ramldoc/carts-public.raml").exists()
    }

    private void thenApiRamlFileGenerated(String expectedVersion) {
        def apiFile = new File(testProjectDir.root, "build/ramldoc/api.raml")
        def lines = apiFile.readLines()
        assert apiFile.exists()
        assert lines.any { it == expectedVersion }
        assert lines.any { it.startsWith("title:") }
        assert lines.any { it.startsWith("baseUri:") }
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

    private def givenTask(String ramlVersion, Boolean separatePublicApi) {
        Project project = ProjectBuilder.builder()
                .withProjectDir(testProjectDir.root)
                .build()
        project.pluginManager.apply 'com.epages.restdocs-raml'
        project.extensions.ramldoc.ramlVersion = ramlVersion
        project.extensions.ramldoc.apiTitle = "mytitle"
        project.extensions.ramldoc.apiBaseUri = "http://localhost/api"
        project.extensions.ramldoc.separatePublicApi = separatePublicApi
        restdocsRamlTask = project.tasks.ramldoc as RestdocsRamlTask
    }
}