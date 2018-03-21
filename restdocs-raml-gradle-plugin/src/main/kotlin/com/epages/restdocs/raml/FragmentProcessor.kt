package com.epages.restdocs.raml

import java.io.File


class FragmentProcessor(val aggregateFragmentsForSameResource: Boolean) {

    fun groupFragments(fragments: List<RamlFragment>): List<FragmentGroup> {
        return fragments
                .groupBy { it.firstPathPart }
                .mapValues { entry -> removeCommonPathFromFragmentPath(entry) }
                .mapValues { entry -> mergeFragmentsWithSamePath(entry.value) }
                .map { entry -> FragmentGroup(entry.key, entry.value) }
    }

    fun aggregateFileMap(apiTitle: String, apiBaseUri: String?, outputFileNamePrefix: String, fragmentGroups: List<FragmentGroup>, fileNameSuffix: String = ".raml"): Map<*, *> {
        return listOfNotNull(
                "title" to apiTitle,
                apiBaseUri?.let { "baseUri" to apiBaseUri })
                .plus(fragmentGroups.map { it.commonPathPrefix to Include(groupFileName(it.commonPathPrefix, fileNameSuffix, outputFileNamePrefix)) })
                .toMap()
    }

    fun groupFileMap(fragmentGroup: FragmentGroup): Map<*, *> {
        return fragmentGroup.ramlFragments
                .flatMap {
                    if (it.path.isNotEmpty()) listOf(it.path to it.content)
                    else it.content.toList()
                }.toMap()
    }

    fun groupFileName(path: String, fileNameSuffix: String, outputFileNamePrefix: String): String {
        val fileNamePrefix = if (path.equals("/")) "root" else path
                .replaceFirst("/", "")
                .replace("\\{", "")
                .replace("}", "")
                .replace("/", "-")

        return if (fileNamePrefix == outputFileNamePrefix) "$fileNamePrefix-group$fileNameSuffix"
        else "$fileNamePrefix$fileNameSuffix"
    }

    private fun mergeFragmentsWithSamePath(fragments: List<RamlFragment>): List<RamlFragment> {
        return fragments
                .groupBy { it.path }
                .map { (_, fragments) ->
                    if (fragments.size > 1) reduceFragmentsWithSamePath(fragments)
                    else fragments.single()
                }
    }

    private fun reduceFragmentsWithSamePath(value: List<RamlFragment>): RamlFragment {
        return value.groupBy { it.requestMethod }
                .map { (_, fragments) -> mergeFragmentsWithSameRequestMethod(fragments) }
                .reduce({ f1, f2 -> RamlFragment(f1.id, f1.path, f1.content + f2.content) })
    }

    private fun mergeFragmentsWithSameRequestMethod(fragments: List<RamlFragment>): RamlFragment {

        fun replaceExamples(fragments: List<RamlFragment>, target: Map<*,*>, mapExtractor: (RamlFragment) -> Map<*,*>?) =
                fragments
                        .mapNotNull { fragment -> mapExtractor(fragment)?.findValueByKeyRecursive("example")
                                ?.let { example -> Pair(fragment.id, example) } }
                        .toMap()
                        .let { examplesMap -> target.replaceMapEntryRecursive("example", "examples", { _ -> examplesMap }) }

        if (!aggregateFragmentsForSameResource)
            return fragments.first() // for RAML 0.8 we cannot aggregate examples because multiple examples are not supported

        return fragments.first()
                .let { fragment -> fragment.requestBody
                        ?.let { fragment.withBody(replaceExamples(fragments, it, { f -> f.requestBody })) }
                        ?: fragment }
                .let { fragment -> fragment.responses
                        ?.let { fragment.withResponses(replaceExamples(fragments, it, { f -> f.responses })) }
                        ?: fragment }
    }


    private fun removeCommonPathFromFragmentPath(entry: Map.Entry<String, List<RamlFragment>>) =
            entry.value
                    .map { frag -> frag.copy(path = frag.path.replaceFirst(entry.key, "")) }
}

data class FragmentGroup(val commonPathPrefix: String, private val _ramlFragments: List<RamlFragment>) {

    val ramlFragments//sort by path length - shorter paths first
        get() = _ramlFragments.sortedBy { it.path.length }
}

data class RamlFragment(val id: String, val path: String, val content: Map<*, *>) {

    @Suppress("UNCHECKED_CAST")
    val privateResource by lazy {
        content.anyMatchRecursive{ key is String && key == "is" && value is List<*> && (value as List<String>).contains("private") }
    }

    val firstPathPart by lazy {
        path.split("/").find { !it.isEmpty() }
                ?.let{ "/$it" }?:"/"
    }

    val requestMethod by lazy { content.keys.first() as String }

    val requestBody
        get() = content.findValueByKeyRecursive("body", listOf("responses")) as? Map<*,*>

    val responses
        get() = content.findValueByKeyRecursive("responses") as? Map<*,*>

    fun withBody(body: Map<*,*>) = copy(content = content.replaceMapEntryRecursive("body", "body", { _ -> body }, listOf("responses")))

    fun withResponses(responses: Map<*,*>) = copy(content = content.replaceMapEntryRecursive("responses", "responses", { _ -> responses }))

    fun replaceSchemaWithType(): RamlFragment {
        return RamlFragment(id, path, content.replaceMapEntryRecursive("schema", "type"))
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromYamlMap(id: String, yamlMap: Map<*, *>): RamlFragment {
            return RamlFragment(
                    id = id,
                    path = yamlMap.keys.first() as String,
                    content = yamlMap.values.first() as Map<*, *>)
        }

        fun fromFile(file: File): RamlFragment {
            val id = file.path
                    .removeSuffix(file.name)
                    .removeSuffix(File.separator)
                    .split(File.separator)
                    .let { it[it.size - 1] }
            return fromYamlMap(id, RamlParser.parseFragment(file))
        }
    }
}
