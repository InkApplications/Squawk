package squawk.script

import kotlinx.serialization.Serializable

@Serializable
data class ScriptEvaluationResult(
    val descriptor: FileDescriptor,
    val endpointResults: List<EndpointBuilder>,
    val children: List<ScriptEvaluationResult>,
    val namespace: String?,
) {
    val allEndpointResults: List<Pair<ScriptEvaluationResult, List<EndpointBuilder>>> get() {
        val parentEntry = this to endpointResults
        val childrenEntries = children.flatMap { it.allEndpointResults }
            .map { (childResult, endpoints) -> childResult to endpoints }

        return listOf(parentEntry) + childrenEntries
    }
}
