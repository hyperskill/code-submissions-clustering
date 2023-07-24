package org.jetbrains.research.code.submissions.clustering.client

import kotlinx.coroutines.runBlocking
import org.jetbrains.research.code.submissions.clustering.load.distance.CodeDistanceMeasurerBase
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge

class IjGumTreeDistanceMeasurer(private val clientImpl: CodeServerClientImpl) : CodeDistanceMeasurerBase<Int>() {
    override fun Int.calculateWeight(): Int = this

    override fun computeFullDistance(edge: SubmissionsGraphEdge, graph: SubmissionsGraphAlias): Int = runBlocking {
        clientImpl.calculateDist(edge, graph)
    }
}
