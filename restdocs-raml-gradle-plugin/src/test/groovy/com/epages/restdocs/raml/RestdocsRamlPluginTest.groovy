package com.epages.restdocs.raml

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification


class RestdocsRamlPluginTest extends Specification {

    def "should configure plugin with defaults"() {
        given:
            Project project = ProjectBuilder.builder().build()
        when:
            project.pluginManager.apply 'com.epages.restdocs-raml'
        then:
            project.tasks.ramldoc instanceof RestdocsRamlTask
            def ramldocTask = project.tasks.ramldoc as RestdocsRamlTask
            ramldocTask.snippetsDirectory == project.file("build/generated-snippets")
            ramldocTask.outputDirectory == project.file("build/ramldoc")
            ramldocTask.ramlVersion.get() == "1.0"
            !ramldocTask.apiTitle.isPresent()
            !ramldocTask.apiBaseUri.isPresent()

    }

    def "should configure plugin with extension"() {
        given:
            Project project = ProjectBuilder.builder()build()
        when:
            project.pluginManager.apply 'com.epages.restdocs-raml'
            project.extensions.ramldoc.ramlVersion = "0.8"
            project.extensions.ramldoc.apiTitle = "mytitle"
            project.extensions.ramldoc.apiBaseUri = "http://localhost/api"
            project.extensions.ramldoc.outputDirectory = "output"
            project.extensions.ramldoc.snippetsDirectory = "snippets"
        then:
            project.tasks.ramldoc instanceof RestdocsRamlTask
            def ramldocTask = project.tasks.ramldoc as RestdocsRamlTask
            ramldocTask.ramlVersion.get() == "0.8"
            ramldocTask.apiTitle.get() == "mytitle"
            ramldocTask.apiBaseUri.get() == "http://localhost/api"
            ramldocTask.snippetsDirectory == project.file("snippets")
            ramldocTask.outputDirectory == project.file("output")
    }
}
