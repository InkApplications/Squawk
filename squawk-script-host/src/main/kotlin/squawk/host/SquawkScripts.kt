package squawk.host

import squawk.script.Evaluator
import squawk.script.SquawkScript
import java.io.File
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.onFailure
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

private object ScriptEvaluator: Evaluator
{
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SquawkScript>()
    val host = BasicJvmScriptingHost()

    override fun evaluateFile(
        file: File,
        parent: SquawkScript?,
        propertyFiles: List<File>,
        properties: Map<String, String>,
    ): SquawkScript {
        return host.eval(
            script = file.toScriptSource(),
            compilationConfiguration = compilationConfiguration,
            evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<SquawkScript> {
                constructorArgs(file, this@ScriptEvaluator, parent, propertyFiles, properties)
            }
        ).handleOrThrow()
    }

    private fun ResultWithDiagnostics<EvaluationResult>.handleOrThrow(): SquawkScript
    {
        onFailure { result ->
            throw result.reports
                .filter { it.severity > ScriptDiagnostic.Severity.INFO }
                .map {
                    ScriptEvaluationException.ScriptError(
                        message = it.message,
                        startLine = it.location?.start?.let {
                            ScriptEvaluationException.FileCoordinate(
                                line = it.line,
                                column = it.col,
                            )
                        },
                        endLine = it.location?.end?.let {
                            ScriptEvaluationException.FileCoordinate(
                                line = it.line,
                                column = it.col,
                            )
                        },
                        exception = it.exception,
                    )
                }
                .let { ScriptEvaluationException(it) }
        }

        return when (val returnValue = valueOrThrow().returnValue) {
            is ResultValue.Error -> throw returnValue.error
            is ResultValue.NotEvaluated -> throw IllegalStateException("Script was not evaluated")
            else -> returnValue.scriptInstance as SquawkScript
        }
    }
}

fun evaluateOrThrow(
    result: File,
    propertyFiles: List<File>,
    properties: Map<String, String>,
): SquawkScript {
    return ScriptEvaluator.evaluateFile(result, null, propertyFiles, properties)
}
