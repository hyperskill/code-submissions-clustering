package org.jetbrains.research.code.submissions.clustering.load.distance

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionInfo
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DistanceCalculationTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @JvmField
    @Parameterized.Parameter(0)
    var protoGraph: ProtoSubmissionsGraph? = null

    @JvmField
    @Parameterized.Parameter(1)
    var expectedProtoGraph: ProtoSubmissionsGraph? = null

    @Test
    fun testCalculateDistances() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            clearFactory()
            expectedProtoGraph!!.assertEquals(protoGraph!!.toGraph().calculateDistances(mockContext).toProto())
        }
    }

    companion object {
        @Suppress("TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() = listOf(
            arrayOf(
                ProtoGraphBuilder().build(),
                ProtoGraphBuilder().build()
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .build(),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .build()
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
                    .build(),
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
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "for v1 in [1, 2, 3]: print(v1)\n"
                        addAllInfo(listOf(
                            SubmissionInfo(1, 1),
                        ).map { it.toProto() })
                    }
                    .addNode {
                        code = "v1 = [1, 2, 3]\nfor v2 in v1: print(v2)\n"
                        addAllInfo(listOf(
                            SubmissionInfo(2, 1),
                            SubmissionInfo(3, 1),
                        ).map { it.toProto() })
                    }
                    .build(),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "for v1 in [1, 2, 3]: print(v1)\n"
                        addAllInfo(listOf(
                            SubmissionInfo(1, 1),
                        ).map { it.toProto() })
                    }
                    .addNode {
                        code = "v1 = [1, 2, 3]\nfor v2 in v1: print(v2)\n"
                        addAllInfo(listOf(
                            SubmissionInfo(2, 1),
                            SubmissionInfo(3, 1),
                        ).map { it.toProto() })
                    }
                    .addEdge(0, 1, 87.0)
                    .build()
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .addNode {
                        code = "print(2)\n"
                        addInfo(SubmissionInfo(2, 1).toProto())
                    }
                    .addNode {
                        code = "print(3)\n"
                        addInfo(SubmissionInfo(3, 1).toProto())
                    }
                    .build(),
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .addNode {
                        code = "print(2)\n"
                        addInfo(SubmissionInfo(2, 1).toProto())
                    }
                    .addNode {
                        code = "print(3)\n"
                        addInfo(SubmissionInfo(3, 1).toProto())
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 1.0)
                    .addEdge(1, 2, 1.0)
                    .build()
            ),
        )
    }
}
