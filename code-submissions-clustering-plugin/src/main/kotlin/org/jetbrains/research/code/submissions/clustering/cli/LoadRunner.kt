package org.jetbrains.research.code.submissions.clustering.cli

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.util.*
import java.nio.file.Paths
import kotlin.system.exitProcess

object LoadRunner : ApplicationStarter {
    private val logger = Logger.getInstance(this::class.java)
    private var toBinary: Boolean = false
    private var toCSV: Boolean = false
    private lateinit var inputFilename: String
    private lateinit var outputPath: String

    override fun getCommandName(): String = "load"

    @Suppress("TooGenericExceptionCaught")
    override fun main(args: MutableList<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(::TransformationsRunnerArgs).run {
                inputFilename = Paths.get(input).toString()
                outputPath = Paths.get(output).toString()
                toBinary = serialize
                toCSV = saveCSV
            }

            val df = DataFrame.readCSV(inputFilename)
            val language = Language.PYTHON
            val submissionsGraph = df.loadGraph(language)

            createFolder(outputPath)
            submissionsGraph.writeToString(outputPath)
            if (toBinary) {
                submissionsGraph.writeToBinary(outputPath)
            }
            if (toCSV) {
                submissionsGraph.writeToCsv(outputPath)
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
            "-o", "--output_path",
            help = "Directory to store all output files",
        )
        val serialize by parser.flagging(
            "--serialize",
            help = "Save submissions graph to binary file"
        )
        val saveCSV by parser.flagging(
            "--saveCSV",
            help = "Save unified solutions to .csv file"
        )
    }
}
