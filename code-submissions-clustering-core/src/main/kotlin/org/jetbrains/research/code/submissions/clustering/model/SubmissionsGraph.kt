package org.jetbrains.research.code.submissions.clustering.model

import org.jetbrains.research.code.submissions.clustering.load.AbstractUnifier
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph

data class SubmissionsGraph(private val graph: Graph<SubmissionsNode, DefaultWeightedEdge>) {
    fun buildStringRepresentation() = graph.toString()
}

class GraphBuilder(private val unifier: AbstractUnifier) {
    private val graph: Graph<SubmissionsNode, DefaultWeightedEdge> =
        SimpleDirectedWeightedGraph(DefaultWeightedEdge::class.java)

    fun add(submission: Submission) {
        unifier.run {
            val unifiedSubmission = submission.unify()
            // Check if suitable vertex already exists
            val vertex = graph.vertexSet().find { it.code == unifiedSubmission.code }
            vertex?.let {
                // Update vertex
                graph.removeVertex(vertex)
                graph.addVertex(SubmissionsNode(vertex, unifiedSubmission))
            } ?:  // Add new vertex with single id
            graph.addVertex(SubmissionsNode(unifiedSubmission))
        }
    }

    fun build(): SubmissionsGraph = SubmissionsGraph(graph)
}

fun buildGraph(unifier: AbstractUnifier, block: GraphBuilder.() -> Unit): SubmissionsGraph {
    val builder = GraphBuilder(unifier)
    return builder.apply(block).build()
}
