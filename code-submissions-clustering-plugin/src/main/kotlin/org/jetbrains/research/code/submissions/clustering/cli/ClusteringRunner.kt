package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.load.clustering.hac.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import org.jetbrains.research.code.submissions.clustering.util.toSubmissionsGraph
import java.nio.file.Paths
import kotlin.system.exitProcess

object ClusteringRunner : AbstractGraphBuilder() {
    private lateinit var inputFilename: String
    private lateinit var dl: String

    override fun getCommandName() = "cluster"

    @Suppress(
        "TooGenericExceptionCaught",
        "MagicNumber",
        "PrintStackTrace",
        "MAGIC_NUMBER"
    )
    override fun main(args: MutableList<String>) {
        try {
            parseArgs(args, ::GraphClusteringRunnerArgs).run {
                inputFilename = Paths.get(input).toString()
                dl = distLimit
            }
            val submissionsGraph = binaryDir?.toSubmissionsGraph()
                ?: DataFrame.readCSV(inputFilename).let {
                    val context = this.buildGraphContext()
                    it.loadGraph(context)
                }
            val clusterer = SubmissionsGraphHAC(dl.toDouble())
            submissionsGraph.cluster(clusterer)
            submissionsGraph.writeOutputData()
        } catch (ex: Throwable) {
            logger.severe { ex.stackTraceToString() }
        } finally {
            exitProcess(0)
        }
    }

    data class GraphClusteringRunnerArgs(private val parser: ArgParser) : AbstractGraphBuilderArgs(parser) {
        val input by parser.storing(
            "-i", "--input_file",
            help = "Input .csv file with code submissions"
        )
        val distLimit by parser.storing(
            "--distLimit",
            help = "Max distance between two vertices inside one cluster"
        )
    }
}
