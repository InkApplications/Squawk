package squawk.cli

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle

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
}
