package org.jetbrains.research.code.submissions.clustering.load.clustering.submissions

import org.jetbrains.research.code.submissions.clustering.load.clustering.Cluster
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClusterWeightProvider
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode

class SubmissionsClusterWeightProvider : ClusterWeightProvider<SubmissionsNode> {
    override fun getWeight(cluster: Cluster<SubmissionsNode>): Int = cluster.entities.sumOf { it.idList.size }
}
