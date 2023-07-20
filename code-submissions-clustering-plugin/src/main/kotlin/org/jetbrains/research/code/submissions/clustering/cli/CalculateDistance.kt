package org.jetbrains.research.code.submissions.clustering.cli

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import org.jetbrains.research.code.submissions.clustering.load.distance.calculateDistances
import org.jetbrains.research.code.submissions.clustering.util.toSubmissionsGraph
import java.io.File

@Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
class CalculateDistance : AbstractGraphBuilder(
    name = "calculate-dist",
    help = "Calculate distances in serialized submissions graph from .bin file"
) {
    private val inputFile: File by argument(
        "inputFile",
        help = "Input .bin file with serialized graph",
    ).file(mustExist = true)

    override fun run() {
        startRunner {
            val context = buildGraphContext()
            val submissionsGraph = inputFile.toSubmissionsGraph().calculateDistances(context)
            submissionsGraph.writeOutputData()
        }
    }
}
