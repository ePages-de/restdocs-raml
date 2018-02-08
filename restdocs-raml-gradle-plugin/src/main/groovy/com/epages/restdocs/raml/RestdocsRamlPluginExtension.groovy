package com.epages.restdocs.raml

import org.gradle.api.Project

class RestdocsRamlPluginExtension {
    String ramlVersion = "1.0"
    String apiBaseUri
    String apiTitle = "API documentation"

    Boolean separatePublicApi = false
    String outputDirectory = "build/ramldoc"
    String snippetsDirectory = "build/generated-snippets"

    String outputFileNamePrefix = "api"

    RestdocsRamlPluginExtension(Project project) {
    }
}
