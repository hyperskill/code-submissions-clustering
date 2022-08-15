package org.jetbrains.research.code.submissions.clustering.model

import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.context.builder.SubmissionNodeIdentifierFactoryImpl
import org.jetbrains.research.code.submissions.clustering.util.toProto
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph

typealias SubmissionsGraphEdge = DefaultWeightedEdge

typealias SubmissionsGraphAlias = Graph<SubmissionsNode, SubmissionsGraphEdge>

/**
 * @property graph inner representation of submissions graph
 */
data class SubmissionsGraph(val graph: SubmissionsGraphAlias) {
    fun buildStringRepresentation() = toProto().toString()
}

class GraphBuilder<T>(private val submissionsGraphContext: SubmissionsGraphContext<T>) {
    private val graph: SubmissionsGraphAlias = SimpleDirectedWeightedGraph(SubmissionsGraphEdge::class.java)
    private val vertexByCode = HashMap<String, SubmissionsNode>()
    private val idNodeFactory = SubmissionNodeIdentifierFactoryImpl()

    fun add(submission: Submission) {
        submissionsGraphContext.unifier.run {
            val unifiedSubmission = submission.unify()
            vertexByCode.compute(unifiedSubmission.code) { _, vertex ->
                vertex?.let {
                    // Update existing vertex
                    vertex.idList.add(unifiedSubmission.id)
                    vertex
                } ?:  // Add new vertex with single id
                SubmissionsNode(unifiedSubmission, idNodeFactory.uniqueIdentifier()).also {
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
                val edge: SubmissionsGraphEdge = graph.addEdge(vertex, otherVertex)
                val dist = submissionsGraphContext.codeDistanceMeasurer.computeDistanceWeight(edge, graph)
                graph.setEdgeWeight(edge, dist.toDouble())
            }
        }
    }

    fun build(): SubmissionsGraph = SubmissionsGraph(graph)
}

fun <T> buildGraph(context: SubmissionsGraphContext<T>, block: GraphBuilder<T>.() -> Unit): SubmissionsGraph {
    val builder = GraphBuilder(context)
    return builder.apply(block).build()
}
