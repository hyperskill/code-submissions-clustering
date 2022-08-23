package org.jetbrains.research.code.submissions.clustering.cli

import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import java.nio.file.Paths
import kotlin.system.exitProcess

object LoadRunner : AbstractGraphConstructor() {
    private lateinit var inputFilename: String

    override fun getCommandName(): String = "load"

    @Suppress("TooGenericExceptionCaught")
    override fun main(args: MutableList<String>) {
        try {
            parseArgs(args, ::LoadRunnerArgs) {
                inputFilename = Paths.get(input).toString()
            }
            val df = DataFrame.readCSV(inputFilename)
            val context = buildGraphContext()
            val submissionsGraph = df.loadGraph(context)
            submissionsGraph.writeOutputData()
        } catch (ex: Throwable) {
            logger.severe { ex.toString() }
        } finally {
            exitProcess(0)
        }
    }

    data class LoadRunnerArgs(private val parser: ArgParser) : AbstractGraphConstructionArgs(parser) {
        val input by parser.storing(
            "-i", "--input_file",
            help = "Input .csv file with code submissions"
        )
    }
}
