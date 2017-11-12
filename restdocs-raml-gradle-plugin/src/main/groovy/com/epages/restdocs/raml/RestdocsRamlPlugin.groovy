package com.epages.restdocs.raml

import org.gradle.api.Plugin
import org.gradle.api.Project

class RestdocsRamlPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = project.extensions.create('ramldoc', RestdocsRamlPluginExtension, project)


        project.task("ramldoc", type: RestdocsRamlTask, dependsOn: 'check') {
            description = 'aggregate raml fragments into a service raml'

            ramlVersion = extension.ramlVersion
            apiBaseUri = extension.apiBaseUri
            apiTitle = extension.apiTitle

            outputDirectory = extension.outputDirectory
            snippetsDirectory = extension.snippetsDirectory
        }
    }
}
