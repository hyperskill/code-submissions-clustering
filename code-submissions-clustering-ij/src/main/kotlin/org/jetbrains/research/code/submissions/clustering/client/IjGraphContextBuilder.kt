package org.jetbrains.research.code.submissions.clustering.client

import io.grpc.ManagedChannelBuilder
import org.jetbrains.research.code.submissions.clustering.load.context.GraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext

class IjGraphContextBuilder(private val addressName: String, private val addressPort: Int) : GraphContextBuilder<Int> {
    override fun buildContext(): SubmissionsGraphContext<Int> {
        val clientImpl = CodeServerClientImpl(
            ManagedChannelBuilder
                .forAddress(addressName, addressPort)
                .usePlaintext()
                .build()
        )
        return SubmissionsGraphContext(
            IjUnifier(clientImpl),
            IjGumTreeDistanceMeasurer(clientImpl)
        )
    }
}
