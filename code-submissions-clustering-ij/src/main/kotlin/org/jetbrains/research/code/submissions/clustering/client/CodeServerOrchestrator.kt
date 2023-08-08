package org.jetbrains.research.code.submissions.clustering.client

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge
import org.jetbrains.research.code.submissions.clustering.server.CodeServerOrchestratorConfig

@DelicateCoroutinesApi
class CodeServerOrchestrator(config: CodeServerOrchestratorConfig) {
    private val clients = config.servers.map {
        CodeServerClientImpl(
            ManagedChannelBuilder
                .forAddress("localhost", it.port)
                .usePlaintext()
                .build()
        )
    }
    private val tokens = Channel<Int>(capacity = clients.size).apply {
        runBlocking {
            clients.indices.forEach { send(it) }
        }
    }

    suspend fun unify(submission: Submission): Submission = withClient { unify(submission) }

    suspend fun calculateDist(
        edge: SubmissionsGraphEdge,
        graph: SubmissionsGraphAlias,
    ): Int = withClient { calculateDist(edge, graph) }

    suspend fun clearUnifier() = withClient { clearUnifier() }

    suspend fun clearDistMeasurer() = withClient { clearDistMeasurer() }

    private suspend inline fun <T> withClient(block: CodeServerClientImpl.() -> T): T {
        val token = tokens.receive()
        return clients[token].run {
            val result = block()
            tokens.send(token)
            result
        }
    }
}
