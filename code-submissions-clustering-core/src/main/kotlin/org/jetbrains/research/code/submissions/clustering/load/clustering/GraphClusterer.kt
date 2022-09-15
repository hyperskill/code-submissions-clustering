package org.jetbrains.research.code.submissions.clustering.load.clustering

import org.jgrapht.Graph

interface GraphClusterer <V, E> {
    fun buildClustering(graph: Graph<V, E>): ClusteredGraph<V>
}
