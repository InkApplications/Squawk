package squawk.host

import squawk.script.Evaluator
import squawk.script.RunConfiguration
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
        runConfiguration: RunConfiguration,
        parent: SquawkScript?,
    ): SquawkScript {
        return host.eval(
            script = runConfiguration.target.toScriptSource(),
            compilationConfiguration = compilationConfiguration,
            evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<SquawkScript> {
                constructorArgs(runConfiguration, this@ScriptEvaluator, parent)
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
    runConfiguration: RunConfiguration,
): SquawkScript {
    return ScriptEvaluator.evaluateFile(runConfiguration, null)
}
