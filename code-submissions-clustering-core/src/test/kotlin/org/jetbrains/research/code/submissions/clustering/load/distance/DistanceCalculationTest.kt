package org.jetbrains.research.code.submissions.clustering.load.distance

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionInfo
import org.jetbrains.research.code.submissions.clustering.util.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class DistanceCalculationTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @ParameterizedTest
    @MethodSource("getTestData")
    fun testCalculateDistances(protoGraph: ProtoSubmissionsGraph, expectedProtoGraph: ProtoSubmissionsGraph) {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            expectedProtoGraph.assertEquals(protoGraph.toGraph().calculateDistances(mockContext).toProto())
        }
    }

    companion object {
        @Suppress("TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        fun getTestData(): List<Arguments> = listOf(
            Arguments.of(
                ProtoGraphBuilder().build(),
                ProtoGraphBuilder().build()
            ),
            Arguments.of(
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
            Arguments.of(
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
            Arguments.of(
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
