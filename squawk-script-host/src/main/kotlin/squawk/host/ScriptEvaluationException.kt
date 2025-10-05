package squawk.host

import java.lang.RuntimeException

class ScriptEvaluationException(
    val errors: List<ScriptError>
): RuntimeException(
    when (errors.size) {
        0 -> "Script evaluation failed with unknown error"
        1 -> errors.single().message
        else -> "Script evaluation failed with ${errors.size} errors."
    },
    errors.singleOrNull(),
) {
    data class ScriptError(
        override val message: String,
        val startLine: FileCoordinate?,
        val endLine: FileCoordinate?,
        val exception: Throwable?,
    ): Error()

    data class FileCoordinate(
        val line: Int,
        val column: Int,
    )
}
