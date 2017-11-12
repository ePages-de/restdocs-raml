package com.epages.restdocs.raml

import org.gradle.api.Project
import org.gradle.api.provider.Property

class RestdocsRamlPluginExtension {

    Property<String> ramlVersion
    Property<String> apiBaseUri
    Property<String> apiTitle

    Property<String> outputDirectory
    Property<String> snippetsDirectory

    RestdocsRamlPluginExtension(Project project) {
        ramlVersion = project.objects.property(String)
        ramlVersion.set("1.0")

        apiBaseUri = project.objects.property(String)

        apiTitle = project.objects.property(String)

        outputDirectory = project.objects.property(String)
        outputDirectory.set("build/ramldoc")

        snippetsDirectory = project.objects.property(String)
        snippetsDirectory.set("build/generated-snippets")
    }
}
