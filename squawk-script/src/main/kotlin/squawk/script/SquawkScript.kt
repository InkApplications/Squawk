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
) {
    private var children: MutableList<SquawkScript> = mutableListOf()
    private var localEndpoints = mutableListOf<EndpointBuilder>()
    var namespace: String? = null
    val endpoints: List<EndpointBuilder> get() {
        return localEndpoints + children.flatMap { it.endpoints }
    }
    val scriptEndpoints: Map<SquawkScript, List<EndpointBuilder>> get() {
        return (listOf(this) + children).associateWith { it.localEndpoints }
    }

    fun endpoint(builder: EndpointBuilder.() -> Unit)
    {
        localEndpoints += EndpointBuilder().apply(builder)
    }

    fun include(path: String)
    {
        val file = File(scriptFile.parentFile, path).canonicalFile
        if (!file.exists()) {
            throw IllegalArgumentException("Included file does not exist: $file")
        }

        children += evaluator.evaluateFile(file)
    }
}
