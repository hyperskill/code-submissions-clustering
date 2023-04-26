package org.jetbrains.research.code.submissions.clustering

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.research.code.submissions.clustering.model.SubmissionInfo
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class LoadGraphTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @JvmField
    @Parameterized.Parameter(0)
    var dataFrame: DataFrame<*>? = null

    @JvmField
    @Parameterized.Parameter(1)
    var expectedProtoGraph: ProtoSubmissionsGraph? = null

    @Test
    fun testLoadGraphFromDataFrame() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            mockContext.unifier.clearFactory()
            expectedProtoGraph!!.assertEquals(dataFrame!!.loadGraph(mockContext).toProto())
        }
    }

    companion object {
        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = listOf(
            arrayOf(
                dataFrameOf("id", "step_id", "code", "quality")(emptySequence()),
                ProtoGraphBuilder().build()
            ),
            arrayOf(
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
            arrayOf(
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
            arrayOf(
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
            arrayOf(
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
