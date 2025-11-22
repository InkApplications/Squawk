package squawk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import squawk.cli.formatting.printBadEndpointArgument
import squawk.cli.formatting.printEndpointLabel
import squawk.cli.formatting.printEndpointTitle
import squawk.cli.formatting.printProgress
import squawk.cli.formatting.rawOutput
import squawk.cli.formatting.printRequestUrl
import squawk.cli.formatting.printEvaluationError
import squawk.cli.formatting.printConfigurationError
import squawk.cli.formatting.printRequestMeta
import squawk.cli.formatting.printSocketClosed
import squawk.cli.formatting.printSocketOpen
import squawk.cli.formatting.printSocketReceiveFrame
import squawk.cli.formatting.printSocketSendFrame
import squawk.cli.formatting.printStatus
import squawk.cli.formatting.printTitle
import squawk.cli.formatting.printUnhandledError
import squawk.cli.formatting.printUnresolvedHost
import squawk.host.ScriptEvaluationException
import squawk.host.evaluateOrThrow
import squawk.script.ConfigurationError
import squawk.script.EndpointBuilder
import squawk.script.FileDescriptor
import squawk.script.OnConnectContext
import squawk.script.OnMessageContext
import squawk.script.RequestBuilder
import squawk.script.RunConfiguration
import squawk.script.ScriptEvaluationResult
import squawk.script.WebsocketBuilder
import squawk.script.cache.ScriptCacheRepository
import squawk.script.loadDescriptor
import java.io.File
import java.nio.channels.UnresolvedAddressException
import kotlin.time.measureTimedValue

