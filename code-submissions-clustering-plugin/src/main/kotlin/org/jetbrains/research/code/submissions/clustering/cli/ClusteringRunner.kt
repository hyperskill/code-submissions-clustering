package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.cli.ClusteringRunner.writeOutputData
import org.jetbrains.research.code.submissions.clustering.cli.models.AbstractGraphBuilderArgs
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import org.jetbrains.research.code.submissions.clustering.util.toSubmissionsGraph
import java.nio.file.Paths

@Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
object ClusteringRunner : AbstractGraphBuilder() {
    lateinit var inputFilename: String
    lateinit var distLimit: String

    data class GraphClusteringRunnerArgs(private val parser: ArgParser) : AbstractGraphBuilderArgs(parser) {
        val inputFile by parser.storing(
            "-i", "--inputFile",
            help = "Input .csv file with code submissions"
        )
        val distanceLimit by parser.storing(
            "--distanceLimit",
            help = "Max distance between two vertices inside one cluster"
        )
    }
}

fun main(args: Array<String>) {
    val mutableArgs = args.toMutableList()
    ClusteringRunner.startRunner(mutableArgs) {
        ClusteringRunner.parseArgs(mutableArgs, ClusteringRunner::GraphClusteringRunnerArgs).run {
            ClusteringRunner.inputFilename = Paths.get(inputFile).toString()
            ClusteringRunner.distLimit = distanceLimit
        }
        val context = ClusteringRunner.buildGraphContext()
        val submissionsGraph = ClusteringRunner.binInput?.toSubmissionsGraph()
            ?: DataFrame.readCSV(ClusteringRunner.inputFilename).loadGraph(context)
        val clusterer = SubmissionsGraphHAC(ClusteringRunner.distLimit.toDouble())
        submissionsGraph.cluster(clusterer)
        submissionsGraph.writeOutputData()
    }
}
