package org.jetbrains.research.code.submissions.clustering.util

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.column
import org.jetbrains.kotlinx.dataframe.api.forEach
import org.jetbrains.kotlinx.dataframe.api.getValue
import org.jetbrains.research.code.submissions.clustering.load.AbstractUnifier
import org.jetbrains.research.code.submissions.clustering.model.Submission
import org.jetbrains.research.code.submissions.clustering.model.SubmissionsGraph
import org.jetbrains.research.code.submissions.clustering.model.buildGraph

@Suppress("VariableNaming")
fun DataFrame<*>.loadGraph(unifier: AbstractUnifier): SubmissionsGraph {
    val id by column<Int>()
    val step_id by column<Int>()
    val code by column<String>()
    val graph = let { dataFrame ->
        buildGraph(unifier) {
            dataFrame.forEach {
                add(
                    Submission(
                        id = getValue(id),
                        stepId = getValue(step_id),
                        code = getValue(code)
                    )
                )
            }
        }
    }
    return graph
}
