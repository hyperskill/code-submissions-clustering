package org.jetbrains.research.code.submissions.clustering.load.clustering.hac

import org.jetbrains.research.code.submissions.clustering.load.clustering.*
import org.jetbrains.research.code.submissions.clustering.util.parallel.ParallelContext
import org.jetbrains.research.code.submissions.clustering.load.context.builder.IdentifierFactoryImpl
import org.jetbrains.research.code.submissions.clustering.util.parallel.ParallelUtils.combineWith
import org.jgrapht.Graph
import java.util.*
import java.util.function.Consumer
import java.util.logging.Logger
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * HAC algorithm implementation for graphs.
 *
 * Class implements graph clusterer using hierarchical agglomerative clustering (HAC) with complete linkage criterion.
 *
 * @param distanceLimit max distance between two vertices inside one cluster
 * @param minClustersCount min final number of clusters (single cluster by default)
 */
class GraphHierarchicalAgglomerativeClustering<V, E>(
    private val weightProvider: ClusterSizeProvider<V>,
    private val distanceLimit: Double,
    private val minClustersCount: Int = 1,
) : GraphClusterer<V, E> {
    private val logger = Logger.getLogger(javaClass.name)
    private val heap: SortedSet<ClusterTriple> = TreeSet()
    private val triples: MutableMap<Long, ClusterTriple> = HashMap()
    private val clusters: MutableSet<Cluster<V>> = HashSet()
    private val identifierFactory = IdentifierFactoryImpl()

    private fun init(graph: Graph<V, E>) {
        val values = graph.vertexSet()
        heap.clear()
        triples.clear()
        clusters.clear()
        values.forEach { value ->
            clusters.add(singletonCluster(value))
        }
        val communitiesAsList = clusters.toMutableList()
        communitiesAsList.shuffle()
        ParallelContext().use { context ->
            val toInsert = context.runParallel(
                communitiesAsList,
                { mutableListOf() },
                { cluster: Cluster<V>,
                    accumulator: MutableList<ClusterTriple> ->
                    graph.findTriples(cluster, accumulator)
                },
                { first, second -> first.combineWith(second) }
            )
            toInsert.forEach(Consumer { triple: ClusterTriple -> insertTriple(triple) })
        }
    }

    private fun Graph<V, E>.findTriples(
        cluster: Cluster<V>,
        accumulator: MutableList<ClusterTriple>
    ): MutableList<ClusterTriple> {
        for (another in clusters) {
            if (another === cluster) {
                break
            }
            val firstVertex = cluster.entities[0]
            val secondVertex = another.entities[0]
            val edge = getEdge(firstVertex, secondVertex)
            val distance = getEdgeWeight(edge)
            accumulator.add(ClusterTriple(distance, cluster, another))
        }
        return accumulator
    }

    @Suppress("TooGenericExceptionCaught")
    override fun buildClustering(graph: Graph<V, E>): ClusteredGraph<V> {
        logger.finer { "Clusterer initialization started" }
        init(graph)
        logger.finer { "Clusterer initialization finished" }
        logger.finer { "Clustering started" }
        while (heap.isNotEmpty() && clusters.size > minClustersCount) {
            val minTriple: ClusterTriple = heap.first()
            invalidateTriple(minTriple)
            val first = minTriple.first
            val second = minTriple.second
            logger.fine {
                """Merging clusters:
                |$minTriple
            """.trimMargin()
            }
            try {
                mergeCommunities(first, second)
            } catch (ex: Throwable) {
                logger.severe {
                    """Clusters merging error {$ex}:
                        |$minTriple
                    """.trimMargin()
                }
            }
        }
        logger.finer { "Clustering finished" }
        if (clusters.size == 1) {
            return buildClusteredGraph { add(clusters.first()) }
        }
        return buildClusteredGraph {
            triples.values.forEach {
                add(it.distance, it.first, it.second)
            }
        }
    }

    private fun mergeCommunities(first: Cluster<V>, second: Cluster<V>) {
        val merged: MutableList<V> = first.entities.combineWith(second.entities)
        val newCluster = Cluster(identifierFactory.uniqueIdentifier(), merged)
        clusters.removeAll(setOf(first, second))
        for (cluster in clusters) {
            val fromFirstId = getTripleId(first, cluster)
            val fromSecondId = getTripleId(second, cluster)
            val fromFirst = triples[fromFirstId]
            val fromSecond = triples[fromSecondId]
            val newDistance = getDistance(fromFirst).coerceAtLeast(getDistance(fromSecond))
            fromFirst?.let { invalidateTriple(fromFirst) }
            fromSecond?.let { invalidateTriple(fromSecond) }
            insertTriple(newDistance, newCluster, cluster)
        }
        clusters.add(newCluster)
    }

    private fun getDistance(triple: ClusterTriple?): Double = triple?.distance ?: Double.POSITIVE_INFINITY

    private fun getTripleId(first: Cluster<V>, second: Cluster<V>): Long = if (second.id > first.id) {
        getTripleId(second, first)
    } else {
        first.id * ID_FACTOR + second.id
    }

    private fun insertTriple(triple: ClusterTriple) {
        triples[getTripleId(triple.first, triple.second)] = triple
        if (triple.distance <= distanceLimit) {
            heap.add(triple)
        }
    }

    private fun insertTriple(distance: Double, first: Cluster<V>, second: Cluster<V>) =
        ClusterTriple(distance, first, second).also { insertTriple(it) }

    private fun invalidateTriple(triple: ClusterTriple) {
        val tripleId = getTripleId(triple.first, triple.second)
        triples.remove(tripleId)
        heap.remove(triple)
    }

    private fun singletonCluster(entity: V): Cluster<V> {
        val singletonList: MutableList<V> = ArrayList(1)
        singletonList.add(entity)
        return Cluster(identifierFactory.uniqueIdentifier(), singletonList)
    }

    /**
     * @property distance distance between two clusters
     * @property first first cluster
     * @property second second cluster
     */
    private inner class ClusterTriple(
        val distance: Double,
        val first: Cluster<V>,
        val second: Cluster<V>
    ) : Comparable<ClusterTriple> {
        override operator fun compareTo(other: ClusterTriple): Int {
            if (other === this) {
                return 0
            }
            if (distance != other.distance) {
                return distance.compareTo(other.distance)
            }
            return maxOf(weightProvider.getSize(first), weightProvider.getSize(second)) -
                maxOf(weightProvider.getSize(other.first), weightProvider.getSize(other.second))
        }

        override fun toString() =
            """Distance: $distance
                |
                |First cluster:
                |$first
                |
                |Second cluster:
                |$second
            """.trimMargin()
    }

    companion object {
        private const val ID_FACTOR = 1_000_000_009L
    }
}
