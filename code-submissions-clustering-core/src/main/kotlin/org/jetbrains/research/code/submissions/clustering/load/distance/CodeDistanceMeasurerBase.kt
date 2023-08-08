package org.jetbrains.research.code.submissions.clustering.load.distance

import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge

abstract class CodeDistanceMeasurerBase<T> {
    abstract suspend fun T.calculateWeight(): Int

    abstract suspend fun computeFullDistance(edge: SubmissionsGraphEdge, graph: SubmissionsGraphAlias): T

    suspend fun computeDistanceWeight(
        edge: SubmissionsGraphEdge,
        graph: SubmissionsGraphAlias,
    ) = computeFullDistance(edge, graph).calculateWeight()

    open suspend fun clear() = Unit
}
