package org.jetbrains.research.code.submissions.clustering.client

import org.jetbrains.research.code.submissions.clustering.load.distance.CodeDistanceMeasurerBase
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge

class IjGumTreeDistanceMeasurer(private val orchestrator: CodeServerOrchestrator) : CodeDistanceMeasurerBase<Int>() {
    override suspend fun Int.calculateWeight(): Int = this

    override suspend fun computeFullDistance(edge: SubmissionsGraphEdge, graph: SubmissionsGraphAlias): Int =
        orchestrator.calculateDist(edge, graph)

    override suspend fun clear() {
        orchestrator.clearDistMeasurer()
    }
}
