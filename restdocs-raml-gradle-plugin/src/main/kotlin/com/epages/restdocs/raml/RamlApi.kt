package com.epages.restdocs.raml

import com.epages.restdocs.raml.RamlVersion.V_1_0
import java.io.File

data class RamlApi(val title: String, val baseUri: String?, val ramlVersion: RamlVersion, private val _resourceGroups: List<ResourceGroup> ) {
    val resourceGroups by lazy {
        _resourceGroups.sortedBy { it.firstPathPart }
    }

    fun toMainFileMap(groupFileNameProvider: (String) -> String) =
            mapOf("title" to title)
                    .let { if (baseUri != null) it.plus("baseUri" to baseUri) else it }
                    .plus(resourceGroups.map { it.firstPathPart to Include(groupFileNameProvider(it.firstPathPart)) } )
                    .toMap()

    fun toResourceGroupRamlMaps(ramlVersion: RamlVersion) = resourceGroups.map { it.toRamlMap(ramlVersion) }
}

enum class RamlVersion(val versionString: String) {
    V_1_0("#%RAML 1.0"),
    V_0_8("#%RAML 0.8")
}

interface ToRamlMap { fun toRamlMap(ramlVersion: RamlVersion): Map<*, *> }

data class ResourceGroup(val firstPathPart: String, private val _ramlResources: List<RamlResource>): ToRamlMap {
    val ramlResources by lazy {
        _ramlResources.map { it.copy(path = it.path.removePrefix(firstPathPart)) }.sortedBy { it.path.length }
    }

    override fun toRamlMap(ramlVersion: RamlVersion): Map<*, *> =
        ramlResources.flatMap { it.toRamlMap(ramlVersion).toList() }.toMap()
}

data class Parameter(val name: String, val description: String, val type: String): ToRamlMap {
    override fun toRamlMap(ramlVersion: RamlVersion): Map<*, *> =
        mapOf(name to mapOf(
                "description" to description,
                "type" to type
        ))
}

fun List<ToRamlMap>.toRamlMap(key: String, ramlVersion: RamlVersion): Map<*, *> =
        toRamlMap(ramlVersion)
                .let { if (it.isEmpty()) it else mapOf(key to it) }

fun List<ToRamlMap>.toRamlMap(ramlVersion: RamlVersion): Map<*, *> =
        this.flatMap { it.toRamlMap(ramlVersion).toList() }.toMap()

data class Body(val contentType: String,
                val example: Include? = null,
                val schema: Include? = null,
                val examples: List<Include> = emptyList()): ToRamlMap {

    override fun toRamlMap(ramlVersion: RamlVersion): Map<*, *> {

        return mapOf(contentType to
                when (ramlVersion) {
                    V_1_0 -> mapOf("examples" to examples.map { it.location
                            .replace("-request.json", "")
                            .replace("-response.json", "") to it }.toMap())
                    else -> mapOf("example" to examples.firstOrNull())
                }.let {
                    if (schema != null) it.plus((if (ramlVersion == V_1_0) "type" else "schema") to schema)
                    else it
                }
        )
    }
}

data class Response(val status: Int, val bodies: List<Body>): ToRamlMap {
    override fun toRamlMap(ramlVersion: RamlVersion): Map<*, *> =
            mapOf(status to bodies.toRamlMap(ramlVersion))
}

data class Method(val method: String,
                  val description: String? = null,
                  val queryParameters: List<Parameter> = emptyList(),
                  val traits: List<String> = emptyList(),
                  val securedBy: List<String> = emptyList(),
                  val requestBodies: List<Body> = emptyList(),
                  val responses: List<Response> = emptyList()): ToRamlMap {

    override fun toRamlMap(ramlVersion: RamlVersion): Map<*, *> =
            mapOf(method to (if (description != null) mapOf("description" to description) else emptyMap())
                    .plus(queryParameters.toRamlMap("queryParameters", ramlVersion))
                    .let { if (traits.isNotEmpty()) it.plus("is" to traits) else it }
                    .let { if (securedBy.isNotEmpty()) it.plus("securedBy" to securedBy) else it }
                    .plus(requestBodies.toRamlMap("body", ramlVersion))
                    .plus(responses.toRamlMap("responses", ramlVersion))
            )
}

