@file:Suppress("FILE_NAME_INCORRECT")

package org.jetbrains.research.code.submissions.clustering.load.clustering.submissions

import org.jetbrains.research.code.submissions.clustering.load.clustering.GraphClusterer
import org.jetbrains.research.code.submissions.clustering.load.clustering.hac.GraphHierarchicalAgglomerativeClustering
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode

@Suppress("CLASS_NAME_INCORRECT")
class SubmissionsGraphHAC(distanceLimit: Double) :
    GraphClusterer<SubmissionsNode, SubmissionsGraphEdge> by GraphHierarchicalAgglomerativeClustering(
    SubmissionsClusterSizeProvider(),
    distanceLimit
)
