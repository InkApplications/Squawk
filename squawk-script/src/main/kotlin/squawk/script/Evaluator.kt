package squawk.script

import java.io.File

interface Evaluator
{
    fun evaluateFile(parent: SquawkScript?, file: File): SquawkScript
}
