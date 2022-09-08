package org.jetbrains.research.code.submissions.clustering.load.clustering.hac

import org.jetbrains.research.code.submissions.clustering.load.clustering.Cluster
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClusteringResult
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClustersGraph
import org.jetbrains.research.code.submissions.clustering.load.clustering.GraphClusterer
import org.jetbrains.research.code.submissions.clustering.load.clustering.hac.parallel.ParallelContext
import org.jetbrains.research.code.submissions.clustering.load.clustering.hac.parallel.ParallelUtils
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jgrapht.Graph
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.ClusteringImpl
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleWeightedGraph
import java.util.*
import java.util.function.Consumer
import java.util.logging.Logger
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap
import kotlin.collections.set

@Suppress("TYPEALIAS_NAME_INCORRECT_CASE")
typealias SubmissionsGraphHAC = GraphHierarchicalAgglomerativeClustering<SubmissionsNode, SubmissionsGraphEdge>

@Suppress("TooManyFunctions")
class GraphHierarchicalAgglomerativeClustering<V, E>(
    private val distanceLimit: Double,
    private val minClustersCount: Int,
) : GraphClusterer<V, E> {
    private val logger = Logger.getLogger(javaClass.name)
    private val heap: SortedSet<Triple> = TreeSet()
    private val triples: MutableMap<Long, Triple> = HashMap()
    private val communities: MutableSet<Community> = HashSet()
    private var idGenerator = 0
    private val triplesPoll: ArrayDeque<Triple> = ArrayDeque()

    private fun init(graph: Graph<V, E>) {
        val values = graph.vertexSet()
        heap.clear()
        triples.clear()
        communities.clear()
        idGenerator = 0
        values.forEach { value ->
            communities.add(singletonCommunity(value))
        }
        val communitiesAsList = communities.toMutableList()
        communitiesAsList.shuffle()
        ParallelContext().use { context ->
            val toInsert = context.runParallel(
                communitiesAsList,
                { mutableListOf() },
                { community: Community,
                    accumulator: MutableList<Triple> ->
                    graph.findTriples(community, accumulator)
                },
                ParallelUtils::combineLists
            )
            toInsert.forEach(Consumer { triple: Triple -> insertTriple(triple) })
        }
    }

    private fun Graph<V, E>.findTriples(community: Community, accumulator: MutableList<Triple>): MutableList<Triple> {
        for (another in communities) {
            if (another === community) {
                break
            }
            val firstVertex = community.entities[0]
            val secondVertex = another.entities[0]
            val edge = getEdge(firstVertex, secondVertex)
            val distance = getEdgeWeight(edge)
            accumulator.add(Triple(distance, community, another))
        }
        return accumulator
    }

    @Suppress("TooGenericExceptionCaught")
    override fun buildClustering(graph: Graph<V, E>): ClusteringResult<V> {
        logger.finer { "Clusterer initialization started" }
        init(graph)
        logger.finer { "Clusterer initialization finished" }
        logger.finer { "Clustering started" }
        while (heap.isNotEmpty() && communities.size > minClustersCount) {
            val minTriple: Triple = heap.first()
            invalidateTriple(minTriple)
            val first = minTriple.first
            val second = minTriple.second
            try {
                mergeCommunities(first, second)
            } catch (ex: Throwable) {
                logger.severe {
                    """Communities merging error {$ex}:
                        |First triple: $first
                        |Second triple: $second
                    """.trimMargin()
                }
            }
        }
        logger.finer { "Clustering finished" }
        val clustering = ClusteringImpl(communities.map { it.entities.toSet() })
        val clustersGraph = buildClustersGraph()
        return ClusteringResult(clustering, clustersGraph)
    }

    private fun buildClustersGraph(): ClustersGraph<V> {
        val clustrersGraph = SimpleWeightedGraph<Cluster<V>, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)
        triples.values.forEach { triple ->
            val firstVertex = triple.first.entities.toSet()
            val secondVertex = triple.second.entities.toSet()
            clustrersGraph.addVertex(firstVertex)
            clustrersGraph.addVertex(secondVertex)
            clustrersGraph.addEdge(firstVertex, secondVertex)
            val edge = clustrersGraph.getEdge(firstVertex, secondVertex)
            clustrersGraph.setEdgeWeight(edge, triple.distance)
        }
        return clustrersGraph
    }

    private fun mergeCommunities(first: Community, second: Community) {
        val merged: MutableList<V>
        if (first.entities.size < second.entities.size) {
            merged = second.entities
            merged.addAll(first.entities)
        } else {
            merged = first.entities
            merged.addAll(second.entities)
        }
        val newCommunity = Community(merged)
        communities.remove(first)
        communities.remove(second)
        for (community in communities) {
            val fromFirstId = getTripleId(first, community)
            val fromSecondId = getTripleId(second, community)
            val fromFirst = triples[fromFirstId]
            val fromSecond = triples[fromSecondId]
            val newDistance = getDistance(fromFirst).coerceAtLeast(getDistance(fromSecond))
            invalidateTriple(fromFirst)
            invalidateTriple(fromSecond)
            insertTriple(newDistance, newCommunity, community)
        }
        communities.add(newCommunity)
    }

    private fun getDistance(triple: Triple?): Double = triple?.distance ?: Double.POSITIVE_INFINITY

    private fun getTripleId(first: Community, second: Community): Long = if (second.id > first.id) {
        getTripleId(second, first)
    } else {
        first.id * ID_FACTOR + second.id
    }

    private fun insertTriple(triple: Triple) {
        triples[getTripleId(triple.first, triple.second)] = triple
        if (triple.distance < distanceLimit) {
            heap.add(triple)
        }
    }

    private fun insertTriple(distance: Double, first: Community, second: Community) {
        val triple = createTriple(distance, first, second)
        insertTriple(triple)
    }

    private fun invalidateTriple(triple: Triple?) {
        triple ?: return
        val tripleId = getTripleId(triple.first, triple.second)
        triples.remove(tripleId)
        heap.remove(triple)
        triple.release()
    }

    private fun singletonCommunity(entity: V): Community {
        val singletonList: MutableList<V> = ArrayList(1)
        singletonList.add(entity)
        return Community(singletonList)
    }

    private fun createTriple(distance: Double, first: Community, second: Community): Triple {
        if (triplesPoll.isNotEmpty()) {
            triplesPoll.removeFirst()
        }
        return Triple(distance, first, second)
    }

    /**
     * @property entities list of entities stored in the community
     */
    private inner class Community(val entities: MutableList<V>) :
        Comparable<Community> {
        val id: Int = idGenerator++

        override fun compareTo(other: Community): Int = id - other.id

        override fun hashCode(): Int = id

        override fun equals(other: Any?): Boolean =
            other!!.javaClass == Community::class.java && (other as GraphHierarchicalAgglomerativeClustering<*, *>.Community).id == id
    }

    /**
     * @property distance distance between two communities
     * @property first first community
     * @property second second community
     */
    private inner class Triple(
        var distance: Double,
        var first: Community,
        var second: Community
    ) :
        Comparable<Triple> {
        fun release() {
            triplesPoll.add(this)
        }

        override operator fun compareTo(other: Triple): Int {
            if (other === this) {
                return 0
            }
            if (distance != other.distance) {
                return distance.compareTo(other.distance)
            }
            return if (first !== other.first) {
                first.compareTo(other.first)
            } else {
                second.compareTo(other.second)
            }
        }

        override fun toString() = "($distance,[${first.entities.joinToString(",")}],[${second.entities.joinToString(",")}])"
    }

    companion object {
        private const val ID_FACTOR = 1_000_000_009L
    }
}
