package com.epages.restdocs.raml

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.util.regex.Pattern

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

class RestdocsRamlTask extends DefaultTask {

    @Input
    String ramlVersion

    @Input
    @Optional
    String apiBaseUri

    @Input
    @Optional
    String apiTitle

    @Input
    Boolean separatePublicApi

    @Input
    String outputDirectory

    @Input
    String snippetsDirectory

    static String version08 = "#%RAML 0.8"
    static String version10 = "#%RAML 1.0"

    static String ramlVersionSetting08 = "0.8"

    private String outputFileNamePrefix = "api"

    File getOutputDirectory() {
        project.file(outputDirectory)
    }

    File getSnippetsDirectory() {
        project.file(snippetsDirectory)
    }

    @TaskAction
    def aggregateRamlFragments() {
        getOutputDirectory().mkdirs()

        copyBodyJsonFilesToOutput()

        List<File> ramlFragmentFiles = []
        getSnippetsDirectory().eachFileRecurse { if (it.name.startsWith("raml-resource")) ramlFragmentFiles.add(it)}

        def ramlFragments = new RamlFragments(ramlFragmentFiles.collect { new RamlFragment(it.readLines())})

        writeFiles(ramlFragments, false)

        if (separatePublicApi)
            writeFiles(ramlFragments, true)
    }

    private def copyBodyJsonFilesToOutput() {
        getSnippetsDirectory()
                .eachFileRecurse { if (it.name.endsWith("-request.json") || it.name.endsWith("-response.json")) Files.copy(it.toPath(), getOutputDirectory().toPath().resolve(it.name), REPLACE_EXISTING) }
    }

    private void writeFiles(RamlFragments ramlFragments, boolean ignorePrivate) {

        def groupedFragments = ramlFragments.groupByFirstPathPart(ignorePrivate)

        writeGroupFiles(groupedFragments, getOutputDirectory(), ignorePrivate)
        def aggregateFileName = ignorePrivate ? "${outputDirectory}/${outputFileNamePrefix}-public.raml" : "${outputDirectory}/${outputFileNamePrefix}.raml"
        writeAggregateFile(groupedFragments, project.file(aggregateFileName), ignorePrivate)
    }

    def writeGroupFiles(List<RamlFragments> ramlFragmentsList, File outputDirectory, boolean ignorePrivate) {
        ramlFragmentsList.each { fragments ->
            new File(outputDirectory, fragments.getGroupFileName(ignorePrivate)).withWriter('utf-8') { writer ->
                fragments.ramlFragments.each { fragment ->
                    def remainingContent = ramlVersion == ramlVersionSetting08 ? fragment.remainingContent : fragment.remainingContent.replaceAll("schema: !include", "type: !include")
                    if (!fragment.remainingPath(fragments.commonPath).isEmpty()) {
                        writer.write("  ${fragment.remainingPath(fragments.commonPath)}:\n")
                        writer.write("  ${remainingContent.replaceAll("\n", "\n  ")}\n")
                    } else {
                        writer.write("${remainingContent}\n")
                    }
                }
            }
        }
    }

    def writeAggregateFile(List<RamlFragments> ramlFragmentsList, File outputFile, boolean ignorePrivate) {
        outputFile.withWriter('utf-8') { writer ->

            def ramlVersionString = ramlVersion == ramlVersionSetting08 ? version08 : version10
            writer.write("$ramlVersionString\n")
            writer.write("title: ${apiTitle == null ? "API documentation" : apiTitle}\n")

            if (apiBaseUri != null) {
                writer.write("baseUri: ${apiBaseUri}\n")
            }
            ramlFragmentsList.each { fragments ->
                writer.write("${fragments.commonPath}: !include ${fragments.getGroupFileName(ignorePrivate)}\n")
            }
        }
    }
}

class RamlFragments {
    String commonPath
    List<RamlFragment> ramlFragments

    RamlFragments(String commonPath = null, List<RamlFragment> ramlFragments) {
        this.commonPath = commonPath
        this.ramlFragments = ramlFragments
    }

    String getGroupFileName(boolean ignorePrivate) {
        def fileNamePrefix = commonPath.equals("/") ? "root" : commonPath
                .replaceFirst("/", "")
                .replaceAll("\\{", "")
                .replaceAll("}", "")
                .replaceAll("/", "-")
        def fileNameSuffix = ".raml"
        ignorePrivate ? "$fileNamePrefix-public$fileNameSuffix" : "$fileNamePrefix$fileNameSuffix"
    }

    List<RamlFragments> groupByFirstPathPart(boolean ignorePrivate) {
        ramlFragments.findAll { !ignorePrivate || !it.privateResource}
                .groupBy { it.firstPathPart() }
                .collect { new RamlFragments(it.key, it.value.sort { l -> l.remainingPath(it.key).size() })}
                .collect { mergeDuplicatePaths(it) }
    }

    RamlFragments mergeDuplicatePaths(RamlFragments ramlFragments) {
        List<RamlFragment> mergedFragmentList = ramlFragments.ramlFragments
                .groupBy { it.remainingPath(ramlFragments.commonPath) }
                .collect { remainingPath, fragments ->
                    if (fragments.size() > 1) {
                        String zero = remainingPath.isEmpty() ? "" : "$remainingPath:"
                        new RamlFragment(fragments.inject(["$zero"]) { contentList, item -> contentList + item.remainingContentList})
                    } else {
                        fragments.get(0)
                    }
                }
        new RamlFragments(ramlFragments.commonPath, mergedFragmentList)
    }

    String toString() { "commonPath: $commonPath\n fragments: $ramlFragments" }
}


class RamlFragment {
    String path
    String remainingContent
    List<String> remainingContentList
    boolean privateResource

    RamlFragment(List<String> contents) {
        this.path = contents.head().replaceAll(":", "")
        this.remainingContent = contents.tail().join("\n")
        this.remainingContentList = contents.tail()
        privateResource = remainingContent.find(Pattern.compile("is:.*\\[.*private.*]")) != null //has the private trait
    }

    String firstPathPart() {
        String firstPart = path.split("/").find { !it.isEmpty() }
        firstPart == null ? '/' : "/$firstPart"
    }

    String remainingPath(String prefix) {
        path.replaceAll(Pattern.quote(prefix), "")
    }

    String toString() {
        "path: $path\n remainingContent: $remainingContent\n"
    }
}
