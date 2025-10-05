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
import kotlin.script.experimental.api.valueOrThrow

class SquawkCommand: CliktCommand()
{
    val scriptFile by argument(name = "config").file(mustExist = true, canBeDir = false, mustBeReadable = true)
    override fun run() = runBlocking {
        val client = HttpClient(CIO)
        val result = SquawkScript.evalFile(scriptFile)
        result.valueOrThrow()
            .returnValue
            .scriptInstance
            .let { it as SquawkScript }
            .endpoints
            .forEach { endpoint ->
                val name = endpoint.name ?: scriptFile.nameWithoutExtension
                println(name)
                println("-".repeat(name.length))
                println("${endpoint.method.key}: ${endpoint.url}")
                val result = client.request {
                    method = HttpMethod(endpoint.method.key)
                    accept(ContentType.Application.Json)
                    url(endpoint.url)
                }
                println(result.bodyAsText())
            }
    }
}

