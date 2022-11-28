package org.jetbrains.research.code.submissions.clustering

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.research.code.submissions.clustering.model.SubmissionInfo
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
                dataFrameOf("id", "step_id", "code", "quality")(emptySequence()),
                ProtoGraphBuilder().build()
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code", "quality")(
                    1, 1000, "print(1)", 1,
                ),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .build()
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code", "quality")(
                    1, 1000, "y = 1\n", 1,
                    2, 1000, "var = 1\n", 1,
                    3, 1000, "a=1\n", 1,
                ),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "v1 = 1\n"
                        addAllInfo(listOf(
                            SubmissionInfo(1, 1),
                            SubmissionInfo(2, 1),
                            SubmissionInfo(3, 1),
                        ).map { it.toProto() })
                    }
                    .build()
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code", "quality")(
                    1, 1000, "y =         1\n", 1,
                    2, 1000, "var       = 1\n", 1,
                    3, 1000, "a=1\n", 1,
                ),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "v1 = 1\n"
                        addAllInfo(listOf(
                            SubmissionInfo(1, 1),
                            SubmissionInfo(2, 1),
                            SubmissionInfo(3, 1),
                        ).map { it.toProto() })
                    }
                    .build()
            ),
            Arguments.of(
                dataFrameOf("id", "step_id", "code", "quality")(
                    1, 1000, "print(1)\n", 1,
                    2, 1000, "x = 1\nprint(1)\n", 1,
                ),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 1).toProto())
                    }
                    .addEdge(0, 1, 3.0)
                    .build()
            )
        )
    }
}
