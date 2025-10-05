package squawk.cli

import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.constructorArgs
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
    fun endpoint(builder: EndpointBuilder.() -> Unit)
    {
        EndpointBuilder().apply(builder)
            .run {
                println("Endpoint: name='$name', url='$url'")
            }
    }

    companion object
    {
        internal fun evalFile(scriptFile: File): ResultWithDiagnostics<EvaluationResult>
        {
            val source = scriptFile.toScriptSource()
            val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SquawkScript> {
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
    }
}
