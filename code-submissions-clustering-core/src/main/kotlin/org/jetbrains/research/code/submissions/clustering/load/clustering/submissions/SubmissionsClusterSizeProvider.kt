package org.jetbrains.research.code.submissions.clustering.load.clustering.submissions

import org.jetbrains.research.code.submissions.clustering.load.clustering.Cluster
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClusterSizeProvider
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode

class SubmissionsClusterSizeProvider : ClusterSizeProvider<SubmissionsNode> {
    override fun getSize(cluster: Cluster<SubmissionsNode>): Int = cluster.entities.sumOf { it.submissionsList.size }
}
