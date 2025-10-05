package squawk.script

import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

@KotlinScript(
    fileExtension = "squawk.kts",
    compilationConfiguration = SquawkScriptConfig::class
)
@Suppress("unused")
abstract class SquawkScript(
    val scriptFile: File,
) {
    var endpoints = mutableListOf<EndpointBuilder>()

    fun endpoint(builder: EndpointBuilder.() -> Unit)
    {
        endpoints += EndpointBuilder().apply(builder)
    }
}

private fun evalFile(scriptFile: File): ResultWithDiagnostics<EvaluationResult>
{
    val source = scriptFile.toScriptSource()
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SquawkScript> {
        defaultImports("squawk.cli.Method")
        defaultImports("squawk.cli.Method.Companion.GET")
        defaultImports("squawk.cli.Method.Companion.POST")
        defaultImports("squawk.cli.Method.Companion.PUT")
        defaultImports("squawk.cli.Method.Companion.DELETE")
    }
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
