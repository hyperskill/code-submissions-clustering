package org.jetbrains.research.code.submissions.clustering.server

import com.intellij.openapi.application.ApplicationStarter
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.research.code.submissions.clustering.impl.unifiers.TransformationsConfig
import org.jetbrains.research.code.submissions.clustering.model.Language
import java.nio.file.Paths
import java.util.logging.Logger
import kotlin.system.exitProcess

@Suppress("TooGenericExceptionCaught")
class CodeServerStarter : ApplicationStarter {
    private val logger: Logger = Logger.getLogger(javaClass.name)
    private var portId: Int = BASE_PORT
    override val commandName: String = "ij-code-server"
    private lateinit var lang: Language
    private lateinit var transformationsCfg: TransformationsConfig

    override fun main(args: List<String>) {
        try {
            parseArgs(args.toMutableList())
            logger.info("Starting IntelliJ Code Server on port=$portId")
            val server = CodeServerImpl(portId, lang, transformationsCfg)
            server.start()
        } catch (ex: Throwable) {
            logger.severe { ex.stackTraceToString() }
            exitProcess(1)
        } finally {
            exitProcess(0)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun parseArgs(args: MutableList<String>) {
        val parser = ArgParser(args.drop(1).toTypedArray())
        parser.parseInto(CodeServerStarter::CodeServerStarterArgs).apply {
            lang = Language.valueOf(Paths.get(language).toString())
            portId = Paths.get(port).toString().toInt()
            transformationsCfg = Json.decodeFromStream(Paths.get(transformationsConfig).toFile().inputStream())
        }
    }

    data class CodeServerStarterArgs(private val parser: ArgParser) {
        val port by parser.storing(
            "-p", "--port",
            help = "Server port"
        ).default(BASE_PORT.toString())
        val language by parser.storing(
            "-l", "--language",
            help = "Programming language of code submissions"
        )
        val transformationsConfig by parser.storing(
            "-c", "--transformationsConfig",
            help = "Path to .json file configuring code transformations"
        )
    }

    companion object {
        private const val BASE_PORT: Int = 8000
    }
}
