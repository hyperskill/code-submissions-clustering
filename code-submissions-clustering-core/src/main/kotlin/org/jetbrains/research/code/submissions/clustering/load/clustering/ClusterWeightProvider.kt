package org.jetbrains.research.code.submissions.clustering.load.clustering

interface ClusterWeightProvider<V> {
    fun getWeight(cluster: Cluster<V>): Int = cluster.entities.size
}
