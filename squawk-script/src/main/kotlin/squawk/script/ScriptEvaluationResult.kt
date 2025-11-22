package squawk.script

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ScriptEvaluationResult(
    val configuration: RunConfiguration,
    val requestBuilders: List<RequestBuilder>,
    val children: List<ScriptEvaluationResult>,
    val namespace: String?,
) {
    @Transient
    val allRequestBuilders: List<Pair<ScriptEvaluationResult, List<RequestBuilder>>> =
        listOf(this to requestBuilders)
        .plus(
            children.flatMap { it.allRequestBuilders }
                .map { (childResult, endpoints) -> childResult to endpoints }
        )
}
