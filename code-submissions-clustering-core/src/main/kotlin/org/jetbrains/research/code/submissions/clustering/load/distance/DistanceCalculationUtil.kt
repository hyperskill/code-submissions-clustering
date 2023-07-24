package org.jetbrains.research.code.submissions.clustering.load.distance

import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.util.IdentifierFactoryImpl
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge
import org.jetbrains.research.code.submissions.clustering.model.transformGraph
import org.jgrapht.graph.SimpleWeightedGraph

fun <T> SubmissionsGraph.calculateDistances(context: SubmissionsGraphContext<T>): SubmissionsGraph {
    val graph = this.graph.enumerateNodes()
    return transformGraph(context, graph) {
        calculateDistances()
    }
}

private fun SubmissionsGraphAlias.enumerateNodes(): SubmissionsGraphAlias {
    val vertices = this.vertexSet()
    val ids = vertices.map { it.id }.toSet()
    if (ids.size == 1 && ids.first() == 0) {
        val graph: SubmissionsGraphAlias = SimpleWeightedGraph(SubmissionsGraphEdge::class.java)
        val identifierFactory = IdentifierFactoryImpl()
        vertices.forEach {
            graph.addVertex(
                it.copy(id = identifierFactory.uniqueIdentifier())
            )
        }
        return graph
    }
    return this
}
