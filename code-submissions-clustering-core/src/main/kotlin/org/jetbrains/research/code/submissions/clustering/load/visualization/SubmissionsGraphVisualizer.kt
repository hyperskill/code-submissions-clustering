package org.jetbrains.research.code.submissions.clustering.load.visualization

import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jetbrains.research.code.submissions.clustering.util.addFileToProject
import org.jetbrains.research.code.submissions.clustering.util.deleteFromProject
import org.jetbrains.research.code.submissions.clustering.util.getTmpProjectDir
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering
import java.io.File

class SubmissionsGraphVisualizer {
    fun visualize(submissionsGraph: SubmissionsGraph, outputFile: File) {
        val basePath = getTmpProjectDir(toCreateFolder = false)
        val fileName = "graph.dot"
        val dotRepresentation = toDot(submissionsGraph)
        val dotFile = addFileToProject(basePath, fileName, dotRepresentation)
        val cmd = "dot -Tpng $dotFile -o $outputFile"
        val process = Runtime.getRuntime().exec(cmd)
        process.waitFor()
        dotFile.deleteFromProject()
    }

    fun toDot(submissionsGraph: SubmissionsGraph): String {
        val stepId = submissionsGraph.graph.vertexSet().firstOrNull()?.stepId ?: "?"
        var dotRepresentation = "graph $stepId {\n"
        dotRepresentation += submissionsGraph.graph.verticesToDot()
        dotRepresentation += submissionsGraph.clustering?.let {
            submissionsGraph.graph.clustersToDot(it)
        } ?: submissionsGraph.graph.edgesToDot()
        dotRepresentation += "\n}"
        return dotRepresentation
    }

    private fun SubmissionsGraphAlias.verticesToDot(): String {
        var dotRepresentation = ""
        val vertices = vertexSet()
        val totalSubmissionsCnt = vertices.sumOf { it.idList.size }
        vertices.forEach {
            dotRepresentation += "\n  v${it.id} [label = \"v${it.id}\", style = filled, " +
                "fillcolor = ${calculateColor(it.idList.size, totalSubmissionsCnt)}]"
        }
        if (dotRepresentation.isNotEmpty()) {
            dotRepresentation += "\n"
        }
        return dotRepresentation
    }

    private fun SubmissionsGraphAlias.edgesToDot(): String {
        var dotRepresentation = ""
        val vertices = vertexSet()
        vertices.forEach { first ->
            vertices.forEach { second ->
                if (first.id < second.id) {
                    val weight = getEdgeWeight(getEdge(first, second)).toInt()
                    dotRepresentation +=
                        "\n  v${first.id} -- v${second.id} [label = \"$weight\"]"
                }
            }
        }
        if (dotRepresentation.isNotEmpty()) {
            dotRepresentation += "\n"
        }
        return dotRepresentation
    }

    private fun SubmissionsGraphAlias.clustersToDot(clustering: Clustering<SubmissionsNode>): String {
        var dotRepresentation = ""
        val clusters = clustering.clusters
        clusters.forEachIndexed { i, clusterVertices ->
            dotRepresentation += "  subgraph cluster_$i {"
            if (clusterVertices.size == 1) {
                dotRepresentation += "\n    v${clusterVertices.first().id}\n  }\n\n"
                return@forEachIndexed
            }
            clusterVertices.forEach { first ->
                clusterVertices.forEach { second ->
                    if (first.id < second.id) {
                        dotRepresentation +=
                            "\n    v${first.id} -- v${second.id} " +
                                "[label = ${getEdgeWeight(getEdge(first, second)).toInt()}]"
                    }
                }
            }
            dotRepresentation += "\n  }\n\n"
        }
        return dotRepresentation
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
