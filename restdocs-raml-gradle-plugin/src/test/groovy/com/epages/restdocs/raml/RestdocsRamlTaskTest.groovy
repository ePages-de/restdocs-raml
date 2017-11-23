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
            givenTask()
            givenSnippetFiles()
            givenRequestBodyJsonFile()
        when:
            restdocsRamlTask.aggregateRamlFragments()
        then:
            thenApiRamlFileGenerated()
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
    }

    private void thenApiRamlFileGenerated() {
        def apiFile = new File(testProjectDir.root, "build/ramldoc/api.raml")
        def lines = apiFile.readLines()
        assert apiFile.exists()
        assert lines.any { it =="#%RAML 0.8" }
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

    private def givenTask() {
        Project project = ProjectBuilder.builder()
                .withProjectDir(testProjectDir.root)
                .build()
        project.pluginManager.apply 'com.epages.restdocs-raml'
        project.extensions.ramldoc.ramlVersion = "0.8"
        project.extensions.ramldoc.apiTitle = "mytitle"
        project.extensions.ramldoc.apiBaseUri = "http://localhost/api"
        restdocsRamlTask = project.tasks.ramldoc as RestdocsRamlTask
    }
}