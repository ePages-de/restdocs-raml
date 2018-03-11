package com.epages.restdocs.raml

import org.gradle.api.Project

open class RestdocsRamlPluginExtension(project: Project) {
    var ramlVersion = "1.0"
    var apiBaseUri: String? = null
    var apiTitle = "API documentation"

    var separatePublicApi: Boolean = false
    var outputDirectory = "build/ramldoc"
    var snippetsDirectory = "build/generated-snippets"

    var outputFileNamePrefix = "api"
}
