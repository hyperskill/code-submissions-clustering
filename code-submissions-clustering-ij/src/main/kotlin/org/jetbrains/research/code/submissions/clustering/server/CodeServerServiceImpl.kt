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

    override suspend fun clear(request: Empty): Empty {
        requestChannel.send(ClearRequest)
        return Empty.newBuilder().build()
    }
}
