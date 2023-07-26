package org.jetbrains.research.code.submissions.clustering.client

import io.grpc.ManagedChannel
import org.jetbrains.research.code.submissions.clustering.CodeServerGrpcKt
import org.jetbrains.research.code.submissions.clustering.Empty
import org.jetbrains.research.code.submissions.clustering.SubmissionCode
import org.jetbrains.research.code.submissions.clustering.SubmissionsEdge
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge
import java.io.Closeable
import java.util.concurrent.TimeUnit

class CodeServerClientImpl(private val channel: ManagedChannel) : Closeable {
    private val stub = CodeServerGrpcKt.CodeServerCoroutineStub(channel)

    override fun close() {
        channel.shutdown().awaitTermination(AWAIT_SECONDS, TimeUnit.SECONDS)
    }

    suspend fun unify(submission: Submission): Submission {
        val request = submissionsCode(submission.code)
        val unifiedSubmissionCode = stub.unify(request)
        return submission.copy(code = unifiedSubmissionCode.code)
    }

    suspend fun calculateDist(
        edge: SubmissionsGraphEdge,
        graph: SubmissionsGraphAlias,
    ): Int {
        val request = submissionsEdge(
            submissionsCode(graph.getEdgeSource(edge).code),
            submissionsCode(graph.getEdgeTarget(edge).code)
        )
        return stub.calculateWeight(request).weight
    }

    suspend fun clear() = stub.clear(Empty.newBuilder().build())

    private fun submissionsCode(code: String) =
        SubmissionCode
            .newBuilder()
            .setCode(code)
            .build()

    private fun submissionsEdge(from: SubmissionCode, to: SubmissionCode) =
        SubmissionsEdge
            .newBuilder()
            .setFrom(from)
            .setTo(to)
            .build()

    companion object {
        const val AWAIT_SECONDS: Long = 5
    }
}
