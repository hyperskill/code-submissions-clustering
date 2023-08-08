package org.jetbrains.research.code.submissions.clustering.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.research.code.submissions.clustering.cli.models.AbstractGraphBuilderFlags
import org.jetbrains.research.code.submissions.clustering.cli.models.AbstractGraphBuilderOptions
import org.jetbrains.research.code.submissions.clustering.cli.models.Writer
import org.jetbrains.research.code.submissions.clustering.client.IjGraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.util.*
import java.util.logging.Logger
import kotlin.io.path.Path
import kotlin.system.exitProcess

abstract class AbstractGraphBuilder(name: String, help: String) : CliktCommand(name = name, help = help) {
    private val logger: Logger = Logger.getLogger(javaClass.name)
    protected val commonOptions by AbstractGraphBuilderOptions()
    private val flags by AbstractGraphBuilderFlags()

    private fun getWriters() = listOf(
        Writer(SubmissionsGraph::writeToTxt, true),
        Writer(SubmissionsGraph::writeToBinary, flags.serializeGraph),
        Writer(SubmissionsGraph::writeToCsv, flags.saveCSV),
        Writer(SubmissionsGraph::writeToPng, flags.visualize),
        Writer(SubmissionsGraph::writeClustersToTxt, flags.saveClusters),
        Writer(SubmissionsGraph::writeClusteringResult, flags.clusteringResult),
    )

    fun buildGraphContext(): SubmissionsGraphContext<*> =
        IjGraphContextBuilder(
            Json.decodeFromStream(commonOptions.configFile.inputStream())
        ).buildContext()

    fun SubmissionsGraph.writeOutputData() {
        createFolder(Path(commonOptions.outputDir))
        getWriters().filter { it.toWrite }.forEach { tryToWrite(it.writer) }
    }

    @Suppress("TooGenericExceptionCaught")
    fun startRunner(run: suspend () -> Unit) {
        try {
            runBlocking {
                run()
            }
        } catch (ex: Throwable) {
            logger.severe { ex.stackTraceToString() }
            exitProcess(1)
        } finally {
            exitProcess(0)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun SubmissionsGraph.tryToWrite(write: SubmissionsGraph.(String) -> Unit) {
        try {
            write(commonOptions.outputDir)
        } catch (ex: Throwable) {
            logger.severe { "Writing failed: $ex" }
        }
    }
}
