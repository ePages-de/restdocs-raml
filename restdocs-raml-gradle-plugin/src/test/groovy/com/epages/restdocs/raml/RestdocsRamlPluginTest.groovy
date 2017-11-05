package com.epages.restdocs.raml

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification


class RestdocsRamlPluginTest extends Specification {

    def "should configure plugin"() {
        given:
            Project project = ProjectBuilder.builder().build()
        when:
            project.pluginManager.apply 'com.epages.restdocs-raml'
        then:
            project.tasks.ramldoc instanceof RestdocsRamlTask
            def ramldocTask = project.tasks.ramldoc as RestdocsRamlTask
            ramldocTask.snippetsDirectory == project.file("build/generated-snippets")
            ramldocTask.outputDirectory == project.file("build/ramldoc")
    }
}
