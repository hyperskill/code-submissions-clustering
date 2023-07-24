package org.jetbrains.research.code.submissions.clustering.load.clustering

import org.jetbrains.research.code.submissions.clustering.util.Identifier
import org.jetbrains.research.code.submissions.clustering.util.IdentifierFactoryImpl
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jgrapht.Graph
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.ClusteringImpl
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleWeightedGraph

typealias ClusteredGraphEdge = DefaultWeightedEdge

typealias ClusteredGraphAlias <V> = Graph<Cluster<V>, ClusteredGraphEdge>

/**
 * @property id cluster id
 * @property entities cluster entities
 */
data class Cluster<V>(val id: Identifier, val entities: MutableList<V>) : Comparable<Cluster<V>> {
    override fun compareTo(other: Cluster<V>): Int = id - other.id

    override fun hashCode(): Int = id

    override fun equals(other: Any?): Boolean =
        other?.javaClass == Cluster::class.java && (other as Cluster<*>).id == id

    override fun toString(): String {
        val sep = "${System.lineSeparator()}${"-".repeat(SEP_CNT)}${System.lineSeparator()}"
        return entities.joinToString(sep, prefix = sep, postfix = sep) {
            """# [id=${(it as SubmissionsNode).id}]
                |
                |${(it as SubmissionsNode).code}
            """.trimMargin()
        }
    }

    companion object {
        const val SEP_CNT = 60
    }
}

/**
 * @property graph inner representation of clustered graph
 */
data class ClusteredGraph<V>(val graph: ClusteredGraphAlias<V>) {
    fun getClustering(): Clustering<V> = ClusteringImpl(graph.vertexSet().map { it.entities.toSet() })

    override fun toString() = buildString {
        val sep = "=".repeat(SEP_CNT)
        graph.vertexSet().sorted().forEach { cluster ->
            appendLine("$sep Cluster ${cluster.id} $sep")
            appendLine(cluster)
        }
    }

    companion object {
        const val SEP_CNT = 30
    }
}

class ClusteredGraphBuilder<V>(private val graph: ClusteredGraphAlias<V>) {
    private val identifierFactory = IdentifierFactoryImpl(0)
    private val initialIndexToCluster = HashMap<Int, Cluster<V>>()

    private fun getOrNewCluster(cluster: Cluster<V>): Cluster<V> = initialIndexToCluster[cluster.id] ?: Cluster(
        identifierFactory.uniqueIdentifier(),
        cluster.entities
    ).also { initialIndexToCluster[cluster.id] = it }

    fun add(distance: Double, first: Cluster<V>, second: Cluster<V>) {
        val firstCluster = getOrNewCluster(first)
        val secondCluster = getOrNewCluster(second)
        graph.addVertex(firstCluster)
        graph.addVertex(secondCluster)
        graph.addEdge(firstCluster, secondCluster).also {
            graph.setEdgeWeight(it, distance)
        }
    }

    fun add(cluster: Cluster<V>) = graph.addVertex(getOrNewCluster(cluster))

    fun build(): ClusteredGraph<V> = ClusteredGraph(graph)
}

fun <V> buildClusteredGraph(block: ClusteredGraphBuilder<V>.() -> Unit): ClusteredGraph<V> {
    val builder = ClusteredGraphBuilder<V>(SimpleWeightedGraph(ClusteredGraphEdge::class.java))
    return builder.apply(block).build()
}
