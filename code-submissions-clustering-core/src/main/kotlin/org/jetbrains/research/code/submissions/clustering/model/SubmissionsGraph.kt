package org.jetbrains.research.code.submissions.clustering.model

import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.context.builder.IdentifierFactoryImpl
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

class GraphTransformer<T>(
    private val submissionsGraphContext: SubmissionsGraphContext<T>,
    private val graph: SubmissionsGraphAlias
) {
    private val vertexByCode = HashMap<String, SubmissionsNode>()
    private val idFactory = IdentifierFactoryImpl()

    init {
        graph.vertexSet().forEach {
            vertexByCode[it.code] = it
        }
    }

    fun add(submission: Submission) {
        submissionsGraphContext.unifier.run {
            val unifiedSubmission = submission.unify()
            vertexByCode.compute(unifiedSubmission.code) { _, vertex ->
                vertex?.let {
                    // Update existing vertex
                    vertex.idList.add(unifiedSubmission.id)
                    vertex
                } ?:  // Add new vertex with single id
                SubmissionsNode(unifiedSubmission, idFactory.uniqueIdentifier()).also {
                    graph.addVertex(it)
                }
            }
        }
    }

    fun calculateDistances() {
        val vertices = graph.vertexSet()
        vertices.forEach { first ->
            vertices.forEach { second ->
                if (first.id != second.id && !graph.containsEdge(first, second)) {
                    val edge: SubmissionsGraphEdge = graph.addEdge(first, second)
                    val dist = submissionsGraphContext.codeDistanceMeasurer.computeDistanceWeight(edge, graph)
                    graph.setEdgeWeight(edge, dist.toDouble())
                }
            }
        }
    }

    fun build(): SubmissionsGraph = SubmissionsGraph(graph)
}

fun <T> transformGraph(
    context: SubmissionsGraphContext<T>,
    graph: SubmissionsGraphAlias = SimpleDirectedWeightedGraph(SubmissionsGraphEdge::class.java),
    transformation: GraphTransformer<T>.() -> Unit
): SubmissionsGraph {
    val builder = GraphTransformer(context, graph)
    return builder.apply(transformation).build()
}
