package squawk.cli

import squawk.script.EndpointBuilder
import squawk.script.RequestBuilder
import squawk.script.ScriptEvaluationResult
import squawk.script.WebsocketBuilder

class EndpointLabels(
    private val result: ScriptEvaluationResult
) {
    val names = result.allRequestBuilders
        .flatMap { (endpointScript, endpoints) ->
            endpoints.map { createCanonicalName(endpointScript, it) }
        }

    private fun createCanonicalName(
        script: ScriptEvaluationResult,
        request: RequestBuilder
    ): String {
        val prefix = script.namespace?.let { "$it:" }.orEmpty()
        if (request.name != null) {
            return request.name!!.lowercase().replace(' ', '-').let { "$prefix$it" }
        }
        val matchingMethods = result.allRequestBuilders
            .flatMap { it.second }
            .filter { it.name == null && it.methodLabel == request.methodLabel }
        val duplicateIndex = matchingMethods.indexOfFirst { it === request }
            .takeIf { it != -1 }
            ?: throw IllegalStateException("Endpoint not in method list")

        return when {
            matchingMethods.size == 1 || duplicateIndex == 0 -> request.methodLabel
            else -> "${request.methodLabel}-${duplicateIndex + 1}"
        }.let { "$prefix$it" }
    }

    private val RequestBuilder.methodLabel: String get() = when (this) {
        is EndpointBuilder -> method.key.lowercase()
        is WebsocketBuilder -> "websocket"
    }
}
