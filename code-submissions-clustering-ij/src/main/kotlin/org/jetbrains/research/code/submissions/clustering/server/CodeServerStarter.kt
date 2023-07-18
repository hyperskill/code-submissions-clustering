package org.jetbrains.research.code.submissions.clustering.server

import com.intellij.openapi.application.ApplicationStarter
import org.jetbrains.research.code.submissions.clustering.model.Language
import java.util.logging.Logger
import kotlin.system.exitProcess

@Suppress("TooGenericExceptionCaught")
object CodeServerStarter : ApplicationStarter {
    private val logger: Logger = Logger.getLogger(javaClass.name)
    private const val BASE_PORT: Int = 8000
    private var portId: Int = BASE_PORT
    override val commandName: String = "ij-code-server"

    override fun main(args: List<String>) {
        try {
            logger.info("Starting IntelliJ Code Server on port=$portId")
            val server = CodeServerImpl(portId, Language.PYTHON)
            server.start()
            server.blockUntilShutdown()
        } catch (ex: Throwable) {
            logger.severe { ex.stackTraceToString() }
            exitProcess(1)
        } finally {
            exitProcess(0)
        }
    }
}
