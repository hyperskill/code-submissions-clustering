package org.jetbrains.research.code.submissions.clustering.util

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.writeCSV
import org.jetbrains.research.code.submissions.clustering.load.context.SubmissionsGraphContext
import org.jetbrains.research.code.submissions.clustering.load.visualization.SubmissionsGraphVisualizer
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.transformGraph
import java.io.File

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
            calculateDistances()
        }
    }
    return graph
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

fun SubmissionsGraph.writeToCsv(outputPath: String) {
    val path = "$outputPath/graph.csv"
    toDataFrame().writeCSV(path)
}

fun SubmissionsGraph.writeToPng(outputPath: String) {
    val path = "$outputPath/graph.png"
    val file = File(path)
    val visualizer = SubmissionsGraphVisualizer()
    visualizer.visualize(this, file)
}
