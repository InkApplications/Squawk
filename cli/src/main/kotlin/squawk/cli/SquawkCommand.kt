package squawk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.accept
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import kotlinx.coroutines.runBlocking
import squawk.host.ScriptEvaluationException
import squawk.host.evaluateOrThrow
import squawk.script.EndpointBuilder
import java.io.File
import java.nio.channels.UnresolvedAddressException
import kotlin.time.measureTimedValue

class SquawkCommand: CliktCommand()
{
    private val scriptFile by argument(name = "config")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)
    private val client = HttpClient(CIO)

    override fun run()
    {
        runBlocking {
            runCatching { evaluateOrThrow(scriptFile) }
                .onFailure { handleError(scriptFile, it) }
                .onSuccess { it.endpoints.forEach { runRequest(it) } }
        }
    }

    private suspend fun runRequest(endpoint: EndpointBuilder)
    {
        with(DisplayOutput) {
            val name = endpoint.name ?: scriptFile.nameWithoutExtension
            endpointTitle(name)
            requestUrl(endpoint.method.key, endpoint.url)
            runCatching {
                measureTimedValue {
                    client.request {
                        method = HttpMethod(endpoint.method.key)
                        accept(ContentType.Application.Json)
                        url(endpoint.url)
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
}
