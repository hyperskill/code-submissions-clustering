package org.jetbrains.research.code.submissions.clustering.server

import io.grpc.Server
import io.grpc.ServerBuilder
import org.jetbrains.research.code.submissions.clustering.model.Language
import java.util.logging.Logger

class CodeServerImpl(private val port: Int, language: Language) {
    private val logger: Logger = Logger.getLogger(javaClass.name)
    val server: Server = ServerBuilder
        .forPort(port)
        .addService(CodeServerServiceImpl(language))
        .build()

    fun start() {
        server.start()
        logger.info("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info("Shutting down gRPC server since JVM is shutting down")
                this@CodeServerImpl.stop()
                logger.info("Server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}
