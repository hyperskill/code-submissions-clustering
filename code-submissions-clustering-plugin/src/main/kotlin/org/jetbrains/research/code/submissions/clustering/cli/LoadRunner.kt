package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.cli.LoadRunner.writeOutputData
import org.jetbrains.research.code.submissions.clustering.cli.models.AbstractGraphBuilderArgs
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import java.nio.file.Paths

@Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
object LoadRunner : AbstractGraphBuilder() {
    lateinit var inputFilename: String

    data class LoadRunnerArgs(private val parser: ArgParser) : AbstractGraphBuilderArgs(parser) {
        val inputFile by parser.storing(
            "-i", "--inputFile",
            help = "Input .csv file with code submissions"
        )
    }
}

fun main(args: Array<String>) {
    val mutableArgs = args.toMutableList()
    LoadRunner.startRunner(mutableArgs) {
        LoadRunner.parseArgs(mutableArgs, LoadRunner::LoadRunnerArgs).run {
            LoadRunner.inputFilename = Paths.get(inputFile).toString()
        }
        val df = DataFrame.readCSV(LoadRunner.inputFilename)
        val context = LoadRunner.buildGraphContext()
        val submissionsGraph = df.loadGraph(context)
        submissionsGraph.writeOutputData()
    }
}
