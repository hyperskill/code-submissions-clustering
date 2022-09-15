package org.jetbrains.research.code.submissions.clustering.load.clustering

import org.jetbrains.research.code.submissions.clustering.load.context.builder.Identifier
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
        val sep = "\n~~~~~~~~~~~~~~~~\n"
        return entities.joinToString(sep, prefix = sep, postfix = sep) { (it as SubmissionsNode).code }
    }
}

/**
 * @property graph inner representation of clustered graph
 */
data class ClusteredGraph<V>(val graph: ClusteredGraphAlias<V>) {
    fun getClustering(): Clustering<V> = ClusteringImpl(graph.vertexSet().map { it.entities.toSet() })
}

class ClusteredGraphBuilder<V>(private val graph: ClusteredGraphAlias<V>) {
    fun add(distance: Double, first: Cluster<V>, second: Cluster<V>) {
        graph.addVertex(first)
        graph.addVertex(second)
        graph.addEdge(first, second).also {
            graph.setEdgeWeight(it, distance)
        }
    }

    fun add(cluster: Cluster<V>) = graph.addVertex(cluster)

    fun build(): ClusteredGraph<V> = ClusteredGraph(graph)
}

fun <V> buildClusteredGraph(block: ClusteredGraphBuilder<V>.() -> Unit): ClusteredGraph<V> {
    val builder = ClusteredGraphBuilder<V>(SimpleWeightedGraph(ClusteredGraphEdge::class.java))
    return builder.apply(block).build()
}
