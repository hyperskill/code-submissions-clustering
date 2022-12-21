package org.jetbrains.research.code.submissions.clustering

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.util.*
import org.jgrapht.Graph
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SerializationTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @JvmField
    @Parameterized.Parameter(0)
    var dataFrame: DataFrame<*>? = null

    @Test
    fun testSerializeGraph() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val graph = dataFrame!!.loadGraph(mockContext)
            val bytes = graph.toProto().toByteArray()
            val deserializedGraph = ProtoSubmissionsGraph.parseFrom(bytes).toGraph()
            assertEquals(
                graph.buildStringRepresentation(),
                deserializedGraph.buildStringRepresentation()
            )
        }
    }

    @Test
    fun testSerializeClusteredGraph() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val graph = dataFrame!!.loadGraph(mockContext)
            val clusterer = SubmissionsGraphHAC(DIST_LIMIT)
            graph.cluster(clusterer)
            val clusteredGraph = graph.getClusteredGraph()
            val bytes = clusteredGraph.toProto().toByteArray()
            val deserializedGraph = ProtoClusteredGraph.parseFrom(bytes).toGraph()

            clusteredGraph.getClustering().assertEquals(deserializedGraph.getClustering())
            val comparator = compareBy<Triple<Int, Int, Double>> { it.first }.thenBy { it.second }.thenBy { it.third }
            assertOrderedEquals(
                clusteredGraph.graph.mapEdges { it.id }.sortedWith(comparator),
                deserializedGraph.graph.mapEdges { it.id }.sortedWith(comparator)
            )
        }
    }

    private fun <V, E, T> Graph<V, E>.mapEdges(block: (V) -> T) =
        edgeSet().map {
            val from = block(getEdgeSource(it))
            val to = block(getEdgeTarget(it))
            val weight = getEdgeWeight(it)
            Triple(from, to, weight)
        }

    companion object {
        const val DIST_LIMIT = 3.0

        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION")
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0})")
        fun getTestData() = listOf(
            arrayOf(dataFrameOf("id", "step_id", "code", "quality")(emptySequence())),
            arrayOf(
                dataFrameOf("id", "step_id", "code", "quality")(
                    1, 1000, "print(1)", 1,
                )
            ),
            arrayOf(
                dataFrameOf("id", "step_id", "code", "quality")(
                    1, 1000, "y = 1\n", 1,
                    2, 1000, "var = 1\n", 1,
                    3, 1000, "a=1\n", 1,
                )
            ),
            arrayOf(
                dataFrameOf("id", "step_id", "code", "quality")(
                    1, 1000, "y =         1\n", 1,
                    2, 1000, "var       = 1\n", 1,
                    3, 1000, "a=1\n", 1,
                )
            ),
        )
    }
}
