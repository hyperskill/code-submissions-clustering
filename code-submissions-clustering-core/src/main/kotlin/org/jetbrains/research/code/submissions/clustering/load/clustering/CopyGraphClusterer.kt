package org.jetbrains.research.code.submissions.clustering.load.clustering

import org.jgrapht.Graph

class CopyGraphClusterer<V, E>(private val clusteredGraph: ClusteredGraph<V>) : GraphClusterer<V, E> {
    override fun buildClustering(graph: Graph<V, E>): ClusteredGraph<V> = clusteredGraph
}
