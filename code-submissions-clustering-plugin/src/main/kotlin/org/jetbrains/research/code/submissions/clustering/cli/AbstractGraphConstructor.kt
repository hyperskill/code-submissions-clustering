package org.jetbrains.research.code.submissions.clustering.cli

import com.intellij.openapi.application.ApplicationStarter
import com.xenomachina.argparser.ArgParser
import org.jetbrains.research.code.submissions.clustering.load.context.builder.gumtree.GumTreeGraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.util.createFolder
import org.jetbrains.research.code.submissions.clustering.util.writeToBinary
import org.jetbrains.research.code.submissions.clustering.util.writeToCsv
import org.jetbrains.research.code.submissions.clustering.util.writeToString
import java.nio.file.Paths
import java.util.logging.Logger

abstract class AbstractGraphConstructor : ApplicationStarter {
    protected val logger: Logger = Logger.getLogger(javaClass.name)
    private var toBinary: Boolean = false
    private var toCSV: Boolean = false
    private lateinit var language: Language
    private lateinit var outputPath: String

    protected fun <T : AbstractGraphConstructionArgs> parseArgs(
        args: MutableList<String>,
        argsClassConstructor: (ArgParser) -> T,
        block: T.() -> Unit
    ) {
        val parser = ArgParser(args.drop(1).toTypedArray())
        parser.parseInto(argsClassConstructor).run {
            language = Language.valueOf(Paths.get(lang).toString())
            outputPath = Paths.get(output).toString()
            toBinary = serialize
            toCSV = saveCSV
            block()
        }
    }

    protected fun buildGraphContext() = GumTreeGraphContextBuilder.getContext(language)

    @Suppress("TooGenericExceptionCaught")
    protected fun SubmissionsGraph.writeOutputData() {
        createFolder(outputPath)
        try {
            writeToString(outputPath)
        } catch (ex: Throwable) {
            logger.severe { "Writing to txt failed: $ex" }
        }
        if (toBinary) {
            try {
                writeToBinary(outputPath)
            } catch (ex: Throwable) {
                logger.severe { "Writing to bin failed: $ex" }
            }
        }
        if (toCSV) {
            try {
                writeToCsv(outputPath)
            } catch (ex: Throwable) {
                logger.severe { "Writing to csv failed: $ex" }
            }
        }
    }
}

open class AbstractGraphConstructionArgs(parser: ArgParser) {
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
}
