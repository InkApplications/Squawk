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
    val runConfiguration: RunConfiguration,
    private val evaluator: Evaluator,
    private val parent: SquawkScript?,
) {
    private var children: MutableList<SquawkScript> = mutableListOf()
    private val overrides: Map<String, String> by lazy {
        runConfiguration.propertyFiles.flatMap { file ->
            Properties()
                .apply { file.inputStream().use { load(it) } }
                .map { (key, value) -> key.toString() to value.toString() }
        }.toMap() + runConfiguration.properties
    }
    private var localEndpoints = mutableListOf<EndpointBuilder>()
    private var localProperties = mutableMapOf<String, String>()
    private val allProperties: Map<String, String> get() {
        return parent?.allProperties.orEmpty() + localProperties + overrides
    }
    var namespace: String? = null
    val endpoints: List<EndpointBuilder> get() {
        return localEndpoints + children.flatMap { it.endpoints }
    }
    val scriptEndpoints: Map<SquawkScript, List<EndpointBuilder>> get() {
        return (listOf(this) + children).associateWith { it.localEndpoints }
    }

    fun endpoint(builder: EndpointBuilder.() -> Unit)
    {
        localEndpoints += EndpointBuilder(allProperties).apply(builder)
    }

    fun include(path: String)
    {
        val file = File(runConfiguration.target.parentFile, path).canonicalFile
        if (!file.exists()) {
            throw IncludedFileNotFound(path, runConfiguration.target)
        }

        try {
            children += evaluator.evaluateFile(
                runConfiguration = runConfiguration.copy(
                    target = file,
                ),
                parent = this,
            )
        } catch (e: ConfigurationError) {
            if (e.context == null) throw e.withContext(file)
            else throw e
        }
    }

    fun loadProperties(path: String, optional: Boolean = false)
    {
        val file = File(runConfiguration.target.parentFile, path).canonicalFile

        if (optional && !file.exists()) {
            return
        }
        if (!file.exists()) {
            throw PropertiesFileNotFound(path, runConfiguration.target)
        }

        localProperties += Properties()
            .apply { file.inputStream().use { load(it) } }
            .map { (key, value) -> key.toString() to value.toString() }
            .toMap()
    }
}
