package org.jetbrains.research.code.submissions.clustering.visualize

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.load.visualization.SubmissionsGraphToDotConverter
import org.jetbrains.research.code.submissions.clustering.model.SubmissionInfo
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class DotRepresentationBuildTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @ParameterizedTest
    @MethodSource("getClustersTestData")
    fun testClustersToDot(protoGraph: ProtoSubmissionsGraph, distanceLimit: Double, expectedDotRepr: String) {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val submissionsGraph = protoGraph.toGraph()
            val clusterer = SubmissionsGraphHAC(distanceLimit)
            submissionsGraph.cluster(clusterer)
            with(SubmissionsGraphToDotConverter()) {
                assertEquals(expectedDotRepr, submissionsGraph.toDot())
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getStructureTestData")
    fun testStructureToDot(protoGraph: ProtoSubmissionsGraph, distanceLimit: Double, expectedDotRepr: String) {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val submissionsGraph = protoGraph.toGraph()
            val clusterer = SubmissionsGraphHAC(distanceLimit)
            submissionsGraph.cluster(clusterer)
            with(SubmissionsGraphToDotConverter()) {
                val clusteredGraph = submissionsGraph.getClusteredGraph()
                assertEquals(expectedDotRepr, clusteredGraph.toDot())
            }
        }
    }

    companion object {
        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        fun getClustersTestData(): List<Arguments> = listOf(
            Arguments.of(
                ProtoGraphBuilder().build(),
                1.0,
                """graph ? {
                    |
                    |
                    |}
                """.trimMargin()
            ),
            Arguments.of(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .build(),
                1.0,
                """graph 1000 {
                    |
                    |  v1 [label = "v1", style = filled, fillcolor = "0.1 1.00 1.0"]
                    |
                    |  subgraph cluster_0 {
                    |    label = < <B>C0</B>  [1 node] >
                    |    v1
                    |  }
                    |
                    |}
                """.trimMargin()
            ),
            Arguments.of(
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
                    |  v1 [label = "v1", style = filled, fillcolor = "0.1 1.00 1.0"]
                    |
                    |  subgraph cluster_0 {
                    |    label = < <B>C0</B>  [1 node] >
                    |    v1
                    |  }
                    |
                    |}
                """.trimMargin()
            ),
            Arguments.of(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 1).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .build(),
                1.0,
                """graph 1000 {
                    |
                    |  v1 [label = "v1", style = filled, fillcolor = "0.1 0.55 1.0"]
                    |  v2 [label = "v2", style = filled, fillcolor = "0.1 0.55 1.0"]
                    |
                    |  subgraph cluster_0 {
                    |    label = < <B>C0</B>  [2 nodes] >
                    |    v1 -- v2 [label = "1"]
                    |  }
                    |
                    |}
                """.trimMargin()
            ),
            Arguments.of(
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
                    |  v1 [label = "v1", style = filled, fillcolor = "0.1 0.40 1.0"]
                    |  v2 [label = "v2", style = filled, fillcolor = "0.1 0.40 1.0"]
                    |  v3 [label = "v3", style = filled, fillcolor = "0.1 0.40 1.0"]
                    |
                    |  subgraph cluster_0 {
                    |    label = < <B>C0</B>  [2 nodes] >
                    |    v1 -- v2 [label = "1"]
                    |  }
                    |  subgraph cluster_1 {
                    |    label = < <B>C1</B>  [1 node] >
                    |    v3
                    |  }
                    |
                    |}
                """.trimMargin()
            ),
        )

        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        fun getStructureTestData(): List<Arguments> = listOf(
            Arguments.of(
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
            Arguments.of(
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
            Arguments.of(
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
            Arguments.of(
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