class SquawkCommand: CliktCommand()
{
    private val scriptFile by option("--config", "-c")
        .help("Path to the main configuration script file to evaluate for endpoints (default: api.squawk)")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)
        .default(File("api.squawk"))
    private val endpointArg by argument(help = "Name of the endpoint to execute. If only one is defined, this argument is not required.")
        .optional()
    private val list by option("--list", "-l")
        .help("List the available endpoints by name and description.")
        .flag()
    private val propertyFiles by option("--properties", "--props")
        .help("Specify a properties file to load from. This will take precedent over any properties imported by the configuration (allows multiple)")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)
        .convert { it.loadDescriptor() }
        .multiple()
    private val properties by option("--property", "--prop")
        .help("Override a single property value. This argument has the highest precedence. Specified as key=value (allows multiple)")
        .convert {
            require(it.count { it == '=' } == 1) { "Properties must be in the form key=value" }
            it.split('=').let { (key, value) -> key to value }
        }
        .multiple()
    private val noCache by option("--no-cache")
        .help("Disable script caching for this run.")
        .flag()

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }
    private val requestScope = CoroutineScope(Dispatchers.IO)
    private val jsonPrinter = Json { prettyPrint = true }
    private val scriptCache = ScriptCacheRepository()

    override fun run()
    {
        runBlocking {
            printProgress("Loading")
            val runConfiguration = RunConfiguration(
                target = scriptFile.loadDescriptor(),
                propertyFiles = propertyFiles,
                properties = properties.toMap(),
                parentProperties = emptyMap(),
            )
            val cache = if (noCache) null else scriptCache.getCache(runConfiguration)
                ?.takeIf {
                    it.children.all { it.configuration.target.isValid() }
                }

            val evaluationResult = cache ?: run {
                printProgress("Compiling ${scriptFile.name}")
                runCatching { evaluateOrThrow(runConfiguration) }
                    .onFailure { handleError(scriptFile, it); return@runBlocking  }
                    .getOrThrow()
                    .toScriptEvaluationResult()
                    .also { scriptCache.putCache(runConfiguration, it) }
            }

            val labels = EndpointLabels(evaluationResult)
            val allEndpoints = evaluationResult.allRequestBuilders
                .flatMap { it.second }
            if (list || (endpointArg == null && evaluationResult.allRequestBuilders.size > 1)) {
                printTitle("Available endpoints:")
                labels.names.forEach {
                    val endpoint = allEndpoints[labels.names.indexOf(it)]
                    printEndpointLabel(it, endpoint)
                }
            } else {
                labels.names.find { (endpointArg == null && evaluationResult.allRequestBuilders.size == 1) || it == endpointArg }
                    ?.let { allEndpoints[labels.names.indexOf(it)] }
                    .let { endpoint ->
                        if (endpoint == null) {
                            printBadEndpointArgument(endpointArg ?: "<empty>")
                        } else {
                            val definingScript = evaluationResult.allRequestBuilders
                                .single { it.second.contains(endpoint) }
                                .first
                            val recompiledEndpoint = if (cache != null && endpoint.hasDynamics) {
                                printProgress("Recompiling ${definingScript.configuration.target.file.name} for handlers")
                                val index = definingScript.requestBuilders.indexOf(endpoint)
                                val handlerCompile = runCatching { evaluateOrThrow(definingScript.configuration) }
                                    .onFailure { handleError(scriptFile, it); return@runBlocking  }
                                    .getOrThrow()
                                    .toScriptEvaluationResult()
                                handlerCompile.requestBuilders[index]
                            } else null
                            requestScope.async {
                                runRequest(recompiledEndpoint ?: endpoint, definingScript)
                            }.await()
                        }
                    }
            }
        }
    }

    private suspend fun runRequest(request: RequestBuilder, definingScript: ScriptEvaluationResult)
    {
        when (request) {
            is EndpointBuilder -> runRequest(request, definingScript.configuration.target)
            is WebsocketBuilder -> runRequest(request, definingScript)
        }
    }

    private suspend fun runRequest(socket: WebsocketBuilder, definingScript: ScriptEvaluationResult)
    {
        val url = socket.url
        if (url == null) {
            handleError(scriptFile, ConfigurationError(
                context = definingScript.configuration.target.file,
                message = "No URL specified for endpoint '${endpointArg}'"
            ))
            return
        }
        val name = socket.name ?: scriptFile.nameWithoutExtension
        printEndpointTitle(name)
        printRequestUrl("websocket", url)

        runCatching {
            measureTimedValue {
                client.webSocket(
                    urlString = url,
                    request = {
                        val headerData = socket.headers
                            .groupBy { it.first }
                            .mapValues { it.value.map { it.second } }
                            .map { it.key to it.value }
                            .toTypedArray()
                        headersOf(*headerData)
                    }
                ) {
                    printSocketOpen()
                    socket.onConnect?.invoke(
                        OnConnectContext(
                            onSend = {
                                printSocketSendFrame(it)
                                send(it)
                            },
                        )
                    )
                    incoming.consumeAsFlow().collect {
                        val message = it.data.decodeToString()
                        printSocketReceiveFrame(message)
                        socket.onMessage?.invoke(
                            OnMessageContext(
                                message = message,
                                onSend = {
                                    printSocketSendFrame(it)
                                    send(it)
                                },
                            )
                        )
                    }
                }
            }
        }.onSuccess { timedValue ->
            printSocketClosed(timedValue.duration)
        }.onFailure { error ->
            handleError(scriptFile, error)
        }
    }

    private suspend fun runRequest(endpoint: EndpointBuilder, definingScript: FileDescriptor)
    {
        if (endpoint.url == null) {
            handleError(scriptFile, ConfigurationError(
                context = definingScript.file,
                message = "No URL specified for endpoint '${endpointArg}'"
            ))
            return
        }
        val name = endpoint.name ?: scriptFile.nameWithoutExtension
        printEndpointTitle(name)
        printRequestUrl(endpoint.method.key, endpoint.url!!)
        runCatching {
            measureTimedValue {
                client.request {
                    method = HttpMethod(endpoint.method.key)
                    url(urlString = endpoint.url!!)
                    if (endpoint.body != null) {
                        setBody(endpoint.body)
                    }
                    val headerData = endpoint.headers
                        .groupBy { it.first }
                        .mapValues { it.value.map { it.second } }
                        .map { it.key to it.value }
                        .toTypedArray()
                    headerData.forEach { (key, values) ->
                        values.forEach {
                            printRequestMeta(key, it)
                        }
                    }
                    headersOf(*headerData)
                }
            }
        }.onSuccess { timedValue ->
            printProgress("Response:")
            val rawOut = timedValue.value.bodyAsText()
            val formatted = when(timedValue.value.contentType()?.withoutParameters()) {
                ContentType.Application.Json -> runCatching {
                    jsonPrinter.parseToJsonElement(rawOut)
                        .let { jsonPrinter.encodeToString(it) }
                }.getOrNull()
                else -> null
            }
            rawOutput(formatted ?: rawOut)
            timedValue.value.headers.forEach { key, values ->
                values.forEach {
                    printRequestMeta(key, it)
                }
            }
            printStatus(
                code = timedValue.value.status.value,
                description = timedValue.value.status.description,
                duration = timedValue.duration,
            )
        }.onFailure { error ->
            handleError(scriptFile, error)
        }
    }

    private fun handleError(file: File, exception: Throwable) {
        when (exception) {
            is ScriptEvaluationException -> printEvaluationError(file, exception)
            is UnresolvedAddressException -> printUnresolvedHost(exception)
            is ConfigurationError -> printConfigurationError(exception)
            else -> printUnhandledError(file, exception)
        }
    }
}
