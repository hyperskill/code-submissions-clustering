package org.jetbrains.research.code.submissions.clustering.model

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.research.code.submissions.clustering.load.clustering.Cluster
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClusteredGraph
import org.jetbrains.research.code.submissions.clustering.load.clustering.GraphClusterer
import org.jetbrains.research.code.submissions.clustering.load.clustering.buildClusteredGraph
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.util.IdentifierFactoryImpl
import org.jetbrains.research.code.submissions.clustering.util.parallel.ConcurrentCache
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
    private val vertexByUnifiedCode = ConcurrentCache<String, SubmissionsNode>()
    private val vertexByInitialCode = ConcurrentCache<String, SubmissionsNode>()
    private val idFactory = IdentifierFactoryImpl()
    private val mutex = Mutex()

    init {
        graph.vertexSet().forEach {
            vertexByInitialCode[it.code] = it
        }
    }

    suspend fun add(submission: Submission) {
        if (submission.code.length > CODE_LENGTH_LIMIT) {
            return
        }
        submissionsGraphContext.unifier.run {
            vertexByInitialCode.compute(submission) {
                val unifiedSubmission = it.unify()
                vertexByUnifiedCode.compute(unifiedSubmission) {
                    SubmissionsNode(unifiedSubmission, idFactory.uniqueIdentifier()).also {
                        mutex.withLock {
                            graph.addVertex(it)
                        }
                    }
                }
            }
        }
    }

    private suspend fun ConcurrentCache<String, SubmissionsNode>.compute(
        submission: Submission,
        newNodeAction: suspend (Submission) -> SubmissionsNode
    ) = this.computeOrUpdate(submission.code) {
        it?.also {
            it.submissionsList.add(submission.info)
        } ?: newNodeAction(submission)
    }

    suspend fun calculateDistances() = coroutineScope {
        val vertices = graph.vertexSet()
        vertices.forEach { first ->
            vertices.forEach { second ->
                if (first.id < second.id && !graph.containsEdge(first, second)) {
                    val edge: SubmissionsGraphEdge = graph.addEdge(first, second)
                    launch {
                        val dist = submissionsGraphContext.codeDistanceMeasurer.computeDistanceWeight(
                            edge,
                            graph,
                        )
                        mutex.withLock {
                            graph.setEdgeWeight(edge, dist.toDouble())
                        }
                    }
                }
            }
        }
    }

    fun build(): SubmissionsGraph = SubmissionsGraph(graph).also {
        vertexByInitialCode.clear()
        vertexByUnifiedCode.clear()
    }

    companion object {
        const val CODE_LENGTH_LIMIT = 10000
    }
}

suspend fun <T> transformGraph(
    context: SubmissionsGraphContext<T>,
    graph: SubmissionsGraphAlias = SimpleWeightedGraph(SubmissionsGraphEdge::class.java),
    transformation: suspend GraphTransformer<T>.() -> Unit
): SubmissionsGraph {
    val builder = GraphTransformer(context, graph)
    return builder.apply { transformation() }.build()
}
