package org.jetbrains.research.code.submissions.clustering.client

import org.jetbrains.research.code.submissions.clustering.load.context.GraphContextBuilder
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.server.CodeServerOrchestratorConfig

class IjGraphContextBuilder(private val config: CodeServerOrchestratorConfig) : GraphContextBuilder<Int> {
    override fun buildContext(): SubmissionsGraphContext<Int> {
        val orchestrator = CodeServerOrchestrator(config)
        return SubmissionsGraphContext(
            IjUnifier(orchestrator),
            IjGumTreeDistanceMeasurer(orchestrator)
        )
    }
}
