package org.jetbrains.research.code.submissions.clustering.load.visualization

import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jetbrains.research.code.submissions.clustering.util.*
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.ClusteringImpl
import java.io.File

class SubmissionsGraphToDotConverter {
    fun SubmissionsGraph.toDot() = buildString {
        val stepId = graph.vertexSet().firstOrNull()?.stepId ?: "?"
        appendLine("graph $stepId {")
        appendLine()
        append(clustering?.let {
            graph.clustersToDot(it)
        } ?: graph.clustersToDot(ClusteringImpl(listOf(graph.vertexSet()))))
        append("}")
    }

    private fun SubmissionsGraphAlias.clustersToDot(clustering: Clustering<SubmissionsNode>): String {
        val vsb = StringBuilder()
        val csb = StringBuilder()
        val totalSubmissionsCnt = vertexSet().sumOf { it.idList.size }
        val clusters = clustering.clusters
        clusters.forEachIndexed { i, clusterVertices ->
            if (clusterVertices.isEmpty()) {
                return@forEachIndexed
            }
            csb.appendLine("  subgraph cluster_$i {")
            if (clusterVertices.size == 1) {
                val vertex = clusterVertices.first()
                vsb.append("  ").appendLine(vertexToDot(vertex, totalSubmissionsCnt))
                csb.append("    ").appendLine("v${vertex.id}").appendLine("  }")
                    .appendLine()
                return@forEachIndexed
            }
            clusterVertices.forEach { first ->
                vsb.append("  ").appendLine(vertexToDot(first, totalSubmissionsCnt))
                clusterVertices.forEach { second ->
                    if (first.id < second.id) {
                        csb.append("    ").appendLine(edgeToDot(first, second))
                    }
                }
            }
            csb.appendLine("  }").appendLine()
        }
        if (vsb.isEmpty()) {
            return ""
        }
        return listOf(vsb, csb).joinToString(separator = System.lineSeparator())
    }

    private fun vertexToDot(vertex: SubmissionsNode, totalSubmissionsCnt: Int): String {
        val color = calculateColor(vertex.idList.size, totalSubmissionsCnt)
        return "v${vertex.id} [label = \"v${vertex.id}\", style = filled, fillcolor = $color]"
    }

    private fun SubmissionsGraphAlias.edgeToDot(sourceVertex: SubmissionsNode, targetVertex: SubmissionsNode): String {
        val edge = getEdge(sourceVertex, targetVertex)
        val weight = getEdgeWeight(edge).toInt()
        return "v${sourceVertex.id} -- v${targetVertex.id} [label = \"$weight\"]"
    }

    private fun calculateColor(curIdsCnt: Int, totalIdsCnt: Int): String {
        val hue = LOWER_HUE_BOUND + HUE_COEFFICIENT * (curIdsCnt.toDouble() / totalIdsCnt)
        return "\"0.1 %.2f 1.0\"".format(hue)
    }

    companion object {
        const val HUE_COEFFICIENT = 0.9
        const val LOWER_HUE_BOUND = 0.1
    }
}

fun SubmissionsGraph.visualize(outputFile: File) {
    val basePath = getTmpProjectDir(toCreateFolder = false)
    val fileName = "graph.dot"
    with(SubmissionsGraphToDotConverter()) {
        val dotRepresentation = toDot()
        val dotFile = addFileToProject(basePath, fileName, dotRepresentation)
        runProcessBuilder(Command(listOf("dot", "-Tpng", dotFile.absolutePath, "-o", outputFile.absolutePath)))
        dotFile.deleteFromProject()
    }
}
