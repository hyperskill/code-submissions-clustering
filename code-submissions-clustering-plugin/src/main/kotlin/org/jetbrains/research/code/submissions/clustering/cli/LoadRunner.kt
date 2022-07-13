package org.jetbrains.research.code.submissions.clustering.cli

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.column
import org.jetbrains.kotlinx.dataframe.api.forEach
import org.jetbrains.kotlinx.dataframe.api.getValue
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.Submission
import org.jetbrains.research.code.submissions.clustering.buildGraph
import org.jetbrains.research.code.submissions.clustering.load.Unifier
import java.nio.file.Paths
import kotlin.system.exitProcess

object LoadRunner : ApplicationStarter {
    private lateinit var inputFile: String
    private lateinit var outputDir: String

    private val logger = Logger.getInstance(this::class.java)

    override fun getCommandName(): String = "load"

    class TransformationsRunnerArgs(parser: ArgParser) {
        val input by parser.storing(
            "-i", "--input_file", help = "Input .csv file with code submissions"
        )

        val output by parser.storing(
            "-o", "--output_path", help = "Output directory"
        )
    }

    @Suppress("TooGenericExceptionCaught")
    override fun main(args: MutableList<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(::TransformationsRunnerArgs).run {
                inputFile = Paths.get(input).toString()
                outputDir = Paths.get(output).toString().removeSuffix("/")
            }

            val df = DataFrame.readCSV(inputFile)
            val id by column<Int>()
            val step_id by column<Int>()
            val code by column<String>()
            val submissionsGraph = buildGraph(Unifier()) {
                df.forEach {
                    add(
                        Submission(
                            id = getValue(id),
                            stepId = getValue(step_id),
                            code = getValue(code)
                        )
                    )
                }
            }
            submissionsGraph.print()
        } catch (ex: Throwable) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }
}
