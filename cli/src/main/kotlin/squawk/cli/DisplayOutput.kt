package squawk.cli

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import squawk.host.ScriptEvaluationException
import java.io.File

object DisplayOutput
{
    fun endpointTitle(name: String)
    {
        val style = TextStyle(
            bold = true,
        )
        println("> " + style("endpoint: $name"))
    }

    fun requestUrl(method: String, url: String)
    {
        val urlStyle = TextStyle(
            color = TextColors.blue,
            underline = true,
            hyperlink = url,
        )
        val methodStyle = TextStyle()

        println("> ${methodStyle(method)}: ${urlStyle(url)}")
    }

    fun rawOutput(text: String)
    {
        println(text)
    }

    fun statusLine(code: Int, description: String)
    {
        val codeStyle = when (code) {
            in 200..299 -> TextStyle(color = TextColors.brightWhite, bgColor = TextColors.green)
            in 300..399 -> TextStyle(color = TextColors.brightWhite, bgColor = TextColors.cyan)
            in 400..499 -> TextStyle(color = TextColors.brightWhite, bgColor = TextColors.red)
            in 500..599 -> TextStyle(color = TextColors.brightWhite, bgColor = TextColors.yellow)
            else -> TextStyle(color = TextColors.brightWhite, bgColor = TextColors.yellow)
        }
        println("> ${codeStyle(" $code ")} $description")
    }

    fun scriptEvaluationError(
        file: File,
        exception: ScriptEvaluationException,
    ) {
        val errorLabel = TextStyle(
            bold = true,
            color = TextColors.red,
        )
        val locationStyle = TextStyle(
            color = TextColors.yellow,
        )
        println("${errorLabel("Evaluation Error")}: ${exception.message}")
        exception.errors.forEach { error ->
            println()
            val startLine = error.startLine
            val endLine = error.endLine
            val lineCoordinates = when {
                startLine == null -> ""
                endLine == null -> "line ${startLine.line} col ${startLine.column}"
                startLine.line == endLine.line -> "line ${startLine.line} col ${startLine.column}-${endLine.column}"
                else -> "line ${startLine.line} col ${startLine.column} through line ${endLine.line} col ${endLine.column}"
            }
            println(locationStyle("error in '${file.absolutePath}', $lineCoordinates:"))
            println(error.message)
            error.cause?.stackTraceToString()?.run {
                println(this)
            }
        }
    }

    fun unhandledError(
        script: File,
        exception: Throwable,
    ) {
        val errorLabel = TextStyle(
            bold = true,
            color = TextColors.red,
        )
        println("${errorLabel("Unhandled Error")}: ${exception.message}")
        println("while attempting to run script: '${script.absolutePath}'")
        println(exception.stackTraceToString())
    }
}
