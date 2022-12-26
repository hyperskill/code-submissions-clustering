package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.code.submissions.clustering.cli.models.AbstractGraphBuilderArgs
import org.jetbrains.research.code.submissions.clustering.load.distance.calculateDistances
import org.jetbrains.research.code.submissions.clustering.util.toSubmissionsGraph
import java.io.File
import java.nio.file.Paths

@Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
object DistanceCalculationRunner : AbstractGraphBuilder() {
    private lateinit var inputFilename: String

    @Deprecated("Specify it as `id` for extension definition in a plugin descriptor")
    override val commandName: String
        get() = "calculate-dist"

    override fun main(args: List<String>) {
        val mutableArgs = args.toMutableList()
        startRunner(mutableArgs) {
            parseArgs(mutableArgs, ::DistanceCalculationRunnerArgs).run {
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
