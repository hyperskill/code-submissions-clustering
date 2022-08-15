package org.jetbrains.research.code.submissions.clustering

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class LoadGraphTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @ParameterizedTest
    @MethodSource("getTestData")
    fun testLoadGraphFromDataFrame(dataFrame: DataFrame<*>, expectedProtoGraph: ProtoSubmissionsGraph) {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            expectedProtoGraph.assertEquals(dataFrame.loadGraph(mockContext).toProto())
        }
    }

    companion object {
        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        fun getTestData(): List<Arguments> = listOf(
            Arguments.of(
                dataFrameOf("id", "step_id", "code")(emptySequence()),
                ProtoGraphBuilder().build()
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code")(
                    1, 1000, "print(1)",
                ),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addIdList(1)
                    }
                    .build()
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code")(
                    1, 1000, "y = 1\n",
                    2, 1000, "var = 1\n",
                    3, 1000, "a=1\n",
                ),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "v1 = 1\n"
                        addAllIdList(listOf(1, 2, 3))
                    }
                    .build()
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code")(
                    1, 1000, "y =         1\n",
                    2, 1000, "var       = 1\n",
                    3, 1000, "a=1\n",
                ),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "v1 = 1\n"
                        addAllIdList(listOf(1, 2, 3))
                    }
                    .build()
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code")(
                    1, 1000, "print(1)\n",
                    2, 1000, "x = 1\nprint(1)\n",
                ),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addIdList(1)
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addIdList(2)
                    }
                    .addEdge(0, 1, 3.0)
                    .addEdge(1, 0, 3.0)
                    .build()
            )
        )
    }
}
