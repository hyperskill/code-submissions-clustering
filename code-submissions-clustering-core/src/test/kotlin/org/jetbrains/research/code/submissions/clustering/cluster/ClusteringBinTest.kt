package org.jetbrains.research.code.submissions.clustering.cluster

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.nio.file.Paths

@RunWith(Parameterized::class)
class ClusteringBinTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @JvmField
    @Parameterized.Parameter(0)
    var submissionsGraph: SubmissionsGraph? = null

    @JvmField
    @Parameterized.Parameter(1)
    var distanceLimit: Double? = null

    @JvmField
    @Parameterized.Parameter(2)
    var binDirName: String? = null

    @Test
    fun testInfiniteEdgesClustering() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val clusterer = SubmissionsGraphHAC(distanceLimit!!)
            submissionsGraph!!.cluster(clusterer)
            val clusteredGraph = submissionsGraph!!.getClusteredGraph().graph
            assert(
                (clusteredGraph.edgeSet().maxOfOrNull {
                    clusteredGraph.getEdgeWeight(it)
                } ?: 0).toDouble() < Double.POSITIVE_INFINITY
            )
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({2}, {1})")
        fun getTestDataFromBin(): List<Array<Any>> {
            val binDirName = "bin.data"
            val classLoader = Thread.currentThread().contextClassLoader
            val binDir = classLoader.getResource(binDirName)?.toURI()?.let { Paths.get(it) }
                ?: error("The bin folder was not found")

            val submissionsGraphs = binDir.toFile().walkTopDown().mapNotNull {
                (if (it.isSerializationFolder()) Paths.get(it.toURI()) else null)?.toSubmissionsGraph()
                    ?.let { graph -> graph to it.name }
            }

            return submissionsGraphs.map { (graph, path) ->
                (25..600 step 50).map { limit ->
                    arrayOf(graph, limit.toDouble(), path)
                }
            }.flatten().toList()
        }

        private fun File.isSerializationFolder(): Boolean {
            val graphSerName = "graph.bin"
            val clusterSerName = "clusters.bin"
            return this.isDirectory && File(this, graphSerName).exists() && File(this, clusterSerName).exists()
        }
    }
}
