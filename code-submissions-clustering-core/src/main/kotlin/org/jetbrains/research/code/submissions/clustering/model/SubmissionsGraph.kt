package org.jetbrains.research.code.submissions.clustering.model

import org.jetbrains.research.code.submissions.clustering.load.AbstractUnifier
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph

data class SubmissionsGraph(private val graph: Graph<Submission, DefaultWeightedEdge>) {
    fun buildStringRepresentation() = graph.toString()
}

class GraphBuilder(private val unifier: AbstractUnifier) {
    private val graph: Graph<Submission, DefaultWeightedEdge> =
        SimpleDirectedWeightedGraph(DefaultWeightedEdge::class.java)

    fun add(submission: Submission) {
        unifier.run {
            graph.addVertex(submission.unify())
        }
    }

    fun build(): SubmissionsGraph = SubmissionsGraph(graph)
}

fun buildGraph(unifier: AbstractUnifier, block: GraphBuilder.() -> Unit): SubmissionsGraph {
    val builder = GraphBuilder(unifier)
    return builder.apply(block).build()
}
