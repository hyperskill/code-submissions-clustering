package org.jetbrains.research.code.submissions.clustering.cli

import com.intellij.openapi.application.ApplicationStarter
import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.GumTreeGraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.util.*
import java.nio.file.Paths
import java.util.logging.Logger

abstract class AbstractGraphBuilder : ApplicationStarter {
    protected val logger: Logger = Logger.getLogger(javaClass.name)
    private var toBinary: Boolean = false
    private var toCSV: Boolean = false
    private var toPNG: Boolean = false
    private lateinit var language: Language
    private lateinit var outputPath: String

    protected fun <T : AbstractGraphBuilderArgs> parseArgs(
        args: MutableList<String>,
        argsClassConstructor: (ArgParser) -> T
    ): T {
        val parser = ArgParser(args.drop(1).toTypedArray())
        return parser.parseInto(argsClassConstructor).apply {
            language = Language.valueOf(Paths.get(lang).toString())
            outputPath = Paths.get(output).toString()
            toBinary = serialize
            toCSV = saveCSV
            toPNG = visualize
        }
    }

    protected fun buildGraphContext() = GumTreeGraphContextBuilder.getContext(language)

    protected fun SubmissionsGraph.writeOutputData() {
        createFolder(outputPath)
        tryToWrite(::writeToString)
        if (toBinary) {
            tryToWrite(::writeToBinary)
        }
        if (toCSV) {
            tryToWrite(::writeToCsv)
        }
        if (toPNG) {
            tryToWrite(::writeToPng)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun tryToWrite(write: (String) -> Unit) {
        try {
            write(outputPath)
        } catch (ex: Throwable) {
            logger.severe { "Writing failed: $ex" }
        }
    }
}

open class AbstractGraphBuilderArgs(parser: ArgParser) {
    val lang by parser.storing(
        "-l", "--language",
        help = "Programming language of code submissions"
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
    val visualize by parser.flagging(
        "--visualize",
        help = "Save submissions graph visualization to .png file"
    )
}
