package org.jetbrains.research.code.submissions.clustering.cli

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import org.jetbrains.research.code.submissions.clustering.load.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import org.jetbrains.research.code.submissions.clustering.util.toDataFrame
import org.jetbrains.research.code.submissions.clustering.util.toProto
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

object LoadRunner : ApplicationStarter {
    private val logger = Logger.getInstance(this::class.java)
    private var outputFilename: String? = null
    private var csvFilename: String? = null
    private lateinit var inputFilename: String

    override fun getCommandName(): String = "load"

    @Suppress("TooGenericExceptionCaught")
    override fun main(args: MutableList<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(::TransformationsRunnerArgs).run {
                inputFilename = Paths.get(input).toString()
                outputFilename = output?.let { Paths.get(it).toString() }
                csvFilename = outputCSV?.let { Paths.get(it).toString() }
            }

            val df = DataFrame.readCSV(inputFilename)
            val unifier = PyUnifier()
            val submissionsGraph = df.loadGraph(unifier)

            outputFilename?.let {
                val outputFile = File(it)
                submissionsGraph.toProto().writeTo(outputFile.outputStream())
            }
            csvFilename?.let {
                submissionsGraph.toDataFrame().writeCSV(it)
            }
        } catch (ex: Throwable) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }

    data class TransformationsRunnerArgs(private val parser: ArgParser) {
        val input by parser.storing(
            "-i", "--input_file",
            help = "Input .csv file with code submissions"
        )
        val output by parser.storing(
            "-o", "--output_file",
            help = "Output binary file to store submissions graph",
        ).default(null)
        val outputCSV by parser.storing(
            "-c", "--csv_file",
            help = "Output .csv file to store unified code submissions"
        ).default(null)
    }
}
