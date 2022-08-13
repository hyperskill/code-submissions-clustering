package org.jetbrains.research.code.submissions.clustering.model

import org.jetbrains.research.code.submissions.clustering.load.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.util.toProto
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph

/**
 * @property graph inner representation of submissions graph
 */
data class SubmissionsGraph(val graph: Graph<SubmissionsNode, DefaultWeightedEdge>) {
    fun buildStringRepresentation() = toProto().toString()
}

class GraphBuilder(private val submissionsGraphContext: SubmissionsGraphContext) {
    private val graph: Graph<SubmissionsNode, DefaultWeightedEdge> =
        SimpleDirectedWeightedGraph(DefaultWeightedEdge::class.java)
    private val vertexByCode = HashMap<String, SubmissionsNode>()

    fun add(submission: Submission) {
        submissionsGraphContext.unifier.run {
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

    fun makeComplete() {
        vertexByCode.forEach { (code, vertex) ->
            vertexByCode.forEach innerLoop@ { (otherCode, otherVertex) ->
                if (code == otherCode) {
                    return@innerLoop
                }
                val edge: DefaultWeightedEdge = graph.addEdge(vertex, otherVertex)
                val dist = submissionsGraphContext.codeDistanceMeasurer.computeDistance(code, otherCode)
                graph.setEdgeWeight(edge, dist.toDouble())
            }
        }
    }

    fun build(): SubmissionsGraph = SubmissionsGraph(graph)
}

fun buildGraph(context: SubmissionsGraphContext, block: GraphBuilder.() -> Unit): SubmissionsGraph {
    val builder = GraphBuilder(context)
    return builder.apply(block).build()
}
