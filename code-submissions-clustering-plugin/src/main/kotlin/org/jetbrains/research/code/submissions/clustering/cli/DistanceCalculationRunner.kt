package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.code.submissions.clustering.cli.DistanceCalculationRunner.writeOutputData
import org.jetbrains.research.code.submissions.clustering.cli.models.AbstractGraphBuilderArgs
import org.jetbrains.research.code.submissions.clustering.load.distance.calculateDistances
import org.jetbrains.research.code.submissions.clustering.util.toSubmissionsGraph
import java.io.File
import java.nio.file.Paths

@Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
object DistanceCalculationRunner : AbstractGraphBuilder() {
    lateinit var inputFilename: String

    data class DistanceCalculationRunnerArgs(private val parser: ArgParser) : AbstractGraphBuilderArgs(parser) {
        val inputFile by parser.storing(
            "-i", "--inputFile",
            help = "Input .bin file with serialized graph"
        )
    }
}

fun main(args: Array<String>) {
    val mutableArgs = args.toMutableList()
    DistanceCalculationRunner.startRunner(mutableArgs) {
        DistanceCalculationRunner.parseArgs(mutableArgs, DistanceCalculationRunner::DistanceCalculationRunnerArgs).run {
            DistanceCalculationRunner.inputFilename = Paths.get(inputFile).toString()
        }
        val file = File(DistanceCalculationRunner.inputFilename)
        val context = DistanceCalculationRunner.buildGraphContext()
        val submissionsGraph = file.toSubmissionsGraph().calculateDistances(context)
        submissionsGraph.writeOutputData()
    }
}
