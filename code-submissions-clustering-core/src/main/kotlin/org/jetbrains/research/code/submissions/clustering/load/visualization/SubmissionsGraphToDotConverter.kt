package org.jetbrains.research.code.submissions.clustering.load.visualization

import org.jetbrains.research.code.submissions.clustering.load.clustering.Cluster
import org.jetbrains.research.code.submissions.clustering.load.clustering.Clusters
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClustersGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraphAlias
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsNode
import org.jetbrains.research.code.submissions.clustering.util.*
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.ClusteringImpl
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.graph.SimpleWeightedGraph
import java.io.File

class SubmissionsGraphToDotConverter {
    fun SubmissionsGraph.toDot() = buildString {
        val stepId = graph.vertexSet().firstOrNull()?.stepId ?: "?"
        appendLine("graph $stepId {")
        appendLine()
        append(clustering?.let {
            graph.clustersToDot(it, clustersGraph!!)
        } ?: run {
            val singleClusterGraph =
                SimpleWeightedGraph<Cluster<SubmissionsNode>, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)
            singleClusterGraph.addVertex(graph.vertexSet())
            graph.clustersToDot(ClusteringImpl(listOf(graph.vertexSet())), singleClusterGraph)
        })
        append("}")
    }

    private fun SubmissionsGraphAlias.clustersToDot(
        clustering: Clustering<SubmissionsNode>,
        clustersGraph: ClustersGraph<SubmissionsNode>
    ): String {
        val vsb = StringBuilder()
        val csb = StringBuilder()
        val cgsb = StringBuilder()
        val totalSubmissionsCnt = vertexSet().sumOf { it.idList.size }
        val clusters = clustering.clusters
        clusters.forEachIndexed { i, cluster ->
            if (cluster.isEmpty()) {
                return@forEachIndexed
            }
            csb.appendLine("  subgraph cluster_$i {")
            csb.append("    ").appendLine(cluster.buildLabel(i))
            if (cluster.size == 1) {
                val vertex = cluster.first()
                vsb.append("  ").appendLine(vertexToDot(vertex, totalSubmissionsCnt))
                csb.append("    ").appendLine("v${vertex.id}").appendLine("  }")
                return@forEachIndexed
            }
            cluster.forEach { first ->
                vsb.append("  ").appendLine(vertexToDot(first, totalSubmissionsCnt))
                cluster.forEach { second ->
                    if (first.id < second.id) {
                        csb.append("    ").appendLine(edgeToDot(first, second))
                    }
                }
            }
            csb.appendLine("  }")
        }
        cgsb.append(clustersGraph.toDot(clusters)).appendLine()
        if (vsb.isEmpty()) {
            return ""
        }
        return listOf(vsb, csb, cgsb).joinToString(separator = System.lineSeparator())
    }

    private fun ClustersGraph<SubmissionsNode>.toDot(clusters: Clusters<SubmissionsNode>) = buildString {
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
        append("label = < <B>C$i</B>  [$size node")
        if (size > 1) {
            append("s")
        }
        append("] >")
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
        println(dotRepresentation)
        val dotFile = addFileToProject(basePath, fileName, dotRepresentation)
        runProcessBuilder(Command(listOf("dot", "-Tpng", dotFile.absolutePath, "-o", outputFile.absolutePath)))
        dotFile.deleteFromProject()
    }
}
