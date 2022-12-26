package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.cli.models.AbstractGraphBuilderArgs
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import java.nio.file.Paths

@Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
object LoadRunner : AbstractGraphBuilder() {
    private lateinit var inputFilename: String

    @Deprecated("Specify it as `id` for extension definition in a plugin descriptor")
    override val commandName: String
        get() = "load"

    override fun main(args: List<String>) {
        val mutableArgs = args.toMutableList()
        startRunner(mutableArgs) {
            parseArgs(mutableArgs, ::LoadRunnerArgs).run {
                inputFilename = Paths.get(inputFile).toString()
            }
            val df = DataFrame.readCSV(inputFilename)
            val context = buildGraphContext()
            val submissionsGraph = df.loadGraph(context)
            submissionsGraph.writeOutputData()
        }
    }

    data class LoadRunnerArgs(private val parser: ArgParser) : AbstractGraphBuilderArgs(parser) {
        val inputFile by parser.storing(
            "-i", "--inputFile",
            help = "Input .csv file with code submissions"
        )
    }
}
