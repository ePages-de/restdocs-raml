package com.epages.restdocs.raml

import org.gradle.api.Plugin
import org.gradle.api.Project

class RestdocsRamlPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('ramldoc', RestdocsRamlPluginExtension, project)

        project.afterEvaluate {
            project.task("ramldoc", type: RestdocsRamlTask, dependsOn: 'check') {
                description = 'Aggregate raml fragments into a service raml'

                ramlVersion = project.ramldoc.ramlVersion
                apiBaseUri = project.ramldoc.apiBaseUri
                apiTitle = project.ramldoc.apiTitle

                separatePublicApi = project.ramldoc.separatePublicApi

                outputDirectory = project.ramldoc.outputDirectory
                snippetsDirectory = project.ramldoc.snippetsDirectory

                outputFileNamePrefix = project.ramldoc.outputFileNamePrefix
            }
        }
    }
}
