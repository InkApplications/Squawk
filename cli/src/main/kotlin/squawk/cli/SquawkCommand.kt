package squawk.cli

import com.github.ajalt.clikt.core.CliktCommand

class SquawkCommand: CliktCommand()
{
    override fun run()
    {
        println("SQUAWK!")
    }
}
