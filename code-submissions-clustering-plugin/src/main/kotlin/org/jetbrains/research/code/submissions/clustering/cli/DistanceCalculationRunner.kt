package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.code.submissions.clustering.cli.models.AbstractGraphBuilderArgs
import org.jetbrains.research.code.submissions.clustering.load.distance.calculateDistances
import org.jetbrains.research.code.submissions.clustering.util.toSubmissionsGraph
import java.io.File
import java.nio.file.Paths

object DistanceCalculationRunner : AbstractGraphBuilder() {
    private lateinit var inputFilename: String

    override fun getCommandName(): String = "calculate-dist"

    override fun main(args: MutableList<String>) {
        startRunner(args) {
            parseArgs(args, ::DistanceCalculationRunnerArgs).run {
                inputFilename = Paths.get(inputFile).toString()
            }
            val file = File(inputFilename)
            val context = buildGraphContext()
            val submissionsGraph = file.toSubmissionsGraph().calculateDistances(context)
            submissionsGraph.writeOutputData()
        }
    }

    data class DistanceCalculationRunnerArgs(private val parser: ArgParser) : AbstractGraphBuilderArgs(parser) {
        val inputFile by parser.storing(
            "-i", "--inputFile",
            help = "Input .bin file with serialized graph"
        )
    }
}
