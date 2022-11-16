package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.cli.models.AbstractGraphBuilderArgs
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import java.nio.file.Paths

object LoadRunner : AbstractGraphBuilder() {
    private lateinit var inputFilename: String

    override fun getCommandName(): String = "load"

    override fun main(args: MutableList<String>) {
        startRunner(args) {
            parseArgs(args, ::LoadRunnerArgs).run {
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
