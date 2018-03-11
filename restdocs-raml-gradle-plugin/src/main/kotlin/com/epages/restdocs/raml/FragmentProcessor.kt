package com.epages.restdocs.raml


object FragmentProcessor {

    fun groupFragments(fragments: List<RamlFragment>): List<FragmentGroup> {
        return fragments
                .groupBy { it.firstPathPart }
                .mapValues { entry -> removeCommonPathFromFragmentPath(entry) }
                .mapValues { entry -> mergeFragmentsWithSamePath(entry.value) }
                .map { entry -> FragmentGroup(entry.key, entry.value) }
    }

    fun aggregateFileMap(apiTitle: String, apiBaseUri: String?, ramlVersion: String, outputFileNamePrefix: String, fragmentGroups: List<FragmentGroup>, fileNameSuffix: String = ".raml"): Map<*, *> {
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
                .map {
                    if (it.value.size > 1)
                        it.value.reduce({ f1, f2 -> RamlFragment(f1.path, f1.content.plus(f2.content)) })
                    else it.value.single()
                }
    }

    private fun removeCommonPathFromFragmentPath(entry: Map.Entry<String, List<RamlFragment>>) =
            entry.value
                    .map { frag -> frag.copy(path = frag.path.replaceFirst(entry.key, "")) }
}

data class FragmentGroup(val commonPathPrefix: String, private val _ramlFragments: List<RamlFragment>) {

    val ramlFragments//sort by path length - shorter paths first
        get() = _ramlFragments.sortedBy { it.path.length }
}

data class RamlFragment(val path: String, val content: Map<*, *>) {

    @Suppress("UNCHECKED_CAST")
    val privateResource by lazy {
        content.anyMatchRecursive{ key is String && key == "is" && value is List<*> && (value as List<String>).contains("private") }
    }

    val firstPathPart by lazy {
        path.split("/").find { !it.isEmpty() }
                ?.let{ "/$it" }?:"/"
    }

    fun replaceSchemaWithType(): RamlFragment {
        return RamlFragment(path, content.replaceLKeyRecursive("schema", "type"))
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromYamlMap(yamlMap: Map<*, *>): RamlFragment {
            return RamlFragment(
                    yamlMap.keys.first() as String,
                    yamlMap.values.first() as Map<*, *>)
        }
    }
}
