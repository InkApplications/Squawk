package squawk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.types.file
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.accept
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import squawk.host.ScriptEvaluationException
import squawk.host.evaluateOrThrow
import squawk.script.EndpointBuilder
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.channels.UnresolvedAddressException
import kotlin.time.measureTimedValue

class SquawkCommand: CliktCommand()
{
    private val scriptFile by argument(name = "config")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)
    private val endpoint by argument().optional()

    private val client = HttpClient(CIO)
    private val requestScope = CoroutineScope(Dispatchers.IO)

    override fun run()
    {
        runBlocking {
            runCatching { evaluateOrThrow(scriptFile) }
                .onFailure { handleError(scriptFile, it) }
                .onSuccess { script ->
                    if (endpoint == null) {
                        DisplayOutput.title("Available endpoints:")
                        script.endpoints.canonicalNames.forEach {
                            val endpoint = script.endpoints[script.endpoints.canonicalNames.indexOf(it)]
                            DisplayOutput.endpointLabel(it, endpoint)
                        }
                    } else {
                        script.endpoints.canonicalNames
                            .find { it == endpoint }
                            ?.let { script.endpoints[script.endpoints.canonicalNames.indexOf(it)] }
                            .let { endpoint ->
                                if (endpoint == null) {
                                    handleError(scriptFile, IllegalArgumentException("Unknown endpoint: $endpoint"))
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
        with(DisplayOutput) {
            val name = endpoint.name ?: scriptFile.nameWithoutExtension
            endpointTitle(name)
            requestUrl(endpoint.method.key, endpoint.url!!)
            runCatching {
                measureTimedValue {
                    client.request {
                        method = HttpMethod(endpoint.method.key)
                        url(urlString = endpoint.url!!)
                    }
                }
            }.onSuccess { timedValue ->
                statusLine(
                    code = timedValue.value.status.value,
                    description = timedValue.value.status.description,
                    duration = timedValue.duration,
                )
                rawOutput(timedValue.value.bodyAsText())
            }.onFailure { error ->
                handleError(scriptFile, error)
            }
        }
    }

    private fun handleError(file: File, exception: Throwable) {
        when (exception) {
            is ScriptEvaluationException -> DisplayOutput.scriptEvaluationError(file, exception)
            is UnresolvedAddressException -> DisplayOutput.unresolvedHostError(exception)
            else -> DisplayOutput.unhandledError(file, exception)
        }
    }

    private val List<EndpointBuilder>.canonicalNames: List<String> get() {
        return map { builder ->
            if (builder.name != null) {
                return@map builder.name!!.lowercase().replace(' ', '-')
            }
            val matchingMethods = filter { it.name == null && it.method == builder.method }
            when {
                matchingMethods.size == 1 || matchingMethods.indexOf(builder) == 0 -> builder.method.key.lowercase()
                else -> "${builder.method.key.lowercase()}-${matchingMethods.indexOf(builder) + 1}"
            }
        }
    }
}
