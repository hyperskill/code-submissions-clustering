package org.jetbrains.research.code.submissions.clustering.load.distance.measurers

import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge

abstract class CodeDistanceMeasurerBase<T> {
    private val cache: MutableMap<SubmissionsGraphEdge, T> = mutableMapOf()

    abstract fun T.calculateWeight(): Int

    abstract fun computeFullDistance(edge: SubmissionsGraphEdge, graph: SubmissionsGraphAlias, context: SubmissionsGraphContext<*>): T

    private fun computeFullDistanceWithCache(edge: SubmissionsGraphEdge, graph: SubmissionsGraphAlias, context: SubmissionsGraphContext<*>) =
        cache.getOrPut(edge) {
            computeFullDistance(edge, graph, context)
        }

    fun computeDistanceWeight(
        edge: SubmissionsGraphEdge,
        graph: SubmissionsGraphAlias,
        context: SubmissionsGraphContext<*>
    ) = computeFullDistanceWithCache(edge, graph, context).calculateWeight()
}
