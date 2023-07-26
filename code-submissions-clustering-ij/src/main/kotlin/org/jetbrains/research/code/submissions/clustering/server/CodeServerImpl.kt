package org.jetbrains.research.code.submissions.clustering.server

import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.jetbrains.research.code.submissions.clustering.SubmissionCode
import org.jetbrains.research.code.submissions.clustering.SubmissionsEdge
import org.jetbrains.research.code.submissions.clustering.SubmissionsWeight
import org.jetbrains.research.code.submissions.clustering.impl.context.gumtree.GumTreeGraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.impl.distance.gumtree.GumTreeDistanceMeasurerByPsi
import org.jetbrains.research.code.submissions.clustering.model.Language
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.code.submissions.clustering.model.SubmissionInfo
import java.util.logging.Logger

class CodeServerImpl(private val port: Int, language: Language) {
    private val logger: Logger = Logger.getLogger(javaClass.name)
    private val graphContext = GumTreeGraphContextBuilder().setLanguage(language).buildContext()
    private val requestChannel = Channel<CodeServerRequest>()
    private val responseChannel = Channel<CodeServerResponse>()
    val server: Server = ServerBuilder
        .forPort(port)
        .addService(CodeServerServiceImpl(requestChannel, responseChannel))
        .build()

    fun start() {
        server.start()
        logger.info("Server started, listening on $port")
        runBlocking {
            while (true) {
                when (val request = requestChannel.receive()) {
                    is UnifyRequest ->
                        responseChannel.send(UnifyResponse(unifyImpl(request.submissionCode)))
                    is CalcDistRequest ->
                        responseChannel.send(CalcDistResponse(calculateWeightImpl(request.submissionsEdge)))
                    is ClearRequest -> graphContext.unifier.clear()
                }
            }
        }
    }

    private fun unifyImpl(request: SubmissionCode): SubmissionCode {
        logger.info("Receive request: \n${request.code}")
        val code = graphContext.unifier.run {
            createMockSubmission(request.code).unify().code
        }
        logger.info("Unification finished")
        return SubmissionCode.newBuilder().setCode(code).build()
    }

    private fun calculateWeightImpl(request: SubmissionsEdge): SubmissionsWeight {
        logger.info(
            """Receive request: 
                    |Source submission:
                    |${request.from.code}
                    |Target submission:
                    |${request.to.code}
                    |""".trimMargin()
        )
        val weight = (graphContext.codeDistanceMeasurer as GumTreeDistanceMeasurerByPsi).run {
            val source = request.from.code.parseTree()
            val target = request.to.code.parseTree()
            calculateDistance(source, target).calculateWeight()
        }
        logger.info("Distance calculation finished")
        return SubmissionsWeight.newBuilder().setWeight(weight).build()
    }

    private fun createMockSubmission(code: String) = Submission(SubmissionInfo(0, 0), 0, code)
}
