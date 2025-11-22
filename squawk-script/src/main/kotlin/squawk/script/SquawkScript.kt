package squawk.script

import java.io.File
import java.util.Properties
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript
    (
    fileExtension = "squawk",
    compilationConfiguration = SquawkScriptConfig::class
)
@Suppress("unused")
abstract class SquawkScript(
    val runConfiguration: RunConfiguration,
    private val evaluator: Evaluator,
) {
    private var children: MutableList<SquawkScript> = mutableListOf()
    private val overrides: Map<String, String> by lazy {
        runConfiguration.propertyFiles
            .map { it.file }
            .flatMap { file ->
                Properties()
                    .apply { file.inputStream().use { load(it) } }
                    .map { (key, value) -> key.toString() to value.toString() }
            }.toMap() + runConfiguration.properties
    }
    private var requestBuilders = mutableListOf<RequestBuilder>()
    private var localProperties = mutableMapOf<String, String>()
    private val allProperties: Map<String, String> get() {
        return runConfiguration.parentProperties + localProperties + overrides
    }
    var namespace: String? = null

    fun toScriptEvaluationResult(): ScriptEvaluationResult
    {
        return ScriptEvaluationResult(
            configuration = runConfiguration,
            namespace = namespace,
            requestBuilders = requestBuilders,
            children = children.map { it.toScriptEvaluationResult() }
        )
    }

    fun endpoint(builder: EndpointBuilder.() -> Unit)
    {
        requestBuilders += EndpointBuilder(allProperties).apply(builder)
    }

    fun websocket(builder: WebsocketBuilder.() -> Unit)
    {
        requestBuilders += WebsocketBuilder(allProperties).apply(builder)
    }

    fun include(path: String)
    {
        val file = File(runConfiguration.target.file.parentFile, path).canonicalFile
        if (!file.exists()) {
            throw IncludedFileNotFound(path, runConfiguration.target.file)
        }

        try {
            children += evaluator.evaluateFile(
                runConfiguration = runConfiguration.
                copy(
                    target = file.loadDescriptor(),
                    parentProperties = localProperties,
                ),
            )
        } catch (e: ConfigurationError) {
            if (e.context == null) throw e.withContext(file)
            else throw e
        }
    }

    fun loadProperties(path: String, optional: Boolean = false)
    {
        val file = File(runConfiguration.target.file.parentFile, path).canonicalFile

        if (optional && !file.exists()) {
            return
        }
        if (!file.exists()) {
            throw PropertiesFileNotFound(path, runConfiguration.target.file)
        }

        localProperties += Properties()
            .apply { file.inputStream().use { load(it) } }
            .map { (key, value) -> key.toString() to value.toString() }
            .toMap()
    }
}
