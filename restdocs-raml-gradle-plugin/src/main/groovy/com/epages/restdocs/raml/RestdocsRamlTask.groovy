package com.epages.restdocs.raml

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.util.regex.Pattern

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING

class RestdocsRamlTask extends DefaultTask {

    @Input
    Property<String> ramlVersion = project.objects.property(String)
    @Input
    Property<String> apiBaseUri = project.objects.property(String)
    @Input
    Property<String> apiTitle = project.objects.property(String)
    @Input
    Property<String> outputDirectory
    @Input
    Property<String> snippetsDirectory

    static String version08 = "#%RAML 0.8"
    static String version10 = "#%RAML 1.0"

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
        writeFiles(ramlFragments, true)
    }

    private def copyBodyJsonFilesToOutput() {
        getSnippetsDirectory()
                .eachFileRecurse { if (it.name.endsWith("-request.json") || it.name.endsWith("-response.json")) Files.copy(it.toPath(), getOutputDirectory().toPath().resolve(it.name), REPLACE_EXISTING) }
    }

    private void writeFiles(RamlFragments ramlFragments, boolean ignorePrivate) {

        def groupedFragments = ramlFragments.groupByFirstPathPart(ignorePrivate)

        writeGroupFiles(groupedFragments, getOutputDirectory(), ignorePrivate)
        def aggregateFileName = ignorePrivate ? "${outputDirectory.get()}/${outputFileNamePrefix}-public.raml" : "${outputDirectory.get()}/${outputFileNamePrefix}.raml"
        writeAggregateFile(groupedFragments, project.file(aggregateFileName), ignorePrivate)
    }

    def writeGroupFiles(List<RamlFragments> ramlFragmentsList, File outputDirectory, boolean ignorePrivate) {
        ramlFragmentsList.each { fragments ->
            new File(outputDirectory, fragments.getGroupFileName(ignorePrivate)).withWriter('utf-8') { writer ->
                fragments.ramlFragments.each { fragment ->
                    if (!fragment.remainingPath(fragments.commonPath).isEmpty()) {
                        writer.write("  ${fragment.remainingPath(fragments.commonPath)}:\n")
                        writer.write("  ${fragment.remainingContent.replaceAll("\n", "\n  ")}\n")
                    } else {
                        writer.write("${fragment.remainingContent}\n")
                    }
                }
            }
        }
    }

    def writeAggregateFile(List<RamlFragments> ramlFragmentsList, File outputFile, boolean ignorePrivate) {
        outputFile.withWriter('utf-8') { writer ->

            def ramlVersionString = ramlVersion.get() == "0.8" ? version08 : version10
            writer.write("$ramlVersionString\n")
            if (apiTitle.isPresent()) {
                writer.write("title: ${apiTitle.get()}\n")
            }
            if (apiBaseUri.isPresent()) {
                writer.write("baseUri: ${apiBaseUri.get()}\n")
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
        def fileNamePrefix = commonPath
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
                    if (!remainingPath.isEmpty() && fragments.size() > 1) {
                        new RamlFragment(fragments.inject(["$remainingPath:"]) { contentList, item -> contentList + item.remainingContentList})
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
        "/" + path.split("/").find { !it.isEmpty() }
    }

    String remainingPath(String prefix) {
        path.replaceAll(Pattern.quote(prefix), "")
    }
    String toString() {
        "path: $path\n remainingContent: $remainingContent\n"
    }
}
