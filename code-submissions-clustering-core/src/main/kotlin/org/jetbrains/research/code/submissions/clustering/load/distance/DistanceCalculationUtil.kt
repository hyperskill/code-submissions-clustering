package org.jetbrains.research.code.submissions.clustering.load.distance

import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.context.builder.IdentifierFactoryImpl
import org.jetbrains.research.code.submissions.clustering.model.*
import org.jetbrains.research.code.submissions.clustering.util.toGraph
import org.jgrapht.graph.SimpleWeightedGraph
import java.io.File

fun <T> SubmissionsGraph.calculateDistances(context: SubmissionsGraphContext<T>): SubmissionsGraph {
    val graph = this.graph.enumerateNodes()
    return transformGraph(context, graph) {
        calculateDistances()
    }
}

fun File.toSubmissionsGraph() = ProtoSubmissionsGraph.parseFrom(this.inputStream()).toGraph()

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
