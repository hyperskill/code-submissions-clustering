package org.jetbrains.research.code.submissions.clustering.server

import com.intellij.openapi.application.ApplicationStarter
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
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

    override val requiredModality: Int
        get() = ApplicationStarter.NOT_IN_EDT

    override fun main(args: List<String>) {
        try {
            parseArgs(args.toMutableList())
            logger.info("Starting IntelliJ Code Server on port=$portId")
            val server = CodeServerImpl(portId, lang)
            server.start()
        } catch (ex: Throwable) {
            logger.severe { ex.stackTraceToString() }
            exitProcess(1)
        } finally {
            exitProcess(0)
        }
    }

    private fun parseArgs(args: MutableList<String>) {
        val parser = ArgParser(args.drop(1).toTypedArray())
        parser.parseInto(CodeServerStarter::CodeServerStarterArgs).apply {
            lang = Language.valueOf(Paths.get(language).toString())
            portId = Paths.get(port).toString().toInt()
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
    }

    companion object {
        private const val BASE_PORT: Int = 8000
    }
}