data class RamlResource(val path: String,
                        val methods: List<Method> = emptyList(),
                        val uriParameters: List<Parameter> = emptyList()): ToRamlMap {
    val firstPathPart by lazy {
        path.split("/").find { !it.isEmpty() }?.let{ "/$it" }?:"/"
    }

    override fun toRamlMap(ramlVersion: RamlVersion): Map<*, *> =
            uriParameters.toRamlMap("uriParameters", ramlVersion)
                    .plus(methods.flatMap { it.toRamlMap(ramlVersion).toList() }.toMap() )
                    .let { if (path.isEmpty()) it else mapOf(path to it)}

    companion object {
        fun fromFragments(allFragments: List<RamlFragment>, jsonSchemaMerger: JsonSchemaMerger): RamlResource {
            if (allFragments.groupBy { it.path }.size > 1)
                throw IllegalArgumentException("Fragments for a resource must have a common path")

            val methods = allFragments
                    .groupBy { it.method.method }
                    .map { (_, fragments) ->
                        val bodiesByContentType = fragments
                                .mapNotNull { it.method.requestBodies.firstOrNull() }
                                .groupBy { it.contentType }
                        val responsesByStatus = fragments
                                .mapNotNull { it.method.responses.firstOrNull() }
                                .groupBy { it.status }

                        fragments.first().method.copy(
                                requestBodies = mergeBodiesWithSameContentType(bodiesByContentType, jsonSchemaMerger),
                                responses = mergeResponsesWithSameStatusAndContentType(responsesByStatus, jsonSchemaMerger)
                        )
                    }

            return RamlResource(allFragments.first().path, methods, allFragments.first().uriParameters)
        }

        private fun mergeBodiesWithSameContentType(
                bodiesByContentType: Map<String, List<Body>>,
                jsonSchemaMerger: JsonSchemaMerger): List<Body> {
            return bodiesByContentType.map { (contentType, bodies) ->
                Body(
                        contentType = contentType,
                        examples = bodies.mapNotNull { it.example },
                        schema = jsonSchemaMerger.mergeSchemas(bodies.mapNotNull { it.schema })
                )
            }
        }

        private fun mergeResponsesWithSameStatusAndContentType(
                responsesByStatus: Map<Int, List<Response>>,
                jsonSchemaMerger: JsonSchemaMerger): List<Response> {
            return responsesByStatus.map { (status, responses) ->
                Response(
                        status = status,
                        bodies = mergeBodiesWithSameContentType(responses
                                .flatMap { it.bodies }
                                .groupBy { it.contentType }, jsonSchemaMerger)
                )
            }
        }
    }
}

data class RamlFragment(val id: String,
                        val path: String,
                        val method: Method,
                        val uriParameters: List<Parameter> = emptyList()) {

    @Suppress("UNCHECKED_CAST")
    val privateResource = method.traits.contains("private")

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromYamlMap(id: String, yamlMap: Map<*, *>): RamlFragment {

            val path = yamlMap.keys.first()
            val values = yamlMap[path] as Map<*, *>
            val uriParameters = (values["uriParameters"] as? Map<*,*>).orEmpty()
            val methodMap = values.filterKeys { it != "uriParameters" }
            return RamlFragment(
                    id = id,
                    path = path as String,
                    uriParameters = parameters(uriParameters),
                    method = method(methodMap)
            )
        }

        fun fromFile(file: File): RamlFragment {
            val id = file.path
                    .removeSuffix(file.name)
                    .removeSuffix(File.separator)
                    .split(File.separator)
                    .let { it[it.size - 1] }
            return fromYamlMap(id, RamlParser.parseFragment(file))
        }

        private fun body(map: Map<*,*>): Body {
            val contentType = map.keys.first() as String
            val values = map[contentType] as Map<*,*>
            return Body(
                    contentType = contentType,
                    example = values["example"] as Include,
                    schema = values["schema"] as? Include
            )
        }

        private fun response(map: Map<*,*>): Response {
            val status = map.keys.first() as Int
            val values = map[status] as Map<*,*>
            return Response(
                    status = status,
                    bodies = listOf(body(values["body"] as Map<*,*>))
            )
        }

        private fun method(map: Map<*,*>): Method {
            val methodContent = map[map.keys.first()] as Map<*,*>
            val response = methodContent["responses"] as? Map<*,*>
            return Method(
                    method = map.keys.first() as String,
                    description = methodContent["description"] as? String,
                    requestBodies = (methodContent["body"] as? Map<*,*>)?.let { listOf(body(it)) }.orEmpty(),
                    queryParameters = parameters((methodContent["queryParameters"] as? Map<*,*>).orEmpty()),
                    traits =  (methodContent["is"] as? List<String>).orEmpty(),
                    securedBy =  (methodContent["securedBy"] as? List<String>).orEmpty(),
                    responses = response?.let { listOf(response(it)) }.orEmpty()
            )
        }

        private fun parameters(map: Map<*,*>): List<Parameter> {
            return map.map { (key, value) -> with(value as Map<*, *>) {
                Parameter(key as String, value["description"] as String, value["type"] as String)
            } }
        }
    }
}
