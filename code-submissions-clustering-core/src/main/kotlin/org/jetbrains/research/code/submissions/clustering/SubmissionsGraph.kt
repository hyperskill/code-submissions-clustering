package org.jetbrains.research.code.submissions.clustering

import com.jetbrains.rd.util.string.printToString
import org.jetbrains.research.code.submissions.clustering.load.Unifier
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph

class SubmissionsGraph(private val graph: Graph<Submission, DefaultWeightedEdge>) {
    fun print() = graph.printToString()
}

class GraphBuilder(private val unifier: Unifier) {
    private val graph: Graph<Submission, DefaultWeightedEdge> =
        SimpleDirectedWeightedGraph(DefaultWeightedEdge::class.java)

    fun add(submission: Submission) {
        unifier.run {
            graph.addVertex(submission.unify())
        }
    }

    fun build(): SubmissionsGraph = SubmissionsGraph(graph)
}

fun buildGraph(unifier: Unifier, block: GraphBuilder.() -> Unit): SubmissionsGraph {
    val builder = GraphBuilder(unifier)
    return builder.apply(block).build()
}
