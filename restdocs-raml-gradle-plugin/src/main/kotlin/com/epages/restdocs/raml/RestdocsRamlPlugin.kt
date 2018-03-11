package com.epages.restdocs.raml

import org.gradle.api.Plugin
import org.gradle.api.Project


class RestdocsRamlPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            extensions.create("ramldoc", RestdocsRamlPluginExtension::class.java, project)
            afterEvaluate {
                val ramldoc = extensions.findByName("ramldoc") as RestdocsRamlPluginExtension
                tasks.create("ramldoc", RestdocsRamlTask::class.java).apply {
                    dependsOn("check")
                    description = "Aggregate raml fragments into a service raml"

                    ramlVersion = ramldoc.ramlVersion
                    apiBaseUri = ramldoc.apiBaseUri
                    apiTitle = ramldoc.apiTitle

                    separatePublicApi = ramldoc.separatePublicApi

                    outputDirectory = ramldoc.outputDirectory
                    snippetsDirectory = ramldoc.snippetsDirectory

                    outputFileNamePrefix = ramldoc.outputFileNamePrefix
                }
            }
        }
    }

}
