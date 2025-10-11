package squawk.cli.formatting

import com.github.ajalt.mordant.rendering.TextStyle

fun printTitle(text: String)
{
    val style = TextStyle(
        bold = true,
    )
    println(style(text))
}

fun printProgress(message: String)
{
    val style = TextStyle(
        bold = true
    )
    println(style("> $message"))
}
