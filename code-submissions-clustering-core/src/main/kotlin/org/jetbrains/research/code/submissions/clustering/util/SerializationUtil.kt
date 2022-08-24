package org.jetbrains.research.code.submissions.clustering.util

import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsEdge
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsNode
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
