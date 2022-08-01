package org.jetbrains.research.code.submissions.clustering.cli

import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.diagnostic.Logger
import com.xenomachina.argparser.ArgParser
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.jetbrains.research.code.submissions.clustering.load.unifiers.PyUnifier
import org.jetbrains.research.code.submissions.clustering.util.loadGraph
import java.nio.file.Paths
import kotlin.system.exitProcess

object LoadRunner : ApplicationStarter {
    private val logger = Logger.getInstance(this::class.java)
    private lateinit var inputFile: String

    override fun getCommandName(): String = "load"

    @Suppress("TooGenericExceptionCaught")
    override fun main(args: MutableList<String>) {
        try {
            ArgParser(args.drop(1).toTypedArray()).parseInto(::TransformationsRunnerArgs).run {
                inputFile = Paths.get(input).toString()
            }

            val df = DataFrame.readCSV(inputFile)
            val unifier = PyUnifier()
            val submissionsGraph = df.loadGraph(unifier)
            println(submissionsGraph.buildStringRepresentation())
        } catch (ex: Throwable) {
            logger.error(ex)
        } finally {
            exitProcess(0)
        }
    }

    data class TransformationsRunnerArgs(private val parser: ArgParser) {
        val input by parser.storing(
            "-i", "--input_file", help = "Input .csv file with code submissions"
        )
    }
}
