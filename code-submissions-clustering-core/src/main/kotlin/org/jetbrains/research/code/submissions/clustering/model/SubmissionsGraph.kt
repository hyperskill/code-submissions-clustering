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
    private val vertexByCode = HashMap<String, SubmissionsNode>()

    fun add(submission: Submission) {
        unifier.run {
            val unifiedSubmission = submission.unify()
            vertexByCode.compute(unifiedSubmission.code) { _, vertex ->
                vertex?.let {
                    // Update existing vertex
                    vertex.idList.add(unifiedSubmission.id)
                    vertex
                } ?:  // Add new vertex with single id
                SubmissionsNode(unifiedSubmission).also {
                    graph.addVertex(it)
                }
            }
        }
    }

    fun build(): SubmissionsGraph = SubmissionsGraph(graph)
}

fun buildGraph(unifier: AbstractUnifier, block: GraphBuilder.() -> Unit): SubmissionsGraph {
    val builder = GraphBuilder(unifier)
    return builder.apply(block).build()
}
