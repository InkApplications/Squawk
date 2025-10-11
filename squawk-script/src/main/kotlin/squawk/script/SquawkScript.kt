package squawk.script

import java.io.File
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    fileExtension = "squawk",
    compilationConfiguration = SquawkScriptConfig::class
)
@Suppress("unused")
abstract class SquawkScript(
    val scriptFile: File,
    private val evaluator: Evaluator,
    val parent: SquawkScript? = null
) {
    var endpoints = mutableListOf<EndpointBuilder>()

    fun endpoint(builder: EndpointBuilder.() -> Unit)
    {
        if (parent != null) {
            parent.endpoint(builder)
            return
        }
        endpoints += EndpointBuilder().apply(builder)
    }

    fun include(path: String)
    {
        val file = File(scriptFile.parentFile, path).canonicalFile
        if (!file.exists()) {
            throw IllegalArgumentException("Included file does not exist: $file")
        }

        evaluator.evaluateFile(this, file)
    }
}
