package org.jetbrains.research.code.submissions.clustering.util

import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsEdge
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsNode
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleDirectedWeightedGraph

fun SubmissionsNode.toProto(): ProtoSubmissionsNode = let { node ->
    ProtoSubmissionsNode.newBuilder().apply {
        code = node.code
        stepId = node.stepId
        addAllIdList(idList)
    }.build()
}

fun ProtoSubmissionsNode.toNode(): SubmissionsNode =
    SubmissionsNode(code, stepId, idListList.toMutableSet())

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
    val graph = SimpleDirectedWeightedGraph<SubmissionsNode, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)
    val deserializedNodes = mutableMapOf<ProtoSubmissionsNode, SubmissionsNode>()
    verticesList.forEach {
        graph.addVertex(
            deserializedNodes.computeIfAbsent(it, ProtoSubmissionsNode::toNode)
        )
    }

    edgesList.forEach {
        val edge = graph.addEdge(
            deserializedNodes[it.from],
            deserializedNodes[it.to]
        )
        graph.setEdgeWeight(edge, it.weight)
    }

    return SubmissionsGraph(graph)
}

private fun DefaultWeightedEdge.toProto(
    graph: Graph<SubmissionsNode, DefaultWeightedEdge>,
    serializedNodes: Map<SubmissionsNode, ProtoSubmissionsNode>
): ProtoSubmissionsEdge = let { edge ->
    ProtoSubmissionsEdge.newBuilder().apply {
        from = serializedNodes[graph.getEdgeSource(edge)]
        to = serializedNodes[graph.getEdgeTarget(edge)]
        weight = graph.getEdgeWeight(edge)
    }.build()
}
