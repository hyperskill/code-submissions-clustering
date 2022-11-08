package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import org.jetbrains.research.code.submissions.clustering.util.toSubmissionsGraph
import java.nio.file.Paths
import kotlin.system.exitProcess

object ClusteringRunner : AbstractGraphBuilder() {
    private lateinit var inputFilename: String
    private lateinit var distLimit: String

    override fun getCommandName() = "cluster"

    @Suppress("TooGenericExceptionCaught")
    override fun main(args: MutableList<String>) {
        try {
            parseArgs(args, ::GraphClusteringRunnerArgs).run {
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
        } catch (ex: Throwable) {
            logger.severe { ex.stackTraceToString() }
        } finally {
            exitProcess(0)
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
