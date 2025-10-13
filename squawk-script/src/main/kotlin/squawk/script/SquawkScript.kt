package squawk.script

import java.io.File
import java.util.Properties
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
    private var localProperties = mutableMapOf<String, String>()
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

    fun loadProperties(path: String, optional: Boolean = false)
    {
        val file = File(scriptFile.parentFile, path).canonicalFile

        if (optional && !file.exists()) {
            return
        }
        if (!file.exists()) {
            throw IllegalArgumentException("Properties file does not exist: $file")
        }

        localProperties += Properties()
            .apply { file.inputStream().use { load(it) } }
            .map { (key, value) -> key.toString() to value.toString() }
            .toMap()
    }

    fun property(key: String, default: String? = null): String
    {
        return localProperties[key]
            ?: default
            ?: throw IllegalArgumentException("Property not found: $key")
    }

    fun hasProperty(key: String): Boolean
    {
        return localProperties.containsKey(key)
    }
}
