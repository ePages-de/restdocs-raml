package com.epages.restdocs.raml

import com.epages.restdocs.raml.FragmentProcessor.aggregateFileMap
import com.epages.restdocs.raml.FragmentProcessor.groupFragments
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption


open class RestdocsRamlTask: DefaultTask() {

    @Input
    lateinit var ramlVersion: String

    @Input
    @Optional
    var apiBaseUri: String? = null

    @Input
    @Optional
    lateinit var apiTitle: String

    @Input
    var separatePublicApi: Boolean = false

    @Input
    lateinit var outputDirectory: String

    @Input
    lateinit var snippetsDirectory: String

    @Input
    lateinit var outputFileNamePrefix: String

    private val outputDirectoryFile
        get() = project.file(outputDirectory)

    private val snippetsDirectoryFile
        get() = project.file(snippetsDirectory)


    @TaskAction
    fun aggregateRamlFragments() {
        outputDirectoryFile.mkdirs()

        copyBodyJsonFilesToOutput()

        val ramlFragments = snippetsDirectoryFile.walkTopDown()
                .filter { it.name is String && it.name.startsWith("raml-resource") }
                .map { RamlParser.parseFragment(it) }
                .map { RamlFragment.fromYamlMap(it) }
                .map { if (ramlVersion == "1.0") it.replaceSchemaWithType() else it }
                .toList()

        writeFiles(ramlFragments, ".raml")

        if (separatePublicApi)
            writeFiles(ramlFragments.filterNot { it.privateResource }, "-public.raml")
    }

    fun writeFiles(ramlFragments: List<RamlFragment>, fileNameSuffix: String) {
        val fragmentGroups = groupFragments(ramlFragments)
        RamlWriter.writeFile(
                targetFile = project.file("${outputDirectory}/${outputFileNamePrefix}$fileNameSuffix"),
                contentMap = aggregateFileMap(
                        apiTitle,
                        apiBaseUri,
                        ramlVersion,
                        outputFileNamePrefix,
                        fragmentGroups),
                headerLine = if (ramlVersion == "0.8") "#%RAML 0.8" else "#%RAML 1.0"
        )

        fragmentGroups.forEach {
            RamlWriter.writeFile(
                    targetFile = project.file("${outputDirectory}/${FragmentProcessor.groupFileName(it.commonPathPrefix, fileNameSuffix, outputFileNamePrefix)}"),
                    contentMap = FragmentProcessor.groupFileMap(it)
            )}
    }

    private fun copyBodyJsonFilesToOutput() {
        snippetsDirectoryFile.walkTopDown().forEach {
                    if (it.name.endsWith("-request.json") || it.name.endsWith("-response.json"))
                        Files.copy(it.toPath(), outputDirectoryFile.toPath().resolve(it.name), StandardCopyOption.REPLACE_EXISTING)
                }
    }
}
