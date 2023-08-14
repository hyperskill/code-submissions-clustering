package org.jetbrains.research.code.submissions.clustering.util

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.toCsv
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.visualization.visualizeDot
import org.jetbrains.research.code.submissions.clustering.model.*
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.pathString

@Suppress("VariableNaming")
fun <T> DataFrame<*>.loadGraph(context: SubmissionsGraphContext<T>): SubmissionsGraph {
    val id by column<Int>()
    val step_id by column<Int>()
    val code by column<String>()
    val quality by column<Int>()
    val graph = let { dataFrame ->
        transformGraph(context) {
            dataFrame.forEach {
                add(
                    Submission(
                        info = SubmissionInfo(getValue(id), getValue(quality)),
                        stepId = getValue(step_id),
                        code = getValue(code).normalize()
                    )
                )
            }
            // We don't share it with distances, so we can remove extra information
            context.unifier.clear()
            calculateDistances()
            // Caches might be controlled by external processes, so we should clear them explicitly
            context.codeDistanceMeasurer.clear()
        }
    }
    return graph
}

fun SubmissionsGraph.writeClusteringResult(outputPath: String) {
    val path = Path(outputPath) / "clustering.csv.gz"
    val file = path.toFile()
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
    val idList = vertices.map { it.submissionsList.map { info -> info.id } }.toColumn() named "idList"
    return dataFrameOf(code, idList)
}

fun SubmissionsGraph.writeToTxt(outputPath: String) {
    val txtFolder = Path(outputPath) / "txt"
    createFolder(txtFolder)
    val path = txtFolder / "graph.txt"
    val file = path.toFile()
    file.writeText(buildStringRepresentation())
}

fun SubmissionsGraph.writeToBinary(outputPath: String) {
    val serializationFolder = Path(outputPath) / "serialization"
    createFolder(serializationFolder)
    val graphFilePath = serializationFolder / "graph.bin"
    val graphFile = graphFilePath.toFile()
    toProto().writeTo(graphFile.outputStream())
    val clustersFilePath = serializationFolder / "clusters.bin"
    val clustersFile = clustersFilePath.toFile()
    getClusteredGraph().toProto().writeTo(clustersFile.outputStream())
}

fun SubmissionsGraph.writeToCsv(outputPath: String) {
    val path = Path(outputPath) / "graph.csv"
    toDataFrame().writeCSV(path.pathString)
}

fun SubmissionsGraph.writeToPng(outputPath: String) {
    val visualizationFolder = Path(outputPath) / "visualization"
    createFolder(visualizationFolder)
    val clustersFilePath = visualizationFolder / "clusters.png"
    val clustersFile = clustersFilePath.toFile()
    val structureFilePath = visualizationFolder / "structure.png"
    val structureFile = structureFilePath.toFile()
    visualizeDot(clustersFile, structureFile)
}

fun SubmissionsGraph.writeClustersToTxt(outputPath: String) {
    val txtFolder = Path(outputPath) / "txt"
    createFolder(txtFolder)
    val path = txtFolder / "clusters.txt"
    val file = path.toFile()
    file.createNewFile()
    file.writeText(getClusteredGraph().toString())
}

fun SubmissionsGraph.toClusteringDataFrame(): DataFrame<*> {
    val submissions = mutableListOf<Int>()
    val clusters = mutableListOf<Int>()
    val positions = mutableListOf<Int>()
    getClusteredGraph().graph.vertexSet().forEach { cluster ->
        cluster.entities.flatMap { it.submissionsList }.sorted().forEachIndexed { index, submissionInfo ->
            submissions.add(submissionInfo.id)
            clusters.add(cluster.id)
            positions.add(index)
        }
    }
    return dataFrameOf(
        submissions.toColumn() named "submission_id",
        clusters.toColumn() named "cluster_id",
        positions.toColumn() named "position"
    ).sortBy("submission_id")
}
