package org.jetbrains.research.code.submissions.clustering.cluster

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.io.toCsv
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.model.SubmissionInfo
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ClusteringRangingResultTest : ClusteringResultBaseTest() {
    @Test
    fun testToClusteringDataFrame() {
        testToClusteringDataFrameHelper()
    }

    companion object {
        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1}, {2})")
        fun getRangingTestData() = listOf(
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 4).toProto())
                    }
                    .build(),
                1.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 0, 0,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 2).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 4).toProto())
                    }
                    .addEdge(0, 1, 3.0)
                    .build(),
                5.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 0, 1,
                    2, 0, 0,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 2).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 4).toProto())
                    }
                    .addEdge(0, 1, 3.0)
                    .build(),
                2.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 1, 0,
                    2, 0, 0,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 3).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 2).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addInfo(SubmissionInfo(3, 1).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                0.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 1, 0,
                    2, 0, 0,
                    3, 2, 0,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 3).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 2).toProto())
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
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 0, 0,
                    2, 0, 1,
                    3, 1, 0,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 3).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 2).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addInfo(SubmissionInfo(3, 1).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                2.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 0, 0,
                    2, 0, 1,
                    3, 1, 0,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 3).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 2).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addInfo(SubmissionInfo(3, 1).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                3.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 0, 0,
                    2, 0, 1,
                    3, 0, 2,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 2).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addInfo(SubmissionInfo(3, 3).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                3.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 0, 2,
                    2, 0, 1,
                    3, 0, 0,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 3).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addInfo(SubmissionInfo(3, 2).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                3.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 0, 2,
                    2, 0, 0,
                    3, 0, 1,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 2).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 1).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addInfo(SubmissionInfo(3, 3).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                3.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 0, 1,
                    2, 0, 2,
                    3, 0, 0,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 2).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 3).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addInfo(SubmissionInfo(3, 1).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                3.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 0, 1,
                    2, 0, 0,
                    3, 0, 2,
                )
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 3).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(1)\n"
                        addInfo(SubmissionInfo(2, 1).toProto())
                    }
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addInfo(SubmissionInfo(3, 2).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                3.0,
                dataFrameOf("submission_id", "cluster_id", "position")(
                    1, 0, 0,
                    2, 0, 2,
                    3, 0, 1,
                )
            ),
        )
    }
}
