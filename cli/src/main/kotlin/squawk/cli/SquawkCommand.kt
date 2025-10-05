package squawk.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file

class SquawkCommand: CliktCommand()
{
    val scriptFile by argument(name = "config").file(mustExist = true, canBeDir = false, mustBeReadable = true)
    override fun run()
    {
        SquawkScript.evalFile(scriptFile)
    }
}

