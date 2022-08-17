package org.jetbrains.research.code.submissions.clustering.cli

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.GumTreeGraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.util.*
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

object DistanceCalculationRunner : ApplicationStarter {
    private val logger = Logger.getInstance(this::class.java)
    private var toBinary: Boolean = false
    private lateinit var inputFilename: String
    private lateinit var outputPath: String

    override fun getCommandName(): String = "calculate-dist"

    @Suppress("TooGenericExceptionCaught")
    override fun main(args: MutableList<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(::TransformationsRunnerArgs).run {
                inputFilename = Paths.get(input).toString()
                outputPath = Paths.get(output).toString()
                toBinary = serialize
            }

            val file = File(inputFilename)
            val language = Language.PYTHON
            val context = GumTreeGraphContextBuilder.getContext(language)
            val submissionsGraph = file.toSubmissionsGraph().calculateDistances(context)

            createFolder(outputPath)
            submissionsGraph.writeToString(outputPath)
            if (toBinary) {
                submissionsGraph.writeToBinary(outputPath)
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
            help = "Input .bin file with serialized graph"
        )
        val output by parser.storing(
            "-o", "--output_path",
            help = "Directory to store all output files",
        )
        val serialize by parser.flagging(
            "--serialize",
            help = "Save resulting submissions graph to binary file"
        )
    }
}
