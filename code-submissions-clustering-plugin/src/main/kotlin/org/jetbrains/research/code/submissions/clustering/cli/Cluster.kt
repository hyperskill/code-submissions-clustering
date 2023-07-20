package org.jetbrains.research.code.submissions.clustering.cli

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import org.jetbrains.research.code.submissions.clustering.util.toSubmissionsGraph
import java.io.File

@Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
class Cluster : AbstractGraphBuilder(
    name = "cluster",
    help = "Cluster code submissions from .csv file with set distance limit") {
    private val inputFile: File by argument(
        "inputFile",
        help = "Input .csv file with code submissions",
    ).file(mustExist = true)
    private val distanceLimit: Double by argument(
        "distanceLimit",
        help = "Max distance between two vertices inside one cluster",
    ).double()

    override fun run() {
        startRunner {
            val context = buildGraphContext()
            val submissionsGraph = commonOptions.binaryInput?.toSubmissionsGraph()
                ?: DataFrame.readCSV(inputFile).loadGraph(context)
            val clusterer = SubmissionsGraphHAC(distanceLimit)
            submissionsGraph.cluster(clusterer)
            submissionsGraph.writeOutputData()
        }
    }
}
