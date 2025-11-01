package squawk.cli

import squawk.script.EndpointBuilder
import squawk.script.ScriptEvaluationResult

class EndpointLabels(
    result: ScriptEvaluationResult
) {
    val names = result.allEndpointResults
        .flatMap { (endpointScript, endpoints) ->
            endpoints.map { createCanonicalName(endpointScript, it) }
        }

    private fun createCanonicalName(
        script: ScriptEvaluationResult,
        endpoint: EndpointBuilder
    ): String {
        val prefix = script.namespace?.let { "$it:" }.orEmpty()
        if (endpoint.name != null) {
            return endpoint.name!!.lowercase().replace(' ', '-').let { "$prefix$it" }
        }
        val matchingMethods = script.endpointResults.filter { it.name == null && it.method == endpoint.method }
        val duplicateIndex = matchingMethods.indexOfFirst { it === endpoint }
            .takeIf { it != -1 }
            ?: throw IllegalStateException("Endpoint not in method list")

        return when {
            matchingMethods.size == 1 || duplicateIndex == 0 -> endpoint.method.key.lowercase()
            else -> "${endpoint.method.key.lowercase()}-${duplicateIndex + 1}"
        }.let { "$prefix$it" }
    }
}
