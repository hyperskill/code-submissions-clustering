package org.jetbrains.research.code.submissions.clustering.cluster

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.model.SubmissionInfo
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jetbrains.research.code.submissions.clustering.util.*
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.ClusteringImpl
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ClusteringTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @JvmField
    @Parameterized.Parameter(0)
    var protoGraph: ProtoSubmissionsGraph? = null

    @JvmField
    @Parameterized.Parameter(1)
    var distanceLimit: Double? = null

    @JvmField
    @Parameterized.Parameter(2)
    var expectedClustering: Clustering<SubmissionsNode>? = null

    @Test
    fun testClustering() {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val submissionsGraph = protoGraph!!.toGraph()
            val clusterer = SubmissionsGraphHAC(distanceLimit!!)
            submissionsGraph.cluster(clusterer)
            val clusteredGraph = submissionsGraph.getClusteredGraph()
            expectedClustering!!.assertEquals(clusteredGraph.getClustering())
        }
    }

    companion object {
        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1}, {2})")
        fun getTestData() = listOf(
            arrayOf(
                ProtoGraphBuilder().build(),
                1.0,
                ClusteringImpl<SubmissionsNode>(listOf())
            ),
            arrayOf(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addInfo(SubmissionInfo(1, 1).toProto())
                    }
                    .build(),
                1.0,
                ClusteringImpl(
                    listOf(
                        setOf(
                            SubmissionsNode(
                                1, "print(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(1, 1)
                                )
                            )
                        ),
                    )
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
                        addInfo(SubmissionInfo(2, 1).toProto())
                    }
                    .addEdge(0, 1, 3.0)
                    .build(),
                5.0,
                ClusteringImpl(
                    listOf(
                        setOf(
                            SubmissionsNode(
                                1, "print(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(1, 1)
                                )
                            ),
                            SubmissionsNode(
                                2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(2, 1)
                                )
                            ),
                        )
                    )
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
                        addInfo(SubmissionInfo(2, 1).toProto())
                    }
                    .addEdge(0, 1, 3.0)
                    .build(),
                2.0,
                ClusteringImpl(
                    listOf(
                        setOf(
                            SubmissionsNode(
                                1, "print(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(1, 1)
                                )
                            )
                        ),
                        setOf(
                            SubmissionsNode(
                                2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(2, 1)
                                )
                            )
                        ),
                    )
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
                0.0,
                ClusteringImpl(
                    listOf(
                        setOf(
                            SubmissionsNode(
                                1, "print(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(1, 1)
                                )
                            )
                        ),
                        setOf(
                            SubmissionsNode(
                                2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(2, 1)
                                )
                            )
                        ),
                        setOf(
                            SubmissionsNode(
                                2, "v1 = 1\nprint(v1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(3, 1)
                                )
                            )
                        ),
                    )
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
                ClusteringImpl(
                    listOf(
                        setOf(
                            SubmissionsNode(
                                1, "print(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(1, 1)
                                )
                            ),
                            SubmissionsNode(
                                2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(2, 1)
                                )
                            ),
                        ),
                        setOf(
                            SubmissionsNode(
                                2, "v1 = 1\nprint(v1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(3, 1)
                                )
                            )
                        ),
                    )
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
                2.0,
                ClusteringImpl(
                    listOf(
                        setOf(
                            SubmissionsNode(
                                1, "print(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(1, 1)
                                )
                            ),
                            SubmissionsNode(
                                2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(2, 1)
                                )
                            ),
                        ),
                        setOf(
                            SubmissionsNode(
                                2, "v1 = 1\nprint(v1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(3, 1)
                                )
                            )
                        ),
                    )
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
                3.0,
                ClusteringImpl(
                    listOf(
                        setOf(
                            SubmissionsNode(
                                1, "print(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(1, 1)
                                )
                            ),
                            SubmissionsNode(
                                2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(2, 1)
                                )
                            ),
                            SubmissionsNode(
                                2, "v1 = 1\nprint(v1)\n", 1000, mutableSetOf(
                                    SubmissionInfo(3, 1)
                                )
                            ),
                        )
                    )
                )
            ),
        )
    }
}
