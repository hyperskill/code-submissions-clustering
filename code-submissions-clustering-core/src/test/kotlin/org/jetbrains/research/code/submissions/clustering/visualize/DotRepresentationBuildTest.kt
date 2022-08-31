package org.jetbrains.research.code.submissions.clustering.visualize

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.load.visualization.SubmissionsGraphVisualizer
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class DotRepresentationBuildTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @ParameterizedTest
    @MethodSource("getTestData")
    fun testLoadGraphFromDataFrame(protoGraph: ProtoSubmissionsGraph, expectedDotRepr: String) {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val submissionsGraph = protoGraph.toGraph()
            val visualizer = SubmissionsGraphVisualizer()
            assertEquals(expectedDotRepr, visualizer.toDot(submissionsGraph))
        }
    }

    companion object {
        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        fun getTestData(): List<Arguments> = listOf(
            Arguments.of(
                ProtoGraphBuilder().build(),
                """graph ? {
                    |
                    |}
                """.trimMargin()
            ),
            Arguments.of(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addIdList(1)
                    }
                    .build(),
                """graph 1000 {
                    |
                    |  v1 [label = "v1", style = filled, fillcolor = "0.1 1.00 1.0"]
                    |
                    |}
                """.trimMargin()
            ),
            Arguments.of(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "v1 = 1\n"
                        addAllIdList(listOf(1, 2, 3))
                    }
                    .build(),
                """graph 1000 {
                    |
                    |  v1 [label = "v1", style = filled, fillcolor = "0.1 1.00 1.0"]
                    |
                    |}
                """.trimMargin()
            ),
            Arguments.of(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addIdList(1)
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addIdList(2)
                    }
                    .addEdge(0, 1, 1.0)
                    .build(),
                """graph 1000 {
                    |
                    |  v1 [label = "v1", style = filled, fillcolor = "0.1 0.55 1.0"]
                    |  v2 [label = "v2", style = filled, fillcolor = "0.1 0.55 1.0"]
                    |
                    |  v1 -- v2 [label = "1"]
                    |
                    |}
                """.trimMargin()
            )
        )
    }
}
