package org.jetbrains.research.code.submissions.clustering.client

import io.grpc.ManagedChannel
import org.jetbrains.research.code.submissions.clustering.CodeServerGrpcKt
import org.jetbrains.research.code.submissions.clustering.SubmissionCode
import org.jetbrains.research.code.submissions.clustering.model.Submission
import java.io.Closeable
import java.util.concurrent.TimeUnit

class CodeServerClientImpl(private val channel: ManagedChannel) : Closeable {
    private val stub = CodeServerGrpcKt.CodeServerCoroutineStub(channel)

    override fun close() {
        channel.shutdown().awaitTermination(AWAIT_SECONDS, TimeUnit.SECONDS)
    }

    suspend fun unify(submission: Submission): Submission {
        val request = SubmissionCode.newBuilder().setCode(submission.code).build()
        val unifiedSubmissionCode = stub.unify(request)
        return submission.copy(code = unifiedSubmissionCode.code)
    }

    companion object {
        const val AWAIT_SECONDS: Long = 5
    }
}
