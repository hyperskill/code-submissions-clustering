package org.jetbrains.research.code.submissions.clustering.visualize

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.load.visualization.SubmissionsGraphToDotConverter
import org.jetbrains.research.code.submissions.clustering.model.SubmissionInfo
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.Test
import org.junit.jupiter.params.provider.Arguments
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DotRepresentationBuildStructureToDotTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @JvmField
    @Parameterized.Parameter(0)
    var protoGraph: ProtoSubmissionsGraph? = null

    @JvmField
    @Parameterized.Parameter(1)
    var distanceLimit: Double? = null

    @JvmField
    @Parameterized.Parameter(2)
    var expectedDotRepr: String? = null

    @Test
    fun testStructureToDot() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val submissionsGraph = protoGraph!!.toGraph()
            val clusterer = SubmissionsGraphHAC(distanceLimit!!)
            submissionsGraph.cluster(clusterer)
            with(SubmissionsGraphToDotConverter()) {
                val clusteredGraph = submissionsGraph.getClusteredGraph()
                assertEquals(expectedDotRepr!!, clusteredGraph.toDot())
            }
        }
    }

    companion object {
        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1}, {2})")
        fun getStructureTestData() = listOf(
            arrayOf(
                ProtoGraphBuilder().build(),
                1.0,
                """graph ? {
                    |
                    |  subgraph {
                    |    node [shape = box]
                    |  }
                    |
                    |}
                """.trimMargin()
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .build(),
                1.0,
                """graph 1000 {
                    |
                    |  subgraph {
                    |    node [shape = box]
                    |    C0
                    |  }
                    |
                    |}
                """.trimMargin()
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "v1 = 1\n"
                        addAllInfo(listOf(
                            SubmissionInfo(1, 1),
                            SubmissionInfo(2, 1),
                            SubmissionInfo(3, 1),
                        ).map { it.toProto() })
                    }
                    .build(),
                1.0,
                """graph 1000 {
                    |
                    |  subgraph {
                    |    node [shape = box]
                    |    C0
                    |  }
                    |
                    |}
                """.trimMargin()
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 1).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addInfo(SubmissionInfo(3, 1).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                1.0,
                """graph 1000 {
                    |
                    |  subgraph {
                    |    node [shape = box]
                    |    C0 -- C1 [label = "3"]
                    |  }
                    |
                    |}
                """.trimMargin()
            ),
        )
    }
}
