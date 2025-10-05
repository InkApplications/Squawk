package squawk.host

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

private fun evalFile(scriptFile: File): ResultWithDiagnostics<EvaluationResult>
{
    val source = scriptFile.toScriptSource()
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SquawkScript>()
    val evaluationConfiguration = createJvmEvaluationConfigurationFromTemplate<SquawkScript> {
        constructorArgs(scriptFile)
    }
    return BasicJvmScriptingHost().eval(
        script = source,
        compilationConfiguration = compilationConfiguration,
        evaluationConfiguration = evaluationConfiguration
    )
}

fun evaluateOrThrow(result: File): SquawkScript
{
    val result = evalFile(result)
    result.onFailure { result ->
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

    return when (val returnValue = result.valueOrThrow().returnValue) {
        is ResultValue.Error -> throw returnValue.error
        is ResultValue.NotEvaluated -> throw IllegalStateException("Script was not evaluated")
        else -> returnValue.scriptInstance as SquawkScript
    }
}
