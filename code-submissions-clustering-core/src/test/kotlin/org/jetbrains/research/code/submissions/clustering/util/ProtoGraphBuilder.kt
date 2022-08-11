package org.jetbrains.research.code.submissions.clustering.util

import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsEdge
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.ProtoSubmissionsNode
import kotlin.properties.Delegates

@Suppress("EMPTY_PRIMARY_CONSTRUCTOR")
class ProtoGraphBuilder() {
    private var edges = mutableListOf<ProtoSubmissionsEdge>()
    private var nodes = HashMap<Int, ProtoSubmissionsNode>()
    private var nextNode = 0
    private var stepId by Delegates.notNull<Int>()

    constructor(stepId: Int) : this() {
        this.stepId = stepId
    }

    fun addNode(block: ProtoSubmissionsNode.Builder.() -> Unit): ProtoGraphBuilder {
        val newNode = ProtoSubmissionsNode.newBuilder()
            .setStepId(stepId)
            .apply(block)
            .build()
        nodes[nextNode++] = newNode
        return this
    }

    fun addEdge(src: Int, dst: Int, weight: Double): ProtoGraphBuilder {
        val newEdge1 = ProtoSubmissionsEdge.newBuilder().apply {
            this.from = nodes[src]
            this.to = nodes[dst]
            this.weight = weight
        }.build()
        val newEdge2 = ProtoSubmissionsEdge.newBuilder().apply {
            this.to = nodes[src]
            this.from = nodes[dst]
            this.weight = weight
        }.build()
        edges.add(newEdge1)
        edges.add(newEdge2)
        return this
    }

    fun build(): ProtoSubmissionsGraph =
        ProtoSubmissionsGraph.newBuilder().apply {
            addAllVertices(nodes.values)
            addAllEdges(edges)
        }.build()
}
