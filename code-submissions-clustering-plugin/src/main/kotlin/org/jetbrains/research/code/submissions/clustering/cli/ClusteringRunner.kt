package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.cli.models.AbstractGraphBuilderArgs
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import org.jetbrains.research.code.submissions.clustering.util.toSubmissionsGraph
import java.nio.file.Paths

object ClusteringRunner : AbstractGraphBuilder() {
    private lateinit var inputFilename: String
    private lateinit var distLimit: String

    @Deprecated("Specify it as `id` for extension definition in a plugin descriptor")
    override val commandName: String
        get() = "cluster"

    override fun main(args: List<String>) {
        val mutableArgs = args.toMutableList()
        startRunner(mutableArgs) {
            parseArgs(mutableArgs, ::GraphClusteringRunnerArgs).run {
                inputFilename = Paths.get(inputFile).toString()
                distLimit = distanceLimit
            }
            val submissionsGraph = binInput?.toSubmissionsGraph()
                ?: DataFrame.readCSV(inputFilename).let {
                    val context = this.buildGraphContext()
                    it.loadGraph(context)
                }
            val clusterer = SubmissionsGraphHAC(distLimit.toDouble())
            submissionsGraph.cluster(clusterer)
            submissionsGraph.writeOutputData()
        }
    }

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
