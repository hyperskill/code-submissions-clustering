package org.jetbrains.research.code.submissions.clustering.cluster

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.toCsv
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.Ignore
import org.junit.runners.Parameterized

@Ignore
open class ClusteringResultBaseTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @JvmField
    @Parameterized.Parameter(0)
    var protoGraph: ProtoSubmissionsGraph? = null

    @JvmField
    @Parameterized.Parameter(1)
    var distanceLimit: Double? = null

    @JvmField
    @Parameterized.Parameter(2)
    var expectedDataFrame: DataFrame<*>? = null

    fun testToClusteringDataFrameHelper() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val submissionsGraph = protoGraph!!.toGraph()
            val clusterer = SubmissionsGraphHAC(distanceLimit!!)
            submissionsGraph.cluster(clusterer)
            val clusteringResult = submissionsGraph.toClusteringDataFrame()
            assertEquals(expectedDataFrame!!.toCsv(), clusteringResult.toCsv())
        }
    }
}
