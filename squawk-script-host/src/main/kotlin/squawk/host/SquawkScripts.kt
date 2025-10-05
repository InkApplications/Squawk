package squawk.host

import squawk.script.SquawkScript
import java.io.File
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.constructorArgs
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
    return evalFile(result)
        .valueOrThrow()
        .returnValue
        .scriptInstance
        .let { it as SquawkScript }
}
