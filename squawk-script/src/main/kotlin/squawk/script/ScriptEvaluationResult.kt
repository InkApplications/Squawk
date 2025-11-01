package squawk.script

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ScriptEvaluationResult(
    val descriptor: FileDescriptor,
    val endpointResults: List<EndpointBuilder>,
    val children: List<ScriptEvaluationResult>,
    val namespace: String?,
) {
    @Transient
    val allEndpointResults: List<Pair<ScriptEvaluationResult, List<EndpointBuilder>>> =
        listOf(this to endpointResults)
        .plus(
            children.flatMap { it.allEndpointResults }
                .map { (childResult, endpoints) -> childResult to endpoints }
        )
}
