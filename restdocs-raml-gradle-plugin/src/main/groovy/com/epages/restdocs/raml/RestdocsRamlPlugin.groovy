package com.epages.restdocs.raml

import org.gradle.api.Plugin
import org.gradle.api.Project

class RestdocsRamlPlugin implements Plugin<Project> {

    def outputDir = 'build/ramldoc/'

    @Override
    void apply(Project project) {
        project.task("ramldoc", type: RestdocsRamlTask, dependsOn: 'check') {
            description = 'aggregate raml fragments into a service raml'
            outputDirectory = "build/ramldoc"
            snippetsDirectory = "build/generated-snippets"
        }
    }
}
