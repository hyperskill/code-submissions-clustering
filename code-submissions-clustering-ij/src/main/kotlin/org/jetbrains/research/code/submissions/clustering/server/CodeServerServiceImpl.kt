package org.jetbrains.research.code.submissions.clustering.server

import kotlinx.coroutines.channels.Channel
import org.jetbrains.research.code.submissions.clustering.*
import java.util.logging.Logger

class CodeServerServiceImpl(
    private val requestChannel: Channel<CodeServerRequest>,
    private val responseChannel: Channel<CodeServerResponse>
) : CodeServerGrpcKt.CodeServerCoroutineImplBase() {
    private val logger: Logger = Logger.getLogger(javaClass.name)

    override suspend fun unify(request: SubmissionCode): SubmissionCode {
        requestChannel.send(UnifyRequest(request))
        logger.info("Unification request sent to server")
        return (responseChannel.receive() as UnifyResponse).submissionCode
    }

    override suspend fun calculateWeight(request: SubmissionsEdge): SubmissionsWeight {
        requestChannel.send(CalcDistRequest(request))
        logger.info("Distance calculation request sent to server")
        return (responseChannel.receive() as CalcDistResponse).submissionsWeight
    }

    override suspend fun clearUnifier(request: Empty): Empty {
        logger.info("Clear unifier request sent to server")
        requestChannel.send(ClearUnifierRequest)
        return Empty.newBuilder().build()
    }

    override suspend fun clearDistMeasurer(request: Empty): Empty {
        logger.info("Clear code distance measurer request sent to server")
        requestChannel.send(ClearDistMeasurerRequest)
        return Empty.newBuilder().build()
    }
}
