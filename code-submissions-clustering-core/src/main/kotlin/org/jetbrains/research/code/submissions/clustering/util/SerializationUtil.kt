package org.jetbrains.research.code.submissions.clustering.util

import org.jetbrains.research.code.submissions.clustering.ProtoCluster
import org.jetbrains.research.code.submissions.clustering.ProtoClusterEdge
import org.jetbrains.research.code.submissions.clustering.ProtoClusteredGraph
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsEdge
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsNode
import org.jetbrains.research.code.submissions.clustering.load.clustering.Cluster
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClusteredGraph
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClusteredGraphAlias
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClusteredGraphEdge
import org.jetbrains.research.code.submissions.clustering.load.context.builder.Identifier
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphEdge
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jgrapht.Graph
import org.jgrapht.graph.SimpleWeightedGraph

fun SubmissionsNode.toProto(): ProtoSubmissionsNode = let { node ->
    ProtoSubmissionsNode.newBuilder().apply {
        id = node.id
        code = node.code
        stepId = node.stepId
        addAllIdList(idList)
    }.build()
}

fun ProtoSubmissionsNode.toNode(): SubmissionsNode =
    SubmissionsNode(id, code, stepId, idListList.toMutableSet())

fun SubmissionsGraph.toProto(): ProtoSubmissionsGraph =
    ProtoSubmissionsGraph.newBuilder().apply {
        val serializedNodes = mutableMapOf<SubmissionsNode, ProtoSubmissionsNode>()

        addAllVertices(graph.vertexSet().map {
            serializedNodes.computeIfAbsent(it, SubmissionsNode::toProto)
        })

        addAllEdges(graph.edgeSet().map {
            it.toProto(graph, serializedNodes)
        })
    }.build()

fun ProtoSubmissionsGraph.toGraph(): SubmissionsGraph {
    val graph = SimpleWeightedGraph<SubmissionsNode, SubmissionsGraphEdge>(SubmissionsGraphEdge::class.java)
    val deserializedNodes = mutableMapOf<ProtoSubmissionsNode, SubmissionsNode>()
    verticesList.forEach {
        graph.addVertex(
            deserializedNodes.computeIfAbsent(it, ProtoSubmissionsNode::toNode)
        )
    }

    edgesList.forEach { edge ->
        val addedEdge = graph.addEdge(
            deserializedNodes[edge.from],
            deserializedNodes[edge.to]
        )
        addedEdge?.let {
            graph.setEdgeWeight(it, edge.weight)
        }
    }

    return SubmissionsGraph(graph)
}

fun Cluster<SubmissionsNode>.toProto(): ProtoCluster = let { cluster ->
    ProtoCluster.newBuilder().apply {
        id = cluster.id
        addAllNodes(cluster.entities.map { it.toProto() })
    }.build()
}

fun ProtoCluster.toCluster(): Cluster<SubmissionsNode> =
    Cluster(id, nodesList.mapTo(mutableListOf()) { it.toNode() })

fun ClusteredGraph<SubmissionsNode>.toProto(): ProtoClusteredGraph =
    ProtoClusteredGraph.newBuilder().apply {
        addAllClusters(graph.vertexSet().map { it.toProto() })
        addAllEdges(graph.edgeSet().map { it.toProto(graph) })
    }.build()

fun ProtoClusteredGraph.toGraph(): ClusteredGraph<SubmissionsNode> {
    val graph = SimpleWeightedGraph<Cluster<SubmissionsNode>, ClusteredGraphEdge>(ClusteredGraphEdge::class.java)
    val idToCluster = mutableMapOf<Identifier, Cluster<SubmissionsNode>>()
    clustersList.forEach { cluster ->
        graph.addVertex(
            idToCluster.computeIfAbsent(cluster.id) { cluster.toCluster() }
        )
    }

    edgesList.forEach { edge ->
        val addedEdge = graph.addEdge(
            idToCluster[edge.fromClusterId],
            idToCluster[edge.toClusterId]
        )
        addedEdge?.let {
            graph.setEdgeWeight(it, edge.weight)
        }
    }

    return ClusteredGraph(graph)
}

private fun SubmissionsGraphEdge.toProto(
    graph: Graph<SubmissionsNode, SubmissionsGraphEdge>,
    serializedNodes: Map<SubmissionsNode, ProtoSubmissionsNode>
): ProtoSubmissionsEdge = let { edge ->
    ProtoSubmissionsEdge.newBuilder().apply {
        from = serializedNodes[graph.getEdgeSource(edge)]
        to = serializedNodes[graph.getEdgeTarget(edge)]
        weight = graph.getEdgeWeight(edge)
    }.build()
}

private fun ClusteredGraphEdge.toProto(
    graph: ClusteredGraphAlias<SubmissionsNode>
): ProtoClusterEdge = let { edge ->
    ProtoClusterEdge.newBuilder().apply {
        fromClusterId = graph.getEdgeSource(edge).id
        toClusterId = graph.getEdgeTarget(edge).id
        weight = graph.getEdgeWeight(edge)
    }.build()
}
