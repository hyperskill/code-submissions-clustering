package org.jetbrains.research.code.submissions.clustering.cluster

import com.intellij.openapi.command.WriteCommandAction
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.load.clustering.submissions.SubmissionsGraphHAC
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jetbrains.research.code.submissions.clustering.util.*
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.ClusteringImpl
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.nio.file.Paths

class ClusteringTest : ParametrizedBaseWithUnifierTest(getTmpProjectDir()) {
    @ParameterizedTest
    @MethodSource("getTestData")
    fun testClustering(
        protoGraph: ProtoSubmissionsGraph,
        distanceLimit: Double,
        expectedClustering: Clustering<SubmissionsNode>
    ) {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val submissionsGraph = protoGraph.toGraph()
            val clusterer = SubmissionsGraphHAC(distanceLimit)
            submissionsGraph.cluster(clusterer)
            val clusteredGraph = submissionsGraph.getClusteredGraph()
            expectedClustering.assertEquals(clusteredGraph.getClustering())
        }
    }

    @ParameterizedTest
    @MethodSource("getTestDataFromBin")
    fun testInfiniteEdgesClustering(
        submissionsGraph: SubmissionsGraph,
        distanceLimit: Double
    ) {
        WriteCommandAction.runWriteCommandAction(mockProject) {
            val clusterer = SubmissionsGraphHAC(distanceLimit)
            submissionsGraph.cluster(clusterer)
            val clusteredGraph = submissionsGraph.getClusteredGraph().graph
            assert(
                (clusteredGraph.edgeSet().maxOfOrNull {
                    clusteredGraph.getEdgeWeight(it)
                } ?: 0).toDouble() < Double.POSITIVE_INFINITY
            )
        }
    }

    companion object {
        @Suppress("WRONG_NEWLINES", "TOO_LONG_FUNCTION", "LongMethod")
        @JvmStatic
        fun getTestData(): List<Arguments> = listOf(
            Arguments.of(
                ProtoGraphBuilder().build(),
                1.0,
                ClusteringImpl<SubmissionsNode>(listOf())
            ),
            Arguments.of(
                ProtoGraphBuilder(1000)
                    .addNode {
                        code = "print(1)\n"
                        addIdList(1)
                    }
                    .build(),
                1.0,
                ClusteringImpl(listOf(
                    setOf(SubmissionsNode(1, "print(1)\n", 1000, mutableSetOf(1))),
                ))
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
                    .addEdge(0, 1, 3.0)
                    .build(),
                5.0,
                ClusteringImpl(listOf(
                    setOf(
                        SubmissionsNode(1, "print(1)\n", 1000, mutableSetOf(1)),
                        SubmissionsNode(2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(2)),
                    )
                ))
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
                    .addEdge(0, 1, 3.0)
                    .build(),
                2.0,
                ClusteringImpl(listOf(
                    setOf(SubmissionsNode(1, "print(1)\n", 1000, mutableSetOf(1))),
                    setOf(SubmissionsNode(2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(2))),
                ))
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
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addIdList(3)
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                0.0,
                ClusteringImpl(listOf(
                    setOf(SubmissionsNode(1, "print(1)\n", 1000, mutableSetOf(1))),
                    setOf(SubmissionsNode(2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(2))),
                    setOf(SubmissionsNode(2, "v1 = 1\nprint(v1)\n", 1000, mutableSetOf(3))),
                ))
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
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addIdList(3)
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                1.0,
                ClusteringImpl(listOf(
                    setOf(
                        SubmissionsNode(1, "print(1)\n", 1000, mutableSetOf(1)),
                        SubmissionsNode(2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(2)),
                    ),
                    setOf(SubmissionsNode(2, "v1 = 1\nprint(v1)\n", 1000, mutableSetOf(3))),
                ))
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
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addIdList(3)
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                2.0,
                ClusteringImpl(listOf(
                    setOf(
                        SubmissionsNode(1, "print(1)\n", 1000, mutableSetOf(1)),
                        SubmissionsNode(2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(2)),
                    ),
                    setOf(SubmissionsNode(2, "v1 = 1\nprint(v1)\n", 1000, mutableSetOf(3))),
                ))
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
                    .addNode {
                        code = "v1 = 1\nprint(v1)\n"
                        addIdList(3)
                    }
                    .addEdge(0, 1, 1.0)
                    .addEdge(0, 2, 2.0)
                    .addEdge(1, 2, 3.0)
                    .build(),
                3.0,
                ClusteringImpl(listOf(
                    setOf(
                        SubmissionsNode(1, "print(1)\n", 1000, mutableSetOf(1)),
                        SubmissionsNode(2, "v1 = 1\nprint(1)\n", 1000, mutableSetOf(2)),
                        SubmissionsNode(2, "v1 = 1\nprint(v1)\n", 1000, mutableSetOf(3)),
                    )
                ))
            ),
        )

        @JvmStatic
        fun getTestDataFromBin(): List<Arguments> {
            val binDirName = "bin.data/"
            val classLoader = Thread.currentThread().contextClassLoader
            val binDir = classLoader.getResource(binDirName)?.toURI()?.let { Paths.get(it) }

            val submissionsGraphs = binDir?.toFile()?.walkTopDown()?.mapNotNull {
                (if (it.isSerializationFolder()) Paths.get(it.toURI()) else null)?.toSubmissionsGraph()
            }
                ?.toList()

            return submissionsGraphs?.map { graph ->
                (25..600 step 50).map { limit ->
                    Arguments.of(graph, limit)
                }
            }?.flatten() ?: listOf()
        }

        private fun File.isSerializationFolder(): Boolean {
            val graphSerName = "graph.bin"
            val clusterSerName = "clusters.bin"
            return this.isDirectory && File(this, graphSerName).exists() && File(this, clusterSerName).exists()
        }
    }
}
