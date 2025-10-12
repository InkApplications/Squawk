package squawk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import squawk.cli.formatting.printEndpointLabel
import squawk.cli.formatting.printEndpointTitle
import squawk.cli.formatting.printProgress
import squawk.cli.formatting.rawOutput
import squawk.cli.formatting.printRequestUrl
import squawk.cli.formatting.printEvaluationError
import squawk.cli.formatting.printRequestMeta
import squawk.cli.formatting.printStatus
import squawk.cli.formatting.printTitle
import squawk.cli.formatting.printUnhandledError
import squawk.cli.formatting.printUnresolvedHost
import squawk.host.ScriptEvaluationException
import squawk.host.evaluateOrThrow
import squawk.script.EndpointBuilder
import squawk.script.SquawkScript
import java.io.File
import java.lang.IllegalArgumentException
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

    private val client = HttpClient(CIO)
    private val requestScope = CoroutineScope(Dispatchers.IO)

    override fun run()
    {
        runBlocking {
            printProgress("Loading ${scriptFile.name}")
            runCatching { evaluateOrThrow(scriptFile) }
                .onFailure { handleError(scriptFile, it) }
                .onSuccess { script ->
                    val names = script.scriptEndpoints
                        .flatMap { (endpointScript, endpoints) -> endpoints.map { createCanonicalName(endpointScript, it) } }
                    if (list || (endpointArg == null && script.endpoints.size > 1)) {
                        printTitle("Available endpoints:")
                        names.forEach {
                            val endpoint = script.endpoints[names.indexOf(it)]
                            printEndpointLabel(it, endpoint)
                        }
                    } else {
                        names
                            .find { (endpointArg == null && script.endpoints.size == 1) || it == endpointArg }
                            ?.let { script.endpoints[names.indexOf(it)] }
                            .let { endpoint ->
                                if (endpoint == null) {
                                    handleError(scriptFile, IllegalArgumentException("Unknown endpoint: $endpointArg"))
                                } else {
                                    requestScope.async {
                                        runRequest(endpoint)
                                    }.await()
                                }
                            }
                    }
                }
        }
    }

    private suspend fun runRequest(endpoint: EndpointBuilder)
    {
        if (endpoint.url == null) {
            handleError(scriptFile, IllegalArgumentException("Endpoint missing URL"))
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
            rawOutput(timedValue.value.bodyAsText())
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
            else -> printUnhandledError(file, exception)
        }
    }

    private fun createCanonicalName(
        script: SquawkScript,
        endpoint: EndpointBuilder
    ): String {
        val prefix = script.namespace?.let { "$it:" }.orEmpty()
        if (endpoint.name != null) {
            return endpoint.name!!.lowercase().replace(' ', '-').let { "$prefix$it" }
        }
        val matchingMethods = script.endpoints.filter { it.name == null && it.method == endpoint.method }
        return when {
            matchingMethods.size == 1 || matchingMethods.indexOf(endpoint) == 0 -> endpoint.method.key.lowercase()
            else -> "${endpoint.method.key.lowercase()}-${matchingMethods.indexOf(endpoint) + 1}"
        }.let { "$prefix$it" }
    }
}
