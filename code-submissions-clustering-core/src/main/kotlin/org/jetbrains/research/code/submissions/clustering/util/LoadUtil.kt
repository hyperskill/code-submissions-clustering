package org.jetbrains.research.code.submissions.clustering.util

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.toCsv
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import org.jetbrains.research.code.submissions.clustering.load.clustering.ClusteredGraph
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.visualization.visualizeDot
import org.jetbrains.research.code.submissions.clustering.model.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPOutputStream

@Suppress("VariableNaming")
fun <T> DataFrame<*>.loadGraph(context: SubmissionsGraphContext<T>): SubmissionsGraph {
    val id by column<Int>()
    val step_id by column<Int>()
    val code by column<String>()
    val graph = let { dataFrame ->
        transformGraph(context) {
            dataFrame.forEach {
                add(
                    Submission(
                        id = getValue(id),
                        stepId = getValue(step_id),
                        code = getValue(code)
                    )
                )
            }
            // We don't share it with distances, so we can remove extra information
            context.unifier.clearCache()

            calculateDistances()
        }
    }
    return graph
}

private fun SubmissionsGraph.toClusteringDataFrame(): DataFrame<*> {
    val submissions = mutableListOf<Int>()
    val clusters = mutableListOf<Int>()
    val positions = mutableListOf<Int>()
    getClusteredGraph().graph.vertexSet().forEach { cluster ->
        var currentPosition = 0
        cluster.entities.forEach { submissionsNode ->
            submissionsNode.idList.forEach { submissionId ->
                submissions.add(submissionId)
                clusters.add(cluster.id)
                positions.add(currentPosition++)
            }
        }
    }
    return dataFrameOf(
        submissions.toColumn() named "submission_id",
        clusters.toColumn() named "cluster_id",
        positions.toColumn() named "position"
    )
}

fun SubmissionsGraph.writeClusteringResult(outputPath: String) {
    val path = "$outputPath/clustering.csv.gz"
    val file = File(path)
    file.createNewFile()

    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use {
        it.write(toClusteringDataFrame().toCsv())
    }
    file.writeBytes(bos.toByteArray())
}

fun SubmissionsGraph.toDataFrame(): DataFrame<*> {
    val vertices = graph.vertexSet()
    val code = vertices.map { it.code }.toColumn() named "code"
    val idList = vertices.map { it.idList }.toColumn() named "idList"
    return dataFrameOf(code, idList)
}

fun SubmissionsGraph.writeToString(outputPath: String) {
    val path = "$outputPath/graph.txt"
    val file = File(path)
    file.writeText(buildStringRepresentation())
}

fun SubmissionsGraph.writeToBinary(outputPath: String) {
    val path = "$outputPath/graph.bin"
    val file = File(path)
    toProto().writeTo(file.outputStream())
}

fun ClusteredGraph<SubmissionsNode>.writeToBinary(outputPath: String) {
    val path = "$outputPath/clusters.bin"
    val file = File(path)
    toProto().writeTo(file.outputStream())
}

fun SubmissionsGraph.writeToCsv(outputPath: String) {
    val path = "$outputPath/graph.csv"
    toDataFrame().writeCSV(path)
}

fun SubmissionsGraph.writeToPng(outputPath: String) {
    val clustersFilePath = "$outputPath/clusters.png"
    val clustersFile = File(clustersFilePath)
    val structureFilePath = "$outputPath/structure.png"
    val structureFile = File(structureFilePath)
    visualizeDot(clustersFile, structureFile)
}

fun SubmissionsGraph.writeClusters(outputPath: String) {
    val path = "$outputPath/clusters.txt"
    val file = File(path)
    file.createNewFile()
    file.writeText(getClusteredGraph().toString())
}
