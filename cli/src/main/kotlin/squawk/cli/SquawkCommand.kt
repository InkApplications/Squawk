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
import java.io.File

class SquawkCommand: CliktCommand()
{
    val scriptFile by argument(name = "config")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)
    override fun run() = runBlocking<Unit> {
        val client = HttpClient(CIO)
        runCatching { evaluateOrThrow(scriptFile) }
            .onFailure { handleError(scriptFile, it) }
            .onSuccess {
                it.endpoints
                    .forEach { endpoint ->
                        val name = endpoint.name ?: scriptFile.nameWithoutExtension
                        DisplayOutput.endpointTitle(name)
                        DisplayOutput.requestUrl(endpoint.method.key, endpoint.url)
                        val result = client.request {
                            method = HttpMethod(endpoint.method.key)
                            accept(ContentType.Application.Json)
                            url(endpoint.url)
                        }
                        DisplayOutput.rawOutput(result.bodyAsText())
                    }
            }
    }
}

fun handleError(file: File, exception: Throwable) {
    when (exception) {
        is ScriptEvaluationException -> DisplayOutput.scriptEvaluationError(file, exception)
        else -> DisplayOutput.unhandledError(file, exception)
    }
}
