package org.jetbrains.research.code.submissions.clustering.util

import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.context.builder.IdentifierFactoryImpl
import org.jetbrains.research.code.submissions.clustering.model.*
import org.jgrapht.graph.SimpleDirectedWeightedGraph
import java.io.File

fun <T> SubmissionsGraph.calculateDistances(context: SubmissionsGraphContext<T>): SubmissionsGraph {
    val graph = this.graph.enumerateNodes()
    return buildGraph(context, graph) {
        calculateDistances()
    }
}

fun File.toSubmissionsGraph() = ProtoSubmissionsGraph.parseFrom(this.inputStream()).toGraph()

private fun SubmissionsGraphAlias.enumerateNodes(): SubmissionsGraphAlias {
    val vertices = this.vertexSet()
    val ids = vertices.map { it.id }.toSet()
    if (ids == setOf(0)) {
        val graph: SubmissionsGraphAlias = SimpleDirectedWeightedGraph(SubmissionsGraphEdge::class.java)
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
