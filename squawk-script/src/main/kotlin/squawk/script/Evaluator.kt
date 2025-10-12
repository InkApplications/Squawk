package squawk.script

import java.io.File

interface Evaluator
{
    fun evaluateFile(file: File): SquawkScript
}
