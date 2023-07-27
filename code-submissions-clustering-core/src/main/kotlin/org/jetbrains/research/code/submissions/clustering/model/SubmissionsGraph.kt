package org.jetbrains.research.code.submissions.clustering.model

import org.jetbrains.research.code.submissions.clustering.load.clustering.Cluster
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClusteredGraph
import org.jetbrains.research.code.submissions.clustering.load.clustering.GraphClusterer
import org.jetbrains.research.code.submissions.clustering.load.clustering.buildClusteredGraph
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.util.IdentifierFactoryImpl
import org.jetbrains.research.code.submissions.clustering.util.toProto
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleWeightedGraph

typealias SubmissionsGraphEdge = DefaultWeightedEdge

typealias SubmissionsGraphAlias = Graph<SubmissionsNode, SubmissionsGraphEdge>

/**
 * @property graph inner representation of submissions graph
 */
data class SubmissionsGraph(val graph: SubmissionsGraphAlias) {
    private var clusteredGraph: ClusteredGraph<SubmissionsNode>? = null

    fun getClusteredGraph(): ClusteredGraph<SubmissionsNode> = clusteredGraph ?: buildClusteredGraph {
        add(Cluster(0, graph.vertexSet().toMutableList()))
    }

    fun cluster(clusterer: GraphClusterer<SubmissionsNode, SubmissionsGraphEdge>) {
        clusteredGraph = clusterer.buildClustering(graph)
    }

    fun buildStringRepresentation() = toProto().toString()
}

class GraphTransformer<T>(
    private val submissionsGraphContext: SubmissionsGraphContext<T>,
    private val graph: SubmissionsGraphAlias
) {
    private val vertexByUnifiedCode = HashMap<String, SubmissionsNode>()
    private val vertexByInitialCode = HashMap<String, SubmissionsNode>()
    private val idFactory = IdentifierFactoryImpl()

    init {
        graph.vertexSet().forEach {
            vertexByInitialCode[it.code] = it
        }
    }

    fun add(submission: Submission) {
        submissionsGraphContext.unifier.run {
            vertexByInitialCode.compute(submission.code) { _, vertexByInitCode ->
                vertexByInitCode?.let {
                    // Update existing vertex
                    vertexByInitCode.submissionsList.add(submission.info)
                    vertexByInitCode
                } ?: run {
                    val unifiedSubmission = submission.unify()
                    vertexByUnifiedCode.compute(unifiedSubmission.code) { _, vertexByUnifCode ->
                        vertexByUnifCode?.let {
                            // Update existing vertex
                            vertexByUnifCode.submissionsList.add(unifiedSubmission.info)
                            vertexByUnifCode
                        } ?:  // Add new vertex with single id
                        SubmissionsNode(unifiedSubmission, idFactory.uniqueIdentifier()).also {
                            graph.addVertex(it)
                        }
                    }!!
                }
            }
        }
    }

    fun calculateDistances() {
        val vertices = graph.vertexSet()
        vertices.forEach { first ->
            vertices.forEach { second ->
                if (first.id < second.id && !graph.containsEdge(first, second)) {
                    val edge: SubmissionsGraphEdge = graph.addEdge(first, second)
                    val dist = submissionsGraphContext.codeDistanceMeasurer.computeDistanceWeight(
                        edge,
                        graph,
                    )
                    graph.setEdgeWeight(edge, dist.toDouble())
                }
            }
        }
    }

    fun build(): SubmissionsGraph = SubmissionsGraph(graph).also {
        vertexByInitialCode.clear()
        vertexByUnifiedCode.clear()
    }
}

fun <T> transformGraph(
    context: SubmissionsGraphContext<T>,
    graph: SubmissionsGraphAlias = SimpleWeightedGraph(SubmissionsGraphEdge::class.java),
    transformation: GraphTransformer<T>.() -> Unit
): SubmissionsGraph {
    val builder = GraphTransformer(context, graph)
    return builder.apply(transformation).build()
}
