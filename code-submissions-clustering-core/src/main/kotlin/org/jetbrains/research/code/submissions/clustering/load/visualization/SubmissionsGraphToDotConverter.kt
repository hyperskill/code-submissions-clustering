package org.jetbrains.research.code.submissions.clustering.load.visualization

import org.jetbrains.research.code.submissions.clustering.load.clustering.*
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jetbrains.research.code.submissions.clustering.util.*
import java.io.File

class SubmissionsGraphToDotConverter {
    fun SubmissionsGraph.toDot(): String {
        val stepId = graph.vertexSet().firstOrNull()?.stepId
        val dotRepresentation = graph.clustersToDot(getClusteredGraph())
        return buildDotGraph(dotRepresentation, stepId)
    }

    fun ClusteredGraph<SubmissionsNode>.toDot() = buildString {
        val stepId = graph.vertexSet().firstOrNull()?.entities
            ?.first()?.stepId
        val dotRepresentation = graph.toDot()
        return buildDotGraph(dotRepresentation, stepId)
    }

    private fun buildDotGraph(dotRepresentation: String, stepId: Int?) = buildString {
        appendLine("graph ${stepId ?: "?"} {")
        appendLine()
        appendLine(dotRepresentation)
        append("}")
    }

    private fun SubmissionsGraphAlias.clustersToDot(
        clusteredGraph: ClusteredGraph<SubmissionsNode>
    ): String {
        val vsb = StringBuilder()
        val csb = StringBuilder()
        val totalSubmissionsCnt = vertexSet().sumOf { it.idList.size }
        val clusters = clusteredGraph.graph.vertexSet()
        clusters.forEachIndexed { i, cluster ->
            val entities = cluster.entities
            if (entities.isEmpty()) {
                return@forEachIndexed
            }
            csb.appendLine("  subgraph cluster_$i {")
            csb.append("    ").appendLine(cluster.buildLabel(i))
            if (entities.size == 1) {
                val vertex = entities.first()
                vsb.append("  ").appendLine(vertexToDot(vertex, totalSubmissionsCnt))
                csb.append("    ").appendLine("v${vertex.id}").appendLine("  }")
                return@forEachIndexed
            }
            entities.sorted().forEach { first ->
                vsb.append("  ").appendLine(vertexToDot(first, totalSubmissionsCnt))
                entities.forEach { second ->
                    if (first.id < second.id) {
                        csb.append("    ").appendLine(edgeToDot(first, second))
                    }
                }
            }
            csb.appendLine("  }")
        }
        if (vsb.isEmpty()) {
            return ""
        }
        return listOf(vsb, csb).joinToString(separator = System.lineSeparator())
    }

    private fun ClusteredGraphAlias<SubmissionsNode>.toDot() = buildString {
        val clusters = vertexSet()
        appendLine("  subgraph {")
        append("    ").appendLine("node [shape = box]")
        if (clusters.size == 1) {
            append("    ").appendLine("C0")
        } else {
            clusters.forEachIndexed { i, first ->
                clusters.forEachIndexed { j, second ->
                    if (j > i) {
                        val edge = getEdge(first, second)
                        val weight = getEdgeWeight(edge).toInt()
                        append("    ").appendLine("C$i -- C$j [label = \"$weight\"]")
                    }
                }
            }
        }
        appendLine("  }")
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

    private fun Cluster<SubmissionsNode>.buildLabel(i: Int) = buildString {
        append("label = < <B>C$i</B>  [${entities.size} node")
        if (entities.size > 1) {
            append("s")
        }
        append("] >")
    }

    companion object {
        const val HUE_COEFFICIENT = 0.9
        const val LOWER_HUE_BOUND = 0.1
    }
}

fun SubmissionsGraph.visualizeDot(clustersOutputFile: File, structureOutputFile: File) {
    with(SubmissionsGraphToDotConverter()) {
        visualizeDot(clustersOutputFile, toDot())
        visualizeDot(structureOutputFile, getClusteredGraph().toDot())
    }
}

private fun visualizeDot(outputFile: File, dotRepresentation: String) {
    val basePath = getTmpProjectDir(toCreateFolder = false)
    val fileName = "graph.dot"
    val dotFile = addFileToProject(basePath, fileName, dotRepresentation)
    runProcessBuilder(Command(listOf("dot", "-Tpng", dotFile.absolutePath, "-o", outputFile.absolutePath)))
    dotFile.deleteFromProject()
}
