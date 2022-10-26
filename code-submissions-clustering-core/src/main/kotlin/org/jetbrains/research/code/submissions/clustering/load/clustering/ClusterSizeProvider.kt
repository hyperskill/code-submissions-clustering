package org.jetbrains.research.code.submissions.clustering.load.clustering

interface ClusterSizeProvider<V> {
    fun getSize(cluster: Cluster<V>): Int = cluster.entities.size
}
