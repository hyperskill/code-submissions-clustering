package org.jetbrains.research.code.submissions.clustering.cli

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import java.io.File

@Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
class Load : AbstractGraphBuilder(
    name = "load",
    help = "Load submissions graph from .csv file"
) {
    private val inputFile: File by argument(
        "inputFile",
        help = "Input .csv file with code submissions",
    ).file(mustExist = true)

    override fun run() {
        startRunner {
            val df = DataFrame.readCSV(inputFile)
            val context = buildGraphContext()
            val submissionsGraph = df.loadGraph(context)
            submissionsGraph.writeOutputData()
        }
    }
}
