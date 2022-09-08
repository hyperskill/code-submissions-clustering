package org.jetbrains.research.code.submissions.clustering.load.clustering

import org.jgrapht.Graph
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering
import org.jgrapht.graph.DefaultWeightedEdge

typealias Cluster<V> = Set<V>
typealias Clusters<V> = List<Cluster<V>>
typealias ClustersGraph<V> = Graph<Cluster<V>, DefaultWeightedEdge>
typealias ClusteringResult<V> = Pair<Clustering<V>, ClustersGraph<V>>

interface GraphClusterer <V, E> {
    fun buildClustering(graph: Graph<V, E>): ClusteringResult<V>
}
