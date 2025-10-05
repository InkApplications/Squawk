package squawk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import kotlinx.coroutines.runBlocking
import kotlin.script.experimental.api.isError
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
                println(endpoint.name)
                println("-".repeat(endpoint.name.length))
                println("GET: ${endpoint.url}")
                val result = client.get {
                    accept(ContentType.Application.Json)
                    url(endpoint.url)
                }
                println(result.bodyAsText())
            }
    }
}

