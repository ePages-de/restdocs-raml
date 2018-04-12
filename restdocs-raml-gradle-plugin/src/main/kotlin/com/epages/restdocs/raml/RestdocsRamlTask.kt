package com.epages.restdocs.raml

import com.epages.restdocs.raml.RamlVersion.V_0_8
import com.epages.restdocs.raml.RamlVersion.V_1_0
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
                .map { RamlFragment.fromFile(it) }
                .toList()

        writeFiles(ramlFragments, ".raml")

        if (separatePublicApi)
            writeFiles(ramlFragments.filterNot { it.privateResource }, "-public.raml")
    }


    private fun writeFiles(ramlFragments: List<RamlFragment>, fileNameSuffix: String) {

        val ramlApi = ramlFragments.groupBy { it.path }
                .map { (_, fragmentsWithSamePath) -> RamlResource.fromFragments(fragmentsWithSamePath, JsonSchemaMerger(outputDirectoryFile)) }
                .let { ramlResources -> ramlResources
                        .groupBy { it.firstPathPart }
                        .map { (firstPathPart, resources) -> ResourceGroup(firstPathPart, resources) } }
                .let { RamlApi(apiTitle, apiBaseUri, ramlVersion(), it) }

        RamlWriter.writeApi(
                fileFactory = { filename -> project.file("$outputDirectory/$filename") },
                api = ramlApi,
                apiFileName = "$outputFileNamePrefix$fileNameSuffix",
                groupFileNameProvider = { path -> groupFileName(path, fileNameSuffix) }
        )
    }

    private fun groupFileName(path: String, fileNameSuffix: String): String {
        val fileNamePrefix = if (path == "/") "root" else path
                .replaceFirst("/", "")
                .replace("\\{", "")
                .replace("}", "")
                .replace("/", "-")

        return if (fileNamePrefix == outputFileNamePrefix) "$fileNamePrefix-group$fileNameSuffix"
        else "$fileNamePrefix$fileNameSuffix"
    }

    private fun ramlVersion() = if (ramlVersion == "1.0") V_1_0 else V_0_8

    private fun copyBodyJsonFilesToOutput() {
        snippetsDirectoryFile.walkTopDown().forEach {
                    if (it.name.endsWith("-request.json") || it.name.endsWith("-response.json"))
                        Files.copy(it.toPath(), outputDirectoryFile.toPath().resolve(it.name), StandardCopyOption.REPLACE_EXISTING)
                }
    }
}
